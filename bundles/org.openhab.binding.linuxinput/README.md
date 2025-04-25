# Linux Input Binding

This binding allows to you use a keyboard to control your openHAB instance.
It works by exposing all keys on the keyboard as channels.

As indicated by the name of the binding this only works on Linux.
(It uses the libevdev library the receive events from the kernel)
As all the low-level protocols are handled by the Linux kernel it works for any
kind of keyboard; USB, Bluetooth, etc.

## Supported Things

All keyboards supported by the Linux kernel.

## Discovery

The discovery feature finds all cold- or hotplugged keyboards by watching the
`/dev/input/` directory.
The discovery uses the numeric ids for the devices. (`/dev/input/event0`,
etc...).
This can lead to issues when the kernel autodiscovery enumerates devices in a
nondeterministic order. This problem can be circumvented by using predictable
device names in `/dev/input/by-id/` or `/dev/input/by-path/` or by using Udev
facilities (out of scope for this document).

## Binding Configuration

openHAB will need rights on the `/dev/input/` files it is supposed to access.
This can be implemented by group-memberships, (custom) initscripts or Udev
rules.
The exact configurations possible depend on your system (out of scope for this document).

The `libevdev` library has to be installed for this plugin to work.
(It should be available via your package manager)

## Thing Configuration

Each thing has has to be explicitly enabled after it is configured.
While it is enabled _all_ of the generated events will be consumed by openHAB.
The device will not be available for normal input processing!

### Static configuration

#### Thing

```java
Thing linuxinput:input-device:some-keyboard [ enable=true, path="/dev/input/eventXX" ]
```

#### Item

```java
Contact SomeButton "Some Button" { channel="linuxinput:input-device:event17:keypresses#KEY_0" }
```

## Channels

Each Thing provides multiple channels

- A `key` channel that aggregates all events.
- Per physical key channels.

### Events

The following happens when pressing and releasing a key:

#### Press

1. State of global key channel updated to new key.
1. State of per-key channel updated to `"CLOSED"`.
1. Global key channel triggered with the current key name.
1. Per-key channel triggered with `"PRESSED"`".
1. State of global key channel updated to `""` (Empty string)

#### Release

1. State of per-key channel updated to `"OPEN"`
1. Per-key channel triggered with `"RELEASED"`

#### Rationale

Channel states are updated first to allow rules triggered by channel triggers to access the new state.

#### Channel names

The binding tries to translate the numeric event codes to their symbolic names; `KEY_1`, `KEY_A`, `KEY_BACKSPACE` etc.

If the currently installed version of libevdev does not know the symbolic name of a key, the numeric value is used.
Please note that future versions of libevdev may start translating the symbolic names.
