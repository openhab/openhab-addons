# Gardena Binding

This is the binding for [Gardena Smart Home](http://www.gardena.com/de/rasenpflege/smartsystem/).
This binding allows you to integrate, view and control all Gardena Smart Home devices in the openHAB environment.

## Supported Things

All devices connected to Gardena Smart Home, currently:
* smart Home Gateway 
* smart Sileno(+) Mower
* smart Water Control
* smart Sensor

The schedules are not yet integrated!

## Discovery

A bridge must be specified, all things for a bridge are discovered automatically.

## Bridge Configuration

There are several settings for a bridge:
- **email** (required)  
The email address for logging in to Gardena Smart Home

- **password** (required)  
The password for logging in to Gardena Smart Home

- **sessionTimeout**  
The timeout in minutes for a session to Gardena Smart Home (default = 30)

- **connectionTimeout**  
The timeout in seconds for connections to Gardena Smart Home (default = 10)

- **refresh**  
The interval in seconds for refreshing the data from Gardena Smart Home (default = 60)

The syntax for a bridge is:
```
gardena:bridge:NAME
```
- **gardena** the binding id, fixed
- **bridge** the type, fixed
- **name** the name of the bridge

#### Example
- minimum configuration
```
Bridge gardena:bridge:home [ email="...", password="..." ]
```

- with refresh
```
Bridge gardena:bridge:home [ email="...", password="...", refresh = 30 ]
```

- multiple bridges
```
Bridge gardena:bridge:home1 [ email="...", password="..." ]
Bridge gardena:bridge:home2 [ email="...", password="..." ]
```

## Thing Configuration
Things are all discovered automatically, you can handle them in PaperUI.  

If you really like to manually configure a thing:

```
Bridge gardena:bridge:home [ email="...", password="..." ]
{
  Thing mower c81ad682-6e45-42ce-bed1-6b4eff5620c8
}
```
The first parameter after Thing is the device type, the second the device id. Supported device types:
* gateway
* mower
* watering_computer
* sensor

## Items
In the items file, you can map the properties, the syntax is:
```
gardena:TYPE:BRIDGE:ID:ABILITY#PROPERTY
```
* **gateway:** the binding id, fixed  
* **type:** the type of the Gardena device  
* **bridge:** the name of the bridge  
* **id:** the id of the Gardena device  
* **ability:** the Gardena ability (channel group)
* **property:** the name of the Gardena property

#### Example:
```
Number Battery_Level "Battery %d %%" {channel="gardena:watering_computer:home:394c3dbe-14c4-4a2a-96f1-ea282518f43c:battery#level"}
```
