# French Government Energy Data Binding

This binding provides regulated electricity prices in France.

This can be used to plan energy consumption, for example to calculate the cheapest period for running a dishwasher or charging an EV.

## Supported Things

The binding offers things for the two usual tariff classes (proposed by example by EDF).

- `base`: This is the basic subscription with a fixed kWh price.
- `hphc`: Alternative subscription offering variable price in a given hour set (low hours/high hours).

## Thing Configuration

Things (both `base` and `hphc`) only offers the configuration of the power output of the electrical delivery point (Linky terminal).

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

## Full Example

### Thing Configuration

```java
Thing frenchgovtenergydata:hphc:local "Tarification Actuelle HP/HC" [puissance=9]
```

### Item Configuration

```java
DateTime Tarif_Start { channel="frenchgovtenergydata:hphc:local:tariff-start" }
Number:Currency Abonnement_Annuel {channel="frenchgovtenergydata:hphc:local:fixed-ttc"}
Number:EnergyPrice Prix_Heure_Pleine {channel="frenchgovtenergydata:hphc:local:hp-ttc"}
Number:EnergyPrice Prix_Heure_Creuse {channel="frenchgovtenergydata:hphc:local:hc-ttc"}
```
