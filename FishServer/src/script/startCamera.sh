#!/bin/bash

~/rtsp/rtsp-simple-server

wait 2

libcamera-vid --width 1920 --height 1080 -n -t 0 --inline -o - | ffmpeg -re -i pipe:0 -c:v copy -f rtsp -muxdelay 0.1 rtsp://localhost:8888/fishtank

