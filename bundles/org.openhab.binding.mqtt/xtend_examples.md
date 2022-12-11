# Xtend Examples

## Secure connection

In a first example a very secure connection to a broker is defined. It pins the returned certificate and public key.
If someone tries a man in the middle attack later on, this broker connection will recognize it and refuse a connection.
Be aware that if your brokers certificate changes, you need to remove the connection entry and add it again.

`mqttConnections.things`:

```java
mqtt:broker:mySecureBroker [ host="192.168.0.41", secure=true, certificatepin=true, publickeypin=true ]
```

## Plain, unsecured connection

The second connection is a plain, unsecured one. Unsecure connections are default, if you do not provide the "secure" parameter. Use this only for local MQTT Brokers.

`mqttConnections.things`:

```java
mqtt:broker:myUnsecureBroker [ host="192.168.0.42", secure=false ]
```

## Authentication with username and password

A third connection uses a username and password for authentication.
Secure is set to false in this example. This is a bad idea!
The credentials are plain values on the wire, therefore you should only use this on a secure connection.

`mqttConnections.things`:

```java
mqtt:broker:myAuthentificatedBroker [ host="192.168.0.43", secure=false, username="user", password="password" ]
```

## Public key pinning

In a fourth connection, the public key pinning is enabled again.
This time, a public key hash is provided to pin the connection to a specific server.
It follows the form "hashname:hashvalue". Valid _hashnames_ are SHA-1, SHA-224, SHA-256, SHA-384, SHA-512 and all others listed
in [Java MessageDigest Algorithms](https://docs.oracle.com/javase/9/docs/specs/security/standard-names.html#messagedigest-algorithms).

`mqttConnections.things`:

```java
mqtt:broker:pinToPublicKey [ host="192.168.0.44", secure=true, publickeypin=true, publickey="SHA-256:9a6f30e67ae9723579da2575c35daf7da3b370b04ac0bde031f5e1f5e4617eb8" ]
```
