# Haas Sohn Pellet Stove Binding

The binding for Haassohnpelletstove communicates with a Haas and Sohn Pelletstove through the optional
WIFI module. More information about the WIFI module can be found here: https://www.haassohn.com/de/ihr-plus/WLAN-Funktion

## Supported Things

| Things                    | Description                                                                  | Thing Type |
|---------------------------|------------------------------------------------------------------------------|------------|
| haassohnpelletoven        | Control of a Haas & Sohn Pellet Stove                                        | oven	    |



## Thing Configuration

In general two parameters are required. The IP-Address of the WIFI-Modul of the Stove in the local Network and the Access PIN of the Stove.
The PIN can be found directly at the stove under the Menue/Network/WLAN-PIN

```Thing haassohnpelletoven:oven:myOven "Pelletstove"  [ hostIP="192.168.0.23", hostPIN=1234]

## Channels

The following channels are yet supported:

| channel  		  | type               | description                                              						  |
|-----------------|--------------------|----------------------------------------------------------------------------------|
| channelPrg      | Switch 	 	       | Turn the Stove On/Off		                              						  |
| channelIsTemp   | Number:Temperature | Receives the actual temperature of the stove	          						  |
| channelSpTemp   | Number:Temperature | Receives and sets the target temperature of the stove	  						  |
| channelMode     | String             | Receives the actual mode the stove is in like heating, cooling, error, ....	  |


## Full Example

demo.items:

```
Number:Temperature isTemp { channel="oven:isTemp" }
Number:Temperature spTemp { channel="oven:spTemp" }
String mode   { channel="oven:mode" }
Switch prg    	{ channel="oven:prg" }
```

## Tested Hardware

The binding was succesfully tested with the following ovens:

- HSP 7 DIANA
- HSP6 434.08
