# Pollytts Text-to-Speech

## Overview

PollyTTS is a TTS voice service for Openhab 2.x utilizing a Internet based TTS service provided by amazon called Polly. There are servers set in various geographic locations. It requires a set of API keys provided by Amazon to get access to the service. Provides multiple languages and multiple voices for each language. 
https://aws.amazon.com/polly/

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

When you sign up for Amazon Web Services (AWS), your AWS account is automatically signed up for all services in AWS, including Amazon Polly. 

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

The Pollytts service does cache audio files from previous requests, to reduce traffic, improve performance, reduce number of requests and provide offline capability.  
cacheExpiration - Cache expiration life in days. As Cache files are used their time stamps are updated, files that are never used will be purged if their time stamp exceeds the specified age. A default value of 0 set to disable functionality.  
Example, 365 not used in a year.

audioFormat -  Optional User specified audio format. 
The user can override the system default audio format with their prefered option. 
"mp3" and "ogg" are the only audio formats that are supported.
Once specified use "default" to revert to system default since openhab caches cfg values.
            


### Contents e.g. :  

```
######################## Polly  Text-to-Speech Engine ########################  
#configuration data from Amazon Polly Service when registering  
accessKey=BKIAJIBOBQWL35PUIQLZ  
secretKey=1zv5TS96WiJa/zBobbyeVPdeKrNkui7GwkYD8x  
serviceRegion=us-east-1  
cacheExpiration=40
audioFormat=default  
###################################
```

## Caching







## Open Issues

* tbd
