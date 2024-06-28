# SNMP Binding

This binding integrates the Simple Network Management Protocol (SNMP).
SNMP can be used to monitor or control a large variety of network equipment, e.g. routers, switches, NAS-systems.
Currently, protocol version 1 and 2c are supported.

## Supported Things

There are two supported things:

 - `target` for SNMP v1/v2c agents
 - `target3` for SNMP v3 agents

Both represent a single network device. 
Things can be extended with `number`, `string` and `switch` channels.

## Binding Configuration

If only GET/SET-requests shall be used, no configuration is necessary.
In this case the `port` parameter defaults to `0`.

For receiving traps a port for receiving traps needs to be configured.
The standard port for receiving traps is 162, however binding to ports lower than 1024 is only allowed with privileged right on most *nix systems.
Therefore, it is recommended to bind to a port higher than 1024 (e.g. 8162).
In case the trap sending equipment does not allow to change the destination port (e.g. Mikrotik routers), it is necessary to forward the received packets to the new port.
This can be done either by software like _snmptrapd_ or by adding a firewall rule to your system, e.g. by executing

```shell
iptables -t nat -I PREROUTING --src 0/0 --dst 192.168.0.10 -p udp --dport 162 -j REDIRECT --to-ports 8162
```

would forward all TCP packets addressed to 192.168.0.10 from port 162 to 8162.
Check with your operating system manual how to make that change permanent.

Example configuration for using port 8162:

```text
# Configuration for the SNMP Binding
#
# Port used for receiving traps.
# This setting defaults to 0 (disabled / not receiving traps)
port=8162
```

## Thing Configuration

### Common parameters for all thing-types

The `hostname` is mandatory and can be set as FQDN or IP address. 

An optional configuration parameter is `refresh`.
By using the `refresh` parameter the time between two subsequent GET requests to the target can be set.
The default is `60` for 60s.

Three advanced parameters are available `port`, `timeout`, `retries`
Usually these do not need to be changed.

If the SNMP service on the target is running on a non-standard port, it can be set with the `port` parameter.
It defaults to 161.

By using the `timeout` and `retries` parameters the timeout/error behaviour can be defined.
A single request times out after `timeout` ms.
After `retries` timeouts the refresh operation is considered to be fails and the status of the thing set accordingly.
The default values are `timeout=1500` and `retries=2`.

### `target`

The `target` thing has two optional configuration parameters: `community` and `version`.

The SNMP community for SNMP version 2c can be set with the `community` parameter.
It defaults to `public`.

Currently two protocol versions are supported.
The protocol version can be set with the `protocol` parameter.
The allowed values are `v1` or `V1` for v1 and `v2c` or `V2C` for v2c.
The default is `v1`.

### `target3`

The `target3` thing has an additional mandatory parameter: `user`.
This value of this parameter is named "securityName" or "userName" in most agents.

Optional configuration parameters are: `securityModel`, `authProtocol`, `authPassphrase`, `privProtocol` and `privPassphrase`.

The `securityModel` can be set to

- `NO_AUTH_NO_PRIV` (default) - no encryption on authentication data, no encryption on transmitted data
- `AUTH_NO_PRIV` - encryption on authentication data, no encryption on transmitted data 
- `AUTH_PRIV` - encryption on authentication data, encryption on transmitted data

Depending on the `securityModel` some of the other parameters are also mandatory.

If authentication encryption is required, at least `authPassphrase` needs to be set, while `authProtocol` has a default of `MD5`.
Other possible values for `authProtocol` are `SHA`, `HMAC128SHA224`, `HMAC192SHA256`, `HMAC256SHA384` and `HMAC384SHA512`.

If encryption of transmitted data (privacy encryption) is required, at least `privPassphrase` needs to be set, while `privProtocol` defaults to `DES`.
Other possible values for `privProtocol` are `DES3`, `AES128`, `AES192` and `AES256`.

## Channels

The `target` thing has no fixed channels.
It can be extended with channels of type `number`, `string`, `switch`.

All channel-types have one mandatory parameter: `oid`.
It defines the OID that should be linked to this channel in dotted format (e.g. .1.2.3.4.5.6.8).

Channels can be configured in four different modes via the `mode` parameter.
Available options are `READ`, `WRITE`, `READ_WRITE` and `TRAP`.
`READ` creates a read-only channel, i.e. data is requested from the target but cannot be written.
`WRITE` creates a write-only channel, i.e. the status is never read from the target but changes to the item are written to the target.
`READ_WRITE` allows reading the status and writing it for controlling remote equipment.
`TRAP` creates a channel that ONLY reacts to traps.
It is never actively read and local changes to the item's state are not written to the target.
Using`TRAP` channels requires configuring the receiving port (see "Binding configuration").

