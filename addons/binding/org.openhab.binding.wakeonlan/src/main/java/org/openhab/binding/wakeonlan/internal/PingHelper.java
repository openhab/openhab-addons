/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @author Ganesh Ingle <ganesh.ingle@asvilabs.com>
 */

package org.openhab.binding.wakeonlan.internal;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.commons.exec.ExecuteException;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;

/**
 * Handles built-in Java ping or external command ping
 *
 * @author Ganesh Ingle - Initial contribution
 *
 */
public class PingHelper {
    protected static String pingLocation = null;
    public static final Integer TIMEOUT_SEC = 10;
    public static final Integer TIMEOUT_MILLIS = TIMEOUT_SEC * 1000;
    static {
        detectPingBinaryLocation();
    }

    private static synchronized void detectPingBinaryLocation() {
        String osname = System.getProperty("os.name");
        if (osname.indexOf("win") >= 0) {
            pingLocation = "ping";
        } else {
            for (String loc : new String[] { "/bin", "/usr/sbin", "/sbin", "/usr/bin", "/usr/local/bin",
                    "/usr/local/sbin" }) {
                File f = new File(loc + "/ping");
                if (f.canExecute()) {
                    pingLocation = loc + "/ping";
                }
            }
            if (pingLocation == null) {
                pingLocation = "ping";
            }
        }
    }

    protected static String getPingCommandByOS(String hostnameOrIp) throws UnsupportedOSException {
        // Supported OSes: Linux, Solaris, Unix, Mac, Windows
        String osname = System.getProperty("os.name");
        if (osname == null || osname.length() == 0) {
            throw new UnsupportedOSException("Couldn't determine os name. Checked System.getProperty(\"os.name\")");
        }
        osname = osname.toLowerCase();
        String sep = ExecUtil.DELIMITER;
        String ping = pingLocation;
        if (osname.indexOf("linux") >= 0 || osname.indexOf("mac") >= 0) {
            // manpage: https://linux.die.net/man/8/ping
            // manpage: https://www.unix.com/man-page/osx/8/ping/
            return ping + sep + "-c" + sep + "3" + sep + "-W" + sep + "" + TIMEOUT_SEC + sep + hostnameOrIp;
        } else if (osname.indexOf("win") > 0) {
            // manpage: https://www.lifewire.com/ping-command-2618099
            return ping + sep + "-n" + sep + "3" + sep + "-w" + sep + TIMEOUT_MILLIS + sep + hostnameOrIp;
        } else if (osname.matches(".*free.*bsd.*")) {
            // manpage: https://www.freebsd.org/cgi/man.cgi?query=ping&sektion=8&apropos=0&manpath=FreeBSD+4.3-RELEASE
            return ping + sep + "-c" + sep + "3" + sep + "-t" + sep + TIMEOUT_SEC + sep + hostnameOrIp;
        } else if (osname.matches(".*open.*bsd.*")) {
            // manpage: https://man.openbsd.org/ping.8
            return ping + sep + "-c" + sep + "3" + sep + "-w" + sep + TIMEOUT_SEC + sep + hostnameOrIp;
        } else if (osname.matches(".*hp.*ux.*")) {
            // manpage: http://nixdoc.net/man-pages/hp-ux/man1/ping.1m.html
            return ping + sep + hostnameOrIp + sep + "-n" + sep + "3" + sep + "-m" + sep + TIMEOUT_SEC;
        } else if (osname.indexOf("sunos") > 0 || osname.indexOf("sun os") > 0 || osname.indexOf("solaris") > 0) {
            // manpage: https://docs.oracle.com/cd/E26505_01/html/816-5166/ping-1m.html
            return ping + sep + hostnameOrIp + sep + TIMEOUT_SEC;
        } else {
            throw new UnsupportedOSException(
                    "OS " + osname + " not supported. Don't know ping command syntax and location");
        }
    }

    public static boolean isHostOnline(String hostnameOrIp, @NonNull Logger logger)
            throws UnsupportedOSException, ExecuteException, InterruptedException, IOException {
        String cmdLine = getPingCommandByOS(hostnameOrIp);
        Map<Integer, String> ret = ExecUtil.executeCommandLineAndWaitResponse(cmdLine, TIMEOUT_MILLIS + 1000, logger);
        if (ret == null || ret.size() == 0) {
            String cmdWithSpaces = cmdLine.replaceAll("@@", " ");
            logger.warn("Ping command failed: {}", cmdWithSpaces);
            return false;
        } else if (ret.keySet().iterator().next() != 0) {
            logger.debug("Host {} seems to be down.", hostnameOrIp);
            return false;
        } else {
            logger.debug("Host {} seems to be online.", hostnameOrIp);
            return true;
        }
    }

    public static boolean isHostOnlineJava(String hostnameOrIp, Logger thingLogger)
            throws UnknownHostException, IOException {
        InetAddress inet = InetAddress.getByName(hostnameOrIp);
        return inet.isReachable(TIMEOUT_MILLIS);
    }
}
