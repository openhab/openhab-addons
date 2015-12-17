/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.karaf.internal.command;

import static org.apache.karaf.shell.support.ansi.SimpleAnsi.INTENSITY_BOLD;
import static org.apache.karaf.shell.support.ansi.SimpleAnsi.INTENSITY_NORMAL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.wrapper.WrapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Karaf shell command 'openhab:install-service' that installs openHAB as a
 * system service . This command will first call the default Karaf wrapper
 * service installation to install a Java Service Wrapper. After installation,
 * it will inject the current JVM arguments into the wrapper.conf. This will
 * make sure that all necessary properties like openhab.home, etc are available
 * when running as a system service.
 * 
 * @author Davy Vanherbergen
 */
@Command(scope = "openhab", name = "install-service", description = "Install openHAB as a system service.")
@Service
public class InstallServiceCommand implements Action {

    private final Logger logger = LoggerFactory.getLogger(InstallServiceCommand.class);

    private static final String SERVICE_NAME = "openHAB";

    private static final String SERVICE_DISPLAY_NAME = "openHAB runtime";

    private static final String SERVICE_DESCRIPTION = "System service for openHAB.";

    private static final String SERVICE_START_TYPE = "AUTO_START";

    private static final String[] ARG_EXCLUSION_LIST = new String[] { "-Djava.endorsed", "-Djava.ext", "-Dkaraf.home", "-Dkaraf.base", "-Dkaraf.data",
	    "-Dkaraf.etc", "-Dkaraf.start", "-agentlib" };

    @Reference
    private WrapperService service;

    @Override
    public Object execute() throws Exception {

	System.out.println("");
	System.out.println("Starting openHAB system service installation...");
	System.out.println("");

	// get current JVM arguments
	RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
	List<String> jvmArguments = runtimeMxBean.getInputArguments();
	List<String> argumentsToAdd = new ArrayList<String>();

	// filter out standard karaf and debug arguments
	for (String arg : jvmArguments) {
	    boolean excluded = false;
	    for (String exclude : ARG_EXCLUSION_LIST) {
		if (arg.toLowerCase().startsWith(exclude.toLowerCase())) {
		    excluded = true;
		    break;
		}
	    }
	    if (!excluded) {
		argumentsToAdd.add(arg);
	    }
	}

	// install Karaf service
	File[] files = service.install(SERVICE_NAME, SERVICE_DISPLAY_NAME, SERVICE_DESCRIPTION, SERVICE_START_TYPE);
	File wrapperConf = files[0];
	File serviceFile = files[1];
	File systemdFile = files[2];

	// read contents of config file.
	List<String> content = new ArrayList<String>();
	InputStream is = null;
	try {
	    is = new FileInputStream(wrapperConf);
	    Scanner scanner = new Scanner(is);
	    while (scanner.hasNextLine()) {
		String line = scanner.nextLine();
		if (line.indexOf("/log/") > 0) {
		    // update log location
		    line = line.replace("/log/", "/logs/");
		}
		content.add(line);
	    }
	    scanner.close();
	} finally {
	    safeClose(is);
	}

	// find max index of java properties
	int maxIndex = 1;
	for (String line : content) {
	    if (line.startsWith("wrapper.java.additional.") && line.indexOf('=') > 0) {
		String key = line.substring(0, line.indexOf('='));
		String value = line.substring(line.indexOf('='));
		try {
		    int propIndex = Integer.parseInt(key.substring(key.lastIndexOf('.') + 1));
		    if (propIndex > maxIndex) {
			maxIndex = propIndex;
		    }
		} catch (NumberFormatException e) {
		    logger.error("Config file contains invalid property {}", key);
		}
		// let's not add duplicates
		argumentsToAdd.remove(value);
	    }
	}

	// overwrite wrapper.conf file with new contents
	PrintStream out = null;
	try {
	    out = new PrintStream(new FileOutputStream(wrapperConf));
	    for (String line : content) {
		out.println(line);
		if (line.startsWith("wrapper.java.additional." + maxIndex)) {
		    // inject our JVM arguments here
		    for (String arg : argumentsToAdd) {
			out.println("wrapper.java.additional." + ++maxIndex + "=" + arg);
		    }
		}
	    }
	} finally {
	    safeClose(out);
	}

	// print post installation steps to be done by user
	System.out.println("");
	String os = System.getProperty("os.name", "Unknown");
	if (os.startsWith("Win")) {
	    printPostInstallStepsWindows(serviceFile);
	} else if (os.startsWith("Mac OS X")) {
	    printPostInstallStepsOsX(createOpenHabPList());
	} else if (os.startsWith("Linux")) {
	    File debianVersion = new File("/etc/debian_version");
	    File redhatRelease = new File("/etc/redhat-release");
	    if (redhatRelease.exists()) {
		printPostInstallStepsRedHat(serviceFile);
	    } else if (debianVersion.exists()) {
		printPostInstallStepsDebian(serviceFile);
	    } else {
		printPostInstallStepsRedHat(serviceFile);
		System.out.println("");
		printPostInstallStepsDebian(serviceFile);
	    }
	    if (systemdFile != null) {
		System.out.println("");
		printPostInstallStepsSystemd(systemdFile);
	    }
	}
	System.out.println("");

	return null;
    }

