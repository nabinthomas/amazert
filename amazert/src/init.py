#!/usr/bin/python3

import json
import uuid
import sys

configFilePath = "/etc/amazert.json"
##TODO . This is for local testing. The file should be in /etc along with other configs. 
#configFilePath = "/tmp/amazert.json"

"""
Generate And returns a UUID for this device
## TODO May be use the MAC address once we make the path more secure ??
"""
def generateDeviceUUID():
    deviceUUID = uuid.uuid1() 
    return deviceUUID

"""
Generate and returns a UUID to indentify this registration
"""
def generateRegistrationUUID():
    registrationUUID = uuid.uuid4()
    return registrationUUID

"""
Generate the registration Json for this device.
@param emailid - email Id associated with the device
@param uid - uid associated with the email for simplification of database access
@param deviceId - Device UUID
@param registrationId - Registration UUID
"""
def generateRegistrationJson(emailId, uid, deviceId, registrationId):
    registration = {}
    registration['email'] = emailId
    registration['uid'] = uid
    registration['deviceId'] = str(deviceId)
    registration['registrationId'] = str(registrationId)
    registrationJson = json.dumps(registration)
    return registration

"""
Loads and retuns the current Registration Configuration if any
"""
def loadCurrentRegistration():
    try:
        configFile = open(configFilePath)
        currentRegistration = json.load(configFile)
        if ((currentRegistration['email']) and (currentRegistration['deviceId']) and (currentRegistration['registrationId']) and (currentRegistration['uid'])):
            return currentRegistration
        return None
    except Exception:
        print("AmazeRT Config json does not exist or is invalid")
    return None

"""
Entry point. 

@note If the device is already registered, then the registration process is skipped. 
If the device should be forced to reregister, delete the config File first
The config is stored in the file pointed to by configFilePath
@param argv - arguments
    argv[0] - email id of the user associated with this device
    argv[1] - uid of ther user. user for simplification of the data base access
"""
def main(argv):
    try:
        currentRegistrationData = loadCurrentRegistration()
        print(currentRegistrationData)
        if (currentRegistrationData is None):
            # create a new configuration only if it does not exist
            # This prevents accidentally overwriting config data.
            # Since the ids are different with each generation, overwriting 
            # this will otherwise cause the previously registered accounts to be deactivated
            emailId = argv[0]
            uid = argv[1]
            deviceId = generateDeviceUUID()
            print (deviceId)
            registrationId = generateRegistrationUUID()
            print (registrationId)
            currentRegistrationData = generateRegistrationJson(emailId, uid, deviceId, registrationId)
            print (currentRegistrationData)
        try:
            configFile = open(configFilePath, "w")
            json.dump(currentRegistrationData, configFile, indent=4)
            configFile.close()
        except Exception as e:
            print("Unable to create/write configfile")
            raise e
    except Exception as e:
        print ("AmazeRT Failed to Register device. Details: " + str(e) + "\n")

if __name__ == "__main__":
    main(sys.argv[1:])