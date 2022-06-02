#!/bin/bash

PID_FILE="exe/pid"

if [ -f "exe/pid" ]; then

	pid=`cat $PID_FILE`
	echo "Stopping FishServer on $pid"

	kill -9 $pid
	rm $PID_FILE
else
	echo "FishServer is not running. If you sure server is running, delete $PID_FILE manually"
	exit 1

fi
