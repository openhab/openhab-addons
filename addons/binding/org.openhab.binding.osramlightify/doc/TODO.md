* When we handle a LIST_PAIRED message we need to offline any devices that are no longer paired.
  * Can we use bridge-agnostic thing UIDs and reparent existing things to different bridges?
  * Yes. The thing can use getBridge().removeThing(this), setBridgeUID(<newUID>), bridge.addThing(thing).
  * But first we need to get the thing and it is not on the current bridge...

* Transition time: could we make it a rate? i.e. time given is for 0-100% so to go 0-50% we scale the transition time so the _rate_ is constant rather than the _time_.
  * what about ops on groups? If we perform an op on multiple things we may want them to complete at the same time?
  * so we can have absolute transition time or scale rates
    * we need toggles in the thing config to say which we want there
    * this will need to be handled in PolicyCommand framework probably. Policies attach to messages and are applied in the transmit queue (which becomes a priority queue) and/or encodedMessage (which sets transition time/rate)

* Add a virtual all-paired-devices thing. (Address ff:ff:ff:ff:ff:ff:ff:ff)

* Could we / should we attempt to maintain state for groups? We would have to merge each member item change into the group state.

* When we remove a device should we remove any outstanding messages in the TX queue?

* We do not really need to update the discovery inbox on _every_ poll. There is a lot of wasted cpu there.

* I18n.

* Add a switch channel that indicates whether we are in white mode or colour mode.
  * But how do we tell initially? Do we just force it on start?

* Map on/off to soft on/off (i.e. brightness changes) if transition time is non-zero as normal on/off commands do not allow a transition time.
  * For off, set device state power to off then do a luminance transition to 0. The state should ignore the luminance change.
  * For on, do a luminance transition to whatever the state says the current luminance is.

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

* Add a firmware table detailing known firmware deficiencies and pairing requirements. Those who do not want to advertise their state to "approved" partners of OSRAM may not be getting prompted about availability of firmware updates and may not care.

* Add a config switch to allow items to be updated during transitions. Sometimes, especially with long-preiod transitions you may want to have the current state reflected by items.
  * Might there be any way to make it a per-command option? Maybe a time threshold above which updates are done? Or maybe it is something that could be part of the (TBD) priority queuing framework?

* Groups should ignore reachable in state - they always show as 0

* Changing the luminance via the color channel should update the dimmer channel and vice versa - groups have both
  * Would it be too confusing if groups only had the color channel bearing in mind they may not have _any_ RGBW members?

* Currently groups never update state themselves. This means that a transition on a group will transition each individual member but the members do not know about the transition and thus do not suppress updates during it and do not force an update after it.
  * We could record group membership and use that when starting transitions to start the transition on each member. Then they would suppress updates and force a GET_DEVICE_INFO on completion. Of course, that means there are potentially lots of GET_DEVICE_INFOs all at the same time...
  * Can we groupcast a GET_DEVICE_INFO? What would the response look like?
  * We should have a per-device option to switch update suppression on and off and should be able to override it from the group level.
