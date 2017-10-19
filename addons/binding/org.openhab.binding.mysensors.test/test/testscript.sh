#!/bin/sh

serialPort=$1

# socat -d -d pty,raw,echo=0 pty,raw,echo=0

# Gateway startup notification
#echo "0;0;3;0;14;Gateway startup complete." > $serialPort

# Switch light on:
# 102;1;1;0;2;1

# Request node id
#echo "255;255;3;0;3" > $serialPort #Sensor --> gateway
# 255;255;3;0;4;<id> gateway --> sensor


# Representation of a new (started) sensor
#0;0;3;0;9;read: 172-172-0 s=255,c=0,t=18,pt=0,l=5:1.4.1
#echo "172;255;0;0;18;1.4.1" > $serialPort
#0;0;3;0;9;read: 172-172-0 s=255,c=3,t=6,pt=1,l=1:0

#0;0;3;0;9;read: 172-172-0 s=255,c=3,t=11,pt=0,l=23:Humidity + Temp + 
#echo "172;255;3;0;11;Humidity + Temp + Relay" > $serialPort
#0;0;3;0;9;read: 172-172-0 s=255,c=3,t=12,pt=0,l=3:1.0
#echo "172;255;3;0;12;1.0" > $serialPort
#0;0;3;0;9;read: 172-172-0 s=0,c=0,t=7,pt=0,l=5:1.4.1
#echo "172;0;0;0;7;1.4.1" > $serialPort
#0;0;3;0;9;read: 172-172-0 s=1,c=0,t=6,pt=0,l=5:1.4.1
#echo "172;1;0;0;6;1.4.1" > $serialPort
#0;0;3;0;9;read: 172-172-0 s=2,c=0,t=38,pt=0,l=5:1.4.1
#echo "172;2;0;0;38;1.4.1" > $serialPort
#0;0;3;0;9;read: 172-172-0 s=3,c=0,t=3,pt=0,l=5:1.4.1
#echo "172;3;0;0;3;1.4.1" > $serialPort

# Request I_TIME
#echo "172;255;3;0;1;0" > $serialPort # What time is it?

# I_VERSION
#echo "255;255;3;0;2;2.2.0" > $serialPort # What time is it?

#echo "172;255;3;0;6;0" > $serialPort ############################### is metric?

# Set Humidty status
#echo "172;0;1;0;1;87" > $serialPort

# Set Humidty status
echo "173;0;0;0;7;2.1.1" > $serialPort
echo "173;0;1;0;1;44" > $serialPort

# Set var 4 of humidity child
echo "173;0;1;0;27;0815" > $serialPort

# Set Temperature status
#echo "172;1;1;0;0;27" > $serialPort

# Set V_TEXT
#echo "123;123;1;0;47;ipsumlorum" > $serialPort

# Set V_IR_RECEIVE
#echo "111;111;1;0;33;FADEXXFE" > $serialPort

# Set V_IR_SEND
#echo "111;112;1;0;32;ABCDEFGHIJKL" > $serialPort

#### Represent door
echo "172;4;0;0;0;1.4.1" > $serialPort

# Set Tripped status
echo "172;4;1;0;16;1" > $serialPort

# Set Armed status
echo "172;4;1;0;15;1" > $serialPort

#### Represent motion
#echo "174;0;0;0;1;2.0.1" > $serialPort

# Set Tripped status
#echo "174;0;1;0;16;1" > $serialPort

# Set Armed status
#echo "174;0;1;0;15;0" > $serialPort

#### Represent smoke
#echo "172;6;0;0;2;1.4.1" > $serialPort

# Set Tripped status
#echo "172;6;1;0;16;1" > $serialPort

#### Represent Dimmer
echo "172;7;0;0;4;1.4.1" > $serialPort

# Set dimmer status
echo "172;7;1;0;3;49" > $serialPort

# Set dimmer status
#echo "172;7;1;0;2;1" > $serialPort

#### Represent Cover
echo "172;8;0;0;5;1.4.1" > $serialPort

# Set cover status UP(29) == 1, DOWN(30) == 1
#echo "172;8;1;0;29;1" > $serialPort

