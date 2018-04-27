# Ephemeris Binding

This binding adds ephemeris capabilities to openHab. 

## Introduction

With the Ephemeris biding, your openHab setup will be aware
of general day of the year events.


## Supported Things

Currently supported things include:

* holiday : Holidays for many countries of the world (provided by included Jollyday project).
* sotd : Saint of the day (currently only France available)
* userfile : Describe your own calendar events

### User Defined calendars

An example of user calendar defined file is provided below.
This file must follow the format defined in the JollyDay project.
The XSD is available here : http://jollyday.sourceforge.net/schema.html
Many examples are available on the Jollyday website : http://jollyday.sourceforge.net/index.html

## Binding configuration

No configuration required for the binding

## Thing configuration

#### Manual Thing Creation

Devices can be manually created in the *PaperUI* or *HABmin*, or by placing a *.things* file in the *conf/things* directory.  See example below.

All Things accepts the optional parameter `offset`, 0 by default. +1 means tomorrow, -1 yesterday ...

For Saint of the Day and Holydays, you can specify the `country` (two letters ISO3166 code). If no value is provided system default will be used.

Optional, for certain countries, you can define sharpen the definition with region and city codes - according to those defined and available at http://jollyday.sourceforge.net/names_country_fr.html.
 
## Channels

The Holyday thing :

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|------------- |
| eventName | String       | The name of the Holiday or null |
| eventDate | DateTime      | The observed date |
| isOfficial | Switch       | If the holiday is official or not |

The Saint of The Day thing :

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|------------- |
| eventName | String       | The name of the Saint  |
| eventDate | DateTime      | The observed date |

The User defined thing :

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|------------- |
| eventName | String       | The name of the event  |
| eventDate | DateTime      | The event date |

## Commands

The binding does not handle commands.

### Manual Thing Creation

Place a file named *ephemeris.things* in the *conf/things* directory.  The file should contain lines formatted like this.

## Examples

ephemeris.things:

```
ephemeris:userfile:home [ filename="/home/sysadmin/Bureau/ordures_menageres.xml" ]
ephemeris:holiday:home [ country="fr", offset=0 ]
ephemeris:sotd:tomorrow [ country="fr", offset=1 ]
```

ephemeris.items:
```
Switch   HolidayOfficial    "Official"              { channel="ephemeris:holiday:home:isOfficial" }
DateTime HolidayDate        "Date [%1$tH:%1$tM]"    { channel="ephemeris:holiday:home:eventDate" }
String   HolidatName        "Name [%s]"             { channel="ephemeris:holiday:home:eventName" }
```

A user defined Calendar :
ordures_menageres.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<tns:Configuration hierarchy="fr" description="France"
    xmlns:tns="http://www.example.org/Holiday" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.example.org/Holiday /Holiday.xsd">
    <tns:Holidays>
        <tns:FixedWeekday which="THIRD" weekday="THURSDAY" month="JANUARY" descriptionPropertiesKey="Encombrants"/>
        <tns:FixedWeekday which="THIRD" weekday="THURSDAY" month="FEBRUARY" descriptionPropertiesKey="Encombrants"/>
        <tns:FixedWeekday which="THIRD" weekday="THURSDAY" month="MARCH" descriptionPropertiesKey="Encombrants"/>
        <tns:FixedWeekday which="THIRD" weekday="THURSDAY" month="APRIL" descriptionPropertiesKey="Encombrants"/>
        <tns:FixedWeekday which="THIRD" weekday="THURSDAY" month="MAY" descriptionPropertiesKey="Encombrants"/>
        <tns:FixedWeekday which="THIRD" weekday="THURSDAY" month="JUNE" descriptionPropertiesKey="Encombrants"/>
        <tns:FixedWeekday which="THIRD" weekday="THURSDAY" month="JULY" descriptionPropertiesKey="Encombrants"/>
        <tns:FixedWeekday which="THIRD" weekday="THURSDAY" month="AUGUST" descriptionPropertiesKey="Encombrants"/>
        <tns:FixedWeekday which="THIRD" weekday="THURSDAY" month="SEPTEMBER" descriptionPropertiesKey="Encombrants"/>
        <tns:FixedWeekday which="THIRD" weekday="THURSDAY" month="OCTOBER" descriptionPropertiesKey="Encombrants"/>
        <tns:FixedWeekday which="THIRD" weekday="THURSDAY" month="NOVEMBER" descriptionPropertiesKey="Encombrants"/>
        <tns:FixedWeekday which="THIRD" weekday="THURSDAY" month="DECEMBER" descriptionPropertiesKey="Encombrants"/>
    </tns:Holidays>
</tns:Configuration>
```
