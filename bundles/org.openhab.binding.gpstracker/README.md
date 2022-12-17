# GPSTracker Binding

This binding allows you to connect mobile GPS tracker applications to openHAB and process GPS location reports.

Currently two applications are supported:

- [OwnTracks](https://owntracks.org/booklet/) - iOS, Android
- [GPSLogger](https://gpslogger.app/) - Android

GPS location reports are sent to openHAB using HTTP.
Please be aware that this communication uses the public network so make sure your openHAB installation is [secured](https://www.openhab.org/docs/installation/security.html#encrypted-communication) (but accessible from public internet through myopenhab.org or using a reverse proxy) and you configured HTTP**S** access in tracking applications.
The easiest way to achieve this is to use the [openHAB Cloud Connector](https://www.openhab.org/addons/integrations/openhabcloud/) in conjunction with [myopenHAB.org](https://www.myopenhab.org/).

The binding can process two message types received from trackers:

- **Location** - This is a simple location report with coordinates extended with some extra information about the tracker (e.g. tracker device battery level). [OwnTracks, GPSLogger]
- **Transition** - This report is based on regions defined in tracker application only. A message is sent every time the tracker enters or leaves a region. [OwnTracks only] See "Distance Channel and Presence Switch" how this behavior can be achieved with openHAB's on-board tools.

## Configuration

### OwnTracks

Install [OwnTracks for Android](https://play.google.com/store/apps/details?id=org.owntracks.android) or [OwnTracks for iOS](https://itunes.apple.com/us/app/owntracks/id692424691) on your device.

Go to Preferences/Connection and set:

- **Mode** - select Private HTTP
- **Host**
  - `https://<your.ip.address>/gpstracker/owntracks` or
  - `https://home.myopenhab.org/gpstracker/owntracks`
- **Identification**
  - Turn Authentication ON
  - Set username and password to be able to reach your openHAB server (myopenhab.org credential, if choosen as host)
  - Device ID is not important. Set it to e.g. phone
  - Tracker ID - This id identifies the tracker as a thing. This must be unique for each tracker connected to the same openHAB instance (e.g. family members).

### GPSLogger

Install [GPSLogger for Android](https://github.com/mendhak/gpslogger/releases) on your device.
After the launch, go to General Options.
Enable **Start on boot-up** and **Start on app launch**.

Go to _Logging details_ and enable **Log to custom URL**.
If you only want to use GPSLogger for this binding, you can disable all other "_Log to_" entries.
Right after enabling, the app takes you to the _Log to custom URL_ settings:

- **URL**
  - `https://<your.ip.address>/gpstracker/gpslogger` or
  - `https://home.myopenhab.org/gpstracker/gpslogger`
- **HTTP Body** - type in: { "_type":"location", "lat":%LAT, "lon":%LON, "tid":"XY", "acc":%ACC, "batt":%BATT, "tst":%TIMESTAMP }
  - Note: "_tid_" is the tracker id that identifies the tracker as a thing. This must be unique for each tracker connected to the same openHAB instance (e.g. family members).
- **HTTP Headers** - type in: _Content-Type: application/json_
- **HTTP Method** - type in: _POST_
- **Basic Authentication** - Set username and password to be able to reach your openHAB server (myopenhab.org credential, if choosen as URL)
- Check if everything is ok by clicking on **Validate SSL Certificate**.

### Things

It is possible to define things manually or to use the discovery feature of the openHAB.
An important detail for both methods is that a tracker is identified by a **tracker id** configured on mobile devices.
Make sure these tracker ids are unique in group of trackers connected to a single openHAB instance.

#### Discovery

If the things are not defined in **.things** files the first time the tracker sends a GPS log record the binding recognizes it as new tracker and inserts an entry into the Inbox as new tracker with name **GPS Tracker ??**.

### Channels

Basic channels provided by the tracker things:

- **Location** - Current location of the tracker
- **Accuracy** - GPS accuracy
- **Last Report** - Timestamp of the last location report
- **Battery Level** - Battery level of the device running the tracker application
- **Region trigger channel** - Used by regions defined in tracker application. Event is fired with payload of the region name when the binding receives a **transition** log record or a distance calculation for a **location** record indicates that the tracker is outside of the region circle. Payload is suffixed with `/enter` for entering and with `/leave` for leaving events.

#### Distance Calculation

Tracker thing can be extended with **Distance** channels (channel type is `regionDistance`) if a distance calculation is needed for a region.
These dynamic channels require the following parameters:

| Parameter     | Type     | Description                                                                                                                                                                       |
|---------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Region Name   | String   | Region name. If the region is configured in the tracker app as well use the same name. Distance channels can also be defined as binding only regions (not configured in trackers) |
| Region center | Location | Region center location                                                                                                                                                            |
| Region Radius | Integer  | Geofence radius                                                                                                                                                                 |
| Accuracy Threshold | Integer  | Location accuracy threshold (0 to disable)                                                                                                                                                                 |

Distance values will be updated each time a GPS location log record is received from the tracker if the accuracy is below the threshold or if the threshold is disabled.

When this calculated distance is less than the defined geofence radius the binding also fires event on Region Trigger channel.

- When the tracker is approaching (the new calculated distance is less then the previous one) the payload is <<region_name>>/enter.
- If the tracker is distancing (the new calculated distance is greater then the previous one) payload is <<region_name>>/leave.

If the tracker is moving inside/outside the region (both the previous and the current calculated distance value is less/greater than the radius) no events are fired.
This means that the region events are triggered **ONLY IN CASE THE REGION BORDER IS CROSSED**.

**Note**: In case the location is set for openHAB installation (Configuration/System/Regional Settings) the binding automatically creates the System Distance channel (gpstracker:tracker:??:distanceSystem) with region name set to **System**.

#### Tracker Location Status

In case external regions are defined in mobile application the binding fires entering/leaving events on region trigger channel.
These events are fired in case of distance channels as well when the tracker crosses the geofence (the distance from the region center becomes less/greater than the radius).
In order to have this state available in stateful switch items (e.g. for rule logic) switch type items can be linked to **regionTrigger** channel.
There is a special profile (gpstracker:trigger-geofence) that transforms trigger enter/leave events to switch states:

- **<<region_name>>/enter** will update the switch state to **ON**
- **<<region_name>>/leave** will update the switch state to **OFF**

To link a switch item to regionTrigger channel the following parameters are required by the item link:

| Parameter    | Type      | Description                                                                                              |
|--------------|-----------|----------------------------------------------------------------------------------------------------------|
| Profile Name | Selection | Select the Geofence(gpstracker:trigger-geofence) from dropdown                                           |
| Region Name  | String    | Region name which should be the same as used in the tracker application or defined for distance channels |

## Manual Configuration

### Things

```java
//tracker definition
Thing gpstracker:tracker:1   "XY tracker" [trackerId="XY"]

//tracker definition with extra distance channel
Thing gpstracker:tracker:EX   "EX tracker" [trackerId="EX"] {
    Channels:
            Type regionDistance : homeDistance "Distance from Home" [
                regionName="Home",
                regionCenterLocation="11.1111,22.2222",
                regionRadius=100,
                accuracyThreshold=30
            ]
}
```

### Items

```java
//items for basic channels
Location locationEX "Location"  {channel="gpstracker:tracker:1:lastLocation"}
DateTime lastSeenEX "Last seen"  {channel="gpstracker:tracker:1:lastReport"}
Number  batteryEX "Battery level"  {channel="gpstracker:tracker:1:batteryLevel"}
Number:Length  accuracyEX "GPS Accuracy [%d m]"  {channel="gpstracker:tracker:1:gpsAccuracy"}

//linking switch item to regionTrigger channel. assuming the Home distance channel is defined in the binding config (see above)
Switch atHomeEX "Home presence" {channel="gpstracker:tracker:EX:regionTrigger" [profile="gpstracker:trigger-geofence", regionName="Home"]}

//another switch for work region. assuming the OTWork is defined in OwnTracks application (no distance channel is needed like for Home)
Switch atWorkEX "Work presence" {channel="gpstracker:tracker:EX:regionTrigger" [profile="gpstracker:trigger-geofence", regionName="OTWork"]}
```

### Sitemaps

```perl
sitemap gpstracker label="GPSTracker Binding" {
    Text item=distanceEX
    Text item=atWorkEX
    Text item=atHomeEX
    Text item=lastSeenEX
    Text item=batteryEX
    Text item=accuracyEX
    Mapview item=locationEX height=4
}
```

## Debug

As the setup is not that simple here are some hints for debugging.
In order to see detailed debug information [set TRACE debug level](https://www.openhab.org/docs/administration/logging.html) for `org.openhab.binding.gpstracker` package.

### Binding Start

```text
2018-10-03 18:12:38.950 [DEBUG] [org.openhab.binding.gpstracker      ] - ServiceEvent REGISTERED - {org.openhab.core.config.discovery.DiscoveryService, org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService}={service.id=425, service.bundleid=183, service.scope=bundle, component.name=org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService, component.id=268} - org.openhab.binding.gpstracker
2018-10-03 18:12:38.965 [DEBUG] [org.openhab.binding.gpstracker      ] - ServiceEvent REGISTERED - {org.openhab.core.thing.binding.ThingHandlerFactory, org.openhab.core.config.core.ConfigOptionProvider}={location=47.536178,19.169812, service.id=426, service.bundleid=183, service.scope=bundle, radius=100, name=Home, component.name=org.openhab.binding.gpstracker.internal.GPSTrackerHandlerFactory, component.id=267, additionalRegionsJSON=[
], triggerEvent=false, service.pid=binding.gpstracker} - org.openhab.binding.gpstracker
2018-10-03 18:12:38.994 [DEBUG] [er.internal.GPSTrackerHandlerFactory] - Initializing callback servlets
2018-10-03 18:12:39.013 [DEBUG] [org.openhab.binding.gpstracker      ] - ServiceEvent REGISTERED - {javax.servlet.ServletContext}={osgi.web.version=2.4.0.201809241418, osgi.web.contextpath=/, service.id=427, osgi.web.symbolicname=org.openhab.binding.gpstracker, service.bundleid=183, service.scope=singleton, osgi.web.contextname=default} - org.openhab.binding.gpstracker
2018-10-03 18:12:39.047 [DEBUG] [er.internal.GPSTrackerHandlerFactory] - Started GPSTracker Callback servlet on /gpstracker/owntracks
2018-10-03 18:12:39.058 [DEBUG] [er.internal.GPSTrackerHandlerFactory] - Started GPSTracker Callback servlet on /gpstracker/gpslogger
2018-10-03 18:12:39.072 [DEBUG] [org.openhab.binding.gpstracker      ] - ServiceEvent REGISTERED - {org.openhab.core.thing.profiles.ProfileFactory, org.openhab.core.thing.profiles.ProfileAdvisor, org.openhab.core.thing.profiles.ProfileTypeProvider}={service.id=428, service.bundleid=183, service.scope=bundle, component.name=org.openhab.binding.gpstracker.internal.profile.GPSTrackerProfileFactory, component.id=269} - org.openhab.binding.gpstracker
2018-10-03 18:12:39.092 [DEBUG] [org.openhab.binding.gpstracker      ] - BundleEvent STARTING - org.openhab.binding.gpstracker
2018-10-03 18:12:39.098 [DEBUG] [org.openhab.binding.gpstracker      ] - BundleEvent STARTED - org.openhab.binding.gpstracker
```

Please note the lines about started servlets:

```text
Started GPSTracker Callback servlet on /gpstracker/owntracks
Started GPSTracker Callback servlet on /gpstracker/gpslogger
```

### Registration

In case the discovery is used the first message from a tracker registers it in the inbox:

```text
2018-10-05 08:36:14.283 [DEBUG] [nal.provider.AbstractCallbackServlet] - Post message received from OwnTracks tracker: {"_type":"location","tid":"XX","acc":10.0,"lat":41.53,"lon":16.16,"tst":1527966973,"wtst":1524244195,"batt":96}
2018-10-05 08:36:14.286 [DEBUG] [nal.provider.AbstractCallbackServlet] - There is no handler for tracker XX. Check the inbox for the new tracker.
```

### Location Update

The next location message already calculates the distance for System location:

```text
2018-10-05 08:38:33.916 [DEBUG] [nal.provider.AbstractCallbackServlet] - Post message received from OwnTracks tracker: {"_type":"location","tid":"XX","acc":10.0,"lat":41.53,"lon":16.16,"tst":1527966973,"wtst":1524244195,"batt":96}
2018-10-05 08:38:33.917 [DEBUG] [cker.internal.handler.TrackerHandler] - Update base channels for tracker XX from message: org.openhab.binding.gpstracker.internal.message.LocationMessage@31dfed63
2018-10-05 08:38:33.941 [TRACE] [cker.internal.handler.TrackerHandler] - batteryLevel -> 96
2018-10-05 08:38:33.942 [TRACE] [cker.internal.handler.TrackerHandler] - lastLocation -> 41.53,16.16
2018-10-05 08:38:33.943 [TRACE] [cker.internal.handler.TrackerHandler] - lastReport -> 2018-06-02T19:16:13.000+0000
2018-10-05 08:38:33.943 [DEBUG] [cker.internal.handler.TrackerHandler] - Updating distance channels tracker XX
2018-10-05 08:38:33.944 [TRACE] [cker.internal.handler.TrackerHandler] - Region center distance from tracker location 41.53,16.16 is 709835.1673811453m
2018-10-05 08:38:33.944 [TRACE] [cker.internal.handler.TrackerHandler] - System uses SI measurement units. No conversion is needed.
```

### Distance Channel and Presence Switch

Assumptions:

- A Home distance channel is added to the tracker with parameters:
  - Channel ID: distanceHome
  - Region Name: Home
  - Region Radius: 100
  - Region Center: 42.53,16.16
- Presence switch is linked to the regionTrigger channel with parameters:
  - Profile: Geofence(gpstracker:trigger-geofence)
  - Region Name: Home

![Image](doc/example.png)

After a location message received from the tracker the log should contain these lines:

```text
2018-10-05 09:27:58.768 [DEBUG] [nal.provider.AbstractCallbackServlet] - Post message received from OwnTracks tracker: {"_type":"location","tid":"XX","acc":10.0,"lat":42.53,"lon":17.16,"tst":1527966973,"wtst":1524244195,"batt":96}
2018-10-05 09:27:58.769 [DEBUG] [cker.internal.handler.TrackerHandler] - Update base channels for tracker XX from message: org.openhab.binding.gpstracker.internal.message.LocationMessage@67e5d438
2018-10-05 09:27:58.770 [TRACE] [cker.internal.handler.TrackerHandler] - batteryLevel -> 96
2018-10-05 09:27:58.771 [TRACE] [cker.internal.handler.TrackerHandler] - lastLocation -> 42.53,17.16
2018-10-05 09:27:58.772 [TRACE] [cker.internal.handler.TrackerHandler] - lastReport -> 2018-06-02T19:16:13.000+0000
2018-10-05 09:27:58.773 [DEBUG] [cker.internal.handler.TrackerHandler] - Updating distance channels tracker XX
2018-10-05 09:27:58.774 [TRACE] [cker.internal.handler.TrackerHandler] - Region Home center distance from tracker location 42.53,17.16 is 82033.47272145993m
2018-10-05 09:27:58.775 [TRACE] [cker.internal.handler.TrackerHandler] - System uses SI measurement units. No conversion is needed.
2018-10-05 09:27:58.779 [DEBUG] [ofile.GPSTrackerTriggerSwitchProfile] - Trigger switch profile created for region Home
2018-10-05 09:27:58.779 [DEBUG] [ofile.GPSTrackerTriggerSwitchProfile] - Transition trigger Home/leave handled for region Home by profile: OFF
2018-10-05 09:27:58.792 [TRACE] [cker.internal.handler.TrackerHandler] - Triggering Home for XX/Home/leave
2018-10-05 09:27:58.793 [TRACE] [cker.internal.handler.TrackerHandler] - Region System center distance from tracker location 42.53,17.16 is 579224.192171576m
2018-10-05 09:27:58.794 [TRACE] [cker.internal.handler.TrackerHandler] - System uses SI measurement units. No conversion is needed.
```

### External Region and Presence Switch

Assumptions:

- A **shared** region is defined in OwnTracks application. Lets call it Work.
- Presence switch is linked to the regionTrigger channel with parameters:
  - Profile: Geofence(gpstracker:trigger-geofence)
  - Region Name: Work

```text
2018-10-05 09:35:27.203 [DEBUG] [nal.provider.AbstractCallbackServlet] - Post message received from OwnTracks tracker: {"_type":"transition","tid":"XX","acc":10.0,"desc":"Work","event":"enter","lat":42.53,"lon":18.22,"tst":1527966973,"wtst":1524244195,"t":"c"}
2018-10-05 09:35:27.204 [DEBUG] [cker.internal.handler.TrackerHandler] - ConfigHelper transition event received: Work
2018-10-05 09:35:27.204 [DEBUG] [cker.internal.handler.TrackerHandler] - Update base channels for tracker XX from message: org.openhab.binding.gpstracker.internal.message.TransitionMessage@5e5b0d59
2018-10-05 09:35:27.204 [TRACE] [cker.internal.handler.TrackerHandler] - lastLocation -> 42.53,18.22
2018-10-05 09:35:27.205 [TRACE] [cker.internal.handler.TrackerHandler] - lastReport -> 2018-06-02T19:16:13.000+0000
2018-10-05 09:35:27.205 [DEBUG] [cker.internal.handler.TrackerHandler] - Updating distance channels tracker XX
2018-10-05 09:35:27.206 [TRACE] [cker.internal.handler.TrackerHandler] - Region Home center distance from tracker location 42.53,18.22 is 168985.77453131412m
2018-10-05 09:35:27.207 [TRACE] [cker.internal.handler.TrackerHandler] - Region System center distance from tracker location 42.53,18.22 is 562259.467093007m
2018-10-05 09:35:27.208 [TRACE] [cker.internal.handler.TrackerHandler] - Triggering Work for XX/Work/enter
2018-10-05 09:35:27.208 [DEBUG] [ofile.GPSTrackerTriggerSwitchProfile] - Trigger switch profile created for region Home
2018-10-05 09:35:27.210 [DEBUG] [ofile.GPSTrackerTriggerSwitchProfile] - Trigger switch profile created for region Work
2018-10-05 09:35:27.210 [DEBUG] [ofile.GPSTrackerTriggerSwitchProfile] - Transition trigger Work/enter handled for region Work by profile: ON
```

**Note**:

- If the binding was restarted only the second transition update will trigger event as the binding has to know the previous state.
- The distance is not calculated for Work as the binding doesn't know the Work region center.
