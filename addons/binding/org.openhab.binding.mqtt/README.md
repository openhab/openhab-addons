# MQTT Binding

> MQTT is a machine-to-machine (M2M)/"Internet of Things" connectivity protocol.
> It was designed as an extremely lightweight publish/subscribe messaging transport.

MQTT is a server/client architecture.
A server, also called broker is not provided within this binding,
but it allows to detect running brokers and to manage connections.
The hereby configured broker connections make it possible to link MQTT topics to Things and Channels.

It has the following extensions:

<!--list-subs-->

## Supported Bridges

* Broker: This bridge represents an MQTT Broker connection, configured and managed by this binding.
* SystemBroker: A system configured broker cannot be changed by this binding and will be listed as read-only system-broker.

## Bridge Configuration
 
Required configuration parameters are:

* __host__: The IP/Hostname of the MQTT broker. Be aware that this binding allows only one bridge / one connection per unique host:port.
* __port__: The optional port of the MQTT broker. If none is provided, the typical ports 1883 and 8883 (SSL) are used. Be aware that this binding allows only one bridge / one connection per unique host:port.
* __secure__: Uses TLS/SSL to establish a secure connection to the broker. Can be true or false. Defaults to false.

Additionally the following parameters can be set:

* __qos__: Quality of Service. Can be 0, 1 or 2. Please read the MQTT specification for details. Defaults to 0.
* __clientID__: Use a fixed client ID. Defaults to empty which means a user ID is generated for this connection.
* __retainMessages__: Retain messages. Defaults to false.

Reconnect parameters are:

* __reconnectTime__: Reconnect time in ms. If a connection is lost, the binding will wait this time before it tries to reconnect. Defaults to 60000 (60s).
* __keepAlive__: Keep alive / heartbeat timer in ms. It can take up to this time to determine if a server connection is lost. A lower value may keep the broker unnecessarily busy for no or little additional value. Defaults to 60000 (60s).

An MQTT last will and testament can be configured:

* __lwtMessage__: An optional last will and testament message. Defaults to empty. 
* __lwtTopic__: The last will topic. Defaults to empty and therefore disables the last will. 
* __lwtQos__: The optional qos of the last will. Defaults to 0. 
* __lwtRetain__: Retain last will message. Defaults to false.

For more security, the following optional parameters can be altered:

* __username__: The MQTT username (since MQTT 3.1). Defaults to empty.
* __password__: The MQTT password (since MQTT 3.1). Defaults to empty.
* __certificatepin__: If this is set: After the next connection has been successfully established, the certificate is pinned. The connection will be refused if another certificate is used. Clear **certificate** to allow a new certificate for the next connection attempt. This option will increase security.
* __publickeypin__: If this is set: After the next connection has been successfully established, the public key of the broker is pinned. The connection will be refused if another public key is used. Clear **publickey** to allow a new public key for the next connection attempt. This option will increase security.
* __certificate__: The certificate hash. If **certificatepin** is set this hash is used to verify the connection. Clear to allow a new certificate pinning on the next connection attempt. If empty will be filled automatically by the next successful connection. An example input would be `SHA-256:83F9171E06A313118889F7D79302BD1B7A2042EE0CFD029ABF8DD06FFA6CD9D3`.
* __publickey__: The public key hash. If **publickeypin** is set this hash is used to verify the connection. Clear to allow a new public key pinning on the next connection attempt. If empty will be filled automatically by the next successful connection. An example input would be `SHA-256:83F9171E06A313118889F7D79302BD1B7A2042EE0CFD029ABF8DD06FFA6CD9D3`.

## Supported Channels

You can extend your broker connection bridges with a channel:

* __publishTrigger__: This channel is triggered when a value is published to the configured MQTT topic on this broker connection. The event payload (in `receivedEvent`) will be the received MQTT topic and its value, separated by the hash character (`#`).

Configuration parameters are:

* __stateTopic__: This channel will trigger on this MQTT topic. This topic can contain wildcards like + and # for example "all/in/#" or "sensors/+/config".
* __payload__: An optional condition on the value of the MQTT topic that must match before this channel is triggered.

## Full Example

In a first example a very secure connection to a broker is defined. It pins the returned certificate and public key.
If someone tries a man in the middle attack later on, this broker connection will recognize it and refuse a connection.
Be aware that if your brokers certificate changes, you need to remove the connection entry and add it again. 

The second connection is a plain, unsecured one. Unsecure connections are default, if you do not provide the "secure" parameter. Use this only for local MQTT Brokers.

A third connection uses a username and password for authentication.
Secure is set to false as the username and password is requested by the broker.
The credentials are plain values on the wire, therefore you should only use this on a secure connection.

In a forth connection, the public key pinning is enabled again.
This time, a public key hash is provided to pin the connection to a specific server.
It follows the form "hashname:hashvalue". Valid *hashnames* are SHA-1, SHA-224, SHA-256, SHA-384, SHA-512 and all others listed
in [Java MessageDigest Algorithms](https://docs.oracle.com/javase/9/docs/specs/security/standard-names.html#messagedigest-algorithms).

`mqttConnections.things`:

```xtend
mqtt:broker:mySecureBroker [ host="192.168.0.41", secure=true, certificatepin=true, publickeypin=true ]
mqtt:broker:myUnsecureBroker [ host="192.168.0.42", secure=false ]

mqtt:broker:myAuthentificatedBroker [ host="192.168.0.43", secure=false, username="user", password="password" ]

mqtt:broker:pinToPublicKey [ host="192.168.0.44", secure=true, publickeypin=true, publickey="SHA-256:9a6f30e67ae9723579da2575c35daf7da3b370b04ac0bde031f5e1f5e4617eb8" ]
```
