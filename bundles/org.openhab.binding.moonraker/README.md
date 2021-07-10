# Klipper/Moonraker Binding

This binding supports 3D printer running Klipper via the Moonraker API.
For more information on Klipper: [klipper | Klipper is a 3d-printer firmware (klipper3d.org)](https://www.klipper3d.org/)
For more information on the Moonraker Web API: [Arksine/moonraker: Web API Server for Klipper (github.com)](https://github.com/Arksine/moonraker) 

## Supported Things

3D Printer controlled by Klipper

## Discovery

Auto discovery is not supported. The hostname of the machine runnign Moonraker must be entered manually

## Binding Configuration

No binding configuration

## Thing Configuration

Host is the hostname or ipaddress of the machine running Moonraker. You can customize the port on which Moonraker is running and optionally you can specify an API Key.

## Channels

Almost all field documented in: https://github.com/Arksine/moonraker/blob/master/docs/printer_objects.md are made accessible.