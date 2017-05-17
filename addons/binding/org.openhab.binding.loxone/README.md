# Loxone Binding

This binding integrates [Loxone Miniserver](https://www.loxone.com/enen/products/miniserver-extensions/) with [OpenHAB](http://www.openhab.org/). Miniserver is represented as a [Thing](http://docs.openhab.org/configuration/things.html). Miniserver controls, that are visible in the Loxone [UI](https://www.loxone.com/enen/kb/user-interface-configuration/), are exposed as OpenHAB channels.

Binding has the Loxone-specific code separated in a .core package. This code does not depend on the openHAB framework and can be easily used to handle Loxone Miniservers in other Java applications.

## Features
Following features are currently supported:
* [Discovery](https://en.wikipedia.org/wiki/Simple_Service_Discovery_Protocol) of Miniservers available on the local network
* Creation of channels for Loxone controls that are exposed in the Loxone [UI](https://www.loxone.com/enen/kb/user-interface-configuration/)
* Tagging of channels and [items](http://docs.openhab.org/configuration/items.html) with tags that can be recognized by [Alexa](https://en.wikipedia.org/wiki/Amazon_Alexa) openHAB [skill](https://www.amazon.com/openHAB-Foundation/dp/B01MTY7Z5L), so voice can be used to command Loxone controls
* Management of a Websocket connection to the Miniserver and updating Thing status accordingly
* Updates of OpenHAB channel's state in runtime according to control's state changes on the Miniserver
* Passing channel commands to the Miniserver's controls

## Supported Things

This binding supports [Loxone Miniservers](https://www.loxone.com/enen/products/miniserver-extensions/) for accessing controls that are configured in their UI.

Thing ID is defined in the following way: `loxone:miniserver:<serial>`, where <serial> is a serial number of the Miniserver (effectively this is the MAC address of its network interface).

## Discovery

[Loxone Miniservers](https://www.loxone.com/enen/products/miniserver-extensions/) are automatically discovered by the binding and put in the Inbox. [Discovery](https://en.wikipedia.org/wiki/Simple_Service_Discovery_Protocol) is performed using [UPnP](https://en.wikipedia.org/wiki/Universal_Plug_and_Play) protocol.

Before a Miniserver Thing can go online, it must be configured with a user name and a password of an account available on the Miniserver. Please set them manually in Thing configuration after you add a new Miniserver Thing from your Inbox.

## Channels

This binding creates channels for controls that are [used in Loxone's user interface](https://www.loxone.com/enen/kb/user-interface-configuration/).

The following control types are currently supported. Please consult [API](https://www.loxone.com/enen/kb/api/) structure documentation to understand how controls map onto objects from [Loxone Config](https://www.loxone.com/enen/kb-cat/loxone-config/).

* Switch ([Virtual inputs](https://www.loxone.com/enen/kb/virtual-inputs-outputs/) of switch type and [Push-button](https://www.loxone.com/enen/kb/push-button/) functional blocks)
* Pushbutton (virtual inputs of push button type)
* Rollershutter (Blinds, Automatic Blinds, Automatic Blinds Integrated)
* InfoOnlyDigital (Digital virtual inputs)
* InfoOnlyAnalog (Analog virtual inputs)

If your control is supported, but binding does not recognize it, please check if it is exposed in Loxone UI using [Loxone Config](https://www.loxone.com/enen/kb-cat/loxone-config/). application.

Each channel has a dedicated channel type dynamically created. This is done in order to provide a proper look and feel of features that are realized by the controls, rather than their control types. Channel type's label and channel's label are derived from control's name.

Channel type ID is defined in the following way: `loxone:miniserver:<serial>:<control-type>:<control-UUID>`.

Channel ID is defined in the following way: `loxone:miniserver:<serial>:<control-UUID>`.


### Loxone and Amazon Alexa

Your OpenHAB server can be exposed through [myopenHAB](http://www.myopenhab.org/) cloud service to  [Amazon Alexa](https://en.wikipedia.org/wiki/Amazon_Alexa) device with [openHAB skill](https://www.amazon.com/openHAB-Foundation/dp/B01MTY7Z5L) enabled. To enable this service, please consult instructions available [here](https://community.openhab.org/t/official-alexa-smart-home-skill-for-openhab-2/23533).

When creating a Miniserver Thing in the openHAB's Item Linking Simple Mode, Loxone binding will automatically create item tags required by Alexa, so that Miniserver's controls can be discovered by [Alexa smart home]( https://www.amazon.com/alexasmarthome) module. Tags will be created for switches, which belong to a category of "lighting" type. This will allow you to command Loxone controls with your voice.

Alexa will recognize items by their labels, which will be equal to the corresponding control's name on the Miniserver. In case your controls are named in a way not directly suiting voice commands, you will need to manually add and link new items to the channels, and add proper tags as described in the above instructions.

Please consult [tutorial](http://docs.openhab.org/tutorials/beginner/configuration.html) on what simple mode is and how to enable it.

## Limitations
* As there is no push button item type in OpenHAB, Loxone's push button is an OpenHAB's switch, which always generates a short pulse on changing its state to on. If you use simple UI mode and framework generates items for you, switches for push buttons will still be toggle switches. To change it to push button style, you have to create item manually with autoupdate=false parameter.


