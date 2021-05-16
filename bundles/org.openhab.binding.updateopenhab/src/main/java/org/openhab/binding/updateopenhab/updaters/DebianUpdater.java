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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DebianUpdater} is the shell script for updating OpenHab on this OS
 *
 * @author AndrewFG - Initial contribution
 */
@NonNullByDefault
public class DebianUpdater extends BaseUpdater {

    private static final String EXECUTE_FOLDER = "/usr/share/openhab";
    private static final String EXECUTE_FILENAME = FILE_ID + ".sh";
    private static final String RUNTIME_FOLDER = EXECUTE_FOLDER + "/runtime";

    /*
     * TESTING: plain shell (works for scripts that require no credentials)
     * private static final String EXECUTE_COMMAND = "sh -c ./" + EXECUTE_FILENAME;
     */

    /*
     * TESTING: inject password and user name to sudo command
     * private static final String EXECUTE_COMMAND = "printf \"" + PlaceHolder.PASSWORD.key + "\\n\" | sudo -k -S -u " +
     * PlaceHolder.USER_NAME.key + " ./" + EXECUTE_FILENAME;
     */

    /*
     * TESTING: inject password only to sudo command
     */
    private static final String EXECUTE_COMMAND = "printf \"" + PlaceHolder.PASSWORD.key + "\\n\" | sudo -k -S ./"
            + EXECUTE_FILENAME;

    @Override
    protected void initializeExtendedPlaceholders() {
        placeHolders.put(PlaceHolder.EXECUTE_FOLDER, EXECUTE_FOLDER);
        placeHolders.put(PlaceHolder.EXECUTE_FILENAME, EXECUTE_FILENAME);
        placeHolders.put(PlaceHolder.EXECUTE_COMMAND, EXECUTE_COMMAND);
        placeHolders.put(PlaceHolder.RUNTIME_FOLDER, RUNTIME_FOLDER);
        placeHolders.put(PlaceHolder.STD_OUT_FILENAME, FILE_ID + ".txt");
    }

    /**
     * Unix systems require 'execute' permissions on the script file to allow it to run.
     */
    @Override
    protected boolean createScriptFile() {
        if (super.createScriptFile()) {
            String folder = placeHolders.get(PlaceHolder.EXECUTE_FOLDER);
            String filename = placeHolders.get(PlaceHolder.EXECUTE_FILENAME);
            try {
                Files.setPosixFilePermissions(Paths.get(folder + File.separator + filename),
                        PosixFilePermissions.fromString("rwxr-xr-x"));
                return true;
            } catch (IOException e) {
                logger.debug("Error setting execute permissions.");
            }
        }
        return false;
    }
}
