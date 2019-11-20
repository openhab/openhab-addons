# GraalJS Script Engine Provider

This binding provides the GraalJS scripting engine and a CommonJS module loader. This allows running an 
up-to-date version of ecmascript, as well as using CommonJS style libraries.

## Writing scripts

Scripts should be written in the same manner as you would with the standard, existing Nashorn script engine,
so place them under `$OPENHAB_CONF/automation/jsr223`. It is recommended that script directories use the
following layout:

```
$OPENHAB_CONF/automation
├── jsr223
│   └── javascript
│       ├── community
│       │   └── some_community_script.js <- community scripts live here
│       ├── core
│       │   └── some_core_script.js <- core scripts live here
│       └── personal
│           └── some_personal_script.js <- your personal scripts live here
└── lib
    └── javascript
        ├── community
        │   └── some_community_library.js <- community libs live here
        ├── core
        │   └── some_core_library.js <- core libs live here
        └── personal
            ├── some_personal_library.js <- your personal libs live here
            └── node_modules
                └── some_3rd_party_library.js <- 3rd party libs live here
```

[GraalJS](https://github.com/graalvm/graaljs) supports the lastest ecmascript specs, so (for example) using ES6
constructs are fine (Nashorn only supports ES5).

## Using modules

Modules are supported when written in the CommonJS-style, the same as NodeJS uses. Existing NodeJS modules 
should work fine, assuming that they are pure javascript.

An example of javascript lib code:
```
exports.my_great_feature = function(...) { /* do stuff */}
```

An example of using it:
```
let my_module = require('my_module');
my_module.my_great_feature(...);
```

(Note that CommonJS module support can be disabled by setting a java system property of `graaljs.script.commonjs.disabled` to `true`)

### Library location and loading order

Prefixed modules will be loaded relative to the current directory. E.g. `require('./module')` will look for a file
`module.js` in the same directory as the file containing the require statement.
Unprefixed modules will be searched for in the following order under $OPENHAB_CONF/automation/lib:
- `javascript/personal`
- `javascript/community`
- `javascript/core`

(Note that this behaviour can be overriden by setting a java system property of `graaljs.commonjs.lib.paths` 
specifying a semicolon-delimited list of load paths.)

### NodeJS module loading paths

As well as searching for .js files in the library directories, the `node_modules` directory in any of these paths will 
also be searched, as well as any directories with the name of the required module. Any directories containing modules
will have their `index.js` used, or whatever is specified in `package.json`, just like NodeJS. This means that you can
install and use any 3rd party library written for NodeJS, as long as it does not use NodeJS-specific APIs. To install
a 3rd party module for use as a dependency for a personal script or module, simply run `npm install <module>` in the 
`lib/javascript/personal` directory (and the `node_modules` directory will be created if it doesn't already exist).

## Debugging Scripts

Whilst attaching and stepping though scripts is not supported, script stack traces will be logged in the namespace 
`org.openhab.automation.script.javascript.#stack`. This can be disabled by setting a java system property of 
`graaljs.script.debug.disabled` to `true`.