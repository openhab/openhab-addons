# Energi Data Service Binding

This binding integrates electricity prices from the Danish Energi Data Service ("Open energy data from Energinet to society").

This can be used to plan energy consumption, for example to calculate the cheapest period for running a dishwasher or charging an EV.

## Supported Things

All channels are available for thing type `service`.

## Binding Configuration

This advanced configuration option can be used if the transition to the Day-Ahead Prices dataset is postponed.
For the latest updates, please refer to the [Energi Data Service news](https://energidataservice.dk/news).

| Name                   | Type    | Description                                                            | Default    | Required |
| ---------------------- | ------- | ---------------------------------------------------------------------- | ---------- | -------- |
| dayAheadTransitionDate | text    | The date when the addon switches to using the Day-Ahead Prices dataset | 2025-09-30 | no       |

## Thing Configuration

### `service` Thing Configuration

| Name                  | Type    | Description                                                          | Default       | Required |
| --------------------- | ------- | -------------------------------------------------------------------- | ------------- | -------- |
| priceArea             | text    | Price area for spot prices (same as bidding zone)                    |               | yes      |
| currencyCode          | text    | Currency code in which to obtain spot prices                         | DKK           | no       |
| gridCompanyGLN        | integer | Global Location Number of the Grid Company                           |               | no       |
| energinetGLN          | integer | Global Location Number of Energinet                                  | 5790000432752 | no       |
| reducedElectricityTax | boolean | Reduced electricity tax applies. For electric heating customers only | false         | no       |

#### Global Location Number of the Grid Company

The Global Location Number of your grid company can be selected from a built-in list of grid companies.
To find the company in your area, you can go to [Find netselskab](https://greenpowerdenmark.dk/vejledning-teknik/nettilslutning/find-netselskab), enter your address, and the company will be shown.

If your company is not on the list, you can configure it manually.
To obtain the Global Location Number of your grid company:

- Open a browser and go to [Eloverblik](https://eloverblik.dk/).
- Click "Private customers" and log in with MitID (confirmation will appear as Energinet).
- Click "Retrieve data" and select "Price data".
- Open the file and look for the rows having **Price_type** = "Subscription".
- In the columns **Name** and/or **Description** you should see the name of your grid company.
- In column **Owner** you can find the GLN ("Global Location Number").
- Most rows will have this **Owner**. If in doubt, try to look for rows _not_ having 5790000432752 as owner.

#### Reduced electricity tax applies

For customers using electricity for heating, a reduced electricity tax rate may apply after consuming the first 4000 kWh within a year.
When you are entitled to reduced electricity tax, this option should be set.
This will ensure that thing action calculations use the reduced electricity tax rate when price components are not explicitly provided.
It will not impact channels, see [Electricity Tax](#electricity-tax) for further information.

## Channels

### Channel Group `electricity`

| Channel                  | Type                     | Description                                                                            |
| ------------------------ | ------------------------ | -------------------------------------------------------------------------------------- |
| spot-price               | Number:EnergyPrice       | Spot price in DKK or EUR per kWh                                                       |
| grid-tariff              | Number:EnergyPrice       | Grid tariff in DKK per kWh. Only available when `gridCompanyGLN` is configured         |
| system-tariff            | Number:EnergyPrice       | System tariff in DKK per kWh                                                           |
| transmission-grid-tariff | Number:EnergyPrice       | Transmission grid tariff in DKK per kWh                                                |
| electricity-tax          | Number:EnergyPrice       | Electricity tax in DKK per kWh                                                         |
| reduced-electricity-tax  | Number:EnergyPrice       | Reduced electricity tax in DKK per kWh. For electric heating customers only            |
| co2-emission-prognosis   | Number:EmissionIntensity | Estimated prognosis for CO₂ emission following the day-ahead market in g/kWh           |
| co2-emission-realtime    | Number:EmissionIntensity | Near up-to-date history for CO₂ emission from electricity consumed in Denmark in g/kWh |

#### Total Price

_Please note:_ There is no channel providing the total price.
Instead, create a group item with `SUM` as aggregate function and add the individual price items as children.
This has the following advantages:

- Full customization possible: Freely choose the channels which should be included in the total (even between different bindings).
- Spot price can be configured in EUR while tariffs are in DKK (and currency conversions are performed outside the binding).
- An additional item containing the kWh fee from your electricity supplier can be added also (and it can be dynamic).

If you want electricity tax included in your total price, please add either `electricity-tax` or `reduced-electricity-tax` to the group - depending on which one applies.
See [Electricity Tax](#electricity-tax) for further information.

##### Time Series

Group items with aggregate functions are not automatically recalculated into the future when the time series for child items are updated.
Therefore, the `SUM` function mentioned above will only work for the current price.
Calculation of future total prices can be achieved with a rule:

:::: tabs

::: tab JavaScript

In this example file-based using Rule Builder:

```javascript
rules.when()
    .channel('energidataservice:service:energidataservice:electricity#event').triggered('DAY_AHEAD_AVAILABLE')
    .then(event => {
        // Short delay because persistence is asynchronous.
        setTimeout(() => {
            var timeSeries = new items.TimeSeries('REPLACE');
            var start = time.LocalDate.now().atStartOfDay().atZone(time.ZoneId.systemDefault());
            var spotPrices = items.SpotPrice.persistence.getAllStatesBetween(start, start.plusDays(2));
            for (var spotPrice of spotPrices) {
                var totalPrice = spotPrice.quantityState
                    .add(items.GridTariff.persistence.persistedState(spotPrice.timestamp).quantityState)
                    .add(items.SystemTariff.persistence.persistedState(spotPrice.timestamp).quantityState)
                    .add(items.TransmissionGridTariff.persistence.persistedState(spotPrice.timestamp).quantityState)
                    .add(items.ElectricityTax.persistence.persistedState(spotPrice.timestamp).quantityState);

                timeSeries.add(spotPrice.timestamp, totalPrice);
            }
            items.TotalPrice.persistence.persist(timeSeries);
        }, 5000);
    })
    .build("Calculate total price");
```

:::

::: tab JRuby

```ruby
rule "Calculate total price" do
  channel "energidataservice:service:energidataservice:electricity#event", triggered: "DAY_AHEAD_AVAILABLE"
  run do
    after 5.seconds do # Short delay because persistence is asynchronous.
      # Persistence methods will call LocalDate#to_zoned_date_time which converts it
      # to a ZonedDateTime in the default system zone, with 00:00 as its time portion
      start = LocalDate.now
      spot_prices = SpotPrice.all_states_between(start, start + 2.days)

      next unless spot_prices # don't proceed if the persistence result is nil

      time_series = TimeSeries.new # the default policy is replace
      spot_prices.each do |spot_price|
        total_price = spot_price +
                      GridTariff.persisted_state(spot_price.timestamp) +
                      SystemTariff.persisted_state(spot_price.timestamp) +
                      TransmissionGridTariff.persisted_state(spot_price.timestamp) +
                      ElectricityTax.persisted_state(spot_price.timestamp)
        time_series.add(spot_price.timestamp, total_price)
      end
      TotalPrice.persist(time_series)
    end
  end
end
```

:::

::::

#### Currencies

There are some existing limitations related to currency support.
While the binding attempts to update channels in the correct currency, such attempts may face rejection.
In such cases, the binding will resort to omitting the currency unit.
While this ensures correct prices, it's important to note that the currency information may be incorrect in these instances.

#### Value-Added Tax

VAT is not included in any of the prices.
To include VAT for items linked to the `Number` channels, the [VAT profile](https://www.openhab.org/addons/transformations/vat/) can be used.
This must be installed separately.
Once installed, simply select "Value-Added Tax" as Profile when linking an item.

#### Persisting Time Series

The binding offers support for persisting both historical and upcoming prices.
The recommended persistence strategy is `forecast`, as it ensures a clean history without redundancy.
Prices from the past 24 hours and all forthcoming prices will be stored.
Any changes that impact published prices (e.g. selecting or deselecting VAT Profile) will result in the replacement of persisted prices within this period.

##### Manually Persisting History

During extended service interruptions, data unavailability, or openHAB downtime, historic prices may be absent from persistence.
A console command is provided to fill gaps: `energidataservice update [SpotPrice|GridTariff|SystemTariff|TransmissionGridTariff|ElectricityTax|ReducedElectricitytax] <StartDate> [<EndDate>]`.

Example:

```shell
energidataservice update spotprice 2024-04-12 2024-04-14
```

This can also be useful for retrospectively changing the [VAT profile](https://www.openhab.org/addons/transformations/vat/).

#### Grid Tariff

Discounts are automatically taken into account for channel `grid-tariff` so that it represents the actual price.

The tariffs are downloaded using pre-configured filters for the different [Grid Company GLN's](#global-location-number-of-the-grid-company).
If your company is not in the list, or the filters are not working, they can be manually overridden.
To override filters, the channel `grid-tariff` has the following configuration parameters:

| Name            | Type | Description                                                                                                                      | Default | Required | Advanced |
| --------------- | ---- | -------------------------------------------------------------------------------------------------------------------------------- | ------- | -------- | -------- |
| chargeTypeCodes | text | Comma-separated list of charge type codes                                                                                        |         | no       | yes      |
| notes           | text | Comma-separated list of notes                                                                                                    |         | no       | yes      |
| start           | text | Query start date parameter expressed as either YYYY-MM-DD or dynamically as one of `StartOfDay`, `StartOfMonth` or `StartOfYear` |         | no       | yes      |
| offset          | text | Query start date offset expressed as an ISO 8601 duration                                                                        |         | no       | yes      |

The parameters `chargeTypeCodes` and `notes` are logically combined with "AND", so if only one parameter is needed for the filter, only provide this parameter and leave the other one empty.
Using any of these parameters will override the pre-configured filter entirely.

The parameter `start` can be used independently to override the query start date parameter.
If used while leaving `chargeTypeCodes` and `notes` empty, only the date will be overridden.

The parameter `offset` can be used in combination with `start` to provide an offset to a dynamic start date parameter, i.e. `StartOfDay`, `StartOfMonth` or `StartOfYear`.
The needed amount of historic hours is automatically taken into consideration.
This parameter is ignored when start date is supplied as YYYY-MM-DD.

Determining the right filters can be tricky, so if in doubt ask in the community forum.
See also [Datahub Price List](https://www.energidataservice.dk/tso-electricity/DatahubPricelist).

##### Filter Examples

_N1:_

| Parameter       | Value   |
| --------------- | ------- |
| chargeTypeCodes | CD,CD R |
| notes           |         |

_Nord Energi Net:_

| Parameter       | Value      |
| --------------- | ---------- |
| chargeTypeCodes | TAC        |
| notes           | Nettarif C |
| start           | StartOfDay |
| offset          | -P1D       |

#### Electricity Tax

The standard channel for electricity tax is `electricity-tax`.
For customers using electricity for heating, a reduced electricity tax rate may apply (see [Reduced electricity tax applies](#reduced-electricity-tax-applies)).
This reduced rate is made available through channel `reduced-electricity-tax`.

The binding cannot determine or manage rate variations as they depend on metering data.
Usually `reduced-electricity-tax` is preferred when using electricity for heating.

#### CO₂ Emissions

Data for the CO₂ emission channels is published as time series with a resolution of 5 minutes.

Channel `co2-emission-realtime` provides near up-to-date historic emission and is refreshed every 5 minutes.
When the binding is started, or a new item is linked, or a linked item receives an update command, historic data for the last 24 hours is provided in addition to the current value.

Channel `co2-emission-prognosis` provides estimated prognosis for future emissions and is refreshed every 15 minutes.
Depending on the time of the day, an update of the prognosis may include estimates for more than 9 hours, but every update will have at least 9 hours into the future.
A persistence configuration is required for this channel.

Please note that the CO₂ emission channels only apply to Denmark.
These channels will not be updated when the configured price area is not DK1 or DK2.

#### Trigger Channels

Advanced channel `event` can trigger the following events:

| Event               | Description                    |
| ------------------- | ------------------------------ |
| DAY_AHEAD_AVAILABLE | Day-ahead prices are available |

## Thing Actions

Thing actions can be used to perform calculations as well as import prices directly into rules without relying on persistence.
This is convenient, fast, and provides automatic summation of the price components of interest.

Actions use cached data for performing operations.
Since data is only fetched when an item is linked to a channel, there might not be any cached data available.
In this case the data will be fetched on demand and cached afterwards.
The first action triggered on a given day may therefore be a bit slower, and is also prone to failing if the server call fails for any reason.
This potential problem can be prevented by linking the individual channels to items.

### `calculateCheapestPeriod`

This action will determine the cheapest period for using energy.
It comes in four variants with different input parameters.

The result is a `Map` with the following keys:

| Key                | Type         | Description                                           |
| ------------------ | ------------ | ----------------------------------------------------- |
| CheapestStart      | `Instant`    | Start time of cheapest calculated period              |
| LowestPrice        | `BigDecimal` | The total price when starting at cheapest start       |
| MostExpensiveStart | `Instant`    | Start time of most expensive calculated period        |
| HighestPrice       | `BigDecimal` | The total price when starting at most expensive start |

#### `calculateCheapestPeriod` from Duration

| Parameter     | Type       | Description                             |
| ------------- | ---------- | --------------------------------------- |
| earliestStart | `Instant`  | Earliest start time allowed             |
| latestEnd     | `Instant`  | Latest end time allowed                 |
| duration      | `Duration` | The duration to fit within the timeslot |

This is a convenience method that can be used when the power consumption is not known.
The calculation will assume linear consumption and will find the best timeslot based on that.
For this reason the resulting `Map` will not contain the keys `LowestPrice` and `HighestPrice`.

Example:

:::: tabs

::: tab DSL

```java
val actions = getActions("energidataservice", "energidataservice:service:energidataservice")
var Map<String, Object> result = actions.calculateCheapestPeriod(now.toInstant(), now.plusHours(12).toInstant(), Duration.ofMinutes(90))
```

:::

::: tab JavaScript

```javascript
var edsActions = actions.get("energidataservice", "energidataservice:service:energidataservice");
var result = edsActions.calculateCheapestPeriod(time.Instant.now(), time.Instant.now().plusSeconds(12*60*60), time.Duration.ofMinutes(90));
```

:::

::: tab JRuby

```ruby
eds = things["energidataservice:service:energidataservice"]
result = eds.calculate_cheapest_period(Instant.now, 2.hours.from_now.to_instant, 90.minutes)
```

:::

::: tab Python

```python
eds_actions = Things.getActions("energidataservice", "energidataservice:service:energidataservice")
result = eds_actions.calculateCheapestPeriod(datetime.now(tz=timezone.utc), datetime.now(tz=timezone.utc) + timedelta(hours=12), timedelta(minutes=90));
```

:::

::::

#### `calculateCheapestPeriod` from Duration and Power

| Parameter     | Type                  | Description                             |
| ------------- | --------------------- | --------------------------------------- |
| earliestStart | `Instant`             | Earliest start time allowed             |
| latestEnd     | `Instant`             | Latest end time allowed                 |
| duration      | `Duration`            | The duration to fit within the timeslot |
| power         | `QuantityType<Power>` | Linear power consumption                |

This action is identical to the variant above, but with a known linear power consumption.
As a result the price is also included in the result.

Example:

:::: tabs

::: tab DSL

```java
val actions = getActions("energidataservice", "energidataservice:service:energidataservice")
var Map<String, Object> result = actions.calculateCheapestPeriod(now.toInstant(), now.plusHours(12).toInstant(), Duration.ofMinutes(90), 250 | W)
```

:::

::: tab JavaScript

```javascript
var edsActions = actions.get("energidataservice", "energidataservice:service:energidataservice");
var result = edsActions.calculateCheapestPeriod(time.Instant.now(), time.Instant.now().plusSeconds(12*60*60), time.Duration.ofMinutes(90), Quantity("250 W"));
```

:::

::: tab JRuby

```ruby
eds = things["energidataservice:service:energidataservice"]
result = eds.calculate_cheapest_period(Instant.now, 12.hours.from_now.to_instant, 90.minutes, 250 | "W")
```

:::

::: tab Python

```python
eds_actions = Things.getActions("energidataservice", "energidataservice:service:energidataservice")
result = eds_actions.calculateCheapestPeriod(datetime.now(tz=timezone.utc), datetime.now(tz=timezone.utc) + timedelta(hours=12), timedelta(minutes=90), QuantityType("250 W"))
```

:::

::::

#### `calculateCheapestPeriod` from Power Phases

| Parameter      | Type                        | Description                                            |
| -------------- | --------------------------- | ------------------------------------------------------ |
| earliestStart  | `Instant`                   | Earliest start time allowed                            |
| latestEnd      | `Instant`                   | Latest end time allowed                                |
| durationPhases | `List<Duration>`            | List of durations for the phases                       |
| powerPhases    | `List<QuantityType<Power>>` | List of power consumption for each corresponding phase |

This variant is similar to the one above, but is based on a supplied timetable.

The timetable is supplied as two individual parameters, `durationPhases` and `powerPhases`, which must have the same size.
This can be considered as different phases of using power, so each list member represents a period with a linear use of power.
`durationPhases` should be a List populated by `Duration` objects, while `powerPhases` should be a List populated by `QuantityType<Power>` objects for that duration of time.

Example:

:::: tabs

::: tab DSL

```java
val ArrayList<Duration> durationPhases = new ArrayList<Duration>()
durationPhases.add(Duration.ofMinutes(37))
durationPhases.add(Duration.ofMinutes(8))
durationPhases.add(Duration.ofMinutes(4))
durationPhases.add(Duration.ofMinutes(2))
durationPhases.add(Duration.ofMinutes(4))
durationPhases.add(Duration.ofMinutes(36))
durationPhases.add(Duration.ofMinutes(41))
durationPhases.add(Duration.ofMinutes(104))

val ArrayList<QuantityType<Power>> powerPhases = new ArrayList<QuantityType<Power>>()
powerPhases.add(162.162 | W)
powerPhases.add(750 | W)
powerPhases.add(1500 | W)
powerPhases.add(3000 | W)
powerPhases.add(1500 | W)
powerPhases.add(166.666 | W)
powerPhases.add(146.341 | W)
powerPhases.add(0 | W)

val actions = getActions("energidataservice", "energidataservice:service:energidataservice")
var Map<String, Object> result = actions.calculateCheapestPeriod(now.toInstant(), now.plusHours(12).toInstant(), durationPhases, powerPhases)
```

:::

::: tab JavaScript

```javascript
var durationPhases = [
    time.Duration.ofMinutes(37),
    time.Duration.ofMinutes(8),
    time.Duration.ofMinutes(4),
    time.Duration.ofMinutes(2),
    time.Duration.ofMinutes(4),
    time.Duration.ofMinutes(36),
    time.Duration.ofMinutes(41),
    time.Duration.ofMinutes(104)
];

var powerPhases = [
    Quantity("162.162 W"),
    Quantity("750 W"),
    Quantity("1500 W"),
    Quantity("3000 W"),
    Quantity("1500 W"),
    Quantity("166.666 W"),
    Quantity("146.341 W"),
    Quantity("0 W")
];

var edsActions = actions.get("energidataservice", "energidataservice:service:energidataservice");
var result = edsActions.calculateCheapestPeriod(time.Instant.now(), time.Instant.now().plusSeconds(12*60*60), durationPhases, powerPhases);
```

:::

::: tab JRuby

```ruby
duration_phases = [37, 8, 4, 2, 4, 36, 41, 104].map { |duration| duration.minutes }
power_phases = [
  162.162 | "W",
  750 | "W",
  1500 | "W",
  3000 | "W",
  1500 | "W",
  166.666 | "W",
  146.341 | "W",
  0 | "W"
]

eds = things["energidataservice:service:energidataservice"]
result = eds.calculate_cheapest_period(Instant.now, 12.hours.from_now.to_instant, duration_phases, power_phases)
```

:::

::: tab Python

```python
duration_phases = [
    timedelta(minutes=37),
    timedelta(minutes=8),
    timedelta(minutes=4),
    timedelta(minutes=2),
    timedelta(minutes=4),
    timedelta(minutes=36),
    timedelta(minutes=41),
    timedelta(minutes=104)
]

power_phases = [
    QuantityType("162.162 W"),
    QuantityType("750 W"),
    QuantityType("1500 W"),
    QuantityType("3000 W"),
    QuantityType("1500 W"),
    QuantityType("166.666 W"),
    QuantityType("146.341 W"),
    QuantityType("0 W")
]

eds_actions = Things.getActions("energidataservice", "energidataservice:service:energidataservice")
result = eds_actions.calculateCheapestPeriod(datetime.now(tz=timezone.utc), datetime.now(tz=timezone.utc) + timedelta(hours=12), duration_phases, power_phases)
```

:::

::::

Please note that the total duration will be calculated automatically as a sum of provided duration phases.
Therefore, if the total duration is longer than the sum of phase durations, the remaining duration must be provided as last item with a corresponding 0 W power item.
This is to ensure that the full program will finish before the provided `latestEnd`.

#### `calculateCheapestPeriod` from Energy per Phase

| Parameter          | Type                   | Description                           |
| ------------------ | ---------------------- | ------------------------------------- |
| earliestStart      | `Instant`              | Earliest start time allowed           |
| latestEnd          | `Instant`              | Latest end time allowed               |
| totalDuration      | `Duration`             | The total duration of all phases      |
| durationPhases     | `List<Duration>`       | List of durations for the phases      |
| energyUsedPerPhase | `QuantityType<Energy>` | Fixed amount of energy used per phase |

This variant will assign the provided amount of energy into each phase.
The use case for this variant is a simplification of the previous variant.
For example, a dishwasher may provide energy consumption in 0.1 kWh steps.
In this case it's a simple task to create a timetable accordingly without having to calculate the average power consumption per phase.
Since a last phase may use no significant energy, the total duration must be provided also.

Example:

:::: tabs

::: tab DSL

```java
val ArrayList<Duration> durationPhases = new ArrayList<Duration>()
durationPhases.add(Duration.ofMinutes(37))
durationPhases.add(Duration.ofMinutes(8))
durationPhases.add(Duration.ofMinutes(4))
durationPhases.add(Duration.ofMinutes(2))
durationPhases.add(Duration.ofMinutes(4))
durationPhases.add(Duration.ofMinutes(36))
durationPhases.add(Duration.ofMinutes(41))

val actions = getActions("energidataservice", "energidataservice:service:energidataservice")

// 0.7 kWh is used in total (number of phases × energy used per phase)
var Map<String, Object> result = actions.calculateCheapestPeriod(now.toInstant(), now.plusHours(12).toInstant(), Duration.ofMinutes(236), durationPhases, 0.1 | kWh)
```

:::

::: tab JavaScript

```javascript
var durationPhases = [
    time.Duration.ofMinutes(37),
    time.Duration.ofMinutes(8),
    time.Duration.ofMinutes(4),
    time.Duration.ofMinutes(2),
    time.Duration.ofMinutes(4),
    time.Duration.ofMinutes(36),
    time.Duration.ofMinutes(41)
];

var edsActions = actions.get("energidataservice", "energidataservice:service:energidataservice");

// 0.7 kWh is used in total (number of phases × energy used per phase)
var result = edsActions.calculateCheapestPeriod(time.Instant.now(), time.Instant.now().plusSeconds(12*60*60), time.Duration.ofMinutes(236), durationPhases, Quantity("0.1 kWh"));
```

:::

::: tab JRuby

```ruby
duration_phases = [37, 8, 4, 2, 4, 36, 41].map { |duration| duration.minutes }

eds = things["energidataservice:service:energidataservice"]

# 0.7 kWh is used in total (number of phases × energy used per phase)
result = eds.calculate_cheapest_period(Instant.now, 12.hours.from_now.to_instant, 236.minutes, duration_phases, 0.1 | "kWh")
```

:::

::: tab Python

```python
duration_phases = [
    timedelta(minutes=37),
    timedelta(minutes=8),
    timedelta(minutes=4),
    timedelta(minutes=2),
    timedelta(minutes=4),
    timedelta(minutes=36),
    timedelta(minutes=41)
]

# 0.7 kWh is used in total (number of phases × energy used per phase)
eds_actions = Things.getActions("energidataservice", "energidataservice:service:energidataservice")
result = eds_actions.calculateCheapestPeriod(datetime.now(tz=timezone.utc), datetime.now(tz=timezone.utc) + timedelta(hours=12), timedelta(minutes=236), duration_phases, QuantityType("0.1 kWh"))
```

:::

::::

### `calculatePrice`

| Parameter | Type                  | Description              |
| --------- | --------------------- | ------------------------ |
| start     | `Instant`             | Start time               |
| end       | `Instant`             | End time                 |
| power     | `QuantityType<Power>` | Linear power consumption |

**Result:** Price as `BigDecimal`.

This action calculates the price for using given amount of power in the period from `start` till `end`.
Returns `null` if the calculation cannot be performed due to missing price data within the requested period.

Example:

:::: tabs

::: tab DSL

```java
val actions = getActions("energidataservice", "energidataservice:service:energidataservice")
var price = actions.calculatePrice(now.toInstant(), now.plusHours(4).toInstant, 200 | W)
```

:::

::: tab JavaScript

```javascript
var edsActions = actions.get("energidataservice", "energidataservice:service:energidataservice");
var price = edsActions.calculatePrice(time.Instant.now(), time.ZonedDateTime.now().plusHours(4).toInstant(), Quantity("200 W"));
```

:::

::: tab JRuby

```ruby
eds = things["energidataservice:service:energidataservice"]
price = eds.calculate_price(Instant.now, 4.hours.from_now.to_instant, 200 | "W")
```

:::

::: tab Python

```python
eds_actions = Things.getActions("energidataservice", "energidataservice:service:energidataservice")
price = eds_actions.calculatePrice(datetime.now(tz=timezone.utc), datetime.now(tz=timezone.utc) + timedelta(hours=4), QuantityType("200 W"))
```

:::

::::

### `getPrices`

| Parameter       | Type     | Description                                         |
| --------------- | -------- | --------------------------------------------------- |
| priceComponents | `String` | Comma-separated list of price components to include |

**Result:** `Map<Instant, BigDecimal>`

The parameter `priceComponents` is a case-insensitive comma-separated list of price components to include in the returned hourly prices.
These components can be requested:

| Price component        | Description              |
| ---------------------- | ------------------------ |
| SpotPrice              | Spot price               |
| GridTariff             | Grid tariff              |
| SystemTariff           | System tariff            |
| TransmissionGridTariff | Transmission grid tariff |
| ElectricityTax         | Electricity tax          |
| ReducedElectricityTax  | Reduced electricity tax  |

Using `null` as parameter returns the total prices including all price components.
If **Reduced Electricity Tax** is set in Thing configuration, `ElectricityTax` will be excluded, otherwise `ReducedElectricityTax`.
This logic ensures consistent and comparable results not affected by artificial changes in the rate for electricity tax two times per year.

Example:

:::: tabs

::: tab DSL

```java
val actions = getActions("energidataservice", "energidataservice:service:energidataservice")
var priceMap = actions.getPrices("SpotPrice,GridTariff")
```

:::

::: tab JavaScript

```javascript
var edsActions = actions.get("energidataservice", "energidataservice:service:energidataservice");
var priceMap = utils.javaMapToJsMap(edsActions.getPrices("SpotPrice,GridTariff"));
```

:::

::: tab JRuby

```ruby
eds = things["energidataservice:service:energidataservice"]
price_map = eds.get_prices("SpotPrice,GridTariff")
```

:::

::: tab Python

```python
eds_actions = Things.getActions("energidataservice", "energidataservice:service:energidataservice")
price_dict = {
    datetime.fromtimestamp(entry.getKey().getEpochSecond(), tz=timezone.utc): float(entry.getValue().doubleValue())
    for entry in eds_actions.getPrices("SpotPrice,GridTariff").entrySet()
}
```

:::

::::

## Full Example

### Thing Configuration

```java
Thing energidataservice:service:energidataservice "Energi Data Service" [ priceArea="DK1", currencyCode="DKK", gridCompanyGLN="5790001089030" ] {
    Channels:
        Number : electricity#grid-tariff [ chargeTypeCodes="CD,CD R", start="StartOfYear" ]
}
```

### Item Configuration

```java
Group:Number:EnergyPrice:SUM TotalPrice "Total Price" <price>
Number:EnergyPrice SpotPrice "Spot Price" <price> (TotalPrice) { channel="energidataservice:service:energidataservice:electricity#spot-price" [profile="transform:VAT"] }
Number:EnergyPrice GridTariff "Grid Tariff" <price> (TotalPrice) { channel="energidataservice:service:energidataservice:electricity#grid-tariff" [profile="transform:VAT"] }
Number:EnergyPrice SystemTariff "System Tariff" <price> (TotalPrice) { channel="energidataservice:service:energidataservice:electricity#system-tariff" [profile="transform:VAT"] }
Number:EnergyPrice TransmissionGridTariff "Transmission Grid Tariff" <price> (TotalPrice) { channel="energidataservice:service:energidataservice:electricity#transmission-grid-tariff" [profile="transform:VAT"] }
Number:EnergyPrice ElectricityTax "Electricity Tax" <price> (TotalPrice) { channel="energidataservice:service:energidataservice:electricity#electricity-tax" [profile="transform:VAT"] }
```

### Persistence Configuration

```java
Strategies {
    default = everyChange
}

Items {
    SpotPrice,
    GridTariff,
    SystemTariff,
    TransmissionGridTariff,
    ElectricityTax: strategy = forecast
}
```

In case persistence is only needed for charts and/or accessing prices from rules, [InMemory Persistence](https://www.openhab.org/addons/persistence/inmemory/) can be used.

### Thing Actions Example

:::: tabs

::: tab DSL

```java
import java.time.Duration
import java.util.ArrayList
import java.util.Map
import java.time.temporal.ChronoUnit

val actions = getActions("energidataservice", "energidataservice:service:energidataservice")

var priceMap = actions.getPrices(null)
var hourStart = now.toInstant().truncatedTo(ChronoUnit.HOURS)
logInfo("Current total price excl. VAT", priceMap.get(hourStart).toString)

var priceMap = actions.getPrices("SpotPrice,GridTariff");
logInfo("Current spot price + grid tariff excl. VAT", priceMap.get(hourStart).toString)

var price = actions.calculatePrice(Instant.now, now.plusHours(1).toInstant, 150 | W)
if (price != null) {
    logInfo("Total price for using 150 W for the next hour", price.toString)
}

val ArrayList<Duration> durationPhases = new ArrayList<Duration>()
durationPhases.add(Duration.ofMinutes(37))
durationPhases.add(Duration.ofMinutes(8))
durationPhases.add(Duration.ofMinutes(4))
durationPhases.add(Duration.ofMinutes(2))
durationPhases.add(Duration.ofMinutes(4))
durationPhases.add(Duration.ofMinutes(36))
durationPhases.add(Duration.ofMinutes(41))
durationPhases.add(Duration.ofMinutes(104))

val ArrayList<QuantityType<Power>> consumptionPhases = new ArrayList<QuantityType<Power>>()
consumptionPhases.add(162.162 | W)
consumptionPhases.add(750 | W)
consumptionPhases.add(1500 | W)
consumptionPhases.add(3000 | W)
consumptionPhases.add(1500 | W)
consumptionPhases.add(166.666 | W)
consumptionPhases.add(146.341 | W)
consumptionPhases.add(0 | W)

var Map<String, Object> result = actions.calculateCheapestPeriod(now.toInstant, now.plusHours(24).toInstant, durationPhases, consumptionPhases)
logInfo("Cheapest start", (result.get("CheapestStart") as Instant).toString)
logInfo("Lowest price", (result.get("LowestPrice") as Number).doubleValue.toString)
logInfo("Highest price", (result.get("HighestPrice") as Number).doubleValue.toString)
logInfo("Most expensive start", (result.get("MostExpensiveStart") as Instant).toString)

// This is a simpler version taking advantage of the fact that each interval here represents 0.1 kWh of consumed energy.
// In this example we have to provide the total duration to make sure we fit the latest end. This is because there is no
// registered consumption in the last phase.
val ArrayList<Duration> durationPhases = new ArrayList<Duration>()
durationPhases.add(Duration.ofMinutes(37))
durationPhases.add(Duration.ofMinutes(8))
durationPhases.add(Duration.ofMinutes(4))
durationPhases.add(Duration.ofMinutes(2))
durationPhases.add(Duration.ofMinutes(4))
durationPhases.add(Duration.ofMinutes(36))
durationPhases.add(Duration.ofMinutes(41))

var Map<String, Object> result = actions.calculateCheapestPeriod(now.toInstant(), now.plusHours(24).toInstant(), Duration.ofMinutes(236), durationPhases, 0.1 | kWh)
```

:::

::: tab JavaScript

```javascript
var edsActions = actions.get("energidataservice", "energidataservice:service:energidataservice");

// Get prices and convert to JavaScript Map with Instant string representation as keys.
var priceMap = new Map();
utils.javaMapToJsMap(edsActions.getPrices()).forEach((value, key) => {
    priceMap.set(key.toString(), value);
});

var hourStart = time.Instant.now().truncatedTo(time.ChronoUnit.HOURS);
console.log("Current total price excl. VAT: " + priceMap.get(hourStart.toString()));

utils.javaMapToJsMap(edsActions.getPrices("SpotPrice,GridTariff")).forEach((value, key) => {
    priceMap.set(key.toString(), value);
});
console.log("Current spot price + grid tariff excl. VAT: " + priceMap.get(hourStart.toString()));

var price = edsActions.calculatePrice(time.Instant.now(), time.Instant.now().plusSeconds(3600), Quantity("150 W"));
if (price !== null) {
    console.log("Total price for using 150 W for the next hour: " + price.toString());
}

var durationPhases = [
    time.Duration.ofMinutes(37),
    time.Duration.ofMinutes(8),
    time.Duration.ofMinutes(4),
    time.Duration.ofMinutes(2),
    time.Duration.ofMinutes(4),
    time.Duration.ofMinutes(36),
    time.Duration.ofMinutes(41),
    time.Duration.ofMinutes(104)
];

var consumptionPhases = [
    Quantity("162.162 W"),
    Quantity("750 W"),
    Quantity("1500 W"),
    Quantity("3000 W"),
    Quantity("1500 W"),
    Quantity("166.666 W"),
    Quantity("146.341 W"),
    Quantity("0 W")
];

var result = edsActions.calculateCheapestPeriod(time.Instant.now(), time.Instant.now().plusSeconds(24*60*60), durationPhases, consumptionPhases);

console.log("Cheapest start: " + result.get("CheapestStart").toString());
console.log("Lowest price: " + result.get("LowestPrice"));
console.log("Highest price: " + result.get("HighestPrice"));
console.log("Most expensive start: " + result.get("MostExpensiveStart").toString());

// This is a simpler version taking advantage of the fact that each interval here represents 0.1 kWh of consumed energy.
// In this example we have to provide the total duration to make sure we fit the latest end. This is because there is no
// registered consumption in the last phase.
var durationPhases = [
    time.Duration.ofMinutes(37),
    time.Duration.ofMinutes(8),
    time.Duration.ofMinutes(4),
    time.Duration.ofMinutes(2),
    time.Duration.ofMinutes(4),
    time.Duration.ofMinutes(36),
    time.Duration.ofMinutes(41)
];

var result = edsActions.calculateCheapestPeriod(time.Instant.now(), time.Instant.now().plusSeconds(24*60*60), time.Duration.ofMinutes(236), durationPhases, Quantity("0.1 kWh"));
```

:::

::: tab JRuby

```ruby
eds = things["energidataservice:service:energidataservice"]

price_map = eds.get_prices
hour_start = Instant.now.truncated_to(ChronoUnit::HOURS)
logger.info "Current total price excl. VAT: #{price_map[hour_start]}"

price_map = eds.get_prices("SpotPrice,GridTariff")
logger.info "Current spot price + grid tariff excl. VAT: #{price_map[hour_start]}"

price = eds.calculate_price(Instant.now, 1.hour.from_now.to_instant, 150 | "W")
logger.info "Total price for using 150 W for the next hour: #{price}" if price

duration_phases = [
  37.minutes,
  8.minutes,
  4.minutes,
  2.minutes,
  4.minutes,
  36.minutes,
  41.minutes,
  104.minutes
]

consumption_phases = [
  162.162 | "W",
  750 | "W",
  1500 | "W",
  3000 | "W",
  1500 | "W",
  166.666 | "W",
  146.341 | "W",
  0 | "W"
],

result = eds.calculate_cheapest_period(ZonedDateTime.now.to_instant,
                                          24.hours.from_now.to_instant,
                                          duration_phases,
                                          consumption_phases)

logger.info "Cheapest start #{result["CheapestStart"]}"
logger.info "Lowest price #{result["LowestPrice"]}"
logger.info "Highest price #{result["HighestPrice"]}"
logger.info "Most expensive start #{result["MostExpensiveStart"]}"

# This is a simpler version taking advantage of the fact that each interval here represents 0.1 kWh of consumed energy.
# In this example we have to provide the total duration to make sure we fit the latest end. This is because there is no
# registered consumption in the last phase.
# Here we are using an alternative way of constructing an array of Durations.
# The `#minutes` method on an Integer object returns a corresponding Duration object.
duration_phases = [37, 8, 4, 2, 4, 36, 41].map { |i| i.minutes }

result = eds.calculate_cheapest_period(ZonedDateTime.now.to_instant,
                                          24.hours.from_now.to_instant,
                                          236.minutes,
                                          duration_phases,
                                          0.1 | "kWh")
```

:::

::: tab Python

```python
from datetime import datetime, timedelta, timezone
from openhab import rule
from openhab.actions import Things
from org.openhab.core.library.types import QuantityType

eds_actions = Things.getActions("energidataservice", "energidataservice:service:energidataservice")

# Get prices and convert to Python dictionary with datetime as keys.
price_dict = {
    datetime.fromtimestamp(entry.getKey().getEpochSecond(), tz=timezone.utc): float(entry.getValue().doubleValue())
    for entry in eds_actions.getPrices().entrySet()
}
hour_start = datetime.now(tz=timezone.utc).replace(minute=0, second=0, microsecond=0)
self.logger.info("Current total price excl. VAT: {}".format(price_dict.get(hour_start)))

price_dict = {
    datetime.fromtimestamp(entry.getKey().getEpochSecond(), tz=timezone.utc): float(entry.getValue().doubleValue())
    for entry in eds_actions.getPrices("SpotPrice,GridTariff").entrySet()
}
self.logger.info("Current spot price + grid tariff excl. VAT: {}".format(price_dict.get(hour_start)))

price = eds_actions.calculatePrice(datetime.now(tz=timezone.utc), datetime.now(tz=timezone.utc) + timedelta(hours=1), QuantityType("150 W"))
if price is not None:
    self.logger.info("Total price for using 150 W for the next hour: {}".format(price))

duration_phases = [
    timedelta(minutes=37),
    timedelta(minutes=8),
    timedelta(minutes=4),
    timedelta(minutes=2),
    timedelta(minutes=4),
    timedelta(minutes=36),
    timedelta(minutes=41),
    timedelta(minutes=104)
]

power_phases = [
    QuantityType("162.162 W"),
    QuantityType("750 W"),
    QuantityType("1500 W"),
    QuantityType("3000 W"),
    QuantityType("1500 W"),
    QuantityType("166.666 W"),
    QuantityType("146.341 W"),
    QuantityType("0 W")
]

result = eds_actions.calculateCheapestPeriod(datetime.now(tz=timezone.utc), datetime.now(tz=timezone.utc) + timedelta(hours=12), duration_phases, power_phases)
self.logger.info("Cheapest start: {}".format(result.get("CheapestStart")))
self.logger.info("Lowest price: {}".format(result.get("LowestPrice")))
self.logger.info("Highest price: {}".format(result.get("HighestPrice")))
self.logger.info("Most expensive start: {}".format(result.get("MostExpensiveStart")))

# This is a simpler version taking advantage of the fact that each interval here represents 0.1 kWh of consumed energy.
# In this example we have to provide the total duration to make sure we fit the latest end. This is because there is no
# registered consumption in the last phase.
duration_phases = [
    timedelta(minutes=37),
    timedelta(minutes=8),
    timedelta(minutes=4),
    timedelta(minutes=2),
    timedelta(minutes=4),
    timedelta(minutes=36),
    timedelta(minutes=41)
]

result = eds_actions.calculateCheapestPeriod(datetime.now(tz=timezone.utc), datetime.now(tz=timezone.utc) + timedelta(hours=12), timedelta(minutes=236), duration_phases, QuantityType("0.1 kWh"))
```

:::

::::

### Persistence Rule Example

:::: tabs

::: tab DSL

```java
var hourStart = now.plusHours(2).truncatedTo(ChronoUnit.HOURS)
var price = SpotPrice.persistedState(hourStart).state
logInfo("Spot price two hours from now", price.toString)
```

:::

::: tab JavaScript

```javascript
var hourStart = time.toZDT().plusHours(2).truncatedTo(time.ChronoUnit.HOURS);
var price = items.SpotPrice.persistence.persistedState(hourStart).quantityState;
console.log("Spot price two hours from now: " + price);
```

:::

::: tab JRuby

```ruby
hour_start = 2.hours.from_now.truncated_to(ChronoUnit::HOURS)
price = SpotPrice.persisted_state(hour_start)
logger.info "Spot price two hours from now: #{price}"
```

:::

::::

### Trigger Channel Example

:::: tabs

::: tab DSL

```java
rule "Day-ahead event"
when
    Channel 'energidataservice:service:energidataservice:electricity#event' triggered 'DAY_AHEAD_AVAILABLE'
then
    logInfo("Day-ahead", "Day-ahead prices for the next day are now available")
end
```

:::

::: tab JavaScript

```javascript
rules.when()
    .channel('energidataservice:service:energidataservice:electricity#event').triggered('DAY_AHEAD_AVAILABLE')
    .then(event =>
    {
        console.log('Day-ahead prices for the next day are now available');
    })
    .build("Day-ahead event");
```

:::

::: tab JRuby

```ruby
rule "Day-ahead event" do
  channel "energidataservice:service:energidataservice:electricity#event", triggered: "DAY_AHEAD_AVAILABLE"
  run do
    logger.info "Day-ahead prices for the next day are now available"
  end
end
```

:::

::::
