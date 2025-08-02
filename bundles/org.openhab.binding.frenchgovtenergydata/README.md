# French Government Energy Data Binding

This binding provides regulated electricity prices in France.

This can be used to plan energy consumption, for example to calculate the cheapest period for running a dishwasher or charging an EV.

## Supported Things

The binding offers things for the two usual tariff classes (proposed by example by EDF).

- `base`: This is the basic subscription with a fixed kWh price.
- `hphc`: Alternative subscription offering variable price in a given hour set (low hours/high hours).
- `tempo`: Alternative suscription with different price in regards of day colors and day or night.
    Day colors can be Red, White or Blue.
    Red day are the one where there is most energy demands in France, and are the most higher price.
    White day are intermediate pricing, for day where energy demands is not almost important as red day.
    Blue day are the one with the lower price.

    Blue day, and in some proportion White day get very interesting discount in regards of base tariff.
    But Red day, in counter part, have very high rate during the daylight.

## Thing Configuration

Things (both `base`, `hphc` and `tempo`) only offers the configuration of the power output of the electrical delivery point (Linky terminal).

| Name                  | Type    | Description                                 | Default       | Required |
|-----------------------|---------|---------------------------------------------|---------------|----------|
| puissance             | integer | PDL power output (in kVA)                   | 6             | no       |

## Channels

### `base` Tariff Thing

All channels are read-only.

| Channel      | Type               | Description                             | Advanced |
|--------------|--------------------|-----------------------------------------|----------|
| fixed-ttc    | Number:Currency    | Yearly fixed price including taxes      | No       |
| variable-ttc | Number:EnergyPrice | Energy price in €/kWh including taxes   | No       |
| tariff-start | DateTime           | Beginning date for this tariff          | Yes      |
| fixed-ht     | Number:Currency    | Yearly fixed price excluding taxes      | Yes      |
| variable-ht  | Number:EnergyPrice | Energy price in €/kWh excluding taxes   | Yes      |

### `hphc` Tariff Thing

All channels are read-only.

| Channel      | Type               | Description                                        | Advanced |
|--------------|--------------------|----------------------------------------------------|----------|
| fixed-ttc    | Number:Currency    | Yearly fixed price including taxes                 | No       |
| hc-ttc       | Number:EnergyPrice | Low hours energy price in €/kWh including taxes    | No       |
| hp-ttc       | Number:EnergyPrice | High hours energy price in €/kWh including taxes   | No       |
| tariff-start | DateTime           | Beginning date for this tariff                     | Yes      |
| fixed-ht     | Number:Currency    | Yearly fixed price excluding taxes                 | Yes      |
| hc-ht        | Number:EnergyPrice | Low hours energy price in €/kWh excluding taxes    | Yes      |
| hp-ht        | Number:EnergyPrice | High hours energy price in €/kWh excluding taxes   | Yes      |

### `tempo` Tariff Thing

  | Channel      | Type               | Description                                                 | Advanced |
  |--------------|--------------------|-------------------------------------------------------------|----------|
  | fixed-ttc    | Number:Currency    | Yearly fixed price including taxes                          | No       |
  | blue-hc-ttc  | Number:EnergyPrice | Low hours blue day energy price in €/kWh including taxes    | No       |
  | blue-hp-ttc  | Number:EnergyPrice | High hours blue day energy price in €/kWh including taxes   | No       |
  | white-hc-ttc | Number:EnergyPrice | Low hours white day energy price in €/kWh including taxes   | No       |
  | white-hp-ttc | Number:EnergyPrice | High hours white day energy price in €/kWh including taxes  | No       |
  | red-hc-ttc   | Number:EnergyPrice | Low hours red day energy price in €/kWh including taxes     | No       |
  | red-hp-ttc   | Number:EnergyPrice | High hours red day energy price in €/kWh including taxes    | No       |
  | tariff-start | DateTime           | Beginning date for this tariff                              | Yes      |
  | fixed-ht     | Number:Currency    | Yearly fixed price excluding taxes                          | Yes      |
  | blue-hc-ht   | Number:EnergyPrice | Low hours blue day energy price in €/kWh excluding taxes    | Yes      |
  | blue-hp-ht   | Number:EnergyPrice | High hours blue day energy price in €/kWh excluding taxes   | Yes      |
  | white-hc-ht  | Number:EnergyPrice | Low hours white day energy price in €/kWh excluding taxes   | Yes      |
  | white-hp-ht  | Number:EnergyPrice | High hours white day energy price in €/kWh excluding taxes  | Yes      |
  | red-hc-ht    | Number:EnergyPrice | Low hours red day energy price in €/kWh excluding taxes     | Yes      |
  | red-hp-ht    | Number:EnergyPrice | High hours red day energy price in €/kWh excluding taxes    | Yes      |

## Full Example

### Thing Configuration Example

```java
Thing frenchgovtenergydata:base:local "Tarification Actuelle Base" [puissance=9]
Thing frenchgovtenergydata:hphc:local "Tarification Actuelle HP/HC" [puissance=9]
Thing frenchgovtenergydata:tempo:local "Tarification Actuelle Tempo" [puissance=9]
```

### Item Configuration

```java
DateTime Tarif_Start { channel="frenchgovtenergydata:hphc:local:tariff-start" }
Number:Currency Abonnement_Annuel {channel="frenchgovtenergydata:hphc:local:fixed-ttc"}
Number:EnergyPrice Prix_Heure_Pleine {channel="frenchgovtenergydata:hphc:local:hp-ttc"}
Number:EnergyPrice Prix_Heure_Creuse {channel="frenchgovtenergydata:hphc:local:hc-ttc"}
```
