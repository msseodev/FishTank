cmake_minimum_required(VERSION 3.23)
project(FishTankNative)

set(CMAKE_CXX_STANDARD 11)

add_executable(FishTankNative main.cpp FishTank.cpp OneWire.cpp DallasTemperature.cpp Arduino.cpp Arduino.h)
