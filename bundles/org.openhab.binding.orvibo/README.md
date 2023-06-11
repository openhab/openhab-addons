# Orvibo Binding

This binding integrates Orvibo devices that communicate using UDP.
Primarily this was designed for S20 Wifi Sockets but other products using the same protocol may be implemented in future.

## Supported Things

- S20 Wifi Sockets

## Discovery

This binding can automatically discover devices that have already been added to the Wifi network.  Please see the check your instruction manual or the help guide in the app for instructions on how to add your device to your Wifi network.

## Binding Configuration

This binding does not require any special configuration.

## Thing Configuration

This is optional, it is recommended to let the binding discover and add Orvibo devices.
To manually configure an S20 Thing you must specify its deviceId (MAC address).
In the thing file, this looks like:

```java
Thing orvibo:s20:mysocket [ deviceId="AABBCCDDEEFF"]
```

## Channels

### S20:

| Channel | Description                   | Example                   |
|---------|-------------------------------|---------------------------|
| power   | Current power state of switch | orvibo:s20:mysocket:power |

## Items:

```java
Switch MySwitch              "Switch state [%s]"  { channel="orvibo:s20:mysocket:power" }
```

## Example Sitemap

Using the above things channels and items
Sitemap:

```perl
sitemap demo label="Main Menu" {
        Frame  {
                Switch item=MySwitch
        }
}
```
