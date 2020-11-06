import websocket
import json
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db


#DB connection 
cred = credentials.Certificate("./key.json")
firebase_admin.initialize_app(cred,{
        'databaseURL' : 'https://amaze-id1.firebaseio.com'
    })


#App Engine websocket uri
uri = "wss://amaze-id1.wl.r.appspot.com/notify"

from websocket import create_connection


def notify_cloud(message):
    ws = create_connection(uri)
    ws.send(message)
    
def hello_rtdb(event, context):
    """
    Triggered by a change to a Firebase RTDB reference.
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
    
    uidSetting = db.reference( "/uniqueId") 
    uidJson= uidSetting.get()
    print("unique json in db  =" + str(uidJson))    
    
   
    resource_string = context.resource
    print(f"Function triggered by change to: {resource_string}.")
    
    delta  = event["delta"]
    print("Delta :" + str(delta))
    msg = {
        "key":  str(uidJson["id"]),
        "resource_string": resource_string,
        "data": delta
    }
    msgS=json.dumps(msg)
    notify_cloud  (msgS)
    print("MsgSend = "+ str(msgS))