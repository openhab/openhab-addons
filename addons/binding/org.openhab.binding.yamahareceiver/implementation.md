# Yamahareceiver Binding - Developer documentation
An overview is presented with the following diagram:
![Class Diagram](doc/classes.png "Class Diagram")

The control flow is visualised here:
![ControlFlow](doc/ControlFlow.png "ControlFlow")

The YamahaReceiverCommunication class is a pure protocol implementation.
Yamaha uses a XML API. The receiver does not allow long polling or any
other type of state-update notification, therefore we use a timer for
periodically refreshing the state.

The thing handler needs a host and zone configuration that is usually
given by the discovery service (YamahaDiscoveryParticipant via UPNP).
createCommunicationObject() is called in initialize(). There we create
a communication object (YamahaReceiverCommunication) which needs the host
and zone parameters and hand it over to a newly created YamahaReceiverState.
We also setup the refresh timer.

There is a second discovery service, the ZoneDiscoveryService which is created
and used in createCommunicationObject() Main_Zone thing handler.
For every other zone a discovery result will be generated and can be added by the user.

## Authors
 * David Gräff <david.graeff@tu-dortmund.de>, 2016