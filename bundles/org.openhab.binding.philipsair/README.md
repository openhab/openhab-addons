# Philips Air Purifier Binding (org.openhab.binding.philipsair)

The OpenHAB binding provides readings and control of Philips Air Purifier devices. 

This project mostly adopts the work done by [rgerganov](https://github.com/rgerganov/py-air-control/commits?author=rgerganov) to the needs of OpenHAB - big thanks for the reverse engineering the protocol and communication with Philips Air Purifiers devices.
See the project on [py-air-control](https://github.com/rgerganov/py-air-control)

The binding compatible with OpenHAB 2.4 can be found at https://git.dabucomp.com/borowa/org.openhab.binding.philipsair

## Supported Things

Supported Philips Air Purifiers (hardware testes) devices are

+ AC2889/10
+ AC2889/10
+ AC2729
+ AC2729/50
+ AC1214/10
+ AC3829/10

You are welcome to provide any information about compatibility with other versions.

**Features**

* first OpenHAB 2 binding poviding support for Phlips Air Purifiers devices
* auto discovery via UPNP
* power off/on,
* fan speed control,
* purification mode control,
* lights control,
* sensor readings (air quality, temperature, humidity)
* filter status
* child lock
* temperature/humidity offset

## Discovery

This binding can discover Philips Air Purifiers automatically via UPNP protocol.
Currently following devices are recognized precisely, all other Philips Air purifiers family is recognized as 'Universal' thing type

-   AC2889/10
-   AC2729
-   AC2729/50
-   AC1214/10
-   AC3829/10

## Binding Configuration

This binding is able to automatically discover the Philips Air Purifiers on your local network and does not require any extra parameter in order to get any Philips Air Purifier model to work.
The auto-discovery is enabled by default.
To disable it, you can do it via Karaf console or create a file in the services directory called philipsair.cfg with the following content:

```text
org.openhab.philispair:enableAutoDiscovery=false
```

## Thing Configuration

The binding in order to work does not need to configure anything, as UPNP discovery should configure everything automatically allready. After adding the thing the binding automatically exchange keys with the device used for communication encryption/decryption.

Nevertheless there are thing parameters that can be set manually:

| Parameter         | Description                                                                                                           |
|-------------------|-----------------------------------------------------------------------------------------------------------------------|
| key               | Air Purifier device token for encrypted communication with the device. Optional, created automatically upon discovery |
| host              | IP or hostname of the thing. Optional, set automatically upon discovery                                               |
| deviceUUID        | Device ID number for communication (in UUID). Optional, detected automatically upon discovery                         |
| refreshInterval   | Refresh interval in minutes. Optional, the default value is 60 seconds.                                               |
| modelId           | Name of Air Purifier model. Optional, detected automatically upon discovery                                           |
| humidityOffset    | Humidity sensor readings offset used for correction. Optional, the default value is 0 degrees.                        |
| temperatureOffset | Temperature sensor readings offset used for correction. Optional, the default value is 0 Celsius degrees.             |

demo.things

```java
philipsair:ac2889_10:123 "Philips Air AC2889_10" @ "Wroclaw" [ key="XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", host="1.1.1.1", refreshInterval=15 ]
```

## Channels

| Channel ID | Item Type            | Description                                                                         |
|------------|----------------------|-------------------------------------------------------------------------------------|
| pwr        | Switch               | Device power on/off                                                                 |
| om         | String               | Fan speed (silent, 1, 2, 3, turbo)                                                  |
| mode       | String               | Filter mode P - Auto,  A - Allergen, S - Sleep, M - Manual, B - Bacteria, N - Night |
| uil        | Switch               | Buttons light on/off                                                                |
| ddp        | String               | Display PM2.5 index or Allergen Index                                               |
| aqil       | Number               | LED light level, (100, 75, 50, 25, 0)                                               |
| dtrs       | Number               | Timer minutes lefts                                                                 |
| dt         | Number               | Timer in hours (0-5)                                                                |
| cl         | Switch               | Child lock enabled/disabled                                                         |
| pm25       | Number:Density       | PM 2.5 particles pollution level                                                    |
| iaql       | Number               | Allergen index                                                                      |
| aqit       | Number               | Air quality notification threshold                                                  |
| err        | String               | Error message code                                                                  |
| fltsts0    | Number               | Pre-filter estimated clean up                                                       |
| fltsts1    | Number               | HEPA filter estimated lifetime                                                      |
| fltsts2    | Number               | Active carbon filter estimated lifetime                                             |
| wicksts    | Number               | Wick filter estimated lifetime                                                      |
| rh         | Number:Dimensionless | Current humidity                                                                    |
| rhset      | Number:Dimensionless | Humidity setpoint                                                                   |
| temp       | Number:Temperature   | Current temperature                                                                 |
| func       | String               | Device mode - P: Purification, PH: Purification and Humidification                  |
| wl         | Number               | Water tank level                                                                    |
| swversion  | String               | Firmware Version of the thing                                                       |

## Item Configuration

demo.items

```java
Switch                ac2889_10_pwr      "Power"              <switch>       { channel="philipsair:ac2889_10:livingroom:controls-basic#pwr" }
String                ac2889_10_om       "Fan Speed"          <fan>          { channel="philipsair:ac2889_10:livingroom:controls-basic#om" }
String                ac2889_10_mode     "Mode"               <text>         { channel="philipsair:ac2889_10:livingroom:controls-basic#mode" }
Switch                ac2889_10_uil      "Buttons light"      <lightbulb>    { channel="philipsair:ac2889_10:livingroom:controls-ui#uil" }
String                ac2889_10_ddp      "Displayed index"    <text>         { channel="philipsair:ac2889_10:livingroom:controls-ui#ddp" }
Number                ac2889_10_aqil     "LED light level"    <lightbulb>    { channel="philipsair:ac2889_10:livingroom:controls-ui#aqil" }
Number                ac2889_10_dtrs     "Timer left"         <time>         { channel="philipsair:ac2889_10:livingroom:controls-basic#dtrs" }
Number                ac2889_10_dt       "Timer"              <time>         { channel="philipsair:ac2889_10:livingroom:controls-basic#dt" }
Number:Density        ac2889_10_pm25     "PM2.5"              <smoke>        { channel="philipsair:ac2889_10:livingroom:sensors-basic#pm25" }
Number                ac2889_10_iaql     "Allergen index"     <text>         { channel="philipsair:ac2889_10:livingroom:sensors-basic#iaql" }
Number                ac2889_10_aqit     "AQIT"               <text>         { channel="philipsair:ac2889_10:livingroom:sensors-basic#aqit" }
String                ac2889_10_err      "Error message"      <error>        { channel="philipsair:ac2889_10:livingroom:sensors-basic#err" }
Number                ac2889_10_fltsts0  "Pre-filter"         <text>         { channel="philipsair:ac2889_10:livingroom:filters#fltsts0" }
Number                ac2889_10_fltsts2  "Carbon"             <text>         { channel="philipsair:ac2889_10:livingroom:filters#fltsts2" }
Number                ac2889_10_fltsts1  "HEPA filter"        <text>         { channel="philipsair:ac2889_10:livingroom:filters#fltsts1" }

Switch                ac3889_10_cl       "Child lock"         <lock>         { channel="philipsair:ac3889_10:livingroom:controls-adv#cl" }
Number                ac3889_10_wicksts  "Wick"               <text>         { channel="philipsair:ac3889_10:livingroom:filters-wicks#wicksts" }
Number:Dimensionless  ac3889_10_rh       "Humidity"           <humidity>     { channel="philipsair:ac3889_10:livingroom:sensors-adv#rh" }
Number:Dimensionless  ac3889_10_rhset    "Humidity setpoint"  <humidity>     { channel="philipsair:ac3889_10:livingroom:controls-adv#rhset" }
Number:Temperature    ac3889_10_temp     "Temperature"        <temperature>  { channel="philipsair:ac3889_10:livingroom:sensors-adv#temp" }
String                ac3889_10_func     "Function"           <text>         { channel="philipsair:ac3889_10:livingroom:controls-adv#func" }
Number                ac3889_10_wl       "Water level"        <cistern>      { channel="philipsair:ac3889_10:livingroom:sensors-adv#wl" }
```

## Sitemap Configuration

demo.sitemap

```java
sitemap philips_air_purifier_ac2889 label="Philips Air Purifier AC2889" {   
    Frame label="Control" {
        Switch  item=ac2889_10_pwr
        Text  item=ac2889_10_om
        Text  item=ac2889_10_mode
    }
    Frame label="Control-UI" {
        Switch  item=ac2889_10_uil
        Switch  item=ac2889_10_ddp
    }
    Frame label="Sensors" {
        Text  item=ac2889_10_pm25
        Text  item=ac3889_10_temp
        Text  item=ac3889_10_rh
        Text  item=ac3889_10_temp
    }    
}
```