    /**
     * Print the necessary next steps to complete the installation on Red Hat to
     * the console.
     * 
     * @param serviceFile
     *            wrapper service file
     */
    private void printPostInstallStepsRedHat(File serviceFile) {

	System.out.println(INTENSITY_BOLD + "RedHat/Fedora/CentOS Linux system detected (SystemV):" + INTENSITY_NORMAL);
	System.out.println("  To install the service:");
	System.out.println("    $ ln -s " + serviceFile.getPath() + " /etc/init.d/");
	System.out.println("    $ chkconfig " + serviceFile.getName() + " --add");
	System.out.println("");
	System.out.println("  To start the service when the machine is rebooted:");
	System.out.println("    $ chkconfig " + serviceFile.getName() + " on");
	System.out.println("");
	System.out.println("  To disable starting the service when the machine is rebooted:");
	System.out.println("    $ chkconfig " + serviceFile.getName() + " off");
	System.out.println("");
	System.out.println("  To start the service:");
	System.out.println("    $ service " + serviceFile.getName() + " start");
	System.out.println("");
	System.out.println("  To stop the service:");
	System.out.println("    $ service " + serviceFile.getName() + " stop");
	System.out.println("");
	System.out.println("  To uninstall the service :");
	System.out.println("    $ chkconfig " + serviceFile.getName() + " --del");
	System.out.println("    $ rm /etc/init.d/" + serviceFile.getPath());

    }

    /**
     * Print the necessary next steps to complete the installation on Debian to
     * the console.
     * 
     * @param serviceFile
     *            wrapper service file
     */
    private void printPostInstallStepsDebian(File serviceFile) {

	System.out.println(INTENSITY_BOLD + "Ubuntu/Debian Linux system detected (SystemV):" + INTENSITY_NORMAL);
	System.out.println("  To install the service:");
	System.out.println("    $ ln -s " + serviceFile.getPath() + " /etc/init.d/");
	System.out.println("");
	System.out.println("  To start the service when the machine is rebooted:");
	System.out.println("    $ update-rc.d " + serviceFile.getName() + " defaults");
	System.out.println("");
	System.out.println("  To disable starting the service when the machine is rebooted:");
	System.out.println("    $ update-rc.d -f " + serviceFile.getName() + " remove");
	System.out.println("");
	System.out.println("  To start the service:");
	System.out.println("    $ /etc/init.d/" + serviceFile.getName() + " start");
	System.out.println("");
	System.out.println("  To stop the service:");
	System.out.println("    $ /etc/init.d/" + serviceFile.getName() + " stop");
	System.out.println("");
	System.out.println("  To uninstall the service :");
	System.out.println("    $ rm /etc/init.d/" + serviceFile.getName());

    }

    /**
     * Print the necessary next steps to complete the installation on Systemd
     * compliant linux to the console.
     * 
     * @param systemdFile
     *            systemd file
     */
    private void printPostInstallStepsSystemd(File systemdFile) {

	System.out.println(INTENSITY_BOLD + "For systemd compliant Linux: " + INTENSITY_NORMAL);
	System.out.println("  To install the service (and enable at system boot):");
	System.out.println("   $ systemctl enable " + systemdFile.getPath());
	System.out.println("");
	System.out.println("  To start the service:");
	System.out.println("   $ systemctl start " + SERVICE_NAME);
	System.out.println("");
	System.out.println("  To stop the service:");
	System.out.println("   $ systemctl stop " + SERVICE_NAME);
	System.out.println("");
	System.out.println("  To check the current service status:");
	System.out.println("   $ systemctl status " + SERVICE_NAME);
	System.out.println("");
	System.out.println("  To see service activity journal:");
	System.out.println("   $ journalctl -u " + SERVICE_NAME);
	System.out.println("");
	System.out.println("  To uninstall the service (and disable at system boot):");
	System.out.println("   $ systemctl disable " + SERVICE_NAME);

    }

