# AirParif Binding

This binding uses the [AirParif service](https://www.airparif.fr/) for providing air quality information for Paris and departments of the Ile-de-France.
To use it, you first need to [register and get your API key](https://www.airparif.fr/interface-de-programmation-applicative).
You'll receive your API Key by mail.

## Supported Things

- `api`: bridge used to connect to the AirParif service. Provides some general informations for the whole area.
- `location`: Presents the pollen and air quality information for a given location.

Of course, you can add multiple `location`s, e.g. for gathering pollen or air quality data for different locations.

## Discovery

Once your `api` bridge is created and configured with the API Key, a default `location` can be auto-discovered based on system location. 
It will be configured with the system location and detected department.

## Thing Configuration

### `api` Thing Configuration

| Name            | Type    | Description                       | Default | Required | Advanced |
|-----------------|---------|-----------------------------------|---------|----------|----------|
| apikey          | text    | Token used to access the service  | N/A     | yes      | no       |

### `location` Thing Configuration

| Name            | Type    | Description                                                                    | Default | Required | Advanced |
|-----------------|---------|--------------------------------------------------------------------------------|---------|----------|----------|
| location        | text    | Geo coordinates to be considered (as <latitude>,<longitude>[,<altitude in m>]) | N/A     | yes      | no       |
| department      | text    | Code of the department (two digits) (*)                                        | N/A     | yes      | no       |

(*) When auto-discovered, the department will be pre-filled based on the location and bounding limits defined in the internal department database.
Please check that proposed value is correct according to the place.

## Channels

### `api` Thing Channels

| Group                | Channel        | Type           | Read/Write | Description                                  |
|----------------------|----------------|----------------|------------|----------------------------------------------|
| pollens              | comment        | String         | R          | Current pollens situation                    |
| pollens              | begin-validity | DateTime       | R          | Bulletin validity start                      |
| pollens              | end-validity   | DateTime       | R          | Bulletin validity end                        |
| aq-bulletin          | comment        | String         | R          | General message for the air quality bulletin |
| aq-bulletin          | no2-min        | Number:Density | R          | Minimum level of NO2 concentration           |
| aq-bulletin          | no2-max        | Number:Density | R          | Maximum level of NO2 concentration           |
| aq-bulletin          | o3-min         | Number:Density | R          | Minimum level of O3 concentration            |
| aq-bulletin          | o3-max         | Number:Density | R          | Maximum level of O3 concentration            |
| aq-bulletin          | pm10-min       | Number:Density | R          | Minimum level of PM 10 concentration         |
| aq-bulletin          | pm10-max       | Number:Density | R          | Maximum level of PM 10 concentration         |
| aq-bulletin          | pm25-min       | Number:Density | R          | Minimum level of PM 2.5 concentration        |
| aq-bulletin          | pm25-max       | Number:Density | R          | Maximum level of PM 2.5 concentration        |
| aq-bulletin-tomorrow | comment        | String         | R          | General message for the air quality bulletin |
| aq-bulletin-tomorrow | no2-min        | Number:Density | R          | Minimum level of NO2 concentration           |
| aq-bulletin-tomorrow | no2-max        | Number:Density | R          | Maximum level of NO2 concentration           |
| aq-bulletin-tomorrow | o3-min         | Number:Density | R          | Minimum level of O3 concentration            |
| aq-bulletin-tomorrow | o3-max         | Number:Density | R          | Maximum level of O3 concentration            |
| aq-bulletin-tomorrow | pm10-min       | Number:Density | R          | Minimum level of PM 10 concentration         |
| aq-bulletin-tomorrow | pm10-max       | Number:Density | R          | Maximum level of PM 10 concentration         |
| aq-bulletin-tomorrow | pm25-min       | Number:Density | R          | Minimum level of PM 2.5 concentration        |
| aq-bulletin-tomorrow | pm25-max       | Number:Density | R          | Maximum level of PM 2.5 concentration        |
| daily                | message        | String         | R          | Today's daily general information            ||| daily                | tomorrow       | String         | R          | Tomorrow's daily general information         |

### `location` Thing Channels

| Group   | Channel    | Type           | Read/Write | Description                                              |
|---------|------------|----------------|------------|----------------------------------------------------------|
| pollens | cypress    | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | hazel      | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | alder      | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | poplar     | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | willow     | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | ash        | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | hornbeam   | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | birch      | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | plane      | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | oak        | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | olive      | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | linden     | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | chestnut   | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | rumex      | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | grasses    | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | plantain   | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | urticaceae | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | wormwood   | Number         | R          | Alert level associated to this taxon (*)                 |
| pollens | ragweed    | Number         | R          | Alert level associated to this taxon (*)                 |
| indice  | message    | String         | R          | Alert message associated to the value of the index       |
| indice  | timestamp  | DateTime       | R          | Timestamp of the evaluation                              |
| indice  | alert      | Number         | R          | ATMO Index associated to highest pollutant concentration |
| o3      | message    | String         | R          | Polllutant concentration alert message                   |
| o3      | value      | Number:Density | R          | Concentration of the given pollutant                     |
| o3      | alert      | Number         | R          | Alert Level associated to pollutant concentration (**)   |
| no2     | message    | String         | R          | Polllutant concentration alert message                   |
| no2     | value      | Number:Density | R          | Concentration of the given pollutant                     |
| no2     | alert      | Number         | R          | Alert Level associated to pollutant concentration (**)   |
| pm25    | message    | String         | R          | Polllutant concentration alert message                   |
| pm25    | value      | Number:Density | R          | Concentration of the given pollutant                     |
| pm25    | alert      | Number         | R          | Alert Level associated to pollutant concentration (**)   |
| pm10    | message    | String         | R          | Polllutant concentration alert message                   |
| pm10    | value      | Number:Density | R          | Concentration of the given pollutant                     |
| pm10    | alert      | Number         | R          | Alert Level associated to pollutant concentration (**)   |

(*) Each pollen alert level has an associated color and description:

| Code | Color  | Description           |
|------|--------|-----------------------|
| 0    | Green  | No allergic risk      |
| 1    | Yellow | Low allergic risk     |
| 2    | Orange | Average allergic risk |
| 3    | Red    | High allergic risk    |

(*) Each pollutant concentration is associated to an alert level (and an icon) :

| Code | Description   |
|------|---------------|
| 0    | Good          |
| 1    | Average       |
| 2    | Degrated      |
| 3    | Bad           |
| 4    | Very Bad      |
| 5    | Extremely Bad |

## Provided icon set

This binding has its own IconProvider and makes available the following list of icons

| Icon Name              | Dynamic | Illustration |
|------------------------|---------|--------------|
| oh:airparif:aq         |   Yes   | ![](doc/images/aq.svg) |
| oh:airparif:alder      |   Yes   | ![](doc/images/alder.svg) |
| oh:airparif:ash        |   Yes   | ![](doc/images/ash.svg) |
| oh:airparif:birch      |   Yes   | ![](doc/images/birch.svg) |
| oh:airparif:chestnut   |   Yes   | ![](doc/images/chestnut.svg) |
| oh:airparif:cypress    |   Yes   | ![](doc/images/cypress.svg) |
| oh:airparif:grasses    |   Yes   | ![](doc/images/grasses.svg) |
| oh:airparif:hazel      |   Yes   | ![](doc/images/hazel.svg) |
| oh:airparif:hornbeam   |   Yes   | ![](doc/images/hornbeam.svg) |
| oh:airparif:linden     |   Yes   | ![](doc/images/linden.svg) |
| oh:airparif:oak        |   Yes   | ![](doc/images/oak.svg) |
| oh:airparif:olive      |   Yes   | ![](doc/images/olive.svg) |
| oh:airparif:plane      |   Yes   | ![](doc/images/plane.svg) |
| oh:airparif:plantain   |   Yes   | ![](doc/images/plantain.svg) |
| oh:airparif:pollen     |   Yes   | ![](doc/images/pollen.svg) |
| oh:airparif:poplar     |   Yes   | ![](doc/images/poplar.svg) |
| oh:airparif:ragweed    |   Yes   | ![](doc/images/ragweed.svg) |
| oh:airparif:rumex      |   Yes   | ![](doc/images/rumex.svg) |
| oh:airparif:urticaceae |   Yes   | ![](doc/images/urticaceae.svg) |
| oh:airparif:willow     |   Yes   | ![](doc/images/willow.svg) |
| oh:airparif:wormwood   |   Yes   | ![](doc/images/wormwood.svg) |


## Full Examplee

### Thing Configurationn

```jav
Bridge airparif:api:local "AirParif" [ apikey="xxxxx-dddd-cccc-4321-zzzzzzzzzzzzz" ] {
    location yvelines "Yvelines" [ department="78", location="52.639,1.8284" ]
}a
```
### Item Configurationn

```java
Example item configuration goes here.
``

