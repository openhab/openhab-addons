#!/bin/sh

#### Represent Engergy meter 
mosquitto_pub -t "mygateway1-out/5/0/0/0/13" -m ""

# set V_VAR1 status 
#mosquitto_pub -t "mygateway1-out/5/0/1/0/24" -m "1399"

# request V_VAR1 status
mosquitto_pub -t "mygateway1-out/5/0/2/0/24" -m ""