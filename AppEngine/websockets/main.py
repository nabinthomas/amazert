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

cred = credentials.Certificate("./key.json")
firebase_admin.initialize_app(cred,{
        'databaseURL' : 'https://amaze-id1.firebaseio.com'
    })
#ref = db.reference('/users')
#queryResults1 = ref.get()
#print (queryResults1)
app = Flask(__name__)
sockets = Sockets(app)

scockeDevMap= {}

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
   while not ws.closed:
        message = ws.receive()
        if message is None:  # message is "None" if the client has closed.
            continue
            
        '''
        {
         "resource_string"= "projects/_/instances/amaze-id1/refs/users/_SDFsEfRSDjFCZXCVASEf/532e8c40-18cd-11eb-a4ca-dca6328f80c0"  ,
          "data" = {'settings': {'1': {'value': 'MuttuWRT'}}}
        }
        '''
        print(message)
        msg = json.loads(message)
        print("jsonload ok ")
        resource_string = msg["resource_string"]
        print("resource_string ok = " + str(resource_string))
        #resource_string= "projects/_/instances/amaze-id1/refs/users/_SDFsEfRSDjFCZXCVASEf/532e8c40-18cd-11eb-a4ca-dca6328f80c0"
                          #0       /1/2        /3        /4   /5    /6                    /7
        path= resource_string.split("/")
        uid = path[6]
        print("uid", uid)
        did= path[7]
        print("did", did)

        settings = msg["data"]["settings"]
        print("settings", settings)
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
            print(email)
            reply = {
                    "identifier" : {
                            "email": email, 
                            "uid": uid,
                            "deviceId": did
                    },
                    "action": "setting",
                    "setting" : { "name" : name, "value" : val1}
            }
            wrtWs= scockeDevMap[did]
            wrtWs.send(json.dumps(reply))
        
# [END gae_flex_websockets_app]
# [END gae_flex_websockets_app]


"""
WRT devices send message to this url for registration and heartbeat
@param message - JSON Object holding the message from WRT device
"""
@sockets.route('/register')
def register_socket(ws):
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
            return
            

        if (reg["action"] == "register" ):
            settings = reg["settings"]
            print("settings" + str(settings))
            
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
                
                print("DB key = " + str(ref) + "Device got from message=" + str(deviceId))
                queryResults1 = ref.get()
                print("DB result" + str(queryResults1))

                
                uref = db.reference(UIdpath )
                snapshot = uref.order_by_key().get()
                print("DB ressnapshotult= " + str(snapshot))
                devIdFound = False
                for key, val in snapshot.items():
                    if key == deviceId:
                        print("Found in DB")
                        settingsPath = "/users/" + uid  + "/" + deviceId + "/settings"
                        settingsRef = db.reference(settingsPath) 
                        settingsRef.set(settings)
                        sendReply(ws, message, "PASS")
                        scockeDevMap[deviceId]=ws

                        devIdFound = True
                if devIdFound == False:
                    print("NOT Found in DB")
                    sendReply(ws, message, "FAIL")
                else:
                    print("Every Thing looks good")
                #return 
            except Exception as e:
                print("Exceptoin here1")
                sendReply(ws, message, "FAIL")
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

""")
