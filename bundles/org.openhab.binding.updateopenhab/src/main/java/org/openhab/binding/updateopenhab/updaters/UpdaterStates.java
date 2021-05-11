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

    public static State getActualVersion() {
        return StringType.valueOf(BaseUpdater.getActualVersion());
    }

    public static State getRemoteVersion(BaseUpdater updater) {
        String newVersion = updater.getRemoteVersion();
        return BaseUpdater.VERSION_NOT_DEFINED.equals(newVersion) ? UnDefType.UNDEF : StringType.valueOf(newVersion);
    }

    public static State getRemoteVersionHigher(BaseUpdater updater) {
        TriState result = updater.getRemoteVersionHigher();
        return result == TriState.YES ? OnOffType.ON : result == TriState.NO ? OnOffType.OFF : UnDefType.UNDEF;
    }
}
