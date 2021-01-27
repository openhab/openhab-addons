# HCC Rubbish Collection Binding

Hamilton City Council (NZ) _"Fight the Landfill"_ binding. This binding will keep track of your rubbish collection days and uses the [Fight the Landfill](https://www.fightthelandfill.co.nz/) website API to fetch the upcoming collection dates.

## Supported Things

A single supported thing called `collection`.

## Thing Configuration

The thing supports one setting labelled `address` which is your street number and name as it appears on Google. *__IE:__ 1 Victoria Street*

> Note: The above address will not return any data.

*__If the address is not valid or rubbish collection service does not apply (for example, a business address) then an `CONFIGURATION_ERROR` will occur.__*

## Channels

| channel   | type   | description                                              |
| --------- | ------ | -------------------------------------------------------- |
| day       | String | The upcoming rubbish collection day of the week          |
| redbin    | Date   | The next red bin collection day                          |
| yellowbin | Date   | The next yellow bin                                      |
