
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


configFilePath = "/etc/amazert.json"
##TODO . This is for local testing. The file should be in /etc along with other configs. 
#configFilePath = "/tmp/amazert.json"

"""
Settings that can be controlled via AmazeRT
This is used for data driven control instead of writing specific code for each setting
General format
[
    {
        name : settingname,
        handler:  {
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
dataDrivenSettingsRules = [ {
        "name" : "system.@system[0].hostname",
        "handler" : {
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
Helper function to run a command in shell and collect the output. 
to be used only for reading and writing settings
@param is an array of strings which form the command line
"""
def runShellcommand(command):
    resultString = ""
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
    return resultString.strip()

""" 
Prepare a list of all the settings which will be sent across to the AmazeRT App Engine
""" 
def getAllSupportedSettings():
    allSettings = []
    for rule in dataDrivenSettingsRules:
        setting = {}
        setting['name'] = rule['name']
        if (rule['handler']['commandType'] == "uci"):
            setting['value'] = runShellcommand(["uci", "get", setting["name"]])
            print ("Setting " + setting["name"] + "=" + setting['value'])
            allSettings.append(setting)
    return allSettings

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
        self.sendAllConfiguration()
        while (keepRunning == True):
            self.sendHeartbeat()
            time.sleep(heartBeatIntervalInSeconds)

    def sendAllConfiguration(self):
        message = { "action" : "register"}
        allSettings = getAllSupportedSettings()
        message["settings"] = allSettings
        packettoSend = preparePacketToSend(self.config, message)
        print(f"> {packettoSend}")
        self.websocket.send(packettoSend)

    def sendHeartbeat(self):
        message = { "action" : "heartbeat"}
        packettoSend = preparePacketToSend(self.config, message)
        print(f"> {packettoSend}")
        self.websocket.send(packettoSend)

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
Loads and retuns the current Registration Configuration if any
"""
def loadCurrentRegistration():
    try:
        configFile = open(configFilePath)
        currentRegistration = json.load(configFile)
        if ((currentRegistration['email']) and (currentRegistration['deviceId']) and (currentRegistration['registrationId'])):
            return currentRegistration
        return None
    except Exception:
        print("AmazeRT Config json does not exist or is invalid")
    return None

"""
Main for the service. Connects to the Cloud App Engine, Initialize local handlers 
and keep listening to requests
"""
async def amazeRTServiceMain():
    uri = "ws://localhost:6789"
    config = loadCurrentRegistration()
    if (config is None):
        print("AmazeRT is not configured on this machine. please run initial configuration using init.py")
        exit(-1)
    
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



