# SMA Inverter Bluetooth Binding

This binding fetches data from a SMA inverter over Bluetooth. If your inverter works with [Sunny Explorer](https://www.sma.de/en/products/energy-management/sunny-explorer) then it is likely to work with this binding.
SMA's Bluetooth protocol is proprietary and has been partly reverse engineered by the GitHub community.
As the Java BlueCove library is not maintained and likely not compatible with Java 21 or later, the development of a Bluetooth interface is better achieved using the Python [pybluez](https://github.com/pybluez/pybluez) module for low-level Bluetooth Classic (BR/EDR) socket communication.


The project calls extensively on work done by others:

- [SBFspot](https://github.com/SBFspot/SBFspot/tree/master/SBFspot)
- [understanding-sma-bluetooth-protocol](http://blog.jamesball.co.uk/2013/02/understanding-sma-bluetooth-protocol-in.html?q=SMA)
- [python-smadata2](https://github.com/dgibson/python-smadata2)

There has been previous discussion in the openHAB Community Forum on this subject and this may be of interest to some. [Example on how to access data of a Sunny Boy SMA solar inverter](https://community.openhab.org/t/example-on-how-to-access-data-of-a-sunny-boy-sma-solar-inverter/50963)

This binding requires the installation of a Command Line Interface (CLI) programme specifically written for this binding.The legacy code used for the core of the CLI has been shown to work on inverters SMA3000HF, 3000TL, 4000TL - but in theory should work with all Bluetooth enabled SMA inverters.

## Supported Things

There is only a single Thing type supported.

 - `solar-inverter`: fetches data from an SMA Solar inverter via Bluetooth. Thing UID `smainverterbluetooth:solar-inverter:xxxxxxx`

## Discovery

Discovery is manual, the `solar-inverter` Thing needs to know the Bluetooth address of the SMA inverter. To find this you can use the linux command line.

```bash
>> hcitool Scan
Scanning ...
        00:00:00:00:00:00       SomethingElse
         ...
        00:80:25:00:00:00       SMA001d SN: XXXXXXXXXX SNXXXXXXXXXX
```

The SMA Inverter is identified with SMA001d SN: XXXXXXXXXX where XXXXXXXXXX is the same serial number you see in Sunny Explorer and the Bluetooth address is to the left.
If you don't have Linux available there are Apps for Android or Windows that have a similar function.

## Binding Configuration

This binding requires that an additional utility sma2json is installed on your openHAB userdata directory.
The utility is a CLI that connects to the SMA inverter via Bluetooth and returns a number of parameters.

To build the executable for your operating system start by downloading all you need from this repository. On the computer you run openHAB in a terminal window type:

For Linux ```wget https:// TODO```
 Follow the instructions in the downloaded folder.

For Windows ```curl "https:// TODO .zip"```
 Unzip and follow the instructions.

Once you have completed the installation you should have the sma2json.exe utility in the folder $OPENHAB_HOME/userdata/files. You can test it out from its home directory with this command.

```
>> sma2json.exe -b<00:00:00:00:00:00> -p<yourpassword>
{"code" : 0, "message" : "Success", "data" : {"daily" : 1888, "total" : 42555658, "watts" : 165, "temperature" : 27.62, "acvolts" : 241.02, "time" : "Fri, 31 Oct 2025 16:10:53 GMT Standard Time"}}
```

You should get a response similar to the above.

## Thing Configuration

To manually configure the ```solar-inverter``` Thing you need to know the bluetooth address and password. Use whatever password you use to login via Sunny Explorer. There is an advance configuration parameter to set the refresh interval that defaults to 60s, in practice the concrete inverter updates approximately every 20 seconds, the binding prevents calls faster than this even though you can configure what you like. 

### `solar-inverter` Thing Configuration

| Name              | Type    | Description                           | Default | Required | Advanced |
|-------------------|---------|---------------------------------------|---------|----------|----------|
| bluetooth-address | text    | Bluetooth address of the device       | N/A     | yes      | no       |
| password          | text    | Password to access the device         | N/A     | yes      | no       |
| refreshInterval   | integer | Interval the device is polled in sec. | 60      | no       | yes      |

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

| Channel                   | Type   | Read/Write | Description                                           |
|---------------------------|--------|------------|-------------------------------------------------------|
| polling-switch            | Switch | RW         | Enable or disable the binding polling of the inverter |
| inverter-day-generation   | Number | R          | Energy generated by the inverter today                |
| inverter-total-generation | Number | R          | Total energy generated by the inverter                |
| inverter-spot-power       | Number | R          | Current power output of the inverter                  |
| inverter-spot-ac-voltage  | Number | R          | Current AC voltage output of the inverter             |
| inverter-spot-temperature | Number | R          | Current internal temperature of the inverter          |
| inverter-time             | String | R          | Time of the last reading from the inverter            |
| sma2json-status-code      | Number | R          | Status code from the inverter CLI bridge              |
| sma2json-status-message   | String | R          | Status message from the inverter CLI bridge           |
 

## Full Example

### Thing Configuration

```java
Thing smainverterbluetooth:solar-inverter:my_solar_inverter "solar_inverter" @ "Attic" [ bluetoothAddress="00:00:00:00:00:00", refreshInterval=20, password= "yourpassword"]
```

### Item Configuration

```java
Group                           Attic           "Attic"	                                        ["Attic"]					
Group                           SolarEnergy     "SMA Solar Inverter"            (Attic)         ["Inverter"]

Switch                          PollingSwitch   "Enable Polling"                (SolarEnergy)   ["Switch"]      {channel="smainverterbluetooth:solar-inverter:my_solar_inverter:thing-polling-switch"}
Number:Energy                   EnergyToday     "Today Energy [%.2f kWh]"       (SolarEnergy)   ["Energy"]      {channel="smainverterbluetooth:solar-inverter:my_solar_inverter:inverter-day-generation"}
Number:Energy                   EnergyTotal     "Total Energy [%.2f kWh]"       (SolarEnergy)   ["Energy"]      {channel="smainverterbluetooth:solar-inverter:my_solar_inverter:inverter-total-generation"}
Number:Power                    SpotPower       "Spot Power [%d W]"             (SolarEnergy)   ["Power"]       {channel="smainverterbluetooth:solar-inverter:my_solar_inverter:inverter-spot-power"}
Number:Temperature              Temperature     "Temperature [%.2f °C]"	        (SolarEnergy)   ["Temperature"] {channel="smainverterbluetooth:solar-inverter:my_solar_inverter:inverter-spot-temperature"}      
Number:ElectricPotential        SpotVoltage     "Spot Voltage [%.1f A]"	        (SolarEnergy)   ["Voltage"]     {channel="smainverterbluetooth:solar-inverter:my_solar_inverter:inverter-spot-ac-voltage"}
String                          SampleTime      "Sample Time: [%s]"             (SolarEnergy)   ["Timestamp"]   {channel="smainverterbluetooth:solar-inverter:my_solar_inverter:inverter-time"}
Number:Dimensionless            CLIStatus       "CLI Status: [%s]"              (SolarEnergy)   ["Status"]      {channel="smainverterbluetooth:solar-inverter:my_solar_inverter:sma2json-status-code"}
String                          CLIMessage      "CLI Message: [%s]"             (SolarEnergy)   ["Status"]      {channel="smainverterbluetooth:solar-inverter:my_solar_inverter:sma2json-status-message"}
```


## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_

## Trouble Shooting

If the bluetooth from your inverter is disabled you will need to set the NetID to 1 (default). The Bluetooth NetID is a physical setting that must be changed using a rotary switch inside the inverter's enclosure. See the [Installation Manual](https://www.inbalance-energy.co.uk/datasheets_downloads/SunnyBoy/sb3600tl_installation_manual.pdf)


