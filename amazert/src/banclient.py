
#ubus call hostapd.wlan0 del_client '{"addr":"$MAC", "reason":5, "deauth":false, "ban_time":$TIME_MS}

import json
import sys
import os


commandString = "ubus call hostapd.wlan0 del_client " 
commandString =  commandString + "'" 
commandString =  commandString + '{"addr" :"'
commandString =  commandString + sys.argv[1]
commandString =  commandString + '", "reason": 5, "deauth": false, "ban_time":'
commandString = commandString +   sys.argv[2] + "}"
commandString =  commandString + "'" 

#print (commandString)
os.system(commandString)