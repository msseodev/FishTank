#!/bin/bash

libcamera-vid -n -t 0 --inline -o - | cvlc stream:///dev/stdin --sout '#rtp{sdp=rtsp://:8888/fishtank}' :demux=h264