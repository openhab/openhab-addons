# Meross Binding

This binding integrates **Meross**&reg; devices

![](doc/image.png)

Supported capabilities:

* `togglex` Power bulb/plug capability

## Supported things

The binding supports one thing type, Light:

 Meross Name         | Type   | Description         | Supported | Tested 
|---------------------|--------|---------------------|-----------|--------|
| Smart ambient light | msl430 | Smart ambient light | yes       | yes    |
| Smart plug          | mss210 | Smart plug          | yes       | yes    |

Anyway the binding by supporting the togglex capability may consent to control on/off of the majority of plugs and bulbs

## Discovery

The Discovery service is supported through the scan capability.

if a refresh of the devices is needed, e.g. to fetch new devices please disable  and re-enable the bridge via the interface button 

## Binding Configuration

To utilize the binding you should first create an account via the **Meross**&reg; **Android**&reg; app. Moreover, the devices should be in an
online status

## Bridge Configuration

| Name     | Type | Description                                              | Default                    | Required | Advanced |
|----------|------|----------------------------------------------------------|----------------------------|----------|----------|
| hostname | text | Meross Hostname or IP address (for Europe located users) | https://iotx-eu.meross.com | yes      | yes      |
| email    | text | Email of your Meross Account                             | N/A                        | yes      | no       |
| password | text | Password of your Meross Account                          | N/A                        | yes      | no       |


NOTICE: Due to  **Meross**&reg; security policy please minimize host connections in order to avoid TOO MANY TOKENS  (code 1301) error occurs
which leads to a  8-10 hours suspension of your account. Hence, configure the bridge once and leave it! 

## Thing Configuration

| Name      | Type | Description                                             | Default | Required | Advanced |
|-----------|------|---------------------------------------------------------|---------|----------|----------|
| lightName | text | The name of the light as registered to Meross account   | N/A     | yes      | no       |


## Channels

Only togglex channel is supported:

| Channel | Type   | Read/Write | Description                      |
|---------|--------|------------|----------------------------------|
| togglex | Switch | N/A        | controls  bulbs and plugs on/off |

NOTICE: Due to  **Meross**&reg; security policy  please limit  communication to no more than 150 messages every one hour at
the earliest convenience otherwise, the user is  emailed  by Meross of the limit exceed and if such a behaviour does not change 
the user's  account will be **BANNED**! The inappropriate usage is user's responsibility

NOTICE: Due to the above mentioned security policy  currently is not possible to get the device on/off status  

## Full Example

### meross.things

### Thing Configuration

```java
Bridge meross:bridge:mybridge "Meross bridge" [ hostName="https://iotx-eu.meross.com", userEmail="abcde" userPassword="fghij" ]{
light SC_plug                 "Desk"       [lightName="Desk"]
}
```
### meross.items

### Item Configuration

```java
Switch              iSC_plug                 "Desk"                                    { channel="meross:light:mybridge:SC_plug:togglex" }

```

### Sitemap Configuration

```perl
sitemap meross label="Meross Binding Example Sitemap"
{
    Frame label="Living Room"
    {
          Default item=iSC_plug          icon="light"
    }

}
```

## Contributions

Pull requests are really welcomed

## Disclaimer

This binding is not associated by any means with  **Meross**&reg; or other subsidiaries

## Special thanks

Special thanks to the fantastic OPENHAB community!
