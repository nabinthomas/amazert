
#!/usr/bin/env python

# WS client example

import asyncio
import pathlib
import ssl
import threading
import websocket
import websockets
import time
import json
import subprocess
import logging 
import base64
from Crypto.Cipher import AES
import binascii, os
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

from websocket import create_connection

keepRunning = True   

amazertlogfile = "/var/log/amazert.log"
configFilePath = "/etc/amazert.json"
appEngineUri = "ws://amaze-id1.wl.r.appspot.com/register"
appEngineUri = "wss://amaze-id1.wl.r.appspot.com/register"
lastSettingsSent = []
lastStatusSent = []

##TODO . This is for local testing. The file should be in /etc along with other configs. 
#configFilePath = "/tmp/amazert.json"
#appEngineUri = "ws://localhost:6789"

"""
Settings that can be controlled via AmazeRT
This is used for data driven control instead of writing specific code for each setting
General format
[
    {
        name : settingname,
        handler:  {
            write : {
                prologue : [commands to be run before applying this setting. Optional. eg: turn of wifi before changing something]
                commandType : < uci -> will run uci set <name>=<value>>, if a filter exist, then values not in filter will not be applied. 
                              < uci.custom is similar to uci.filtered, except that each value has a custom set of commands to be done . Unsupported values will be ignored
            filter : { # for uci.custom
                value1 : [ command to run in sequence to set this value. 
                        This may be a shell script too. 
                        This is valid only for settings with restricted set of possibleValues
                        ],
                value2...
            },
             OR
            filter : [val1, val2 ...] if using "uci"

            epilogue : [ commands to be run after applying this setting. Optional. eg: wifi on after applying settings/ wifi restart etc]

            }, 
            read : {
                prologue : [commands to be run before reading this setting. Optional. ]
                commandType : < uci -> will run uci get <name>=<value>
                epilogue : [ commands to be run after reading this setting. Optional. ]
                default : <default value>
            }
            
        },
        ...
    }
]
"""
dataDrivenSettingsRules = [ {
    "name" : "system.@system[0].hostname",
    "handler" : {
        "read" : { 
            "commandType" : "uci" 
        }, 
        "write" : {
            "commandType" : "uci"
        }
    }
}, {
    "name" : "wireless.wifinet0.ssid",
    "handler" : {
        "read" : { 
            "commandType" : "uci" 
        }, 
        "write" : {
            "commandType" : "uci",
            "epilogue" : ["wifi"]
        }
    }
}, {
    "name" : "wireless.radio0.country",
    "handler" : {
        "read" : { 
            "commandType" : "uci" 
        }, 
        "write" : {
            "commandType" : "uci",
            "filter" : ["AF","AX","AL","DZ","AS","AD","AO","AI","AQ","AG","AR","AM","AW","AU","AT","AZ","BS","BH","BD","BB","BY","BE","BZ","BJ","BM","BT","BO","BQ","BA","BW","BV","BR","IO","VG","BN","BG","BF","BI","KH","CM","CA","CV","KY","CF","TD","CL","CN","CX","CC","CO","KM","CK","CR","HR","CU","CW","CY","CZ","CD","DK","DJ","DM","DO","TL","EC","EG","SV","GQ","ER","EE","ET","FK","FO","FJ","FI","FR","GF","PF","TF","GA","GM","GE","DE","GH","GI","GR","GL","GD","GP","GU","GT","GG","GN","GW","GY","HT","HM","HN","HK","HU","IS","IN","ID","IR","IQ","IE","IM","IL","IT","CI","JM","JP","JE","JO","KZ","KE","KI","XK","KW","KG","LA","LV","LB","LS","LR","LY","LI","LT","LU","MO","MK","MG","MW","MY","MV","ML","MT","MH","MQ","MR","MU","YT","MX","FM","MD","MC","MN","ME","MS","MA","MZ","MM","NA","NR","NP","NL","AN","NC","NZ","NI","NE","NG","NU","NF","KP","MP","NO","OM","PK","PW","PS","PA","PG","PY","PE","PH","PN","PL","PT","PR","QA","CG","RE","RO","RU","RW","BL","SH","KN","LC","MF","PM","VC","WS","SM","ST","SA","SN","RS","CS","SC","SL","SG","SX","SK","SI","SB","SO","ZA","GS","KR","SS","ES","LK","SD","SR","SJ","SZ","SE","CH","SY","TW","TJ","TZ","TH","TG","TK","TO","TT","TN","TR","TM","TC","TV","VI","UG","UA","AE","GB","US","UM","UY","UZ","VU","VA","VE","VN","WF","EH","YE","ZM","ZW"],
            "epilogue" : ["wifi"]
        }
    }
}, {
    "name" : "wireless.wifinet0.macfilter",
    "handler" : {
        "read" : { 
            "commandType" : "uci", 
            "default" : "disable"
        }, 
        "write" : {
            "commandType" : "uci",
            "filter" : ["allow", "deny", "disable"],
            "epilogue" : ["wifi"]
        }
    }
}, {
    "name" : "wireless.wifinet0.maclist",
    "handler" : {
        "read" : { 
            "commandType" : "uci",
            "default" : ""
        }, 
        "write" : {
            "commandType" : "uci",
            "epilogue" : ["wifi"]
        }
    }
}, {
    "name" : "wireless.wifinet0.disabled",
    "handler" : {
        "read" : { 
            "commandType" : "uci",
            "default" : "0"
        }, 
        "write" : {
            "commandType" : "uci.custom",
            "filter" : {
                "0" : ["uci", "delete", "wireless.wifinet0.disabled"],
                "1" : ["uci", "set", "wireless.wifinet0.disabled=1"]
            },
            "epilogue" : ["wifi"]
        }
    }
}]

