#ubus call iwinfo assoclist '{"device" : "$D", "mac" :""}'



import json
import sys
import os

commandParam = {"device" : sys.argv[1], "mac": ""}
commandString = "ubus call iwinfo assoclist " 
commandString =  commandString + "'" 
commandString =  commandString + '{"device" :"'
commandString =  commandString + sys.argv[1]
commandString =  commandString + '", "mac": ""}'
commandString =  commandString + "'" 

os.system(commandString)