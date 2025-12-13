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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor.Version;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes Python Configuration Parameters.
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class PythonScriptEngineHelper {

    private static final Logger logger = LoggerFactory.getLogger(PythonScriptEngineHelper.class);

    private static final Pattern VERSION_PATTERN = Pattern.compile("__version__\\s*=\\s*\"([^\"]*)\"",
            Pattern.CASE_INSENSITIVE);

    public static void initPipModules(PythonScriptEngineConfiguration configuration,
            PythonScriptEngineFactory factory) {
        String pipModulesConfig = configuration.getPIPModules().strip();
        if (pipModulesConfig.isEmpty()) {
            return;
        }

        if (!configuration.isVEnvEnabled()) {
            logger.error("Can't install pip modules. VEnv not enabled.");
            return;
        }

        List<String> pipModules = Arrays.stream(pipModulesConfig.split(",")).map(String::trim)
                .filter(module -> !module.isEmpty()).collect(Collectors.toList());

        if (pipModules.isEmpty()) {
            return;
        }

        final String pipCode = """
                import subprocess
                import sys

                command_list = [sys.executable, "-m", "pip", "install"] + pipModules
                proc = subprocess.run(command_list, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, check=False)
                if proc.returncode != 0:
                    print(proc.stdout)
                    exit(1)
                """;

        ScriptEngine engine = factory.createScriptEngine(PythonScriptEngineFactory.SCRIPT_TYPE,
                "python-setup-" + UUID.randomUUID().toString());
        if (engine != null) {
            engine.getContext().setAttribute("pipModules", pipModules, ScriptContext.ENGINE_SCOPE);
            try {
                logger.info("Checking for pip module{} '{}'", pipModules.size() > 1 ? "s" : "",
                        configuration.getPIPModules());
                engine.eval(pipCode);
            } catch (ScriptException e) {
                logger.warn("Error installing pip module{}", pipModules.size() > 1 ? "s" : "");
                logger.trace("TRACE:", unwrap(e));
            }
        } else {
            logger.warn("Can't install pip modules. No script engine available.");
        }
    }

    public static @Nullable Version initHelperLib(PythonScriptEngineConfiguration configuration,
            Version providedHelperLibVersion) {
        Version installedHelperLibVersion = null;

        if (!configuration.isHelperEnabled()) {
            return installedHelperLibVersion;
        }

        logger.info("Checking for helper libs version '{}'", configuration.getProvidedHelperLibVersion());

        String resourceLibPath = PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH.toString()
                .substring(PythonScriptEngineConfiguration.PYTHON_DEFAULT_PATH.toString().length())
                + PythonScriptEngineConfiguration.PATH_SEPARATOR;
        if (!PythonScriptEngineConfiguration.RESOURCE_SEPARATOR
                .equals(PythonScriptEngineConfiguration.PATH_SEPARATOR)) {
            resourceLibPath = resourceLibPath.replace(PythonScriptEngineConfiguration.PATH_SEPARATOR,
                    PythonScriptEngineConfiguration.RESOURCE_SEPARATOR);
        }

        if (Files.exists(PythonScriptEngineConfiguration.PYTHON_INIT_FILE_PATH)) {
            try {
                String content = Files.readString(PythonScriptEngineConfiguration.PYTHON_INIT_FILE_PATH,
                        StandardCharsets.UTF_8);
                Matcher currentMatcher = VERSION_PATTERN.matcher(content);
                if (currentMatcher.find()) {
                    installedHelperLibVersion = Version.parse(currentMatcher.group(1));
                    @SuppressWarnings("null")
                    int compareResult = installedHelperLibVersion.compareTo(providedHelperLibVersion);
                    if (compareResult >= 0) {
                        if (compareResult > 0) {
                            logger.info("Newer helper libs version '{}' already installed.", installedHelperLibVersion);
                        }
                        return installedHelperLibVersion;
                    }
                } else {
                    logger.warn("Unable to parse current version. Proceed as if it was not installed.");
                }
            } catch (IOException | IllegalArgumentException e) {
                logger.warn("Unable to detect current version. Proceed as if it was not installed.");
            }
        }

        if (installedHelperLibVersion != null) {
            logger.info("Update helper libs version '{}' to version {}.", installedHelperLibVersion,
                    providedHelperLibVersion);
        } else {
            logger.info("Install helper libs version {}.", providedHelperLibVersion);
        }

        try {
            Path bakLibPath = preProcessHelperLibUpdate();

            try {
                Enumeration<URL> resourceFiles = FrameworkUtil.getBundle(PythonScriptEngineHelper.class)
                        .findEntries(resourceLibPath, "*.py", true);

                while (resourceFiles.hasMoreElements()) {
                    URL resourceFile = resourceFiles.nextElement();
                    String resourcePath = resourceFile.getPath();

                    ClassLoader clsLoader = PythonScriptEngineHelper.class.getClassLoader();
                    if (clsLoader != null) {
                        try (InputStream is = clsLoader.getResourceAsStream(resourcePath)) {
                            Path target = PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH
                                    .resolve(resourcePath.substring(
                                            resourcePath.lastIndexOf(PythonScriptEngineConfiguration.RESOURCE_SEPARATOR)
                                                    + 1));

                            Files.copy(is, target);
                            initFile(target);
                        }
                    } else {
                        throw new IllegalArgumentException("Class loader is null");
                    }
                }

                installedHelperLibVersion = postProcessHelperLibUpdateOnSuccess(providedHelperLibVersion, bakLibPath);
            } catch (Exception e) {
                postProcessHelperLibUpdateOnFailure(bakLibPath);
                throw e;
            }
        } catch (Exception e) {
            logger.error("Exception during helper lib initialisation", e);
        }

        return installedHelperLibVersion;
    }

    public static void installHelperLib(String remoteUrl, Version remoteVersion,
            PythonScriptEngineConfiguration configuration) throws URISyntaxException, IOException {
        Path bakLibPath = null;
        try {
            bakLibPath = preProcessHelperLibUpdate();
            URL zipfileUrl = new URI(remoteUrl).toURL();
            InputStream in = new BufferedInputStream(zipfileUrl.openStream(), 1024);
            ZipInputStream stream = new ZipInputStream(in);
            byte[] buffer = new byte[1024];
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                if (!entry.getName().contains("/src/") || entry.isDirectory()) {
                    continue;
                }

                int read;
                StringBuilder sb = new StringBuilder();
                while ((read = stream.read(buffer, 0, 1024)) >= 0) {
                    sb.append(new String(buffer, 0, read));
                }

                Path target = PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH
                        .resolve(new File(entry.getName()).getName());
                Files.write(target, sb.toString().getBytes());
                initFile(target);
            }

            Version version = postProcessHelperLibUpdateOnSuccess(remoteVersion, bakLibPath);
            configuration.setHelperLibVersion(version);
        } catch (IOException | URISyntaxException e) {
            postProcessHelperLibUpdateOnFailure(bakLibPath);
            throw e;
        }
    }

    private static @Nullable Path preProcessHelperLibUpdate() throws IOException {
        Path bakLibPath = null;
        // backup old lib folder before update
        if (Files.exists(PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH)) {
            bakLibPath = PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH.getParent()
                    .resolve(PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH.getFileName() + ".bak");
            Files.move(PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH, bakLibPath);
        }
        initDirectory(PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH);
        return bakLibPath;
    }

    private static Version postProcessHelperLibUpdateOnSuccess(Version version, @Nullable Path bakLibPath)
            throws IOException {
        String content = Files.readString(PythonScriptEngineConfiguration.PYTHON_INIT_FILE_PATH, StandardCharsets.UTF_8)
                .trim();
        Matcher currentMatcher = VERSION_PATTERN.matcher(content);
        content = currentMatcher.replaceAll("__version__ = \"" + version.toString() + "\"");
        Files.writeString(PythonScriptEngineConfiguration.PYTHON_INIT_FILE_PATH, content, StandardCharsets.UTF_8);

        if (bakLibPath != null) {
            // cleanup old files
            try (var dirStream = Files.walk(bakLibPath)) {
                dirStream.map(Path::toFile).sorted(Comparator.reverseOrder()).forEach(File::delete);
            }
        }

        return version;
    }

    private static void postProcessHelperLibUpdateOnFailure(@Nullable Path bakLibPath) throws IOException {
        // cleanup new files
        try (var dirStream = Files.walk(PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH)) {
            dirStream.map(Path::toFile).sorted(Comparator.reverseOrder()).forEach(File::delete);
        }

        if (bakLibPath != null) {
            // restore old files
            Files.move(bakLibPath, PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH);
        }
    }

    /**
     * Unwraps the cause of an exception, if it has one.
     *
     * Since a user cares about the _Python_ stack trace of the throwable, not
     * the details of where openHAB called it.
     */
    private static Throwable unwrap(Throwable e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            return cause;
        }
        return e;
    }

    private static void initFile(Path path) {
        File file = path.toFile();
        file.setReadable(true, false);
        file.setWritable(true, true);
    }

    public static Path initDirectory(Path path) {
        File directory = path.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
            directory.setExecutable(true, false);
            directory.setReadable(true, false);
            directory.setWritable(true, true);
        }
        return path;
    }
}
