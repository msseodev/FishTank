#!/bin/bash

usbDevs=`ls /dev/ttyUSB*`

for usbDev in ${usbDevs[@]};
do
	echo "Check $usbDev"
	driver=`udevadm info $usbDev | grep ID_USB_DRIVER | cut -d '=' -f 2`
	echo "$usbDev driver is $driver"
	
	if [ "$driver" = "ch341" ] 
	then
		echo "$usbDev is Arduino"
		serverJar=`find Fish*.jar`
		echo "Starting Server with $usbDev"
		java -jar $serverJar $usbDev
	fi
done



