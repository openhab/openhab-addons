# openHAB NEEO Integration

This integration will allow openHAB things/items to be exposed to the NEEO Brain and allow the NEEO smart remote to control them.
NEEO is a smart home solution that includes an IP based remote.
More information can be found at [NEEO](https://neeo.com) or in the forums at [NEEO Planet](https://planet.neeo.com).
**This integration was not developed by NEEO** - so please don't ask questions on the NEEO forums.

The openHAB NEEO integration allows mapping of openHAB things/item to a NEEO device/capabilities (see Mappings below).
Additionally, this integration provides full two-way communication between an openHAB instance and a NEEO Brain.

This integration compliments the NEEO binding by exposing a brain's devices to all other brains as well (allowing you to create multi-brain recipes).

## Features

The openHAB NEEO Integration will provide the following:

- Automatic discovery of NEEO brains on the network and automatic registering of the openHAB as an SDK.
- A NEEO dashboard tile that will show the status of NEEO Brain connections and provide the ability to customize the mapping between openHAB things/item and NEEO device/capabilities.
- Discovery of openHAB things on the NEEO app.
- Full two-way communcation between openHAB and brain.

Item changes in openHAB will appear in NEEO and vice-versa.

## Troubleshooting

If searching for openHAB devices on the NEEO Brain is always returning nothing, here are a few tips to solve the issue:

1. Read sections IP Address and openHAB Primary Address below.
1. Make sure the openHAB primary address is set to an address that is reachable from the NEEO Brain (see openHAB Primary Address section below).
1. Make sure your firewall is not blocking access to the openHAB server
1. Your search criteria has included too many openHAB devices (especially if "Expose ALL" setting has been turned on).

The NEEO brain has an (unknown) size limit to the amount of items that can be returned for a search and you may be going beyond that limit.
Narrow your search to a specific item to see if you were hitting that search limit.

### IP Address

Whatever openHAB (with this integration) runs on will need a static IP address assigned to it.
The integration will register the IP address with the brain and the brain will callback to the IP address.
If your IP address changes, the brain will no longer be able to reach the integration and operation will fail (such as a search or receiving any updates in NEEO).

### openHAB Primary Address

This integration will use the primary address defined in openHAB to register itself as an SDK with the NEEO Brain (allowing the NEEO Brain to discover your things and to callback to openHAB to retrieve item values).
If you change the primary address option, this binding will de-register the old address and re-register the new address with the NEEO Brain.

## Mappings

For openHAB things/items to appear on the NEEO system, you must create mappings between openHAB and NEEO.
To accomplish this, go to the NEEO Integration dashboard (typically <http://localhost:8080/neeo/index.html>).
This will open up a screen similar to the following

![Configuration](doc/dashboardmain.png)

### Tabs

There are a number of tabs:

1. Brains - will allow you to view the status of any connected brain (see Brains Tab below).
1. Things - will allow you to manage the exposure of things to the brain (see Things Tab below).

Press the refresh button (next to the tab header) to refresh the information on the particular tab.

### Brains Tab

The Brains tab provides a listing of all NEEO brains that have been found, some information about them and their current status (connected or not).

If your brain has not been discovered, you may manually enter the brain by pressing the "Add Brain" button.
You will be prompted for the Brain's IP address and once entered, the brain should be listed in the resulting table.

#### Actions

1. The 'phone' icon will bring up the EUI associated with the NEEO Brain.
1. The 'file' icon will allow you to view the associated NEEO Brain's log.
1. The 'dot' icon will blink the LED on the associate NEEO Brain (allowing you to identify the brain if you have multiple ones).
1. The 'trash' icon will disconnect from the related Brain.

This will allow you to remove a stale (no longer used) Brain or remove the Brain to be rediscovered again (which may fix a communication issue with the brain).

Please note the integration will properly handle IP address changes.
If the brain is assigned a new IP address, the integration will reconnect to the new IP address change when it receives the notification.
You can force the issue immediately by doing an "Add Brain" with the new IP address.

### Things Tab

The Things tab is where you specify the mappings.

There are two types of devices listed here:

1. openHAB things - any OH2 thing in the openHAB system and optionally (see configuration), any NEEO binding thing.
1. virtual things - these are things you defined that can mix/match any OH1 or OH2 item.

openHAB things can be mapped to a NEEO device and the items within the thing can be mapped to NEEO device capabilities (buttons, lists, switches, sliders, etc).
Virtual things represent a 'thing' that doesn't exist in openHAB but that you would like to define as a NEEO device.
You can then add OH1/OH2 items to that virtual thing and those items will be mapped to a NEEO device capabilities.

There are two types of items supported:

1. Any valid openHAB item (OH1 or OH2).
1. Trigger channels - any trigger channel defined in an openHAB thing or any trigger channel that you add to an existing thing (regardless if it is an openHAB thing or a virtual thing).

Trigger channels are useful to add to an existing thing if you want to add a button on NEEO that when pushed, can perform some rule that you have defined on a trigger.

Please review [NEEO SDK](https://github.com/NEEOInc/neeo-sdk) documentation before attempting to map.
This document describes some (but not all) of the requirements for NEEO things and capabilities.
The NEEO SDK document is written from the perspective of writing native plugins for NEEO but much of its concepts apply here.

Mappings consist of two parts:

1. Mapping of an openHAB thing to a NEEO device or the creation of a new virtual thing.
1. Mapping of an openHAB item or trigger channels to one or more NEEO capabilities.

Please see this screen shot for reference to the next two sections.
This screen show partially shows the setup of my Russound (whole house music system):
![Configuration](doc/dashboardthings.png)

#### NEEO Text Label

The NEEO text label type will allow you to specify showing the label and text or just the text.
To show both a label and the text on the NEEO remote, uncheck the check mark next the the associated label field and enter in the label you wish to assign to the text.
By turning off the label and specifying the format of the text field, you will be able to create your own custom label then.

##### NEEO List

You can now specify a LIST type for items.
A list is simply a list of labels and associated values that the user can choose from on the remote (similar, in concept, to a drop down).
Simply choose the LIST type and press the icon that appears next to the label.
You can then build the list by specifying:

1. The value to send to openHAB if the item is chosen.
1. The label shown on the remote for that value.
1. An optional URI to an image to show for the line.

The image will be scaled down by the remote.
Best practice: although you can specify any URI, to avoid issues where the internet is not available (or the source of the image is offline) - put the image in your conf/images directory and specify the URI to that.
That way the image will always be available if openHAB is running.

Please note that any changes to the LIST will become active IMMEDIATELY upon saving changes.
There is no need to drop and re-add the device if you make changes (even though the UI may say to re-add the device).

#### Thing to Device

The first step in mapping is to map an openHAB thing to a NEEO device type.
You may either choose an existing openHAB item or create a new 'virtual' thing by pressing the "+ Virtual" button and assign a name to it.

The following action can then be performed:

1. Press the "+" (or "-") icon to see/hide the items for the thing.
1. Press the "i" button to see information about the thing (online status, etc).
1. [Virtual things] Modify the device name.
1. Specify or choose the NEEO device type for the openHAB thing.
1. [Virtual things] Press the "+" icon to add new items to the device.
1. Press the puzzle piece icon to add new trigger channel to the device.
1. Press the SAVE icon to save the mapping for the openHAB thing.
1. Press the REFRESH icon to refresh the mapping to the last saved state (discarding any pending changes).
1. [openHAB things] Press the RESTORE icon to restore the mapping to its original content (discarding any pending changes).
1. [Virtual things] Press the DELETE icon to delete the thing.
1. [things with trigger channel(s)] Press the RULES icon to download an example .rules file for the triggers.
1. [NON ACCESSORIE/LIGHT]  Specify device timings (see below).

The list of device types is unknown, must match those that NEEO expects (such as "ACCESSORIE", "AUDIO", etc) and you have the ability to enter the device type directly or select from a list of device types that have been discovered (although all may not be functional on the NEEO brain yet).
Please review the NEEO SDK documentation for hints on what device types are supported and what capabilities are supported by each device type.

Please note that "ACCESSORIE" is not a misspelling.
That is how the NEEO brain expects it.
You may specify a type of "ACCESSORY" when defining a new type, but after saving - the type will switch back to "ACCESSORIE"

#### Properties Page

The properties page (accessible via the gear icon) will present properties specific to the device.

##### Advanced Properties

1. The driver version can be used to override the driver version sent to the brain.  Please note that this number is automatically incremented by one whenever you save the device definition.
1. The 'Specific name' can be used to override the thing name presented to the NEEO Brain.
1. The 'Custom Icon' can be used to assign a custom icon to the device (if left blank, the icon is assigned by the NEEO Brain according to the NEEO Type).

The only 'officially' supported custom icon is "sonos" however you can assign any variety of icons available on the brain.
A list of some of the icons that can be assigned: ![Configuration](doc/icons.png)

##### Device Timings

You can specify three device timings for any non ACCESSORIE and non LIGHT thing:

1. ON - specify how long (in milliseconds) it take the device to turn on.
1. SWITCH - specify how long (in milliseconds) it takes the device to switch inputs.
1. OFF - specify how long (in milliseconds) it takes the device to turn off.

If the device does not have power state or doesn't support input switching, the numbers will be ignored.

##### Device Capabilities

This following device capabilities are available:

1. "Always On" - check if there is no power management for the device.

You do NOT need to specify any POWER buttons or POWER STATE sensor nor will the device be marked as 'stupid'.

#### Example

In the example screen shown above:

1. The Russound AM/FM tuner was mapped to an "ACCESSORIE" NEEO type.
1. The Russound Great Room zone was mapped to an "AUDIO" device (to allow volume keys to work).

Any device type that is marked with any type will be visible to the NEEO App when searching for devices.

### Format

When you have a text label, you can specify the text format to use and will be prefilled if the channel provides a default.
You can use any standard java string format for the value.
You may also provide a transformation format (in the same format as in .items file).
Example: "MAP(stuff.map):%s" will use the MAP tranformation file "stuff.map" to convert the value to a string.

#### Items to Capabilities

The second step is to map openHAB items to one or more NEEO capabilities.
A NEEO capability can either be a virtual item on the screen or a hard button on the remote.

For each item, you may:

1. Press the ADD icon to add a new mapping from the openHAB item (or DELETE icon to delete the mapping).
1. Specify or choose the NEEO capability type for the openHAB item.
1. Specify the NEEO label (or hard button) for the mapping.
1. Optionally set the format or command for the mapping.

At the time of this writing, the following NEEO capability types are supported:

1. Text Label - this will simply take the toString() of the item value and optionally format via the Java String Format found in the "Format/Command" field before sending it to the NEEO brain.
1. Button - this will create virtual button with the text from the "NEEO Label".
Upon pressing the button, the specified command in the "Format/Command" will be sent to the item (or ON will be sent if nothing has been specified).
Please note that you can also specify a hard button in the "NEEO Label" - in which case nothing will appear on the NEEO remote screen and the action will occur from the NEEO remote hard button.
You must specify all the hard buttons for a capability (as specified in the NEEO SDK documentation) for the button to work.
Example: if you only defined VOLUME DOWN but not VOLUME UP - the button will not work on the remote.
Likewise, which hard buttons are active or not additionally depends on the NEEO device type.
1. Switch - this will create a virtual switch with the text from the "NEEO Label" and will send an ON or OFF command to the associated item.
Additionally, a switch can be bound to hard button pairs (the VOLUME keys, the POWER ON/OFF, the CHANNELS, etc).
The command that is sent is dependent on the KEYS chosen (POWER ON/OFF will send ON/OFF to the underlying item, all others will send an INCREASE/DECREASE).
Similar to the "Button" type - please review the NEEO SDK documentation.
1. Slider - this will create a virtual slider that will send the associated value to the item.
The value sent will always be between 0 and 100.
1. ImageURL - this will create an image on the remote from the toString() of the item value (assuming it is a URL to an image).
1. Sensor - this will create a sensor (non-visual) that can be used in recipes on the brain.
1. Power - this will create a powerstate sensor on the brain that can be used to stop/start the device.
NOTE: you MUST also assign a POWER OFF/POWER ON for this to work.
1. List - this will create a directory on the brain that can be used to show a list.

The label assigned will show up on the remote to start the list processing.
When a user selects a list item on the remote, the command that will be sent will be the value associated with the list item selected.

Please note the value for each of the hard buttons is specified in the NEEO SDK documentation.

The following chart shows what openHAB command types are supported for each NEEO Capability type:

| NEEO Capability Type | openHAB Command Type                                                                                                         |
|----------------------|------------------------------------------------------------------------------------------------------------------------------|
| Text Label           | Any                                                                                                                          |
| Button               | Any non-readonly item                                                                                                        |
| Switch               | onofftype, increasedecreasetype, nextprevioustype,openclosedtype,playpausetype,rewindfastforwardtype,stopmovetype,updowntype |
| Slider               | percenttype, decimaltype, hsbtype, quantitytype                                                                              |
| ImageURL             | stringtype                                                                                                                   |
| Power                | onofftype                                                                                                                    |
| List                 | stringtype, decimaltype                                                                                                      |

##### HSBType

HSBType has three attributes - Hue, Brightness and Saturation.
This type is special in that the integration will create 4 capabilities for it:

1. The first capability will you to control the on/off and will be named simply "item".
1. The second capability will allow you to control the HUE and will be named "item (Hue)".
1. The third capability will allow you to control the brightness and will be named "item (Bri)".
1. The forth capability will allow you to control the saturation and will be named "item (Sat)".

If you are trying to bind a LIFX or HUE bulb, here are the following channels you need to create to enable the NEEO Light capability:

1. Set the device type to "LIGHT".
1. Set the overall (HSBType) item to a NEEO type of "Power".
1. Duplicate the overall item and on the duplicate, set the NEEO Type to "Switch" with the label "POWERONOFF".
1. Duplicate the overall item (again) and on the duplicate, set the NEEO Type to "Switch" with a label of "power".
1. Set the HUE/SATURATION/TEMPERATURE to a NEEO type of "Slider" (you can set the BRIGHTNESS as well - but NEEO will automatically assign that for you).

Please note that NEEO will automatically combine all your "LIGHT" types into a single light on the remote (not ideal).
You will get a single screen with all lights listed with a power toggle and slider for brightness.
You will need to add the HUE/SATURATION/TEMPERATURE as shortcuts.

##### Power State Capability type

The power state capability type _REQUIRES_ a POWER ON and POWER OFF button to be assigned as well.
This is a NEEO Brain requirement for the power state.
Sending ON to the power state item will start the device (similar to POWER ON button) and will stop the device on OFF.

##### Virtual Device Items

When you press the "+" icon (on the device) to add new items to the virtual device.
You will be presented with a screen that will allow you to:

1. Add a new item by pressing the "Add Item" button.
1. Delete all your items by pressing the "Delete All" button.
1. Import OH1 items from an .item files by pressing the "Import Item" button.

The items section then provide a list of the items you have specified.
To specify a new item, simply click on the line in question - and then enter the item name.
All item names (OH1 and OH2) are shown in the dropdown (and will allow you to search/choose from that dropdown).
You will NOT be able to select an item not shown in this list - all items must be valid openHAB items.

You can delete individual items by pressing the "-" key next to it.

Press OK will add those items to the virtual thing.
Pressing cancel will simply dismiss the window without doing any action.

#### Trigger Channels

Pressing the puzzle piece icon will add a new trigger channel to the associated thing.
A trigger channel will appear as a button on the NEEO Brain (with the specified label) and when pressed, will create a trigger event (with the optional payload) to be consumed by a rule.
You can use the RULES icon (on the device) to download an example .rules file with the rule definition for all triggers defined.

##### Example

In the example screen above:

1. The "KeyRelease Event" item was duplicated 3 times and bound to the hard buttons "CURSOR LEFT", "CURSOR RIGHT", "CURSOR UP" and "CURSOR DOWN".
Furthermore the command string "MenuLeft" will be sent to the "KeyRelease Event" item when the "CURSOR LEFT" hard button is pressed on the remote (right/up/down for the other buttons).
1. The "Source" item was duplicated once and will create two virtual buttons on the NEEO screen labeled "AM/FM" and "PANDORA".
Pressing the "AM/FM" button the remote screen will send the value 1 to the "Source" item (2 if pressing the "PANDORA" button).
1. The "Status" item was bound to both the power state and a switch assigned to the POWER ON/OFF hard buttons.
Pressing the POWER ON/OFF will send ON/OFF to the "Status" item.
1. The "Volume" item was duplicated once.

The first instance is assigned to a text label that will then be formatted with the "VOLUME % of %%".
The second instance binds the item to a switch that uses the hard volume buttons on the remote.
When the volumes keys are pressed, a "INCREASE" or "DECREASE" will be sent to the item.

### Integration Storage

All data used by the integration is stored under the "userdata/neeo" directory  (please note that this directory is shared with the NEEO binding).
You may backup and restore these files as needed.
If you restore the directory, you will likely need to restart openHAB for the new file contents to be applied.

There are two files being stored by the integration:

1. discoveredbrains.json will contain the brains that are discovered or manually added from the 'brains' tab.
As brains are discovered, manually added or removed, this file will be updated.
1. neeodefinitions.json will contain the device mappings defined on the 'things' tab.

As definitions are saved, this file will be updated.

## Firmware

The following are notes on some of the NEEO Firmwares:

### 52.10

The following changes have occurred:

1. Added support for driver version
1. Added support for UI actions on lists

### 51.1

The following changes have occurred:

1. Added support for the LIST type.
1. Changed internals to reflect new naming standards enforced by NEEO.
1. Updated search to provide better results (matches NEEO search perfectly now).

As of this firmware, NEEO is enforcing some internal naming standards (sdk name, device name and sensor names).
It is **STRONGLY** recommended that you delete and readd all the devices you have after upgrading to this version.
The old devices will still work but there is one danger to leaving them.
As part of this firmware, there is a new "SDK Integeration" found on the NEEO Brain.
Within this screen, there is a button the "Cleanup unused adapters" that will remove SDK's that have no references to them.
Unfortunately this functionality depends on the new naming standards and existing devices will be 'stranded' (and rendered non-working) since the openHAB SDK name did not match that naming standard.
According to NEEO, there is a chance that future firmware changes will continue looking for these naming standards and there may be additional issues with these devices.

### 50.x

The following changes have occurred:

1. Added support for the MUSICPLAYER type.
1. Added support to hide the label for a text field.

### 49.2

The following changes have occurred:

1. SKIP BACKWARD should be changed to PREVIOUS.
1. SKIP FORWARD should be changed to NEXT.
1. ENTER should be changed to CURSOR ENTER.
1. Added support for 'specificname' (allowing you to override the name shown in NEEO).
1. Added support for HSBType channels (creates 4 channels: overall, hue, brightness and saturation).
1. Added NEEO Brain name to the Brain tab.

## Configuration

After installing this add-on, you can configure the integration using the "NEEO Integration" settings in the UI.

Alternatively, you can configure the settings in the file `conf/services/neeo.cfg`:

```ini
############################## openHAB NEEO Integration #############################

# A boolean value describing whether to expose all things/items
# by default or not.
# Default is 'false'.
#exposeAll=true|false

# Whether to automatically expose things from NEEO Binding
# Default is 'true'
#exposeNeeoBinding=true|false

# The maximum number of search results to return to the brain for any given
# search request.
# Default is 10
#searchLimit=10

# The interval (in seconds) to check the status of the brain to determine if the
# brain is reachable or not
# Default is 10
#checkStatusInterval=10
```
