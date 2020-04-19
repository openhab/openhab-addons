# Teleinfo Binding

The Teleinfo binding supports an interface to ENEDIS/ERDF [Teleinfo protocol](http://www.linuxembarque.free.fr/electro/compt_energie/specifications_techniques_edf_teleinfo.pdf) for (French) Electricity Meter.
Teleinfo is a protocol to read many electrical statistics of your electricity meter: instantaneous power consumption, current price period, meter reading... 
These values can be used to

- send your meter reading to your electricity provider with a simple copy/paste,
- improve your rules and minimize electricity costs,
- check if your subscription is relevant for your needs,
- monitor your electricity consumption,

## Supported Things

The Teleinfo binding provides support for the following mode:

- HCHP mode
- Base mode
- Tempo mode
- EJP mode

## Thing Configuration

Before the binding can be used, a serial controller must be added. This needs to be done manually. Select __Teleinfo Serial Controller__ and enter the serial port. Once the serial controller added, electricity meters will be automatically discovered and a new thing labelled __Teleinfo ADCO #id__ will be created (where __#id__ is  your delivery point identifier).

| Thing type       | Parameter  | Meaning                               | Posible values |
|------------------|------------|---------------------------------------|----------------|
| SerialController | serialPort | Path to the serial controller         | /dev/ttyXXXX   |
| cbemm_xxx        | adco       | Electricity delivery point identifier | 031728832562   |

## Channels

Channel availabity depends on the electricity connection (single or three phase) and on the meter mode (Base, HCHP, EJP or Tempo).

| Channel  | Type                      | Description                                              | Single-phase | Three-phase | Base | HCHP | EJP | Tempo |
|----------|---------------------------|----------------------------------------------------------|--------------|-------------|------|------|-----|-------|
| isousc   | `Number:ElectricCurrent`  | Subscribed electric current                              | [x]          |             | [x]  | [x]  | [x] | [x]   |
| ptec     | `String`                  | Current pricing period                                   | [x]          |             | [x]  | [x]  | [x] | [x]   |
| imax     | `Number:ElectricCurrent`  | Maximum consumed electric current                        | [x]          |             | [x]  | [x]  | [x] | [x]   |
| adps     | `Number:ElectricCurrent`  | Excess electric current warning                          | [x]          |             | [x]  | [x]  | [x] | [x]   |
| iinst    | `Number:ElectricCurrent`  | Instantaneous electric current                           | [x]          |             | [x]  | [x]  | [x] | [x]   |
| papp     | `Number`                  | Instantaneous apparent power (Unit: `VA`)                | [x]          |             | [x]  | [x]  | [x] | [x]   |
| hhphc    | `String`                  | Pricing schedule group                                   | [x]          |             |      | [x]  |   |     |
| hchc     | `Number:Energy`           | Total consumed energy at low rate pricing                | [x]          |             |      | [x]  |   |     |
| hchp     | `Number:Energy`           | Total consumed energy at high rate pricing               | [x]          |             |      | [x]  |   |     |
| base     | `Number:Energy`           | Total consumed energy                                    | [x]          |             | [x]  |      |      |      |
| ejphn    | `Number:Energy`           | Total consumed energy at low rate pricing                | [x]          |             |      |      | [x]  |      |
| ejphpm   | `Number:Energy`           | Total consumed energy at high rate pricing               | [x]          |             |      |      | [x]  |      |
| bbrhcjb  | `Number:Energy`           | Total consumed energy at low rate pricing on blue days   | [x]          |             |      |      |      | [x]  |
| bbrhpjb  | `Number:Energy`           | Total consumed energy at high rate pricing on blue days  | [x]          |             |      |      |      | [x]  |
| bbrhcjw  | `Number:Energy`           | Total consumed energy at low rate pricing on white days  | [x]          |             |      |      |      | [x]  |
| bbrhpjw  | `Number:Energy`           | Total consumed energy at high rate pricing on white days | [x]          |             |      |      |      | [x]  |
| bbrhcjr  | `Number:Energy`           | Total consumed energy at low rate pricing on red days    | [x]          |             |      |      |      | [x]  |
| bbrhpjr  | `Number:Energy`           | Total consumed energy at high rate pricing on red days   | [x]          |             |      |      |      | [x]  |
| pejp     | `Number:Duration`         | Prior notice to EJP start                                | [x]          |             |      |      | [x]  |      |
| demain   | `String`                  | Following day color                                      | [x]          |

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

The Teleinfo binding has been successfully validated with below hardware configuration:

| Serial interface | Power Energy Meter model    | Mode                   |
|----------|--------|------------------------------|
| GCE Electronics USB Teleinfo module [(more details)](http://gce-electronics.com/fr/usb/655-module-teleinfo-usb.html) | Actaris A14C5 | Single-phase HCHC  |
| Cartelectronic USB Teleinfo modem [(more details)](https://www.cartelectronic.fr/teleinfo-compteur-enedis/17-teleinfo-1-compteur-usb-rail-din-3760313520028.html) | Actaris A14C5 | Single-Phase HCHP |


