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
 * The {@link WindowsUpdater} is the shell script for updating OpenHab on this OS
 *
 * @author AndrewFG
 */
@NonNullByDefault
public class WindowsUpdater extends BaseUpdater {

    private static final String COMMAND_ID = "cmd.exe";
    private static final String COMMAND_SWITCH = "/c";
    private static final String FILE_EXTENSION = ".cmd";
    private static final String RUN_DIRECTORY = "runtime\\bin\\";

    private String getScriptCommands() {
        // @formatter:off
        String commands =
                runDirectory + "backup.bat\n" +
                runDirectory + "update.bat " + getLatestVersion() +"\n"+
                "EXIT /B %ERRORLEVEL%\n";
        // @formatter:on
        return commands;
    }

    public WindowsUpdater(TargetVersion targetVersion) {
        super(targetVersion);
        runDirectory = getOpenHabRootFolder() + RUN_DIRECTORY;
        fileExtension = FILE_EXTENSION;
    }

    @Override
    protected boolean prerequisitesOk() {
        if (!super.prerequisitesOk()) {
            return false;
        }
        if (!writeFile(getScriptFileName(), getScriptCommands())) {
            logger.warn("Cannot run OpenHAB update script: error writing {}", getScriptFileName());
            return false;
        }
        return true;
    }

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
