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
 * The {@link MacUpdater} is the shell script for updating OpenHab on this OS
 *
 * @author AndrewFG
 */
@NonNullByDefault
public class MacUpdater extends BaseUpdater {

    private static final String COMMAND_ID = "TBD";
    private static final String FILE_EXTENSION = ".TBD";
    private static final String RUN_DIRECTORY = "runtime/bin/";

    private String getScriptCommands() {
        // @formatter:off
        String commands =
                "sudo " + runDirectory + "backup\n" +
                "sudo " + runDirectory + "update "  + getLatestVersion() +"\n";
        // @formatter:on
        return commands;
    }

    public MacUpdater(TargetVersion targetVersion) {
        super(targetVersion);
        runDirectory = RUN_DIRECTORY;
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
            builder.directory(new File(runDirectory));
            builder.command().add(getScriptFileName());
            executeProcess(builder);
        }
    }
}
