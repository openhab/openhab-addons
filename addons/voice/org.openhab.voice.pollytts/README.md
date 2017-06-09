# Pollytts Text-to-Speech

## Overview

Polly is a Internet based TTS service hosted by Amazon
It requires authorization Keys to get access to this service. T

## Samples


```
```

## Configuration

You have to add configuration data by adding a file "pollytts.cfg" to the services folder.
Contents e.g. :
######################## Polly  Text-to-Speech Engine ########################
#configuration data from Amazon Polly Service when registering
accessKey=BKIAJIBOBQWL35PUIQLZ
secretKey=1zv5TS96WiJa/zBobbyeVPdeKrNkui7GwkYD8x
serviceRegion=us-east-1


```
```


## Caching

The Polly extension does cache audio files from previous requests, to reduce traffic, improve performance, reduce number of requests and provide same time offline capability.

For convenience, there is a tool where the audio cache can be generated in advance, to have a prefilled cache when starting this extension. You have to copy the generated data to your userdata/pollytts/cache folder.

Synopsis of this tool:

```
Usage: java org.openhab.voice.pollytts.tool.CreateTTSCache <args>
Arguments: --accessKey <akey> --secretKey <skey> --regionVal <region> <cache-dir> <voice-Name> { <text> | @inputfile }

  akey       the Polly Access Key, e.g. "A123456789"
  skey       the Polly Secret Key, e.g. "S123456789"
  region     the Polly server region, e.g. "us-east-1"
  cache-dir  directory where the files will be stored, e.g. "pollytts-cache"
  voice-name Polly voice name, has to be valid, e.g. "Joey", "Joanna"
             names are unique across all languages
             so selecting a name selects a language 
  text       the text to create audio file for, e.g. "Hello World"
  inputfile  name of a file, e.g. "@message.txt"
             all the lines within the file will be translated

Sample: java org.openhab.voice.pollytts.tool.CreateTTSCache --accessKey A1234567890
                                      --secretKey S1234567890 --regionVal us-east-1
                                      cache en-US @messages.txt



```


## Open Issues

* add all media formats
* add expiration method for obsolete cached files
