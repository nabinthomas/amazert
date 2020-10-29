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
   
    Notify message format:
    {
    "resource_string":"projects/_/instances/amaze-id1/refs/users/_SDFsEfRSDjFCZXCVASEf/532e8c40-18cd-11eb-a4ca-dca6328f80c0",
        "data":{
            "settings":{
                "1":{
                    "value":"MuttuWRTdddd"
                }
            }
        }
    }   

    """
    
    resource_string = context.resource
    print(f"Function triggered by change to: {resource_string}.")
    
    delta  = event["delta"]
    print("Delta" + str(delta))
    msg = {
        "resource_string": resource_string,
        "data": delta
    }
    msgS=json.sumps(msg)
    notify_cloud  (msgS)
    print("MsgSend"+msgS)