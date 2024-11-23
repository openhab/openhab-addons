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
package org.openhab.binding.lametrictime.internal.api.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Test utility class.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class TestUtil {
    private static final String RESOURCES_PATH = "src/test/resources/";

    public static Path getTestDataPath(Class<?> clazz, String name) {
        String packageName = clazz.getPackage().getName();

        List<String> paths = new ArrayList<>();
        paths.addAll(Arrays.asList(packageName.split("\\.")));
        paths.add(name);

        return Paths.get(RESOURCES_PATH, paths.toArray(new String[paths.size()]));
    }

    // @formatter:off
    private TestUtil() {}
    // @formatter:on
}
