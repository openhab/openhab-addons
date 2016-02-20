# Kostal inverter Binding

Uses jsoup to scrape the web interface of the inverter for the metrics below.

## Supported Things

Kostal Inverter

## Discovery

None

## Channels

acPower
totalEnergy
dayEnergy
status

## Full Example

Number SolarPower "Solar Power [%.2f Watt]" (gGF) {kostal="aktuell"}
Number SolarEnergyDay "TagesLeistung Solar[%.2f kwh]" (gGF) {webscrape="Tagesenergie"}
