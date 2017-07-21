* There does not appear to be any rate limiting. It is possible to do back-to-back polling using LIST_PAIRED_DEVICES. OpenHAB was able to sustain this on my HW at about 60ms per query during development.
  * This may be true of the gateway but sending back-to-back messages to a single device can result in it becoming (temporarily?) "unreachable" and not providing status. After this occurs we have to stop sending messages to that device for a period of several seconds (this includes GET_DEVICE_INFO).
  * Is it only retrieving state with GET_DEVICE_INFO that fails? Can we still send commands? I have not seen observable issues other than with GET_DEVICE_INFO so maybe GET_DEVICE_INFO does an actual query of the device and can return "busy"?
  * Does it only  stall a single device? Can we still work with other devices?

* A DEVICE_COMMAND with an address of ff:ff:ff:ff:ff:ff:ff:ff is broadcast to all devices.

* Lightify does not use the B component of HSB colour. It uses a separate luminance channel. More strictly it uses RGB and scales whatever it is asked to show so that the brightest component is at 255. The luminance setting then determines the actual brightness.

* RGB is not 100% accurate. The Osram PAR16 RGBW bulbs seem to get within 2% on each channel.
  * It is worse. If we request one RGB triple we get another, slightly different, one set. It seems consistent and the value of one component seems to depend on what the other components values are. This implies a consistent mapping but whether this is a gateway thing or device thing is unclear (I only have PAR16 RGBWs and CLA 60 RGBWs). Since the protocol is completely undocumented we cannot assume the RGB mapping is going to be static however.
  * The ZLL spec specifically says that when asked to change colour the implementation (i.e. device) shall select a colour, within the limits of the hardware of the device, which is as close as possible to that requested.
  * PAR16 (300 lumen) lights have noticable variation and are "steppy" when transitioning. This may be related to internal design characteristics such as number of LEDs for each component. I have a feeling that the A60 (800 lumen) lights do better (but not checked yet).

* Zigbee specifies a maximum of 9 multicast messages broadcast over a 9 second period.
  * This may affect group dimming operations?

* Zigbee max data rate is 250kb/s.

* ZigBee spec says 1/10 of a second resolution for transition times.

* Zigbee indirect transmission timeout is 7.680 seconds. This suggests a commanded action may be delayed up to 7.680 seconds _within_ the Zigbee network. After that the message is discarded.

* Once a transition has been started a subsequent message may update the target but not the transition time. i.e. the new target will be reached when the original transition time ends. Changing a value which was not a target of the original transition (e.g. start a colour transition then send a luminance change) will cause the new value to be set immediately and the transition will continue uninterrupted. I.e. lights can only have one transition outstanding at once although the transition target value can be modified.
  * But... temperature and luminance can both transition at the same time?

* In ZLL the way to abort a transition (or "move") is with a "stop move step" command. This has no payload. If the gateway exposes the capability we do not (yet) know of it.

* Although we probe for the minimum and maximum colour temperatures for a device it is not clear whether what we see are device limits (i.e. the ZigBee ColorTempPhysical{Min,Max} attributes) or simply gateway capabilities.
