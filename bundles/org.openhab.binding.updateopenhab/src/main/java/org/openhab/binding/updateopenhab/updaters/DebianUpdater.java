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

/**
 * The {@link DebianUpdater} is the shell script for updating OpenHab on this OS
 *
 * @author AndrewFG - Initial contribution
 */
@NonNullByDefault
public class DebianUpdater extends BaseUpdater {

    private static final String EXECUTE_FOLDER = "/usr/share/openhab/";
    private static final String EXECUTE_FILENAME = FILE_ID + ".sh";
    private static final String EXECUTE_COMMAND = "echo " + PlaceHolder.PASSWORD.key + " | sudo -S -k nohup sh "
            + EXECUTE_FILENAME + " >/dev/null 2>&1";
    private static final String RUNTIME_FOLDER = EXECUTE_FOLDER + "/runtime";

    @Override
    protected void initializeExtendedPlaceholders() {
        placeHolders.put(PlaceHolder.EXECUTE_FOLDER, EXECUTE_FOLDER);
        placeHolders.put(PlaceHolder.EXECUTE_FILENAME, EXECUTE_FILENAME);
        placeHolders.put(PlaceHolder.EXECUTE_COMMAND, EXECUTE_COMMAND);
        placeHolders.put(PlaceHolder.RUNTIME_FOLDER, RUNTIME_FOLDER);
    }
}
