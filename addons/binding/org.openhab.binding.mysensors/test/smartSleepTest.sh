#!/bin/sh

serialPort=$1

# Receive random value for sensor
echo "104;0;1;0;1;87" > $serialPort

# sleep a bit
sleep 3

# I_HEARTBEAT_RESPONSE
echo "104;255;3;0;22;4999" > $serialPort