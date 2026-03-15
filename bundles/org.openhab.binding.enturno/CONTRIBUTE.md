# How can you contribute?

This binding is meant to get Norwegian public transport real-time (estimated) data from [Entur.no API](https://developer.entur.org/content/journey-planner-0).

It runs GraphQL-API query against the service.

Entur provides an [IDE for the API](https://api.entur.org/doc/shamash-journeyplanner/) (use query from GraphQL request example below as a start point) Ctrl+Space gives hints of valid values

There are several possibilities to get and process data from endpoints that entur.no provides. One of possible examples is to get all departures for all lines from specified stop place (all directions) or even specified quay (specified direction). Those are not supported yet so feel free to extend functionality of this binding.

## GraphQL request example

```json
{
  stopPlace(id: "NSR:StopPlace:30848") {
    id
    name
    transportMode
    estimatedCalls(startTime:"2019-04-06T10:00:00+01:00" timeRange: 86400, numberOfDepartures: 400) {
      realtime
      aimedArrivalTime
      aimedDepartureTime
      expectedArrivalTime
      expectedDepartureTime
      date
      forBoarding
      forAlighting
      destinationDisplay {
        frontText
      }
      quay {
        id
        publicCode
      }
      serviceJourney {
        journeyPattern {
          line {
            id
            name
            transportMode
            publicCode
          }
        }
      }
    }
  }
}
```
