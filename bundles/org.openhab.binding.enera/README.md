# Enera Binding
This binding provides access to live data gathered in Project Enera (https://projekt-enera.de), a public project in the area of Smart Grids.
Enera itself only provides access to gathered data via their mobile Apps with no means to analyse them elsewhere.

Using the Enera Binding, you can get access to the devices in your Enera accont(s) and retrieve meter readings as well as current consumption levels.
Other uses, such as access to historical data etc., could be added in the future.

No tests could yet be done using more than one account or more than one device per account, although everything is built for these scenarios to work.  

## Supported Things
This binding provides two different thing types:

### Enera Account
This thing is your Enera Account. It acts as a bridge for the Enera Devices.
To get started you need to add at least one Enera Account and provide login credentials for it.

### Enera Device
This thing is a single device that you can see in your Enera App.
Devices cannot be added manually but will only be discovered automatically after you add an Enera Account.

On the device, you can configure the Refresh Rate. As Enera sends updates at little faster than every second, we have to reduce this rate.
  
You can roughly read this parameter as "update every X seconds", while it actually means "update channels only every X updates".

So, if you stick with the default value of 60, you can except one update roughly any minute.
For live consumption data, the average for all received values in the meantime will be reported.
For meter readings, only the last value will be reported.

## Discovery
After adding an Enera Account, you can (and have to) discover Device things.
To do this, go to Things > + > Enera Binding.

Note that devices added in the Enera app don't get discovered automatically, you have to trigger the discover process manually.

## Channels

| channel | type | description |
|-------------------------|--------|----------------------------------------------------------------------------------------------------------------|
| meter-reading | Number | contains the meter reading (OBIS key 1-0:1.8.0*255) |
| meter-reading-outbound | Number | contains the meter reading for outbound power (OBIS key 1-0:2.8.0*255)<br>_Note:_ This is not visible in the Enera app! |
| current-consumption | Number | contains the current power consumption (OBIS key 1-0:1.7.0*255) |