#!/usr/bin/python3

import json
import uuid
import sys

configFilePath = "/etc/amazert.json"
##TODO . This is for local testing. The file should be in /etc along with other configs. 
configFilePath = "/tmp/amazert.json"

def generateDeviceUUID():
    deviceUUID = uuid.uuid1() 
    return deviceUUID

def generateRegistrationUUID():
    registrationUUID = uuid.uuid4()
    return registrationUUID

def generateRegistrationJson(emailId, deviceId, registrationId):
    registration = {}
    registration['email'] = emailId
    registration['deviceId'] = str(deviceId)
    registration['registrationId'] = str(registrationId)
    registrationJson = json.dumps(registration)
    return registration

def loadCurrentRegistration():
    try:
        configFile = open(configFilePath)
        currentRegistration = json.load(configFile)
        if ((currentRegistration['email']) and (currentRegistration['deviceId']) and (currentRegistration['registrationId'])):
            return currentRegistration
        return currentRegistration
    except Exception:
        print("AmazeRT Config json does not exist or is invalid")
    return None

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
            deviceId = generateDeviceUUID()
            print (deviceId)
            registrationId = generateRegistrationUUID()
            print (registrationId)
            currentRegistrationData = generateRegistrationJson(emailId, deviceId, registrationId)
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