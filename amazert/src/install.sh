opkg update
opkg install openssl-util
opkg install python3
opkg install python3-pip
# WAR to fix broken pip3. Forcing this for now. 
opkg install curl
curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
python3 get-pip.py --force-reinstall
pip3 install websockets