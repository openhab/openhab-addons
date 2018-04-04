# Pentair Pool 

This is an openHAB binding for a Pentair Pool System. It is based on combined efforts of many on the internet in reverse-engineering the proprietary Pentair protocol (see References section). The binding was developed and tested on a system with a Pentair EasyTouch controller, but should operate with other Pentair systems.

## Hardware Setup

> REQUISITE DISCLAIMER: CONNECTING 3RD PARTY DEVICES TO THE PENTAIR SYSTEM BUS COULD CAUSE SERIOUS DAMAGE TO THE SYSTEM SHOULD SOMETHING MALFUNCTION.  IT IS NOT ENDORSED BY PENTAIR AND COULD VOID WARRENTY. IF YOU DECIDE TO USE THIS BINDING TO INTERFACE TO A PENTAIR CONTROLLER, THE AUTHOR(S) CAN NOT BE HELD RESPONSIBLE.

This binding requires an adapter to interface to the Pentair system bus. This bus/wire runs between the Pentair control system, indoor control panels, IntelliFlo pumps, etc. It is a standard RS-485 bus running at 9600,8N1 so any RS-485 adapter should work and you should be able to buy one for under $30. Pentair does not publish any information on the protocol so this binding was developed using the great reverse-engineering efforts of others made available on the internet.  I have cited sevearl of those in the References section below.

### Connecting adapter to your system

A usb or serial RS-485 interface or IP based interface can be used to interface to the Pentair system bus. The binding includes 2 different bridge Things depending on which type of interface you use, serial_bridge or ip_bridge.

If your openHAB system is physically located far from your Pentair equipment or indoor control panel, you can use a Raspberry Pi or other computer to redirect USB/serial port traffic over the internet using a program called ser2sock (see Reference section). An example setup would run the following command: "ser2sock -p 10000 -s /dev/ttyUSB1 -b 9600 -d". Note: This is the setup utlized for the majority of my testing of this binding.

Note: if you are on a linux system, the framework may not see a symbolically linked device (i.e. /dev/ttyRS485).  To use a symbolically linked device, add the following line to /etc/default/openhab2, `EXTRA_JAVA_OPTS="-Dgnu.io.rxtx.SerialPorts=/dev/ttyRS485"`

Once you have the interface connected to your system, it is best to test basic connectivity. Note the protocol is a binary protocol (not ASCII text based) and in order to view the communication packets, one must use a program capable of a binary/HEX mode. If connected properly, you will see a periodic traffic with packets staring with FF00FFA5.  This is the preamble for Pentairs communication packet. After you see this traffic, you can proceed to configuring the Pentair binding in openHAB.

#### USB/Serial interface

For a USB/Serial interface, you can use most terminal emulators. For linux, you can use minicom with the following options: `minicom -H -D /dev/ttyUSB1 -b 9600`

#### IP interface

For an IP based interface (or utilizing ser2sock) on a linux system, you can use nc command with the following options: `nc localhost 10000 | xxd`

### Pentair Controller panel configuration

In order for the Pentair EasyTouch controller to receive commands from this binding, you may need to enable "Spa-side" remote on the controller itself.

## Supported Things

This binding supports the following thing types:

| Thing           | Thing Type | Description                           
| _______________ | :________: | _______________________________________
| ip_bridge       | Bridge     | A TCP network RS-485 bridge device.
| serial_bridge   | Bridge     | A USB or serial RS-485 device.
| EasyTouch       | Thing      | Pentiar EasyTouch pool controller.
| Intelliflo Pump | Thing      | Pentair Intelliflo variable speed pump.
| Intellichlor    | Thing      | Pentair Intellichlor chlorinator.


## Binding Configuration

There are no overall binding configurations that need to be set up as all configuration is done at the "Thing" level.

## Thing Configuration

Pentair things can be configured either through the online Paper UI configuration, or manually through a 'pentair.thing' configuration file.  The following table shows the available configuration parameters for each thing.

