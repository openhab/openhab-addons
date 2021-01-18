# Haas Sohn Pellet Stove Binding

The binding for Haassohnpelletstove communicates with a Haas and Sohn Pelletstove through the optional
WIFI-Modul. More information about the WIFI-Modul can be found here: https://www.haassohn.com/de/ihr-plus/WLAN-Funktion

## Supported Things

| Things                    | Description                                                                  | Thing Type |
|---------------------------|------------------------------------------------------------------------------|------------|
| haassohnpelletoven        | Control of a Haas & Sohn Pellet Stove                                        | oven	    |



## Thing Configuration

In general two parameters are required. The IP-Address of the WIFI-Modul of the Stove in the local Network and the Access PIN of the Stove.
The PIN can be found directly at the stove under the Menue/Network/WLAN-PIN

Thing oven  [ hostIP="192.168.0.23", hostPIN=1234]



## Channels

In V0.1 the following channels are yet supported:

| channel  | type               | description                                              |
|----------|--------------------|----------------------------------------------------------|
| prg      | Switch 	 	    | Turn the Stove On/Off		                               |
| isTemp   | Number:Temperature | Receives the actual Temperature of the stove	           |
| spTemp   | Number:Temperature | Receives and Sets the Target Temperature of the stove	   |
| mode     | String             | Receives the actual mode the stove is in          	   |


## Full Example

demo.items:

```java
Number:Temperature isTemp { channel="oven:isTemp" }
Number:Temperature spTemp { channel="oven:spTemp" }
String mode   { channel="oven:mode" }
Switch prg    	{ channel="oven:prg" }
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
