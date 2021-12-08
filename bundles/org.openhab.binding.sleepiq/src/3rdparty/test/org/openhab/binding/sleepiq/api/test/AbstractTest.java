/*
 * Copyright 2017 Gregory Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
