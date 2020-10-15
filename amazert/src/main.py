
#!/usr/bin/env python

# WS client example

import asyncio
import pathlib
import ssl
import threading
import websockets
import time
import json

keepRunning = True    

class amazeRTHeartBeatThread(threading.Thread):
    def __init__(self, websocket):
        threading.Thread.__init__(self)
        self.websocket = websocket
    def run(self):
        while (keepRunning == True):
            self.sendHeartbeat()
            time.sleep(10)
    def sendHeartbeat(self):
        message = '{ "action" : "register"}'
        print(f"> {message}")
        self.websocket.send(message)

def amazeRTCommandHandler(websocket, command, options):
    print ("Executing command : > " + json.dumps(command))

def amazerRTSettingHandler(websocket, setting, options):
    print ("Applying setting : > " + json.dumps(setting))

def amazerRTControlHandler(websocket, control, options):
    print ("Responding to control : > " + json.dumps(control))
    if control == "exit":
        keepRunning = False

actionHandlerMappings = {
    "command" : amazeRTCommandHandler, 
    "setting" : amazerRTSettingHandler,
    "control"  : amazerRTControlHandler
 }

async def amazeRTActionHandler(websocket):
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
            actionHandler(websocket, actionParams, options)
        except KeyError:
            print ("Unsupported Action")
        if (message == "exit"):
            keepRunning = False

async def amazeRTServiceMain():
    uri = "ws://localhost:6789"
    async with websockets.connect(
        uri #, ssl=ssl_context
    ) as websocket:
        hearbeatThread = amazeRTHeartBeatThread(websocket)
        hearbeatThread.start()
        await asyncio.gather(
           amazeRTActionHandler(websocket)
        )
        hearbeatThread.join()
            

print ("Starting amazeRT Management system\n")

asyncio.get_event_loop().run_until_complete(amazeRTServiceMain())



