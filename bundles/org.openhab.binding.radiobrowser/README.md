# Radio Browser Binding

Radio Browser is a community driven database of internet radio and TV stations, that has an open API that is `free for ALL` to use and has multiple servers to ensure up time is high.
With this binding you can use their database of more than 45,000 stations to apply filters and quickly find internet radio streams that interest you.

## Supported Things

- `radio`: Add one of these manually, and it should come online after fetching language and country data to populate the filter channels.

### `radio` Thing Configuration

| Name          | Type    | Description                                                                                 | Default | Required | Advanced |
|---------------|---------|---------------------------------------------------------------------------------------------|---------|----------|----------|
| filters       | text    | Allows you to specify your own filters from the `advanced search` of the API.           | `?hidebroken=true&limit=1700&order=clickcount`                                                                          | yes      | no      |
| clicks        | boolean | Helps to support the server recommend good results.                                         | true    | yes      | yes      |
| languageCount | integer | If you want less languages to be shown as a filter, you can raise this or create your own.  | 14      | yes      | yes      |

## Filters Configuration

Found by ticking the `show advanced` box, this configuration called `filters` can be used to limit the stations based on codecs, minimum quality, ordering and more.
The default filter is this and can be change to create better results for your needs.

```
?hidebroken=true&limit=1700&order=clickcount
```

To give an example on how to show less stations by removing any low quality streams below 128kbps (FM radio quality), this could be added.
You may not want to do this if you're interested in chat and sports channels.

```
?hidebroken=true&limit=1700&order=clickcount&bitrateMin=128
```

If you want to create your own custom filter, the options are listed here <https://de1.api.radio-browser.info/#Advanced_station_search>

## State Option Metadata

If you wish to display only a couple of languages or choice to any of the filters, you can create your own using metadata>state options.
The countries need to be the country code not the full name, for example US and not `The United States of America`.
The binding will auto select your country based on openHAB's settings you should have already made when setting up openHAB.
It makes sense to do this for languages if the built in way of `languageCount` does not work for your use case.
Genres are a good example for using the metadata, only show the styles of music and other tags that you like.

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

## Station Searches

Update the last search if the filters are not changed.

```
Radio_Station.sendCommand(REFRESH)
```

Search for all stations that contain `hit` in their name, and auto select the first result.
All results are available to be selected, should the wrong one get sent to the top of the list based on clicks or vote counts.

```
Radio_Station.sendCommand("hit")
```

Search and auto select the station if you know the UUID from the website.

```
Radio_Station.sendCommand("962cc6df-0601-11e8-ae97-52543be04c81")
```

## Show Your Thanks

If you enjoy the binding, then please consider sponsoring or giving a once off tip as a thank you via the links.
This allows me to purchase software and hardware to contribute more bindings, and also shows this binding should gain more feature like favourites.
Some coffee to keep me coding faster never hurts :slight_smile:

Sponsor @Skinah on GitHub
<https://github.com/sponsors/Skinah/>

Paypal can also be used via
matt A-T pcmus D-O-T C-O-M
