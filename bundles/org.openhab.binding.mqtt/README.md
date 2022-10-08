# MQTT Binding

> MQTT is a machine-to-machine (M2M)/"Internet of Things" connectivity protocol.
> It was designed as an extremely lightweight publish/subscribe messaging transport.

MQTT is a server/client architecture.

A server, also called broker is not provided within this binding.
You can use any of the freely available MQTT Brokers like [Mosquitto](https://mosquitto.org/).

This particular binding allows to configure connections to brokers via openHAB Things.
This binding does NOT allow you to link Channels to MQTT topics or perform auto-discovery of available
MQTT topics. Please check out the available extensions:

<!--list-subs-->

## Supported Bridges

* Broker: This bridge represents an MQTT Broker connection, configured and managed by this binding.

## Bridge Configuration
 
Required configuration parameters are:

* __host__: The IP/Hostname of the MQTT broker. Be aware that this binding allows only one bridge / one connection per unique host:port.
* __port__: The optional port of the MQTT broker. If none is provided, the typical ports 1883 and 8883 (SSL) are used. Be aware that this binding allows only one bridge / one connection per unique host:port.
* __secure__: Uses TLS/SSL to establish a secure connection to the broker. Can be true or false. Defaults to false.

Additionally the following parameters can be set:

* __hostnameValidated__: Validate hostname from certificate against server hostname for secure connection. Defaults to true.
* __protocol__:  The protocol used for communicating with the broker (TCP, WEBSOCKETS). Defaults to TCP.
* __mqttVersion__: The MQTT version used for communicating with the broker (V3, V5). Defaults to V3.
* __qos__: Quality of Service. Can be 0, 1 or 2. Please read the MQTT specification for details. Defaults to 0.
* __clientID__: Use a fixed client ID. Defaults to empty which means a user ID is generated for this connection.

Reconnect parameters are:

* __reconnectTime__: Reconnect time in ms. If a connection is lost, the binding will wait this time before it tries to reconnect. Defaults to 60000 (60s).
* __keepAlive__: Keep alive / heartbeat timer in s. It can take up to this time to determine if a server connection is lost. A lower value may keep the broker unnecessarily busy for no or little additional value. Defaults to 60s.

An MQTT last will and testament can be configured:

* __lwtMessage__: An optional last will and testament message. Defaults to empty.
* __lwtTopic__: The last will topic. Defaults to empty and therefore disables the last will.
* __lwtQos__: The optional qos of the last will. Defaults to 0.
* __lwtRetain__: Retain last will message. Defaults to true.

An MQTT message can be published upon a successful connection to the MQTT broker with these parameters:

* __birthMessage__: An optional message to be published once the bridge established a connection to the MQTT broker. Defaults to empty.
* __birthTopic__: The birth topic. Defaults to empty and therefore no birth message will be published.
* __birthRetain__: Retain the birth message. Defaults to true.

An MQTT message can be published just before disconnecting from the broker with these parameters:

* __shutdownMessage__: An optional message to be published before the bridge disconnects from the MQTT broker. Defaults to empty.
* __shutdownTopic__: The shutdown topic. Defaults to empty and therefore no shutdown message will be published.
* __shutdownRetain__: Retain the shutdown message. Defaults to true.

For more security, the following optional parameters can be altered:

* __username__: The MQTT username (since MQTT 3.1). Defaults to empty.
* __password__: The MQTT password (since MQTT 3.1). Defaults to empty.
* __certificatepin__: If this is set: After the next connection has been successfully established, the certificate is pinned. The connection will be refused if another certificate is used. Clear **certificate** to allow a new certificate for the next connection attempt. This option will increase security.
* __publickeypin__: If this is set: After the next connection has been successfully established, the public key of the broker is pinned. The connection will be refused if another public key is used. Clear **publickey** to allow a new public key for the next connection attempt. This option will increase security.
* __certificate__: The certificate hash. If **certificatepin** is set this hash is used to verify the connection. Clear to allow a new certificate pinning on the next connection attempt. If empty will be filled automatically by the next successful connection. An example input would be `SHA-256:83F9171E06A313118889F7D79302BD1B7A2042EE0CFD029ABF8DD06FFA6CD9D3`.
* __publickey__: The public key hash. If **publickeypin** is set this hash is used to verify the connection. Clear to allow a new public key pinning on the next connection attempt. If empty will be filled automatically by the next successful connection. An example input would be `SHA-256:83F9171E06A313118889F7D79302BD1B7A2042EE0CFD029ABF8DD06FFA6CD9D3`.

By default discovery services (like homie or homeassistant) are enabled on a broker.
This behaviour can be controlled with a configuration parameter.

* __enableDiscovery__:If set to true, enables discovery on this broker, if set to false, disables discovery services on this broker.

## Supported Channels

You can extend your broker connection bridges with a channel:

* __publishTrigger__: This channel is triggered when a value is published to the configured MQTT topic on this broker connection. The event payload (in `receivedEvent`) will be the received MQTT topic and its value, separated by the hash character (`#`).

Configuration parameters are:

* __stateTopic__: This channel will trigger on this MQTT topic. This topic can contain wildcards like + and # for example "all/in/#" or "sensors/+/config".
* __payload__: An optional condition on the value of the MQTT topic that must match before this channel is triggered.

Note for new users - direct broker Bridge channels are rarely needed. You almost certainly will want to be using one of the binding extensions, or the generic Things and Channels features for most devices or services.
