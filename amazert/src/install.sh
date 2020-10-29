opkg update
opkg install openssl-util
opkg install python3
opkg install python3-pip
# WAR to fix broken pip3. Forcing this for now. 
opkg install curl
curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
python3 get-pip.py --force-reinstall
pip3 install websockets

# Now copy the files to the respective places
mkdir /usr/bin/amazert
cp *.py /usr/bin/amazert/
cp amazert.sh /usr/bin/amazert/
chmod a+x /usr/bin/amazert/amazert.sh

