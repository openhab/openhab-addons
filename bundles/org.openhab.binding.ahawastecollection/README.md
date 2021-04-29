# aha Waste Collection Binding

This binding provides information about the upcoming waste collection dates for places, that are served by aha, the waste collection company for the region of Hannover. The values are retrieved from the online aha waste collection schedule available at: [aha Abfuhrkalender](https://www.aha-region.de/abholtermine/abfuhrkalender).

## Supported Things

-   **collectionSchedule:** Represents the connection to the **aha Waste Collection Schedule** with four channels for the different waste types.

## Discovery

Discovery is not possible, due some form input values from the website above are required.

## Thing Configuration

For configuration of the **collectionSchedule** thing, you need the form inputs from the aha collections schedule web page. Follow the steps below to get the required configuration parameters from the form input values.


1. Open [aha Abfuhrkalender](https://www.aha-region.de/abholtermine/abfuhrkalender) in your favorite brower with developer-console.
2. Open the developer console and switch to network tab (for example press F12 in chrome / edge / firefox).
3. Fill in the form: Select your commune, Street and house number and hit "Suchen".
4. Select the first request to https://www.aha-region.de/abholtermine/abfuhrkalender (see first screenshot below)
5. Check the form data at the end of the request for the form values (see second screenshot below)
5. Fill in the values from the form input in thing configuration (see examples below)

![Chrome Developer Console Top](doc/images/ChromeDevconsoleTop.png "Chrome Developer Console showing request URL")

*Check if you've selected the correct request, that contains the form data*

![Chrome Developer Console Bottom](doc/images/ChromeDevconsoleBottom.png "Chrome Developer Console showing form inputs")

*Grab the values for the configuration parameters from the form data section at the end of the request*

**collectionSchedule** parameters:

| Property | Default | Required | Description |
|-|-|-|-|
| `commune` | | Yes | The selected commune, taken from the form field `gemeinde`. |
| `street` | | Yes | The selected street, taken from the form field `strasse`. This value must look like 67269@Rosmarinweg+/+Kirchhorst@Kirchhorst |
| `houseNumber` |  | Yes | The selected house number, taken from the form field `hausnr`. |
| `houseNumberAddon` | | No | The selected house number addon, taken from the form field `hausnraddon`, may be empty. |
| `collectionPlace` | | Yes | Form value for the collection place, taken from the form field `ladeort`. This value must look like 67269-0010+ |

## Channels

The thing **aha Waste Collection Schedule** provides four channels for the upcoming day of waste collection for the different waste types.


| channel  | type   | description                  |
|----------|--------|------------------------------|
| generalWaste  | DateTime | Next collection day for general waste  |
| leightweightPackaging  | DateTime | Next collection day for leightweight packaging  |
| bioWaste  | DateTime | Next collection day for bio waste  |
| paper  | DateTime | Next collection day for paper  |


## Full Example

wasteCollection.things

```
Thing ahawastecollection:collectionSchedule:wasteCollectionSchedule "aha Abfuhrkalender" [ commune="Isernhagen", street="67269@Rosmarinweg+/+Kirchhorst@Kirchhorst", houseNumber="10", houseNumberAddon="", collectionPlace="67269-0010+" ] 
```

wasteCollection.items

```
DateTime collectionDay_generalWaste "Next general waste collection" {channel="ahawastecollection:collectionSchedule:wasteCollectionSchedule:generalWaste"}
DateTime collectionDay_leightweightPackaging "Next lightweight packaging collection" {channel="ahawastecollection:collectionSchedule:wasteCollectionSchedule:leightweightPackaging"}
DateTime collectionDay_bioWaste "Next bio waste collection" {channel="ahawastecollection:collectionSchedule:wasteCollectionSchedule:bioWaste"}
DateTime collectionDay_paper "Next paper collection" {channel="ahawastecollection:collectionSchedule:wasteCollectionSchedule:paper"}
```
