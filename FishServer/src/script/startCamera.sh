#!/bin/bash

libcamera-vid --width 1920 --height 1080 -n -t 0 --inline -o - | cvlc stream:///dev/stdin --sout '#rtp{sdp=rtsp://:8888/fishtank}' :demux=h264
