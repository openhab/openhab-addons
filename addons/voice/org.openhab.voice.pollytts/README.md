# Pollytts Text-to-Speech

## Overview

PollyTTS is a TTS voice service for Openhab 2.x utilizing a Internet based TTS service provided by amazon called Polly. There are five servers set in various geographic locations. It requires a set of API keys provided by Amazon to get access to the service. Provides multiple languages and multiple voices for each language. The free tier includes 5 million characters per month for the first 12 months. Then it's $4 for a million characters after the one year.

## Samples
```
say("Hello there")  
say("Hello there", "pollytts:Joanne", "enhancedjavasound")  
say("" + item.state,"pollytts:Joey", "enhancedjavasound")  
```


## Configuration

You have to add configuration data by adding a file "pollytts.cfg" to the services folder.

Establish Amazon Polly User Credentials to get values for accessKey and secretKey

1.) Sign up for AWS

When you sign up for Amazon Web Services (AWS), your AWS account is automatically signed up for all services in AWS, including Amazon Polly. You are charged only for the services that you use. For example, I use AWS lamda service to host an Amazon echo skill I built. Free tier is 1 Million request per month. I've never had a charge.

2.) Create an IAM User

Services in AWS, such as Amazon Polly, require that you provide credentials when you access them so that the service can determine whether you have permissions to access the resources owned by that service. Within the AWS console, You can create access keys for your AWS account to access the Polly API. You will need three items 1) access key, 2) secret key, and 3) server region to configure the Openhab Polly voice service.

Directions are Here: http://docs.aws.amazon.com/polly/latest/dg/setting-up.html

## Config values:

accessKey - required credential provided by Amazon 

secretKey - required credential provided by Amazon

serviceRegion - Required value select region closest for best response. ServiceRegion is one of the following:  
["us-east-2"] in US East (Ohio)  
["us-east-1"] in US East (N. Virginia)  
["us-west-2"] in US West (Oregon)  
["eu-west-1"] in EU (Ireland)

cacheExpiration - Cache expiration life in days (Optional value). As Cache files are used their timestamps are updated, files that are never used will be purged if their timestamp exceeds the specified age. If not specified, default value of 0 set to disable functionality.  
Example, 365 not used in a year.

audioFormat - (only works under openhab 2.1) Optional User specified audio format. 
The user can override the system default audio format with their prefered option. 
"mp3" and "ogg" are the only audio formats that are supported.
Once specified use "disabled" to revert to system default since openhab caches cfg values.
            


### Contents e.g. :  
```
######################## Polly  Text-to-Speech Engine ########################  
#configuration data from Amazon Polly Service when registering  
accessKey=BKIAJIBOBQWL35PUIQLZ  
secretKey=1zv5TS96WiJa/zBobbyeVPdeKrNkui7GwkYD8x  
serviceRegion=us-east-1  
cacheExpiration=40
audioFormat=mp3  
###################################
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
                                      cache Joey @messages.txt



```


## Open Issues

* tbd
