# SmlReader Binding

SML is a communication protocol for transmitting meter data for so called smart meters. SML is similar to XML with regard to its ability to nest information tags within other tags. But the standard way of coding SML packets is in a more efficient binary format that is similar but not equal to BER encoding. Also the specification comes in a form that is similar to ASN.1 but not completely standards compliant.

To decode SML messages, the binding takes advantage of jSML library by Fraunhofer Institute.

Please see following page for more informations (German but with good links):
https://de.wikipedia.org/wiki/Smart_Message_Language

## Supported Things

This binding supports only one Thing: `meter`

## Discovery

Discovery is not available, as the binding only reads from serial ports.

## Binding Configuration

No binding configuration required.

## Thing Configuration

The SmlReader thing requires the serial port where the meter device is connected and optionally an refresh intervall.

| Parameter | Name | Description | Required |
|-----------|------|-------------|----------|
| `port` | The serial port to connect to| URL to use for playing notification sounds, e.g. `/dev/ttyUSB0` | yes |
| `refresh` | The refresh interval in seconds | Defines at which interval the values of the meter device shall be read | no |


## Channels

All available OBIS codes which are read out from the device will be created as channels.
At every read out the channels will be synched with the OBIS codes from the device.

Following conversion from OBIS codes to channel ID is done:
`.` is replaced by `-` and `:` is replaced by `#`.

e.g.

| OBIS code   | Channel ID |
|-------------|------------|
|`1-0:1.8.1` | `1-0#1-8-1` |


### Channel config

**conversionRatio:** This configuration is available for every Number OBIS channels. This is a quotient to easily convert  to a different unit. For example if the total electrical consumption is given in Wh from the device you usually want to convert it to kWh. To do this use `1000` as conversionRatio.

## Full Example

Things:

```
smlreader:meter:heating  [ port="COM1", refresh=10 ]
smlreader:meter:house [ port="rfc2217://xxx.xxx.xxx.xxx:3002" ]
```

Items:

```
Number HeatingTarif1        "Heating high price tariff [%.2f kwh]"      { channel="smlreader:meter:heating:1-0#1-8-1" }
Number HeatingTarif2        "Heating low price tariff [%.2f kwh]"       {  channel="smlreader:meter:heating:1-0#1-8-2" }

Number HouseTarif           "Tariff [%.2f kwh]"                         { channel="smlreader:meter:house:1-0#1-8-0" }

Number HeatingActualUsage   "Heating Current usage [%.2f W]"            { channel="smlreader:meter:heating:1-0#16-7-0" }
Number HouseActualUsage     "Current usage [%.2f W]"                    { channel="smlreader:meter:house:1-0#16-7-0" }
```

## Known limitations/issues

- SmlReaderBinding isn't able to read SML from devices which are working in "pull" mode, e.g. Froetec ZG22.
- Octet encoding for OBIS Codes
 
    - '129-129:199.130.5'
    - '1-0:0.0.9'
      doesn't work properly.

Any help/contribution is appreciated!

## Tested Hardware

The binding has been successfully tested with below hardware configuration:

- EMH EDL300 meter connected the IR-Reader USB from open hardware project in volkszaehler
