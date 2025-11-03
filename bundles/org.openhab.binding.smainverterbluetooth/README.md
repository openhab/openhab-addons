# SMA Inverter Bluetooth Binding

This binding fetches data from a SMA inverter over Bluetooth. If your inverter works with [Sunny Explorer](https://www.sma.de/en/products/energy-management/sunny-explorer) then it is likely to work with this binding.
SMA's Bluetooth protocol is proprietary and has been partly reverse engineered by the GitHub community.
As the Java BlueCove library is not maintained and likely not compatible with Java 21 or later, the development of a Bluetooth interface is better achieved using the Python [pybluez](https://github.com/pybluez/pybluez) module for low-level Bluetooth Classic (BR/EDR) socket communication.


The project calls extensively on work done by others:

* [SBFspot](https://github.com/SBFspot/SBFspot/tree/master/SBFspot)
* [understanding-sma-bluetooth-protocol](http://blog.jamesball.co.uk/2013/02/understanding-sma-bluetooth-protocol-in.html?q=SMA)
* [python-smadata2](https://github.com/dgibson/python-smadata2)

There has been previous discussion in the openHAB Community Forum on this subject and this may be of interest to some. [Example on how to access data of a Sunny Boy SMA solar inverter](https://community.openhab.org/t/example-on-how-to-access-data-of-a-sunny-boy-sma-solar-inverter/50963)

This binding requires the installation of a Command Line Interface (CLI) programme specifically written for this binding.The legacy code used for the core of the CLI has been shown to work on inverters SMA3000HF, 3000TL, 4000TL - but in theory should work with all Bluetooth enabled SMA inverters.

## Supported Things

_Please describe the different supported things / devices including their ThingTypeUID within this section._
_Which different types are supported, which models were tested etc.?_
_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

- `bridge`: Short description of the Bridge, if any
- `sample`: Short description of the Thing with the ThingTypeUID `sample`

## Discovery

Discovery is manual, the inverter Thing needs to know the Bluetooth address of the SMA inverter. To find this you can use the linux command line.

```shell
>> hcitool Scan
Scanning ...
        00:00:00:00:00:00       SomethingElse
         ...
        00:80:25:00:00:00       SMA001d SN: XXXXXXXXXX SNXXXXXXXXXX
```
The SMA Inverter is identified with SMA001d SN: XXXXXXXXXX where XXXXXXXXXX is the same serial number you see in Sunny Explorer and the Bluetooth address is to the left.
If you don't have Linux available there are various Apps for Android or Windows that have a similar function


## Binding Configuration

_If your binding requires or supports general configuration settings, please create a folder ```cfg``` and place the configuration file ```<bindingId>.cfg``` inside it._
_In this section, you should link to this file and provide some information about the options._
_The file could e.g. look like:_

```
# Configuration for the smainverterbluetooth Binding
#
# Default secret key for the pairing of the smainverterbluetooth Thing.
# It has to be between 10-40 (alphanumeric) characters.
# This may be changed by the user for security reasons.
secret=openHABSecret
```

_Note that it is planned to generate some part of this based on the information that is available within ```src/main/resources/OH-INF/binding``` of your binding._

_If your binding does not offer any generic configurations, you can remove this section completely._

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the UI or via a thing-file._
_This should be mainly about its mandatory and optional configuration parameters._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

### `sample` Thing Configuration

| Name              | Type    | Description                           | Default | Required | Advanced |
|-------------------|---------|---------------------------------------|---------|----------|----------|
| bluetooth-address | text    | Bluetooth address of the device       | N/A     | yes      | no       |
| password          | text    | Password to access the device         | N/A     | yes      | no       |
| refreshInterval   | integer | Interval the device is polled in sec. | 600     | no       | yes      |

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

| Channel | Type   | Read/Write | Description                 |
|---------|--------|------------|-----------------------------|
| control | Switch | RW         | This is the control channel |

## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

### Thing Configuration

```java
Example thing configuration goes here.
```

### Item Configuration

```java
Example item configuration goes here.
```

### Sitemap Configuration

```perl
Optional Sitemap configuration goes here.
Remove this section, if not needed.
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_

## Trouble Shooting

If the bluetooth from your inverter is disabled you will need to set  the NetID to 1 (default). The Bluetooth NetID is a physical setting that must be changed using a rotary switch inside the inverter's enclosure. See the [Installation Manual](https://www.inbalance-energy.co.uk/datasheets_downloads/SunnyBoy/sb3600tl_installation_manual.pdf)


