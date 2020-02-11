# lightwaverf Binding  ![Lightwave RF](logo.png)


This binding integrates Lightwave RF's range of smart devices. (https://lightwaverf.com/).
A registered account is required with Lightwave Rf in order to use the binding.


## Supported Things

The Link Plus or previous generation 1 hub is required as a gateway between devices.
The current 'Smart Series' (gen2) equipment operates on 867mHz and is only accessible throught the provided api, generation 1 equipment is also integrated through this api.

Devices supported currently include:
Generation 2 Sockets and Dimmers.
Generation 1 Socket(single), Thermostat and energy Monitor.


| Device type              | Generation       | ThingType |
|--------------------------|------------------|------------|
| Socket (1 way)           | 1                | s11        |
| Socket (2 way)           | 2                | s22        |
| Dimmer (1 way)           | 2                | d21        |
| Dimmer (2 way)           | 2                | d22        |
| Dimmer (3 way)           | 2                | d23        |
| Dimmer (4 way)           | 2                | d24        |
| Thermostat               | 1                | t11        |
| Energy Monitor           | 1                | e11        |

A single 'Thing' is used for each device type, with channels being allocated into groups.  Ie, a 2 way socket will have 2 channel groups, each containing the relavent controls.

## Discovery

All devices are avilable for auto discovery.
For any other devices not supported, please add these to the binding where they will be discovered as an unknown device.
A list of supported channels will be generated under the 'Thing' properties in order for them to be added to the binding, please submit this list via github.

## Binding Configuration

Add a lightwave account thing and configure your email and password for your online account.
Additional Properties:
Refresh interval: Frequency to get updates from the api.
Number of items to fetch at once: Due to limitations with the api, polling has to be split into groups, this defines how many channels will be fetched at once.
If the amount of channels goes above this, a further fetch will be initiated resulting in an icreased poll time. (refreshinterval x groups = new polling time).
  


## Thing Configuration

The initial configuration is as follows:

```

Bridge lightwaverf:lightwaverfaccount:accountname [ username="example@microsoft.com", password="password" ]

```

## Devices

Devices are identified by the number between - and - in the deviceId.  This generally starts at 1 for each account.  DeviceId's are not visible in the lightwave app but can be found by running discovery.

Things can be added to a bridge enclosed in {} as follows:

```

ThingType UniqueThingName	"name" @ "group" [ sdId="simplifieddeviceId" ] 

```


## Channels

channels can be assigned to items as follows:

```

{ channel="lightwaverf:thingType:accountname:sdId:channelgroup#channel" }

```


### LinkPlus (Generation 2)

#### Channel 1

| Channel            | Item Type  | Description                     |  Writeable |
|--------------------|------------|---------------------------------|------------|
| 1#currentTime      | DateTime   | Current Time (date and time)    |    No      |
| 1#buttonPress      | Switch     | Link plus button pressed        |    No      |  
| 1#time             | Datetime   | Current Time                    |    No      | 
| 1#date             | DateTime   | Doesnt work                     |    No      | 
| 1#monthArray       | Number     | Doesnt work                     |    No      | 
| 1#weekdayArray     | Number     | Current weekday number (1 Mon)  |    No      | 
| 1#identify         | Switch     | Blink the device LED's          |    Yes     |
| 1#locationLongitude| String     | Longitude set in app            |    No      |
| 1#locationLatitude | String     | Latitude set in app             |    No      |
| 1#duskTime         | DateTime   | Dusk time today                 |    No      | 
| 1#dawnTime         | DateTime   | Dawn time today                 |    No      |
| 1#day              | Number     | Current Day                     |    No      |  
| 1#month            | Number     | Current Month                   |    No      | 
| 1#year             | Number     | Current Year                    |    No      | 
| 1#weekday          | String     | Current weekday (CAPITALS)      |    No      |  
| 1#timeZone         | Number     | Current time zone               |    No      | 
| 1#rgbColor         | Color      | Colour of the device LED's      |    Yes     |


### Sockets (Generation 2)

#### Channel 1

| Channel            | Item Type  | Description                     |  Writeable |
|--------------------|------------|---------------------------------|------------|
| 1#switch           | Switch     | Turn on/off                     |    Yes     |
| 1#power            | Number     | Current power draw in watts     |    No      |  
| 1#energy           | Number     | Total usage in kwH              |    No      | 
| 1#voltage          | Number     | Current voltage being supplied  |    No      | 
| 1#outletInUse      | Switch     | Socket has a device plugged in  |    No      | 
| 1#protection       | Switch     | physical controls disabled      |    Yes     | 
| 1#identify         | Switch     | Blink the device LED's          |    Yes     |
| 1#reset            | Switch     | Reset the device                |    Yes     |
| 1#upgrade          | Switch     | Check for firmware updates      |    Yes     |
| 1#diagnostics      | Switch     | Carry out diagnostics           |    Yes     | 
| 1#periodOfBroadcast| String     | Device Uptime                   |    No      | 
| 1#rgbColor         | Color      | Colour of the device LED's      |    Yes     |

