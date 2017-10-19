#!/bin/sh

#### Represent RGB
mosquitto_pub -t "mygateway1-out/172/8/0/0/26" -m "2.2.0"

# Set RGB status
mosquitto_pub -t "mygateway1-out/172/8/1/0/40" -m "ff00ff"