# amazert 


Notes: 
## amazeRT Router Management Service
### Setup & Build instructions 
#### Creating Certificates and Keys for the server. 
openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout key.pem -out cert.pem

#### The directory strucure is as follows.
amazert/src/* - Contains all the source files and install scripts. 

Makefile - used to build and package the services running on the router

#### Build steps

1. Sync the repository locally 
2. The router management service (everything installed on the router) in amazert dir in the root. 
3. Following commands can bne used to build 

cd amazert

make # Builds the package

make commit #commits the built package so that it can be checked in, and used by the Android App later

The package amazert/amazert.pkg will be packaged with the android app for installing during the initial setup
The App will copy this file to the OpenWRT router, extract, and run install.sh in it. The install.sh script can be customized to copy the other app files to required locations, do the initial setup, and start the amazeRT Management Service


### AmazeRT Initial Install Sequence
The package built above will have everything needed for initial setup for AmazeRT device management software.

Android App will need to do the following
1. Extract the package file (It is really a tar file) to the openWRT device
2. Run install.sh
3. Wait for all the dependecies to be install from the above script
4. Run python3 init.py <emailId> <uid>
    The emailid will need to be the authenticated user's email Id from the Android App
    The uid is for simplifying the App Engine Code, and will need to be filled in once the App is authenticated with the above emailid
5. Wait for the above script to generate the config file. It will generate a new config only if there is nothing present.
6. The new config will be saved to /etc/amazert.json (Note: this is in /tmp/amazert.json for now to do some local testing but will move at some point)
7. Copy the json back to the Androd App, and use the values in there for sending out the initial registration to the App Engine
8. Wait for the device to be registered in the App Engine.
9. Run python3 main.py to start the service. Once this service initializes it will send all of its current configuration to the App Engine with the same Ids used for registration (from amazert.json)
10. App Now can send commands to the Cloud Database for the settings it needs to change

### Format of amazert.json
<pre>
  {
      'registrationId': '8784c5df-f087-4740-b41b-248dc9389a2f',  # Id unique to this registration. This is kind of a secret password to be used. This is used to encrypt secure data so that is is not visible to AmazeRT App Engine. This value is shared with the AmazeRT Android App during initial setup
      'email': 'nabin@gmail.com', # Owner's email id. Used by App Engine to find the device and to validate Android App's authentication state
      'uid': '_SDFsEfRSDjFCZXCVASEf', # Uid associated with this email, used for simplificaiton of database access at the server
      'deviceId': 'fb967061-168a-11eb-9272-88e9fe6b97d6' # Unique, secret Id for the device. All communication jsons will have this uuid as part of the json
  }
</pre>

### AmazeRT communication documentation
#### AmazeRT Communication to Cloud
##### General Structure
Every Json sent across will need to have a way to identify the device being controlled. 
The JSON sent will have "identifier" field to handle this case. 
A Sample is given below
<pre>
{
    "identifier" : {
      'email': 'nabin@gmail.com', 
      'uid': '_SDFsEfRSDjFCZXCVASEf',
      'deviceId': 'fb967061-168a-11eb-9272-88e9fe6b97d6'
  }
  ... Other data
}
</pre>
##### Registration packet
Registration is done on every reboot as the first message to the server.
This make sure that the device's settings and current status are updated on the server once it boots up
A Sample is given below
<pre>
{
 {
     "action": "register",
     "settings": [{
         "name": "system.@system[0].hostname",
         "value": "wtMv/BV7OLUfoV4TOxhWIA==GGiy0zTiVpm8i/17FrDCQA==4iFV4CAHasg="
     }, {
         "name": "wireless.wifinet0.ssid",
         "value": "tT760DVgoZtDjjkpHyIALw==bHuwfcQVlaO6C5wt6Udw4w==Q8pD3w=="
     }, {
         "name": "wireless.radio0.country",
         "value": "d4S2Ji1GHU0ptBOQ9RjHwA==3SAgpb1FR88FgoDUlVAI3w==hZA="
     }, {
         "name": "wireless.wifinet0.macfilter",
         "value": "FU7sqNP8T8Fqo9uL5KgOfg==UE9XeHcwjoZZgdxQPPg+JQ==+39aWhf9aA=="
     }, {
         "name": "wireless.wifinet0.maclist",
         "value": "0wmIN4IxqfXrIaDsTN4eng==IaO/pCgQuH7vIROcYt32Dw=="
     }, {
         "name": "wireless.wifinet0.disabled",
         "value": "ARklPOoAgZ2GM/oidcjGug==ZZ49Wmf04pqJMBr2b0hSOQ==/A=="
     }],
     "status": [{
         "name": "wifi.clients",
         "value": {
             "freq": 5560,
             "clients": {
                 "d4:c9:4b:4f:4c:72": {
                     "auth": true,
                     "assoc": true,
                     "authorized": true,
                     "preauth": false,
                     "wds": false,
                     "wmm": false,
                     "ht": false,
                     "vht": false,
                     "wps": false,
                     "mfp": false,
                     "rrm": [115, 16, 145, 0, 4],
                     "aid": 0
                 }
             }
         }
     }],
     "identifier": {
         "uid": "0NWFFJG764T2ucPAhdLDNwQjcgA2",
         "deviceId": "6650ed14-1a7e-11eb-92da-dca6328f80c0",
         "email": "nabin@gmail.com"
     }
 }
 
</pre>

Note that the values in settings are encrypted using a key derived from the Registration Id of the 
device. Only the Andoid App instance that registered this device has a copy of this registration Id. 
The encrypted string is the result of concatenating IV, digest and the cipher text in that order
Each section is base64 encoded to support binaries. Ecnryption uses AES algorithm in GCM mode, with a 256 bit key used for encryption
##### Hearbeat packet
For simplification, Heartbeat packet is nothing but a registration packet that is sent across to the Cloud periodically.
However, for heartbeats, the settings array is filtered to send only those settings which changed. Status is always sent 
without any filtering.

#### AmazeRT Communication from Cloud
Commnication to from the cloud to AmazeRT Router also follow a similar structure of the communication the other way around

#### AmazeRT Applying a setting from Cloud
Example Request to apply a setting
Note: the value should be encrypted. See "register" packet earlier for details.
<pre>
{
    "identifier" : {
      'email': 'nabin@gmail.com', 
      'uid': '_SDFsEfRSDjFCZXCVASEf',
      'deviceId': 'fb967061-168a-11eb-9272-88e9fe6b97d6'
    },
    "action": "setting",
    "setting" : { "name" : "wireless.wifinet0.ssid", "value" : "PiWRT" }
}
</pre>
See dataDrivenSettingsRules in main.py to see the list of settings 
that is currently supported
#### AmazeRT Receiving a command from Cloud
Example command to reboot the device
<pre>
{
  "identifier": {
    "email": "bkj@gmail.com",
    "uid": "_SDFsEfRSDjFCZXCVASEf",
    "deviceId": "fb967061-168a-11eb-9272-88e9fe6b97d6"
  },
  "action": "command",
  "command": E('["reboot"]')
}

</pre>

### Uninstallation 
Android App can initiate an uninstall for the AmazerRT Agent running on the device by using the "command" interface in the protocol. 
An encrypted command to start the uninstall can be sent via the cloud backend. To make the steps simpler and easily modifiable, an uninstall script is included as part of the installation, which removes AmazeRT agent, logs and startup scripts from the device.


