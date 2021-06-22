# OpenWebNet (BTicino/Legrand) Binding

This binding integrates BTicino / Legrand MyHOME&reg; BUS and ZigBee wireless (MyHOME_Play&reg;) devices using the [OpenWebNet](https://en.wikipedia.org/wiki/OpenWebNet) protocol.

The binding supports:

- both wired BUS/SCS (MyHOME) and wireless setups (MyHOME ZigBee). The two networks can be configured simultaneously
- auto discovery of BUS/SCS IP and ZigBee USB gateways; auto discovery of devices
- commands from openHAB and feedback (events) from BUS/SCS and wireless network


![MyHOMEServer1 Gateway](doc/MyHOMEServer1_gateway.jpg)
![F454 Gateway](doc/F454_gateway.png)
![ZigBee USB Gateway](doc/USB_gateway.jpg)

## Supported Things

In order for this binding to work, a **BTicino/Legrand OpenWebNet gateway** is needed in your home system to talk to devices.

These gateways have been tested with the binding:

- **IP gateways** or scenario programmers, such as BTicino
[F454](https://www.homesystems-legrandgroup.com/BtHomeSystems/productDetail.action?lang=EN&productId=006),
[MyHOMEServer1](https://www.homesystems-legrandgroup.com/BtHomeSystems/productDetail.action?lang=EN&productId=067),
[MyHOME_Screen10](https://www.homesystems-legrandgroup.com/BtHomeSystems/productDetail.action?lang=EN&productId=001),
[MH201](https://www.homesystems-legrandgroup.com/BtHomeSystems/productDetail.action?lang=EN&productId=053),
[MH202](https://www.homesystems-legrandgroup.com/BtHomeSystems/productDetail.action?lang=EN&productId=059),
[F455](https://www.homesystems-legrandgroup.com/BtHomeSystems/productDetail.action?lang=EN&productId=051),
[MH200N](https://www.homesystems-legrandgroup.com/BtHomeSystems/productDetail.action?lang=EN&productId=016),
[F453](https://www.homesystems-legrandgroup.com/BtHomeSystems/productDetail.action?lang=EN&productId=027),  etc.

- **ZigBee USB Gateways**, such as [BTicino 3578](https://catalogo.bticino.it/BTI-3578-IT), also known as Legrand 088328

**NOTE** The new BTicino Living Now&reg; and Livinglight Smart&reg; wireless systems are not supported by this binding as they do not use the OpenWebNet protocol.

The following Things and OpenWebNet `WHOs` are supported:

### For BUS/SCS

| Category             | WHO          | Thing Type IDs                      | Description                                                      | Status           |
| -------------------- | :----------: | :---------------------------------: | ---------------------------------------------------------------- | ---------------- |
| Gateway Management   | `13`         | `bus_gateway`                       | Any IP gateway supporting OpenWebNet protocol should work (e.g. F454 / MyHOMEServer1 / MH202 / F455 / MH200N, ...) | Successfully tested: F454, MyHOMEServer1, MyHOME_Screen10, F455, F452, F453AV, MH201, MH202, MH200N. Some connection stability issues/gateway resets reported with MH202 |
| Lighting             | `1`          | `bus_on_off_switch`, `bus_dimmer`   | BUS switches and dimmers                                         | Successfully tested: F411/2, F411/4, F411U2, F422, F429. Some discovery issues reported with F429 (DALI Dimmers)  |
| Automation           | `2`          | `bus_automation`                    | BUS roller shutters, with position feedback and auto-calibration | Successfully tested: LN4672M2  |
| Temperature Control  | `4`          | `bus_thermostat`, `bus_temp_sensor` | Zones room thermostats (stand-alone) and external wireless temperature sensors. Please note that Central Unit configurations (4 or 99 zones) are not yet supported. See [Channels - Thermo](#thermo-channels) for more details. | Successfully tested: H/LN4691; external sensors: L/N/NT4577 + 3455 |
| Energy Management    | `18`         | `bus_energy_meter`                  | Energy Management                                                | Successfully tested: F520, F521 |

### For ZigBee (Radio)

| Category             | WHO    | Thing Type IDs                    | Description                                                           | Status                               |
| -------------------- | :----: | :-------------------------------: | :-------------------------------------------------------------------: | ------------------------------------ |
| Gateway Management   | `13`   | `zb_gateway`                      | ZigBee USB Gateway (models: BTI-3578 / LG 088328)                     | Tested: BTI-3578 and LG 088328       |
| Lighting             | `1`    | `zb_dimmer`, `zb_on_off_switch`, `zb_on_off_switch2u` | ZigBee dimmers, switches and 2-unit switches      | Tested: BTI-4591, BTI-3584, BTI-4585 |
| Automation           | `2`    | `zb_automation`                   | ZigBee roller shutters                                                |                                      |

## Discovery

Gateway and Things discovery is supported by this binding.

### BUS/SCS Discovery

- BUS Gateway automatic discovery will work only for newer gateways supporting UPnP: F454, MyHOMEServer1, MH201, MH202, MH200N, MyHOME_Screen 10.
For other gateways you can add them manually, see [Thing Configuration](#thing-configuration) below.
- After gateway is discovered and added a connection with default password (`12345`) is tested first: if it does not work the gateway will go offline and an error status will be set. A correct password must then be set in the gateway Thing configuration otherwise the gateway will not become online.
- Once the gateway is online, a second Inbox Scan will discover BUS devices
- BUS/SCS Dimmers must be ON and dimmed (30%-100%) during a Scan, otherwise they will be discovered as simple On/Off switches
    - *KNOWN ISSUE*: In some cases dimmers connected to a F429 Dali-interface are not automatically discovered

#### Discovery by Activation

BUS devices can also be discovered if activated while an Inbox Scan is active: start a new Scan, wait 15-20 seconds and then _while the Scan is still active_, activate the physical device (for example dim the dimmer) to have it discovered by the binding.

If a device cannot be discovered automatically it's always possible to add it manually, see [Configuring Devices](#configuring-devices).

### ZigBee Discovery

- The ZigBee USB Gateway must be inserted in one of the USB ports of the openHAB computer before a discovery is started
- ***IMPORTANT NOTE:*** As for other openHAB bindings using the USB/serial ports, on Linux the `openhab` user must be member of the `dialout` group to be able to use USB/serial port; set the group with the following command:

    ```
    $ sudo usermod -a -G dialout openhab
    ```

    The user will need to logout and login to see the new group added. If you added your user to this group and still cannot get permission, reboot Linux to ensure the new group permission is attached to the `openhab` user.
- Once the ZigBee USB Gateway is added and online, a second Inbox Scan will discover devices connected to it. Because of the ZigBee radio network, device discovery will take ~40-60 sec. Be patient!
- Wireless devices must be part of the same ZigBee network of the ZigBee USB Gateway to discover them. Please refer to [this video by BTicino](https://www.youtube.com/watch?v=CoIgg_Xqhbo) to setup a ZigBee wireless network which includes the ZigBee USB Gateway
- Only powered wireless devices part of the same ZigBee network and within radio coverage of the ZigBee USB Gateway will be discovered. Unreachable or not powered devices will be discovered as *GENERIC* devices and cannot be controlled
- Wireless control units cannot be discovered by the ZigBee USB Gateway and therefore are not supported

## Thing Configuration

### Configuring BUS/SCS Gateway

Configuration parameters are:

- `host` : IP address / hostname of the BUS/SCS gateway (`String`, *mandatory*)
   - Example: `192.168.1.35`
- `port` : port (`int`, *optional*, default: `20000`)
- `passwd` : gateway password (`String`, *required* for gateways that have a password. Default: `12345`)
   - Example: `abcde` or `12345`
   - if the BUS/SCS gateway is configured to accept connections from the openHAB computer IP address, no password should be required
   - in all other cases, a password must be configured. This includes gateways that have been discovered and added from Inbox: without a password configured they will remain OFFLINE
- `discoveryByActivation`: discover BUS devices when they are activated also when a device scan is not currently active (`boolean`, *optional*, default: `false`). See [Discovery by Activation](#discovery-by-activation).

Alternatively the BUS/SCS Gateway thing can be configured using the `.things` file, see `openwebnet.things` example [below](#full-example).

### Configuring Wireless ZigBee USB Gateway

Configuration parameters are:

- `serialPort` : the serial port where the ZigBee USB Gateway is connected (`String`, *mandatory*)
    - Examples: `/dev/ttyUSB0` (Linux/RaPi), `COM3` (Windows)

Alternatively the ZigBee USB Gateway thing can be configured using the `.things` file, see `openwebnet.things` example [below](#full-example).

### Configuring Devices

Devices can be discovered automatically using an Inbox Scan after a gateway has been configured and connected.
For any manually added device, you must configure:

- the associated gateway (`Parent Bridge` menu)
- the `where` config parameter (`OpenWebNet Device Address`):
  - example for BUS/SCS device with WHERE address Point to Point `A=2 PL=4` --> `where="24"`
  - example for BUS/SCS device with WHERE address Point to Point `A=03 PL=11` on local bus --> `where="0311#4#01"`
  - example for BUS/SCS thermo Zones: `Zone=1` --> `where="1"`; external sensor `5` --> `where="500"`
  - example for ZigBee devices: `where=765432101#9`. The ID of the device (ADDR part) is usually written in hexadecimal on the device itself, for example `ID 0074CBB1`: convert to decimal (`7654321`) and add `01#9` at the end to obtain `where=765432101#9`. For 2-unit switch devices (`zb_on_off_switch2u`), last part should be `00#9`.

## Channels 

### Lighting, Automation and Power meter channels

| Channel Type ID (channel ID)             | Applies to Thing Type IDs                                     | Item Type     | Description                                           | Read/Write |
| ---------------------------------------- | ------------------------------------------------------------- | ------------- | ----------------------------------------------------- | :--------: |
| `switch` or `switch_01`/`02` for ZigBee  | `bus_on_off_switch`, `zb_on_off_switch`, `zb_on_off_switch2u` | Switch        | To switch the device `ON` and `OFF`                   |    R/W     |
| `brightness`                             | `bus_dimmer`, `zb_dimmer`                                     | Dimmer        | To adjust the brightness value (Percent, `ON`, `OFF`) |    R/W     |
| `shutter`                                | `bus_automation`                                              | Rollershutter | To activate roller shutters (`UP`, `DOWN`, `STOP`, Percent - [see Shutter position](#shutter-position)) |    R/W     |
| `power`                                  | `bus_energy_meter`                                            | Number:Power  | The current active power usage from Energy Meter      |     R      |

### Thermo channels

Currently only stand-alone thermostats are supported (like  [LN4691](https://catalogo.bticino.it/BTI-LN4691-IT)) and the specific thing `bus_thermostat` was created to manage them.

| Channel Type ID (channel ID) | Applies to Thing Type IDs           | Item Type          | Description                                       | Read/Write | Advanced |
| ---------------------------- | ----------------------------------- | ------------------ | ------------------------------------------------- | :--------: | :------: |
| `temperature`                | `bus_thermostat`, `bus_temp_sensor` | Number:Temperature | The zone currently sensed temperature       | R          | N        |
| `setpointTemperature`        | `bus_thermostat`                    | Number:Temperature | The zone setpoint temperature           | R/W        | N        |
| `function`                   | `bus_thermostat`                    | String             | The zone set thermo function: `COOLING`, `HEATING` or `GENERIC` (heating + cooling)  | R/W | N |
| `mode`                       | `bus_thermostat`                    | String             | The zone set mode: `MANUAL`, `PROTECTION`, `OFF`  | R/W        | N        |
| `speedFanCoil`               | `bus_thermostat`                    | String             | The zone fancoil speed: `AUTO`, `SPEED_1`, `SPEED_2`, `SPEED_3`    | R/W | N |
| `actuator`                   | `bus_thermostat`                    | String             | The zone actuator(s) status: `OFF`, `ON`, `OPENED`, `CLOSED` , `STOP`, `OFF_FAN_COIL`, `ON_SPEED_1`, `ON_SPEED_2`, `ON_SPEED_3`, `OFF_SPEED_1`, `OFF_SPEED_2`, `OFF_SPEED_3` | R | Y |
| `heatingValve`               | `bus_thermostat`                    | String             | The zone heating valve(s) status: `OFF`, `ON`, `OPENED`, `CLOSED` , `STOP`, `OFF_FAN_COIL`, `ON_SPEED_1`, `ON_SPEED_2`, `ON_SPEED_3`, `OFF_SPEED_1`, `OFF_SPEED_2`, `OFF_SPEED_3` | R | Y |
| `conditioningValve`          | `bus_thermostat`                    | String             | The zone conditioning valve(s) status: `OFF`, `ON`, `OPENED`, `CLOSED` , `STOP`, `OFF_FAN_COIL`, `ON_SPEED_1`, `ON_SPEED_2`, `ON_SPEED_3`, `OFF_SPEED_1`, `OFF_SPEED_2`, `OFF_SPEED_3`  | R | Y |


### Notes on channels

#### `shutter` position

For Percent commands and position feedback to work correctly, the `shutterRun` Thing config parameter must be configured equal to the time (in ms) to go from full UP to full DOWN.
It's possible to enter a value manually or set `shutterRun=AUTO` (default) to calibrate `shutterRun` automatically: in this case a *UP >> DOWN >> Position%* cycle will be performed automatically the first time a Percent command is sent to the shutter.

- if `shutterRun` is not set, or is set to `AUTO` but calibration has not been performed yet, then position estimation will remain `UNDEF` (undefined)
- if `shutterRun` is wrongly set higher than the actual runtime, then position estimation will remain `UNDEF`: try to reduce shutterRun until you find the right value
- before adding/configuring roller shutter Things it is suggested to have all roller shutters `UP`, otherwise the Percent command won’t work until the roller shutter is fully rolled up
- if OH is restarted the binding does not know if a shutter position has changed in the meantime, so its position will be `UNDEF`. Move the shutter all `UP`/`DOWN` to synchronize again its position with the binding
- the shutter position is estimated based on UP/DOWN timing: an error of ±2% is normal

## Full Example

### openwebnet.things:

BUS gateway and things configuration:

```xtend
Bridge openwebnet:bus_gateway:mybridge "MyHOMEServer1" [ host="192.168.1.35", passwd="abcde", port=20000, discoveryByActivation=false ] {
      bus_on_off_switch        LR_switch        "Living Room Light"      [ where="51" ]
      bus_dimmer               LR_dimmer        "Living Room Dimmer"     [ where="0311#4#01" ]
      bus_automation           LR_shutter       "Living Room Shutter"    [ where="93", shutterRun="10050"]      
      bus_energy_meter         CENTRAL_Ta       "Energy Meter Ta"        [ where="51" ]	
      bus_energy_meter         CENTRAL_Tb       "Energy Meter Tb"        [ where="52" ]	   
      bus_thermostat           LR_thermostat    "Living Room Thermostat" [ where="2"]
      bus_temp_sensor          EXT_tempsensor   "External Temperature"   [ where="500"]
}
```


ZigBee USB Gateway and things configuration - for radio devices:

```xtend
Bridge openwebnet:zb_gateway:myZBgateway  [ serialPort="COM3" ] {
    zb_dimmer          myZB_dimmer     [ where="765432101#9"]
    zb_on_off_switch   myZB_switch     [ where="765432201#9"]
    zb_on_off_switch2u myZB_2U_switch  [ where="765432300#9"]
}
```

### openwebnet.items:

Example items linked to BUS devices:

```xtend
Switch              iLR_switch                  "Light"                  <light>          (gLivingRoom)                [ "Lighting" ]  { channel="openwebnet:bus_on_off_switch:mybridge:LR_switch:switch" }
Dimmer              iLR_dimmer                  "Dimmer [%.0f %%]"       <DimmableLight>  (gLivingRoom)                [ "Lighting" ]  { channel="openwebnet:bus_dimmer:mybridge:LR_dimmer:brightness" }
Rollershutter       iLR_shutter                 "Shutter [%.0f %%]"      <rollershutter>  (gShutters, gLivingRoom)     [ "Blinds"   ]  { channel="openwebnet:bus_automation:mybridge:LR_shutter:shutter" }
Number:Power        iCENTRAL_Ta                 "Power [%.0f %unit%]"    <energy>                                                      { channel="openwebnet:bus_energy_meter:mybridge:CENTRAL_Ta:power" }
Number:Power        iCENTRAL_Tb                 "Power [%.0f %unit%]"    <energy>                                                      { channel="openwebnet:bus_energy_meter:mybridge:CENTRAL_Tb:power" }
Number:Temperature  iLR_thermostat_temp         "Temperature"                             (gLivingRoom)                                { channel="openwebnet:bus_thermostat:mybridge:LR_thermostat:temperature" }
Number:Temperature  iLR_thermostat_set          "SetPoint Temperature"                    (gLivingRoom)                                { channel="openwebnet:bus_thermostat:mybridge:LR_thermostat:setpointTemperature" }
String              iLR_thermostat_setFanSpeed  "FanSpeed"                                (gLivingRoom)                                { channel="openwebnet:bus_thermostat:mybridge:LR_thermostat:speedFanCoil" }
String              iLR_thermostat_setMode      "Mode"                                    (gLivingRoom)                                { channel="openwebnet:bus_thermostat:mybridge:LR_thermostat:mode" }
String              iLR_thermostat_setFunc      "Function"                                (gLivingRoom)                                { channel="openwebnet:bus_thermostat:mybridge:LR_thermostat:function" }
Number:Temperature  iEXT_temp                   "Temperature [%.1f °C]"  <temperature>    (gExternal)                                  { channel="openwebnet:bus_temp_sensor:mybridge:EXT_tempsensor:temperature" }


```

Example items linked to OpenWebNet ZigBee devices:

```xtend
Dimmer          iDimmer             "Dimmer [%.0f %%]"                  <DimmableLight>  (gKitchen)                   [ "Lighting" ]  { channel="openwebnet:zb_dimmer:myZBgateway:myZB_dimmer:brightness" }
Switch          iSimpleSwitch       "Kitchen Switch"                    <light>          (gKitchen)                   [ "Lighting" ]  { channel="openwebnet:zb_on_off_switch:myZBgateway:myZB_switch:switch_01" }
Switch          iSwitch_01          "2U first light"                    <light>          (gKitchen)                   [ "Lighting" ]  { channel="openwebnet:zb_on_off_switch2u:myZBgateway:myZB_2U_switch:switch_01" }
Switch          iSwitch_02          "2U second light"                   <light>          (gKitchen)                   [ "Lighting" ]  { channel="openwebnet:zb_on_off_switch2u:myZBgateway:myZB_2U_switch:switch_02" }
```

### openwebnet.sitemap

```xtend
sitemap openwebnet label="OpenWebNet Binding Example Sitemap"
{
    Frame label="Living Room"
    {
          Default item=iLR_switch           icon="light"
          Default item=iLR_dimmer           icon="light"
          Default item=iLR_shutter
    }

    Frame label="Energy Meters" icon="energy"
    {
          Default item=iCENTRAL_Ta label="General"      icon="energy" valuecolor=[>3000="red"]
          Default item=iCENTRAL_Tb label="Ground Floor" icon="energy" valuecolor=[>3000="red"]
    }

    Frame label="Thermoregulation"
    {
          Default   item=iLR_thermostat_temp        label="Temperature" icon="fire" valuecolor=[<20="red"] 
          Setpoint  item=iLR_thermostat_set         label="Setpoint [%.1f °C]" step=0.5 minValue=15 maxValue=30
          Selection item=iLR_thermostat_setFanSpeed label="Fan Speed" icon="fan" mappings=[AUTO="AUTO", SPEED_1="Low", SPEED_2="Medium", SPEED_3="High"]
          Switch    item=iLR_thermostat_setMode     label="Mode" icon="settings"
          Selection item=iLR_thermostat_setFunc     label="Function" icon="heating" mappings=[HEATING="Heating", COOLING="Cooling", GENERIC="Heating/Cooling"]
    }
}
```

## Notes

- The OpenWebNet protocol is maintained and Copyright by BTicino/Legrand. The documentation of the protocol if freely accessible for developers on the [Legrand developer web site](https://developer.legrand.com/documentation/open-web-net-for-myhome/)

## Special thanks

Special thanks for helping on testing this binding go to:
[@m4rk](https://community.openhab.org/u/m4rk/),
[@bastler](https://community.openhab.org/u/bastler),
[@gozilla01](https://community.openhab.org/u/gozilla01),
[@enrico.mcc](https://community.openhab.org/u/enrico.mcc),
[@k0nti](https://community.openhab.org/u/k0nti/),
[@gilberto.cocchi](https://community.openhab.org/u/gilberto.cocchi/),
[@llegovich](https://community.openhab.org/u/llegovich),
[@gabriele.daltoe](https://community.openhab.org/u/gabriele.daltoe),
[@feodor](https://community.openhab.org/u/feodor),
[@aconte80](https://community.openhab.org/u/aconte80)
and many others at the fantastic openHAB community!
