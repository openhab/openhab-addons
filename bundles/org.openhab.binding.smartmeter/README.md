# SmartMeter Binding

This binding retrieves and reads SML messages (PUSH) and supports IEC 62056-21 modes A,B,C (PULL) and D (PUSH).

## Supported Things

This binding supports only one Thing: `meter`

## Discovery

Discovery is not available, as the binding only reads from serial ports.

## Thing Configuration

The smartmeter thing requires the serial port where the meter device is connected and optionally a refresh interval.

| Parameter             | Name                            | Description                                                                                                                                                                                   | Required | Default |
| --------------------- | ------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- | ------- |
| `port`                | The serial port to connect to   | URL to use for reading SML data, e.g. `/dev/ttyUSB0`, `rfc2217://xxx.xxx.xxx.xxx:3002`                                                                                                        | yes      |         |
| `refresh`             | The refresh interval in seconds | Defines at which interval the values of the meter device shall be read                                                                                                                        | no       | 20      |
| `mode`                | The protocol mode to use        | Can be `SML` (PUSH mode), `ABC` (PULL) or `D` (PUSH)                                                                                                                                          | no       | `SML`   |
| `baudrateChangeDelay` | Delay of baudrate change in ms  | USB to serial converters often require a delay of up to 250ms after the ACK before changing baudrate (only relevant for 'C' mode)                                                             | no       | 0       |
| `baudrate`            | (initial) Baudrate              | The baudrate of the serial port. If set to `AUTO`, it will be negotiated with the meter. The default is `300` baud for modes A, B, and C and `2400` baud for mode D, and `9600` baud for SML. | no       | `AUTO`  |

## Channels

All available OBIS codes which are read out from the device are created as channels.
At every read out the channels are synchronized with the OBIS codes from the device.

Following conversion from OBIS codes to channel ID is done:
`.` is replaced by `-` and `:` or `*` is replaced by `_`.

e.g.

| OBIS code   | Channel ID  |
| ----------- | ----------- |
| `1-0:1.8.1` | `1-0_1-8-1` |
| `1.8.0*00`  | `1-8-0_00`  |

### Channel Configuration

**negate:** Energy meters often provide absolute values and provide information about the _energy direction_ in a separate bit.
With this config you can specify the channel where this bit is located, the bit position and the bits value which shall be set.

`<negate> ::= <CHANNEL_ID>:<BIT_POSITION>:<BIT_VALUE>[:status]`

e.g.:

```text
"1-0_1-8-0:5:1:status" // negate if status(1-0_1-8-0) and 2^5 = 1
"1-0_96-5-5:5:1" // negate if 1-0#96-5-5 and 2^5 = 1
```

## Unit Conversion

Please use the [Units Of Measurement](https://www.openhab.org/docs/concepts/units-of-measurement.html) concept of openHAB for unit conversion which is fully supported by this binding.
Please see the item example on how to use it.
_NOTE:_ your meter device needs to provide correct unit information to work properly.

## Full Example

Things:

```java
smartmeter:meter:heating  [ port="COM1", refresh=10 ]
smartmeter:meter:house [ port="rfc2217://xxx.xxx.xxx.xxx:3002" ]

smartmeter:meter:BinderPower     [port="/dev/ttyUSB0", refresh=5] {
    Channels:
        Type 1-0_1-8-0 : 1-0_1-8-0
        Type 1-0_16-7-0 : 1-0_16-7-0 [
            negate="1-0_1-8-0:5:1:status"
        ]
}

```

Items:

```java
Number:Energy HeatingTarif1        "Heating high price tariff [%.2f kWh]"      { channel="smartmeter:meter:heating:1-0_1-8-1" }
Number:Energy HeatingTarif2        "Heating low price tariff [%.2f kWh]"       {  channel="smartmeter:meter:heating:1-0_1-8-2" }

Number:Energy HouseTarif           "Tariff [%.2f kWh]"                         { channel="smartmeter:meter:house:1-0_1-8-0" }

Number:Power HeatingActualUsage   "Heating Current usage [%.2f %unit%]"       { channel="smartmeter:meter:heating:1-0_16-7-0" }
Number:Power HouseActualUsage     "Current usage [%.2f %unit%]"               { channel="smartmeter:meter:house:1-0_16-7-0" }
```

## Known Limitations

- Octet encoding for OBIS Codes
  - '129-129:199.130.5'
    - '1-0:0.0.9'
        doesn't work properly.

Any help/contribution is appreciated!

## Tested Hardware

The binding has been successfully tested with below hardware configuration:

### SML PUSH mode

- EMH EDL300 meter connected the IR-Reader USB from open hardware project in volkszaehler
- EMH eHZ-IW8E2A
- ISKRA MT681
- EMH eHZ-K

### IEC 62056-21 Mode C

- Apator EC3 with IR-Reader from volkszaehler
- Landis+Gyr E650 with IR-Reader from volkszaehler

### IEC 62056-21 Mode D

- Hager EHZ 361Z5 and EHZ 161L5
