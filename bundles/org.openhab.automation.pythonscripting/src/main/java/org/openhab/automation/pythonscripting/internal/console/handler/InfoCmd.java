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
package org.openhab.automation.pythonscripting.internal.console.handler;

import java.lang.module.ModuleDescriptor.Version;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.graalvm.polyglot.Language;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngine;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngineConfiguration;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngineFactory;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionRegistry;
import org.openhab.core.io.console.Console;

/**
 * Info command implementation
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class InfoCmd {
    private final PythonScriptEngineConfiguration config;
    private final Console console;

    public InfoCmd(PythonScriptEngineConfiguration config, Console console) {
        this.config = config;
        this.console = console;
    }

    public void show(ConfigDescriptionRegistry registry) {
        console.println("Python Scripting Environment:");
        console.println("======================================");
        console.println("  Runtime:");
        console.println("    Bundle version: " + config.getBundleVersion());
        console.println("    GraalVM version: " + config.getGraalVersion());
        Language language = PythonScriptEngine.getLanguage();
        console.println("    Python version: " + (language != null ? language.getVersion() : "unavailable"));
        Version version = config.getInstalledHelperLibVersion();
        console.println("    Helper lib version: " + (version != null ? version.toString() : "disabled"));
        console.println("    VEnv state: " + (config.isVEnvEnabled() ? "enabled" : "disabled"));
        console.println("    Type hints: "
                + (Files.isDirectory(PythonScriptEngineConfiguration.PYTHON_TYPINGS_PATH) ? "available"
                        : "not yet created"));
        console.println("");
        console.println("  Directories:");
        console.println("    Scripts: " + PythonScriptEngineConfiguration.PYTHON_DEFAULT_PATH);
        console.println("    Libraries: " + PythonScriptEngineConfiguration.PYTHON_LIB_PATH);
        console.println("    Typing: " + PythonScriptEngineConfiguration.PYTHON_TYPINGS_PATH);
        Path tempDirectory = config.getTempDirectory();
        console.println("    Temp: " + tempDirectory.toString());
        Path venvDirectory = config.getVEnvDirectory();
        console.println("    VEnv: " + venvDirectory.toString());

        console.println("");
        console.println("Python Scripting Add-on Configuration:");
        console.println("======================================");
        ConfigDescription configDescription = registry
                .getConfigDescription(URI.create(PythonScriptEngineFactory.CONFIG_DESCRIPTION_URI));

        if (configDescription == null) {
            console.println("No configuration found for Python Scripting add-on. This is probably a bug.");
            return;
        }

        List<ConfigDescriptionParameter> parameters = configDescription.getParameters();
        Map<String, String> configMap = config.getConfigurations();
        configDescription.getParameters().forEach(parameter -> {
            if (parameter.getGroupName() == null) {
                console.println("  " + parameter.getName() + ": " + configMap.get(parameter.getName()));
            }
        });
        configDescription.getParameterGroups().forEach(group -> {
            String groupLabel = group.getLabel();
            if (groupLabel == null) {
                groupLabel = group.getName();
            }
            console.println("  " + groupLabel);
            parameters.forEach(parameter -> {
                if (!group.getName().equals(parameter.getGroupName())) {
                    return;
                }
                console.print("    " + parameter.getName() + ": ");
                String value = configMap.get(parameter.getName());
                if (value == null) {
                    console.println("not set");
                } else if (value.contains("\n")) {
                    console.println("    (multiline)");
                    console.println("      " + value.replace("\n", "\n    "));
                } else {
                    console.println(value);
                }
            });
            console.println("");
        });
    }
}
