#C-Bus Binding

This is the binding for the [Clipsal C-Bus System](http://www2.clipsal.com/cis/technical/product_groups/cbus).
This binding allows you to view and control groups on C-Bus networks from openHAB2.

## Configuration

This binding connects to C-Gate software which can be downloaded from the [Clipsal Downloads Site](https://updates.clipsal.com/ClipsalSoftwareDownload/mainsite/cis/technical/index.html). There is information about setting up the C-Gate software in the [CBus Forums](https://www.cbusforums.com/forums/c-bus-toolkit-and-c-gate-software.4). Make sure that the config/access.txt file allows a connection from computer running openhab.

Whilst all versions of C-Gate should work 2.11.2 contained a fix for handling Indicator Kill messages for trigger groups. Without that they will remain on the last value set and wont match what is shown on CBus devices.

First the CGate Connection bridge needs to be configured with the ip address of the computer running the C-Gate software.
After this a Bridge is creaed for each network configured on the CBus Network. The CBus Project Name and the network Id for that network 


## Supported Things

This binding support 6 different things types

| Thing | Type    | Description  |
|----------------|---------|-----------------------------------|
| cgate | Bridge | This connects to a C-Bus CGate instance to |
| network | Bridge | This connects to a C-Bus Network via a CGate bridge |
| light | Thing | This is for C-Bus lighting groups |
| temperature | Thing | This is for C-Bus temperature groups |
| trigger | Thing | This is for C-Bus trigger groups |
| dali  | Thing | This is for C-Bus DALI dimming groups |

The scan within Paper UI will find all the groups on the CBus network and allow Things to be creaed for them.
##Channels

At startup the binding will scan the network for the values of all the groups and set those on the appropriate channels. It is not possible to fetch the value of a Trigger Group so those values will only be updated when a trigger is set on the CBus network.

### Lights

Light things have 2 channels which show the current state of the group on the cbus network and can also set the state of the group:-

* **Light Channel** - On/Off state of the light
* **Level Channel** - The level of the channel between 0 and 100

### Temperature

Temperature things have 1 channel which shows the current value. This is read-only and will not set the value on the CBus Network.

* **Temperature** - Temperature value

### Trigger

Trigger things have 1 channel which shows the current trigger value on the cbus network and can be used to set a trigger value on the CBus Network.

* **Trigger Channel** - CBus Trigger value

### Dali

Dali things have 1 channel which shows the current value on the cbus network and can be used to set a value on the CBus Network.

* **DALI Channel** - CBus Trigger value




