
#!/usr/bin/env python

# WS client example

import asyncio
import pathlib
#import ssl
import websockets
import time

keepRunning = True

async def amazeRTHeartbeat(websocket):
    while (keepRunning == True):
        message = '{ "action" : "register"}'
        print(f"> {message}")
        await websocket.send(message)
        #time.sleep(10)
        asyncio.get_event_loop().call_later(10, )
        #asyncio.sleep(10)

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
        await asyncio.gather(
            amazeRTCommandHandler(websocket)#,
            #amazeRTHeartbeat(websocket)
            )
            

print ("Starting amazeRT Management system\n")

asyncio.get_event_loop().run_until_complete(amazeRTServiceMain())



