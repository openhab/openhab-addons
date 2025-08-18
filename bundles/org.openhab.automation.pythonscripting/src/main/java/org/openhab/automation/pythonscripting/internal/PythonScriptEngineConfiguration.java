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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.Configuration;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Processes Python Configuration Parameters.
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class PythonScriptEngineConfiguration {

    private final Logger logger = LoggerFactory.getLogger(PythonScriptEngineConfiguration.class);

    private static final String SYSTEM_PROPERTY_POLYGLOT_ENGINE_USERRESOURCECACHE = "polyglot.engine.userResourceCache";

    private static final String SYSTEM_PROPERTY_JAVA_IO_TMPDIR = "java.io.tmpdir";

    private static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();
    private static final String RESOURCE_SEPARATOR = "/";

    public static final Path PYTHON_DEFAULT_PATH = Paths.get(OpenHAB.getConfigFolder(), "automation", "python");
    public static final Path PYTHON_LIB_PATH = PYTHON_DEFAULT_PATH.resolve("lib");
    public static final Path PYTHON_TYPINGS_PATH = PYTHON_DEFAULT_PATH.resolve("typings");
    public static final Path PYTHON_OPENHAB_LIB_PATH = PYTHON_LIB_PATH.resolve("openhab");

    public static final Path PYTHON_WRAPPER_FILE_PATH = PYTHON_OPENHAB_LIB_PATH.resolve("__wrapper__.py");
    private static final Path PYTHON_INIT_FILE_PATH = PYTHON_OPENHAB_LIB_PATH.resolve("__init__.py");

    private static final Pattern VERSION_PATTERN = Pattern.compile("__version__\\s*=\\s*\"([^\"]*)\"",
            Pattern.CASE_INSENSITIVE);

    public static final int INJECTION_DISABLED = 0;
    public static final int INJECTION_ENABLED_FOR_ALL_SCRIPTS = 1;
    public static final int INJECTION_ENABLED_FOR_NON_FILE_BASED_SCRIPTS = 2;

    // The variable names must match the configuration keys in config.xml
    public static class PythonScriptingConfiguration {
        public boolean scopeEnabled = true;
        public boolean helperEnabled = true;
        public int injectionEnabled = INJECTION_ENABLED_FOR_NON_FILE_BASED_SCRIPTS;
        public boolean dependencyTrackingEnabled = true;
        public boolean cachingEnabled = true;
        public boolean jythonEmulation = false;
        public boolean nativeModules = false;
        public String pipModules = "";
    }

    private PythonScriptingConfiguration configuration = new PythonScriptingConfiguration();
    private Path bytecodeDirectory;
    private Path tempDirectory;
    private Path venvDirectory;
    private @Nullable Path venvExecutable = null;

    private Version bundleVersion = Version
            .parse(FrameworkUtil.getBundle(PythonScriptEngineConfiguration.class).getVersion().toString());

    private Version graalVersion = Version.parse("0.0.0");
    private Version providedHelperLibVersion = Version.parse("0.0.0");
    private @Nullable Version installedHelperLibVersion = null;

    public static Version parseHelperLibVersion(@Nullable String version) throws IllegalArgumentException {
        // substring(1) => remove leading 'v'
        return Version.parse(version != null && version.startsWith("v") ? version.substring(1) : version);
    }

    @Activate
    public PythonScriptEngineConfiguration(Map<String, Object> config, PythonScriptEngineFactory factory) {
        Path userdataDir = Paths.get(OpenHAB.getUserDataFolder());

        String tmpDir = System.getProperty(SYSTEM_PROPERTY_JAVA_IO_TMPDIR);
        if (tmpDir != null) {
            tempDirectory = Paths.get(tmpDir);
        } else {
            tempDirectory = userdataDir.resolve("tmp");
        }

        try {
            InputStream is = PythonScriptEngineConfiguration.class
                    .getResourceAsStream(RESOURCE_SEPARATOR + "build.properties");
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                String version = p.getProperty("helperlib.version");
                providedHelperLibVersion = parseHelperLibVersion(version);
                version = p.getProperty("graalpy.version");
                graalVersion = Version.parse(version);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load build.properties");
        }

        String packageName = PythonScriptEngineConfiguration.class.getPackageName();
        packageName = packageName.substring(0, packageName.lastIndexOf("."));
        Path bindingDirectory = userdataDir.resolve("cache").resolve(packageName);

        Properties props = System.getProperties();
        props.setProperty(SYSTEM_PROPERTY_POLYGLOT_ENGINE_USERRESOURCECACHE, bindingDirectory.toString());
        bytecodeDirectory = initDirectory(bindingDirectory.resolve("resources"));
        venvDirectory = initDirectory(bindingDirectory.resolve("venv"));

        Path venvPythonBin = venvDirectory.resolve("bin").resolve("graalpy");
        if (Files.exists(venvPythonBin)) {
            venvExecutable = venvPythonBin;
        }

        initHelperLib();

        this.update(config, factory);
    }

    /**
     * Update configuration
     *
     * @param config Configuration parameters to apply to ScriptEngine
     * @param intitial
     */
    public void modified(Map<String, Object> config, ScriptEngineFactory factory) {
        boolean oldScopeEnabled = configuration.scopeEnabled;
        boolean oldInjectionEnabled = !isInjection(PythonScriptEngineConfiguration.INJECTION_DISABLED);
        boolean oldDependencyTrackingEnabled = isDependencyTrackingEnabled();

        this.update(config, factory);

        if (oldScopeEnabled != isScopeEnabled()) {
            logger.info("{} scope for Python Scripting. Please resave your scripts to apply this change.",
                    isScopeEnabled() ? "Enabled" : "Disabled");
        }
        if (oldInjectionEnabled != !isInjection(PythonScriptEngineConfiguration.INJECTION_DISABLED)) {
            logger.info("{} injection for Python Scripting. Please resave your UI-based scripts to apply this change.",
                    !isInjection(PythonScriptEngineConfiguration.INJECTION_DISABLED) ? "Enabled" : "Disabled");
        }
        if (oldDependencyTrackingEnabled != isDependencyTrackingEnabled()) {
            logger.info("{} dependency tracking for Python Scripting. Please resave your scripts to apply this change.",
                    isDependencyTrackingEnabled() ? "Enabled" : "Disabled");
        }
    }

    private void update(Map<String, Object> config, ScriptEngineFactory factory) {
        logger.trace("Python Script Engine Configuration: {}", config);

        String oldPipModules = configuration.pipModules;
        configuration = new Configuration(config).as(PythonScriptingConfiguration.class);

        if (!oldPipModules.equals(configuration.pipModules)) {
            initPipModules(factory);
        }
    }

    public boolean isScopeEnabled() {
        return configuration.scopeEnabled;
    }

    public boolean isHelperEnabled() {
        return configuration.helperEnabled;
    }

    public boolean isInjection(int type) {
        return configuration.injectionEnabled == type;
    }

    public boolean isDependencyTrackingEnabled() {
        return configuration.dependencyTrackingEnabled;
    }

    public boolean isCachingEnabled() {
        return configuration.cachingEnabled;
    }

    public boolean isJythonEmulation() {
        return configuration.jythonEmulation;
    }

    public boolean isNativeModulesEnabled() {
        return configuration.nativeModules;
    }

    public String getPIPModules() {
        return configuration.pipModules;
    }

    public Path getBytecodeDirectory() {
        return bytecodeDirectory;
    }

    public Path getTempDirectory() {
        return tempDirectory;
    }

    public Path getVEnvDirectory() {
        return venvDirectory;
    }

    public @Nullable Path getVEnvExecutable() {
        return venvExecutable;
    }

    public boolean isVEnvEnabled() {
        return venvExecutable != null;
    }

    public Version getBundleVersion() {
        return bundleVersion;
    }

    public Version getGraalVersion() {
        return graalVersion;
    }

    public Version getProvidedHelperLibVersion() {
        return providedHelperLibVersion;
    }

    public @Nullable Version getInstalledHelperLibVersion() {
        return installedHelperLibVersion;
    }

    /**
     * Returns the current configuration as a map.
     * This is used to display the configuration in the console.
     */
    public Map<String, String> getConfigurations() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> objectMap = objectMapper.convertValue(configuration,
                new TypeReference<Map<String, Object>>() {
                });
        return objectMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            if (entry.getValue() instanceof List<?> listValue) {
                return listValue.stream().map(Object::toString).collect(Collectors.joining("\n"));
            }
            return entry.getValue().toString();
        }));
    }

    private void initPipModules(ScriptEngineFactory factory) {
        String pipModulesConfig = configuration.pipModules.strip();
        if (pipModulesConfig.isEmpty()) {
            return;
        }

        if (!isVEnvEnabled()) {
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

        ScriptEngine engine = factory.createScriptEngine(PythonScriptEngineFactory.SCRIPT_TYPE);
        if (engine != null) {
            engine.getContext().setAttribute("pipModules", pipModules, ScriptContext.ENGINE_SCOPE);
            try {
                logger.info("Checking for pip module{} '{}'", pipModules.size() > 1 ? "s" : "",
                        configuration.pipModules);
                engine.eval(pipCode);
            } catch (ScriptException e) {
                logger.warn("Error installing pip module{}", pipModules.size() > 1 ? "s" : "");
                logger.trace("TRACE:", unwrap(e));
            }
        } else {
            logger.warn("Can't install pip modules. No script engine available.");
        }
    }

    private void initHelperLib() {
        if (!isHelperEnabled()) {
            return;
        }

        logger.info("Checking for helper libs version '{}'", providedHelperLibVersion);

        String resourceLibPath = PYTHON_OPENHAB_LIB_PATH.toString().substring(PYTHON_DEFAULT_PATH.toString().length())
                + PATH_SEPARATOR;
        if (!RESOURCE_SEPARATOR.equals(PATH_SEPARATOR)) {
            resourceLibPath = resourceLibPath.replace(PATH_SEPARATOR, RESOURCE_SEPARATOR);
        }

        if (Files.exists(PYTHON_INIT_FILE_PATH)) {
            try {
                String content = Files.readString(PYTHON_INIT_FILE_PATH, StandardCharsets.UTF_8);
                Matcher currentMatcher = VERSION_PATTERN.matcher(content);
                if (currentMatcher.find()) {
                    installedHelperLibVersion = Version.parse(currentMatcher.group(1));
                    @SuppressWarnings("null")
                    int compareResult = installedHelperLibVersion.compareTo(providedHelperLibVersion);
                    if (compareResult >= 0) {
                        if (compareResult > 0) {
                            logger.info("Newer helper libs version '{}' already installed.", installedHelperLibVersion);
                        }
                        return;
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
                Enumeration<URL> resourceFiles = FrameworkUtil.getBundle(PythonScriptEngineConfiguration.class)
                        .findEntries(resourceLibPath, "*.py", true);

                while (resourceFiles.hasMoreElements()) {
                    URL resourceFile = resourceFiles.nextElement();
                    String resourcePath = resourceFile.getPath();

                    ClassLoader clsLoader = PythonScriptEngineConfiguration.class.getClassLoader();
                    if (clsLoader != null) {
                        try (InputStream is = clsLoader.getResourceAsStream(resourcePath)) {
                            Path target = PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH
                                    .resolve(resourcePath.substring(resourcePath.lastIndexOf(RESOURCE_SEPARATOR) + 1));

                            Files.copy(is, target);
                            initFile(target);
                        }
                    } else {
                        throw new IllegalArgumentException("Class loader is null");
                    }
                }

                postProcessHelperLibUpdateOnSuccess(providedHelperLibVersion, bakLibPath);
            } catch (Exception e) {
                postProcessHelperLibUpdateOnFailure(bakLibPath);
                throw e;
            }
        } catch (Exception e) {
            logger.error("Exception during helper lib initialisation", e);
        }
    }

    public void initHelperLib(String remoteUrl, Version remoteVersion) throws URISyntaxException, IOException {
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

            postProcessHelperLibUpdateOnSuccess(remoteVersion, bakLibPath);
        } catch (IOException | URISyntaxException e) {
            postProcessHelperLibUpdateOnFailure(bakLibPath);
            throw e;
        }
    }

    private @Nullable Path preProcessHelperLibUpdate() throws IOException {
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

    private void postProcessHelperLibUpdateOnSuccess(Version version, @Nullable Path bakLibPath) throws IOException {
        String content = Files.readString(PYTHON_INIT_FILE_PATH, StandardCharsets.UTF_8).trim();
        Matcher currentMatcher = VERSION_PATTERN.matcher(content);
        content = currentMatcher.replaceAll("__version__ = \"" + version.toString() + "\"");
        Files.writeString(PYTHON_INIT_FILE_PATH, content, StandardCharsets.UTF_8);
        installedHelperLibVersion = version;

        if (bakLibPath != null) {
            // cleanup old files
            try (var dirStream = Files.walk(bakLibPath)) {
                dirStream.map(Path::toFile).sorted(Comparator.reverseOrder()).forEach(File::delete);
            }
        }
    }

    private void postProcessHelperLibUpdateOnFailure(@Nullable Path bakLibPath) throws IOException {
        // cleanup new files
        try (var dirStream = Files.walk(PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH)) {
            dirStream.map(Path::toFile).sorted(Comparator.reverseOrder()).forEach(File::delete);
        }

        if (bakLibPath != null) {
            // restore old files
            Files.move(bakLibPath, PythonScriptEngineConfiguration.PYTHON_OPENHAB_LIB_PATH);
        }
    }

    private void initFile(Path path) {
        File file = path.toFile();
        file.setReadable(true, false);
        file.setWritable(true, true);
    }

    private Path initDirectory(Path path) {
        File directory = path.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
            directory.setExecutable(true, false);
            directory.setReadable(true, false);
            directory.setWritable(true, true);
        }
        return path;
    }

    /**
     * Unwraps the cause of an exception, if it has one.
     *
     * Since a user cares about the _Ruby_ stack trace of the throwable, not
     * the details of where openHAB called it.
     */
    private static Throwable unwrap(Throwable e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            return cause;
        }
        return e;
    }
}
