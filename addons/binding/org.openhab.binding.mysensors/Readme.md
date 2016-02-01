# MySensors Serial Binding

## ID Request

ID Requests from sensors received via the serial gateway are answered, when the bridge/gateway is configured and working. The bridge/gateway will check for a free id in the list of things already configured and in a list of already answered id requests in the current runtime. To reduce the chance that two nodes get the same id, the response is a random id. The random id is between 1 and 254, while already used ids are spared. 

After receiving the id the node begins to represent itself and after that can be discovered.

## Configuring the gateway

The binding currently supports the MySensors SerialGateway and the EthernetGateway. You are free to choose, which gateway to use. The first step to configure the binding is to activate the gateway/bridge. 

You're able to configure the gateway via the PaperUI (Configuration->Things) or with an entry in a things file.

Things file under "conf/things/demo.things".

SerialGateway:

```
  mysensors:bridge-ser:gateway [ serialPort="/dev/pts/2", sendDelay=200 ] {
    /** define things connected to that bridge here */
  }
```

The serial gateway from MySensors works with a baud rate of 115.200. If you're using a different baud rate you need to add an additional parameter "baudRate":

```
  mysensors:bridge-ser:gateway [ serialPort="/dev/pts/2", sendDelay=200, baudRate=115200 ] {
    /** define things connected to that bridge here */
  }
```

  
EthernetGateway:

```
  mysensors:bridge-eth:gateway [ ipAddress="127.0.0.1", tcpPort=5003, sendDelay=200 ] {
     /** define things connected to that bridge here */
  }
```
  
  
## Configuring things

Assuming you have configured a bridge, the next step is to configure things. We use the place holder in the bridge configuration and fill it with content:

conf/things/demo.things:

```
  Bridge mysensors:bridge-ser:gateway [ serialPort="/dev/pts/2", sendDelay=200 ] {
	humidity 		hum01 	[ nodeId="172", childId="0" ]
	temperature		temp01 	[ nodeId="172", childId="1" ]
  }
```
  
Now, we've added an humidity sensor with a nodeId of 172 and childId of 0 according to your Arduino sketch, and a temperature sensor with (172,1).

Now we need the corresponding items:

conf/items/demo.items:

```  
  Number hum01 	{ channel="mysensors:humidity:gateway:hum01:hum" }
  Number temp01	{ channel="mysensors:temperature:gateway:temp01:temp" }
  
```

In the channel configuration:

```
  mysensors: name of the binding
  humidity: thing type
  gateway: the bridge
  hum01: thing connected to the bridge
  hum: channel (there is at least one channel per thing
```
  
Last but not least we create or modify our sitemap:

conf/sitemaps/demo.sitemap:

```
  sitemap demo label="Main Menu" { 
	Frame { 
		Text item=hum01
		Text item=temp01 
	} 
	
  }
```

## Supported gateways

- SerialGateway
- EthernetGateway

## Supported sensors

- S_TEMP
- S_HUM
- S_VOLTAGE
- S_LIGHT
- S_POWER
- S_BARO
- S_DOOR
- S_MOTION
- S_SMOKE
- S_DIMMER
- S_COVER
- S_WIND
- S_RAIN
- S_UV
- S_WEIGHT
- S_DISTANCE
- S_LIGHT_LEVEL


## ToDO

- implement discovery of bridges / gateways
- react on requests from sensors (examples: pulsecount (power sensor), isMetric (temperature)

## Done

- look into items-, rules-, config-files (the OpenHAB 1.X way to implement things) -> Readme.md
- representation of light status -> Fixed
- Added a delay between messages send to the gateway (2015-11-19)
