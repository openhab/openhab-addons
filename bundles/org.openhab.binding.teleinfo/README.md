# Teleinfo Binding

_The Teleinfo binding supports an interface to ENEDIS/ERDF [Teleinfo protocol](http://www.linuxembarque.free.fr/electro/compt_energie/specifications_techniques_edf_teleinfo.pdf) for (French) Electricity Meter._
_Teleinfo is a protocol to read many electrical statistics of your electricity meter: TODO_



_If possible, provide some resources like pictures, a YouTube video, etc. to give an impression of what can be done with this binding. You can place such resources into a `doc` folder next to this README.md._

## Supported Things

The Teleinfo binding provides support for the following mode:

- HCHP mode
- Base mode
- Tempo mode
- EJP mode

### Teleinfo controller

Before the binding can be used, a serial controller must be added. This needs to be done manually. Select __Teleinfo Serial Controller__ and enter the serial port.


## Discovery

_Describe the available auto-discovery features here. Mention for what it works and what needs to be kept in mind when using it._

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/ESH-INF/thing``` of your binding._

| Thing type       | Parameter  | Meaning                        | Posible values |
|------------------|------------|--------------------------------|----------------|
| SerialController | serialPort | Path to the serial controller  | /dev/ttyXXXX   |
| cbemm_xxx        | adco       | Electric delivery point number | 031728832562   |

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/ESH-INF/thing``` of your binding._

| channel  | type                      | description                                          |
|----------|---------------------------|------------------------------------------------------|
| isousc   | `Number:ElectricCurrent`  | Subscribed electric current                          |
| ptec     | `String`                  | Current pricing period                               |
| imax     | `Number:ElectricCurrent`  | Maximum electric current                             |
| adps     | `Number:ElectricCurrent`  | Excess electric current warning                      |
| iinst    | `Number:ElectricCurrent`  | Instantaneous electric current                       |
| papp     | `Number`                  | Instantaneous apparent power (Unit: `VA`)            |
| hhphc    | `String`                  | Pricing schedule group (HCHP mode only)              | 
| hchc     | `Number:Energy`           | Meter reading for low rate pricing (HCHP mode only)  | 
| hchp     | `Number:Energy`           | Meter reading for high rate pricing (HCHP mode only) |
| base     | `Number:Energy`           | Meter reading (Base mode only)                       |


## Full Example

`teleinfo.things` for a serial USB controller on `/dev/ttyUSB0` for a Single-phase Electricity meter with HC/HP option - CBEMM Evolution ICC:

```
Bridge teleinfo:bridge:serialcontroller [ serialport="/dev/ttyUSB0" ]{
  Thing cbemm_evolution_icc_hc_electricitymeter teleinfo1 [ adco="031728832562"]
}
```

`teleinfo.items`: 


```
Number:ElectricCurrent iSousc "iSousc" {channel="teleinfo:teleinfo1:isousc"}
```

## Supported hardwares

_The Teleinfo binding has been successfully validated with below hardware configuration:_

| Serial interface | Power Energy Meter model    | Mode                   |
|----------|--------|------------------------------|
| GCE Electronics USB Teleinfo module [(more details)](http://gce-electronics.com/fr/usb/655-module-teleinfo-usb.html) | Actaris A14C5 | Single-phase HCHC  |
| Cartelectronic USB Teleinfo modem [(more details)](https://www.cartelectronic.fr/teleinfo-compteur-enedis/17-teleinfo-1-compteur-usb-rail-din-3760313520028.html) | Actaris A14C5 | Single-Phase HCHP |


