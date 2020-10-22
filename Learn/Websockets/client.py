#!/usr/bin/env python

# WS client example

import asyncio
import pathlib
import ssl
import websockets

ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
localhost_pem = pathlib.Path(__file__).with_name("cert.pem")
ssl_context.load_verify_locations(localhost_pem)

async def hello():
    # un comment the below line , and send any message from
    # http://amaze-id1.appspot.com
    # uri = "ws://amaze-id1.wl.r.appspot.com:80/chat"
    uri = "wss://localhost:8765"
    async with websockets.connect(
        uri, ssl=ssl_context
    ) as websocket:
        keepRunning = True
        while (keepRunning == True):
            message = input(" Enter the message (\"exit\" to stop) :")

            await websocket.send(message)
            print(f"> {message}")

            reply = await websocket.recv()
            print(f"< {reply}")
            if (message == "exit"):
                keepRunning = False

asyncio.get_event_loop().run_until_complete(hello())