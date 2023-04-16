# MyNice Binding

This binding implements the support of the IT4Wifi module through the NHK protocol and enables management of Nice gates actuators.
IT4Wifi is a bridge between the TP4 bus of your gate and your Ethernet network.

## Supported Things

- `it4wifi`: The Bridge between openHAB and your module.
- `swing`: A Thing representing a swinging (two rotating doors) gate.
- `sliding`: A Thing representing a sliding gate.

## Discovery

The binding will auto-discover (by MDNS) your module, creating the associated `it4wifi` bridge.

Once discovered, a user named “org.openhab.binding.mynice” will be created on it.
You will have to grant him permissions using the MyNice app (Android or IOS).

Once configuration of the bridge is completed, your gate(s) will also be auto-discovered and added to the Inbox.

## Thing Configuration

First configuration should be done via UI discovery, this will let you get automatically the password provided by the IT4Wifi module.
Once done, you can also create your things via *.things file.

### `it4wifi` Bridge Configuration

| Name       | Type | Description                                                            | Default | Required | Advanced |
|------------|------|------------------------------------------------------------------------|---------|----------|----------|
| hostname   | text | Hostname or IP address of the device                                   | N/A     | yes      | no       |
| password   | text | Password to access the device                                          | N/A     | yes      | no       |
| macAddress | text | The MAC address of the IT4Wifi                                         | N/A     | yes      | no       |
| username   | text | Pairing Key needed to access the device, provided by the bridge itself | N/A     | yes      | no       |

### Gates Thing Configuration

| Name       | Type | Description                                                            | Default | Required | Advanced |
|------------|------|------------------------------------------------------------------------|---------|----------|----------|
| id         | text | ID of the gate on the TP4 bus connected to the bridge                  | N/A     | yes      | no       |

## Channels

There is no channel associated with the bridge.

Channels available for the gates are :

| Channel   | Type   | Read/Write | Description                                              |
|-----------|--------|------------|----------------------------------------------------------|
| status    | String | R          | Description of the current status of the door (1)        |
| obstruct  | Switch | R          | Flags an obstruction, blocking the door                  |
| moving    | Switch | R          | Indicates if the device is currently operating a command |
| command   | String | W          | Send a given command to the gate (2)                     |
| t4command | String | W          | Send a T4 Command to the gate                            |

(1) : can be open, closed, opening, closing, stopped.
(2) : must be "stop","open","close"

### T4 Commands

Depending upon your gate model and motor capabilities, some T4 commands can be used.
The list of available commands for your model will be automatically discovered by the binding.
This information is stored in the `allowedT4` property held by the gate Thing itself.

Complete list of T4 Commands :

| Command | Action                     |
|---------|----------------------------|
| MDAx    | Step by Step               |
| MDAy    | Stop (as remote control)   |
| MDAz    | Open (as remote control)   |
| MDA0    | Close (as remote control)  |
| MDA1    | Partial opening 1          |
| MDA2    | Partial opening 2          |
| MDA3    | Partial opening 3          |
| MDBi    | Apartment Step by Step     |
| MDBj    | Step by Step high priority |
| MDBk    | Open and block             |
| MDBl    | Close and block            |
| MDBm    | Block                      |
| MDEw    | Release                    |
| MDEx    | Courtesy ligh timer on     |
| MDEy    | Courtesy light on-off      |
| MDEz    | Step by Step master door   |
| MDE0    | Open master door           |
| MDE1    | Close master door          |
| MDE2    | Step by Step slave door    |
| MDE3    | Open slave door            |
| MDE4    | Close slave door           |
| MDE5    | Release and Open           |
| MDFh    | Release and Close          |

## Full Example

### things/mynice.things

```java
Bridge mynice:it4wifi:83eef09166 "Nice IT4WIFI" @ "portail" [
            hostname="192.168.0.198",
            macAddress="00:xx:zz:dd:ff:gg",
            password="v***************************zU=",
            username="neo_prod"] {
      swing 1 "Nice POA3 Moteur Portail" @ "portail" [id="1"]
}
```

### items/mynice.items

```java
String   NiceIT4WIFI_GateStatus    "Gate Status" <gate>   (gMyniceSwing) ["Status","Opening"]     {channel="mynice:swing:83eef09166:1:status"}
String   NiceIT4WIFI_Obstruction   "Obstruction" <none>   (gMyniceSwing)                          {channel="mynice:swing:83eef09166:1:obstruct"}
Switch   NiceIT4WIFI_Moving        "Moving"      <motion> (gMyniceSwing) ["Status","Vibration"]   {channel="mynice:swing:83eef09166:1:moving"}
String   NiceIT4WIFI_Command       "Command"     <none>   (gMyniceSwing)                          {channel="mynice:swing:83eef09166:1:command"}

```
