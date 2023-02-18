/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * abstract test class.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractTest {
    protected File getTestDataFile(String name) {
        return getTestDataPath(name).toFile();
    }

    protected Path getTestDataPath(String name) {
        return TestUtil.getTestDataPath(this.getClass(), name);
    }

    protected String readJson(String jsonFileName) throws IOException {
        return String.join("\n", Files.readAllLines(getTestDataPath(jsonFileName)));
    }
}
