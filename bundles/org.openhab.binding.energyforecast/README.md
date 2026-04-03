# Energy Forecast Binding

Binding provides AI price forecast 48h / 96h beyond day-ahead pricing with [Energy Forecast Service](https://www.energyforecast.de/).
Check in beforehand if your [price zone](https://www.energyforecast.de/api-docs/index.html) is supported.
[Registration](https://www.energyforecast.de/users/sign_up) is mandatory!

## Binding Configuration

### `price-forecast` Thing Configuration

| Name              | Type      | Description                                                                       | Default   | Required |
|-------------------|-----------|-----------------------------------------------------------------------------------|-----------|----------|
| token             | text      | Token for energy forecast service to provide forecast data                        | N/A       | yes      |
| zone              | text      | Bidding zone for price queries                                                    | N/A       | yes      |
| fixCost           | decimal   | Net fix costs in ct/kWh added on top of the forecast price, e.g. 15,3             | 0         | no       |
| resolution        | text      | Resolution in ISO 8601 Duration format                                            | PT15M     | no       |
| refreshInterval   | integer   | Refresh interval in minutes. Check with service throttling                        | 180       | no       |
| errorLimit        | integer   | Limit error percentage values for better visualization                            | 0         | no       |

`token` needs to be generated after registration at [Energy Forecast Service](https://www.energyforecast.de/api_keys).
`zone` from [API](https://www.energyforecast.de/api-docs/index.html) are given as options.
`resolution` time resolution given as options. `PT15M` an `PT60M` are supported.
`refreshInterval` given in minutes. Align this value with your [booked plan](https://www.energyforecast.de/pricing).
`errorLimit` to avoid extraordinary error percentage values in `metric` group. Value `0` is no limit.

#### Calculate Gross Price

`fixCost` shall be net costs which will be added to the net energy price.
If you've already one or more items holding fix cost values consider to [calculate the future prices in a rule](https://www.openhab.org/addons/bindings/energidataservice/#time-series).
Use [VAT Transformation Service](https://www.openhab.org/addons/transformations/vat/) to calculate the gross price.

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
`origin` shows for every price the originator.

- 0: Market
- 1: AI Forecast

### Group `metric`

Metrics for AI price forecasts.
Calculation is done on net prices without configured `fixCost` and any VAT applied via transformations/rules.
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
  
`forecast` timeseries contains only forecast prices, market prices are excluded.
`forecast-error` price difference between market and forecast price.
`percent-error` is the `forecast-error` in percent.
It can show extraordinary high values if market prices are around zero cost.
If market price is 0.001 and forecast was 0.006 the percentage error is _high_ while `forecast-error` is quite low.
For visualization you can limit these values with configuration `errorLimit`.

`mean-abs` shows the average of all absolute `forecast-error` calculations as one value.
`mean-abs-percent` shows the average of all absolute `percent-error` calculations as one value.
  
## Full Example

### `demo.things`

```java
Thing energyforecast:price-forecast:UID "Energy Forecast" [zone="YOUR_BIDDING_ZONE", token="YOUR_TOKEN", fixCost=12.3, resolution="PT15M", refreshInterval=180, errorLimit=0] 
```

### `demo.items`

```java
Number:EnergyPrice      Energy_Forecast_Price_Series            "Price Series"              {channel="energyforecast:price-forecast:UID:price#series"}
Number                  Energy_Forecast_Price_Origins           "Price Origin"              {channel="energyforecast:price-forecast:UID:price#origin"}

Number:EnergyPrice      Energy_Forecast_Forecast                "Forecast"                  {channel="energyforecast:price-forecast:UID:metric#forecast"}
Number:EnergyPrice      Energy_Forecast_Forecast_Error          "Forecast Error"            {channel="energyforecast:price-forecast:UID:metric#forecast-error"}
Number:Dimensionless    Energy_Forecast_Percent_Error           "Percent Error"             {channel="energyforecast:price-forecast:UID:metric#percent-error"}
Number:EnergyPrice      Energy_Forecast_Mean_Absolute           "Mean Absolute"             {channel="energyforecast:price-forecast:UID:metric#mean-abs"}
Number:Dimensionless    Energy_Forecast_Mean_Absolute_Percent   "Mean Absolute Percent"     {channel="energyforecast:price-forecast:UID:metric#mean-abs-percent"}
```
