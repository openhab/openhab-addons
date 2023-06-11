# Serial Button Binding

This is a binding for probably one of the simplest devices possible: A simple push button which short-cuts two pins on a serial port.

## Supported Things

The binding defines a single thing type called `button`.

A button requires the single configuration parameter `port`, which specifies the serial port that should be used.

The only available channel is a `SYSTEM_RAWBUTTON` channel called `button`, which emits `PRESSED` events (no `RELEASED` events though) whenever data is available on the serial port (which will be read and discarded).

The use case is simple: Connect any push button to pins 2 and 7 of an RS-232 interface as short-cutting those will signal that data is available.
Using the default toggle profile, this means that you can use this channel to toggle any Switch item through the button.

## Full Example

demo.things:

```java
serialbutton:button:mybutton "My Button" [ port="/dev/ttyS0" ]
```

demo.items:

```java
Switch MyLight { channel="serialbutton:button:mybutton:button" }
```

_Note:_ This is a trigger channel, so you will most likely bind a second (state) channel to your item, which will control your physical light, so you might end up with the following, if you want to use your button with a Hue bulb:

```java
Switch MyLight { channel="hue:0210:1:bulb1:color,serialbutton:button:mybutton:button" }
```

demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame {
        Switch item=MyLight label="My Light"
    }
}
```
