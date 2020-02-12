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

| Parameter       | Description                    |
|-----------------|--------------------------------|
| username        | Your Enedis platform username. |
| password        | Your Enedis platform password. |

## Channels

The information that is retrieved is available as these channels:

| Channel ID        | Item Type     | Description                |
|-------------------|---------------|----------------------------|
| daily#yesterday   | Number:Energy | Yesterday energy usage     |
| weekly#thisWeek   | Number:Energy | Current week energy usage  |
| weekly#lastWeek   | Number:Energy | Last week energy usage     |
| monthly#thisMonth | Number:Energy | Current month energy usage |
| monthly#lastMonth | Number:Energy | Last month energy usage    |
| yearly#thisYear   | Number:Energy | Current year energy usage  |
| yearly#lastYear   | Number:Energy | Last year energy usage     |

