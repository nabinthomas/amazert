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
ref = db.reference('/users')
queryResults1 = ref.get()
print (queryResults1)

'''
const firebaseConfig = {
  apiKey: "AIzaSyDnsNBeVMqwif9fjBVb_DnxQ-D6fBuTltk",
  authDomain: "amaze-id1.firebaseapp.com",
  databaseURL: "https://amaze-id1.firebaseio.com",
  projectId: "amaze-id1",
  storageBucket: "amaze-id1.appspot.com",
  messagingSenderId: "668969402569",
  appId: "1:668969402569:web:796cc33e91e2934a578d18",
  measurementId: "G-C8PD91FZQZ"
  };
'''


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
   while not ws.closed:
        message = ws.receive()
        if message is None:  # message is "None" if the client has closed.
            continue
        print(message)
       

        
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
                
                for key, val in snapshot.items():
                    if key == deviceId:
                        print("Found in DB")
                        settingsPath = "/users/" + uid  + "/" + deviceId + "/settings"
                        settingsRef = db.reference(settingsPath) 
                        settingsRef.set(settings)
                        sendReply(ws, message, "PASS")
                        return
                print("NOT Found in DB")
                sendReply(ws, message, "FAIL")
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
