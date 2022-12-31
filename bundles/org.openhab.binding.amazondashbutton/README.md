# Amazon Dash Button Binding

The [Amazon Dash Button](https://www.amazon.com/Dash-Buttons/b?node=10667898011) is a cheap and small Wi-Fi connected device to order products from Amazon with the simple press of a button.
This Binding allows you to integrate Dash Buttons into your home automation setup.

The Binding code is inspired by [hortinstein/node-dash-button](https://github.com/hortinstein/node-dash-button).

**Warning:**
The Dash Button will try to contact the Amazon servers every time the button is pressed.
This might not be in line with your privacy preferences but can be prevented.
Please refer to the ["Preventing Communication with Amazon Servers"](#preventing-communication-with-amazon-servers) section for details.

**Response Time:**
Please be aware, that due to the operation method of this binding, the response time for a button press can be rather high (up to five seconds).
You might want to keep that in mind during product selection or task assignment.

## Prerequisites

The Binding uses [Pcap4J](https://www.pcap4j.org/) in order to capture `ARP` and `BOOTP` requests send by the Amazon Dash Button.
Buttons will hence only be usable within the same network as your openHAB instance.

Start with installing libpcap (for Mac/Linux/Unix) or WinPcap (for Windows) on your computer.
They are native libraries that power the core functionalities of Pcap4J.

**Note:**
Pcap4J needs administrator/root privileges.
Instructions for Debian/Ubuntu are given below.

### Installing libpcap on Debian/Ubuntu

Installing [libpcap](https://www.tcpdump.org/) should be as simple as:

```shell
sudo apt-get install libpcap-dev
```

You can run Pcap4J with a non-root openHAB user by granting capabilities `CAP_NET_RAW` and `CAP_NET_ADMIN` to the openHAB java environment by the following command:

```shell
sudo setcap cap_net_raw,cap_net_admin=eip $(realpath /usr/bin/java)
```

Be aware of other capabilities which were previously set by setcap.
**These capabilities will be overwritten!**
You can see which capabilities have already been set with the command:

```shell
sudo getcap $(realpath /usr/bin/java)
```

If you need multiple capabilities (like "cap_net_bind_service" for the Network binding), you have to add them like this:

```shell
sudo setcap 'cap_net_raw,cap_net_admin=+eip cap_net_bind_service=+ep' $(realpath /usr/bin/java)
```

You need to restart openHAB for the capabilities change to take effect.

### Installing WinPcap on Windows

On a Windows system there are two options to go with.

1. The preferred solution is [WinPcap](https://www.winpcap.org) if your network interface is supported.
1. An alternative option is [npcap](https://github.com/nmap/npcap) with the settings "WinPcap 4.1.3 compatibility" and "Raw 802.11 Packet Capture"

### Installing libpcap on Other Operating Systems

The installation methods might differ.
A few known operating systems are:

| Operating System | Command                     |
|:-----------------|:----------------------------|
| CentOS           | `yum install libpcap-devel` |
| Mac              | `brew install libpcap`      |

## Setup Dash Button

Amazon itself doesn't support Dash Buttons anymore.
Instructions how to use them without having to rely on Amazon's servers can be found at [https://blog.christophermullins.com/2019/12/20/rescue-your-amazon-dash-buttons/](https://blog.christophermullins.com/2019/12/20/rescue-your-amazon-dash-buttons/) - at least for some firmware versions.
Take care to block internet access for the button or it will be bricked.

## Preventing Communication with Amazon Servers

Every time a Dash Button is pressed a request will be sent to the Amazon servers.
If no product was configured for the Button, a notification will be presented by the Amazon app on your smartphone.

To prevent the Dash Button from contacting the Amazon Servers, block Internet access for the device.
Please refer to the documentation of your network's router for details.
If your network doesn't provide that option, you can at least deal with the notifications by either uninstalling the Amazon app or disabling notifications for it (possible on most smartphone OSs).

It has shown that blocking the Dash Button communication with the Amazon servers will provoke reconnection attempts.
This increased amount of communication causes a reduced overall battery life.
The built-in AAA battery can be easily replaced.

Preventing the communication with the Amazon servers or the Amazon app is **not** necessary to integrate the Dash Button in openHAB.

## Supported Things

There is one supported Thing, the "Amazon Dash Button".

## Discovery

Background discovery is not supported as it is not possible to distinguish
between a Dash Button and other Amazon devices like the Kindle,
a Fire TV or an Echo speaker.

You can start the discovery process for Dash Button devices manually.
While openHAB is in the scanning process, press the button on the Dash to be recognized and added to your Inbox.

**Caution:**
You have to be aware that other Amazon devices might pop up in your Inbox if they send an `ARP` request while scanning for Dash Buttons.
You can ignore these devices in your Inbox.

## Thing Configuration

### Amazon Dash Button

- `macAddress` - The MAC address of the Amazon Dash Button.

- `pcapNetworkInterfaceName` - The network interface which receives the packets of the Amazon Dash Button.

- `packetInterval` - Often a single button press is recognized multiple times.
    You can specify how long any further detected button pressed should be ignored after one click was processed.
    The parameter is optional and 5000ms by default.

For manual definition of a `dashbutton` Thing the MAC address can either be taken from the discovery output or can e.g. be captured through your router/DHCP frontend or with [Wireshark](https://wireshark.org).

## Channels

- **press:** Trigger channel for recognizing presses on the Amazon Dash Button.
A trigger channel can directly be used in a rule, check the "Full Example" section for one example.
Dispatches a `PRESSED` event when a button is pressed.

The trigger channel `press` is of type `system.rawbutton` to allow the usage of the `rawbutton-toggle-switch` profile.

## Full Example

Things:

```java
Thing amazondashbutton:dashbutton:fc-a6-67-0c-aa-c7 "My Dash Button" @ "Living" [ macAddress="fc:a6:67:0c:aa:c7", pcapNetworkInterfaceName="eth0", packetInterval=5000 ]
```

(Pay attention: The MAC address has to be given in two different formats)

Rules:

```java
rule "My Dash Button pressed"
when
    Channel "amazondashbutton:dashbutton:fc-a6-67-0c-aa-c7:press" triggered
then
    logInfo("amazondashbutton", "My Dash Button has been pressed")
end
```
