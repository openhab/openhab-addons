# Freecurrency Binding

The Freecurrency binding connects [Freecurrency API](https://freecurrencyapi.com) to openHAB.
It allows to get exchange rates between supported currencies and acts as a currency provider for openHAB's UoM support.

The binding automatically updates the exchange rates at 00:01 UTC.
There is a limit of 10 (5.000) free request per minute (month), so a daily refresh (and even some restarts per day) will not get you into trouble.

## Supported Things

There is only one thing: `info` which is extensible with exchange rate channels.
You can add as many of these things as you like, but in general one should be sufficient for most use-cases.

## Binding Configuration

The binding has two configuration parameters: `apiKey` and `baseCurrency`.

The `apiKey` is mandatory and can be retrieved from your dashboard after creating a free account at [Freecurrency API website](https://app.freecurrencyapi.com/login).

The `baseCurrency` defaults to US dollars (`USD`), but can be configured to any other supported currency.
Available currencies are provided as configuration options.
Please note that misconfiguration will result in no exchanges rates being provided.

## Thing Configuration

### `info` Thing Configuration

The thing has no configuration options and is automatically attached to the currency provider.

## Channels

| Channel        | Channel Type  | Item Type | Read/Write | Description                                                                                  |
|----------------|---------------|-----------|------------|----------------------------------------------------------------------------------------------|
| lastUpdate     | last-update   | DateTime  | R/O        | The timestamp of the last exchange rate refresh                                              |
| <user defined> | exchange-rate | Number    | R/O        | The exchange rate between the configured currency and the base currency (or second currency) |

The `exchange-rate` channels have two configuration parameters: `currency1` and `currency2`.
Any currency code can be configured for both parameters, but only `currency1` is mandatory.
If you omit `currency2`, the configured base-currency will be used as reference.
Available currencies are provided as configuration options.
