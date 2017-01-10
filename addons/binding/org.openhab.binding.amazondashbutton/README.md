# Amazon Dash Button Binding

The Amazon Dash Button is a very cheap device in order to integrate it in your home automation setup.

## Prerequisites

The binding uses pcap4j (https://www.pcap4j.org/) in order to capture ARP and BOOTP requests send by the Amazon Dash Button. The code is inspired by https://github.com/hortinstein/node-dash-button.


### Install Native Library

Let’s start with installing libpcap (for Mac/Linux/UNIX) or WinPcap (for Windows) on your computer. They are native libraries that powers the core functionalities of Pcap4J. Pcap4J needs administrator/root privileges.

Source: [https://www.pcap4j.org/](https://www.pcap4j.org/)

#### Debian/Ubuntu

```shell
apt-get install libpcap-dev
```

Note: You can run Pcap4J with a non-root user by granting capabilities `CAP_NET_RAW` and `CAP_NET_ADMIN`
to your java command by the following command: 

```shell
sudo setcap cap_net_raw,cap_net_admin=eip `realpath /usr/bin/java`
```

Be aware of other capabilities which are set by setcap. **These capabilities will be overwritten!** You can see which capabilities have already been set with the command:

```shell
sudo getcap `realpath /usr/bin/java`
```

If you need mulitple capabilities (like "cap_net_bind_service" for the Network binding), you have to add them like this :

```shell
sudo setcap 'cap_net_raw,cap_net_admin=+eip cap_net_bind_service=+ep' `realpath /usr/bin/java`
```

#### Other Operating Systems

| Operating System | Command                     |
|:-----------------|:----------------------------|
| CentOS           | `yum install libpcap-devel` |
| Mac              | `brew install libpcap`      |
| Windows          | `choco install winpcap`     |


## Setup Dash Button

Setting up your Dash button is as simple as following the instructions provided by Amazon **EXCEPT FOR THE LAST STEP**. Just follow the instructions to set it up in their mobile app. When you get to the step where it asks you to pick which product you want to map it to, just quit the setup process.

## Block Internet access for the Dash Button

Completely deny internet access for the Amazaon Dash Button in your router. You need to find out the Dash button's IP address first of all. This is not explained as this job depends on your environment.


## Supported Things

There is one supported thing:

* Amazon Dash Button: Thing 

## Discovery

Background discovery is not supported as it is not possible to distinguish Dash buttons and other Amazon devices like Kindle, Fire TV or Echo.
You can start the discovery process explicitly for Dash button devices. While scanning just press the button in order to put it into your inbox.

__ Caution:__  You have to be aware that other Amazon devices might pop up in your inbox if they send an ARP request while scanning for dash buttons. You can ignore that devices in your inbox.

## Thing Configuration

### Amazon Dash Button

* MAC address: The MAC address of the Amazon Dash Button
* Network interface: The network interface which receives the packets of the Amazon Dash Button
* Packet processing interval: Often a single button press is recognized multiple times. You can specify how long any further detected button pressed should be ignored after one click is handled (in ms).

## Channels

* Press: Trigger channel for recognizing presses on the Amazon Dash Button. You do not have to link this channel to an item. Just reference the channel in your .rules-file like documented in the 'Example usage' section.

## Example usage

```
rule "Dash button pressed"
    when
        Channel "amazondashbutton:dashbutton:ac-63-be-xx-xx-xx:press" triggered
    then
        println("The Dash button has been pressed")
end
```
