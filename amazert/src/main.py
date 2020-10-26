
#!/usr/bin/env python

# WS client example

import asyncio
import pathlib
import ssl
import threading
import websockets
import time
import json
import subprocess

keepRunning = True    

"""
Settings that can be controlled via AmazeRT
This is used for data driven control instead of writing specific code for each setting
General format
[
    {
        name : {
            value : currentValue,
            filter : {
                possibleValues : [ list of possible values that are valid for this. 
                                If this field is not present any value is accepted. 
                                If this field is present and empty then the field is read Only
                                ],
                commandMappings : {
                    value1 : [ commands to run in sequence to set this value. 
                            This may be a shell script too. 
                            This is valid only for settings with restricted set of possibleValues
                        ]
                },
            },
            write : {
                prologue : [commands to be run before applying this setting. Optional. eg: turn of wifi before changing something]
                commandType : < uci -> will run uci set <name>=<value>; uci commit>,
                epilogue : [ commands to be run after applying this setting. Optional. eg: wifi on after applying settings/ wifi restart etc]
            }, 
            read : {
                prologue : [commands to be run before reading this setting. Optional. ]
                commandType : < uci -> will run uci set <name>=<value>; uci commit>,
                epilogue : [ commands to be run after reading this setting. Optional. ]
            }
            
        },
        ...
    }
]
"""
dataDrivenSettings = [ {
        "system.@system[0].hostname" : {
            "commandType" : "uci", 
            "read" : { 
                "commandType" : "uci" 
            }, 
            "write" : {
                "commandType" : "uci"
            }
        }
    }
]


"""
How frequent the heartbeat is sent to the server to keep the connection active.
""" 
heartBeatIntervalInSeconds = 30

"""
Prepares a packet to send. 
Every packet will need to be added with some extra information about the 
identification of the device being controlled/reported. This is a wrapper for generating
and updating that data

@param config - JSON Object holding the device configuration (ID data)
@param dataToSend - JSON Object holding the data to be sent

@return - JSON Object with config data added to dataToSend

"""
def preparePacketToSend(config, dataToSend):
    jsonPacket = dataToSend
    jsonPacket['identifier'] = config
    return jsonPacket

"""
Thread implementing Heartbeat support. This sends all the current settings to the Cloud server once on boot
and then keeps sending a hearbeat message at heartBeatIntervalInSeconds
"""
class amazeRTHeartBeatThread(threading.Thread):
    def __init__(self, config, websocket):
        threading.Thread.__init__(self)
        self.websocket = websocket
        self.config = config

    def run(self):
        while (keepRunning == True):
            self.sendHeartbeat()
            time.sleep(heartBeatIntervalInSeconds)

    def sendAllConfiguration(self):
        message = { "action" : "register"}
        print(f"> {message}")
        self.websocket.send(preparePacketToSend(self.config, message))

    def sendHeartbeat(self):
        message = { "action" : "heartbeat"}
        print(f"> {message}")
        self.websocket.send(preparePacketToSend(self.config, message))

"""
Handles a command sent from the cloud.
Once the processing is complete, a response is sent back to the server
in the format like 
                {   
                     "action" : "response",
                     "response" : resultString, # output of the command executed.
                     "code" : responseCode 
                }
@note identifier is always added to the reply to server

@param config - Identification for this device. 
@param websocket - used for communicating with the server
@command array of command + command line params
@options Special options to run the command. Unused for now. Added for future enhancements
"""
async def amazeRTCommandHandler(config, websocket, command, options):
    print ("Executing command : > " + json.dumps(command))
    # Send a response.
    resultString = ""
    responseCode = 0
    try:
        commandProcess = subprocess.Popen(command,
            stdin = subprocess.PIPE, 
            stdout = subprocess.PIPE,
            stderr = subprocess.PIPE,
            universal_newlines = True,
            bufsize = 0)
        commandProcess.stdin.close() # Command requiring additional input is not supported
        for line in commandProcess.stdout:
            resultString = resultString + line
    except Exception as e:
        resultString = str(e)
        responseCode = -1
    
    responseJson = {
                     "action" : "response",
                     "response" : resultString,
                     "code" : responseCode 
                    }

    responseJson = preparePacketToSend(config, responseJson)
    await websocket.send(json.dumps(responseJson))

"""
Handle a request to apply a particular setting
@param config - Identification for this device. 
@param websocket - used for communicating with the server
@setting name and value for the setting to be applied
@options Special options to run the command. Unused for now. Added for future enhancements

"""
async def amazerRTSettingHandler(config, websocket, setting, options):
    print ("Applying setting : > " + json.dumps(setting))

"""
Handle a request to control the amazeRT SW
@param config - Identification for this device. 
@param websocket - used for communicating with the server
@control control information
@options Special options to run the command. Unused for now. Added for future enhancements

"""
async def amazerRTControlHandler(config, websocket, control, options):
    print ("Responding to control : > " + json.dumps(control))
    if control == "exit":
        keepRunning = False

"""
Used to map the action to a function that can handle that kind of action
"""
actionHandlerMappings = {
    "command" : amazeRTCommandHandler, 
    "setting" : amazerRTSettingHandler,
    "control"  : amazerRTControlHandler
 }

"""
Entry point for any request that comes to the amazeRT management service

@note - Requests will be handled only if the identifier in the request messsage from the websocket
matches the local configuration in config

@param config - Identification for this device. 
@param websocket - used for communicating with the server

"""
async def amazeRTActionHandler(config, websocket):
    print ("amazeRTActionHandler start ")
    async for message in websocket:
        print(f"< {message}")
        request = json.loads(message)
        try:
            action = request["action"]
            actionParams = request[action]
            try:
                options = request["options"]
            except KeyError:
                options = {}
            actionHandler = actionHandlerMappings[action]
            await actionHandler(config, websocket, actionParams, options)
        except KeyError:
            print ("Unsupported Action > " )
        if (message == "exit"):
            keepRunning = False

"""
Main for the service. Connects to the Cloud App Engine, Initialize local handlers 
and keep listening to requests
"""
async def amazeRTServiceMain():
    uri = "ws://localhost:6789"
    config = {}
    #uri = "ws://amaze-id1.wl.r.appspot.com/chat"
    async with websockets.connect(
        uri #, ssl=ssl_context
    ) as websocket:
        hearbeatThread = amazeRTHeartBeatThread(config, websocket)
        hearbeatThread.start()
        await asyncio.gather(
           amazeRTActionHandler(config, websocket)
        )
        hearbeatThread.join()
            

print ("Starting amazeRT Management system\n")

asyncio.get_event_loop().run_until_complete(amazeRTServiceMain())