"""
Helper function to run a command in shell and collect the output. 
to be used only for reading and writing settings
@param is an array of strings which form the command line
"""
def runShellcommand(command):
    resultString = ""
    try:
        commandProcess = subprocess.Popen(command,
            stdin = subprocess.PIPE, 
            stdout = subprocess.PIPE,
            stderr = subprocess.PIPE,
            universal_newlines = True,
            bufsize = 0)
        commandProcess.stdin.close() # Command requiring additional input is not supported
        for line in commandProcess.stdout:
            resultString = resultString + line
        for line in commandProcess.stderr:
            resultString = resultString + line

    except Exception as e:
        resultString = str(e)
        responseCode = -1
    return resultString.strip()

"""
Encrypt a given string value to be sent to the server
@param encryptionConfig - Parameters for encryption
@return a string object of the format
    IV + digest + cipher text
    24 bytes + 24 bytes + cipher text variable length
    The string is base64 encoded
"""
def encryptMessage(encryptionConfig, message):
    msgBytes = message.encode()
    key = encryptionConfig["key"]
    cipher = AES.new(key, AES.MODE_GCM)
    cipherText, digest = cipher.encrypt_and_digest(msgBytes)
    secureValue = {
        "cipherText" : str(base64.b64encode(cipherText), "utf-8"),
        "IV" :  str(base64.b64encode(cipher.nonce), "utf-8"),
        "digest" :  str(base64.b64encode(digest), "utf-8")
    }
    secureValueString = secureValue["IV"] + secureValue["digest"] + secureValue["cipherText"]
    #return secureValue
    return secureValueString

"""
Decrypt a given value received from the server
@param encryptionConfig - Parameters for encryption
@param cipherValue a string object of the format
    IV + digest + cipher text
    24 bytes + 24 bytes + cipher text variable length
    The string is base64 encoded
"""
def decryptMessage(encryptionConfig, cipherValue):
    cipherObject = {
        "IV": cipherValue[0:24],
        "digest": cipherValue[24:48],
        "cipherText": cipherValue[48:]
    }
    cipherText = base64.b64decode(cipherObject["cipherText"])
    IV = base64.b64decode(cipherObject["IV"])
    digest = base64.b64decode(cipherObject["digest"])
    key = encryptionConfig["key"]
    cipher = AES.new(key, AES.MODE_GCM, IV)
    plainText = cipher.decrypt_and_verify(cipherText, digest)
    return plainText.decode("utf-8")

"""
Encrypt the "value" field in every object in the list of jsons passed in. 
Each json object in the array is supposed to have "name" and "value". 
@param encryptionConfig contains the details about what key/algorithm etc to use for encryption
@param elements is the list of items for which the values should be encrypted
@return encrypted list
"""
def encryptAllValues(encryptionConfig, elements):
    encryptedList = []
    for element in elements:
        encryptedElement = {}
        encryptedElement["name"] = element["name"]
        encryptedElement["value"] = encryptMessage(encryptionConfig, element["value"])
        encryptedList.append(encryptedElement)
    return encryptedList

