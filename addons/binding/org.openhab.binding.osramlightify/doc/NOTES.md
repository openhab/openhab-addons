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

* Transitions:
  * Gateway firmware >= 1.1.3.53 and light firmware >= 01020510 are both required. Otherwise overlapping transitions finish at the time specified by the _first_ transition command. i.e. the subsequent command(s) change the target state but not the transition time.
  * Colour/temperature and luminance can both transition independently and in parallel.

* White/colour mode switching
  * If we are in colour mode and send a temperature change causing a switch to white mode the temperature jumps to maximum and then is brought down to the requested value.
  * If we are in white mode and send a colour change causing a switch to colour mode all of RGBA jump to 255 and then are brought down to the requested colour.
  * These jumps cause unpleasant flashes and unexpected transition direction which is especially noticable with larger transition intervals. Since they happen on the change we can avoid them by preceeding a transition that will cause a change by a an immediate switch to the current state values. However since it takes 100-200ms for a command this is only worth doing for longer transitions. Some experimentation will be necessary to tune the threshold. It may be necessary to have it in config.
  * This may be fixed with 1.1.3.53 and 01020510?
    * The change from white to colour appears to be - colour transitions from where it is.
    * The change from colour to white is not - temperature jumps to max and then comes down to the requested value.
      * We could fix this by doing an immediate change to the current temperature and then transitioning but this would add some latency before the transition. Although the latency might not be so obvious since it would be taken up with a change from colour to white?

* It occurs to me that there is an alternative design in which a commanded change or state change noted in a LIST_PAIRED could trigger spamming with GET_DEVICE_INFO until no more changed were observed. This would enable us to track transitions, whether initiated by us or externally, very nicely. However it would be likely to put severe constraints on the maximum number of devices that could be changing at any one time. We would certainly need a priority queue.

* Although we probe for the minimum and maximum colour temperatures for a device it is not clear whether what we see are device limits (i.e. the ZigBee ColorTempPhysical{Min,Max} attributes) or simply gateway capabilities.
  * PAR16 RGBW firmware 01020510 reports white range to be 1501K to 8000K, firmware 01020412 reported 2702K to 6622K. The only difference is light firmware, there is no bridge change. That suggests we are probing device capabilities as per ZLL but why has it changed? The quoted range for PAR16 RGBW is 2000K to 6500K and surely that is dependent on hardware? Perhaps the firmware is generic and Osram have lights with a wider range in the pipeline? Or some of the existing devices were firmware-crippled?
  * This appears to be an enhancement. From observation it appears that 1501K is definitely different from 2000K. It looks as though the new extended temperatures may be using the RGB (mainly R and B) LEDs to extend the range?
  * Gateway firmware 1.2.2.0 no longer lets us probe for the range. We just get 0-65535.


* In LIST_PAIRED (unknown1 and unknown2 are the last two ints in each device state block - they have since been renamed):
  * unknown1 seems to be time since last seen with units of 5mins.
  * unknown2 seems to be a booting or joining flag

  * when a device is powered on reachable changes from 0 to 1 to 2 (the intermediate 1 is not always seen).
    * 15:50:43.777 ...:C6:C2:  reachable=1
    * 15:50:47.399 ...:C6:C2:  reachable=2 ...:CE:59:  reachable=1
    * 15:50:52.524 ...:CE:59:  reachable=2 ...:CC:DA:  reachable=1
    * 15:50:57.644 ...:CC:DA:  reachable=2 (...:C7:45 does NOT go to reachable=1)
    * 15:51:02.771 ...:C7:45:  reachable=0
    * 15:51:07.892 ...:C7:45:  reachable=2
  * ...:C7:45 is known to be faulty (the red LEDs have failed).

  * when a device is powered off unknown1 starts increasing by 1 every 5mins (first increment after 5mins).
    * 15:51:07.892 unknown1=0 unknown2=0 reachable=2
    * 15:56:13.838 unknown1=1 unknown2=0 reachable=2
    * 16:01:12.739 unknown1=2 unknown2=0 reachable=2
    * 16:06:15.249 unknown1=3 unknown2=0 reachable=2
  * after 15-16mins unknown2 changes to 1.
    * 16:07:11.628 unknown1=3 unknown2=1 reachable=2
  * unknown1 continues counting 5min intervals.
    * 16:11:12.583 unknown1=4 unknown2=1 reachable=2
  * 5-6mins later unknown2 goes back to 0 and reachable changes to 0.
    * 16:12:55.092 unknown1=4 unknown2=0 reachable=0

  * a gateway power cycle does not lose unknown1 above some threshold but below that it zeros them and sets reachable to 2.
    * this threshold is not currently known.

* On firmware upgrade of a device the final step is for the device to reboot. When the reboot is initiated we receive a device state with reachable=0 and power=true (all else being as expected) - i.e. the device has powered up and become unreachable. Once the reboot is complete we receive reachable=2 with the rest of the state being the power on default.

* PAR16 RGBW firmware 01020510 causes lights to go offline and back online every 80-100 minutes or so due to the time since last seen becoming non-zero.
  * This update may have been pulled by OSRAM. I have not seen it offered for my second set of lights which were not online at the time the first set received the update.
  * This behaviour appears to be fixed when paired with gateway firmware 1.1.3.53. Lights already on 01020510 work correctly and the 01020510 firmware is being offered for other lights again.

* Changing colour or temperature while a light is off (but powered) has no effect.
