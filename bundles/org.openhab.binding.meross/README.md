# Meross Binding

This binding integrates **Meross**&reg; devices.

The binding will connect to the Meross cloud to get the devices on your account and get push messages with device status updates.
If possible, it will communicate in the local network with the device to send commands or refresh the device status.

## Supported Things

Supported thing types

- `gateway` : Acts as a Bridge to your Meross cloud account.
- `light` : Represents a light device like a Smart ambient light.
- `door`: Represents a garage door.
- `tripleDoor`: Represents a triple garage door.

|   Meross Name       | Type   | Description         | Supported | Tested |
|---------------------|--------|---------------------|-----------|--------|
| Smart ambient light | msl430 | Smart ambient light | yes       | yes    |
| Smart plug          | mss210 | Smart plug          | yes       | yes    |
| Garage door         | msg100 | Garage door         | yes       | yes    |
| Triple garage door  | msg200 | Triple garage door  | yes       |        |

## Discovery

The Discovery service is supported.
Automatic discovery will run when the gateway start, or when manually scanning for new devices.
Background discovery is not supported.

## Binding Configuration

To utilize the binding you should first create an account via the Meross Android or iOs app.
Moreover, the devices should be in an online status.

## Bridge Configuration

| Name     | Type | Description                                              | Default                      | Required | Advanced |
|----------|------|----------------------------------------------------------|------------------------------|----------|----------|
| hostname | text | Meross Hostname or IP address (for Europe located users) | <https://iotx-eu.meross.com> | yes      | yes      |
| email    | text | Email of your Meross Account                             | N/A                          | yes      | no       |
| password | text | Password of your Meross Account                          | N/A                          | yes      | no       |

### Other host locations

| Location     | Hostname                     |
|--------------|------------------------------|
| Asia-Pacific | <https://iotx-ap.meross.com> |
| US           | <https://iotx-us.meross.com> |

NOTICE: Due to  **Meross**&reg; security policy please minimize host connections in order to avoid TOO MANY TOKENS (code 1301) error occurs which leads to a  8-10 hours suspension of your account.
Therefore, background device discovery is also disabled.
You will need to manually scan for new devices.

## Thing Configuration

| Parameter | Type | Description                                              | Default | Required | Thing type id                   | Advanced |
|-----------|------|----------------------------------------------------------|---------|----------|---------------------------------|----------|
| name      | text | The name of the device as registered to Meross account   | N/A     | yes      | light, door, tripleDoor         | no       |
| uuid      | text | The device uuid                                          | N/A     | yes      | light, door, tripleDoor         | no       |
| ipAddress | text | The IP address of the device in the local network        | N/A     | yes      | light, door, tripleDoor         | no       |

The unique key to the device is the `uuid` and will be retrieved and set during discovery.
If you wish to use textual thing configuration, get the ID from the discovered thing.

The `ipAddress` will be retrieved during initial configuration with the device.
Once established, it will be used for local device communication.

## Channels

Only power channel is supported:

| Channel    | Type          | Thing type |Read/Write | Description                                                  |
|------------|---------------|------------|-----------|--------------------------------------------------------------|
| power      | Switch        | light      | x         | Power bulb/plug capability to control bulbs and plugs on/off |
| doorState  | Rollershutter | door       | x         | Garage door up/down control                                  |
| doorState0 | Rollershutter | tripleDoor | x         | Garage door up/down control, first door                      |
| doorState1 | Rollershutter | tripleDoor | x         | Garage door up/down control, second door                     |
| doorState2 | Rollershutter | tripleDoor | x         | Garage door up/down control, third door                      |

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
