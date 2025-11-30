# Meross Binding

This binding integrates **Meross**&reg; devices.

The binding will connect to the Meross cloud to get the devices on your account and get push messages with device status updates.
If possible, it will communicate in the local network with the device to send commands or refresh the device status.

## Supported Things

Supported Thing types

- `gateway` : Acts as a Bridge to your Meross cloud account.
- `light` : Represents a device recognized as a smart light or plug device with no specific support, on/off should work.
- `msg100`: Represents a garage door.
- `msg200`: Represents a triple garage door.
- `garage-door`: Represents a device recognized as garage door with no specific support, open/close should work.

|   Meross Name       | Type   | Thing Type | Description         | Supported | Tested |
|---------------------|--------|------------|---------------------|-----------|--------|
| Smart ambient light | msl430 | light      | Smart ambient light | yes       | yes    |
| Smart plug          | mss210 | light      | Smart plug          | yes       | yes    |
| Garage door         | msg100 | msg100     | Garage door         | yes       | yes    |
| Triple garage door  | msg200 | msg200     | Triple garage door  | yes       |        |

## Discovery

The Discovery service is supported.
Automatic discovery will run when the gateway start, or when manually scanning for new devices.
Background discovery is not supported.

Discovery tries to detect specific hardware and find the appropriate Thing type.
If no specific Thing type is available, it will default to a generic Thing type for the class of devices.

## Binding Configuration

To utilize the binding you should first create an account via the Meross Android or iOs app.
Moreover, the devices should be in an online status.

## Bridge Configuration

| Name     | Type | Description                                              | Default                    | Required | Advanced |
|----------|------|----------------------------------------------------------|----------------------------|----------|----------|
| hostname | text | Meross Hostname or IP address (for Europe located users) | https://iotx-eu.meross.com | yes      | yes      |
| email    | text | Email of your Meross Account                             | N/A                        | yes      | no       |
| password | text | Password of your Meross Account                          | N/A                        | yes      | no       |

### Other host locations

| Location     | Hostname                   |
|--------------|----------------------------|
| Asia-Pacific | https://iotx-ap.meross.com |
| US           | https://iotx-us.meross.com |

If you are outside of Europe, please set the appropriate `Hostname`.

NOTICE: Due to  **Meross**&reg; security policy please minimize host connections in order to avoid TOO MANY TOKENS (code 1301) error occurs which leads to a  8-10 hours suspension of your account.
The binding relies as much as possible on local http communication.
Therefore, background device discovery is also disabled.
You will need to manually scan for new devices.

## Thing Configuration

| Parameter | Type | Description                                              | Default | Required | Thing type id         | Advanced |
|-----------|------|----------------------------------------------------------|---------|----------|-----------------------|----------|
| name      | text | The name of the device as registered to Meross account   | N/A     | yes      | light, msg100, msg200 | no       |
| uuid      | text | The device uuid                                          | N/A     | yes      | light, msg100, msg200 | no       |
| ipAddress | text | The IP address of the device in the local network        | N/A     | no       | light, msg100, msg200 | no       |

The unique key to the device is the `uuid` and will be retrieved and set during discovery.
If you wish to use textual Thing configuration, you get the ID from the discovered Thing or through the console `devices` command.

The `ipAddress` will be retrieved during initial configuration of the device.
Once established, it will be used for local device communication.
For file based configurations, it is advised to set the IP address in the configuration to avoid overloading the cloud communication.

## Channels

Only power channel is supported:

| Channel      | Type          | Thing type |Read/Write | Description                                                  |
|--------------|---------------|------------|-----------|--------------------------------------------------------------|
| power        | Switch        | light      | x         | Power bulb/plug capability to control bulbs and plugs on/off |
| door-state   | Rollershutter | msg100     | x         | Garage door up/down control                                  |
| door-state-0 | Rollershutter | msg200     | x         | Garage door up/down control, first door                      |
| door-state-1 | Rollershutter | msg200     | x         | Garage door up/down control, second door                     |
| door-state-2 | Rollershutter | msg200     | x         | Garage door up/down control, third door                      |

## Console Commands

A number of commands are supported from the console:

- `meross devices <userEmail>`: get a list of devices and their Meross UUID.
- `meross fingerprint <userEmail> <device>`: get a device description in JSON format and write the result to a file.

The arguments are optional and will limit the selection of the returned information.

The `fingerprint` command is especially useful for devices currently not supported by the binding.
It contains information that will help developing new functionalities.
Fingerprint information is written in the user's home `meross` directory.
All personal information is masked.

## Full Example

### meross.things

```java
Bridge meross:gateway:mybridge "Meross bridge" [ hostName="https://iotx-eu.meross.com", userEmail="abcde" userPassword="fghij" ] {
    light SC_plug                 "Desk"       [ name="Desk", uuid="320455acf9845" ]
}
```

### meross.items

```java
Switch              iSC_plug                 "Desk"                                    { channel="meross:light:mybridge:SC_plug:power" }
```

### meross.sitemap Example

```perl
sitemap meross label="Meross Binding Example Sitemap"
{
    Frame label="Living Room"
    {
          Default item=iSC_plug          icon="light"
    }
}
```