The `datatype` parameter is needed in some special cases where data is written to the target.
The default `datatype` for `number` channels is `UINT32`, representing an unsigned integer with 32 bit length.
Alternatively `INT32` (signed integer with 32 bit length), `COUNTER64` (unsigned integer with 64 bit length) or `FLOAT` (floating point number) can be set.
Floating point numbers have to be supplied (and will be sent) as strings.
For `string` channels the default `datatype` is `STRING` (i.e. the item's will be sent as a string).
If it is set to `IPADDRESS`, an SNMP IP address object is constructed from the item's value.
The `HEXSTRING` datatype converts a hexadecimal string (e.g. `aa bb 11`) to the respective octet string before sending data to the target (and vice versa for receiving data).

`number`-type channels can have a parameter `unit` if their `mode` is set to `READ`. This will result in a state update applying [UoM](https://www.openhab.org/docs/concepts/units-of-measurement.html) to the received data if the UoM symbol is recognised.

`switch`-type channels send a pre-defined value if they receive `ON` or `OFF` command in `WRITE` or `READ_WRITE` mode.
In `READ`, `READ_WRITE` or `TRAP` mode they change to either `ON` or `OFF` on these values.
The parameters used for defining the values are `onvalue` and `offvalue`.
The `datatype` parameter is used to convert the configuration strings to the needed values.

`number`-type channels have a `unit` parameter.
The unit is added to the received value before it is passed to the channel.
For commands (i.e. sending), the value is first converted to the configured unit. 

| type     | item   | description                     |
|----------|--------|---------------------------------|
| number   | Number | a channel with a numeric value  |
| string   | String | a channel with a string value   |
| switch   | Switch | a channel that has two states   |


### SNMP Exception (Error) Handling

The standard behaviour if an SNMP exception occurs this is to log at `INFO` level and set the channel value to `UNDEF`.
This can be adjusted at channel level with advanced options.

The logging can be suppressed with the `doNotLogException` parameter.
If this is set to `true` any SNMP exception is not considered as faulty.
The default value is `false`.

By setting `exceptionValue` the default `UNDEF` value can be changed.
Valid values are all valid values for that channel (i.e. `ON`/`OFF` for a switch channel, a string for a string channel and a number for a number channel).

## Full Example

demo.things:

```
Thing snmp:target:router [ hostname="192.168.0.1", protocol="v2c" ] {
    Channels:
        Type number : inBytes [ oid=".1.3.6.1.2.1.31.1.1.1.6.2", mode="READ" ]
        Type number : outBytes [ oid=".1.3.6.1.2.1.31.1.1.1.10.2", mode="READ" ]
        Type number : if4Status [ oid="1.3.6.1.2.1.2.2.1.7.4", mode="TRAP" ]
        Type switch : if4Command [ oid="1.3.6.1.2.1.2.2.1.7.4", mode="READ_WRITE", datatype="UINT32", onvalue="2", offvalue="0" ]
        Type switch : devicePresent [ oid="1.3.6.1.2.1.2.2.1.221.4.192.168.0.1", mode="READ", datatype="UINT32", onValue="1", doNotLogException="true", exceptionValue="OFF" ]
        Type switch : valueReceived [ oid="1.3.6.1.2.1.2.2.1.221.17.5", mode="READ", datatype="HEXSTRING", onValue="00 AA 11", offValue="00 00 00" ]
}
```

demo.items:

```java
Number inBytes "Router bytes in [%d]" { channel="snmp:target:router:inBytes" }
Number outBytes "Router bytes out [%d]" { channel="snmp:target:router:outBytes" }
Number if4Status "Router interface 4 status [%d]" { channel="snmp:target:router:if4Status" }
Switch if4Command "Router interface 4 switch [%s]" { channel="snmp:target:router:if4Command" }
Switch devicePresent "Phone connected [%s]" { channel="snmp:target:router:devicePresent" }
Switch receivedValue "Received 00 AA 11 [%s]" { channel="snmp:target:router:valueReceived" }
```

demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame {
        Text item=inBytes
        Text item=outBytes
        Text item=if4Status
        Switch item=if4Command
        Text item=devicePresent
        Text item=receivedValue
    }
}
```
