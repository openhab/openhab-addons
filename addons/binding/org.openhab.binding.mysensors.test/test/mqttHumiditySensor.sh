#!/bin/sh

mosquitto_pub -t "mygateway1-out/173/0/0/0/7" -m "2.2.0"
mosquitto_pub -t "mygateway1-out/173/0/1/0/1" -m "44"