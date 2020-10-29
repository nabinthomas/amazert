currenttime=`date`
echo "$currenttime : AmazeRT Runner started.." >> /var/log/amazert.log

while [ 1 ]
do
    python3 /usr/bin/amazert/main.py
    currenttime=`date`
    echo "$currenttime : AmazeRT main stopped.. Will attempt Restart in 10 sec.." >> /var/log/amazert.log
    sleep 10
done
