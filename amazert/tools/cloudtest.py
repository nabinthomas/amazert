#!/usr/bin/env python

# WS server example that synchronizes state across clients
# Based on : https://websockets.readthedocs.io/en/stable/intro.html#synchronization-example
# This is being developed as a test tool right now.


import asyncio
import json
import logging
import websockets

logging.basicConfig()

STATE = {"value": 0}

allDevices = set()


def state_event():
    return json.dumps({"type": "state", **STATE})


def users_event():
    return json.dumps({"type": "users", "count": len(allDevices)})


async def notify_state():
    if allDevices:  # asyncio.wait doesn't accept an empty list
        message = state_event()
        await asyncio.wait([device.send(message) for device in allDevices])

async def forward_message(message):
    print ("forwarding + ", message)
    if allDevices:  # asyncio.wait doesn't accept an empty list
        await asyncio.wait([device.send(message) for device in allDevices])


async def notify_users():
    if allDevices:  # asyncio.wait doesn't accept an empty list
        message = users_event()
        await asyncio.wait([user.send(message) for user in allDevices])


async def register(websocket):
    allDevices.add(websocket)
    await notify_users()


async def unregister(websocket):
    allDevices.remove(websocket)
    await notify_users()


async def connectionHandler(websocket, path):
    # register(websocket) sends user_event() to websocket
    await register(websocket)
    try:
        await websocket.send(state_event())
        async for message in websocket:
            print ("message = " + message)
            data = json.loads(message)
            if data["action"] == "minus":
                STATE["value"] -= 1
                await notify_state()
            elif data["action"] == "plus":
                STATE["value"] += 1
                await notify_state()
            else:
                await forward_message(message)
                logging.error("unsupported event: {} but forwarning to all clients (test tool)", data)
    finally:
        await unregister(websocket)


start_server = websockets.serve(connectionHandler, "localhost", 6789)

asyncio.get_event_loop().run_until_complete(start_server)
asyncio.get_event_loop().run_forever()