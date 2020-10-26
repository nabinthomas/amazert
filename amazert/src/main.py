
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

class amazeRTHeartBeatThread(threading.Thread):
    def __init__(self, config, websocket):
        threading.Thread.__init__(self)
        self.websocket = websocket
        self.config = config
    def run(self):
        while (keepRunning == True):
            self.sendHeartbeat()
            time.sleep(10)
    def sendHeartbeat(self):
        message = '{ "action" : "register"}'
        print(f"> {message}")
        self.websocket.send(message)

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
    
    responseJson = { "action" : "response",
                     "response" : resultString,
                     "code" : responseCode 
                    }
    await websocket.send(json.dumps(responseJson))

async def amazerRTSettingHandler(config, websocket, setting, options):
    print ("Applying setting : > " + json.dumps(setting))

async def amazerRTControlHandler(config, websocket, control, options):
    print ("Responding to control : > " + json.dumps(control))
    if control == "exit":
        keepRunning = False

actionHandlerMappings = {
    "command" : amazeRTCommandHandler, 
    "setting" : amazerRTSettingHandler,
    "control"  : amazerRTControlHandler
 }

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
            await actionHandler(websocket, actionParams, options)
        except KeyError:
            print ("Unsupported Action")
        if (message == "exit"):
            keepRunning = False

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



