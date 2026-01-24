/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor.Version;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Context;
import org.openhab.core.OpenHAB;
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

    public static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();
    public static final String RESOURCE_SEPARATOR = "/";

    public static final Path PYTHON_DEFAULT_PATH = Paths.get(OpenHAB.getConfigFolder(), "automation", "python");
    public static final Path PYTHON_LIB_PATH = PYTHON_DEFAULT_PATH.resolve("lib");
    public static final Path PYTHON_TYPINGS_PATH = PYTHON_DEFAULT_PATH.resolve("typings");
    public static final Path PYTHON_OPENHAB_LIB_PATH = PYTHON_LIB_PATH.resolve("openhab");

    public static final Path PYTHON_WRAPPER_FILE_PATH = PYTHON_OPENHAB_LIB_PATH.resolve("__wrapper__.py");
    public static final Path PYTHON_INIT_FILE_PATH = PYTHON_OPENHAB_LIB_PATH.resolve("__init__.py");

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
    public PythonScriptEngineConfiguration(Map<String, Object> config) {
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
                version = FrameworkUtil.getBundle(Context.class).getVersion().toString();
                graalVersion = Version.parse(version);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load build.properties");
        }

        Properties props = System.getProperties();
        props.setProperty(SYSTEM_PROPERTY_POLYGLOT_ENGINE_USERRESOURCECACHE,
                userdataDir.resolve("cache").resolve("org.graalvm.polyglot").toString());

        String packageName = PythonScriptEngineConfiguration.class.getPackageName();
        packageName = packageName.substring(0, packageName.lastIndexOf("."));
        Path bindingDirectory = userdataDir.resolve("cache").resolve(packageName);
        bytecodeDirectory = PythonScriptEngineHelper.initDirectory(bindingDirectory.resolve("resources"));
        venvDirectory = PythonScriptEngineHelper.initDirectory(bindingDirectory.resolve("venv"));

        Path venvPythonBin = venvDirectory.resolve("bin").resolve("graalpy");
        if (Files.exists(venvPythonBin)) {
            venvExecutable = venvPythonBin;
        }

        installedHelperLibVersion = PythonScriptEngineHelper.initHelperLib(this, providedHelperLibVersion);

        configuration = new Configuration(config).as(PythonScriptingConfiguration.class);
    }

    public void init(PythonScriptEngineFactory factory) {
        PythonScriptEngineHelper.initPipModules(this, factory);
    }

    /**
     * Update configuration
     *
     * @param config Configuration parameters to apply to ScriptEngine
     * @param initial
     */
    public void modified(Map<String, Object> config, PythonScriptEngineFactory factory) {
        boolean oldScopeEnabled = configuration.scopeEnabled;
        boolean oldInjectionEnabled = !isInjection(PythonScriptEngineConfiguration.INJECTION_DISABLED);
        boolean oldDependencyTrackingEnabled = isDependencyTrackingEnabled();

        String oldPipModules = configuration.pipModules;
        configuration = new Configuration(config).as(PythonScriptingConfiguration.class);
        if (!oldPipModules.equals(configuration.pipModules)) {
            PythonScriptEngineHelper.initPipModules(this, factory);
        }

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

    public void setHelperLibVersion(Version version) {
        installedHelperLibVersion = version;
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
}
