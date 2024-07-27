# VAT Transformation Service

The VAT Transformation Service adds VAT (Value-Added Tax) to a given input amount.
The input string must be either an ISO 3166 alpha-2 country code or a percentage, i.e. numerical format.

## Examples

### Display

```java
Number CurrentSpotPrice "Current Spot Price incl. VAT [VAT(12.5):%s]" <price>
```

### In a Rule

Add Danish VAT to price:

:::: tabs

::: tab DSL

```java
var Number price = 499
logInfo("Price", "Price incl. VAT: " + transform("VAT", "DK", price.toString))
```

:::

::: tab JavaScript

```javascript
var price = 499;
console.log("Price incl. VAT: " + actions.Transformation.transform("VAT", "DK", price.toString()));
```

:::

::: tab JRuby

```ruby
price = 499
# The currency name can be either a Symbol or a String
# The input value doesn't need to be converted to string, however, the return value is a String
price_incl_vat = transform(:vat, :DK, price)
logger.info "Price incl. VAT: #{price_incl_vat}"
```

:::

::::

## Usage as a Profile

The functionality of this `TransformationService` can also be used in a `Profile` on an `ItemChannelLink`.
This is the most powerful usage since VAT will be added without providing any explicit country code, percentage or configuration.
To use this, an `.items` file can be configured as follows:

```java
Number CurrentSpotPrice "Current Spot Price" <price> { channel="<channelUID>" [profile="transform:VAT"] }
```

To override VAT percentage for configured system country:

```java
Number CurrentSpotPrice "Current Spot Price" <price> { channel="<channelUID>" [profile="transform:VAT", percentage="12.5"] }
```

If VAT is not known for the configured country or the provided percentage is invalid, the default is 0%, so the input value will be put into the transformation without any changes.

Please note: This profile is a one-way transformation, i.e. only values from a device towards the item are changed, the other direction is left untouched.
