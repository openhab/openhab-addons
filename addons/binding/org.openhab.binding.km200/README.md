# KM200 Binding

The KM200 Binding is communicating with a [Buderus Logamatic web KM200 / KM100 / KM50](https://www.buderus.de/de/produkte/catalogue/alle-produkte/7719_gateway-logamatic-web-km200-km100-km50).
It is possible to receive and send parameters like string or float values.

**Important**: If the communication is not working and you see in the logfile errors like "illegal key size" then you have to change the [Java Cryptography Extension to the Unlimited Strength Jurisdiction](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html). 

## Supported Things

### KM 50/100/200

This Binding is tested on a KM200 but it should works on KM50 and KM100, too.

### Discovery and services

This binding is discovering the devices with a IP search method in the local network (may last up to 2 minutes). Only /24 networks are supported. After adding of the dicovered device to the things the configuration of the device has to be completed (In the settings of this thing). 

### Configuration

* IP address of KN200 to connect to

ip4_address=192.168.XXX.XXX (usually auto-detected)
`
* There a two ways of de-/encryption password handling:

1.  With the finished private key, here is this one required:

PrivKey=0000FFFFEEEEDDDDCCCCBBBBAAAA999988887777666655554444333322221111


2.  --OR-- the binding is creating the key from the md5salt, the device and the private password. Here are all three required:

MD5Salt=111122223333444455556666777788889999aaaabbbbccccddddeeeeffff0000

GatewayPassword=AAAABBBBCCCCDDDD

PrivatePassword=MYPASSWORD

### Services

After the configuration is done the device needs up to 2 minutes to goes online. If this happen then it's possible to search for things in the inbox. This binding is creating things and channels depending on the connected heating system full automatically.
It is not possible to add things or channels manually. If you cannot connect the channels to items then enable the simple mode in OH2 configuration. There seems to be somewhere a problem in OH2 in handling of automatically created channels.


