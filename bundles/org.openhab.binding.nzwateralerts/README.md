# NZ Water Alerts Binding

Get Water Alert Levels for cities in New Zealand.
Getting this alert level can help you script and automate smarter tasks for water and avoid getting penalized from your distract or local council.

> Example: Disable automated spinklers based on a level 3 or 4 water alert level

This Binding scrapes multiple websites for Water Levels:

- Northland's [BeWaterWise Website](https://bewaterwise.org.nz/)
- Waikato's [Smart Water Website](https://www.smartwater.org.nz/)
- Napier's [Council Website](https://www.napier.govt.nz)

## Thing Configuration

The binding and thing ID is `nzwateralerts:wateralert`.

### Configuration Values

| Value           | Type         | Description                            |
| --------------- | ------------ | -------------------------------------- |
| location        | string       | The location to get water data from. Refer to the list below for values. |
| refreshInterval | number       | The time interval (in hours) to refresh the data.

### Supported city/area list

| City                     | Config Value                               |
| ------------------------ | ------------------------------------------ |
| Bream Bay                | bewaterwise:whangarei:breambay             |
| Dargaville & Baylys      | bewaterwise:kaipara:dargavilleampbaylys    |
| Glinks Gully             | bewaterwise:kaipara:glinksgully            |
| Hamilton City            | smartwater:hamilton:hamilton               |
| Kaikohe / Ngawha         | bewaterwise:farnorth:kaikohengawha         |
| Kaitaia                  | bewaterwise:farnorth:kaitaia               |
| Kerikeri / Waipapa       | bewaterwise:farnorth:kerikeriwaipapa       |
| Mangapai                 | bewaterwise:whangarei:mangapai             |
| Mangawhai                | bewaterwise:kaipara:mangawhai              |
| Maungakaramea            | bewaterwise:whangarei:maungakaramea        |
| Maungaturoto             | bewaterwise:kaipara:maungaturoto           |
| Moerewa / Kawakawa       | bewaterwise:farnorth:moerewakawakawa       |
| Napier                   | napiercitycouncil:napier:napier            |
| Okaihau                  | bewaterwise:farnorth:okaihau               |
| Opononi / Omapere        | bewaterwise:farnorth:opononiomapere        |
| Rawene                   | bewaterwise:farnorth:rawene                |
| Ruawai                   | bewaterwise:kaipara:ruawai                 |
| Russell                  | bewaterwise:farnorth:russell               |
| Waipa District           | smartwater:waipa:waipa                     |
| Waikato District         | smartwater:waikato:waikato                 |
| Waitangi / Paihia / Opua | bewaterwise:farnorth:waitangipaihiaopua    |
| Whangarei                | bewaterwise:whangarei:whangarei            |

### Example

```java
Thing nzwateralerts:wateralert "HCC" [ location="smartwater:hamilton:hamilton", refreshInterval="4" ]
```

The above gets the Water Alert level for Hamilton and refreshes this data every 4 hours.

## Channels

There is only one channel with this binding labelled `alertlevel` which contains a Number 0-4 to represent the alert level.

Depending on your region, either Alert Level 0 or 1 can represent _No Water Restrictions_.
Check with your regional council for further details.

## Other Cities

At present the supported cities were implemented by scraping the web page on the respective website which contains the restriction information.

**No councils have this data in a programmatic format easily accessible to software.**
Most won't have pages which contain the current alert level and only offer alerts via twitter or text.
If you can convince your council to always have a page displaying the current alert level (even if none is in effect) then I can attempt to parse the page for inclusion in this Binding.
