# Deutsche Bahn Binding

The Deutsche Bahn Binding provides the latest timetable information for all trains that arrive or depart at a specific train station, including live information for delays and changes in timetable.
The information are requested from the timetable api of Deutsche Bahn developer portal, so you'll need a (free) developer account to use this binding.

## Supported Things

- **timetable** The timetable bridge connects to the timetable api and provides information for the next trains that will arrive or depart at the configured station.
- **train** The train thing represents one train within the configured timetable. This may be an arrival or a departure.

## Thing Configuration

### Generate Access-Key for timetable API

To configure a timetable you first need to register at Deutsche Bahn developer portal and register for timetable API to get an access key.

1. Go to [DB API Marketplace](https://developers.deutschebahn.com/)
1. Register new account or login with an existing one
1. If no application is configured yet (check Tab "Anwendungen" at top) create a new application. Only the name is required, for example openHAB, any other fields can be left blank.
1. Remember the shown **Client ID** and **Client Secret**.
1. Go to **Katalog** and search for **Timetables** api.
1. Within **Zugehörige APIs** select the Timetables api.
1. Select **Abonnieren** at top left of the page.
1. Select the Nutzungsplan **Free** by clicking **Abonnieren**.
1. Select the previously created application.
1. Click **Next**.
1. Click **Fertig**.
1. Now you have successfully registered the api within an application. The **Client ID** and **Client Secret** can now be used during bridge configuration.

### Determine the EVA-No of your station

For the selection of the station within openHAB you need the eva no. of the station.
You can look up the number within the csv file available at [Haltestellendaten](https://data.deutschebahn.com/dataset.tags.EVA-Nr..html).

### Configure timetable bridge

With access key for developer portal and eva no. of your station you're ready to configure a timetable (bridge) for this station.
In addition, you can configure if only arrivals, only departures or all trains should be contained within the timetable.

**timetable** parameters:

| Property | Default | Required | Description |
|-|-|-|-|
| `clientId` | | Yes | The Client ID for the application with registered timetable api within the DB API Marketplace. |
| `clientSecret` | | Yes | The Client Secret (API Key) for the application with registered timetable api within the DB API Marketplace. |
| `evaNo` | | Yes | The eva nr. of the train station for which the timetable will be requested.|
| `trainFilter` |  | Yes | Selects the trains that will be displayed in the timetable. Either only arrivals, only departures or all trains can be displayed. |
| `additionalFilter` | | No | Specifies additional filters for trains, that should be displayed within the timetable. |

**Additional filter**
If you only want to display certain trains within your timetable, you can specify an additional filter. This will be evaluated when loading trains,
and only trains that matches the given filter will be contained within the timetable.

To specify an advanced filter you can

- specify a filter for the value of a given channel. Therefore, you must specify the channel name (with channel group) and specify a compare value like this:
`departure#line="RE60"` this will select all trains with line RE60
- use regular expressions for expected channel values, for example:  `departure#line="RE.*"`, this will match all lines starting with "RE".
- combine multiple statements as "and" conjunction by using `&`. If used, both parts must match, for example: `departure#line="RE60" & trip#category="WFB"`
- combine multiple statements as "or" disjunction by using `|`. If used, one of the parts must match, for example: `departure#line="RE60" | departure#line="RE60"`
- use brackets to build more complex queries like `trip#category="RE" AND (departure#line="17" OR departure#line="57")`

If a channel has multiple values, like the channels `arrival#planned-path` and `departure#planned-path` have a list of stations,
only one of the values must match the given expression. So you can specify a filter like `departure#planned-path="Hannover Hbf"`
to easily display only trains that will go to Hannover Hbf.

If the filtered value is not present for a train, for example if you filter a departure attribute but train ends at the selected station,
the filter will not match.

### Configuring the trains

Once you've created the timetable you can add train-things that represent the trains within this timetable.
Each train represents one position within the timetable. For example: If you configure a train with position 1 this will be
the next train that arrives / departs at the given station. Position 2 will be the second one, and so on. If you want to
show the next 4 trains for a station, create 4 things with positions 1 to 4.

**Attention:** The timetable api only provides data for the next 18 hours. If the timetable contains less train entries than you've created
train things, the channels of these trains will be undefined.

**train** parameters:

| Property | Default | Required | Description |
|-|-|-|-|
| `position` | | Yes | The position of the train within the timetable. |

## Channels

Each train has a set of channels, that provides access to any information served by the timetable API. A detailed description of the values and their meaning can be found within
the [Timetables V1 API Description](https://developers.deutschebahn.com/db-api-marketplace/apis/product/timetables).
The information are grouped into three channel-groups:
The first channel group (trip) contains all information for the trip of the train, for example the category (like ICE, RE, S).
The second and third channel group contains information about the arrival and the departure of the train at the given station.
Both of the groups may provide an 'UNDEF' channel value, when the train does not arrive / depart at this station
(due it starts or ends at the given station). If you have configured your timetable to contain only departures (with property trainFilter) the departure channel values will always be defined
and if you have selected only arrivals the arrival channel values will always be defined.
Channels will have a 'NULL' channel value, when the corresponding attribute is not set.

Basically most information are available as planned and changed value. This allows to easy display changed values (for example the delay or changed platform).

### Channels for trip information

| channel  | type   | description                  |
|----------|--------|------------------------------|
| category | String | Provides the category of the trip, e.g. "ICE" or "RE". |
| number | String | Provides the trip/train number, e.g. "4523". |
| filter-flags | String | Provides the filter flags. |
| trip-type | String | Provides the type of the trip. |
| owner | String | Provides the owner of the train. A unique short-form and only intended to map a trip to specific evu (EisenbahnVerkehrsUnternehmen). |

### Channels for arrival / departure

| channel  | type   | description                  |
|----------|--------|------------------------------|
| planned-path  | String | Provides the planned path of a train. |
| changed-path  | String | Provides the changed path of a train. |
| planned-platform  | String | Provides the planned platform of a train.  |
| changed-platform | String | Provides the changed platform of a train. |
| planned-time | DateTime | Provides the planned time of a train. |
| changed-time | DateTime | Provides the changed time of a train. |
| planned-status | String | Provides the planned status (planned, added, cancelled) of a train. |
| changed-status | String | Provides the changed status (planned, added, cancelled) of a train. |
| cancellation-time | DateTime | Time when the cancellation of this stop was created. |
| line | String | The line of the train.  |
| messages | String | Messages for this train. Contains all translated codes from the messages of the selected train stop. Multiple messages will be separated with a single dash. |
| hidden | Switch | On if the event should not be shown because travellers are not supposed to enter or exit the train at this stop. |
| wings | String | A sequence of trip id separated by pipe symbols. |
| transition | String | Trip id of the next or previous train of a shared train. At the start stop this references the previous trip, at the last stop it references the next trip. |
| planned-distant-endpoint | String | Planned distant endpoint of a train. |
| changed-distant-endpoint | String | Changed distant endpoint of a train. |
| distant-change | Number | Distant change |
| planned-final-station | String | Planned final station of the train. For arrivals the starting station is returned, for departures the target station is returned. |
| planned-intermediate-stations | String | Returns the planned stations this train came from (for arrivals) or the stations this train will go to (for departures). Stations will be separated by single dash. |
| changed-final-station | String | Changed final station of the train. For arrivals the starting station is returned, for departures the target station is returned.  |
| changed-intermediate-stations | String | Returns the changed stations this train came from (for arrivals) or the stations this train will go to (for departures). Stations will be separated by single dash. |

## Full Example

timetable.things

```java
Bridge deutschebahn:timetable:timetableLehrte "Fahrplan Lehrte" [ accessToken="XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", trainFilter="departures", evaNo="8000226" ] {
  Thing deutschebahn:train:timetableLehrte:lehrteZug1 "Zug 1" [ position="1" ]
  Thing deutschebahn:train:timetableLehrte:lehrteZug2 "Zug 2" [ position="2" ]
}
```

timetable.items

```java
// Groups
Group zug1 "Zug 1"
Group zug1Fahrt "Zug 1 Fahrt" (zug1)
Group zug1Ankunft "Zug 1 Ankunft" (zug1)
Group zug1Abfahrt "Zug 1 Abfahrt" (zug1)

// Trip Information
String Zug1_Trip_Category "Kategorie" (zug1Fahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:trip#category"}
String Zug1_Trip_Number "Nummer" (zug1Fahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:trip#number"}
String Zug1_Trip_FilterFlags "Filter" (zug1Fahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:trip#filter-flags"}
String Zug1_Trip_TripType "Fahrttyp" (zug1Fahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:trip#trip-type"}
String Zug1_Trip_Owner "Unternehmen" (zug1Fahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:trip#owner"}


// Arrival Information
DateTime Zug1_Arrival_Plannedtime "Geplante Zeit" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#planned-time"}
DateTime Zug1_Arrival_Changedtime "Geänderte Zeit" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#changed-time"}
String Zug1_Arrival_Plannedplatform "Geplantes Gleis" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#planned-platform"}
String Zug1_Arrival_Changedplatform "Geändertes Gleis" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#changed-platform"}
String Zug1_Arrival_Line "Linie" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#line"}
String Zug1_Arrival_Plannedintermediatestations "Geplante Halte" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#planned-intermediate-stations"}
String Zug1_Arrival_Changedintermediatestations "Geänderte Halte" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#changed-intermediate-stations"}
String Zug1_Arrival_Plannedfinalstation "Geplanter Start-/Zielbahnhof" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#planned-final-station"}
String Zug1_Arrival_Changedfinalstation "Geänderter Start-/Zielbahnhof" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#changed-final-station"}
String Zug1_Arrival_Messages "Meldungen" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#messages"}
String Zug1_Arrival_Plannedstatus "Geplanter Status" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#planned-status"}
String Zug1_Arrival_Changedstatus "Geänderter Status" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#changed-status"}
DateTime Zug1_Arrival_Cancellationtime "Stornierungs-Zeitpunkt" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#cancellation-time"}

// Arrival advanced information
String Zug1_Arrival_Planneddistantendpoint "Geplanter entfernter Endpunkt" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#planned-distant-endpoint"}
String Zug1_Arrival_Changeddistantendpoint "Geänderter entfernter Endpunkt" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#changed-distant-endpoint"}
String Zug1_Arrival_Plannedpath "Geplante Route" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#planned-path"}
String Zug1_Arrival_Changedpath "Geändert Route" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#changed-path"}
Number Zug1_Arrival_Distantchange "Geänderter Zielbahnhof" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#distant-change"}
Switch Zug1_Arrival_Hidden "Versteckt" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#hidden"}
String Zug1_Arrival_Transition "Übergang" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#transition"}
String Zug1_Arrival_Wings "Wings" (zug1Ankunft) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:arrival#wings"}

// Departure Information
DateTime Zug1_Departure_Plannedtime "Geplante Zeit" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#planned-time"}
DateTime Zug1_Departure_Changedtime "Geänderte Zeit" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#changed-time"}
String Zug1_Departure_Plannedplatform "Geplantes Gleis" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#planned-platform"}
String Zug1_Departure_Changedplatform "Geändertes Gleis" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#changed-platform"}
String Zug1_Departure_Line "Linie" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#line"}
String Zug1_Departure_Plannedintermediatestations "Geplante Halte" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#planned-intermediate-stations"}
String Zug1_Departure_Changedintermediatestations "Geänderte Halte" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#changed-intermediate-stations"}
String Zug1_Departure_Plannedfinalstation "Geplanter Start-/Zielbahnhof" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#planned-final-station"}
String Zug1_Departure_Changedfinalstation "Geänderter Start-/Zielbahnhof" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#changed-final-station"}
String Zug1_Departure_Messages "Meldungen" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#messages"}
String Zug1_Departure_Plannedstatus "Geplanter Status" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#planned-status"}
String Zug1_Departure_Changedstatus "Geänderter Status" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#changed-status"}
DateTime Zug1_Departure_Cancellationtime "Stornierungs-Zeitpunkt" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#cancellation-time"}

// Departure advanced information
String Zug1_Departure_Planneddistantendpoint "Geplanter entfernter Endpunkt" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#planned-distant-endpoint"}
String Zug1_Departure_Changeddistantendpoint "Geänderter entfernter Endpunkt" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#changed-distant-endpoint"}
String Zug1_Departure_Plannedpath "Geplante Route" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#planned-path"}
String Zug1_Departure_Changedpath "Geändert Route" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#changed-path"}
Number Zug1_Departure_Distantchange "Geänderter Zielbahnhof" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#distant-change"}
Switch Zug1_Departure_Hidden "Versteckt" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#hidden"}
String Zug1_Departure_Transition "Übergang" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#transition"}
String Zug1_Departure_Wings "Wings" (zug1Abfahrt) {channel="deutschebahn:train:timetableLehrte:lehrteZug1:departure#wings"}

```

Example widget for displaying train details

```yaml
uid: timetable_train_details
tags:
  - card
props:
  parameters:
    - context: item
      label: Geplante Zeit
      name: planned_time
      required: true
      type: TEXT
    - context: item
      label: Geänderte Zeit
      name: changed_time
      required: true
      type: TEXT
    - context: item
      label: Geplantes Gleis
      name: planned_platform
      required: true
      type: TEXT
    - context: item
      label: Geändertes Gleis
      name: changed_platform
      required: true
      type: TEXT
    - context: item
      label: Linie
      name: line
      required: true
      type: TEXT
    - context: item
      label: Meldungen
      name: messages
      required: true
      type: TEXT
    - context: item
      label: Geplanter Start-/Zielbahnhof
      name: planned_final_station
      required: true
      type: TEXT
    - context: item
      label: Geplante Halte
      name: planned_intermediate_stations
      required: true
      type: TEXT
    - context: item
      label: Geändeter Start-/Zielbahnhof
      name: changed_final_station
      required: true
      type: TEXT
    - context: item
      label: Geänderte Halte
      name: changed_intermediate_stations
      required: true
      type: TEXT
    - context: item
      label: Geänderter Status
      name: changed_state
      required: true
      type: TEXT
    - context: item
      label: Kategorie
      name: category
      required: true
      type: TEXT
    - context: item
      label: Nummer
      name: number
      required: true
      type: TEXT
  parameterGroups: []
timestamp: Oct 14, 2021, 11:24:45 AM
component: f7-card
config:
  style:
    padding: 10px
slots:
  default:
    - component: f7-row
      slots:
        default:
          - component: f7-col
            config:
              width: 15
            slots:
              default:
                - component: Label
                  config:
                    text: "=items[props.planned_time].displayState + (items[props.changed_time].state != 'NULL' && items[props.changed_time].state != items[props.planned_time].state ? ' (' + items[props.changed_time].displayState + ')' : '')"
                    style:
                      color: "=items[props.changed_time].state != 'NULL' && items[props.changed_time].state != items[props.planned_time].state ? 'red' : ''"
          - component: f7-col
            config:
              width: 75
            slots:
              default:
                - component: Label
                  config:
                    text: "=(items[props.changed_state].state == 'c' ? 'Zug fällt aus - ' : '') + (items[props.messages].state != 'NULL' ? items[props.messages].state : '')"
                    style:
                      color: red
          - component: f7-col
            config:
              width: 10
            slots:
              default:
                - component: Label
                  config:
                    text: "=items[props.changed_platform].state != 'NULL' ? items[props.changed_platform].state :  items[props.planned_platform].state"
                    style:
                      color: "=items[props.changed_platform].state != 'NULL' ? 'red' : ''"
                      text-align: right
    - component: f7-row
      slots:
        default:
          - component: f7-col
            config:
              width: 15
            slots:
              default:
                - component: Label
                  config:
                    text: "=items[props.line].state != 'NULL' ? (items[props.category].state + ' ' + items[props.line].state) : (items[props.category].state + ' ' + items[props.number].state)"
          - component: f7-col
            config:
              width: 50
            slots:
              default:
                - component: Label
                  config:
                    text: "=items[props.changed_intermediate_stations].state != 'NULL' ? items[props.changed_intermediate_stations].state : items[props.planned_intermediate_stations].state"
                    style:
                      color: "=items[props.changed_intermediate_stations].state != 'NULL' ? 'red' : ''"
          - component: f7-col
            config:
              width: 35
            slots:
              default:
                - component: Label
                  config:
                    text: "=items[props.changed_final_station].state != 'NULL' ? items[props.changed_final_station].state : items[props.planned_final_station].state"
                    style:
                      color: "=items[props.changed_final_station].state != 'NULL' ? 'red' : ''"
                      font-weight: bold
                      text-align: right
```
