* Check possible problem with gateway becoming unresponsive when polling interval becomes >3s
  * Connection tracking timeout dropping traffic at the AP maybe? Need to keep connection alive or reconnect on timeout?

* Remove PROPERTY_FIRMWARE_VERSION in a few months when people are likely to have upgraded.

* Transition time: could we make it a rate? i.e. time given is for 0-100% so to go 0-50% we scale the transition time so the _rate_ is constant rather than the _time_.
  * what about ops on groups? If we perform an op on multiple things we may want them to complete at the same time?
  * so we can have absolute transition time or scale rates
    * we need toggles in the thing config to say which we want there
    * this will need to be handled in PolicyCommand framework probably. Policies attach to messages and are applied in the transmit queue (which becomes a priority queue) and/or encodedMessage (which sets transition time/rate)

* Could we / should we attempt to maintain state for groups? We would have to merge each member item change into the group state.

* When we remove a device should we remove any outstanding messages in the TX queue?

* I18n.

* Add a switch channel that indicates whether we are in white mode or colour mode.

* Map on/off to soft on/off (i.e. brightness changes) if transition time is non-zero as normal on/off commands do not allow a transition time.
  * For off, set device state power to off then do a luminance transition to 0. The state should ignore the luminance change.
  * For on, do a luminance transition to whatever the state says the current luminance is.
  * Current firmware allows the Lightify app to set the fade on/off times for switch commands.
    * Should we have a means to set them via openHAB? We know the messages.

* Is it possible to have multiple outstanding requests if they are for different devices? If we have multiple connections?

* Address the conflict issue noted in LightifyDeviceState by adding a config option to disable doing state updates for colour and temperature (and possibly luminance since it is tied to colour?). This means the item state is definitive and devices are assumed to just go along with it as best they can. It does, however, prevent tracking of changes made by external agents such as the Lightify app.

* Need a device option to say whether white mode is initially on or off?

* Need a device option to say whether to restore last known state on device online or stay with whatever the power-on default is set to.

