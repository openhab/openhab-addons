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

## AWS Command Protocol Research

The Android app is [SALUS Premium Lite (`com.salus.eu.smarthome`)](https://play.google.com/store/apps/details?id=com.salus.eu.smarthome).
The [official Salus setup guide](https://saluscontrols.com/wp-content/uploads/2024/05/SALUS-Premium-Lite-setup-guide_EN-2.pdf) provides a [direct APK](https://app-release-salus-euprod.s3.eu-central-1.amazonaws.com/EU_0.70.5_330.apk).
The APK inspected on 2026-10-19 is version **0.70.5**, build **330**; this is the vendor's downloadable reference version, not a claim about the latest Play Store release.
Its SHA-256 is `517ab9f203f0a05e38dd9ed10f1967b7a828dab171d2f87a95849743c637a368`.

To reproduce the extraction:

```bash
curl -fL -o salus.apk https://app-release-salus-euprod.s3.eu-central-1.amazonaws.com/EU_0.70.5_330.apk
apktool d salus.apk -o salus-decoded
strings salus-decoded/lib/arm64-v8a/libapp.so > salus-strings.txt
```

The APK is a Flutter application: `apktool` decodes the Android wrapper and assets, while the Dart application logic resides in `libapp.so`.
The native strings contain `sIT600TH` setpoint and mode command identifiers, `publishHoldType`, `publishSetLockKey`, and AWS shadow topic names.
Payload construction was cross-checked against the vendor's [web application](https://eu.premium.salusconnect.io/), using [its JavaScript bundle](https://eu.premium.salusconnect.io/main.dart.97cd10b6.js) fetched on the same date.
The bundle's SHA-256 is `97cd10b621cea0015e0d7fc49c712dd7cdeaff498e6d73d953642094647141ba`.
This validates the payload against vendor code without claiming that native Dart source was recovered with `apktool`.

### Verified Control Path

The web client's `A.fP.prototype.kT` and `fj` construct `state.desired.<baseKey>.properties`.
Its MQTT publisher uses `$aws/things/<deviceCode>/shadow/update`.
For the IT600 device model supported by this binding, the base key is `11`.
The IT600 model (`A.cRb.prototype.bSW` in the inspected bundle) constructs heating or cooling setpoint commands as integers in hundredths of a degree Celsius.
For example:

```json
{
  "state": {
    "desired": {
      "11": {
        "properties": {
          "ep9:sIT600TH:SetHeatingSetpoint_x100": 2150
        }
      }
    }
  }
}
```

The binding sends this document through [AWS UpdateThingShadow](https://docs.aws.amazon.com/iot/latest/developerguide/device-shadow-rest-api.html#API_UpdateThingShadow) using `POST /things/<dsn>/shadow`.
AWS documents this as the HTTPS equivalent of a shadow update, authenticated with Signature V4 and the `iotdevicegateway` service.
The signature includes the actual host, HTTP method, path, session token, and payload hash.
Only the requested desired property is sent; reported state and unrelated desired properties are not overwritten.
The response must acknowledge the requested desired property; device application is confirmed by subsequent reported-state polling.
Salus account permissions for HTTPS writes and physical device behavior still require testing with a real account/device.

Command properties may be absent from reported state.
The mapper therefore exposes the four verified IT600 commands alongside their numeric readbacks, preserving endpoint prefixes and existing channel identifiers.
It does not infer that every reported property is writable, or treat absence from a transient response as a permanent capability decision.

### Further Opportunities

The APK and web client also contain identifiers for frost setpoints, temperature calibration, setpoint limits, heating/cooling system mode, display settings, and valve protection.
These are candidates for later model-specific support; identifiers alone do not establish valid ranges, prerequisites, or interactions.
Schedules use additional upload/URL and schedule-enable workflows and require separate investigation.
Live AWS MQTT subscriptions could reduce polling latency, but would introduce connection and subscription lifecycle work.
This change implements heating/cooling setpoints, hold mode, and keypad lock only.
