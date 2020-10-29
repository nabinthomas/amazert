opkg update
opkg install openssl-util
opkg install python3
opkg install python3-pip
# WAR to fix broken pip3. Forcing this for now. 
opkg install curl
curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
python3 get-pip.py --force-reinstall
pip3 install websockets

rm get-pip.py
# Now copy the files to the respective places
mkdir -p /usr/bin/amazert
cp *.py /usr/bin/amazert/
cp runner.sh /usr/bin/amazert/
chmod a+x /usr/bin/amazert/runner.sh
cp amazert /etc/init.d/
chmod a+x /etc/init.d/amazert
/etc/init.d/amazert enable


