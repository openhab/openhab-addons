/**
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.module.ModuleDescriptor.Version;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.config.core.ConfigurableService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of {@link ScriptEngineFactory} for Python.
 *
 * @author Holger Hees - initial contribution
 * @author Jeff James - initial contribution
 */
@Component(service = ScriptEngineFactory.class, configurationPid = "org.openhab.pythonscripting", property = Constants.SERVICE_PID
        + "=org.openhab.pythonscripting")
@ConfigurableService(category = "automation", label = "Python Scripting", description_uri = "automation:pythonscripting")
@NonNullByDefault
public class PythonScriptEngineFactory implements ScriptEngineFactory {
    private final Logger logger = LoggerFactory.getLogger(PythonScriptEngineFactory.class);
    private static final String CFG_JYTHON_EMULATION = "jythonEmulation";

    private boolean jythonEmulation = false;

    public static final String SCRIPT_TYPE = "py3";
    private final List<String> scriptTypes = Arrays.asList(PythonScriptEngineFactory.SCRIPT_TYPE);

    @Activate
    public PythonScriptEngineFactory(Map<String, Object> config) {
        logger.debug("Loading PythonScriptEngineFactory");

        initOpenhabLib();

        modified(config);
    }

    @Deactivate
    public void cleanup() {
        logger.debug("Unloading PythonScriptEngineFactory");
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
        // return new OpenhabGraalPythonScriptEngine(jythonEmulation);
        return new DebuggingGraalScriptEngine<>(new OpenhabGraalPythonScriptEngine(jythonEmulation));
    }

    @Modified
    protected void modified(Map<String, ?> config) {
        this.jythonEmulation = ConfigParser.valueAsOrElse(config.get(CFG_JYTHON_EMULATION), Boolean.class, false);
    }

    private void initOpenhabLib() {
        Path libPath = OpenhabGraalPythonScriptEngine.PYTHON_DEFAULT_PATH.resolve("lib");
        Path helperLibPath = libPath.resolve("openhab");
        Path versionFilePath = helperLibPath.resolve("__init__.py");

        List<String> resourceFiles = Arrays.asList("__init__.py", "actions.py", "helper.py", "jsr223.py", "services.py",
                "triggers.py");

        if (Files.exists(helperLibPath)) {
            if (Files.exists(versionFilePath)) {
                Pattern pattern = Pattern.compile("__version__\\s*=\\s*\"([0-9]+\\.[0-9]+\\.[0-9]+)\"",
                        Pattern.CASE_INSENSITIVE);

                Version includedVersion = null;
                try {
                    String _includedVersion = getResourceFileAsString("/lib/openhab/__init__.py");
                    Matcher includedMatcher = pattern.matcher(_includedVersion);
                    if (includedMatcher.find()) {
                        includedVersion = Version.parse(includedMatcher.group(1));
                    }

                } catch (IOException e) {
                }

                Version currentVersion = null;
                try {
                    String _currentVersion = Files.readString(versionFilePath, StandardCharsets.UTF_8);
                    Matcher currentMatcher = pattern.matcher(_currentVersion);
                    if (currentMatcher.find()) {
                        currentVersion = Version.parse(currentMatcher.group(1));
                    }
                } catch (IOException e) {
                }

                if (currentVersion == null) {
                    logger.warn("Custom lib detected. Skip installing helper libs.");
                    return;
                } else if (includedVersion == null) {
                    logger.error("Unable to detect helper lib version. Skip installing helper libs.");
                    return;
                } else if (currentVersion.compareTo(includedVersion) >= 0) {
                    logger.info("Newest helper lib version is deployed.");
                    return;
                }
            } else {
                logger.warn("Custom lib detected. Skip installing helper libs.");
                return;
            }
        }

        logger.info("Deploy helper libs into {}.", helperLibPath.toString());

        try {
            if (Files.exists(helperLibPath)) {
                Files.walkFileTree(helperLibPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, @Nullable BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, @Nullable IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            Files.createDirectory(helperLibPath);
            Files.setPosixFilePermissions(helperLibPath, PosixFilePermissions.fromString("rwxr-xr-x"));

            for (String resourceFile : resourceFiles) {
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("/lib/openhab/" + resourceFile);
                Path target = helperLibPath.resolve(resourceFile);
                Files.copy(is, target);
                Files.setPosixFilePermissions(target, PosixFilePermissions.fromString("rw-r--r--"));
            }
        } catch (IOException e) {
            logger.warn("Unable to deploy helper lib.", e);
            return;
        }
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private @Nullable String getResourceFileAsString(String fileName) throws IOException {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                return null;
            }
            try (InputStreamReader isr = new InputStreamReader(is); BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }
}