# Set cover status
#echo "172;8;1;0;30;1" > $serialPort

# Set cover status percentage
echo "172;8;1;0;3;81" > $serialPort


#### Represent wind
#echo "172;9;0;0;9;1.4.1" > $serialPort

# Set wind speed
#echo "172;9;1;0;8;4.9" > $serialPort

# Set wind gust
#echo "172;9;1;0;9;11.8" > $serialPort


#### Represent rain
#echo "172;10;0;0;10;1.4.1" > $serialPort

# Set rain
#echo "172;10;1;0;6;19.7" > $serialPort

# Set rain rate
#echo "172;10;1;0;7;114" > $serialPort


#### Represent UV
#echo "172;11;0;0;11;1.4.1" > $serialPort

# Set rain
#echo "172;11;1;0;11;8.15" > $serialPort

#### Represent WEIGHT
#echo "172;12;0;0;12;1.4.1" > $serialPort

# Set weight
#echo "172;12;1;0;12;1982.12" > $serialPort

# Set impedance
#echo "172;12;1;0;14;1000000" > $serialPort

#### Represent DISTANCE
#echo "172;13;0;0;15;1.4.1" > $serialPort

# Set DISTANCE
#echo "172;13;1;0;13;1543.98" > $serialPort

#### Represent LIGHT_LEVEL
#echo "172;14;0;0;16;1.4.1" > $serialPort
#echo "101;2;0;0;16;2.1.0" > $serialPort

# Set LIGHT_LEVEL
#echo "101;2;1;0;23;1543.98" > $serialPort
#echo "101;2;1;0;37;246" > $serialPort

# Set watt status
#echo "172;7;1;0;17;0815" > $serialPort

#echo "172;0;1;0;1;38.6" > $serialPort
#echo "172;1;1;0;0;27.2" > $serialPort
#echo "172;2;1;0;38;8.4" > $serialPort


# Representation of a S_POWER Sensor
echo "3;1;0;0;13;1.4.1" > $serialPort

# S_POWER Sensor
echo "3;1;1;0;17;2810.14" > $serialPort # the current power consumption (Watt)
echo "3;1;1;0;18;106.2550" > $serialPort # the overall power usage since sensor boot (KWH)
echo "3;1;1;0;54;4.8" > $serialPort # var
echo "3;1;1;0;55;2809.78" > $serialPort # VA
echo "3;1;1;0;56;-0.69" > $serialPort # Power Factor


# Representation of a S_BARO
#echo "6;3;0;0;8;1.4.1" > $serialPort

# Barometer forecast V_BARO
#echo "6;3;1;0;5;stable" > $serialPort

# Barometer pressure V_PRESSURE
#echo "6;3;1;0;4;1.2" > $serialPort

# Representation of S_MULTIMETER Sensor
#echo "12;3;0;0;30;1.5.0" > $serialPort

# set volt
#echo "12;3;1;0;38;5678" > $serialPort

# set current
#echo "12;3;1;0;39;1234" > $serialPort

# set impedance
#echo "12;3;1;0;14;1000000" > $serialPort

# waterQuality
#echo "10;0;1;0;52;996.3" > $serialPort

#get time
#echo "172;255;3;0;1;0" > $serialPort # I_TIME

# INTERNAL
# batteryLevel
#echo "173;255;3;0;0;91" > $serialPort

# I_HEARTBEAT_RESPONSE
#echo "172;3;3;0;22;1" > $serialPort

# Representation of a S_COLOR_SENSOR
#echo "1;0;0;0;28;2.1.0" > $serialPort

# Represent RGBW
#echo "198;0;0;0;27;2.1.1" > $serialPort
 
# Set RGBW
#echo "198;0;1;0;41;4a30a610" > $serialPort

# Represent RGB
#echo "199;0;0;0;26;2.1.1" > $serialPort

# set rgb
#echo "199;0;1;0;40;ff00ff" > $serialPort

# Representation of a S_DUST
#echo "1;1;0;0;24;2.1.0" > $serialPort

# set dust level
#echo "1;1;1;0;37;66" > $serialPort
