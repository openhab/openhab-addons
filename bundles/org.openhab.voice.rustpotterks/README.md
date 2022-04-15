# Rustpotter Keyword Spotter

This voice service allows you to use the open source library Rustpotter as your keyword spotter in openHAB.
[Rustpotter](https://github.com/GiviMAD/rustpotter) is a free and open-source keywords spotter written in rust.

Rustpotter provides personal on-device wake word detection. You need to generate a model for your keyword using audio samples.

Important: No voice data listened by this service will be uploaded to the Cloud.
The voice data is processed offline, locally on your openHAB server by Rustpotter.

## Configuration

After installing, you will be able to access the service options through the openHAB configuration page in UI (**Settings / Other Services - Rustpotter Keyword Spotter**) to edit them:

* TODO

In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `rustpotterks.cfg`

Its contents should look similar to:

```
TODO
```

## Magic Word Configuration

The magic word to spot is gathered from your 'Voice' configuration. 
The default english keyword models are loaded in the addon (also the english language model) so you can use those without adding anything else.

Note that you can use the pico voice platform to generate your own keyword models. 
To use them, you should place the generated file under '\<openHAB userdata\>/rustpotter' and configure your magic word to match the file name replacing spaces with '_' and adding the extension '.rpw'.
As an example, the file generated for the keyword "ok openhab" will be named 'ok_openhab.rpw'.

The service will only work if it's able to find the correct rpw for your magic word configuration.


## Default Keyword Spotter and Magic Word Configuration

You can setup your preferred default keyword spotter and default magic word in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Rustpotter Keyword Spotter** as **Default Keyword Spotter**.
* Choose your preferred **Magic Word** for your setup.
* Choose optionally your **Listening Switch** item that will be switch ON during the period when the dialog processor has spotted the keyword and is listening for commands.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultKS=rustpotterks
org.openhab.voice:keyword=hey openhab
org.openhab.voice:listeningItem=myItemForDialog
```
