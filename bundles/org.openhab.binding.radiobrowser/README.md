# Radio Browser Binding

Radio Browser is a community driven database of internet radio and TV stations, that has an open API that is free for all to use and has multiple servers to ensure up time is high.
With this binding you can use their database of more than 45,000 stations to apply filters and quickly find internet radio streams that interest you.

## Supported Things

- `radio`: Add one of these manually, and it should come online after fetching language and country data to populate the filter channels.

### `radio` Thing Configuration

| Name          | Type    | Description                                                                                | Default  | Required | Advanced |
|---------------|---------|--------------------------------------------------------------------------------------------|----------|----------|----------|
| filters       | text    | Allows you to specify your own filters from the `advanced search` of the API.          | See below | yes     | no       |
| clicks        | boolean | Helps to support the server recommend good results.                                        | true     | yes      | yes      |
| languageCount | integer | If you want less languages to be shown as a filter, you can raise this or create your own. | 14       | yes      | yes      |
| recentLimit   | integer | Limit the number of stations in the recent channel list. `0` Disables the feature.        | 5        | yes      | no       |

## Filters Configuration

The `advanced` configuration parameter `filters` can be used to limit the stations based on different fields like codecs, minimum quality, ordering and more.
All possible filter options are listed here <https://de1.api.radio-browser.info/#Advanced_station_search>
The default filter (below) can be changed to suit your needs.

```text
hidebroken=true,limit=1700,reverse=true,order=votes
```

You can also try out the various search features on their main website, and then copy what is added to the address bar of your web browser.
<https://www.radio-browser.info/>

## State Option Metadata

If you wish to display only a couple of languages or custom choices to any of the filters, you can create your own using metadata>state options.
The countries need to be the country code not the full name, for example `US` and not `The United States of America`.
The binding will auto select your country based on openHAB's settings that you made when setting up openHAB.
It makes sense to do this for languages if the built in way of `languageCount` does not work for your use case.
Genres are a good example for using the metadata, only show the styles of music and other tags that you like.
If in doubt you can use the [Event Monitor in the Developer Sidebar](https://www.openhab.org/docs/tutorial/tips-and-tricks.html#event-monitor) to watch what commands are sent to the bindings channels.

## Channels

| Channel   | Type   | Read/Write | Description                                                                                                 |
|-----------|--------|------------|-------------------------------------------------------------------------------------------------------------|
| country   | String | RW         | This allows you to only find stations in ALL or a country of your choice.                                   |
| state     | String | RW         | When a country is selected, this will auto populate with states that are in your country.                   |
| language  | String | RW         | You can limit the stations to only be in your language, or you can also use Metadata to set your own list.  |
| genre     | String | RW         | A list of common genres to help you find a station you like. State Options Metadata allows you to change this. |
| station   | String | RW         | These are the search results back from the database that match your filter settings.                        |
| stream    | String | RW         | This is the URL for the selected station.                                                                   |
| name      | String | RW         | This is the name of the selected station.                                                                   |
| icon      | String | RW         | This is the icon for the selected station if available in their database.                                   |
| recent    | String | RW         | Records the last stations you selected to make them easier to find. A config allows this list to be longer. |

## Using the Stream URL

You can send the `stream` channel that holds a URL for a stream to the `playuri` channel of the [Chromecast Binding](https://www.openhab.org/addons/bindings/chromecast/#channels) or the `stream` channel of the [Squeezebox Binding](https://www.openhab.org/addons/bindings/squeezebox/#player-channels).
To make this easier without needing to setup a rule to forward the stream, you can use this [widget found in the marketplace](https://community.openhab.org/t/radio-browser-basic-widget-for-finding-internet-radio-streams-with-the-ui/153783) without needing to create any rules.
The widget is probably the easiest way to get started and have a play with what is possible.

## Station Searches

Searches can be done in a few different ways and since the binding will auto select the first result, you can change what is sent to the top of the list by changing the filters config from including `order=clickcount` (default) to `votes`, `clicktrend` or even `random`.

Examples on how to do searches from rules, or you can also change an item to take input by using `oh-input-item` using metadata called `Default list item widget`.

Search for all stations that contain `hit` in their name, and auto select the first result.

```java
Radio_Station.sendCommand("hit")
```

Search and auto select the station if you know the UUID from the website.

```java
Radio_Station.sendCommand("b6a490e8-f498-4a7c-b024-607b3d997614")
```

Clear any manual search results using the above two methods, and `REFRESH` back to using the normal filter channels.

```java
Radio_Station.sendCommand(REFRESH)
```
