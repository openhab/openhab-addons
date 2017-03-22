# Niko Home Control Binding

The openHAB2 Niko Home Control binding integrates with a [Niko Home Control](http://www.nikohomecontrol.be/) system through a Niko Home Control IP-interface.

The binding has been tested with a Niko Home Control IP-interface (550-00508). This IP-interface provides access on the LAN. The binding does 
not require a Niko Home Control Gateway (550-00580).
It has also been confirmed to work with the Niko Home Control Connected Controller (550-00003).

The binding exposes all actions from the Niko Home Control System that can be triggered from the smartphone/tablet interface, as defined in the Niko Home Control programming software.

## Supported Things

The Niko Home Control Binding supports on/off actions (e.g. for lights or groups of lights), dimmers and rollershutters or blinds.

## Binding Configuration

The bridge representing the Niko Home Control IP-interface needs to be added in the things file or Paper UI. No bridge configuration is required. The communication only works if the openHab system and the Niko Home Control IP-interface are in the same subnet. Only one Niko Home Control IP-interface can exist in the subnet, therefore only one bridge should be added to openHab.

Optionally the IP-address and port can be set. If the IP-address is set, the binding will search for a Niko Home Control IP-interface in the subnet of the IP-address and update the address accordingly.

The port is set to 8000 by default and should not be overwritten.

## Discovery

When the Niko Home Control bridge is added, system information will be read from the Niko Home Control Controller and will be put in the properties, visible through Paper UI.

Subsequently, all defined actions that can be triggered from a smartphone/tablet in the Niko Home Control system will be discovered and put in the inbox.
It is possible to trigger a manual scan for things on the Niko Home Control bridge.

If the Niko Home Control system has locations configured, these will be copied to thing locations and grouped as such in PaperUI.

## Thing Configuration

Besides adding automatically discovered things through PaperUI, you can add thing definitions in the things file.

The Thing configuration for the bridge uses the following syntax:

    Bridge nikohomecontrol:bridge:<bridgeID> [ ADDR="<IP-Address of bridge>", PORT=<listening port>, REFRESH="<Refresh interval>" ]

`bridgeID` can have any value.

All parameters are optional. ADDR will trigger a search for a Niko Home Control IP-interface in the subnet of the given ADDR, but can be omitted. PORT will be the PORT used to connect and is 8000 by default. REFRESH is the interval to restart the communication in minutes (300 by default), if 0 or omitted the connection will not restart.

The Thing configuration for the actions has the following syntax:

    Thing nikohomecontrol:<thing type>:<bridgeID>:<oHActionID>
                        [ ACTIONID=<Niko Home Control action ID>,
                          STEP=<dimmer increase/decrease step value> ]

or nested in the bridge configuration:

    <thing type> <oHActionID> [ ACTIONID=<Niko Home Control action ID>,
                                STEP=<dimmer increase/decrease step value> ]

The following thing types are valid for configuration:

    onOff, dimmer, blind

`oHActionID` can have any value, but will be set to the same value as the ACTIONID parameter if discovery is used.

The ACTIONID parameter is the unique ip Interface Object ID (`ipInterfaceObjectId`) as automatically assigned in the Niko Home Control Controller when programming the Niko Home Control system using the Niko Home Control programming software. It is not visible but will be detected and automatically set by openHAB discovery. For textual configuration, it can be manually retrieved from the content of the .nhcp configuration file created by the programming software. Open the file with an unzip tool to read it's content.

The STEP parameter is only available for dimmers. It sets a step value for dimmer increase/decrease actions. The parameter is optional and set to 10 by default.

## Channels

For thing type `onOff` the supported channel is `switch`. OnOff command types are supported.

For thing type `dimmer` the supported channel is `brightness`. OnOff, IncreaseDecrease and Percent command types are supported. Note that sending an ON command will switch the dimmer to the value stored when last turning the dimmer off, or 100% depending on the configuration in the Niko Home Control Controller. This can be changed with the Niko Home Control programming software.

For thing type `blind` the supported channel is `rollershutter`. UpDown, StopMove and Percent command types are supported.


## Limitations

The binding has been tested with a Niko Home Control IP-interface (550-00508) and the Niko Home Control Connected Controller (550-00003).

The action events implemented are limited to onOff, dimmer and rollershutter or blinds. Other actions have not been implemented.

It is not possible to tilt the slats of venetian blinds.

Beyond action events, the Niko Home Control communication also supports thermostats, electricity usage data and alarms. This has not been implemented.

## Example

.things:

    Bridge nikohomecontrol:bridge:nhc [ ADDR="192.168.0.70", PORT=8000, REFRESH=300 ] {
        onOff 1 [ ACTIONID=1 ]
        dimmer 2 [ ACTIONID=2, STEP=5 ]
        blind 3 [ ACTIONID=3 ]
    }

.items:

    Switch LivingRoom       {channel="nikohomecontrol:onOff:nhc:1#switch"}          # Switch for onOff type action
    Dimmer TVRoom           {channel="nikohomecontrol:dimmer:nhc:2#brightness"}     # Changing brightness dimmer type action
    Rollershutter Kitchen   {channel="nikohomecontrol:blind:nhc:3#rollershutter"}   # Controlling rollershutter or blind type action


.sitemap:

    Switch item=LivingRoom
    Slider item=TVRoom
    Switch item=TVRoom          # allows switching dimmer item off or on (with controller defined behavior)
    Rollershutter item=Kitchen

