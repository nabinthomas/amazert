device=$1
json={"device" : "$device", "mac" :""}
echo $json
ubus call iwinfo assoclist '{"device" : "wlan0", "mac" :""}'