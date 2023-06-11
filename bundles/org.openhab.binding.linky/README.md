# Linky Binding

This binding uses the API provided by Enedis to retrieve your energy consumption data.
You need to create an Enedis account [here](https://espace-client-connexion.enedis.fr/auth/UI/Login?realm=particuliers) if you don't have one already.

Please ensure that you have accepted their conditions, and check that you can see graphs on the website.
Especially, check hourly view/graph. Enedis may ask for permission the first time to start collecting hourly data. 
The binding will not provide these informations unless this step is ok.

## Supported Things

There is one supported thing : the `linky` thing is retrieving the consumption of your home from the [Linky electric meter](https://www.enedis.fr/linky-compteur-communicant).

## Discovery

This binding does not provide discovery service.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has the following configuration parameters:

| Parameter      | Description                    |
|----------------|--------------------------------|
| username       | Your Enedis platform username. |
| password       | Your Enedis platform password. |
| internalAuthId | The internal authID            |

This version is now compatible with the new API of Enedis (deployed from june 2020).
To avoid the captcha login, it is necessary to log before on a classical browser (e.g Chrome, Firefox) and to retrieve the user cookies (internalAuthId).

Instructions given for Firefox : 

1. Go to https://mon-compte-client.enedis.fr/.
2. Select "Particulier" in the drop down list and click on the "Connexion" button.
3. You'll be redirected to a page where you'll have to enter you Enedis account email address and check the "Je ne suis pas un robot" checkbox.
4. Clic on "Suivant".
5. In the login page, prefilled with your mail address, enter your Enedis account password and click on "Connexion à Espace Client Enedis".
6. You will be directed to your Enedis account environment. Get back to previous page in you browser.
7. Disconnect from your Enedis account
8. Repeat steps 1, 2. You should arrive directly on step 5, then open the developer tool window (F12) and select "Stockage" tab. In the "Cookies" entry, select "https://mon-compte-enedis.fr". You'll find an entry named "internalAuthId", copy this value in your openHAB configuration.

## Channels

The information that is retrieved is available as these channels:

| Channel ID        | Item Type     | Description                  |
|-------------------|---------------|------------------------------|
| daily#yesterday   | Number:Energy | Yesterday energy usage       |
| daily#power       | Number:Power  | Yesterday's peak power usage |
| daily#timestamp   | DateTime      | Timestamp of the power peak  |
| weekly#thisWeek   | Number:Energy | Current week energy usage    |
| weekly#lastWeek   | Number:Energy | Last week energy usage       |
| monthly#thisMonth | Number:Energy | Current month energy usage   |
| monthly#lastMonth | Number:Energy | Last month energy usage      |
| yearly#thisYear   | Number:Energy | Current year energy usage    |
| yearly#lastYear   | Number:Energy | Last year energy usage       |

## Console Commands

The binding provides one specific command you can use in the console.
Enter the command `openhab:linky` to get the usage.

```
Usage: openhab:linky <thingUID> report <start day> <end day> [<separator>] - report daily consumptions between two dates
```

The command `report` reports in the console the daily consumptions between two dates.
If no dates are provided, the last 7 are considered by default.
Start and end day are formatted yyyy-mm-dd.

Here is an example of command you can run: `openhab:linky linky:linky:local report 2020-11-15 2020-12-15`.

## Docker specificities

In case you are running openHAB inside Docker, the binding will work only if you set the environment variable `CRYPTO_POLICY` to the value "unlimited" as documented [here](https://github.com/openhab/openhab-docker#java-cryptographic-strength-policy).

## Full Example

### Thing

```
Thing linky:linky:local "Compteur Linky" [ username="example@domaine.fr", password="******" ]
```

### Items

```
Number:Energy ConsoHier "Conso hier [%.0f %unit%]" <energy> { channel="linky:linky:local:daily#yesterday" }
Number:Energy ConsoSemaineEnCours "Conso cette semaine [%.0f %unit%]" <energy> { channel="linky:linky:local:weekly#thisWeek" }
Number:Energy ConsoSemaineDerniere "Conso semaine dernière [%.0f %unit%]" <energy> { channel="linky:linky:local:weekly#lastWeek" }
Number:Energy ConsoMoisEnCours "Conso ce mois [%.0f %unit%]" <energy> { channel="linky:linky:local:monthly#thisMonth" }
Number:Energy ConsoMoisDernier "Conso mois dernier [%.0f %unit%]" <energy> { channel="linky:linky:local:monthly#lastMonth" }
Number:Energy ConsoAnneeEnCours "Conso cette année [%.0f %unit%]" <energy> { channel="linky:linky:local:yearly#thisYear" }
Number:Energy ConsoAnneeDerniere "Conso année dernière [%.0f %unit%]" <energy> { channel="linky:linky:local:yearly#lastYear" }
```
