kill -9 `ps |grep "/usr/bin/amazert/runner.sh" |grep -v "grep" |cut -d ' ' -f 3`
kill -9 `ps |grep " /usr/bin/amazert/main.py" |grep -v "grep" |cut -d ' ' -f 3`
/etc/init.d/amazert disable
rm -rf /etc/init.d/amazert
rm -rf /var/log/amazert.log
rm -rf /var/log/amazert.start
rm -rf /etc/amazert.json
rm -rf /usr/bin/amazert
echo "SUCCESS"
