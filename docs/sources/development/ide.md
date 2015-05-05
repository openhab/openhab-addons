# Setting up a development environment for openHAB 2

If you are a developer yourself, you might want to setup a development environment, so that you can debug and develop openHAB 2 yourself.

Note that the project build is completely mavenized - so running "mvn install" on the repository root will nicely build the product. For development and debugging, we recommend using an Eclipse IDE though. It should be possible to use other IDEs (e.g. NetBeans or IntelliJ), but you will have to work out how to resolve OSGi dependencies etc. yourself. So unless you have a strong reason to go for another IDE, we recommend using Eclipse.

## Prerequisites

Make sure that you have the following things installed on your computer:

1. Git
1. Maven 3.x
1. Oracle JavaSE 7 or 8 
1. [Yoxos Installer](https://yoxos.eclipsesource.com/downloadlauncher.html)

## Setup Instructions

_Note:_ Here you can find a [screencast of the IDE setup on YouTube](https://www.youtube.com/watch?v=8XbQkKd9wkE).

Here are step-by-step instructions:

1. Create [your own fork of the openHAB2 repository](https://github.com/openhab/openhab2/fork) at Github
1. Create a local clone of your repository on the local filesystem by running `git clone https://github.com/<your_github_user>/openhab2.git`
1. Download and execute the file [openHAB2.yoxos](https://raw.githubusercontent.com/openhab/openhab2/master/targetplatform/openhab2.yoxos) (in linux that can be done via command line ./yoxos openhab2.yoxos 
). This will install you an Eclipse IDE with all required features to develop for openHAB 2. Alternatively, you can install all required plugins on top of an existing Eclipse 4.4 installation using this [update site](http://yoxos.eclipsesource.com/userdata/profile/ffb4645d9f172d6d927e2b25f19d1813) or [download a full distribution from Yoxos](http://yoxos.eclipsesource.com/userdata/profile/ffb4645d9f172d6d927e2b25f19d1813), if you register an account there.
1. Create a new workspace and choose `File->Import->General->Existing Projects into Workspace`, enter your repository root folder and press "Finish". Ignore any compilation errors at this point.
1. Switch the perspective to "Plug-in Development"
1. Select the target platform by selecting `Window->Preferences->Plug-in Development->Target Platform->"openHAB 2"` (OS X: `Eclipse->...` instead of `Window->...`) from the main menu and press Ok. All project should now compile without errors.
1. To launch openHAB from within your IDE, go to Run->Run Configurations->Eclipse Application->openHAB_Runtime

### Maven build for binary packages

To produce a binary zip of the runtime yourself, you can simply call `mvn clean package` from the repository root and you will find the result in the folder distribution/target.

To run a single test you have to use following command: `mvn -o org.eclipse.tycho:tycho-surefire-plugin:0.20.0:test` which activates the tycho-surefire-specific goal for OSGI unit test using the fragment bundle xxxx.test on xxxx bundle. The maven -o (offline) option accelerates the project dependency resolution by 10-20x since it lets maven search its local repository. Normally, snapshot-enabled projects are using external repositories to find latest built packages.
