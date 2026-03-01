# Energy Forecast Binding

Binding provides energy information & pricing from [Energy Forecast Provider](https://www.energyforecast.de/).
Check in beforehand if your [price zone](https://www.energyforecast.de/api-docs/index.html) is supported.
[Registration](https://www.energyforecast.de/users/sign_up) is mandatory!

## Binding Configuration

### `energyforecast` Thing Configuration

| Name              | Type      | Description                                                                       | Default   | Required |
|-------------------|-----------|-----------------------------------------------------------------------------------|-----------|----------|
| token             | text      | Token for energy forecast service to provide forecast data                        | N/A       | yes      |
| zone              | text      | Bidding zone for price queries                                                    | N/A       | yes      |
| fixCost           | decimal   | Fix costs in ct/kWh which will be added on top of the forecast price, e.g. 15,3   | 0         | no       |
| vat               | decimal   | VAT in percent which will be added on top of the forecast price, e.g. 19,0        | 0         | no       |
| resolution        | text      | Resolution in ISO 8601 Duration format                                            | PT15M     | no       |
| refreshInterval   | decimal   | Refresh interval in minutes. Check with service throttling                        | 180       | no       |
| errorLimit        | decimal   | Limit error percentage values for better visualization                            | 0         | no       |

`token` needs to be generated after registration at [Energy Forecast Provider](https://www.energyforecast.de/api_keys).

`zone` from [API](https://www.energyforecast.de/api-docs/index.html) are given as options.

`resolution` time resoltion given as options. `PT15M` an `PT60M` are supported.

`refreshInterval` given in minutes. Align this value with your [booked plan](https://www.energyforecast.de/pricing).

`errorLimit` to avoid extraordinary error percentage values in `metric` group. Value `0` is no limit.

## Channels

All channels delivering `timeseries` information. 
Attaching items which are bound only to `rrd4j` persistence will not work.
If you don't have a database installed [InMemory persistence](https://www.openhab.org/addons/persistence/inmemory/) can be used.

### Group `price`

| Channel       | Type                  | Description                                       |
|---------------|-----------------------|---------------------------------------------------|
| series        | Number:EnergyPrice    | Actual and future price series                    |
| origin        | Number                | Originator of the price (market or forecast)      |

`series` delivers price information from 48h up to 96h into the future depending on your [booked plan](https://www.energyforecast.de/pricing).

`origin' shows for every price the originator

- 0: Market
- 1: AI Forecast

### Group `metric`

Metrics for AI price forecasts.
Calculation is done on net prices without configured `fixCost` and `VAT` from configuration.
See [Future Forecasting](https://www.future-forecasting.de/en/wiki/fehlermass/) for further description.

**Note: After first installation these values will stay empty up to 1,5 days!**
To compare market and forecast prices this time is needed until a market price is available. 

| Channel           | Type                  | Description                                                                       |
|-------------------|-----------------------|-----------------------------------------------------------------------------------|
| forecast          | Number:EnergyPrice    | AI forecast price series without any market prices                                |
| forecast-error    | Number:EnergyPrice    | Difference between market and forecast price                                      |
| percent-error     | Number:Dimensionless  | Percentage error between market and forecast price                                |
| mean-abs          | Number:EnergyPrice    | Mean absolute error showing the average of absolute forecast errors               |
| mean-abs-percent  | Number:Dimensionless  | Mean absolute percentage error showing the average of absolute percentage errors  |
  
'forecast` timeseries contains only forecast prices, market prices are excluded

'forecast-error` price difference between market and forecast price

'percent-error` is the `forecast-error` in percent. 
It can show extraordinary high values if market prices are around zero cost.
If market price is 0.001 and forecast was 0.006 the percentage error is *high* while `forecast-error` is quite low.
For visualization you can limit these values with configuration `errorLimit`.

'mean-abs` shows the average of all absolute `forecast-error` calculations as one value.

'mean-percent` shows the average of all absolute `percent-error` calculations as one value.

  
## Full Example

