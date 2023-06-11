# Nikobus Binding

This binding allows openHAB to interact with the Nikobus home automation system.

[![Demo Video Nikobus](https://img.youtube.com/vi/QiNb-8QxXpo/0.jpg)](https://www.youtube.com/watch?v=QiNb-8QxXpo)

More specifically, it allows openHAB to:

- send (simulated) button presses to the Nikobus,
- react to button presses which occur on the Nikobus,
- change the status of switch channels on a Nikobus switch module,
- request the status of switch channels on a Nikobus switch module,
- change the status of dimmer channels on a Nikobus dimmer module,
- request the status of dimmer channels on a Nikobus dimmer module,
- send commands to the Nikobus roller shutter module.

This binding works with at least the following hardware:

- PC-link module (05-200),
- Push buttons (05-060-01, 05-064-01), RF Transmitter (05-314), PIR Sensor (430-00500),
- 4 channel switch module (05-002-02),
- 12 channel switch module (05-000-02),
- 12 channel dimmer module.

## Supported Things

The binding supports a serial connection via `nikobus:pc-link` bridge to the Nikobus installation (PC-Link module):

The bridge enables communication with other Nikobus components:

- `switch-module` - Nikobus switch module, i.e. `05-000-02`,
- `dimmer-module` - Nikobus dim-controller module, i.e. `05-007-02`,
- `rollershutter-module` - Nikobus roller shutter module,
- `push-button` - Nikobus physical push button.

## Bridge Configuration

The binding can connect to the PC-Link via serial interface.

```java
Bridge nikobus:pc-link:mypclink [ port="<serial port>", refreshInterval=<interval> ] {
}
```

where:

- `port` is the name of the serial port used to connect to the Nikobus installation
- `refreshInterval` defines how often the binding reads Nikobus module's status, so having i.e. 30 as above, the binding will read one module’s status each 30s, iterating through all modules, one by one. If one does not specify `refreshInterval`, a default value of 60s is used.

## Thing Configuration

Once connected to the Nikobus installation using a bridge, one can communicate with:

- `switch-module`,
- `dimmer-module`,
- `rollershutter-module`,
- `push-button`.

### Modules

Each module is defined by its address and contains 12 outputs (channels), where `output-1` corresponds to module's first output, `output-2` to module's second output and so on.
If physical module has less outputs, only those channels can be used (i.e. `05-002-02` has only 4 outputs, so only channels 1-4 can be used).

Large module contains 2 channel groups, where the first group controls channels 1-6 and the second one controls channels 7-12.
The small module contains only a single channel group controlling all 4 channels.

All commands sent to/received from the Nikobus switch module are for a single channel group.

In order to be able to read the status of a Nikobus module channel or to switch a channel directly on the module without mimicking a button press, items for each channel of a module needs to be configured.

#### switch-module

```java
Thing switch-module s1 [ address = "BC00" ]
```

Defines a `switch-module` with address `BC00`.

| channel   | type   | description  |
|-----------|--------|--------------|
| output-1  | Switch | Output 1     |
| output-2  | Switch | Output 2     |
| output-3  | Switch | Output 3     |
| output-4  | Switch | Output 4     |
| output-5  | Switch | Output 5     |
| output-6  | Switch | Output 6     |
| output-7  | Switch | Output 7     |
| output-8  | Switch | Output 8     |
| output-9  | Switch | Output 9     |
| output-10 | Switch | Output 10    |
| output-11 | Switch | Output 11    |
| output-12 | Switch | Output 12    |

#### dimmer-module

```java
Thing dimmer-module d1 [ address = "D969" ]
```

Defines a `dimmer-module` with address `D969`.

| channel   | type   | description  |
|-----------|--------|--------------|
| output-1  | Dimmer | Output 1     |
| output-2  | Dimmer | Output 2     |
| output-3  | Dimmer | Output 3     |
| output-4  | Dimmer | Output 4     |
| output-5  | Dimmer | Output 5     |
| output-6  | Dimmer | Output 6     |
| output-7  | Dimmer | Output 7     |
| output-8  | Dimmer | Output 8     |
| output-9  | Dimmer | Output 9     |
| output-10 | Dimmer | Output 10    |
| output-11 | Dimmer | Output 11    |
| output-12 | Dimmer | Output 12    |

#### rollershutter-module

```java
Thing rollershutter-module r1 [ address = "4C6C" ]
```

Defines a `rollershutter-module` with address `4C6C`.

| channel   | type          | description  |
|-----------|---------------|--------------|
| output-1  | Rollershutter | Output 1     |
| output-2  | Rollershutter | Output 2     |
| output-3  | Rollershutter | Output 3     |
| output-4  | Rollershutter | Output 4     |
| output-5  | Rollershutter | Output 5     |
| output-6  | Rollershutter | Output 6     |

In case rollershutters are moving in the oposite direction when sending `UP` or `DOWN` commands, there is a `reverse` parameter, which can be set to `true` in this case to reverse the rollershutter's direction. Defaults to `false`.

##### Estimating Position

Nikobus rollershuter module does not provide information about rollershutter's position. In order to bridge this gap, an optional parameter `duration` can be set per channel, describing the amount of time needed by a rollershutter to get from open to closed state (or vice-versa).

Binding uses this information to interpolate rollershutter’s position. On startup binding will assume completely open rollershutters but opening/closing a rollershutter once should bring it back in sync.

After `duration` seconds elapsed, binding will set module's output back to neutral (OFF) state after additional number of seconds, as specified by the `delay` parameter. If not specified, it defaults to 5 seconds.

Example:

`duration = 30s`

binding will automatically switch Nikobus rollershutter module’s output to OFF after

`30s + 5s = 35s`

**Note:** Please ensure all Nikobus Push Buttons manipulating rollershutters have `impactedModules` set so binding is notified about changes.

### Buttons

Once an openHAB item has been configured as a Nikobus button, it will receive a status update to ON when the physical button is pressed.
When an item receives the ON command from openHAB, it will send a simulated button press to the Nikobus.
This means one could also define virtual buttons in openHAB with non-existing addresses (e.g., `000001`) and use those in the programming of Nikobus installation.

To configure an item for a button in openHAB with address `28092A`, use the following format:

```java
Thing push-button pb1 [ address = "28092A" ]
```

Since all the channels in the entire channel group are switched to their new state, it is important that openHAB knows the current state of all the channels in that group.
Otherwise a channel which was switched on by a button, may be switched off again by the command.

In order to keep an up to date state of the channels in openHAB, button configurations can be extended to include detail on which channel groups the button press affects.

When configured, the status of the channel groups to which the button is linked, will be queried every time the button is pressed.
Every status query takes between ~300 ms, so to get the best performance, only add the affected channel groups in the configuration, which has the following format:

```java
Thing push-button <id> [ address = "<address>", impactedModules = "<moduleType>:<moduleId>:<channelGroup>, <moduleType>:<moduleId>:<channelGroup>, ..." ]
```

where:

- `moduleType` represents module's type,
- `moduleId` represents module's id,
- `channelGroup` represents the first (1) or second (2) channel group in the module.

 Example configurations may look like:

```java
Thing switch-module s1 [ address = "FF2A" ]
Thing push-button pb1 [ address = "28092A", impactedModules = "switch-module:s1:1" ]
```

In addition to the status requests triggered by button presses, there is also a scheduled status update interval defined by the `refreshInterval` parameter and explained above.

#### Push Button Trigger Channels

Beside receiving a status update (ON) when a physical Nikobus push button is pressed (and kept pressed), additional triggers can be added and configured to determine how press&hold of a physical push button should generate trigger events. Two types of trigger channels are supported:

- filter trigger and
- button trigger.

##### Filter Trigger

- `command` - command to be send,
- `delay` - a required delay in milliseconds defining how much time must a button be pressed before an initial trigger event is fired,
- `period` - optional time in milliseconds between successive triggers.

Examples:

- `command = PRESSED, delay = 0, period = <empty>` - triggers `PRESSED` event immediatelly when Nikobus button is pressed and is not triggered anymore while holding down the button,
- `command = INCREMENT, delay = 1000, period = 500` - triggers initial `INCREMENT` event after 1 second and then every half a second while holding down the button.

##### Button Trigger

`threshold` - a required long-press threshold in miliseconds. Defines how long must a button be pressed before a long press event is triggered - pressing&holding a Nikobus push-button for `threshold` or more miliseconds will trigger long press event, otherwise a short press event will be triggered.

## Discovery

Pressing a physical Nikobus push-button will generate a new inbox entry with an exception of buttons already discovered or setup.

Nikobus push buttons have the following format in inbox:

```text
Nikobus Push Button 14E7F4:3
4BF9CA
nikobus:push-button
```

where first line contains name of the discovered button and second one contains button's bus address.

Each discovered button has a Nikobus address appended to its name, same as can be seen in Nikobus's PC application, `14E7F4:3` in above example.

- `14E7F4` - address of the Nikobus switch, as can be seen in Nikobus PC software and
- `3` - represents a button on Nikobus switch.

### Button mappings

#### 2 buttons switch

![Nikobus Switch with 2 buttons](doc/s2.png)

```text
 1 = A
 2 = B
```

#### 4 buttons switch

![Nikobus Switch with 4 buttons](doc/s4.png)

maps as

```text
 3  1  
 4  2
```

so

```text
1 = C
2 = D
3 = A
4 = B
```

#### 8 buttons switch

![Nikobus Switch with 8 buttons](doc/s8.png)

maps as

```text
 7  5  3  1  
 8  6  4  2
```

so

```text
1 = 2C
2 = 2D
3 = 2A
4 = 2B
5 = 1C
6 = 1D
7 = 1A
8 = 1B
```

Above example `14E7F4:3` would give:

- for 4 buttons switch - push button A,
- for 8 buttons switch - push button 2A.

## Full Example

### nikobus.things

```java
Bridge nikobus:pc-link:mypclink [ port = "/dev/ttyUSB0", refreshInterval = 10 ] {
    Thing dimmer-module d1 [ address = "0700" ]
    Thing dimmer-module d2 [ address = "6B00" ]

    Thing switch-module s1 [ address = "FF2A" ]
    Thing switch-module s2 [ address = "4C6C" ]
    Thing switch-module s3 [ address = "A063" ]

    Thing rollershutter-module r1 [ address = "D769" ]

    Thing push-button 92092A "S_2_1_2A" [ address = "92092A", impactedModules = "switch-module:s1:1" ]
    Thing push-button D2092A "S_2_1_2B" [ address = "D2092A", impactedModules = "switch-module:s1:1" ]
    Thing push-button 12092A "S_2_1_2C" [ address = "12092A", impactedModules = "dimmer-module:d1:1" ]
    Thing push-button 52092A "S_2_1_2D" [ address = "52092A", impactedModules = "dimmer-module:d1:1" ]

    Thing push-button 1EE5F2 "S_2_3_A" [ address = "1EE5F2", impactedModules = "dimmer-module:d1:2" ]
    Thing push-button 5EE5F2 "S_2_3_B" [ address = "5EE5F2", impactedModules = "dimmer-module:d1:2" ]

    Thing push-button 0C274A "S_2_4_A" [ address = "0C274A", impactedModules = "dimmer-module:d1:2" ]
    Thing push-button 4C274A "S_2_4_B" [ address = "4C274A", impactedModules = "dimmer-module:d1:2" ]

    Thing push-button 1D1FF2 "S_2_5_A" [ address = "1D1FF2", impactedModules = "switch-module:s1:1" ]
    Thing push-button 5D1FF2 "S_2_5_B" [ address = "5D1FF2", impactedModules = "switch-module:s1:1" ]
}
```

### nikobus.items

```java
Dimmer Light_FF_Gallery_Ceiling "Ceiling" (FF_Gallery, Lights) [ "Lighting" ] { channel="nikobus:dimmer-module:mypclink:d1:output-1" }
Dimmer Light_FF_Bed_Ceiling "Ceiling" (FF_Bed, Lights) [ "Lighting" ] { channel="nikobus:dimmer-module:mypclink:d1:output-7" }
Dimmer Light_FF_Child_Ceiling "Ceiling" (FF_Child, Lights) [ "Lighting" ] { channel="nikobus:dimmer-module:mypclink:d2:output-10" }
Dimmer Light_FF_Child_Wall_Left "Wall Left" (FF_Child, Lights) [ "Lighting" ] { channel="nikobus:dimmer-module:mypclink:d1:output-11" }
Dimmer Light_FF_Child_Wall_Right "Wall Right" (FF_Child, Lights) [ "Lighting" ] { channel="nikobus:dimmer-module:mypclink:d1:output-12" }
Dimmer Light_FF_PlayRoom_Ceiling "Ceiling" (FF_PlayRoom, Lights) [ "Lighting" ] { channel="nikobus:dimmer-module:mypclink:d1:output-6" }
Dimmer Light_FF_PlayRoom_Wall "Wall" (FF_PlayRoom, Lights) [ "Lighting" ]  { channel="nikobus:dimmer-module:mypclink:d1:output-4" }

Switch Light_FF_Gallery_Wall "Wall" (FF_Gallery, Lights) [ "Lighting" ] { channel="nikobus:switch-module:mypclink:s1:output-4" }
Switch Light_FF_Bath_Ceiling "Ceiling" (FF_Bath, Lights) [ "Lighting" ] { channel="nikobus:switch-module:mypclink:s3:output-2" }
Switch Light_FF_Wardrobe_Ceiling "Ceiling" (FF_Wardrobe, Lights) [ "Lighting" ] { channel="nikobus:switch-module:mypclink:s1:output-1" }
Switch Light_FF_Corridor_Ceiling "Ceiling" (FF_Corridor, Lights) [ "Lighting" ] { channel="nikobus:switch-module:mypclink:s2:output-3" }

Rollershutter Shutter_GF_Corridor "Corridor" (GF_Corridor, gShuttersGF) { channel="nikobus:rollershutter-module:mypclink:r1:output-1" }
Rollershutter Shutter_GF_Bed "Bedroom" (GF_Bed, gShuttersGF) { channel="nikobus:rollershutter-module:mypclink:r1:output-3" }
Rollershutter Shutter_GF_Bath "Bathroom" (GF_Bath, gShuttersGF) { channel="nikobus:rollershutter-module:mypclink:r1:output-2" }
Rollershutter Shutter_FF_Child "Child's room" (FF_Child, gShuttersFF) { channel="nikobus:rollershutter-module:mypclink:r1:output-4" }
Rollershutter Shutter_FF_Gallery "Gallery" (FF_Gallery, gShuttersFF) { channel="nikobus:rollershutter-module:mypclink:r1:output-5" }
```
