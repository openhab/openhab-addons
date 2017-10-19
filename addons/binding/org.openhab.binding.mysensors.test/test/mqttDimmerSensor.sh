#!/bin/sh

#### Represent Dimmer
mosquitto_pub -t "mygateway1-out/172/7/0/0/4" -m "2.2.0"

# Set dimmer status
mosquitto_pub -t "mygateway1-out/172/7/1/0/3" -m "49"