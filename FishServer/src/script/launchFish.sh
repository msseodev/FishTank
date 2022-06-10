#!/bin/bash

PID_FILE="exe/pid"
PID_FILE_DEBUG="exe/pid_debug"

if [ -f "$PID_FILE" ]; then
	runningPid=`cat $PID_FILE`
	echo "FishServer is running on $runningPid"
	exit 1
fi

usbDevs=`ls /dev/ttyUSB*`

for usbDev in ${usbDevs[@]};
do
	echo "Check $usbDev"
	driver=`udevadm info $usbDev | grep ID_USB_DRIVER | cut -d '=' -f 2`
	echo "$usbDev driver is $driver"
	
	if [ "$driver" = "ch341" ] 
	then
		echo "$usbDev is Arduino"
		serverJar=`find fish*.jar`
		echo "Starting Server with $usbDev"

		datePrefix=`date +%y-%m-%d`
		java -jar $serverJar $usbDev &> log/server-$datePrefix.log &

		pid=$!
		echo $pid > $PID_FILE
	elif [ "$driver" = "ftdi_sio" ]
	then
	  echo "$usbDev is second Serial"

	  stty -F $usbDev raw 57600
    cat $usbDev > log/serial-$datePrefix-out.log &

    pid=$!
    echo $pid > $PID_FILE_DEBUG
	fi


done



