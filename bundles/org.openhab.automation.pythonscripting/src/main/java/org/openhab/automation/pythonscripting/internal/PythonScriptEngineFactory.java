/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.automation.pythonscripting.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.module.ModuleDescriptor.Version;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.pythonscripting.internal.fs.watch.PythonDependencyTracker;
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigurableService;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of {@link ScriptEngineFactory} for Python.
 *
 * @author Holger Hees - Initial contribution
 * @author Jeff James - Initial contribution
 */
@Component(service = ScriptEngineFactory.class, configurationPid = "org.openhab.automation.pythonscripting", property = Constants.SERVICE_PID
        + "=org.openhab.automation.pythonscripting")
@ConfigurableService(category = "automation", label = "Python Scripting", description_uri = "automation:pythonscripting")
@NonNullByDefault
public class PythonScriptEngineFactory implements ScriptEngineFactory {
    private final Logger logger = LoggerFactory.getLogger(PythonScriptEngineFactory.class);

    private static final String RESOURCE_SEPARATOR = "/";

    public static final Path PYTHON_DEFAULT_PATH = Paths.get(OpenHAB.getConfigFolder(), "automation", "python");
    public static final Path PYTHON_LIB_PATH = PYTHON_DEFAULT_PATH.resolve("lib");

    private static final Path PYTHON_OPENHAB_LIB_PATH = PYTHON_LIB_PATH.resolve("openhab");

    public static final Path PYTHON_WRAPPER_FILE_PATH = PYTHON_OPENHAB_LIB_PATH.resolve("__wrapper__.py");
    private static final Path PYTHON_INIT_FILE_PATH = PYTHON_OPENHAB_LIB_PATH.resolve("__init__.py");

    public static final String SCRIPT_TYPE = "application/x-python3";
    private final List<String> scriptTypes = Arrays.asList("py", SCRIPT_TYPE);

    private final PythonDependencyTracker pythonDependencyTracker;
    private final PythonScriptEngineConfiguration pythonScriptEngineConfiguration;

    @Activate
    public PythonScriptEngineFactory(final @Reference PythonDependencyTracker pythonDependencyTracker,
            Map<String, Object> config) {
        logger.debug("Loading PythonScriptEngineFactory");

        this.pythonDependencyTracker = pythonDependencyTracker;
        this.pythonScriptEngineConfiguration = new PythonScriptEngineConfiguration();

        modified(config);

        if (this.pythonScriptEngineConfiguration.isHelperEnabled()) {
            initHelperLib();
        }
    }

    @Deactivate
    public void cleanup() {
        logger.debug("Unloading PythonScriptEngineFactory");
    }

    @Modified
    protected void modified(Map<String, ?> config) {
        this.pythonScriptEngineConfiguration.update(config);
    }

    @Override
    public List<String> getScriptTypes() {
        return scriptTypes;
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        for (Entry<String, Object> entry : scopeValues.entrySet()) {
            scriptEngine.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        if (!scriptTypes.contains(scriptType)) {
            return null;
        }
        return new PythonScriptEngine(pythonDependencyTracker, pythonScriptEngineConfiguration);
    }

    @Override
    public @Nullable ScriptDependencyTracker getDependencyTracker() {
        return pythonDependencyTracker;
    }

    private void initHelperLib() {
        try {
            String pathSeparator = FileSystems.getDefault().getSeparator();
            String resourceLibPath = PYTHON_OPENHAB_LIB_PATH.toString()
                    .substring(PYTHON_DEFAULT_PATH.toString().length()) + pathSeparator;
            if (!RESOURCE_SEPARATOR.equals(pathSeparator)) {
                resourceLibPath = resourceLibPath.replace(pathSeparator, RESOURCE_SEPARATOR);
            }

            if (Files.exists(PythonScriptEngineFactory.PYTHON_OPENHAB_LIB_PATH)) {
                try (Stream<Path> files = Files.list(PYTHON_OPENHAB_LIB_PATH)) {
                    if (files.count() > 0) {
                        Pattern pattern = Pattern.compile("__version__\\s*=\\s*\"([0-9]+\\.[0-9]+\\.[0-9]+)\"",
                                Pattern.CASE_INSENSITIVE);

                        Version includedVersion = null;
                        try (InputStream is = PythonScriptEngineFactory.class.getClassLoader().getResourceAsStream(
                                resourceLibPath + PYTHON_INIT_FILE_PATH.getFileName().toString())) {
                            try (InputStreamReader isr = new InputStreamReader(is);
                                    BufferedReader reader = new BufferedReader(isr)) {
                                String fileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                                Matcher includedMatcher = pattern.matcher(fileContent);
                                if (includedMatcher.find()) {
                                    includedVersion = Version.parse(includedMatcher.group(1));
                                }
                            }
                        }

                        Version currentVersion = null;
                        String fileContent = Files.readString(PYTHON_INIT_FILE_PATH, StandardCharsets.UTF_8);
                        Matcher currentMatcher = pattern.matcher(fileContent);
                        if (currentMatcher.find()) {
                            currentVersion = Version.parse(currentMatcher.group(1));
                        }

                        if (currentVersion == null) {
                            logger.warn("Unable to detect installed helper lib version. Skip installing helper libs.");
                            return;
                        } else if (includedVersion == null) {
                            logger.error("Unable to detect provided helper lib version. Skip installing helper libs.");
                            return;
                        } else if (currentVersion.compareTo(includedVersion) >= 0) {
                            logger.info("Newest helper lib version is deployed.");
                            return;
                        }
                    }
                }
            }

            logger.info("Deploy helper libs into {}.", PythonScriptEngineFactory.PYTHON_OPENHAB_LIB_PATH);

            if (Files.exists(PythonScriptEngineFactory.PYTHON_OPENHAB_LIB_PATH)) {
                try (Stream<Path> paths = Files.walk(PythonScriptEngineFactory.PYTHON_OPENHAB_LIB_PATH)) {
                    paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                }
            }

            initDirectory(PythonScriptEngineFactory.PYTHON_DEFAULT_PATH);
            initDirectory(PythonScriptEngineFactory.PYTHON_LIB_PATH);
            initDirectory(PythonScriptEngineFactory.PYTHON_OPENHAB_LIB_PATH);

            Enumeration<URL> resourceFiles = FrameworkUtil.getBundle(PythonScriptEngineFactory.class)
                    .findEntries(resourceLibPath, "*.py", true);

            while (resourceFiles.hasMoreElements()) {
                URL resourceFile = resourceFiles.nextElement();
                String resourcePath = resourceFile.getPath();

                try (InputStream is = PythonScriptEngineFactory.class.getClassLoader()
                        .getResourceAsStream(resourcePath)) {
                    Path target = PythonScriptEngineFactory.PYTHON_OPENHAB_LIB_PATH
                            .resolve(resourcePath.substring(resourcePath.lastIndexOf(RESOURCE_SEPARATOR) + 1));

                    Files.copy(is, target);
                    File file = target.toFile();
                    file.setReadable(true, false);
                    file.setWritable(true, true);
                }
            }
        } catch (Exception e) {
            logger.error("Exception during helper lib initialisation", e);
        }
    }

    private void initDirectory(Path path) {
        File directory = path.toFile();
        if (!directory.exists()) {
            directory.mkdir();
            directory.setExecutable(true, false);
            directory.setReadable(true, false);
            directory.setWritable(true, true);
        }
    }
}
