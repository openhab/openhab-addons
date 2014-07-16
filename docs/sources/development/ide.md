# Setting up a development environment for openHAB 2

If you are a developer yourself, you might want to setup a development environment, so that you can debug and develop openHAB 2 yourself.

Note that the project build is completely mavenized - so running "mvn install" on the repository root will nicely build the product. For development and debugging, we recommend using an Eclipse IDE though. It should be possible to use other IDEs (e.g. NetBeans or IntelliJ), but you will have to work out how to resolve OSGi dependencies etc. yourself. So unless you have a strong reason to go for another IDE, we recommend using Eclipse.

## Prerequisites

Make sure that you have the following things installed on your computer:

1. Maven 3.x
1. Oracle JavaSE 7 or 8 
1. [Yoxos Installer](https://yoxos.eclipsesource.com/downloadlauncher.html)

## Setup Instructions

Here are step-by-step instructions:
 
1. Download and execute the file [openHAB2.yoxos](https://raw.githubusercontent.com/openhab/openhab2/master/targetplatform/openhab2.yoxos) (in linux that can be done via command line ./yoxos openhab2.yoxos 
). This will install you an Eclipse IDE with all required features to develop for openHAB 2. Alternatively, you can install all required plugins on top of an existing Eclipse 4.4 installation using this [update site](http://yoxos.eclipsesource.com/userdata/profile/ffb4645d9f172d6d927e2b25f19d1813) or [download a full distribution from Yoxos](http://yoxos.eclipsesource.com/userdata/profile/ffb4645d9f172d6d927e2b25f19d1813), if you register an account there.
1. Create a new workspace and you are all set!
1. To launch openHAB from within your IDE, go to Run->Run Configurations->Eclipse Application->openHAB_Runtime

The IDE setup will automatically do a git clone into the workspace folder under '.repositories/openhab2', so you will find all files located in there.

To produce a binary zip of the runtime yourself, you can simply call `mvn clean install` from the repository root and you will find the result in the folder distribution/target.

To run a single test you have to use following command: `mvn -o org.eclipse.tycho:tycho-surefire-plugin:0.20.0:test` which activates the tycho-surefire-specific goal for OSGI unit test using the fragment bundle xxxx.test on xxxx bundle. The maven -o (offline) option accelerates the project dependency resolution by 10-20x since it lets maven search its local repository. Normally, snapshot-enabled projects are using external repositories to find latest built packages.
