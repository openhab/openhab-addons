# Samsung Smartthings Binding

This binding integrates the Samsung Smartthings Cloud into openhab.

The central part would be the "Smarthins Cloud Hub" bridge, that will enable communication between Openhab and Smartthings Cloud.
There will be also an number of differents things for each of your home device.

The Smarthings hub (the one you could have in your home to enable gateway to zigbee / mater & zware device) would be displayed as a thing (smartthings:hub).

Note that having a Smartthings hub is not mandatory to use this binding.
Some appliance devices like hoven, cooktop, dishwasher and others connect directly to Smartthings Cloud using your Wifi, and without using the local Smartthing hub.

## A little background on Smartthings version change

First version of bindings was bases on groovy script to be installed on the local hub.
This version stop working somewhere in 2023 (to be verified) because of deprecation of groovy by samsung.

Second version in early 2024 was never released.  
This version needs a web hook expose to the internet to handle device events.  
It also needs a complicated registration process, creating some smartapps behind the scene.

The new actual version use SSE subscription to handle device events.  
It's more convenient to setup : no need for end user to setup an external web hook.  
Registration process is also far more easy :  

- We use OAuth authentification in place of registration tokens.
- All setup occurs directly inside Openhab.
- The smartapp stuff are totally hide behind the scene, not needing complex setup.

## Supported things

