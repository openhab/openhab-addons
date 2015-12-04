# Installation of the openHAB 2 Demo Distribution

If you are new to openHAB 2, you might not want to start building your setup from scratch, but rather use a working setup as a starting point - for this purpose, there is a dedicated [demo distribution](https://openhab.ci.cloudbees.com/job/openHAB2/lastSuccessfulBuild/artifact/distribution/target/distribution-2.0.0-SNAPSHOT-demo.zip) available.

## What's in the Demo?

The package contains a full openHAB 2 runtime, a couple of add-ons that support wide-spread devices, such as Philips Hue, WeMo or Sonos and a sample configuration for a house with show cases for all different item types and UI widgets.

## Installing the Demo

The demo comes as a platform independent zip file, which you only need to extract to some folder (please note that the folder must not contain any spaces). As a prerequisite, you need to have Java 7 or 8 installed on the system. 

## Starting the Demo

Now simply start openHAB by calling `start.sh` resp. `start.bat` (on Windows). Point your web browser to ```http://<hostname>:8080``` and off you go!

## Further Documentation

Please note that openHAB 2 is still in an alpha phase and it is not really meant for end users (that are not developers). For this reason, you won't find any end user documentation yet (apart from what you are reading right now). To learn about features and functionalities, please refer to the [openHAB 1 wiki](https://github.com/openhab/openhab/wiki) for the moment - most of this information also applies to openHAB 2.
