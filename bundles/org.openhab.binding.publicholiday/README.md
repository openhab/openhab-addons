# PublicHoliday Binding

This binding is used to provide a public holiday channel.

Using this binding, it is possible to influence rules to handle differently,
if the current day is a public holiday

## Supported Things

A public holiday thing

## Discovery

None

## Thing Configuration

Using the configuration specifies, when the update of the value should be done and which public holidays should be taken
into account.


| Name            | Type    | Description                                                      |     Default    | Required |
|-----------------|---------|------------------------------------------------------------------|----------------|----------|
| updateValueCron | text    | Specifies the update time. Should be called once a day           | "05 0 * * * *" | true     |
| $PUBLIC_HOLIDAY | boolean | True to take this value into account. Unused days can be omitted | false          | false    |

Replace `$PUBLIC_HOLIDAY` with the wanted public holidays. Check the supported list of public holidays for the actual values.

`updateValueCron` should run shortly after midnight

## Channels

| Channel                  | Type   | Read/Write | Description                              |
|--------------------------|--------|------------|------------------------------------------|
| isPublicHoliday          | Switch | R          | The current day is a public holiday      |
| isDayBeforePublicHoliday | Switch | R          | The next day will be a public holiday    |
| holidayName              | Text   | R          | The name of the public holiday or "none" |

The value of the channels is updated at 0:05 AM per default.

To ensure the value is valid on startup, the values are set within the startup process, too.

## Full Example

A short example, how to use the binding.

publicHoliday.thing:

```xtend
publicholiday:publicHoliday:pubicHoliday "Public Holiday" [
	updateValueCron="05 0 * * * *",
	
	newYear="true",
	christmasEve="false",
	christmasDay="true",
	secondChristmasDay="true",
	newYearsEve="true",
]
```

publicHoliday.items:

```xtend
Switch isPublicHoliday "Holiday - Public" { channel = "publicholiday:publicHoliday:pubicHoliday:isPublicHoliday" }
```

## Supported list of public holidays

Currently the following public holidays are supported.
Use their names as written within the configuration to activate them.

### General
* newYear (01/01)
* threeKingsDay (01/06)
* reformationDay (10/31)
* allSaintsDay (11/01)
* christmasEve (12/24)
* christmasDay (12/25)
* secondChristmasDay (12/26)
* newYearsEve (12/31)

### Eastern related
* goodFriday
* easterSunday
* easterMonday
* ascensionDay
* whitSunday
* whitMonday
* corpusChristi

### Country specific
#### Germany
* tagDerArbeit (05/01)
* tagDerDeutschenEinheit (10/03)

## Missing public holiday values

Likely many public holidays are missing. Feel free to add them via a pull
request or leave the required information to add it accordingly
