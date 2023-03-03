/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.broadlink;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openhab.core.OpenHAB;

/**
 * Abstract superclass for all Broadlink unit tests;
 * ensures that the mapping file will be found
 * in a testing context
 * 
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractBroadlinkTest {
    protected static final Path TEST_CONF_DIRECTORY = Paths.get("src", "test", "resources", "conf");

    @BeforeAll
    public static void beforeClass() {
        System.setProperty(OpenHAB.CONFIG_DIR_PROG_ARGUMENT, TEST_CONF_DIRECTORY.toFile().getAbsolutePath());
    }

    @AfterAll
    public static void afterClass() {
        System.clearProperty(OpenHAB.CONFIG_DIR_PROG_ARGUMENT);
    }
}
