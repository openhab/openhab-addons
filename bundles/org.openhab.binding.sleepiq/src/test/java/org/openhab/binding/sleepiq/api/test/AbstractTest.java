/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.api.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AbstractTest} tests deserialization of a sleepiq API objects.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractTest {
    private static final String RESOURCES_PATH = "src/test/resources/";

    protected File getTestDataFile(String name) {
        return getTestDataPath(name).toFile();
    }

    @SuppressWarnings("null")
    protected Path getTestDataPath(String name) {
        String packageName = this.getClass().getPackage().getName();

        List<String> paths = new ArrayList<>();
        paths.addAll(Arrays.asList(packageName.split("\\.")));
        paths.add(name);

        return Paths.get(RESOURCES_PATH, paths.toArray(new String[paths.size()]));
    }

    protected String readJson(String jsonFileName) throws IOException {
        return String.join("\n", Files.readAllLines(getTestDataPath(jsonFileName)));
    }
}