This binding supports most of the Smartthings devices that are defined in the [Smartthings Capabilities list](https://developer-preview.smartthings.com/docs/devices/capabilities/capabilities-reference/). 

If you find a device that doesn't work [follow these instructions](doc/Troubleshooting.md) to collect the required data so it can be added in a future release.

## Smartthings Configuration

**The binding will not work until this part has been completed, do not skip this part of the setup.**

In this version, the binding need to have a redirect URL using openhabcloud to do the first oAuth authorization. This URL will be only use during registration, and not during day to day use of the addons.

URL will be of this form : https://home.myopenhab.org/connectsmartthings

Warning: note that using your personal URL, even if expose on internet, will not work bacause only opencloud URL is registered to Smartthings.

To do the registration, follow this steps:

1. Browse to the URL : https://home.myopenhab.org/connectsmartthings
   You should see a page like this one:

![alt text](doc/Authorize01.png)

2. Click on the Authorize Bridge button.
   You will be redirect to the following page on smartthings.
   If you are already login, go directly to Step 4.
   If not, fill your email, and click on Next.

![alt text](doc/Authorize02.png)

3. Fill your password, and click Connexion.

![alt text](doc/Authorize03.png)

4. On this step, Smartthings should display a page with a combobox to select your location.
  First select your location.
  After this, Smartthings would display the authorization selection.
  Keep all checkbox on, and click "Authorize"

![alt text](doc/Authorize04.png)

5. On this last step, your browser should be redirected to openhab.
   The page will display a confirmation with the selected location, and the number of device foudn in the location.

   You can now close the window, and go to Openhab Inbox to trigger a device scan.

![alt text](doc/Authorize05.png)


## Discovery

Discovery will allow to automically fill Inbox with your smartthings device.

1. Go to the Things pages, click on "+" Button.

![alt text](doc/Scan01.png)

2. Select your binding.

![alt text](doc/Scan02.png)

3. Select your binding.

![alt text](doc/Scan03.png)

4. Click on the "Scan" button
   Your device should display after a few seconds.

![alt text](doc/Scan04.png)


allows openHAB to examine a binding and automatically find the Things available on that binding.
Discovery is supported by the Smartthings binding and is run automatically on startup.



### Bridge Configuration

```
!!! ======================================================================================!!!  
!!! @Todo : bellow this part, documentation needs to be rewrite                           !!!  
!!! ======================================================================================!!!  
```

The bridge requires the IP address and port used to connect the openHAB server to the Smartthings Hub.

```java
Bridge smartthings:smartthings:Home    [ smartthingsIp="192.168.1.12", smartthingsPort=39500 ] {
```

where:

- **smartthings:smartthings:Home** identifies this is a smartthings hub named Home.
    The first two segments must be smartthings:smartthings.
    You can choose any unique name for the the last segment.
    The last segment is used when you identify items connected to this hubthingTypeId.
- **smartthingsIp** is the IP address of the Smartthings Hub.
    Your router should be configured such that the Smartthings Hub is always assigned to this IP address.
- **smartthingsPort** is the port the Smartthings hub listens on. 39500 is the port assigned by Smartthings so it should be used unless you have a good reason for using another port.

**Warning** This binding only supports one Bridge.
If you try to configure a second bridge it will be ignored.

### Thing Configuration

Each attached thing must specify the type of device and it's Smartthings device name. The format of the Thing description is:

```java
Thing <thingTypeId> name [ smartthingsName="<deviceName>", {smartthingsTimeout=<timeout>} ]
```

where:

- **[thingTypeId](https://developer-preview.smartthings.com/docs/devices/capabilities/capabilities-reference/)** corresponds to the "Preferences Reference" in the Smartthings Capabilities document but without the capability.prefix. i.e. A dimmer switch in the Capabilities document has a Preferences reference of capability.switchLevel, therefore the &lt;thingTypeId&gt; is switchLevel.
- **name** is what you want to call this thing and is used in defining the items that use this thing.
- **deviceName** is the name you assigned to the device when you discovered and connected to it in the Smartthings App
- Optional: **timeout** is how long openHAB will wait for a response to the request before throwing a timeout exception. The default is 3 seconds.

#### Example

```java
Bridge smartthings:smartthings:Home    [ smartthingsIp="192.168.1.12", smartthingsPort=39500 ] {
    Thing switchLevel              KitchenLights           [ smartthingsName="Kitchen lights" ]
    Thing contactSensor            MainGarageDoor          [ smartthingsName="Garage Door Open Sensor" ]
    Thing temperatureMeasurement   MainGarageTemp          [ smartthingsName="Garage Door Open Sensor" ]
    Thing battery                  MainGarageBattery       [ smartthingsName="Garage Door Open Sensor" ]
    Thing switch                   OfficeLight             [ smartthingsName="Office Light", smartthingsTimeout=7 ]
    Thing valve                    SimulatedValve          [ smartthingsName="Simulated Valve" ]
}
```

## Items

These are specified in the .items file. This section describes the specifics related to this binding.
Please see the [Items documentation](https://www.openhab.org/docs/configuration/items.html) for a full explanation of configuring items.

The most important thing is getting the **channel** specification correct. The general format is:

```java
{ channel="smartthings:<thingTypeId>:<hubName>:<thingName>:<channelId>" }
```

The parts (separated by :) are defined as:

1. **smartthings** to specify this is a smartthings device
1. **thingTypeId** specifies the type of the thing  you are connecting to. This is the same as described in the last section.
1. **hubName** identifies the name of the hub specified above. This corresponds to the third segment in the **Bridge** definition.
1. **thingName** identifes the thing this is attached to and is the "name" you specified in the **Thing** definition.
1. **channelId** corresponds the the attribute in the [Smartthings Capabilities list](https://docs.smartthings.com/en/latest/capabilities-reference.html). For switch it would be "switch".

### Example

```java
Dimmer  KitchenLights        "Kitchen lights level"     <slider>          { channel="smartthings:switchLevel:Home:KitchenLights:level" }
Switch  KitchenLightSwitch   "Kitchen lights switch"    <light>           { channel="smartthings:switchLevel:Home:KitchenLights:switch" }
Contact MainGarageDoor       "Garage door status [%s]" <garagedoor>       { channel="smartthings:contactSensor:Home:MainGarageDoor:contact" }
Number  MainGarageTemp       "Garage temperature [%.0f]"  <temperature>   { channel="smartthings:temperatureMeasurement:Home:MainGarageTemp:temperature" }
Number  MainGarageBattery    "Garage battery [%.0f]"  <battery>           { channel="smartthings:battery:Home:MainGarageBattery:battery" }
Switch  OfficeLight          "Office light"    <light>                    { channel="smartthings:switch:Home:OfficeLight:switch" }
String  SimulatedValve       "Simulated valve"                            { channel="smartthings:valve:Home:SimulatedValve:valve" }
```

**Special note about Valves** 
Smarttings includes a **valve** which can be Open or Closed but openHAB does not include a Valve item type. Therefore, the valve is defined as a having an item type of String. And, therefore the item needs to be defined with an item type of string. It can be controlled in the sitemap by specifying the Element type of Switch and providing a mapping of: mappings=[open="Open", closed="Close"]. Such as:

```java
Switch item=SimulatedValve mappings=[open="Open", closed="Close"]
```

**RGB Bulb example**
Here is a sample configuration for a RGB bulb, such as a Sengled model E11-N1EA bulb. Currently this binding does not have a RGB specific bulb therefore a Thing is required for each part of the bulb.

## Full Example

### Things File

```java
colorControl            SengledColorControl         [ smartthingsName="Sengled Bulb"]
colorTemperature        SengledColorTemperature     [ smartthingsName="Sengled Bulb"]
switch                  SengledSwitch               [ smartthingsName="Sengled Bulb"]
switchLevel             SengledSwitchLevel          [ smartthingsName="Sengled Bulb"]
```

### Items File

```java
Color  SengledColorControl    "Sengled bulb color"   <colorpicker>   {channel="smartthings:colorControl:Home:SengledColorControl:color"}
Number SengledTemperature     "Sengled bulb color temperature"       {channel="smartthings:colorTemperature:Home:SengledColorTemperature:colorTemperature"}
Switch SengledSwitch          "Sengled bulb switch"   <switch>       {channel="smartthings:switch:Home:SengledSwitch:switch"}
Dimmer SengledDimmer          "Sengled bulb dimmer"   <slider>       {channel="smartthings:switchLevel:Home:SengledSwitchLevel:level"}
```

### Sitemap File

```perl
Frame label="Sengled RGBW Bulb" {
    Switch item=SengledSwitch label="Switch"
    Slider  item=SengledDimmer label="Level [%d]"
    Text item=SengledTemperature label="Color Temperature [%d]"
    Colorpicker item=SengledColorControl label="Color [%s]"  icon="colorwheel"
}
```

## References

1. [openHAB configuration documentation](https://openhab.org/docs/configuration/index.html)
2. [Smartthings Api Documentation](https://developer.smartthings.com/docs/api/public)
3. [Smartthings Capabilities Reference]()
4. [Smartthings Developers Documentation](https://developer.smartthings.com/docs/getting-started/architecture-of-smartthings)
5. [Python implementation](https://github.com/andrewsayre/pysmartthings)
