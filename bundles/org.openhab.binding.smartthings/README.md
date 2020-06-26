# Samsung Smartthings Binding

This binding integrates the Samsung Smartthings Hub into OpenHAB. This is implemented as an OpenHAB 2 binding.

**This binding only works with the Smartthings Classic App**

## Supported things

This binding supports most of the Smartthings devices that are defined in the [Smartthings Capabilities list](http://docs.smartthings.com/en/latest/capabilities-reference.html). If you find a device that doesn't work [follow these instructions](doc/Troubleshooting.md) to collect the required data so it can be added in a future release.

## Discovery

Discovery allows openHAB to examine a binding and automatically find the Things available on that binding. Discovery is supported and has been extensively tested.

Discovery is not run automatically on startup. Therefore to run the discovery process perform the following:

1. Start the PaperUI
2. Click on **Configuration > Things**
3. Click on **ADD THINGS** and select the Smartthings Binding.
4. On the displayed screen select the Smartthings Hub.
5. In the Configurations Parameters section enter the IP of Smartthings hub and enter the port number, which is probably 39500. Click on the check mark
6. Click on **Inbox**
7. At the bottom of the screen click on **SEARCH FOR THINGS**
8. Select **Smartthings Binding**
9. You should now see the Smartthings Things in the Inbox
10. More information on using discovery is available in the [configuration tutorial](https://www.openhab.org/docs/configuration)

## Smartthings Configuration

Prior to running the binding the Smartthings hub must have the required openHAB software installed. [Follow these instructions](doc/SmartthingsInstallation.md)

**The binding will not work until this part has been completed, do not skip this part of the setup.**

## openHAB Configuration

This binding is an openHAB 2 binding and uses the Bridge / Thing design with the Smartthings Hub being the Bridge and the controlled modules being the Things. The following definitions are specified in the .things file.

### Bridge Configuration

The bridge requires the IP address and port used to connect the openHAB server to the Smartthings Hub.

    Bridge smartthings:smartthings:Home    [ smartthingsIp="192.168.1.12", smartthingsPort=39500 ] {

where:

* **smartthings:smartthings:Home** identifies this is a smartthings hub named Home. The first two segments must be smartthings:smartthings. You can choose any unique name for the the last segment. The last segment is used when you identify items connected to this hubthingTypeId. 
* **smartthingsIp** is the IP address of the Smartthings Hub. Your router should be configured such that the Smartthings Hub is always assigned to this IP address.
* **smartthingsPort** is the port the Smartthings hub listens on. 39500 is the port assigned by Smartthings so it should be used unless you have a good reason for using another port.

**Warning** This binding only supports one Bridge. If you try to configure a second bridge it will be ignored.

### Thing Configuration

Each attached thing must specify the type of device and it's Smartthings device name. The format of the Thing description is:

    Thing <thingTypeId> name [ smartthingsName="<deviceName>" ]
    
where:

* **thingTypeId** corresponds to the "Preferences Reference" in the Smartthings Capabilities document but without the capability. prefix. i.e. A dimmer switch in the Capabilities document has a Preferences reference of capability.switchLevel, therefore the &lt;thingTypeId&gt; is switchLevel.
* **name** is what you want to call this thing and is used in defining the items that use this thing. 
* **deviceName** is the name you assigned to the device when you discovered and connected to it in the Smartthings App


**Example**

    Bridge smartthings:smartthings:Home    [ smartthingsIp="192.168.1.12", smartthingsPort=39500 ] {
        Thing switchLevel              KitchenLights           [ smartthingsName="Kitchen lights" ]
        Thing contactSensor            MainGarageDoor          [ smartthingsName="Garage Door Open Sensor" ]
        Thing temperatureMeasurement   MainGarageTemp          [ smartthingsName="Garage Door Open Sensor" ]
        Thing battery                  MainGarageBattery       [ smartthingsName="Garage Door Open Sensor" ]
        Thing switch                   OfficeLight             [ smartthingsName="Family Room" ]
        Thing valve                    SimulatedValve          [ smartthingsName="Simulated Valve" ]
    }

## Items

These are specified in the .items file. This section describes the specifics related to this binding. Please see the [Items documentation](https://www.openhab.org/docs/configuration/items.html) for a full explanation of configuring items.

The most important thing is getting the **channel** specification correct. The general format is:

    { channel="smartthings:<thingTypeId>:<hubName>:<thingName>:<channelId>" }

The parts (separated by :) are defined as:

1. **smartthings** to specify this is a smartthings device
2. **thingTypeId** specifies the type of the thing  you are connecting to. This is the same as described in the last section.
3. **hubName** identifies the name of the hub specified above. This corresponds to the third segment in the **Bridge** definition.
4. **thingName** identifes the thing this is attached to and is the "name" you specified in the **Thing** definition.
5. **channelId** corresponds the the attribute in the [Smartthings Capabilities list](http://docs.smartthings.com/en/latest/capabilities-reference.html). For switch it would be "switch".

**Example**

    Dimmer  KitchenLights        "Kitchen lights level"     <slider>          { channel="smartthings:switchLevel:Home:KitchenLights:level" }
    Switch  KitchenLightSwitch   "Kitchen lights switch"    <light>           { channel="smartthings:switchLevel:Home:KitchenLights:switch" }
    Contact MainGarageDoor       "Garage door status [%s]" <garagedoor>       { channel="smartthings:contactSensor:Home:MainGarageDoor:contact" }  
    Number  MainGarageTemp       "Garage temperature [%.0f]"  <temperature>   { channel="smartthings:temperatureMeasurement:Home:MainGarageTemp:temperature" }  
    Number  MainGarageBattery    "Garage battery [%.0f]"  <battery>           { channel="smartthings:battery:Home:MainGarageBattery:battery" }  
    Switch  OfficeLight          "Office light"    <light>                    { channel="smartthings:switch:Home:OfficeLight:switch" }
    String  SimulatedValve       "Simulated valve"                            { channel="smartthings:valve:Home:SimulatedValve:valve" }

**Special note about Dimmers**
There is a conceptual difference between how openHAB and Smartthings configures the dimmer and switch parts of a Dimmer. The Smartthings dimmer (capability name: switchLevel) is only able to accept a numeric value between 0 and 100 representing the brightness percentage. The openHAB dimmer is able to accept both the percentage and on/off. The openHAB PaperUI shows a dimmer with both a slider and switch. The Off/On part of the level is not able to track changes made in the Smartthings App. However the openHab Dimmer has been defined with both level and switch channels. Therefore the dimmer and associated switch will work well together if the switchLevel Thing is selected in the discovery inbox. The Switch Thing can be left in the inbox. For an example see the KitchenLights thing and items above.

**Special note about Valves**
Smarttings includes a **valve** which can be Open or Closed but openHAB does not include a Valve item type. Therefore, the valve is defined as a having an item type of String. And, therefore the item needs to be defined with an item type of string. It can be controlled in the sitemap by specifying the Element type of Switch and providing a mapping of: mappings=[open="Open", closed="Close"]. Such as:

    Switch item=SimulatedValve mappings=[open="Open", closed="Close"]
    
**RGB Bulb example**
Here is a sample configuration for a RGB bulb, such as a Sengled model E11-N1EA bulb. Currently this binding does not have a RGB specific bulb therefore a Thing is required for each part of the bulb.

**Example**

**things file**

    colorControl            SengledColorControl         [ smartthingsName="Sengled Bulb"]
    colorTemperature        SengledColorTemperature     [ smartthingsName="Sengled Bulb"]
    switch                  SengledSwitch               [ smartthingsName="Sengled Bulb"]
    switchLevel             SengledSwitchLevel          [ smartthingsName="Sengled Bulb"]

**items file**

    Color  SengledColorControl    "Sengled bulb color"   <colorpicker>   {channel="smartthings:colorControl:Home:SengledColorControl:color"}
    Number SengledTemperature     "Sengled bulb color temperature"       {channel="smartthings:colorTemperature:Home:SengledColorTemperature:colorTemperature"}
    Switch SengledSwitch          "Sengled bulb switch"   <switch>       {channel="smartthings:switch:Home:SengledSwitch:switch"}
    Dimmer SengledDimmer          "Sengled bulb dimmer"   <slider>       {channel="smartthings:switchLevel:Home:SengledSwitchLevel:level"}

**sitemap file**

    Frame label="Sengled RGBW Bulb" {
        Switch item=SengledSwitch label="Switch"
        Slider  item=SengledDimmer label="Level [%d]"
        Text item=SengledTemperature label="Color Temperature [%d]"
        Colorpicker item=SengledColorControl label="Color [%s]"  icon="colorwheel"
    }




## References

1. [openHAB configuration documentation](http://docs.openhab.org/configuration/index.html)
2. [Smartthings Capabilities Reference](http://docs.smartthings.com/en/latest/capabilities-reference.html)
3. [Smartthings Developers Documentation](http://docs.smartthings.com/en/latest/index.html)
4. [Smartthings Development Environment](https://graph.api.smartthings.com/)
