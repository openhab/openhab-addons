/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.updateopenhab.scripts;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.updateopenhab.internal.TargetVersion;

/**
 * The {@link DebianUpdater} is the shell script for updating OpenHab on this OS
 *
 * @author AndrewFG - Initial contribution
 */
@NonNullByDefault
public class DebianUpdater extends BaseUpdater {

    private static final String EXT = ".sh";
    private static final String DIRECTORY = "/srv/openhab-sys/runtime/bin/";
    private static final String FILENAME = DIRECTORY + FILE_ID + EXT;

    private final String[] commands = new String[] { "echo", password, "|", "sudo", "-S", "sh", FILENAME };

    private final String aptFile = "/etc/apt/sources.list.d/openhab.list";
    private final String aptUrl = String.format("deb https://openhab.jfrog.io/artifactory/openhab-linuxpkg %s main",
            targetVersion.label);

    private final String contents =
    // @formatter:off
            "#!/bin/sh\n" +
            "echo " + aptUrl + " > " + aptFile + "\n" +
            "cd " + DIRECTORY + "\n" +
            "./stop\n" +
            "sleep 30\n" +
//            "apt-get update\n" +
            "./karaf\n";
    // @formatter:on

    public DebianUpdater(TargetVersion targetVersion, String password) {
        super(targetVersion, password);
    }

    @Override
    protected boolean prerequisitesOk() {
        if (!super.prerequisitesOk()) {
            return false;
        }
        if (!writeFile(FILENAME, contents)) {
            logger.warn("Cannot run OpenHAB update script: error writing {}", FILENAME);
            return false;
        }
        return true;
    }

    /**
     * Method to execute the OpenHAB update script
     */
    @Override
    public void run() {

        if (prerequisitesOk()) {
            ProcessBuilder builder = new ProcessBuilder();
            for (String arg : commands) {
                builder.command().add(arg);
            }
            builder.directory(new File(DIRECTORY));
            executeProcess(builder);
        }
    }
}
