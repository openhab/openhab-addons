# <bindingName> Binding

This binding is for monitoring MPPT Solarcharger from Victron Energy. The Solarcharger should be connected to your Raspberry Pi. The communication is over D-BUS. One way to make this communication possible, you can install vrmlogger.
To do so, look at [this howto](https://github.com/victronenergy/venus/wiki/raspberrypi-install-venus-packages).
To allow the user openhab the access via dbus to com.victronenergy edit the file `/etc/dbus-1/system.d/com.victronenergy.vedirect.conf` and add the follwing configuration between `<busconfig> </busconfig>`

```
        <policy user="openhab">
                <allow own_prefix="com.victronenergy"/>
                <allow send_type="method_call"/>
        </policy>

```

## Supported Things

- [VictronEnergyDBus SolarCharger](https://www.victronenergy.com/solar-charge-controllers) (Thing type `sc`)

Only tested with BlueSolar MPPT 100/30

## Discovery

Auto-Discovery has not implemented yet.


## Thing Configuration

The easiest way to configure a Thing (VictronEnergyDBus SolarCharger - `sc`) is via PaperUi. But it is also possible via Thing-File (see below).

| Configuration Parameter | Required | Default | Description                                         |
|-------------------------|----------|---------|-----------------------------------------------------|
| port                    | X        | ttyUSB0     | The USB-Port to which the sc is connected to   |

## Channels

| Channel  | Item Type | Description                                    |
|----------|-----------|------------------------------------------------|
| state    | Number    | Actual State of your Device as Number          |
| stateStr | String    | Actual State of your Device as String          |
| DcV      | Number    | Solarcharger Battery Voltage                   |
| DcI      | Number    | Solarcharger Current to Battery                |
| PvV      | Number    | Photovoltaics Voltage                          |
| PvI      | Number    | Photovoltaics Current                          |
| YP       | Number    | Yield Power in W                               |
| YU       | Number    | Yield User in KWh                              |
| YS       | Number    | Yield System in KWh                            |
| YT       | Number    | Yield Today in KWh                             |
| MPT      | Number    | Maximum Power Today in W                       |
| TIFT     | Number    | Time in Float Today in Min                     |
| TIAT     | Number    | Time in Absorption Today in Min                |
| TIBT     | Number    | Time in Bulk Today in Min                      |
| MPVT     | Number    | Maximum Photovoltaik Voltage Today in Volt     |
| MBCT     | Number    | Maximum Battery Current Today in Ampere        |
| MinBVT   | Number    | Minimum Battery Voltage Today in Volt          |
| MaxBVT   | Number    | Maximum Battery Voltage Today in Volt          |
| YY       | Number    | Yield Yesterday in KWh                         |
| MPY      | Number    | Maximum Power Yesterday in W                   |
| TIFY     | Number    | Time in Float Yesterday in Min                 |
| TIAY     | Number    | Time in Absorption Yesterday in Min            |
| TIBY     | Number    | Time in Bulk Yesterday in Min                  |
| MPVY     | Number    | Maximum Photovoltaik Voltage Yesterday in Volt |
| MBCY     | Number    | Maximum Battery Current Yesterday in Ampere    |
| MinBVY   | Number    | Minimum Battery Voltage Yesterday in Volt      |
| MaxBVY   | Number    | Maximum Battery Voltage Yesterday in Volt      |
| Serial   | String    | The Serial of your Device                      |
| FwV      | Number    | Firmware Version                               |
| PId      | Number    | Product Id                                     |
| DI       | Number    | Device Instance                                |
| Pn       | String    | Product Name                                   |
| Err      | Number    | Error Code as Number                           |
| ErrStr   | String    | Error of the Device as String                  |

## Full Example

### victronenergydbus.things:

```
Thing victronenergydbus:sc:mysc [port="ttyUSB0"]
```

### victronenergydbus.items:

```
Number state "State Number [%d]" {channel="victronenergydbus:sc:mysc:state"}
String stateStr "State String" {channel="victronenergydbus:sc:mysc:stateStr"}
Number DcV "Solarcharger Voltage [%.2f V]" {channel="victronenergydbus:sc:mysc:DcV"}
Number DcI "Solarcharger Current [%.2f A]" {channel="victronenergydbus:sc:mysc:DcI"}
Number PvV "Photo Photovoltaics Voltage [%.2f V]" {channel="victronenergydbus:sc:mysc:PvV"}
Number PvI "Photo Photovoltaics Current [%.2f V]" {channel="victronenergydbus:sc:mysc:PvI"}
Number YP "Yield Power [%d W]" {channel="victronenergydbus:sc:mysc:YP"}
Number YU "Yield User [%.2f KWh]" {channel="victronenergydbus:sc:mysc:YU"}
Number YS "Yield System [%.2f KWh]" {channel="victronenergydbus:sc:mysc:YS"}
Number YT "Yield Today [%.2f KWh]" {channel="victronenergydbus:sc:mysc:YT"}
Number MPT "Maximum Power Today [%d W]" {channel="victronenergydbus:sc:mysc:MPT"}
Number TIFT "Time in Float Today [%d min]" {channel="victronenergydbus:sc:mysc:TIFT"}
Number TIAT "Time in Absorption Today [%d min]" {channel="victronenergydbus:sc:mysc:TIAT"}
Number TIBT "Time in Bulk Today [%d min]" {channel="victronenergydbus:sc:mysc:TIBT"}
Number MPVT "Maximum Pv Voltage Today [%.2f V]" {channel="victronenergydbus:sc:mysc:MPVT"}
Number MBCT "Maximum Battery Current Today [%.2f A]" {channel="victronenergydbus:sc:mysc:MBCT"}
Number MinBVT "Minimum Battery Voltage Today [%.2f A]" {channel="victronenergydbus:sc:mysc:MinBVT"}
Number MaxBVT "Maximum Battery Voltage Today [%.2f A]" {channel="victronenergydbus:sc:mysc:MaxBVT"}
Number YY "Yield Yesterday [%.2f KWh]" {channel="victronenergydbus:sc:mysc:YY"}
Number MPY "Maximum Power Yesterday [%d W]" {channel="victronenergydbus:sc:mysc:MPY"}
Number TIFY "Time in Float Yesterday [%d min]" {channel="victronenergydbus:sc:mysc:TIFY"}
Number TIAY "Time in Absorption Yesterday [%d min]" {channel="victronenergydbus:sc:mysc:TIAY"}
Number TIBY "Time in Bluk Yesterday [%d min]" {channel="victronenergydbus:sc:mysc:TIBY"}
Number MPVY "Maximum Pv Voltage Yesterday [%.2f V]" {channel="victronenergydbus:sc:mysc:MPVY"}
Number MBCY "Maximum Battery Current Yesterday [%.2f A]" {channel="victronenergydbus:sc:mysc:MBCY"}
Number MinBVY "Minimum Battery Voltage Yesterday [%.2f A]" {channel="victronenergydbus:sc:mysc:MinBVY"}
Number MaxBVY "Maximum Battery Voltage Yesterday [%.2f A]" {channel="victronenergydbus:sc:mysc:MaxBVY"}
String Serial "Serial" {channel="victronenergydbus:sc:mysc:Serial"}
Number FwV "Firmware Version [%d]" {channel="victronenergydbus:sc:mysc:FwV"}
Number PId "Product Id [%d]" {channel="victronenergydbus:sc:mysc:PId"}
Number DI "Device Instance [%d]" {channel="victronenergydbus:sc:mysc:DI"}
String Pn "Product Name" {channel="victronenergydbus:sc:mysc:Pn"}
Number Err "Error Code [%d]" {channel="victronenergydbus:sc:mysc:Err"}
String ErrStr "Error" {channel="victronenergydbus:sc:mysc:ErrStr"}
```

## Example Installation in my Camper

I'm using this binding in my CamperVan Solar Installation which I described [here](http://thejollyjumper.de/2018/10/18/elektrik/) If you have any question you can drop me a comment at this page.
