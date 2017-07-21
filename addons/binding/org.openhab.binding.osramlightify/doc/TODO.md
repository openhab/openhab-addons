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

* When we add a thing we should request an immediate poll.
  * But ONLY if we add it while running, NOT during openHAB start up.
  * Actually it would be fine if we had a mechanism to collapse identical back-to-back messages in the TX queue.

* When we remove a device should we remove any outstanding messages in the TX queue?

* We do not really need to update the discovery inbox on _every_ poll. There is a lot of wasted cpu there.

* I18n.

* Add a switch channel that indicates whether we are in white mode or colour mode.
  * But how do we tell initially? Do we just force it on start?

* Map on/off to soft on/off (i.e. brightness changes) if transition time is non-zero as normal on/off commands do not allow a transition time.

* Is it possible to multiple outstanding requests if they are for different devices? If we have multiple connections?
