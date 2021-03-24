# aha Waste Collection Binding

This Binding allows requesting the next waste collection days from the aha waste collection schedule, reachable at [aha Abfuhrkalender](https://www.aha-region.de/abholtermine/abfuhrkalender).

## Supported Things

The binding supports one thing type **aha Waste Collection Schedule** with four channels for the different waste types.

## Discovery

Discovery is not possible, due some form input values from the website above are required.

## Thing Configuration

To configure an **aha Waste Collection Schedule** thing, you need first to get the required parameters from to get them follow the instructions below:
the form at https://www.aha-region.de/abholtermine/abfuhrkalender.

1. Open [aha Abfuhrkalender](https://www.aha-region.de/abholtermine/abfuhrkalender) in your favorite brower with developer-console.
2. Open the developer console and switch to network tab (for example press F12 in chrome / edge / firefox)
3. Fill in the form: Select your commune, Street and Housenumber and hit "Suchen".
4. Grab the form input fields from the first POST request to https://www.aha-region.de/abholtermine/abfuhrkalender.
5. Fill in the values from the form input in thing configuration.

## Channels

The thing **aha Waste Collection Schedule** provides four channels for the next day of waste collection from the different waste types.


| channel  | type   | description                  |
|----------|--------|------------------------------|
| generalWaste  | DateTime | Next collection day for general waste  |
| leightweightPackaging  | DateTime | Next collection day for leightweight packaging  |
| bioWaste  | DateTime | Next collection day for bio waste  |
| paper  | DateTime | Next collection day for paper  |


## Full Example

wasteCollection.things

```xtend
Thing ahawastecollection:collectionSchedule:wasteCollectionSchedule "aha Abfuhrkalender" [ commune="Isernhagen", street="67269@Rosmarinweg+/+Kirchhorst@Kirchhorst", houseNumber="10", houseNumberAddon="", collectionPlace="67269-0010+" ] 
```

wasteCollection.items

```xtend
DateTime collectionDay_generalWaste "Next genral waste collection" {channel="ahawastecollection:collectionSchedule:wasteCollectionSchedule:generalWaste"}
DateTime collectionDay_leightweightPackaging "Next lightweight packaging collection" {channel="ahawastecollection:collectionSchedule:wasteCollectionSchedule:leightweightPackaging"}
DateTime collectionDay_bioWaste "Next bio waste collection" {channel="ahawastecollection:collectionSchedule:wasteCollectionSchedule:bioWaste"}
DateTime collectionDay_paper "Next paper collection" {channel="ahawastecollection:collectionSchedule:wasteCollectionSchedule:paper"}
```
