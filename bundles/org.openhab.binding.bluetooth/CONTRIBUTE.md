# Bluetooth Binding overview

The Bluetooth binding is implemented to allow bundles to extend the main Bluetooth bundle (this one) in order to add new Bluetooth adapter as well as device support.
This architecture means that such extension bundles must utilise the binding name `bluetooth`.

A base class structure is defined in the `org.openhab.binding.bluetooth` bundle.
This includes the main classes required to implement Bluetooth:

- `BluetoothAdapter`. This interface defines the main functionality required to be implemented by a Bluetooth adapter, including device discovery. Typically, this interface is implemented by a BridgeHandler and then registered as an OSGi service
- `BluetoothDiscoveryParticipant`. An interface to be implemented by services that can identify specific Bluetooth devices.
- `BluetoothDevice`. This implements a Bluetooth device. It manages the notifications of device notifications, Bluetooth service and characteristic management, and provides the main interface to communicate to a Bluetooth device.
- `BluetoothService`. Implements the Bluetooth service. A service holds a number of characteristics.
- `BluetoothCharacteristic`. Implements the Bluetooth characteristic. This is the basic component for communicating data to and from a Bluetooth device.
- `BluetoothDescriptor`. Implements the Bluetooth descriptors for each characteristic.

## Implementing a new Bluetooth Adapter bundle

Bluetooth adapters are modelled as a bridge in openHAB.
The bridge handler provides the link with the Bluetooth hardware (eg a dongle, or system Bluetooth API).
An adapter bundle needs to implement two main classes: the `BridgeHandler` which should implement `BluetoothAdapter` (any be registered as a service), and a `ThingFactory`, which is required to instantiate the handler.

The bridge handler must implement any functionality required to interface to the Bluetooth layer.
It is responsible for managing the Bluetooth scanning, device discovery (i.e. the device interrogation to get the list of services and characteristics) and reading and writing of characteristics.
The bridge needs to manage any interaction between the interface with any things it provides – this needs to account for any constraints that an interface may impose such that things do not need to worry about any peculiarities imposed by a specific interface.

Classes such as `BluetoothCharacteristic` or `BluetoothService` may be extended to provide additional functionality to interface to a specific library if needed.

## Implementing specific Bluetooth device support

A specific Bluetooth thing handler provides the functionality required to interact with a specific Bluetooth device.
The new thing bundle needs to implement three main classes – a `BluetoothDiscoveryParticipant`, a `ThingHandler` and a `ThingFactory`, which is required to instantiate the handler.

Two fundamental communications methods can be employed in Bluetooth: beacons and connected mode. A Bluetooth thing handler can implement one or both of these communications
 In practice, a connected mode Thing implementation would normally handle the beacons in order to provide as a minimum the RSSI data.

### Thing Naming

To avoid naming conflicts with different Bluetooth bundles a strict naming policy for things and thing xml files is proposed.
This should use the bundle name and the thing name, separated with an underscore - e.g. for a Yeelight binding Blue2 thing, the thing type would be `yeelight_blue2`.

### Connected Mode Implementation

The connected mode `BluetoothThingHandler` needs to handle the following functionality

- Extend the connected bluetooth thing handler. This holds the `adapter` through which all communication is done.
- Call the `adapter.getDevice()` method to get the `BluetoothDevice` class for the requested device. The `getDevice()` method will return a `BluetoothDevice` class even if the device is not currently known.
- Implement the `BluetoothDeviceListener` methods. These provide callbacks for various notifications regarding device updates – e.g. when the connection state of a device changes, when the device discovery is complete, when a read and write completes, and when beacon messages are received.
- The parent class calls the `device.connect()` method to connect to the device. Once the device is connected, the `BluetoothDeviceListener.onConnectionStateChange()` callback will be called.
- The parent class  calls the `device.discoverServices()` method to discover all the BluetoothServices and `BluetoothCharacteristic`s implemented by the device. Once this is complete, the `BluetoothDeviceListener.onServicesDiscovered()` callback will be called.
- Call the `readCharacteristic` or `writeCharacteristic` methods to interact with the device. The `BluetoothDeviceListener.onCharacteristicReadComplete()` and `BluetoothDeviceListener.onCharacteristicWriteComplete()` methods will be called on completion.
- Implement the `BluetoothDeviceListener.onCharacteristicUpdate()` method to process any read responses or unsolicited updates of a characteristic value.

### Beacon Mode Implementation

The beacon mode thing handler needs to handle the following functionality:

- Extend the beacon Bluetooth thing handler. This holds the `adapter` through which all communication is done.
- Call the `adapter.getDevice()` method to get the `BluetoothDevice` class for the requested device. The `getDevice()` method will return a `BluetoothDevice` class even if the device is not currently known.
- Implement the `BluetoothDeviceListener.onScanRecordReceived()` method to process the beacons. The notification will provide the current receive signal strength (RSSI), the raw beacon data, and various elements of generally useful beacon data is provided separately.

### Generic Bluetooth Device Support

The core Bluetooth binding already includes generic "beacon" and "connected" Bluetooth thing types.
All devices for which no discovery participant defines a specific thing type are added to the inbox as a beacon device.
The corresponding handler implementation (`BeaconBluetoothHandler`) uses Beacon mode and merely defines a channel for RSSI for such devices.

The "connected" thing type can be used by manually defining a thing.
The corresponding handler implementation (`ConnectedBluetoothHandler`) uses Connected mode and thus immediately connects to the device and reads its services.
Common services are added as channels (t.b.d.).
