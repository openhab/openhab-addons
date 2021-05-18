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
package org.openhab.binding.updateopenhab.updaters;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;

/**
 * The {@link WindowsUpdater} is the shell script for updating OpenHab on this OS
 *
 * @author AndrewFG - Initial contribution
 */
@NonNullByDefault
public class WindowsUpdater extends BaseUpdater {

    private static final String EXEC_FOLDER = OpenHAB.getUserDataFolder().replace("\\userdata", "");
    private static final String EXEC_FILENAME = FILE_ID + ".cmd";
    private static final String EXEC_COMMAND = "cmd.exe /C start \"" + PlaceHolder.TITLE.key + "\" /W " + EXEC_FILENAME;
    private static final String RUNTIME_FOLDER = OpenHAB.getUserDataFolder().replace("userdata", "runtime");

    @Override
    protected void initializeExtendedPlaceholders() {
        placeHolders.put(PlaceHolder.EXEC_FOLDER, EXEC_FOLDER);
        placeHolders.put(PlaceHolder.EXEC_FILENAME, EXEC_FILENAME);
        placeHolders.put(PlaceHolder.EXEC_COMMAND, EXEC_COMMAND);
        placeHolders.put(PlaceHolder.RUNTIME_FOLDER, RUNTIME_FOLDER);
    }

    @Override
    protected void loggerInfoUpdateStarted() {
        super.loggerInfoUpdateStarted();
        logger.info("Note: this may cause a 'Karaf shutdown socket' warning below. But don't worry!");
    }
}
