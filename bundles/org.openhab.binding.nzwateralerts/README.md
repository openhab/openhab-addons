# NZ Water Alerts Binding

Get Water Alert Levels for cities in New Zealand. Getting this alert level can help you script and automate smarter tasks. 

> Example: Disable automated spinklers based on a level 3 or 4 water alert level

_This Binding scrapes the [Smart Water](http://www.smartwater.org.nz/) and [Be Water Wise](https://bewaterwise.org.nz/) websites for Water Alert Levels._

## Thing Configuration

You can configure this Binding through _PaperUI_ or manually.

```
Thing nzwateralerts:wateralert "HCC" [ location="smartwater:hamilton:hamilton", refreshInterval="4" ]
```

The above gets the Water Alert level for Hamilton and refreshes this data every 4 hours.

Supported city/area list:

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

## Channels

There is only one channel with this binding labelled `alertlevel` which contains a Number 0-4 to represent the alert level.

> Water Alert Level 0 represents _No Water Restrictions_ in some cities.

## Other Cities

At present the supported cities were implemented by scraping the web page on the respective website which contains the restriction information. 

**No councils have this data in a programmatic format easily accessible to software.** Most won't have pages which contain the current alert level and only offer alerts via twitter or text. If you can convince your council to always have a page displaying the current alert level (even if none is in effect) then I can attempt to parse the page for inclusion in this Binding.
