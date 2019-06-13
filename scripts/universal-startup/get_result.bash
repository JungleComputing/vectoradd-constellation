#!/bin/bash

# Fetch the logs and results from all devices stored in "config.bash"

timestamp=`date +%s`

echo "Fetching results into \$VECTORADD_DIR/log/$timestamp"

mkdir -p $VECTORADD_DIR/log
mkdir -p $VECTORADD_DIR/log/$timestamp

source $VECTORADD_DIR/bin/universal-startup/config.bash

for ip in "${clientAddresses[@]}"
do
  echo fetching from $ip...
  ssh $ip "cd \$VECTORADD_DIR; tar -zcvf vectoradd.log.tar.gz vectoradd.log" 2>&1 > /dev/null
  
  scp $ip:\$VECTORADD_DIR/result.tar.gz log/$timestamp/result.tar.gz 2> /dev/null
  scp $ip:\$VECTORADD_DIR/vectoradd.log.tar.gz log/$timestamp/log.$ip.tar.gz
done
