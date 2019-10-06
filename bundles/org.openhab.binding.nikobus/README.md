# Nikobus Binding

This binding allows openHAB to interact with the Nikobus home automation system.

[![Demo Video Nikobus](https://img.youtube.com/vi/QiNb-8QxXpo/0.jpg)](http://www.youtube.com/watch?v=QiNb-8QxXpo)

More specifically, it allows openHAB to:

* send (simulated) button presses to the Nikobus,
* react to button presses which occur on the Nikobus,
* change the status of switch channels on a Nikobus switch module,
* request the status of switch channels on a Nikobus switch module,
* change the status of dimmer channels on a Nikobus dimmer module,
* request the status of dimmer channels on a Nikobus dimmer module,
* send commands to the Nikobus roller shutter module.

This binding works with at least the following hardware:

* PC-link module (05-200),
* Push buttons (05-060-01, 05-064-01), RF Transmitter (05-314), PIR Sensor (430-00500),
* 4 channel switch module (05-002-02),
* 12 channel switch module (05-000-02),
* 12 channel dimmer module.

## Supported Things

The binding supports a serial connection via `nikobus:pc-link` bridge to the Nikobus installation (PC-Link module):

The bridge enables communication with other Nikobus components:

* `switch-module` - Nikobus switch module, i.e. `05-000-02`,
* `dimmer-module` - Nikobus dim-controller module, i.e. `05-007-02`,
* `rollershutter-module` - Nikobus roller shutter module,
* `push-button` - Nikobus physical push button.

## Discovery

The binding does not support any automatic discovery of Things.

## Bridge Configuration

The binding can connect to the PC-Link via serial interface.

```
Bridge nikobus:pc-link:mypclink [ port="<serial port>", refreshInterval=<interval> ] {
}
```

where:

* `port` is the name of the serial port used to connect to the Nikobus installation
* `refreshInterval` defines how often the binding reads Nikobus module's status, so having i.e. 30 as above, the binding will read one module’s status each 30s, iterating through all modules, one by one. If one does not specify `refreshInterval`, a default value of 60s is used.

## Thing Configuration

Once connected to the Nikobus installation using a bridge, one can communicate with:

* `switch-module`,
* `dimmer-module`,
* `rollershutter-module`,
* `push-button`.

### Modules

Each module is defined by its address and contains 12 outputs (channels), where `output-1` corresponds to module's first output, `output-2` to module's second output and so on.
If physical module has less outputs, only those channels can be used (i.e. `05-002-02` has only 4 outputs, so only channels 1-4 can be used).

Large module contains 2 channel groups, where the first group controls channels 1-6 and the second one controls channels 7-12.
The small module contains only a single channel group controlling all 4 channels.

All commands sent to/received from the Nikobus switch module are for a single channel group.

In order to be able to read the status of a Nikobus module channel or to switch a channel directly on the module without mimicking a button press, items for each channel of a module needs to be configured.

#### switch-module

```
Thing switch-module BC00
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

```
Thing dimmer-module D969
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

```
Thing rollershutter-module 4C6C
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

### Buttons

Once an openHAB item has been configured as a Nikobus button, it will receive a status update to ON when the physical button is pressed.
When an item receives the ON command from openHAB, it will send a simulated button press to the Nikobus.
This means one could also define virtual buttons in openHAB with non-existing addresses (e.g., `000001`) and use those in the programming of Nikobus installation.

To configure an item for a button in openHAB with address `28092A`, use the following format:

```
Thing push-button 28092A
```

Since all the channels in the entire channel group are switched to their new state, it is important that openHAB knows the current state of all the channels in that group.
Otherwise a channel which was switched on by a button, may be switched off again by the command.

In order to keep an up to date state of the channels in openHAB, button configurations can be extended to include detail on which channel groups the button press affects.

When configured, the status of the channel groups to which the button is linked, will be queried every time the button is pressed.
Every status query takes between ~300 ms, so to get the best performance, only add the affected channel groups in the configuration, which has the following format:

```
Thing push-button <address> [ impactedModules = "<moduleAddress>-<channelGroup>, <moduleAddress>-<channelGroup>, ..." ]
```

where:

* `moduleAddress` represents the address of the switch module,
* `channelGroup` represents the first or second channel group in the module.

 Example configurations may look like:

```
Thing push-button 28092A [ impactedModules = "4C6C-2" ]
```

In addition to the status requests triggered by button presses, there is also a scheduled status update interval defined by the `refreshInterval` parameter and explained above.

## Full Example

t.b.d.