#!/usr/bin/env python

# WS server example

import asyncio
import websockets
import pathlib
import ssl

async def hello(websocket, path):
    keepRunning = True;
    while (keepRunning == True):
        name = await websocket.recv()
        print(f"< {name}")
        if (name == "exit"):
            keepRunning = False
            greeting = f"Bye!"
        else:
            greeting = f" Message Received : \"{name}\""

        await websocket.send(greeting)
        print(f"> {greeting}")

ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
localhost_cert = pathlib.Path(__file__).with_name("cert.pem")
localhost_key = pathlib.Path(__file__).with_name("key.pem")
ssl_context.load_verify_locations(localhost_cert, localhost_key)

ssl_context.load_cert_chain(localhost_cert, localhost_key)

start_server = websockets.serve(hello, "0.0.0.0", 8765, ssl=ssl_context)

asyncio.get_event_loop().run_until_complete(start_server)
asyncio.get_event_loop().run_forever()