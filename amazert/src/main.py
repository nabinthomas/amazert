
#!/usr/bin/env python

# WS client example

import asyncio
import pathlib
import ssl
import threading
import websockets
import time

keepRunning = True

def amazeRTHeartbeat(websocket):
    #while (keepRunning == True):
    message = '{ "action" : "register"}'
    print(f"> {message}")
    websocket.send(message)
    

class amazeRTHeartBeatThread(threading.Thread):
    def __init__(self, websocket):
        threading.Thread.__init__(self)
        self.websocket = websocket
    def run(self):
        while (keepRunning == True):
            amazeRTHeartbeat(self.websocket)    
            time.sleep(10)

async def amazeRTCommandHandler(websocket):
    print ("amazeRTCommandHandler start ")
    async for message in websocket:
        print ("In Loop to receive")
        #reply = await websocket.recv()
        print(f"< {message}")
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
           amazeRTCommandHandler(websocket)
        )
        hearbeatThread.join()
            

print ("Starting amazeRT Management system\n")

asyncio.get_event_loop().run_until_complete(amazeRTServiceMain())



