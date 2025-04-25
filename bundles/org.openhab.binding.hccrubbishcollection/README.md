# HCC Rubbish Collection Binding

A Hamilton City Council (NZ) _"Fight the Landfill"_ binding.
This binding will keep track of your rubbish collection days and uses the [Fight the Landfill](https://www.fightthelandfill.co.nz/) website API to fetch the upcoming collection dates.

## Supported Things

A single supported thing called `collection`.

## Thing Configuration

The thing supports one setting labelled `address` which is your street number and name as it appears on Google.<br>
_For Example:
1 Victoria Street_

> Note: The above address example is not valid as it is a business address.

_If the address is not valid or rubbish collection service does not apply (for example, a business address) then a `CONFIGURATION_ERROR` will occur._

## Channels

| channel          | type   | description                                                          |
| ---------------- | ------ | -------------------------------------------------------------------- |
| day              | Number | The upcoming rubbish collection day of the week (1=Monday, 7=Sunday) |
| general          | Date   | The next general household (red bin) collection day                  |
| recycling        | Date   | The next recycling (yellow bin, glass bin) colleciton day            |
| collection-event | Event  | Event trigger on the day of the rubbish                              |

### Collection Event

The collection event `collection-event` triggers on the day of rubbish collection.

#### Events

| event     | description                     |
| --------- | ------------------------------- |
| GENERAL   | General household rubbish event |
| RECYCLING | Recycling rubbish event         |

#### Configuration

You can set an `offset` in minutes.
This can then trigger the collection event before or after the normal time of 12:00am on the day of the collection.

_For Example:
If you want the event to trigger at 7pm the day before, to remind you to take out the bins, then set the `offset` to `-300` (5 hours x 60 minutes)._