#### Channel 2

| Channel            | Item Type  | Description                     |  Writeable |
|--------------------|------------|---------------------------------|------------|
| 2#switch           | Switch     | Turn on/off                     |    Yes     |
| 2#power            | Number     | Current power draw in watts     |    No      |  
| 2#energy           | Number     | Total usage in kwH              |    No      | 
| 2#outletInUse      | Switch     | Socket has a device plugged in  |    No      | 
| 2#protection       | Switch     | physical controls disabled      |    Yes     |

## Example

### demo.things

```

Bridge lightwaverf:lightwaverfaccount:mylocation "Lightwave Account" [ username="example@hotmail.co.uk", password="xxxxxxxxxx"] {

h21 	LightwaveHub	        "Link Plus"				        [ sdId="1" ]
s11 	KitchenSocket1 	        "Kitchen Socket 1"		        [ sdId="2" ]
s22 	KitchenSocket2 	        "Kitchen Socket 2"		        [ sdId="3" ]
d21 	KitchenDimmer 	        "Kitchen Dimmer"			    [ sdId="4" ]
t11 	KitchenThermostat 	    "Kitchen Thermostat"			[ sdId="5" ]
e11 	KitchenEnergyMonitor	"Kitchen energy Monitor"	    [ sdId="6" ]	
}

```

### demo.items

```
Switch  KitchenSocket2_LeftSwitch        "Kettle"                       { channel="lightwaverf:s22:mylocation:KitchenSocket2:1#switch" }
Number	KitchenSocket2_LeftPower	     "Kettle Power"		            { channel="lightwaverf:s22:mylocation:KitchenSocket2:1#power"  }
Number	KitchenSocket2_LeftEnergy	     "Kettle Energy"		        { channel="lightwaverf:s22:mylocation:KitchenSocket2:1#energy" }
Switch	KitchenSocket2_LeftOutletInUse	 "Kettle Outlet In Use"	        { channel="lightwaverf:s22:mylocation:KitchenSocket2:1#outletInUse"}
Switch	KitchenSocket2_LeftProtection	 "Kettle Protection"		    { channel="lightwaverf:s22:mylocation:KitchenSocket2:1#protection" }

Switch  KitchenSocket2_RightSwitch       "Toaster"                      { channel="lightwaverf:s22:mylocation:KitchenSocket2:2#switch" }
Number	KitchenSocket2_RightPower	     "Toaster Power"		        { channel="lightwaverf:s22:mylocation:KitchenSocket2:2#power"  }
Number	KitchenSocket2_RightEnergy	     "Toater Energy"		        { channel="lightwaverf:s22:mylocation:KitchenSocket2:2#energy" }
Switch	KitchenSocket2_RightOutletInUse	 "Toaster Outlet In Use"	    { channel="lightwaverf:s22:mylocation:KitchenSocket2:2#outletInUse"}
Switch	KitchenSocket2_RightProtection	 "Toaster Protection"	        { channel="lightwaverf:s22:mylocation:KitchenSocket2:2#protection" }

Number	KitchenSocket2_Voltage   	     "Kitchen Socket 2 Voltage"     { channel="lightwaverf:s22:mylocation:KitchenSocket2:1#voltage" }
Switch	KitchenSocket2_Identify			 "Kitchen Socket 2 Identify"	{ channel="lightwaverf:s22:mylocation:KitchenSocket2:1#identify" }
Switch	KitchenSocket2_Reset			 "Kitchen Socket 2 Reset"	    { channel="lightwaverf:s22:mylocation:KitchenSocket2:1#reset" }
Switch	KitchenSocket2_Upgrade		     "Kitchen Socket 2 Upgrade"	    { channel="lightwaverf:s22:mylocation:KitchenSocket2:1#upgrade" }
Switch	KitchenSocket2_Diagnostics		 "Kitchen Socket 2 Diagnostics" { channel="lightwaverf:s22:mylocation:KitchenSocket2:1#diagnostics" }
String	KitchenSocket2_PeriodOfBroadcast "Kitchen Socket 2 Broadcast"   { channel="lightwaverf:s22:mylocation:KitchenSocket2:1#periodOfBroadcast" }
Color	KitchenSocket2_RgbColor			 "Kitchen Socket 2 Colour"      { channel="lightwaverf:s22:mylocation:KitchenSocket2:1#rgbColor" }

```
