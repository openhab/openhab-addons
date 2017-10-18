# wso2iots Binding

This binding is used to connect with a locally hosted WSO2 IoT Server 3.1.0 (https://wso2.com/iot) and get temperature, humidity, light, motion data from the devices enrolled in the WSO2 IoTS building monitor plugin. The binding allows communication between the openHAB server and WSO2 IoT server.

WSO2 is an opensource middleware company and IoTS is one of the five products offered by the company.(https://wso2.com/)

To use the binding, first the user have to locally setup the WSO2 IoTS (https://wso2.com/iot) and deploy the building monitor plugin (https://github.com/wso2/samples-iots/tree/master/floor-analytics-demo). The building monitor agent uses ESP8266 12E, DHT 11, PIR SR501, LM393. (https://github.com/wso2/samples-iots/tree/master/floor-analytics-demo/BuildingMonitorDevice). After enrolling the device agent access token has to be obtained from https://localhost:9443/api-store/.

## Supported Things

There is exactly one supported thing type, which represents one device agent. It has the `buildingMonitor` id. Of course, you can add multiple Things, e.g. Devices enrolled in different floors and different buildings.

## Discovery

There is no discovery implemented. You have to create your things manually.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                              |
|-----------|------------------------------------------------------------------------- |
| apikey    | Access token to invoke the WSO2 IoTS API for building monitor. Mandatory. |
| deviceId  | Unique ID of the device agent |
| refresh   | Refresh interval in minutes. Optional, the default value is 60 minutes.  |

## Channels

The information that is retrieved is available as these channels:


| Channel ID | Item Type    | Description              |
|------------|--------------|------------------------- |
| motion | Number | Motion level in the range 0-1 |
| temperature | Number | Temperature in Celsius degrees |
| light | Number | Light level in the range 0-1024|
| humidity | Number | Humidity level |

Note that light level 0 mean high brightness and 1024 means very dark

## Full Example

wso2iots.things:

```
wso2iots:buildingMonitor:<thingId> "Home" [ apikey="XXXXXXXXXXXX", deviceId=2940205, refresh=60 ]
wso2iots:buildingMonitor:<thingId> "Office" [ apikey="XXXXXXXXXXXX", deviceId=776895, refresh=60 ]
```

wso2iots.items:

```
Group Wso2iots <flow>

Number	wso2_Temperature	"Temperature" <temperature> (Wso2iots) {channel="wso2iots:buildingMonitor:<thingId>:temperature"}
Number	wso2_Humidity		"Humidity" <humidity> (Wso2iots) {channel="wso2iots:buildingMonitor:<thingId>:humidity"}
Number	wso2_Light		"Light" <light> (Wso2iots) {channel="wso2iots:buildingMonitor:<thingId>:light"}
Number	wso2_Motion		"Motion" <motion> (Wso2iots) {channel="wso2iots:buildingMonitor:<thingId>:motion"}

```

wso2iots.sitemap:

```
sitemap wso2iots label="Building Monitor Application"{

	Frame label="Home"{
		Text item=wso2_Temperature label="Temperature [%.1f °C]" icon="temperature"
		Text item=wso2_Motion label="Motion [%.1f ]" icon="motion"
		Text item=wso2_Humidity label="Humidity [%.1f %%]" icon="humidity"
		Text item=wso2_Light label="Light [%.1f ]" icon="light"
		
	}


}

```


## Any custom content here!


