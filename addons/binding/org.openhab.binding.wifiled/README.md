# WiFi LED Binding

This Binding is used to control LED stripes connected by WiFi. These devices are sold with different names, i.e. Magic Home LED, UFO LED, LED NET controller, etc.  

## Supported Things

This Binding supports RGB(W) LED devices.

## Discovery

The LED WiFi Controllers are discovered by broadcast. The device has to be connected to the local network (i.e. by using the WiFi PBC connection method or the native App shipped with the device). For details please refer to the manual of the device. 

## Binding Configuration

No binding configuration required.

## Thing Configuration

Usually no manual configuration is required.
If the automatic discovery does not work for some reason then the IP address and the port have to be set manually. Optionally, a refresh interval (in seconds) can be defined.
The binding supports newer controllers also known as v3 or LD382A (default) and the older generation also known as LD382. As the two generations differ in their protocol it might be necessary to set the configuration appropriately.

## Channels

- **color** Color of the RGB LEDs expressed as values of hue, saturation and brightness
- **white** The brightness of the (warm) white LEDs
- **program** The program to be automatically run by the controller (i.e. color cross fade, strobe, etc.)
- **programSpeed** The speed of the programm

## Full example
N/A
