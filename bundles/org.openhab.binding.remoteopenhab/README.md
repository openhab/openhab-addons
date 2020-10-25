# Remote openHAB Binding

The Remote openHAB binding allows to communicate with remote openHAB servers.
The communication is bidirectional.
The binding on the local server listens to any item state updates on the remote server and updates accordingly the linked channel on the local server.
It also transfers any item command from the local server to the remote server.

One first usage is the distribution of your home automation system on a set of openHAB servers.

A second usage is for users having old openHAB v1 bindings running that were not migrated to openHAB v2 or openHAB v3.
They can keep an openHAB v2 server to run their old openHAB v1 bindings and setup a new openHAB v3 server for everything else.
The Remote openHAB binding installed on the openHAB v3 server will then allow to use the openHAB v1 bindings through communication with the openHAB v2 server.

A third usage is for users that would like to keep unchanged an existing openHAB v2 server but would like to use the new UI from openHAB v3; they can simply setup a new openHAB v3 server with the Remote openHAB binding linked to their openHAB v2 server.

## Supported Things

There is one unique supported thing : the `server` bridge thing 

## Discovery

All openHAB servers in the local network are automatically discovered (through mDNS) by the binding.
You will find in the inbox one discovery thing per remote server interface.
So if your remote server has one IPv4 address and one IPv6 address, you will discover two things in the inbox.
Just choose one of the two things.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has the following configuration parameters:

| Parameter | Required | Description                                                                                            |
|-----------|-------------------------------------------------------------------------------------------------------------------|
| host      | yes      | The host name or IP address of the remote openHAB server.                                              |
| port      | yes      | The HTTP port to be used to communicate with the remote openHAB server. Default is 8080.               |
| restPath  | yes      | The subpath of the REST API on the remote openHAB server. Default is /rest                             |
| token     | no       | The token to use when the remote openHAB server is setup to require authorization to run its REST API. |

## Channels

The channels are built dynamically and automatically by the binding.
One channel is created for each item from the remote server.
Only basic groups (with no state) are ignored.
The channel id of the built channel corresponds to the name of the item on the remote server.

## Limitations

* The binding will not try to communicate with an openHAB v1 server.
* The binding only uses the HTTP protocol for the communications with the remote server (not HTTPS).

## Example

### demo.things:

```
Bridge remoteopenhab:server:oh2 "OH2 server" [ host="192.168.0.100" ]
```

### demo.items:

```
DateTime MyDate "Date [%1$tA %1$td %1$tR]" <calendar> { channel="remoteopenhab:server:oh2:MyDate" }
```
