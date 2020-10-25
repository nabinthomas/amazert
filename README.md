# amazert 


Notes: 
## amazeRT Router Management Service
### Creating Certificates and Keys for the server. 
openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout key.pem -out cert.pem

### The directory strucure is as follows.
amazert/src/* - Contains all the source files and install scripts. 

Makefile - used to build and package the services running on the router

### Build steps

1. Sync the repository locally 
2. The router management service (everything installed on the router) in amazert dir in the root. 
3. Following commands can bne used to build 

cd amazert

make # Builds the package

make commit #commits the built package so that it can be checked in, and used by the Android App later

The package amazert/amazert.pkg will be packaged with the android app for installing during the initial setup
The App will copy this file to the OpenWRT router, extract, and run install.sh in it. The install.sh script can be customized to copy the other app files to required locations, do the initial setup, and start the amazeRT Management Service

# AmazeRT Initial Install Sequence
The package built above will have everything needed for initial setup for AmazeRT device management software.

Android App will need to do the following
1. Extract the package file (It is really a tar file) to the openWRT device
2. Run install.sh
3. Wait for all the dependecies to be install from the above script
4. Run python3 init.py <emailId>
    The emailid will need to be the authenticated user's email Id from the Android App
5. Wait for the above script to generate the config file. It will generate a new config only if there is nothing present.
6. The new config will be saved to /etc/amazert.json (Note: this is in /tmp/amazert.json for now to do some local testing but will move at some point)
7. Copy the json back to the Androd App, and use the values in there for sending out the initial registration to the App Engine
8. Wait for the device to be registered in the App Engine.
9. Run python3 main.py to start the service. Once this service initializes it will send all of its current configuration to the App Engine with the same Ids used for registration (from amazert.json)
10. App Now can send commands to the Cloud Database for the settings it needs to change

# Format of amazert.json
<pre>
  {
      'registrationId': '8784c5df-f087-4740-b41b-248dc9389a2f',  # Id unique to this registration. This is kind of a secret password to be used
      'email': 'nabin@gmail.com', # Owner's email id. Used by App Engine to find the device and to validate Android App's authentication state
      'deviceId': 'fb967061-168a-11eb-9272-88e9fe6b97d6' # Unique, secret Id for the device. All communication jsons will have this uuid as part of the json
  }
</pre>
