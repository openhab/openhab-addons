# MeterReader Binding

The meter reader binding is able to read SML messages (PUSH) and supports IEC 62056-21 modes A,B,C (PULL)and D (PUSH).

To decode SML messages, the binding takes advantage of jSML library by Fraunhofer Institute.
Please see following page for more informations (German but with good links):
https://de.wikipedia.org/wiki/Smart_Message_Language

To support IEC 62056-21 protocol the binding uses [j62056 library](https://www.openmuc.org/iec-62056-21/)


## Supported Things

This binding supports only one Thing: `meter`

## Discovery

Discovery is not available, as the binding only reads from serial ports.

## Binding Configuration

No binding configuration required.

## Thing Configuration

The MeterReader thing requires the serial port where the meter device is connected and optionally an refresh intervall.

| Parameter | Name | Description | Required | Default |
|-----------|------|-------------|----------|---------|
| `port` | The serial port to connect to| URL to use for playing notification sounds, e.g. `/dev/ttyUSB0` | yes | |
| `refresh` | The refresh interval in seconds | Defines at which interval the values of the meter device shall be read | no | 20 |
| `mode` | The protocol mode to use | Can be 'SML' (PUSH mode), 'ABC' (PULL)or D (PUSH) | no | SML |
| `baudrateChangeDelay` | Delay of baudrate change in ms | USB to serial converters often require a delay of up to 250ms after the ACK before changing baudrate (only relevant for 'C' mode) | no | 0 |
| `baudrate` | (initial) Baudrate | The baudrate of the serial port. If set to 'auto', it will be negotiated with the meter. The default is 300 baud for modes A, B, and C and 2400 baud for mode D, and 9600 baud for SML. | no | AUTO |

The default is 300 baud for modes A, B, and C and 2400 baud for mode D

## Channels

All available OBIS codes which are read out from the device will be created as channels.
At every read out the channels will be synched with the OBIS codes from the device.

Following conversion from OBIS codes to channel ID is done:
`.` is replaced by `-` and `:` or `*` is replaced by `#`.

e.g.

| OBIS code   | Channel ID |
|-------------|------------|
|`1-0:1.8.1` | `1-0#1-8-1` |
|`1.8.0*00` | `1-8-0#00` |


### Channel config

**conversionRatio:** This configuration is available for every Number OBIS channels. This is a quotient to easily convert  to a different unit. For example if the total electrical consumption is given in Wh from the device you usually want to convert it to kWh. To do this use `1000` as conversionRatio.

## Full Example

Things:

```
meterreader:meter:heating  [ port="COM1", refresh=10 ]
meterreader:meter:house [ port="rfc2217://xxx.xxx.xxx.xxx:3002" ]

meterreader:meter:BinderPower     [port="/dev/ttyUSB0", refresh=20] {
    Channels:
        Type NumberChannel : 1-0#1-8-0 [
            conversionRatio=1000
        ]
    }
```

Items:

```
Number HeatingTarif1        "Heating high price tariff [%.2f kwh]"      { channel="meterreader:meter:heating:1-0#1-8-1" }
Number HeatingTarif2        "Heating low price tariff [%.2f kwh]"       {  channel="meterreader:meter:heating:1-0#1-8-2" }

Number HouseTarif           "Tariff [%.2f kwh]"                         { channel="meterreader:meter:house:1-0#1-8-0" }

Number HeatingActualUsage   "Heating Current usage [%.2f W]"            { channel="meterreader:meter:heating:1-0#16-7-0" }
Number HouseActualUsage     "Current usage [%.2f W]"                    { channel="meterreader:meter:house:1-0#16-7-0" }
```

## Known limitations/issues

- Octet encoding for OBIS Codes
 
    - '129-129:199.130.5'
    - '1-0:0.0.9'
      doesn't work properly.

Any help/contribution is appreciated!

## Tested Hardware

The binding has been successfully tested with below hardware configuration:

SML PUSH mode:
- EMH EDL300 meter connected the IR-Reader USB from open hardware project in volkszaehler
- EMH eHZ-IW8E2A
- ISKRA MT681
- EMH eHZ-K

IEC 62056-21 Mode C:
- Apator EC3 with IR-Reader from volkszaehler

IEC 62056-21 Mode D:
- Hager EHZ 361Z5 and EHZ 161L5