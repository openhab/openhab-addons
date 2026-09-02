# Orvibo Binding

This binding integrates Orvibo devices that communicate using UDP.
Primarily this was designed for S20 Wi‑Fi sockets, but other products using the same protocol may be implemented in the future.

## Supported Things

- S20 Wi‑Fi sockets

## Discovery

This binding can automatically discover devices that have already been added to the Wi‑Fi network. Please see the instruction manual or the in‑app help guide for instructions on how to add your device to your Wi‑Fi network.

## Binding Configuration

This binding does not require any special configuration.

## Thing Configuration

This is optional; it is recommended to let the binding discover and add Orvibo devices.
To manually configure an S20 Thing you must specify its deviceId (MAC address).
In the Thing file, this looks like:

```java
Thing orvibo:s20:mysocket [ deviceId="AABBCCDDEEFF"]
```

## Channels

### S20

| Channel | Description                   | Example                   |
|---------|-------------------------------|---------------------------|
| power   | Current power state of switch | orvibo:s20:mysocket:power |

## `orvibo.items` Example

```java
Switch MySwitch              "Switch state [%s]"  { channel="orvibo:s20:mysocket:power" }
```

## `orvibo.sitemap` Example

Using the above Things and Items
Sitemap:

```perl
sitemap demo label="Main Menu" {
        Frame  {
                Switch item=MySwitch
        }
}
```
