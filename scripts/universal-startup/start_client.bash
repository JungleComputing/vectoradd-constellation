#!/bin/bash

# Change the path to fit your system
tmpdir=$VECTORADD_DIR/java-io-tmpdir

mkdir -p $tmpdir
rm -rf $tmpdir/*.so

clientTimeout=$1; shift
serverAddress=$1; shift
nrNodes=$1; shift
className=$1; shift
poolName=$1; shift
args="$@"

if [ -z "$args" ]; then
  echo "Missing one or more arguments"
  sleep 5
  exit
fi

jar="lib/vector-add-constellation.jar"

className="nl.junglecomputing.constellation.vectoradd.VectorAdd"

echo "Hello from $HOSTNAME"
echo "starting client with arguments: $args"
echo "connecting to server at: $serverAddress using port $CONSTELLATION_PORT"

sleep 2

# *******NOTE*******
# The variables $VECTORADD_DIR and $CONSTELLATION_PORT MUST be added to the environment
# in the shell accessed by ssh. For example by adding them to the ~/.ssh/environment file

# Start Clients
java -cp $VECTORADD_DIR/lib/*:$CLASSPATH \
        -Djava.rmi.server.hostname=localhost \
        -Djava.io.tmpdir=$tmpdir \
        -Dlog4j.configuration=file:$VECTORADD_DIR/log4j.properties \
        -Dibis.server.address=$serverAddress:$CONSTELLATION_PORT \
        -Dibis.pool.size=$nrNodes \
        -Dibis.server.port=$CONSTELLATION_PORT \
        -Dibis.pool.name=$poolName \
        -Dibis.constellation.closed=true \
        $className \
        $args

# Only master has result
result=~/vectoradd.out
if [ -f "$result" ]; then
  # Compress results to prepare for copying to host, keep original
  cd ~/
  tar -zcvf $VECTORADD_DIR/result.tar.gz vectoradd.out
  mv vectoradd.out $VECTORADD_DIR/vectoradd.out
else
  # Remove possible leftover results from previous runs
  rm -rf $VECTORADD_DIR/result.tar.gz
fi

if [ $clientTimeout -lt 0 ]; then
  while :; do
    sleep 10
  done
else
  echo "Shutting down connection in $clientTimeout seconds"
  sleep $clientTimeout
fi