| Thing         | Configuration Paramaters
| _____________ | __________________________
| ip_bridge     | address - IP address for the RS-485 adapter - Required.
|               | port - TCP port for the RS-485 adapter - Not Required - default = 10000.
|               | id - ID to use when communciating on Pentair control bus - devault = 34.
| serial_bridge | serialPort - Serial port for the IT-100s bridge - Required.
|               | baud - Baud rate of the IT-100 bridge - Not Required - default = 9600.
|               | pollPeriod - Period of time in minutes between the poll command being sent to the IT-100 bridge - Not Required - default=1.
|               | id - ID to use when communciating on Pentair control bus - devault = 34.

Currently automatic discovery is not supported and the binding requires configuration via the PaperUI or a file in the conf/things folder.  Here is an example of a thing configuration file called 'pentair.thing':

    Bridge pentair:ip_bridge:1 [ address="192.168.1.202", port=10001 ] {
        easytouch main [ id=16 ]
        intelliflo pump1 [ id=96 ]
        intellichlor ic40
    }

## Channels

Pentair things support a variety of channels as seen below in the following table:

| Channel         | Item Type  | Description
| _______________ | __________ | __________________
| EasyTouch Controller | |
| pooltemp        | Number     | Current pool temperature (readonly)
| spatemp         | Number     | Current spa temperature (readonly)
| airtemp         | Number     | Current air temperature (readonly)
| solartemp       | Number     | Current solar temperature (readonly)
| poolheatmode    | Number     | Current heat mode setting for pool (readonly): 0=Off, 1=Heater, 2=Solar Preferred, 3=Solar
| poolheatmodestr | String     | Current heat mode setting for pool in string form (readonly)
| spaheatmode     | Number     | Current heat mode setting for spa (readonly): 0=Off, 1=Heater, 2=Solar Preferred, 3=Solar
| spaheatmodestr  | String     | Current heat mode setting for spa in string form (readonly)> 
| poolsetpoint    | Number     | Current pool temperature set point
| spasetpoint     | Number     | Current spa temperature set point
| heatactive      | Number     | Heater mode is active
| pool            | Switch     | Primary pool mode
| spa             | Switch     | Spa mode
| aux1            | Switch     | Aux1 mode
| aux2            | Switch     | Aux2 mode
| aux3            | Switch     | Aux3 mode
| aux4            | Switch     | Aux4 mode
| aux5            | Switch     | Aux5 mode
| aux6            | Switch     | Aux6 mode
| aux7            | Switch     | Aux7 mode
| IntelliChlor    | |
| saltoutput      | Number     | Current salt output % (readonly)
| salinity        | Number     | Salinity (ppm) (readonly)
| IntelliFlo Pump | |
| run             | Number     | Pump running (readonly)
| drivestate      | Number     | Pump drivestate (readonly)
| mode            | Number     | Pump mode (readonly)
| rpm             | Number     | Pump RPM (readonly)
| power           | Number     | Pump power in Watts (readonly)
| error           | Number     | Pump error (readonly)
| ppc             | Number     | Pump PPC? (readonly)

## Full Example

