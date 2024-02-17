# Energi Data Service Binding

This binding integrates electricity prices from the Danish Energi Data Service ("Open energy data from Energinet to society").

This can be used to plan energy consumption, for example to calculate the cheapest period for running a dishwasher or charging an EV.

## Supported Things

All channels are available for thing type `service`.

## Thing Configuration

### `service` Thing Configuration

| Name                  | Type    | Description                                                          | Default       | Required |
|-----------------------|---------|----------------------------------------------------------------------|---------------|----------|
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
- Most rows will have this **Owner**. If in doubt, try to look for rows __not__ having 5790000432752 as owner.

#### Reduced electricity tax applies

For customers using electricity for heating, a reduced electricity tax rate may apply after consuming the first 4000 kWh within a year.
When you are entitled to reduced electricity tax, this option should be set.
This will ensure that thing action calculations use the reduced electricity tax rate when price components are not explicitly provided.
It will not impact channels, see [Electricity Tax](#electricity-tax) for further information.

## Channels

### Channel Group `electricity`

| Channel                  | Type               | Description                                                                    | Advanced |
|--------------------------|--------------------|--------------------------------------------------------------------------------|----------|
| spot-price               | Number:EnergyPrice | Spot price in DKK or EUR per kWh                                               | no       |
| grid-tariff              | Number:EnergyPrice | Grid tariff in DKK per kWh. Only available when `gridCompanyGLN` is configured | no       |
| system-tariff            | Number:EnergyPrice | System tariff in DKK per kWh                                                   | no       |
| transmission-grid-tariff | Number:EnergyPrice | Transmission grid tariff in DKK per kWh                                        | no       |
| electricity-tax          | Number:EnergyPrice | Electricity tax in DKK per kWh                                                 | no       |
| reduced-electricity-tax  | Number:EnergyPrice | Reduced electricity tax in DKK per kWh. For electric heating customers only    | no       |

_Please note:_ There is no channel providing the total price.
Instead, create a group item with `SUM` as aggregate function and add the individual price items as children.
This has the following advantages:

- Full customization possible: Freely choose the channels which should be included in the total.
- An additional item containing the kWh fee from your electricity supplier can be added also.
- Spot price can be configured in EUR while tariffs are in DKK.

If you want electricity tax included in your total price, please add either `electricity-tax` or `reduced-electricity-tax` to the group - depending on which one applies.
See [Electricity Tax](#electricity-tax) for further information.

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

#### Grid Tariff

Discounts are automatically taken into account for channel `grid-tariff` so that it represents the actual price.

The tariffs are downloaded using pre-configured filters for the different [Grid Company GLN's](#global-location-number-of-the-grid-company).
If your company is not in the list, or the filters are not working, they can be manually overridden.
To override filters, the channel `grid-tariff` has the following configuration parameters:

| Name            | Type    | Description                                                                                                                      | Default | Required | Advanced |
|-----------------|---------|----------------------------------------------------------------------------------------------------------------------------------|---------|----------|----------|
| chargeTypeCodes | text    | Comma-separated list of charge type codes                                                                                        |         | no       | yes      |
| notes           | text    | Comma-separated list of notes                                                                                                    |         | no       | yes      |
| start           | text    | Query start date parameter expressed as either YYYY-MM-DD or dynamically as one of `StartOfDay`, `StartOfMonth` or `StartOfYear` |         | no       | yes      |
| offset          | text    | Query start date offset expressed as an ISO 8601 duration                                                                        |         | no       | yes      |

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
| Parameter       | Value      |
|-----------------|------------|
| chargeTypeCodes | CD,CD R    |
| notes           |            |

_Nord Energi Net:_
| Parameter       | Value      |
|-----------------|------------|
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
|--------------------|--------------|-------------------------------------------------------|
| CheapestStart      | `Instant`    | Start time of cheapest calculated period              |
| LowestPrice        | `BigDecimal` | The total price when starting at cheapest start       |
| MostExpensiveStart | `Instant`    | Start time of most expensive calculated period        |
| HighestPrice       | `BigDecimal` | The total price when starting at most expensive start |

#### `calculateCheapestPeriod` from Duration

| Parameter          | Type                        | Description                                            |
|--------------------|-----------------------------|--------------------------------------------------------|
| earliestStart      | `Instant`                   | Earliest start time allowed                            |
| latestEnd          | `Instant`                   | Latest end time allowed                                |
| duration           | `Duration`                  | The duration to fit within the timeslot                |

This is a convenience method that can be used when the power consumption is not known.
The calculation will assume linear consumption and will find the best timeslot based on that.
For this reason the resulting `Map` will not contain the keys `LowestPrice` and `HighestPrice`.

Example:

```javascript
var Map<String, Object> result = actions.calculateCheapestPeriod(now.toInstant(), now.plusHours(12).toInstant(), Duration.ofMinutes(90))
```

#### `calculateCheapestPeriod` from Duration and Power

| Parameter          | Type                        | Description                                            |
|--------------------|-----------------------------|--------------------------------------------------------|
| earliestStart      | `Instant`                   | Earliest start time allowed                            |
| latestEnd          | `Instant`                   | Latest end time allowed                                |
| duration           | `Duration`                  | The duration to fit within the timeslot                |
| power              | `QuantityType<Power>`       | Linear power consumption                               |

This action is identical to the variant above, but with a known linear power consumption.
As a result the price is also included in the result.

Example:

```javascript
var Map<String, Object> result = actions.calculateCheapestPeriod(now.toInstant(), now.plusHours(12).toInstant(), Duration.ofMinutes(90), 250 | W)
```

#### `calculateCheapestPeriod` from Power Phases

| Parameter          | Type                        | Description                                            |
|--------------------|-----------------------------|--------------------------------------------------------|
| earliestStart      | `Instant`                   | Earliest start time allowed                            |
| latestEnd          | `Instant`                   | Latest end time allowed                                |
| durationPhases     | `List<Duration>`            | List of durations for the phases                       |
| powerPhases        | `List<QuantityType<Power>>` | List of power consumption for each corresponding phase |

This variant is similar to the one above, but is based on a supplied timetable.

The timetable is supplied as two individual parameters, `durationPhases` and `powerPhases`, which must have the same size.
This can be considered as different phases of using power, so each list member represents a period with a linear use of power.
`durationPhases` should be a List populated by `Duration` objects, while `powerPhases` should be a List populated by `QuantityType<Power>` objects for that duration of time.

Example:

```javascript
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

var Map<String, Object> result = actions.calculateCheapestPeriod(now.toInstant(), now.plusHours(12).toInstant(), durationPhases, powerPhases)
```

Please note that the total duration will be calculated automatically as a sum of provided duration phases.
Therefore, if the total duration is longer than the sum of phase durations, the remaining duration must be provided as last item with a corresponding 0 W power item.
This is to ensure that the full program will finish before the provided `latestEnd`.

#### `calculateCheapestPeriod` from Energy per Phase

| Parameter          | Type                        | Description                                            |
|--------------------|-----------------------------|--------------------------------------------------------|
| earliestStart      | `Instant`                   | Earliest start time allowed                            |
| latestEnd          | `Instant`                   | Latest end time allowed                                |
| totalDuration      | `Duration`                  | The total duration of all phases                       |
| durationPhases     | `List<Duration>`            | List of durations for the phases                       |
| energyUsedPerPhase | `QuantityType<Energy>`      | Fixed amount of energy used per phase                  |

This variant will assign the provided amount of energy into each phase.
The use case for this variant is a simplification of the previous variant.
For example, a dishwasher may provide energy consumption in 0.1 kWh steps.
In this case it's a simple task to create a timetable accordingly without having to calculate the average power consumption per phase.
Since a last phase may use no significant energy, the total duration must be provided also.

Example:

```javascript
val ArrayList<Duration> durationPhases = new ArrayList<Duration>()
durationPhases.add(Duration.ofMinutes(37))
durationPhases.add(Duration.ofMinutes(8))
durationPhases.add(Duration.ofMinutes(4))
durationPhases.add(Duration.ofMinutes(2))
durationPhases.add(Duration.ofMinutes(4))
durationPhases.add(Duration.ofMinutes(36))
durationPhases.add(Duration.ofMinutes(41))

// 0.7 kWh is used in total (number of phases × energy used per phase)
var Map<String, Object> result = actions.calculateCheapestPeriod(now.toInstant(), now.plusHours(12).toInstant(), Duration.ofMinutes(236), phases, 0.1 | kWh)
```

### `calculatePrice`

| Parameter          | Type                        | Description                                            |
|--------------------|-----------------------------|--------------------------------------------------------|
| start              | `Instant`                   | Start time                                             |
| end                | `Instant`                   | End time                                               |
| power              | `QuantityType<Power>`       | Linear power consumption                               |

**Result:** Price as `BigDecimal`.

This action calculates the price for using given amount of power in the period from `start` till `end`.

Example:

```javascript
var price = actions.calculatePrice(now.toInstant(), now.plusHours(4).toInstant, 200 | W)
```

### `getPrices`

| Parameter          | Type                        | Description                                            |
|--------------------|-----------------------------|--------------------------------------------------------|
| priceComponents    | `String`                    | Comma-separated list of price components to include    |

**Result:** `Map<Instant, BigDecimal>`

The parameter `priceComponents` is a case-insensitive comma-separated list of price components to include in the returned hourly prices.
These components can be requested:

| Price component        | Description              |
|------------------------|--------------------------|
| SpotPrice              | Spot price               |
| GridTariff             | Grid tariff              |
| SystemTariff           | System tariff            |
| TransmissionGridTariff | Transmission grid tariff |
| ElectricityTax         | Electricity tax          |
| ReducedElectricityTax  | Reduced electricity tax  |

Using `null` as parameter returns the total prices including all price components.
If **Reduced Electricity Tax** is set in Thing configuration, `ElectricityTax` will be excluded, otherwise `ReducedElectricityTax`.
This logic ensures consistent and comparable results not affected by artifical changes in the rate for electricity tax two times per year.

Example:

```javascript
var priceMap = actions.getPrices("SpotPrice,GridTariff")
```

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
Group:Number:SUM TotalPrice "Current Total Price" <price>
Number SpotPrice "Current Spot Price" <price> (TotalPrice) { channel="energidataservice:service:energidataservice:electricity#spot-price" [profile="transform:VAT"] }
Number GridTariff "Current Grid Tariff" <price> (TotalPrice) { channel="energidataservice:service:energidataservice:electricity#grid-tariff" [profile="transform:VAT"] }
Number SystemTariff "Current System Tariff" <price> (TotalPrice) { channel="energidataservice:service:energidataservice:electricity#system-tariff" [profile="transform:VAT"] }
Number TransmissionGridTariff "Current Transmission Grid Tariff" <price> (TotalPrice) { channel="energidataservice:service:energidataservice:electricity#transmission-grid-tariff" [profile="transform:VAT"] }
Number ElectricityTax "Current Electricity Tax" <price> (TotalPrice) { channel="energidataservice:service:energidataservice:electricity#electricity-tax" [profile="transform:VAT"] }
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

```javascript
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
logInfo("Total price for using 150 W for the next hour", price.toString)

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
console.log("Total price for using 150 W for the next hour: " + price.toString());

var durationPhases = [];
durationPhases.push(time.Duration.ofMinutes(37));
durationPhases.push(time.Duration.ofMinutes(8));
durationPhases.push(time.Duration.ofMinutes(4));
durationPhases.push(time.Duration.ofMinutes(2));
durationPhases.push(time.Duration.ofMinutes(4));
durationPhases.push(time.Duration.ofMinutes(36));
durationPhases.push(time.Duration.ofMinutes(41));
durationPhases.push(time.Duration.ofMinutes(104));

var consumptionPhases = [];
consumptionPhases.push(Quantity("162.162 W"));
consumptionPhases.push(Quantity("750 W"));
consumptionPhases.push(Quantity("1500 W"));
consumptionPhases.push(Quantity("3000 W"));
consumptionPhases.push(Quantity("1500 W"));
consumptionPhases.push(Quantity("166.666 W"));
consumptionPhases.push(Quantity("146.341 W"));
consumptionPhases.push(Quantity("0 W"));

var result = edsActions.calculateCheapestPeriod(time.Instant.now(), time.Instant.now().plusSeconds(24*60*60), durationPhases, consumptionPhases);

console.log("Cheapest start: " + result.get("CheapestStart").toString());
console.log("Lowest price: " + result.get("LowestPrice"));
console.log("Highest price: " + result.get("HighestPrice"));
console.log("Most expensive start: " + result.get("MostExpensiveStart").toString());

// This is a simpler version taking advantage of the fact that each interval here represents 0.1 kWh of consumed energy.
// In this example we have to provide the total duration to make sure we fit the latest end. This is because there is no
// registered consumption in the last phase.
var durationPhases = [];
durationPhases.push(time.Duration.ofMinutes(37));
durationPhases.push(time.Duration.ofMinutes(8));
durationPhases.push(time.Duration.ofMinutes(4));
durationPhases.push(time.Duration.ofMinutes(2));
durationPhases.push(time.Duration.ofMinutes(4));
durationPhases.push(time.Duration.ofMinutes(36));
durationPhases.push(time.Duration.ofMinutes(41));

var result = edsActions.calculateCheapestPeriod(time.Instant.now(), time.Instant.now().plusSeconds(24*60*60), time.Duration.ofMinutes(236), durationPhases, Quantity("0.1 kWh"));
```

:::

::::

### Persistence Rule Example

:::: tabs

::: tab DSL

```javascript
var hourStart = now.plusHours(2).truncatedTo(ChronoUnit.HOURS)
var price = SpotPrice.historicState(hourStart).state
logInfo("Spot price two hours from now", price.toString)
```

:::

::: tab JavaScript

```javascript
var hourStart = time.toZDT().plusHours(2).truncatedTo(time.ChronoUnit.HOURS);
var price = items.SpotPrice.history.historicState(hourStart).quantityState;
console.log("Spot price two hours from now: " + price);
```

:::

::::
