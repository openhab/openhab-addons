# Z-Wave JS Binding

The `zwavejs` binding integrates Z-Wave JS with openHAB, allowing you to control and monitor Z-Wave devices using the Z-Wave JS Webservice (part of Z-Wave JS UI).
This binding supports a wide range of Z-Wave devices, including sensors, switches, dimmers, and more.
For documentation about Z-Wave JS UI, please visit the [Z-Wave JS UI documentation](https://zwave-js.github.io/zwave-js-ui/).

## Prerequisites

Before using the `zwavejs` binding, ensure the following prerequisites are met:

- You have a running instance of [Z-Wave JS](https://zwave-js.github.io/zwave-js-ui/).
- Your Z-Wave controller is properly connected and recognized by Z-Wave JS.
- The Z-Wave JS instance has the Webservice enabled (Settings -> Home Assistant -> WS Server).
- Network connectivity exists between openHAB and the Z-Wave JS Webservice (the hostname and port must be reachable from openHAB).

**Note**: If you are transitioning from the native Z-Wave binding to the `zwavejs` binding, you can safely test this binding without permanently affecting your setup. All node information will remain on your controller as usual. However, please avoid performing a manual reset unless absolutely necessary, as it may complicate reverting to the native binding.

## Supported Things

This binding supports the following types of things:

- `gateway`: Represents the Z-Wave JS Webservice bridge. This is required to communicate with the Z-Wave network.
- `node`: Represents a Z-Wave device (node) in the network. Each node can have multiple channels corresponding to its capabilities.

**Note**: This binding does not maintain a Z-Wave device database and relies on the external Z-Wave JS project for device compatibility and functionality.

## Discovery

The `zwavejs` binding supports auto-discovery of Z-Wave devices.
When the bridge is added and connected, it will automatically discover and add Z-Wave nodes to the openHAB system.
The following discovery features are available:

- Automatic discovery of Z-Wave nodes when they are added to the network.
- Automatic update of node information when the node is updated in the Z-Wave network.

## Bridge Configuration

The `zwavejs` binding requires configuration of the bridge to connect to the Z-Wave JS Webservice.
The configuration options include:

| Name                  | Type    | Description                                          | Default  | Required | Advanced |
|-----------------------|---------|------------------------------------------------------|----------|----------|----------|
| hostname              | text    | Hostname or IP address of the server                 | N/A      | yes      | no       |
| port                  | number  | Port number to access the service                    | 3000     | yes      | no       |
| maxMessageSize        | number  | Maximum size of messages in bytes                    | 16777216 | no       | yes      |
| configurationChannels | boolean | Expose the command class 'configuration' as channels | false    | no       | yes      |

## Thing Configuration

Each Z-Wave node can have its own configuration options.
All configuration parameters are set by the binding during startup and are read-only.
Only the advanced parameter `id` can (optionally) be set in the Thing configuration in the openHAB UI or in the `.things` file.

## Channels

Z-Wave nodes can have multiple channels corresponding to their capabilities.
The channels can be linked to items in openHAB to control and monitor the device.
These channels are dynamically added to the Thing during node initialization; therefore, there is no list of possible channels in this documentation.

## Full Example

### `demo.things` Example

```java
Bridge zwavejs:gateway:myBridge "Z-Wave JS Bridge" [ hostname="localhost", port=3000 ] {
    Thing node node1 "Z-Wave Node 1" [ id=1 ]
    Thing node node2 "Z-Wave Node 2" [ id=2 ]
}
```

### `demo.items` Example

```java
Switch LightSwitch "Light Switch" { channel="zwavejs:node:myBridge:node1:binary-switch-value" }
```

## Troubleshooting

If you encounter issues with the `zwavejs` binding, check the following:

- Ensure the Z-Wave JS Webservice is running and accessible at the configured hostname and port.
- Check the openHAB logs for any error messages related to the `zwavejs` binding.
- Verify the configuration of the bridge and nodes in the openHAB UI or configuration files.

For further assistance, refer to the openHAB community forums or the Z-Wave JS documentation.

## Resources

- [Z-Wave JS Documentation](https://zwave-js.github.io/node-zwave-js/)
- [openHAB Documentation](https://www.openhab.org/docs/)
- [openHAB Community](https://community.openhab.org/)