The following is an example of an item file (pentair.items):

    Group gPool          "Pool"
    
    Number Pool_Temp               "Pool Temp [%.1f °F]"          <temperature>   (gPool)   { channel = "pentair:easytouch:1:main:pooltemp" }
    Number Spa_Temp                "Spa Temp [%.1f °F]"           <temperature>   (gPool)   { channel = "pentair:easytouch:1:main:spatemp" }
    Number Air_Temp                "Air Temp [%.1f °F]"           <temperature>   (gPool)   { channel = "pentair:easytouch:1:main:airtemp" }
    Number Solar_Temp              "Solar Temp [%.1f °F]"         <temperature>   (gPool)   { channel = "pentair:easytouch:1:main:solartemp" }
    
    Number PoolHeatMode            "Pool Heat Mode [%d]"                          (gPool)   { channel="pentair:easytouch:1:main:poolheatmode" }
    String PoolHeatModeStr         "Pool Heat Mode [%s]"                          (gPool)   { channel="pentair:easytouch:1:main:poolheatmodestr" }
    Number SpaHeatMode             "Spa Heat Mode [%d]"                           (gPool)   { channel="pentair:easytouch:1:main:spaheatmode" }
    String SpaHeatModeStr          "Spa Heat Mode [%s]"                           (gPool)   { channel="pentair:easytouch:1:main:spaheatmodestr" }
    PoolSetPoint                   "Pool Set Point [%.1f °F]"                     (gPool)   { channel="pentair:easytouch:1:main:poolsetpoint" }
    Number SpaSetPoint             "Spa Set Point [%.1f °F]"                      (gPool)   { channel="pentair:easytouch:1:main:spasetpoint" }    
    Number HeatActive              "Heat Active [%d]"                             (gPool)  { channel="pentair:easytouch:1:main:heatactive" }
    
    Switch Mode_Spa                 "Spa Mode"                                    (gPool)  { channel = "pentair:easytouch:1:main:spa" }
    Switch Mode_Pool                "Pool Mode"                                   (gPool)  { channel = "pentair:easytouch:1:main:pool" }
    Switch Mode_PoolLight           "Pool Light"                                  (gPool)  { channel = "pentair:easytouch:1:main:aux1" }
    Switch Mode_SpaLight            "Spa Light"                                   (gPool)  { channel = "pentair:easytouch:1:main:aux2" }
    Switch Mode_Jets                "Jets"                                        (gPool)  { channel = "pentair:easytouch:1:main:aux3" }
    Switch Mode_Boost               "Boost Mode"                                  (gPool)  { channel = "pentair:easytouch:1:main:aux4" }
    Switch Mode_Aux5                "Aux5 Mode"                                   (gPool)  { channel = "pentair:easytouch:1:main:aux5" }
    Switch Mode_Aux6                "Aux6 Mode"                                   (gPool)  { channel = "pentair:easytouch:1:main:aux6" }
    Switch Mode_Aux7                "Aux7 Mode"                                   (gPool)  { channel = "pentair:easytouch:1:main:aux7" }
    
    Number SaltOutput               "Salt Output [%d%%]"                          (gPool)  { channel = "pentair:intellichlor:1:ic40:saltoutput" }
    Number Salinity                 "Salinity [%d ppm]"                           (gPool)  { channel = "pentair:intellichlor:1:ic40:salinity" }
    
    Switch Pump_Run                 "Pump running [%d]"                           (gPool) { channel = "pentair:intelliflo:1:pump1:run" }
    Number Pump_DriveState          "Pump drivestate [%d]"                        (gPool) { channel = "pentair:intelliflo:1:pump1:drivestate" }
    Number Pump_Mode                "Pump Mode [%d]"                              (gPool) { channel = "pentair:intelliflo:1:pump1:mode" }
    Number Pump_RPM                 "Pump RPM [%d]"                               (gPool) { channel = "pentair:intelliflo:1:pump1:rpm" }
    Number Pump_Power               "Pump Power [%d W]"                           (gPool) { channel = "pentair:intelliflo:1:pump1:power" }
    Number Pump_Error               "Pump Error [%d]"                             (gPool) { channel = "pentair:intelliflo:1:pump1:error" }
    Number Pump_PPC                 "Pump PPC [%d]"                               (gPool) { channel = "pentair:intelliflo:1:pump1:ppc" }

Here is an example sitemap:

    Frame label="Pool" {
       Switch item=Mode_Spa
       Switch item=Mode_PoolLight
       Switch item=Mode_SpaLight
       Switch item=Mode_Jets
       Text item=Pool_Temp valuecolor=[>82="red",>77="orange",<=77="blue"]
       Text item=Spa_Temp valuecolor=[>97="red",>93="orange",<=93="blue"]
       Setpoint item=SpaSetPoint minValue=85 maxValue=103 step=1.0
       Group item=gPool label="Advanced"
    }


## References

Setting up RS485 and basic protocol - http://www.sdyoung.com/home/decoding-the-pentair-easytouch-rs-485-protocol/
ser2sock Github - https://github.com/nutechsoftware/ser2sock 

## Future Enhancements

- Add automatic discovery of devices on RS-485
- Add in IntelliBrite light color selection (need to capture protocol on system that has this)
- Add direct control of pump (non read-only channels)
- Fix heat active - not working on my system.
