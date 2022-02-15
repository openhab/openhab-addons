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
[F454](https://catalogue.bticino.com/BTI-F454-EN),
[MyHOMEServer1](https://catalogue.bticino.com/BTI-MYHOMESERVER1-EN),
[MyHOME_Screen10 (MH4893C)](https://catalogue.bticino.com/BTI-MH4893C-EN),
[MyHOME_Screen3,5 (LN4890)](https://catalogue.bticino.com/BTI-LN4890-EN),
[MH201](https://catalogue.bticino.com/BTI-MH201-EN),
[MH202](https://catalogue.bticino.com/BTI-MH202-EN),
[F455](https://www.homesystems-legrandgroup.com/home?p_p_id=it_smc_bticino_homesystems_search_AutocompletesearchPortlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&_it_smc_bticino_homesystems_search_AutocompletesearchPortlet_journalArticleId=2481871&_it_smc_bticino_homesystems_search_AutocompletesearchPortlet_mvcPath=%2Fview_journal_article_content.jsp),
[MH200N](https://www.homesystems-legrandgroup.com/home?p_p_id=it_smc_bticino_homesystems_search_AutocompletesearchPortlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&_it_smc_bticino_homesystems_search_AutocompletesearchPortlet_journalArticleId=2469209&_it_smc_bticino_homesystems_search_AutocompletesearchPortlet_mvcPath=%2Fview_journal_article_content.jsp),
[F453](https://www.homesystems-legrandgroup.com/home?p_p_id=it_smc_bticino_homesystems_search_AutocompletesearchPortlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&_it_smc_bticino_homesystems_search_AutocompletesearchPortlet_journalArticleId=2703566&_it_smc_bticino_homesystems_search_AutocompletesearchPortlet_mvcPath=%2Fview_journal_article_content.jsp),  etc.

- **ZigBee USB Gateways**, such as [BTicino 3578](https://catalogo.bticino.it/BTI-3578-IT), also known as Legrand 088328

**NOTE** The new BTicino Living Now&reg; and Livinglight Smart&reg; wireless systems are not supported by this binding as they do not use the OpenWebNet protocol.

The following Things and OpenWebNet `WHOs` are supported:

### For BUS/SCS

| Category             | WHO          | Thing Type IDs                      | Description                                                      | Status           |
| -------------------- | :----------: | :---------------------------------: | ---------------------------------------------------------------- | ---------------- |
| Gateway Management   | `13`         | `bus_gateway`                       | Any IP gateway supporting OpenWebNet protocol should work (e.g. F454 / MyHOMEServer1 / MH202 / F455 / MH200N, ...) | Successfully tested: F454, MyHOMEServer1, MyHOME_Screen10, MyHOME_Screen3,5, F455, F452, F453AV, MH201, MH202, MH200N. Some connection stability issues/gateway resets reported with MH202 |
| Lighting             | `1`          | `bus_on_off_switch`, `bus_dimmer`   | BUS switches and dimmers                                         | Successfully tested: F411/2, F411/4, F411U2, F422, F429. Some discovery issues reported with F429 (DALI Dimmers)  |
| Automation           | `2`          | `bus_automation`                    | BUS roller shutters, with position feedback and auto-calibration | Successfully tested: LN4672M2  |
| Temperature Control  | `4`          | `bus_thermo_zone`, `bus_thermo_sensor` | Thermo zones management and temperature sensors (probes). NOTE Central Units (4 or 99 zones) are not fully supported yet. See [Channels - Thermo](#configuring-thermo) for more details. | Successfully tested: H/LN4691, HS4692, KG4691; thermo sensors: L/N/NT4577 + 3455 |
| CEN & CEN+ Scenarios  | `15` & `25`  | `bus_cen_scenario_control`, `bus_cenplus_scenario_control` | CEN/CEN+ scenarios events and virtual activation | Successfully tested: scenario buttons: HC/HD/HS/L/N/NT4680 |
| Dry Contact and IR Interfaces | `25`  | `bus_dry_contact_ir`        | Dry Contacts and IR Interfaces                                  | Successfully tested: contact interfaces F428 and 3477;  IR sensors: HC/HD/HS/L/N/NT4610             |
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
- CEN/CEN+ Scenario Control devices will be discovered by activation only. See [discovery by activation](#discovery-by-activation) for details. After confirming a discovered CEN/CEN+ device from Inbox, activate again its scenario buttons to add button channels automatically

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
- the `where` configuration parameter (`OpenWebNet Address`):
    - example for BUS/SCS:
        - light device with WHERE address Point to Point `A=2 PL=4` --> `where="24"`
        - light device with WHERE address Point to Point `A=03 PL=11` on local bus --> `where="0311#4#01"`
        - CEN scenario with WHERE address Point to Point `A=05 PL=12` --> `where="0512"`
        - CEN+ configured scenario `5`: add a `2` before --> `where="25"`
        - Dry Contact or IR Interface `99`: add a `3` before --> `where="399"`
    - example for ZigBee devices: `where=765432101#9`. The ID of the device (ADDR part) is usually written in hexadecimal on the device itself, for example `ID 0074CBB1`: convert to decimal (`7654321`) and add `01#9` at the end to obtain `where=765432101#9`. For 2-unit switch devices (`zb_on_off_switch2u`), last part should be `00#9`.
 

#### Configuring Thermo

In BTicino MyHOME Thermoregulation (WHO=4) each **zone** has associated a thermostat, additional temperature sensors (optional), actuators and heating/conditioning valves. A zone is associated to at least one thermostat and one actuator.

Thermo zones can be configured defining a `bus_thermo_zone` Thing for each zone with the following parameters:

- the `where` configuration parameter (`OpenWebNet Address`):
    - example BUS/SCS Thermo zone `1` --> `where="1"` 
- the `standAlone` configuration parameter (`boolean`, default: `true`): identifies if the zone is managed or not by a Central Unit (4 or 99 zones). `standAlone=true` means no Central Unit is present in the system.

Temperature sensors can be configured defining a `bus_thermo_sensor` Thing with the following parameters:

- the `where` configuration parameter (`OpenWebNet Address`):
    - example sensor `5` of external zone `00` --> `where="500"`
    - example: slave sensor `3` of zone `2` --> `where="302"`

#### NOTE

Systems with Central Units (4 or 99 zones) are not fully supported yet.


## Channels 

### Lighting, Automation, Power meter, CEN/CEN+ Scenario Events and Dry Contact / IR Interfaces channels

| Channel Type ID (channel ID)             | Applies to Thing Type IDs                                     | Item Type     | Description                                           | Read/Write |
| ---------------------------------------- | ------------------------------------------------------------- | ------------- | ----------------------------------------------------- | :--------: |
| `switch` or `switch_01`/`02` for ZigBee  | `bus_on_off_switch`, `zb_on_off_switch`, `zb_on_off_switch2u` | Switch        | To switch the device `ON` and `OFF`                   |    R/W     |
| `brightness`                             | `bus_dimmer`, `zb_dimmer`                                     | Dimmer        | To adjust the brightness value (Percent, `ON`, `OFF`) |    R/W     |
| `shutter`                                | `bus_automation`                                              | Rollershutter | To activate roller shutters (`UP`, `DOWN`, `STOP`, Percent - [see Shutter position](#shutter-position)) |    R/W     |
| `button#X`         | `bus_cen_scenario_control`, `bus_cenplus_scenario_control` | String        | Trigger channel for CEN/CEN+ scenario events [see possible values](#cen-cen-channels)  |     R (TRIGGER)      |
| `sensor`                              |  `bus_dry_contact_ir`                                    | Switch        | Indicates if a Dry Contact Interface is `ON`/`OFF`, or if a IR Sensor is detecting movement (`ON`), or not  (`OFF`) |     R      |
| `power`                                  | `bus_energy_meter`                                            | Number:Power  | The current active power usage from Energy Meter      |     R      |

### Thermo channels

| Channel Type ID (channel ID) | Applies to Thing Type IDs           | Item Type          | Description                                       | Read/Write | Advanced |
| ---------------------------- | ----------------------------------- | ------------------ | ------------------------------------------------- | :--------: | :------: |
| `temperature`                | `bus_thermo_zone`, `bus_thermo_sensor` | Number:Temperature | The zone currently sensed temperature       | R          | N        |
| `setpointTemperature`        | `bus_thermo_zone`                    | Number:Temperature | The zone setpoint temperature           | R/W        | N        |
| `function`                   | `bus_thermo_zone`                    | String             | The zone set thermo function: `COOLING`, `HEATING` or `GENERIC` (heating + cooling)  | R/W | N |
| `mode`                       | `bus_thermo_zone`                    | String             | The zone set mode: `MANUAL`, `PROTECTION`, `OFF`  | R/W        | N        |
| `speedFanCoil`               | `bus_thermo_zone`                    | String             | The zone fancoil speed: `AUTO`, `SPEED_1`, `SPEED_2`, `SPEED_3`    | R/W | N |
| `actuators`                   | `bus_thermo_zone`                    | String             | The zone actuator(s) status: `OFF`, `ON`, `OPENED`, `CLOSED` , `STOP`, `OFF_FAN_COIL`, `ON_SPEED_1`, `ON_SPEED_2`, `ON_SPEED_3`, `OFF_SPEED_1`, `OFF_SPEED_2`, `OFF_SPEED_3` | R | Y |
| `heatingValves`               | `bus_thermo_zone`                    | String             | The zone heating valve(s) status: `OFF`, `ON`, `OPENED`, `CLOSED` , `STOP`, `OFF_FAN_COIL`, `ON_SPEED_1`, `ON_SPEED_2`, `ON_SPEED_3`, `OFF_SPEED_1`, `OFF_SPEED_2`, `OFF_SPEED_3` | R | Y |
| `conditioningValves`          | `bus_thermo_zone`                    | String             | The zone conditioning valve(s) status: `OFF`, `ON`, `OPENED`, `CLOSED` , `STOP`, `OFF_FAN_COIL`, `ON_SPEED_1`, `ON_SPEED_2`, `ON_SPEED_3`, `OFF_SPEED_1`, `OFF_SPEED_2`, `OFF_SPEED_3`  | R | Y |

### Notes on channels

####  `shutter` position

For Percent commands and position feedback to work correctly, the `shutterRun` Thing config parameter must be configured equal to the time (in ms) to go from full UP to full DOWN.
It's possible to enter a value manually or set `shutterRun=AUTO` (default) to calibrate `shutterRun` automatically: in this case a *UP >> DOWN >> Position%* cycle will be performed automatically the first time a Percent command is sent to the shutter.

- if `shutterRun` is not set, or is set to `AUTO` but calibration has not been performed yet, then position estimation will remain `UNDEF` (undefined)
- if `shutterRun` is wrongly set higher than the actual runtime, then position estimation will remain `UNDEF`: try to reduce shutterRun until you find the right value
- before adding/configuring roller shutter Things it is suggested to have all roller shutters `UP`, otherwise the Percent command won’t work until the roller shutter is fully rolled up
- if OH is restarted the binding does not know if a shutter position has changed in the meantime, so its position will be `UNDEF`. Move the shutter all `UP`/`DOWN` to synchronize again its position with the binding
- the shutter position is estimated based on UP/DOWN timing: an error of ±2% is normal

#### CEN/CEN+ channels

CEN/CEN+ are [TRIGGER channels](https://www.openhab.org/docs/configuration/rules-dsl.html#channel-based-triggers]): they handle events and do not have a state.

A powerful feature is to be able to assign CEN or CEN+ commands to your physical wall switches and use the events they generate to trigger rules in openHAB: this way openHAB becomes a very powerful scenario manager activated by physical BTicino switches.
See [openwebnet.rules](#openwebnet-rules) for an example on how to define rules that trigger on CEN/CEN+ buttons events.

It's also possible to send *virtual press* events on the BUS, for example to enable the activation of MH202 scenarios from openHAB.
See [openwebnet.sitemap](#openwebnet-sitemap) & [openwebnet.rules](#openwebnet-rules) sections for an example on how to use the `virtualPress` action connected to a pushbutton on a sitemap.

- channels are named `button#X` where `X` is the button number on the Scenario Control device
- in the .thing file configuration you can specify the `buttons` parameter to define a comma-separated list of buttons numbers [0-31] configured for the scenario device, example: `buttons=1,2,4`
- possible events are:
    - for CEN:
        - `START_PRESS` - sent when you start pressing the button
        - `SHORT_PRESS` - sent if you pressed the button shorter than 0,5sec (sent at the moment when you release it)
        - `EXTENDED_PRESS` - sent if you keep the button pressed longer than 0,5sec; will be sent again every 0,5sec as long as you hold pressed (good for dimming rules)
        - `RELEASE_EXTENDED_PRESS` - sent once when you finally release the button after having it pressed longer than 0,5sec
    - for CEN+:
        - `SHORT_PRESS` - sent if you pressed the button shorter than 0,5sec (sent at the moment when you release it)
        - `START_EXTENDED_PRESS` - sent once as soon as you keep the button pressed longer than 0,5sec
        - `EXTENDED_PRESS` - sent after `START_EXTENDED_PRESS` if you keep the button pressed longer; will be sent again every 0,5sec as long as you hold pressed (good for dimming rules)
        - `RELEASE_EXTENDED_PRESS` - sent once when you finally release the button after having it pressed longer than 0,5sec


## Full Example

### openwebnet.things:

BUS gateway and things configuration:

```
Bridge openwebnet:bus_gateway:mybridge "MyHOMEServer1" [ host="192.168.1.35", passwd="abcde", port=20000, discoveryByActivation=false ] {
      bus_on_off_switch             LR_switch            "Living Room Light"        [ where="51" ]
      bus_dimmer                    LR_dimmer            "Living Room Dimmer"       [ where="0311#4#01" ]
      bus_automation                LR_shutter           "Living Room Shutter"      [ where="93", shutterRun="10050"]      
      bus_energy_meter              CENTRAL_Ta           "Energy Meter Ta"          [ where="51" ]	
      bus_energy_meter              CENTRAL_Tb           "Energy Meter Tb"          [ where="52" ]	   
      bus_thermo_zone               LR_zone              "Living Room Zone"         [ where="2"]
      bus_thermo_sensor             EXT_tempsensor       "External Temperature"     [ where="500"]
      bus_cen_scenario_control      LR_CEN_scenario      "Living Room CEN"          [ where="51", buttons="4,3,8"]
      bus_cenplus_scenario_control  LR_CENplus_scenario  "Living Room CEN+"         [ where="212", buttons="1,5,18" ]
      bus_dry_contact_ir            LR_IR_sensor         "Living Room IR Sensor"    [ where="399" ]
}
```


ZigBee USB Gateway and things configuration - for radio devices:

```
Bridge openwebnet:zb_gateway:myZBgateway  [ serialPort="COM3" ] {
    zb_dimmer          myZB_dimmer     [ where="765432101#9"]
    zb_on_off_switch   myZB_switch     [ where="765432201#9"]
    zb_on_off_switch2u myZB_2U_switch  [ where="765432300#9"]
}
```

### openwebnet.items:

Example items linked to BUS devices:

NOTE: lights, blinds and zones (thermostat) can be handled  from personal assistants (Google Home, Alexa). In the following example `Google Assistant` was configured  (`ga="..."`) according to the [official documentation](https://www.openhab.org/docs/ecosystem/google-assistant).

```
Switch              iLR_switch            "Light"               (gLivingRoom) { channel="openwebnet:bus_on_off_switch:mybridge:LR_switch:switch", ga="Light" }
Dimmer              iLR_dimmer            "Dimmer [%.0f %%]"    (gLivingRoom) { channel="openwebnet:bus_dimmer:mybridge:LR_dimmer:brightness", ga="Light" }

Rollershutter       iLR_shutter           "Shutter [%.0f %%]"   (gShutters, gLivingRoom) { channel="openwebnet:bus_automation:mybridge:LR_shutter:shutter", ga="Blinds" }

Number:Power        iCENTRAL_Ta           "Power [%.0f %unit%]" { channel="openwebnet:bus_energy_meter:mybridge:CENTRAL_Ta:power" }
Number:Power        iCENTRAL_Tb           "Power [%.0f %unit%]" { channel="openwebnet:bus_energy_meter:mybridge:CENTRAL_Tb:power" }


Group   gLivingRoomZone                         "Living Room Zone"   { ga="Thermostat" [ modes="auto=GENERIC,heat=HEATING,cool=COOLING", thermostatTemperatureRange="7,35", useFahrenheit=false ] }
Number:Temperature  iLR_zone_temp               "Temperature [%.1f %unit%]"   (gLivingRoomZone) { channel="openwebnet:bus_thermo_zone:mybridge:LR_zone:temperature", ga="thermostatTemperatureAmbient" }
Number:Temperature  iLR_zone_setTemp            "SetPoint Temperature"        (gLivingRoomZone) { channel="openwebnet:bus_thermo_zone:mybridge:LR_zone:setpointTemperature", ga="thermostatTemperatureSetpoint" }
String              iLR_zone_fanSpeed           "FanSpeed"                    (gLivingRoomZone) { channel="openwebnet:bus_thermo_zone:mybridge:LR_zone:speedFanCoil" }
String              iLR_zone_mode               "Mode"                        (gLivingRoomZone) { channel="openwebnet:bus_thermo_zone:mybridge:LR_zone:mode" }
String              iLR_zone_func               "Function"                    (gLivingRoomZone) { channel="openwebnet:bus_thermo_zone:mybridge:LR_zone:function", ga="thermostatMode" }
String              iLR_zone_actuators          "Actuators"                   (gLivingRoomZone) { channel="openwebnet:bus_thermo_zone:mybridge:LR_zone:actuators" }
String              iLR_zone_hv                 "Heating valves"              (gLivingRoomZone) { channel="openwebnet:bus_thermo_zone:mybridge:LR_zone:heatingValves" }
String              iLR_zone_cv                 "Conditioning valves"         (gLivingRoomZone) { channel="openwebnet:bus_thermo_zone:mybridge:LR_zone:conditioningValves" }

Number:Temperature  iEXT_temp                   "Temperature [%.1f %unit%]"   (gExternal) { channel="openwebnet:bus_thermo_sensor:mybridge:EXT_tempsensor:temperature" }

String	            iCENPlusProxyItem	        "CEN+ Proxy Item"

Switch              iLR_IR_sensor               "Sensor"                                        { channel="openwebnet:bus_dry_contact_ir:mybridge:LR_IR_sensor:sensor" }

```

Example items linked to OpenWebNet ZigBee devices:

```
Dimmer          iDimmer             "Dimmer [%.0f %%]"                  <DimmableLight>  (gKitchen)                   [ "Lighting" ]  { channel="openwebnet:zb_dimmer:myZBgateway:myZB_dimmer:brightness" }
Switch          iSimpleSwitch       "Kitchen Switch"                    <light>          (gKitchen)                   [ "Lighting" ]  { channel="openwebnet:zb_on_off_switch:myZBgateway:myZB_switch:switch_01" }
Switch          iSwitch_01          "2U first light"                    <light>          (gKitchen)                   [ "Lighting" ]  { channel="openwebnet:zb_on_off_switch2u:myZBgateway:myZB_2U_switch:switch_01" }
Switch          iSwitch_02          "2U second light"                   <light>          (gKitchen)                   [ "Lighting" ]  { channel="openwebnet:zb_on_off_switch2u:myZBgateway:myZB_2U_switch:switch_02" }
```

### openwebnet.sitemap

```
sitemap openwebnet label="OpenWebNet Binding Example Sitemap"
{
    Frame label="Living Room"
    {
          Default item=iLR_switch           icon="light"
          Default item=iLR_dimmer           icon="light"
          Default item=iLR_shutter
          Switch  item=iLR_IR_sensor        mappings=[ON="Presence", OFF="No Presence"]
    }

    Frame label="Energy Meters" icon="energy"
    {
          Default item=iCENTRAL_Ta label="General"      icon="energy" valuecolor=[>3000="red"]
          Default item=iCENTRAL_Tb label="Ground Floor" icon="energy" valuecolor=[>3000="red"]
    }

    Frame label="Living Room Thermo"
    {
          Default   item=iLR_zone_temp      label="Temperature" icon="fire" valuecolor=[<20="red"] 
          Setpoint  item=iLR_zone_set       label="Setpoint [%.1f °C]" step=0.5 minValue=15 maxValue=30
          Selection item=iLR_zone_fanSpeed  label="Fan Speed" icon="fan" mappings=[AUTO="AUTO", SPEED_1="Low", SPEED_2="Medium", SPEED_3="High"]
          Switch    item=iLR_zone_mode      label="Mode" icon="settings"
          Selection item=iLR_zone_func      label="Function" icon="heating" mappings=[HEATING="Heating", COOLING="Cooling", GENERIC="Heating/Cooling"]
          Default   item=iLR_zone_actuators label="Actuators status"
          Default   item=iLR_zone_hv        label="Heating valves status"
          Default   item=iLR_zone_cv        label="Conditioning valves status"
    }
    
    Frame label="CEN+ Scenario activation"
    {
          Switch    item=iCENPlusProxyItem  label="My CEN+ scenario" icon="movecontrol"  mappings=[ON="Activate"]
    }
}
```

### openwebnet.rules

```xtend
rule "CEN+ virtual press from OH button"
/* This rule triggers when the proxy item iCENPlusProxyItem is activated, for example from a button on WebUI/sitemap.
When activated it sends a "virtual short press" event (where=212, button=5) on the BUS 
*/
when 
    Item iCENPlusProxyItem received command
then
    val actions = getActions("openwebnet","openwebnet:bus_cenplus_scenario_control:mybridge:212")
    actions.virtualPress("SHORT_PRESS", 5)
end


rule "CEN dimmer increase"
// A "start press" event on CEN where=51, button=4 will increase dimmer%
when
    Channel "openwebnet:bus_cen_scenario_control:mybridge:51:button#4" triggered START_PRESS
then
    sendCommand(iLR_dimmer, INCREASE)  
end


rule "CEN dimmer decrease"
// A "release extended press" event on CEN where=51, button=4 will decrease dimmer%
when
    Channel "openwebnet:bus_cen_scenario_control:mybridge:51:button#4" triggered RELEASE_EXTENDED_PRESS
then
    sendCommand(iLR_dimmer, DECREASE)  
end
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