* State handling
  * Device is added as a thing
  * OpenHAB starts with device already on
    * Uses current state, items refreshed
  * Device power off, then on. Interval < 10mins (happens on firmware update too)
    * Device comes up with reachable 0, existing state is saved, power up state is ignored, device is offline
    * Reachable changes to 2, device is online, saved state is restored
  * Device power off, then on. Interval > 10mins
    * Offlined in openHAB after 10mins
    * Powered on
    * 20-30s later reachable seen as 2, power true, white mode is false but temperature and colour are unchanged
    * Device is online, items are refreshed
    * ~35s later a colour change to 255,255,255 is seen
    * Uses device default state, items refreshed
    * (Non-)issue: advertised state may not be correct for 20-30s, no way to tell mode. This seems to be just the way the gateway works. We cannot tell when the gateway has actual status other than power :-(

* Currently only GetDeviceInfoMessages return false from handleResponse. Only GET_DEVICE_INFO can fail in a way that implies we should try again but if it does do we really want to loop doing the same GET_DEVICE_INFO over and over again until it works at the exclusion of all else?
  * Rather than return false and let the connector do resends BaseGetDeviceInfoMessage should requeue itself and limit its own maximum retries. But that implies we need a priority queue because queries for probe results MUST come before any LIST_PAIRED query.

* The receive method of DeviceState called via ListPairedDevices should take responsibility for telling the connector when to poll again so that we can get accurate transition timing.

* Add state reset on mode switch if we are to transition slowly (see NOTES.md)

* Changing colour or temperature of a light while it is off has no effect. This is a side effect of the gateway using separate RGB and luminance. We should check how other things such as Hue and LIFX behave and consider having the binding send extra colour or temperature updates when changing the power status.

* Need to always do a general LIST_PAIRED poll after a command completes. If the command was for a group thing (gateway-defined group) then we do not know what devices may have been affected until we see their state change.
  * This is a problem for transitions sent to groups since historically we have needed to do a GET_DEVICE_INFO on the specific device to force the gateway to update after the transition is complete.
  * Do we still need to do this GET_DEVICE_INFO? Maybe the gateway has been fixed to poll after a transition?
    * Yes, we do as of gateway firmware 1.1.3.53 and light firmware 01020510 at least.
  * If we send some command after a transition has completed but before the GET_DEVICE_INFO is that sufficient to get the gateway to reread the device state? i.e. can we replace the device info request and simply set the device to the state it should be in?
    * But what about groups again? We might start a transition on a group but transitions on some of the devices in the set may have been interrupted.
  * Is a multicast GET_DEVICE_INFO possible? Do transitions on groups just work?

* Add a config switch to allow items to be updated during transitions. Sometimes, especially with long-preiod transitions you may want to have the current state reflected by items.
  * Might there be any way to make it a per-command option? Maybe a time threshold above which updates are done? Or maybe it is something that could be part of the (TBD) priority queuing framework?

* Groups should ignore reachable in state - they always show as 0

* Changing the luminance via the color channel should update the dimmer channel and vice versa - groups have both. Similar goes for the switch channel.
  * Would it be too confusing if groups only had the color channel bearing in mind they may not have _any_ RGBW members?
  * Yes. It is not logical to require switches to be linked to a colour channel when you have a group of power sockets.

* Currently groups never update state themselves. This means that a transition on a group will transition each individual member but the members do not know about the transition and thus do not suppress updates during it and do not force an update after it.
  * We could record group membership and use that when starting transitions to start the transition on each member. Then they would suppress updates and force a GET_DEVICE_INFO on completion. Of course, that means there are potentially lots of GET_DEVICE_INFOs all at the same time...
  * Can we groupcast a GET_DEVICE_INFO? What would the response look like?
  * We should have a per-device option to switch update suppression on and off and should be able to override it from the group level.

* Only set bridge status once it is online and we have received the first device states. Otherwise we will trigger item refreshes before we actually have anything to tell them.
  * We do.
  * The item refreshes seem to happen before then. Is it when they are linked? It seems the Things go UNKNOWN before the bridge is out of INTIALIZING. UNKNOWN is considered ok - does it trigger a refresh?

* When an effect is applied to a group the effect is only cancelled by a group change.
  * If the effect schedules updates on the group it is impossible to drop a member out of the effect - the group has to be updated to cancel the effect for all.
  * If the effect schedules updates on the members individually then a group update must cancel effects on each individual member.

* seqno in messages to the gateway is really just an id. It does not have to be sequential, just unique in any group of outstanding messages. Since we do not have multiple outstanding messages we can just make it a random number and it will be a little bit harder for anyone to inject fake replies in the wifi path between us and the gateway. If we ever do support multiple outstanding messages we can just set the seqno to a random number every time we TX because we just added a message to an empty queue.

* Add a dummy "examples" effect that dumps out some examples into the lib directory (userdata/org.openhab.binding.osramlightify.effects/)

* In effects: where a median exists make it default to the current device state. min and max should be defined by offsets from median. Offer commands received by the device to any current effect and allow it the option of consuming them or allowing itself to be stopped and the command applied to the device as normal. It then becomes, for instance, possible to control the brightness of, say,  a flame/fire effect using the normal brightness controls.
  * Does this mean a luminance change accepted by an effect is not applied to the device? Should it be? i.e. should we make a note to apply it when we stop the effect? What if we are stopping the effect because we have detected an external change?

* Add a parameter to the flame effect to allow it to expire. If a maximum duration is set then once it is reached we do not allow any further flickers and stop the effect when the current hue/temperature/luminance has decayed back to median.

* Add parameters to the fire effect to allow it to die back, going from a blaze to embers over some period of time and optionally even going out eventually.

* Add an option to turn group support off. If you are not using groups this will reduce polling overhead saving bandwidth and power.

* Add pseudo-effects that set the fade on/off times.
