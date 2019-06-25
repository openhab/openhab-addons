# HomePilot Addon
by <a href="mailto:kontakt@stundzig.de">Steffen Stundzig</a>

# Configuration
This addon is developed against a *Rademacher HomePilot* Version 1.

To use this addon simple add the following thing to your things file.

```
homepilot:bridge:default [ address="x.x.x.x" ]
``` 

The name *default* could be changed to whatever you want. 
The address is the IP Address of your homepilot.

Currently only HomePilot installations without Auth are supported.
And also only items of the following types:
* Rohrmotor
* Universal Aktor

All devices are auto discovered with name and description. All devices are automatically updated every 10 seconds.  