# Meross Binding

This binding integrates **Meross**&reg; devices

## Supported Things

Supported thing types

- `gateway` : Acts as a Bridge to your Meross cloud account.
- `light` : Represents a light device like a Smart ambient light.

|   Meross Name       | Type   | Description         | Supported | Tested|
|---------------------|--------|---------------------|-----------|--------|
| Smart ambient light | msl430 | Smart ambient light | yes       | yes    |
| Smart plug          | mss210 | Smart plug          | yes       | yes    |

## Discovery

The Discovery service is supported.
If a refresh of devices is needed, e.g. to fetch new devices please disable and re-enable the bridge via the interface button.

## Binding Configuration

To utilize the binding you should first create an account via the Meross Android or iOs app.
Moreover, the devices should be in an online status

## Bridge Configuration

| Name     | Type | Description                                              | Default                    | Required | Advanced |
|----------|------|----------------------------------------------------------|----------------------------|----------|----------|
| hostname | text | Meross Hostname or IP address (for Europe located users) | <https://iotx-eu.meross.com> | yes      | yes      |
| email    | text | Email of your Meross Account                             | N/A                        | yes      | no       |
| password | text | Password of your Meross Account                          | N/A                        | yes      | no       |

### Other host locations

| Location     | Hostname                   |
|--------------|----------------------------|
| Asia-Pacific | <https://iotx-ap.meross.com> |
| US           | <https://iotx-us.meross.com> |

NOTICE: Due to  **Meross**&reg; security policy please minimize host connections in order to avoid TOO MANY TOKENS (code 1301) error occurs which leads to a  8-10 hours suspension of your account.

## Thing Configuration

| Parameter | Type | Description                                             | Default | Required | Thing type id | Advanced |
|-----------|------|---------------------------------------------------------|---------|----------|---------------|----------|
| lightName | text | The name of the light as registered to Meross account   | N/A     | yes      | light         | no       |

## Channels

Only power channel is supported:

| Channel | Type   | Read/Write | Description                                                  |
|---------|--------|------------|--------------------------------------------------------------|
| power   | Switch | N/A        | Power bulb/plug capability to control bulbs and plugs on/off |

NOTICE: Due to **Meross**&reg; security policy please limit communication to no more than 150 messages every one hour at the earliest convenience otherwise, the user is emailed by Meross of the limit exceed and if such a behaviour does not change the user's account will be **BANNED**!

The inappropriate usage is user's responsibility

NOTICE: Due to the above mentioned security policy  currently is not possible to get the device on/off status  

## Full Example

### meross.things

```java
Bridge meross:gateway:mybridge "Meross bridge" [ hostName="https://iotx-eu.meross.com", userEmail="abcde" userPassword="fghij" ] {
    light SC_plug                 "Desk"       [lightName="Desk"]
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
