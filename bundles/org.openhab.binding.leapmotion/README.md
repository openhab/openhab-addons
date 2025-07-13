# Leap Motion Binding

The [Leap Motion](https://www.leapmotion.com/) controller is a gesture sensoring device that uses stereoscopic cameras and is connected through USB.
As all processing is done in software, it requires quite some powerful computer, such that it unfortunately does not work on single-board computers such as the Raspberry Pi.
In fact, the binding is currently only working on macOS computers with Intel x86 processors.

## Supported Things

There is a single thing of type `controller` defined and only one can be connected to the computer at a time.

## Discovery

The controller is automatically discovered if plugged into a USB port.

## Thing Configuration

The controller does not have any kind of configuration parameters.

## Channels

The controller has a single trigger channel `gesture`.
It generates the following events with a frequency of at most 200ms:

| Event         | Description                                                                |
|---------------|----------------------------------------------------------------------------|
| nohand        | No hand can be seen                                                        |
| tap           | A tap with a single finger                                                 |
| clockwise     | Rotating a finger clockwise                                                |
| anticlockwise | Rotating a finger anticlockwise                                            |
| fingersX_YYY  | Hand showing X fingers in a height of YYY mm (where YYY can be 1-3 digits) |

## Profiles

This binding specifies 3 profiles for the `gesture` channel to make it easy to link to existing items:

| Profile           | Description                                                                                                                                                                                                                                                                                           |
|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| leapmotion:switch | Simulates a toggle switch using the "tap" gesture                                                                                                                                                                                                                                                     |
| leapmotion:dimmer | Sends percentage values and supports two modes (configuration parameter `mode=fingers|height`): - fingers: 20% for every shown finger, i.e. 0=0%, 1=20%, 2=40%, 3=60%, 4=80%, 5=100% - height: If hand shows all 5 fingers, its height above the controller determines the value. Higher is brighter. |
| leapmotion:color  | Controls a color item by - using taps for switching on and off - height of hand (with 5 fingers shown) to dim - rotating a finger to loop through the colors                                                                                                                                          |

## Full Example

demo.things:

```java
Thing leapmotion:controller:1 MyLeapMotion
```

demo.items:

```java
Switch DemoSwitch  "Switch"         { channel="leapmotion:controller:1:gesture" }
Color  RGBLight    "RGB Light"      { channel="leapmotion:controller:1:gesture" }
Dimmer DimmedLight "Dimmer [%d %%]" { channel="leapmotion:controller:1:gesture"[profile="leapmotion:dimmer", mode="fingers"] }
```
