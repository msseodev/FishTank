#!/bin/bash

PID_FILE="exe/pid"
PID_FILE_DEBUG="exe/pid_debug"

if [ -f "$PID_FILE" ]; then
  pid=$(cat $PID_FILE)
  echo "Stopping FishServer on $pid"
  kill -9 $pid
  rm $PID_FILE

elif [ -f "$PID_FILE_DEBUG"]; then
  pid=$(cat PID_FILE_DEBUG)
  echo "Stopping debug-serial on $pid"
  kill -9 $pid
  rm $PID_FILE

else
  echo "FishServer is not running. If you sure server is running, delete $PID_FILE manually"
  exit 1
fi
