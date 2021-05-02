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
package org.openhab.binding.updateopenhab.test;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link RunScript} is a JUnit test for running the scripts
 *
 * @author AndrewFG - initial contribution
 */
@NonNullByDefault
class RunScript {

    @Test
    void testVersions() {
        // State x;
        // x = BaseUpdater.getUpdateAvailableState0("1.1.1", "1.1.1");
        // x = BaseUpdater.getUpdateAvailableState0("2.0.0", "3.0.0");
        // x = BaseUpdater.getUpdateAvailableState0("3.0.0", "3.0.0");
        // x = BaseUpdater.getUpdateAvailableState0("3.0.0", "3.1.0");
        // x = BaseUpdater.getUpdateAvailableState0("3.1.0", "3.1.1");
        // x = BaseUpdater.getUpdateAvailableState0("3.1.0.M1", "3.1.0.M1");
        // x = BaseUpdater.getUpdateAvailableState0("3.1.0.M1", "3.1.0.M2");
        // x = BaseUpdater.getUpdateAvailableState0("3.2.0.M1", "3.1.0.M2");
        // x = BaseUpdater.getUpdateAvailableState0("3.0.0-SNAPSHOT", "3.0.0");
        // x = BaseUpdater.getUpdateAvailableState0("3.0.0-SNAPSHOT", "3.0.0-SNAPSHOT");
        // x = BaseUpdater.getUpdateAvailableState0("3.0.0-M24", "3.0.0-AARD");
    }

    @Test
    void runScripts() {
        // new WindowsUpdater(TargetVersion.STABLE).run();
        // new WindowsUpdater(TargetVersion.MILESTONE).run();
        // new WindowsUpdater(TargetVersion.SNAPSHOT).run();
        // new DebianUpdater(TargetVersion.STABLE).run();
        // new DebianUpdater(TargetVersion.MILESTONE).run();
        // new DebianUpdater(TargetVersion.SNAPSHOT).run();
    }
}
