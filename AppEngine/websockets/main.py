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

cred = credentials.Certificate("./key.json")
firebase_admin.initialize_app(cred,{
        'databaseURL' : 'https://amaze-id1.firebaseio.com'
    })
ref = db.reference('/user')
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


@sockets.route('/chat')
def chat_socket(ws):
    while not ws.closed:
        message = ws.receive()
        if message is None:  # message is "None" if the client has closed.
            continue
        # Send the message to all clients connected to this webserver
        # process. (To support multiple processes or instances, an
        # extra-instance storage or messaging system would be required.)
        clients = ws.handler.server.clients.values()
        for client in clients:
            client.ws.send(message)
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
