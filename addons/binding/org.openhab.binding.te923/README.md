# TE923, Irox Binding

This is an openHAB binding for most weather stations based on Hideki weather station like IROX Pro X, Mebus TE923 or TFA Nexus. Some other hardware mays be supported.

You need to install and setup this software first http://te923.fukz.org/. This binding act as a wrapper between this command line and openHab2.

## Supported Things

This binding support most weather stations based on Hideki weather station like IROX Pro X, Mebus TE923 or TFA Nexus. Some other hardware mays be supported.

## Discovery

Simply add a new thing and select "TE923 and IROX Binding".

## Binding Configuration

You will need to give the path of te923con. The default is set to /opt/te923/te923con.

You need to have a working http://te923.fukz.org/ software running on your machine. If the command line is not working, the binding will not work.

If you execute manually this command : ./te923con 

You should have something like
1511997548:19.50:47:-1.50:82:18.10:53:18.40:51:8.70:56:16.00:47:907.7:i:5:0:i:i:i:i:79

If you use the debian based installation, don't forget to give the right to read the usb device : addgroup openhab plugdev.
openHab2 mays be running with another user.
 

## Thing Configuration

Simply select the channels you are interested with.

## Channels

We can extract the following information
*  *T0*    - Temperature from internal sensor in °C
*  *H0*    - Humidity from internal sensor in % rel
*  *T1..5* - Temperature from external sensor 1..4 in °C
*  *H1..5* - Humidity from external sensor 1...4 in % rel
*  *PRESS* - Air pressure in mBar
*  *UV*    - UV index from UV sensor
*  *WD*    - Wind direction in n x 22.5°; 0 -> north
*  *WS*    - Wind speed in m/s
*  *RC*    - Rain counter

Select the channels you are interested with.

Don't hesitate to add this thing multiples times if you want to support multiple locations.