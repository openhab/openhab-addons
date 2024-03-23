# HueSync Binding

This binding integrates the [Play HDMI Sync Box](https://www.philips-hue.com/en-us/p/hue-play-hdmi-sync-box-/046677555221) into openHAB.
The integration happens directly through the Hue [HDMI Sync Box API](https://developers.meethue.com/develop/hue-entertainment/hue-hdmi-sync-box-api/) (not via a Hue Bridge).

![Play HDMI Sync Box](doc/bridge1.png) 
![Play HDMI Sync Box](doc/bridge2.png)

## Supported Things

_Please describe the different supported things / devices including their ThingTypeUID within this section._
_Which different types are supported, which models were tested etc.?_
_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

- `bridge`: Short description of the Bridge, if any
- `sample`: Short description of the Thing with the ThingTypeUID `sample`

## Discovery

The beinding is using [mDNS](https://en.wikipedia.org/wiki/Multicast_DNS) to discover HDMI Sync devies in the local network.
The LED on the Sync Box must be white or red.
This indicates that the device is connected to the Network.
If the LED is blinking blue, you need to setup the device using the official [Hue Sync App](https://www.philips-hue.com/en-in/explore-hue/propositions/entertainment/hue-sync).

If the device is not discovered you can check if it is properly configured and discoverable in the network:

<details>
  <summary>Linux (Ubuntu based distributions)</summary>

```bash
$ avahi-browse --resolve _huesync._tcp 
+ wlp0s20f3 IPv4 HueSyncBox-XXXXXXXXXXX                       _huesync._tcp        local
= wlp0s20f3 IPv4 HueSyncBox-XXXXXXXXXXX                       _huesync._tcp        local
   hostname = [XXXXXXXXXXX.local]
   address = [192.168.0.12]
   port = [443]
   txt = ["name=Sync Box" "devicetype=HSB1" "uniqueid=XXXXXXXXXXX" "path=/api"]
```

</details>


## Thing Configuration

To enable the binding to communicate with the device, a registration is required. 
Once the registration process is completed, the acquired token will authorize the binding to communicate with the device. 
After initial discovery and thing creation the device will stay offline.
To complete the authentication you need to pressed the registration button on the sync box for 3 seconds.


_Describe what is needed to manually configure a thing, either through the UI or via a thing-file._
_This should be mainly about its mandatory and optional configuration parameters._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

### `sample` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device  | N/A     | yes      | no       |
| password        | text    | Password to access the device         | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec. | 600     | no       | yes      |

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
