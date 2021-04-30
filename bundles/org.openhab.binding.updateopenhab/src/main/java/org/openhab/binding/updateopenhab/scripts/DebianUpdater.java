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
 * @author AndrewFG
 */
@NonNullByDefault
public class DebianUpdater extends BaseUpdater {

    private static final String COMMAND_ID = "sh";
    private static final String COMMAND_SWITCH = "-c";
    private static final String FILE_EXTENSION = ".sh";
    private static final String APT_FILENAME = "/etc/apt/sources.list.d/openhab.list";
    private static final String DOWNLOAD_SOURCE_FMT = "deb https://openhab.jfrog.io/artifactory/openhab-linuxpkg %s main";
    private static final String SCRIPT_COMMAND = "apt-get update\n";

    public DebianUpdater(TargetVersion targetVersion) {
        super(targetVersion);
        runDirectory = getUserHomeFolder();
        fileExtension = FILE_EXTENSION;
    }

    @Override
    protected boolean prerequisitesOk() {
        if (!super.prerequisitesOk()) {
            return false;
        }
        if (!writeFile(APT_FILENAME, String.format(DOWNLOAD_SOURCE_FMT, targetVersion.label))) {
            logger.warn("Cannot run OpenHAB update script: error writing {}", APT_FILENAME);
            return false;
        }
        if (!writeFile(getScriptFileName(), SCRIPT_COMMAND)) {
            logger.warn("Cannot run OpenHAB update script: error writing {}", getScriptFileName());
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
            builder.command().add(COMMAND_ID);
            builder.command().add(COMMAND_SWITCH);
            builder.directory(new File(runDirectory));
            builder.command().add(getScriptFileName());
            executeProcess(builder);
        }
    }
}
