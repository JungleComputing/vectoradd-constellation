#!/bin/bash

# Executes vectoradd with Constellation on all devices listed in config.bash
# The device which calls this script, will act as the server.

# --- REQUIREMENTS ---
# * All devices must have $VECTORADD_DIR and $CONSTELLATION_PORT set as environment
# variables upon SSH access, this can be done in the ~/.ssh/environment file.
# * The devices acting as clients do not need to have the full repository locally, 
# only the jar files (lib/*) and the scripts. However, they must be using the 
# file _same_ relative file structure, as the repo.

# Result and logs can be retrieved from all devices by running the script bin/universal-startup/get_results.bash

if [ "$#" -ne 4 ]; then
        echo "Wrong number of arguments --Remember to execute on device running server--"
        echo "Usage: $0 -n <vector_length> -computeDivideThreshold <threshold>"
        exit
fi

# Change the path to fit your system
tmpdir=$VECTORADD_DIR/java-io-tmpdir

mkdir -p $tmpdir
rm -rf $tmpdir/*.so

timestamp=`date +%s`

# Get adresses of all devices
source $VECTORADD_DIR/bin/universal-startup/config.bash

nrNodes=${#clientAddresses[*]}

if [ $nrNodes -eq 0 ]; then
  echo "Add at least one client in config.bash"
  exit
fi

className="nl.junglecomputing.constellation.vectoradd.VectorAdd"
poolName="constellation.pool.$timestamp"
jar="lib/vector-add-constellation.jar"
args="-n $2 -computeDivideThreshold $4"

# Client timeout duration in seconds (use -1 for keep open)
clientTimeout=15

# Start Server
x-terminal-emulator -e "$VECTORADD_DIR/bin/constellation-server"

for ip in "${clientAddresses[@]}"
do
  x-terminal-emulator -e ssh $ip "\$VECTORADD_DIR/bin/universal-startup/start_client.bash $clientTimeout $serverAddress $nrNodes $className $poolName $args 2>&1 | tee \$VECTORADD_DIR/vectoradd.log"
done

