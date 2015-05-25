# Installation of openHAB 2

openHAB comes as a [platform independent zip file](https://bintray.com/artifact/download/openhab/bin/openhab-2.0.0.alpha2-runtime.zip), which you only need to extract to some folder (please note that the folder must not contain any spaces).

You will find the following folders:
 - `conf`: This contains all your user specific configuration files.
 - `runtime`: This contains the openHAB binaries, there should normally be no need to touch anything in here.
 - `userdata`: Here you will find all the data that is generated during runtime: log files, database files, etc. In theory this should be the only folder where openHAB needs write permission on.
 - `addons`: Here you can drop all add-ons that you want to use with openHAB. This can be addons from openHAB 1.x and 2.x likewise. All compatible add-ons can be found in the addons zip file, which is available as a separate download.
 
## Using Add-ons
 
Many add-ons require some configuration. In openHAB 1.x, this was done in the central `openhab.cfg` file. In openHAB 2.x this has changed to separate files in the folder `conf/services`, e.g. the add-on 'acme' is configured in the file `conf/services/acme.cfg`.
 
Likewise, the syntax in the configuration files has changed to not require the namespace anymore, i.e. instead of
```
acme:host=192.168.0.2
```
in `openhab.cfg` you would now simply enter
```
host=192.168.0.2
```
in the `acme.cfg` file.

All openHAB 2 add-ons are made available as a separate download, which also contains all compatible 1.x add-ons for convenience.

## Starting the runtime

Once you have configured your runtime, you can simply start openHAB by calling `start.sh` resp. `start.bat` on Windows. Point your browser to ```http://<hostname>:8080``` and off you go!
