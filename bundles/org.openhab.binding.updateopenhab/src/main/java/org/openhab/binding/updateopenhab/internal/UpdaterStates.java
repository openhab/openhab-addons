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
package org.openhab.binding.updateopenhab.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.updateopenhab.updaters.BaseUpdater;
import org.openhab.core.OpenHAB;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link UpdaterStates} implements static methods that convert {@link BaseUpdater} values to OH Channel States
 *
 * @author AndrewFG - Initial contribution
 */
@NonNullByDefault
public class UpdaterStates {

    private static Integer safeConvertInteger(String[] strings, int index) {
        try {
            return index < strings.length ? Integer.valueOf(strings[index]) : 0;
        } catch (NumberFormatException e) {
        }
        return 0;
    }

    public static State getRunningVersion() {
        return StringType.valueOf(BaseUpdater.getRunningVersion());
    }

    public static State getLatestVersion(BaseUpdater updater) {
        String newVersion = updater.getLatestTargetVersion();
        return BaseUpdater.VERSION_NOT_DEFINED.equals(newVersion) ? UnDefType.UNDEF : StringType.valueOf(newVersion);
    }

    public static State getUpdateAvailable(BaseUpdater updater) {
        String oldVersion = OpenHAB.getVersion();
        String newVersion = updater.getLatestTargetVersion();
        String[] oldParts = oldVersion.replace("-", ".").split("\\.");
        String[] newParts = newVersion.replace("-", ".").split("\\.");

        int compareOverall = 0;
        for (int i = 0; i < 3; i++) {
            int compareResult = safeConvertInteger(oldParts, i).compareTo(safeConvertInteger(newParts, i));
            if (compareResult != 0) {
                compareOverall = compareResult;
                break;
            }
        }
        if (compareOverall == 0) {
            switch (updater.getTargetVersion()) {
                case SNAPSHOT:
                    return UnDefType.UNDEF;
                case MILESTONE:
                    if (newParts.length > 3) {
                        compareOverall = oldParts.length > 3 ? oldParts[3].compareTo(newParts[3]) : 1;
                    }
                default:
            }
        }
        return OnOffType.from(compareOverall < 0);
    }
}
