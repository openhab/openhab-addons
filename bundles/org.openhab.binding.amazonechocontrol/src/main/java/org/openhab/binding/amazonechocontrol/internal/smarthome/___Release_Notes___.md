#Ladies and Gentlemen

Please welcome @Lukask in our community!

Lukas and I worked together to implement the controlling of smart home devices in the alexaechocontrol binding. This means you can switch lights, plugs and other devices which are connected directly to the echo device or over a Alexa Skill. Also the Alexa Guard state in US should work, but is not yet tested because of lake of this feature in Europe.

But this features are very new with a lot of new code and so I do not want to have it in the Release Candidate version which should be as stable as possible.

And so we deliver today 2 different version of the binding. One relative stable Release Candidate version with the latest bugfixes and a development version with all the cool new features and of course all changes of the latest release candidate version.

To make it easy for you, there are now 2 threads in this community, this one for support of the stable version and a new "Beta And Preview: Openhab2 Amazon Echo Control Binding".

If you want test our latest feature, switch now to the new thread.

## New Release Candidate RC3

The RC3 release candidate changed the behavior of the announcement channel a little bit. It can now also be used by echo devices. But this causes a **breaking change**:

If you want to use the channel for Echo Show and Echo Spot devices without a spoken message, please use the JSON syntax format explained in the *Tuturial* section under *Show an announcement on the echo show or echo spot*. 

The Version can be downloaded from the [top most posting of this thread](https://community.openhab.org/t/released-openhab2-amazon-echo-control-binding-controlling-alexa-from-openhab2/37844)