"""
Filter out settings that didnot change from last set of settings that was sent to the AppEngine
"""
def filterChangedSettings(newSettings):
    filteredList = []
    for setting in newSettings:
        settingWasPresent = False 
        for oldSetting in lastSettingsSent:
            if (setting["name"] == oldSetting["name"]):
                settingWasPresent = True
                if (setting["value"] != oldSetting["value"]):
                    filteredList.append(setting)
        if (settingWasPresent == False):
            filteredList.append(setting)
    return filteredList

"""
Filter out status that didnot change from last set of settings that was sent to the AppEngine
"""
def filterChangedStatus(newStatus):
    filteredList = []
    for status in newStatus:
        statusWasPresent = False 
        for oldStatus in lastStatusSent:
            if (status["name"] == oldStatus["name"]):
                statusWasPresent = True
                if (status["value"] != oldStatus["value"]):
                    filteredList.append(status)
        if (statusWasPresent == False) :
            filteredList.append(status)
    return filteredList

""" 
Prepare a list of all the settings which will be sent across to the AmazeRT App Engine
""" 
def getAllSupportedSettings():
    allSettings = []
    for rule in dataDrivenSettingsRules:
        setting = {}
        setting['name'] = rule['name']
        
        try: 
            if (rule["handler"]["read"]["prologue"] is not None):
                logger.debug ("prologue")
                runShellcommand(rule["handler"]["prologue"])
        except:
            logger.debug("no prologue")
        if (rule['handler']["read"]['commandType'] == "uci"):
            setting['value'] = runShellcommand(["uci", "get", setting["name"]])
        try:
            if (setting["value"] == "uci: Entry not found"):
                setting["value"] = rule["handler"]["read"]["default"]
        except:
            logger.debug("using default value")
        try: 
            if (rule["handler"]["read"]["epilogue"] is not None):
                runShellcommand(rule["handler"]["read"]["epilogue"])
        except:
            logger.debug( "no epilogue" )
        logger.debug ("Setting " + setting["name"] + "=" + setting['value'])
        allSettings.append(setting)
    return allSettings

"""
Each entry in this specifies how a status is read from the system. All these  can be then sent
to the Cloud backend, for later use from the Mobile App
The format is  { <name> :[command to run to read the json status], ...}
"""
statusRules = [{
    "name": "wifi.clients" ,
    "command" : ["ubus", "call", "hostapd.wlan0", "get_clients"],
}]

""" 
Prepare a list of all the status which will be sent across to the AmazeRT App Engine
""" 
def getAllSupportedStatus():
    allStatus = []
    
    for rule in statusRules:
        try:
            status= {}
            command = rule["command"]
            status["name"] = rule["name"]
            statusOutput = str(runShellcommand(command))
            status["value"] = json.loads(statusOutput)
            allStatus.append(status)
        except Exception as e:
            logger.debug("failed status rule " + str(rule) + str(e))
    return allStatus

"""
How frequent the heartbeat is sent to the server to keep the connection active.
""" 
heartBeatIntervalInSeconds = 30

"""
Prepares a packet to send. 
Every packet will need to be added with some extra information about the 
identification of the device being controlled/reported. This is a wrapper for generating
and updating that data

@param config - JSON Object holding the device configuration (ID data)
@param dataToSend - JSON Object holding the data to be sent

@return - JSON with identification data added to dataToSend

"""
def preparePacketToSend(config, dataToSend):
    jsonPacket = dataToSend
    identification = {}
    identification['uid'] = config['uid']
    identification['deviceId'] = config['deviceId']
    identification['email'] = config['email']
    jsonPacket['identifier'] = identification
    return  json.dumps(jsonPacket)

"""
Thread implementing Heartbeat support. This sends all the current settings to the Cloud server once on boot
and then keeps sending a hearbeat message at heartBeatIntervalInSeconds
"""
class amazeRTHeartBeatThread(threading.Thread):
    def __init__(self, config, ws):
        threading.Thread.__init__(self)
        self.ws = ws
        self.config = config

    def run(self):
        while (keepRunning == True):
            self.sendAllConfiguration()
            time.sleep(heartBeatIntervalInSeconds)

    def sendAllConfiguration(self):
        global lastSettingsSent
        global lastStatusSent
        message = { "action" : "register"}
        allSettings = getAllSupportedSettings()
        allStatus = getAllSupportedStatus()
        message["settings"] = encryptAllValues(self.config["encryption"], filterChangedSettings(allSettings))
        message["status"] = filterChangedStatus(allStatus)
        packettoSend = preparePacketToSend(self.config, message)
        lastSettingsSent = allSettings
        lastStatusSent = allStatus
        logger.debug(f"> {packettoSend}")
        self.ws.send(packettoSend)

