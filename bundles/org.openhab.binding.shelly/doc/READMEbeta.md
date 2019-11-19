# Shelly Binding (org.openhab.binding.shelly)

This openHAB 2 Binding implements control for the Shelly series of devices.
This includes sending commands to the devices as well as reading the device status and sensor data.

Author: Markus Michels (markus7017)
Check  https://community.openhab.org/t/shelly-binding/ for more information, questions and contributing ideas. Any comments are welcome!

Also check section **Additional Information** at the end of the document.
This includes some general comments, information how to debug and request new features.

---

### 2.5-SNAPSHOT

* fix: Sometimes API returns -1 or +101 for the roller position
* change: temperature unit channel removed, temperature is now always Celsius
* Updated to Californium 2.0.0-RC1
* Auto-initialized thing on Coap event
* Use Units of Measurement (channel knows the unit itself)
* Thing definition for Plug-S fixed (no extended meter values)
* "Enable channel cache" removed from thing config - now always active
* Re-factoring driven by PR review process (to become part of OH 2.5)
* README updated, parts moved to doc/READMEbeta.md

### 2.4.2 release notes (stable)

+ Support for Shelly Flood
+ Support for Shelly Dimmer
+ Support for Shelly EM
+ Sense: read IR code list for Sense from device rather than hard coded list
+ CoIoT/COAP support, enabled by default for battery devices
+ Create special device (shelly-protected) when device is password protected
+ new channel last_update for sensors = last time one of the state values have been updated
+ Add IP address to discovered Device Name

* Setting Event URLs reworked to support new Roller urls (roller_on/off/stop) and Dimmer URLs (btn1_on/off, btn2_on/off)
* channel name meter.totalWatts changed to meter.totalKWH (returns kw/h, not Watts)
* Roller: re-added OnOffType  (so you could send OPEN or ON / CLOSE or OFF / STOP)
* RGBW2: adjust numMeter (doesn't report this as part of the device property) -> work around for meter.Watts missing 
* Activation of Channel Cache is delayed for 60s to make sure that Persinstence restore is already done 
* dynamic thing updates removed (messes the log file), time and deviceUpTime removed from properties
* logging revised (include device name on most logs), more details about the bundle on startup
* various bug fixes and improvements
* refactoring started to get ready for 2.5 PR

Please delete and re-discover all things!

### Alpha/Beta versions

The binding is work in progress. You have to expect bugs etc. and each version might be incompatible to the existing thing definition, which means no backware compatibility.

Channel definitions are subject to change with any alpha or beta release. Please make sure to **delete all Shelly things before updating*** the binding and clean out the JSON DB:

- delete all Shelly things from PaperUI's Inbox and Thing list
- stop OH
- run openhab-cli clean-cache
- check the JSON db files for shelly references, remove all entries
- copy the jar to the addons/ folder
- start OH, wait until everything is initialized
- run the device discovery

If you hit a problem make sure to post a TRACE log (or send PM) so I could look into the details.

### Instalation

As described above the binding will be installed by copying the jar into the addons folder of your OH installation.
Once a stable state is reached the binding may become part of the openHAB 2.5 distribution, but this will take some time.
The binding was developed an tested on OH version 2.4 and 2.5. 
Please post an info if you also verified compatibility to version 2.3.
However, this release is not officially supported.

# Additional Notes

## General

* You should use firmware version 1.5.0 or never.
It might be that the binding is working with older versions, but this was never tested.
List of Firmware Versions for the different devices could be found here: https://api.shelly.cloud/files/firmware


* If you gave multiple network interfaces you should check openHAB's default setting.

Open PaperUI and go to Configuration:System-:Network Settings and verify the selected interface. 
If the Shelly devices are not on the same network you could try to add them manually.
However, devices in different networks have not been tested yet (please post a comment in the community thread if you are successful).

## Reporting a problem/bug

If you encounter a problem you could put the device into DEBUG or TRACE mode

- open OH console (execute "openhab-cli console")
- set the debug level ("log:set DEBUG org.openhab.binding.shelly")
- issue command or wait until problem occurs
- post an extract of openhab.log to the community thread (or send the author a PM - make sure the log extract has enough information, some more lines are fine)

## Feature Request

Any comment or feature request is welcome. Post the idea to the community thread, all of us will benefit.

## Other devices

The thing definition of the following devices is primarily.
If you have one of those devices send a PM to marks7017 and we could work on the implementation/testing.

- thing-type: shellysmoke
- thing-type: shellysense
- thing-type: shellyplug

## Supporting new devices

You could help to integrate and support new devices. In general the following information is a good start

- open a browser and issue the following urls
- http://&lt;device ip&gt;/settings
- http://&lt;device ip&gt;/status

once basic discovery is implemented the Coap Discription could be discovered

- enable CoIoT events within the thing configuration
- open the thing properties ([Show Properties])
- and copy&amp;paste the coapDescr property

post this information in the community thread or send a PM to the author.
Depending on the device type and complexity of the integration you should be prepared to run test cycles with snapshort binds of the binding incl. back and forth communication with the author. 


