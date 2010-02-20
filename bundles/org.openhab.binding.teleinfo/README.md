# Teleinfo Binding

The Teleinfo binding supports an interface to ENEDIS/ERDF [Teleinfo protocol](http://www.linuxembarque.free.fr/electro/compt_energie/specifications_techniques_edf_teleinfo.pdf) for (French) Electricity Meter. This binding works with a Teleinfo modem plugged to the I1 and I2 terminals of your electricity meter. Teleinfo modems can be ordered (see the [list of tested hardware](#tested-hardware) below) or build by yourself (see [this example](http://bernard.lefrancois.free.fr)).

Teleinfo is a protocol to read many electrical statistics of your electricity meter: instantaneous power consumption, current price period, meter reading... 
These values can be used to

- send your meter reading to your electricity provider with a simple copy/paste,
- improve your rules and minimize electricity costs,
- check if your subscription is relevant for your needs,
- monitor your electricity consumption,

## Supported Things

The Teleinfo binding provides support for both single phase and three phase connection, ICC evolution and the following pricing modes:

- HCHP mode
- Base mode
- Tempo mode
- EJP mode

| Thing type                                 | Connection   | Pricing mode | ICC evolution |
|--------------------------------------------|--------------|--------------|---------------|
| cbemm_base_electricitymeter                | single-phase | Base         |               |
| cbemm_ejp_electricitymeter                 | single-phase | EJP          |               |
| cbemm_hc_electricitymeter                  | single-phase | HCHP         |               |
| cbemm_tempo_electricitymeter               | single-phase | Tempo        |               |
| cbemm_evolution_icc_base_electricitymeter  | single-phase | Base         | [x]           |
| cbemm_evolution_icc_ejp_electricitymeter   | single-phase | EJP          | [x]           |
| cbemm_evolution_icc_hc_electricitymeter    | single-phase | HCHP         | [x]           |
| cbemm_evolution_icc_tempo_electricitymeter | single-phase | Tempo        | [x]           |
| cbetm_base_electricitymeter                | three-phase  | Base         |               |
| cbetm_ejp_electricitymeter                 | three-phase  | EJP          |               |
| cbetm_hc_electricitymeter                  | three-phase  | HCHP         |               |
| cbetm_tempo_electricitymeter               | three-phase  | Tempo        |               |

## Discovery

Before the binding can be used, a serial controller must be added. This needs to be done manually. Select __Teleinfo Serial Controller__ and enter the serial port. Once the serial controller added, electricity meters will automatically appear when trying to add a new thing, with default label __Teleinfo ADCO #adco__ where __#adco__ is  your electricity meter identifier.

## Thing Configuration

| Thing type           | Parameter    | Meaning                               | Possible values  |
|----------------------|--------------|---------------------------------------|------------------|
| `serialcontroller`   | `serialport` | Path to the serial controller         | /dev/ttyXXXX     |
| `*_electricitymeter` | `adco`       | Electricity meter identifier          | 12 digits number |

## Channels

Channel availability depends on the electricity connection (single or three phase) and on the pricing mode (Base, HCHP, EJP or Tempo).

| Channel  | Type                      | Description                                              | Phase  | Mode  |
|----------|---------------------------|----------------------------------------------------------|--------|-------|
| isousc   | `Number:ElectricCurrent`  | Subscribed electric current                              | All    | All   |
| ptec     | `String`                  | Current pricing period                                   | All    | All   |
| imax     | `Number:ElectricCurrent`  | Maximum consumed electric current                        | Single | All   |
| imax1    | `Number:ElectricCurrent`  | Maximum consumed electric current on phase 1             | Three  | All   |
| imax2    | `Number:ElectricCurrent`  | Maximum consumed electric current on phase 2             | Three  | All   |
| imax3    | `Number:ElectricCurrent`  | Maximum consumed electric current on phase 3             | Three  | All   |
| adps     | `Number:ElectricCurrent`  | Excess electric current warning                          | Single | All   |
| adir1    | `Number:ElectricCurrent`  | Excess electric current on phase 1 warning               | Three  | All   |
| adir2    | `Number:ElectricCurrent`  | Excess electric current on phase 2 warning               | Three  | All   |
| adir3    | `Number:ElectricCurrent`  | Excess electric current on phase 3 warning               | Three  | All   |
| iinst    | `Number:ElectricCurrent`  | Instantaneous electric current                           | Single | All   |
| iinst1   | `Number:ElectricCurrent`  | Instantaneous electric current on phase 1                | Three  | All   |
| iinst2   | `Number:ElectricCurrent`  | Instantaneous electric current on phase 2                | Three  | All   |
| iinst3   | `Number:ElectricCurrent`  | Instantaneous electric current on phase 3                | Three  | All   |
| ppot     | `String`                  | Electrical potential presence                            | Three  | All   |
| pmax     | `Number:Energy`           | Maximum consumed electric power on all phases            | Three  | All   |
| papp     | `Number`                  | Instantaneous apparent power (Unit: `VA`)                | Three, single (ICC evolution only) | All   |
| hhphc    | `String`                  | Pricing schedule group                                   | All    | HCHP  |
| hchc     | `Number:Energy`           | Total consumed energy at low rate pricing                | All    | HCHP  |
| hchp     | `Number:Energy`           | Total consumed energy at high rate pricing               | All    | HCHP  |
| base     | `Number:Energy`           | Total consumed energy                                    | All    | Base  |
| ejphn    | `Number:Energy`           | Total consumed energy at low rate pricing                | All    | EJP   |
| ejphpm   | `Number:Energy`           | Total consumed energy at high rate pricing               | All    | EJP   |
| bbrhcjb  | `Number:Energy`           | Total consumed energy at low rate pricing on blue days   | All    | Tempo |
| bbrhpjb  | `Number:Energy`           | Total consumed energy at high rate pricing on blue days  | All    | Tempo |
| bbrhcjw  | `Number:Energy`           | Total consumed energy at low rate pricing on white days  | All    | Tempo |
| bbrhpjw  | `Number:Energy`           | Total consumed energy at high rate pricing on white days | All    | Tempo |
| bbrhcjr  | `Number:Energy`           | Total consumed energy at low rate pricing on red days    | All    | Tempo |
| bbrhpjr  | `Number:Energy`           | Total consumed energy at high rate pricing on red days   | All    | Tempo |
| pejp     | `Number:Duration`         | Prior notice to EJP start                                | All    | EJP   |
| demain   | `String`                  | Following day color                                      | All    | Tempo |

## Full Example

The following `things` file declare a serial USB controller on `/dev/ttyUSB0` for a Single-phase Electricity meter with HC/HP option - CBEMM Evolution ICC and adco `031528042289` :

```
Bridge teleinfo:serialcontroller:teleinfoUSB [ serialport="/dev/ttyUSB0" ]{
    Thing cbemm_evolution_icc_hc_electricitymeter myElectricityMeter [ adco="031528042289"]
}
```

`adco` is a 12 digit number writen on the electricity meter (There might be two additional digits on the electricity meter, in this case the two last digits must be omitted to obtain 12 digits). The first 6 digits of `adco` can also be retrieved by pushing 6 times the `selection` button of your electricity meter, and the last 6 digits by pushing the `defilement` button.

This `items` file links some supported channels to items: 

```
Number TLInfoEDF_PAPP "PAPP" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:papp"}
Number:ElectricCurrent TLInfoEDF_ISOUSC "ISOUSC" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:isousc"}
String TLInfoEDF_PTEC "PTEC" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:ptec"}
Number:ElectricCurrent TLInfoEDF_IMAX "IMAX" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:imax"}
Number:ElectricCurrent TLInfoEDF_ADPS "ADPS" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:adps"}
Number:ElectricCurrent TLInfoEDF_IINST "IINST" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:iinst"}
Number:Energy TLInfoEDF_HCHC "HCHC" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:hchc"}
Number:Energy TLInfoEDF_HCHP "HCHP" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:hchp"}
String TLInfoEDF_HHPHC "HHPHC" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:hhphc"}
```

## Tested hardwares

The Teleinfo binding has been successfully validated with below hardware configuration:

| Serial interface | Power Energy Meter model    | Mode(s)                   |
|----------|--------|------------------------------|
| GCE Electronics USB Teleinfo module [(more details)](http://gce-electronics.com/fr/usb/655-module-teleinfo-usb.html) | Actaris A14C5 | - Single-phase HCHP & Base |
| Cartelectronic USB Teleinfo modem [(more details)](https://www.cartelectronic.fr/teleinfo-compteur-enedis/17-teleinfo-1-compteur-usb-rail-din-3760313520028.html) | Sagem S10C4 | Single-phase HCHP |