"""
Handles a command sent from the cloud.
Once the processing is complete, a response is sent back to the server
in the format like 
                {   
                     "action" : "response",
                     "response" : resultString, # output of the command executed.
                     "code" : responseCode 
                }
@note identifier is always added to the reply to server

@param config - Identification for this device. 
@param ws - used for communicating with the server
@command array of command + command line params
@options Special options to run the command. Unused for now. Added for future enhancements
"""
def amazeRTCommandHandler(config, ws, command, options):
    logger.debug ("Executing command : > " + json.dumps(command))
    # Send a response.
    resultString = ""
    responseCode = 0
    try:
        commandProcess = subprocess.Popen(command,
            stdin = subprocess.PIPE, 
            stdout = subprocess.PIPE,
            stderr = subprocess.PIPE,
            universal_newlines = True,
            bufsize = 0)
        commandProcess.stdin.close() # Command requiring additional input is not supported
        for line in commandProcess.stdout:
            resultString = resultString + line
    except Exception as e:
        resultString = str(e)
        responseCode = -1
    
    responseJson = {
                     "action" : "response",
                     "response" : resultString,
                     "code" : responseCode 
                    }

    responseJson = preparePacketToSend(config, responseJson)
    ws.send(json.dumps(responseJson))

"""
A Setting that is stored in the cloud may be encrypted. If we receive a request from the 
cloud to change a setting, we need to decrypt the setting.
@param encSetting - Encrypted Setting string
    @see encryptMessage for details on the format
@return - setting that was decrypted. 
Note: If the decryption failed, an exception will be raised.
"""
def decryptSetting(config, value):
    try:
        plainText = decryptMessage(config["encryption"], value)
    except Exception as e:
        #Assuming this is unencrypted setting
        raise e
    return plainText

"""
Handle a request to apply a particular setting
@param config - Identification for this device. 
@param ws - used for communicating with the server
@settings array of name and value for the setting to be applied in the format [{ name : <name>, value : <value>},...]
@options Special options to run the command. Unused for now. Added for future enhancements

"""
def amazerRTSettingHandler(config, ws, settings, options):
    logger.debug ("Applying settings : > " + json.dumps(settings))
    for setting in settings:
        settingName = str(setting["name"])
        settingValue = str(setting["value"])
        try:
            settingValue = decryptSetting(config, settingValue)
        except Exception as e:
            # If the decryption failed, then something is wrong with the data sent from the cloud, 
            # Or the database itself may be corrupted. In that case, try to recover the database next time we 
            # do the heartbeat. 
            # Do not apply this setting, but proceed with other settings. 
            global lastSettingsSent
            lastSettingsSent = []
            continue

        response = ""
        # Find the rule for handling this setting. 
        for rule in dataDrivenSettingsRules:
            logger.debug(rule["name"])
            if (rule["name"] == settingName):
                logger.debug ("Match with " + rule["handler"]["write"]["commandType"] )

                try: 
                    if (rule["handler"]["write"]["prologue"] is not None):
                        logger.debug ("prologue")
                        response = response + runShellcommand(rule["handler"]["prologue"])
                except:
                    response += "no prologue\n"
                if (rule["handler"]["write"]["commandType"] == "uci"):
                    command = [""]
                    try: 
                        supportedList = rule["handler"]["write"]["filter"]
                    except KeyError as e:
                        supportedList = None
                    if ((supportedList is None) or (settingValue in supportedList)):
                        command = ["uci", "set", settingName + "=" + settingValue]
                    else:
                        logger.debug ("Unsupported Value " + settingValue + " For setting " + settingName)        
                elif (rule["handler"]["write"]["commandType"] == "uci.custom"):
                    command = rule["handler"]["write"]["filter"][settingValue]
                logger.debug (command)
                response = response + runShellcommand(command)
                logger.debug("Response is " + response)
                response = response + runShellcommand(["uci", "commit"])
                logger.debug("Response is " + response)

                try: 
                    if (rule["handler"]["write"]["epilogue"] is not None):
                        response = response + runShellcommand(rule["handler"]["write"]["epilogue"])
                except:
                    response += "no epilogue\n"
                logger.debug("Response is " + response)