    /**
     * Print the necessary next steps to complete the installation on Windows to
     * the console.
     * 
     * @param serviceFile
     *            wrapper service file
     */
    private void printPostInstallStepsWindows(File serviceFile) {

	System.out.println(INTENSITY_BOLD + "MS Windows system detected:" + INTENSITY_NORMAL);
	System.out.println("To install the service, run: ");
	System.out.println("  C:> " + serviceFile.getPath() + " install");
	System.out.println("");
	System.out.println("Once installed, to start the service run: ");
	System.out.println("  C:> net start \"" + SERVICE_NAME + "\"");
	System.out.println("");
	System.out.println("Once running, to stop the service run: ");
	System.out.println("  C:> net stop \"" + SERVICE_NAME + "\"");
	System.out.println("");
	System.out.println("Once stopped, to remove the installed the service run: ");
	System.out.println("  C:> " + serviceFile.getPath() + " remove");

    }

    /**
     * Print the necessary next steps to complete the installation on OsX to the
     * console.
     * 
     * @param serviceFile
     *            openHAB pList file
     */
    private void printPostInstallStepsOsX(File serviceFile) {

	System.out.println(INTENSITY_BOLD + "Mac OS X system detected:" + INTENSITY_NORMAL);
	System.out.println("");
	System.out.println("To configure openHAB as a " + INTENSITY_BOLD + "user service" + INTENSITY_NORMAL + " execute the following file move:");
	System.out.println("> mv " + serviceFile.getPath() + " ~/Library/LaunchAgents/");
	System.out.println("");
	System.out.println("To configure openHAB as a " + INTENSITY_BOLD + "system service" + INTENSITY_NORMAL
		+ " execute the following file move and authorisation changes:");
	System.out.println("> sudo mv " + serviceFile.getPath() + " /Library/LaunchDaemons/");
	System.out.println("> sudo chown root:wheel /Library/LaunchDaemons/" + serviceFile.getName());
	System.out.println("> sudo chmod u=rw,g=r,o=r /Library/LaunchDaemons/" + serviceFile.getName());
	System.out.println("");
	System.out.println("To test your service: ");
	System.out.println("> launchctl load ~/Library/LaunchAgents/" + serviceFile.getName());
	System.out.println("> launchctl start openHAB");
	System.out.println("> launchctl stop openHAB");
	System.out.println("");
	System.out.println("After restart of your session or system you can use the launchctl command to start and stop your service");
	System.out.println("");
	System.out.println("For removing the service call:");
	System.out.println("> launchctl remove openHAB");

    }

    /**
     * Convert the default generated karaf.plist to the openHAB.plist.
     * 
     * @return File openHAB.plist
     * @throws FileNotFoundException 
     */
    private File createOpenHabPList() throws FileNotFoundException {

	// tweak plist file
	String plistPath = System.getProperty("karaf.base") + "/bin/";
	File karafPlist = new File(plistPath + "org.apache.karaf.openHAB.plist");
	File openHabPlist = new File(plistPath + "openHAB.plist");

	InputStream is = null;
	PrintStream out = null;
	try {
	    is = new FileInputStream(karafPlist);
	    out = new PrintStream(new FileOutputStream(openHabPlist));
	    Scanner scanner = new Scanner(is);
	    while (scanner.hasNextLine()) {
		String line = scanner.nextLine();
		if (line.indexOf("org.apache.karaf.KARAF") > 0) {
		    line = line.replace("org.apache.karaf.KARAF", "openHAB");
		}
		out.println(line);
	    }
	    scanner.close();
	} finally {
	    safeClose(is);
	    safeClose(out);
	}
	karafPlist.delete();
	return openHabPlist;
    }

    /**
     * Silently close inputstream
     * 
     * @param is
     *            inputstream
     */
    private void safeClose(InputStream is) {
	if (is == null)
	    return;
	try {
	    is.close();
	} catch (Throwable t) {
	    logger.error("Error closing inputstream", t);
	}
    }

    /**
     * Silently close outputstream
     * 
     * @param is
     *            outputstream
     */
    private void safeClose(OutputStream os) {
	if (os == null)
	    return;
	try {
	    os.close();
	} catch (Throwable t) {
	    logger.error("Error closing outputstream", t);
	}
    }
}
