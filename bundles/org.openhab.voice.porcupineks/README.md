# Porcupine Keyword Spotter

This voice service allows you to use the PicoVoice product Porcupine as your keyword spotter in openHAB.

Porcupine provides on-device wake word detection powered by deep learning.
This add-on should work on all the platforms supported by Porcupine, if you encounter a problem you can try to run one of the Porcupine java demos on your machine.

Important: No voice data listened by this service will be uploaded to the Cloud.
The voice data is processed offline, locally on your openHAB server by Porcupine.
Once in a while, access keys are validated to stay active and this requires an Internet connection.

## How to use it

After installing, you will be able to access the addon options through the openHAB configuration page under the 'Other Services' section.
There you will need to provide your PicoVoice Api Key.

After that, you can select Porcupine as your default Keyword Spotter in your 'Voice' settings.

## Magic Word Configuration

The magic word to spot is gathered from your 'Voice' configuration. 
The default english keyword models are loaded in the addon (also the english language model) so you can use those without adding anything else.

Note that you can use the pico voice platform to generate your own keyword models. 
To use them, you should place the generated file under '\<openHAB userdata\>/porcupine' and configure your magic word to match the file name replacing spaces with '_' and adding the extension '.ppn'.
As an example, the file generated for the keyword "ok openhab" will be named 'ok_openhab.ppn'.

The service will only work if it's able to find the correct ppn for your magic word configuration.

#### Build-in keywords

Remember that they only work with the English language model. (read bellow section)

* alexa
* americano
* blueberry
* bumblebee
* computer
* grapefruits
* grasshopper
* hey google
* hey siri
* jarvis
* ok google
* picovoice
* porcupine
* terminator


## Language support

This service currently supports English, German, French and Spanish. 

Only the English model binary is included with the addon and will be used if the one for your configured language is not found under '\<openHAB userdata\>/porcupine'.

To get the language model files, go to the [Porcupine repo](https://github.com/Picovoice/porcupine/tree/v2.0/lib/common).

Note that the keyword model you use should match the language model.