"""
Handle a request to control the amazeRT SW, or some special commands
@param config - Identification for this device. 
@param ws - used for communicating with the server
@control control information
@options Special options to run the command. Unused for now. Added for future enhancements

"""
def amazerRTControlHandler(config, ws, control, options):
    logger.debug ("Responding to control : > " + json.dumps(control))
    if control == "exit":
        keepRunning = False

"""
Used to map the action to a function that can handle that kind of action
"""
actionHandlerMappings = {
    "command" : amazeRTCommandHandler, 
    "setting" : amazerRTSettingHandler,
    "control"  : amazerRTControlHandler
 }

"""
Entry point for any request that comes to the amazeRT management service

@note - Requests will be handled only if the identifier in the request messsage from the ws
matches the local configuration in config

@param config - Identification for this device. 
@param ws - used for communicating with the server

"""
def amazeRTActionHandler(config, ws):
    logger.debug ("amazeRTActionHandler start ")
    message = ws.recv()
    logger.debug(f"< {message}")
    request = json.loads(message)
    try:
        action = request["action"]
        actionParams = request[action]
        try:
            options = request["options"]
        except KeyError:
            options = {}
        actionHandler = actionHandlerMappings[action]
        actionHandler(config, ws, actionParams, options)
    except KeyError:
        logger.debug ("Unsupported Action for request "  + json.dumps(request))
    if (message == "exit"):
        keepRunning = False

"""
Loads and retuns the current Registration Configuration if any
"""
def loadCurrentRegistration():
    try:
        configFile = open(configFilePath)
        currentRegistration = json.load(configFile)
        if ((currentRegistration['email']) and (currentRegistration['deviceId']) and (currentRegistration['registrationId']) and (currentRegistration['uid'])):
            ## del currentRegistration['registrationId']
            return currentRegistration
        return None
    except Exception:
        logger.debug("AmazeRT Config json does not exist or is invalid")
    return None

"""
Generates a 256 bit key to be used for encryption
@param password - password used to generate the key. We will use the registrationId for this
@param salt - salt to be used for key generation. 
@return bytes containing the key
"""
def generateKey(password, salt):
    password = password.encode()
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=32,
        salt=salt, 
        iterations=100000,
        backend=default_backend()
    )
    key = kdf.derive(password) 
    #print("key = [" + str(key) + "]")
    return key

"""
Main for the service. Connects to the Cloud App Engine, Initialize local handlers 
and keep listening to requests
"""
async def amazeRTServiceMain():

    config = loadCurrentRegistration()
    if (config is None):
        logger.debug("AmazeRT is not configured on this machine. please run initial configuration using init.py")
        exit(-1)
    ## Generate Encryption Keys
    config["encryption"] = {
        "algorithm" : "AES-256-GCM",
        "key" : ""
    }

    ## salt used is static since the password is already a random uuid generated during install
    config["encryption"]["key"] = generateKey(config["registrationId"], b'salt_')

    #testMessage = "1234"
    #cipherText = encryptMessage(config["encryption"], testMessage)
    #secureValueString = secureValue["IV"] + secureValue["digest"] + secureValue["cipherText"]
    #print("CipherText = " + cipherText)
    #cipherObject = {
    #    "IV": cipherText[0:24],
    #    "digest": cipherText[24:48],
    #    "cipherText": cipherText[48:]
    #}
    #print ("cipher = " + json.dumps(cipherObject))
    #plainText = decryptMessage(config["encryption"], cipherText)

    #print ("test = ", testMessage, " plain = ", plainText)
    ws = create_connection(appEngineUri)

    hearbeatThread = amazeRTHeartBeatThread(config, ws)
    hearbeatThread.start()
    try:
        while (keepRunning):
            amazeRTActionHandler(config, ws)
    except websockets.exceptions.ConnectionClosedError as e:
        logger.debug("Connection was closed by server. Will quit now and restart + ", str(e))
        exit(-1)
    hearbeatThread.join()

## Setup debug logging
logger = logging.getLogger("AmazeRT::Main")
logger.setLevel(logging.DEBUG)
loghandler = logging.FileHandler(amazertlogfile)
logformatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
loghandler.setFormatter(logformatter)
logger.addHandler(loghandler)

logger.debug ("Starting amazeRT Management system\n")

asyncio.get_event_loop().run_until_complete(amazeRTServiceMain())



