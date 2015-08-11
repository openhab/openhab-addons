# Honeywell WiFi Thermostat Binding

This binding provides OpenHAB with the means to control a Honeywell WiFi thermostat. It requires an active internet connection due to the fact that the binding uses the Honeywell Total Connect Comfort web site to control the thermostat. 

# Configuration

You will need to add a new thing manually in order to use this binding. There is no way to autodetect thermostats. When you first add the thing, enter the thing configuration page, from here several options need to be set. First enter your username from the My Total Connect Comfort web site. Enter your password, then the Device ID.

You have to determine your device ID manually for now, that can be accomplished by using the following directions:

1) Login to the Honeywell My Total Connect Comfort web site at: https://mytotalconnectcomfort.com/portal/

2) Use your user name and password to enter the site.

3) Click login and select the thermostat you wish to control.

4) Look at the URL bar, it should have a number at the end. This is your device ID.
    https://mytotalconnectcomfort.com/portal/Device/Control/XXXXX?page=1
    
# Channels

sysMode
currentTemp
heatSP
coolSP
fanMode

# Future Goals

- Autodetect thermostats, somehow.
- Improve UI function
- ...

# Contact

JD Steffen <jdsteffen81@gmail.com>
