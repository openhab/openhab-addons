#!/bin/sh

#### Represent RGBW
mosquitto_pub -t "mygateway1-out/172/9/0/0/27" -m "2.2.0"

# Set RGBW status
mosquitto_pub -t "mygateway1-out/172/9/1/0/41" -m "ffffffff"