[![Build Status](https://travis-ci.org/jpg0/ohj.svg?branch=master)](https://travis-ci.org/jpg0/ohj)
# Openhab Javascript Library

This library aims to be a fairly high-level ES6 library to support automation in Openhab.

[API Documentation](https://jpg0.github.io/ohj/ohj/0.1.1/)

## Requirements

- ES6 (e.g. GraalJS)
- CommonJS support

## Installation

- Install the Openhab 'Experimental Rule Engine' in Paper UI
- Install the [GraalJS bundle](https://openhab.jfrog.io/openhab/libs-pullrequest-local/org/openhab/addons/bundles/org.openhab.automation.module.script.graaljs/2.5.0-SNAPSHOT/org.openhab.automation.module.script.graaljs-2.5.0-SNAPSHOT.jar) to upgrade JS runtime to ES6
- Ensure that you have created the automation scripting directories
- Go to the javascript community lib directory: `cd $OPENHAB_CONF/automation/lib/javascript/community`
- `npm i ohj` (you may need to install npm)

## Usage

You should create scripts in $OPENHAB_CONF/automation/jsr223/javascript/personal.

The API can be imported as a standard CommonJS module: `require('ohj')`. The ohj module itself has various sections that
can be imported as properties of the primary import, e.g.

```
//use destructing
const { rules, triggers } = require('ohj');
//or simply
const rules = require('ohj').rules;
```

## Fluent API

The fluent section of the API can be used to write rules in a high-level, readable style.

The cleanest way to use the API is with a `with` statement. This is so that it's possible to use the exported functions
without a prefix. An alternative approach (to allow `'use strict'`) would be to explicitly import the functions that you
use, such as `const {when, then} = require('ohj').fluent`. The following examples will use the `with` style of importing.

Note that for the `timeOfDay` API, you must create a `vTimeOfDay` String item, which is updated like in [the Openhab pattern](https://community.openhab.org/t/design-pattern-time-of-day/15407). A future release will check this.


## Fluent Examples

```
with(require('ohj').fluent){

    //turn on the kitchen light at SUNSET
    when(timeOfDay("SUNSET")).then(sendOn().toItem("KitchenLight"));

    //turn off the kitchen light at 9PM
    when(cron("0 0 21 * * ?")).then(sendOff().toItem("KitchenLight"));

    //set the colour of the hall light to pink at 9PM
    when(cron("0 0 21 * * ?")).then(send("300,100,100").toItem("HallLight")

    //when the switch S1 status changes to ON, then turn on the HallLight
    when(item('S1').changed().toOn()).then(sendOn().toItem('HallLight'));

    //when the HallLight colour changes pink, if the function fn returns true, then toggle the state of the OutsideLight
    when(item('HallLight').changed().to("300,100,100")).if(fn).then(sendToggle().toItem('OutsideLight'));
}

//and some rules which can be toggled by the items created in the 'gRules' Group:

with(require('ohj').fluent.withToggle) {

    //when the HallLight receives a command, send the same command to the KitchenLight
    when(item('HallLight').receivedCommand()).then(sendIt().toItem('KitchenLight'));
 
    //when the HallLight is updated to ON, make sure that BedroomLight1 is set to the same state as the BedroomLight2
    when(item('HallLight').receivedUpdate()).then(copyState().fromItem('BedroomLight1').toItem('BedroomLight2'));

    //when the BedroomLight1 is changed, run a custom function
    when(item('BedroomLight1').changed()).then(() => {
        // do stuff
    });
}
```
