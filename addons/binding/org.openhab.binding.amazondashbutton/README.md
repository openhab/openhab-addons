# Amazon Dash Button Binding

The Amazon Dash Button is a very cheap device in order to integrate it in your home automation setup. In openHAB it can be used as a simple switch.

## Prerequisites
The binding uses pcap4j (https://www.pcap4j.org/) in order to capture ARP requests send by the Amazon Dash Button. The code is inspired by https://github.com/hortinstein/node-dash-button.


### Install Native Library
Let’s start with installing libpcap (for Mac/Linux/UNIX) or WinPcap (for Windows) on your computer. They are native libraries that powers the core functionalities of Pcap4J.
#### Ubuntu
```
apt-get install libpcap-dev
```
#### CentOs
```
yum install libpcap-devel
```
#### Mac
```
brew install libpcap
```

#### Windows
```
choco install winpcap
```

Source: https://www.pcap4j.org/

### Setup Dash Button
Setting up your Dash button is as simple as following the instructions provided by Amazon **EXCEPT FOR THE LAST STEP**. Just follow the instructions to set it up in their mobile app. When you get to the step where it asks you to pick which product you want to map it to, just quit the setup process.

### Block Internet access for the Dash Button
Completely deny internet access for the Amazaon Dash Button in your router. You need to find out the Dash button's IP address first of all. This is not explained as this job depends on your environment.


## Supported Things
There is one supported thing:
* Amazon Dash Button: Thing 

## Discovery
Background discovery is not supported.
You have to scan for new Amazon Dash Buttons. While Scanning you have to press the button in order to put it in your inbox it.

## Thing Configuration
### Amazon Dash Button:
* MAC address: The MAC address of the Amazon Dash Button
* Network interface: The network interface which receives the packets of the Amazon Dash Button
* Packet processing interval: Often a single button press is recognized multiple times. You can specify how long any further detected button pressed should be ignored after one click is handled (in ms).

## Channels
* Press: Channel for recognizing presses on the Amazon Dash Button
