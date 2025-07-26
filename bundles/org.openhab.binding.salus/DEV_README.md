# ReverseEngineerProtocol CLI Documentation

This documentation provides instructions on how to use the ReverseEngineerProtocol CLI program to reverse engineer the proprietary Salus protocol.

## How to Run

To execute the CLI program, run the `main` method from the `ReverseEngineerProtocol` class. You can either run it directly from an IDE or use the `java` command. The program requires three parameters: `email`, `password`, and the Salus backend type (`AwsSalusApi` or `HttpSalusApi`).

### Running from an IDE

1. Open the project in your IDE.
1. Navigate to the `ReverseEngineerProtocol` class.
1. Run the `main` method, passing in the required parameters.

### Running from the Command Line

```bash
java -cp <your-compiled-class-path> ReverseEngineerProtocol <email> <password> <backendType>
```

Replace `<your-compiled-class-path>` with the path to your compiled classes, and `<email>`, `<password>`, and `<backendType>` with your actual credentials and backend type.

## Methods

### `findDevices`

Finds and lists all devices associated with your Salus cloud account.

**Usage:**

```bash
./ReverseEngineerProtocol <email> <password> <backendType> findDevices
```

### `findDeviceProperties <dsn>`

Retrieves all properties for the device with the given Device Serial Number (DSN).

**Parameters:**

- `<dsn>`: The Device Serial Number of the target device.

**Usage:**

```bash
./ReverseEngineerProtocol <email> <password> <backendType> findDeviceProperties <dsn>
```

### `findDeltaInProperties <dsn>`

Initializes by loading all properties from the given device, then filters out the properties that have changed or remained unchanged. This method is useful for identifying which property corresponds to a specific value or state.

**Parameters:**

- `<dsn>`: The Device Serial Number of the target device.

**Example Use Case:**

To find which property stores the "running" state of a device:

1. Run `findDeltaInProperties <dsn>`.
1. Filter out properties that have changed (this can be done multiple times).
1. Trigger the device to change state (e.g., set the temperature higher than the current one to make the device run).
1. Filter out properties that have not changed.
1. Repeat steps 2-4 until the desired property is identified.

**Usage:**

```bash
./ReverseEngineerProtocol <email> <password> <backendType> findDeltaInProperties <dsn>
```

### `monitorProperty <dsn> <propertyName> <sleep>`

Monitors and retrieves the value of a specific property from a given device at specified intervals.

**Parameters:**

- `<dsn>`: The Device Serial Number of the target device.
- `<propertyName>`: The name of the property to monitor.
- `<sleep>`: (optional; default 1) The sleep interval (in seconds) between each check.

**Usage:**

```bash
./ReverseEngineerProtocol <email> <password> <backendType> monitorProperty <dsn> <propertyName> <sleep>
```
