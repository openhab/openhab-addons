# Buienradar Binding

The Buienradar Binding periodically (5 minute intervals) retrieves rainfall predictions from the Dutch [buienradar.nl webservice API.](https://www.buienradar.nl/overbuienradar/gratis-weerdata).

Using the binding, we can

- warn of upcoming rainfall when there are open windows or doors
- prevent watering the outside plants needlessly,
- warn when we are about to leave the house.

## Supported Things

The binding supports one thing, which can be added manually via the web interface. The thing needs longitude and latitude of the location which needs forecasts.

## Discovery

No auto-discovery is currently possible.

## Configuration of the thing

The configuration can be done by adding a Rain Forecast Thing using the UI, or by adding it to a `.things` file:

```java
Thing buienradar:rain_forecast:home [ location="52.198864211111925,5.4192629660193585" ]
```

and adding the relevant items as such in your `.items` file. Please note that the buienradar service only provides predictions in 5 minutes intervals with a maximum of two hours (120 minutes):

```java
Number RAIN_CURRENT "Current rain" (Rain) {channel="buienradar:rain_forecast:home:forecast_0" }
Number RAIN_5MIN "Rain 5 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_5" }
Number RAIN_10MIN "Rain 10 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_10" }
Number RAIN_15MIN "Rain 15 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_15" }
Number RAIN_20MIN "Rain 20 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_20" }
Number RAIN_25MIN "Rain 25 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_25" }
Number RAIN_30MIN "Rain 30 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_30" }
Number RAIN_35MIN "Rain 35 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_35" }
Number RAIN_40MIN "Rain 40 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_40" }
Number RAIN_45MIN "Rain 45 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_45" }
Number RAIN_50MIN "Rain 50 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_50" }
Number RAIN_55MIN "Rain 55 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_55" }
Number RAIN_60MIN "Rain 60 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_60" }
Number RAIN_65MIN "Rain 65 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_65" }
Number RAIN_70MIN "Rain 70 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_70" }
Number RAIN_75MIN "Rain 75 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_75" }
Number RAIN_80MIN "Rain 80 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_80" }
Number RAIN_85MIN "Rain 85 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_85" }
Number RAIN_90MIN "Rain 90 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_90" }
Number RAIN_95MIN "Rain 95 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_95" }
Number RAIN_100MIN "Rain 100 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_100" }
Number RAIN_105MIN "Rain 105 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_105" }
Number RAIN_110MIN "Rain 110 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_110" }
Number RAIN_115MIN "Rain 115 min." (Rain) {channel="buienradar:rain_forecast:home:forecast_115" }
```

## Example data visualisation

In this example we use the 'Discrete' plugin of Grafana to visualize the predictions. Mappings are made according to precipitation intensity (light, medium, heavy) and those categories are given appropriate colors.

![Z-Way Binding](doc/img/grafana-dashboard.png)

The mappings are as follows:

- 0 – 0.01: None (rgba(204, 204, 204, 0))
- 0.01 – 1: Very light (#badff4)
- 1 – 5: Light (#6ed0e0)
- 5 – 20: Medium (#1f78c1)
- 20 – 50: Heavy (#ef843c)
- 50 – 80: Very heavy (#e24d42)
- 80 – 100: Extremely heavy (#890f02)
