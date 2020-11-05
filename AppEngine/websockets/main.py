# Copyright 2018 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from __future__ import print_function

# [START gae_flex_websockets_app]
from flask import Flask, render_template
from flask_sockets import Sockets

import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
from firebase_admin import messaging

import firebase_admin
from firebase_admin import credentials

import json
import uuid

app = Flask(__name__)
sockets = Sockets(app)
scockeDevMap= {}


#DB connection 
cred = credentials.Certificate("./key.json")
firebase_admin.initialize_app(cred,{
        'databaseURL' : 'https://amaze-id1.firebaseio.com'
    })

'''
There will be only one AppEngine running .
When AppEngine First instanciate a unique key will be generated and stored in the data base.
Each time the Cloud function is triggered , the cloud function should read the key from database 
and send to App Engine , when it calls the /notify websocket interface. 
AppEngine Will check the unique key for authentication .
'''

def generateUUID():
    uniqueKey = uuid.uuid1() 
    return uniqueKey

uniqueKey = generateUUID();

uidJson = {"id" : str(uniqueKey)}
uidSetting = db.reference( "/uniqueId") 
uidSetting.set(uidJson)

print("Unique Id updated in DB " + str(uniqueKey))


#Websocket related functions 
def sendReply(ws,message, result):
    response = {
        "action" : "response", 
        "response" : {
            "code" : result ,
            "message":message
        }
    }
    ws.send(json.dumps(response))

def sendToAll(ws,message):
    clients = ws.handler.server.clients.values()
    for client in clients:
        client.ws.send(message)



@sockets.route('/chat')
def chat_socket(ws):
    while not ws.closed:
        message = ws.receive()
        if message is None:  # message is "None" if the client has closed.
            continue
        # Send the message to all clients connected to this webserver
        # process. (To support multiple processes or instances, an
        # extra-instance storage or messaging system would be required.)
        sendToAll(ws,message)


"""
Notify the appropriate WRT device about a realtime data base change event
@param message - JSON Object holding the change in data base 
"""
@sockets.route('/notify')
def notify_socket(ws):
    print("NOTIFY")
    try:
        while not ws.closed:
            message = ws.receive()
            if message is None:  # message is "None" if the client has closed.
                continue
                
            '''
            {
            "key" : "xxuniquekeyxx",
            "resource_string"= "projects/_/instances/amaze-id1/refs/users/_SDFsEfRSDjFCZXCVASEf/532e8c40-18cd-11eb-a4ca-dca6328f80c0"  ,
            "data" = {'settings': {'1': {'value': 'MuttuWRT'}}}
            }
            '''
            print(message)
            msg = json.loads(message)
            print("jsonload ok ")
            #validate the unique key and make sure that this is infact genuine cloud function call
            inputkey= msg["key"]
            if str(inputkey) != str(uniqueKey) :
                print("unique key mismatch! Ignore this")
                continue
            else:
                print("unique key matched Process the call")
            resource_string = msg["resource_string"]
            print("resource_string =" + str(resource_string))
            #resource_string= "projects/_/instances/amaze-id1/refs/users/_SDFsEfRSDjFCZXCVASEf/532e8c40-18cd-11eb-a4ca-dca6328f80c0"
                            #0       /1/2        /3        /4   /5    /6                    /7
            path= resource_string.split("/")
            uid = path[6]
            print("uid=", uid)
            did= path[7]
            print("did=", did)

            settings = msg["data"]["settings"]
            print("settings", settings)

            settingData=[]
            for node in settings.keys():
                print("node=", node)
                
                #refpath = "/users/" + str(uid) + "/" + str(did)+"/settings/"+node
                #{'settings': {'1': {'value': 'MuttuWRT'}}}
                refSetting = db.reference("/users/" + str(uid) + "/" + str(did)+ "/settings/" + str(node))
                settingChnage = refSetting.get()
                print("settingChnage=" + str(settingChnage))
                name = settingChnage["name"]
                val1= settingChnage["value"]
                print("name=" + name + "  Value=" +val1 )

                identifierRef= db.reference("/users/" + str(uid) + "/" + str(did)+ "/identifier")
                identifier = identifierRef.get()
                email = identifier["email"]
                print("Email :" + email)
                settingData.append({ "name" : str(name), "value" : str(val1)})
            reply = {
                "identifier" : {
                        "email": str(email), 
                        "uid": str(uid),
                        "deviceId": str(did)
                },
                "action": "setting",
                "setting" : settingData
            }
            print(reply)
            wrtWs= scockeDevMap[str(did)]
            wrtWs.send(json.dumps(reply))
    except Exception as e:
            print("Message jason parse Error" + str(e))
                    
# [END gae_flex_websockets_app]
# [END gae_flex_websockets_app]


"""
WRT devices send message to this url for registration and heartbeat
@param message - JSON Object holding the message from WRT device
"""
@sockets.route('/register')
def register_socket(ws):
    try:
        print("Register ")
        while not ws.closed:
            message = ws.receive()
            if message is None:  # message is "None" if the client has closed.
                continue
            #TODO: json parsing error
            try :
                print("Before Parsing")
                reg = json.loads(message)
                print("after Parsing")
            except Exception as e:
                print("Message jason parse error" + str(e))
                continue
                

            if (reg["action"] == "register" ):
                identifier= reg["identifier"]
                print("identifier" + str(identifier))
                uid = identifier["uid"]
                print("uid" + str(uid))
                deviceId =identifier["deviceId"]
                print("deviceId" + str(deviceId))
                # Now check whether the user device id in the db is same as the device id send by wrt. 
                devIdpath = "/users/" + uid  + "/" + deviceId
                UIdpath = "/users/" + uid 
                try :
                    ref = db.reference(devIdpath )
                    queryResults1 = ref.get()
                    print("DB Device Path query result: " + str(queryResults1))
                    if queryResults1 == None :
                        #there is no user or device registered
                        print("Given user or device NOT Found in DB")
                        sendReply(ws, message, "FAIL")
                    else:
                        #find the devices in the message and update the settigns under them 
                        uref = db.reference(UIdpath )
                        snapshot = uref.order_by_key().get()
                        print("DB ressnapshotult= " + str(snapshot))
                        #do we really need this loop ?
                        for key,val in snapshot.items():
                            print("key = " +str(key))
                            if key == deviceId:
                                settings = reg["settings"]
                                print("settings" + str(settings))
                                print("Found in DB")
                                settingsPath = "/users/" + uid  + "/" + deviceId + "/settings"
                                settingsRef = db.reference(settingsPath) 
                                settingsRef.set(settings)
                                sendReply(ws, message, "PASS")
                                scockeDevMap[deviceId]=ws
                                print("Every Thing looks good")
                except Exception as e:
                    print("Exceptoin here1 = " + str(e))
                    sendReply(ws, message, "FAIL")
                    continue
    except Exception as e:
        print("Exceptoin at top level " + str(e))
        # closing the ws and returning 
        return      
# [END gae_flex_websockets_app]
# [END gae_flex_websockets_app]


@app.route('/')
def index():
    return render_template('index.html')


if __name__ == '__main__':
    print("""
This can not be run directly because the Flask development server does not
support web sockets. Instead, use gunicorn:

gunicorn -b 127.0.0.1:8080 -k flask_sockets.worker main:app

Other useful commands are 
gunicorn -b 127.0.0.1:8080 -k flask_sockets.worker main:app
gcloud app deploy app.yaml     --project amaze-id1
gcloud config set project amaze-id1
gcloud app logs tail

http://amaze-id1.wl.r.appspot.com

""")
