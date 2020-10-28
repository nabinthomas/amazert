import websocket
import json
uri = "ws://amaze-id1.wl.r.appspot.com/notify"

from websocket import create_connection
ws = create_connection(uri)

def notify_cloud(message):
    ws.send(message)
    
def hello_rtdb(event, context):
    """Triggered by a change to a Firebase RTDB reference.
    Args:
         event (dict): Event payload.
         context (google.cloud.functions.Context): Metadata for the event.
    """
    resource_string = context.resource
    # print out the resource string that triggered the function
    print(f"Function triggered by change to: {resource_string}.")
    
    delta  = event["delta"]
    print(delta)
    msg = json.dumps(delta)
    notify_cloud  (msg)
    print("binu "+msg)