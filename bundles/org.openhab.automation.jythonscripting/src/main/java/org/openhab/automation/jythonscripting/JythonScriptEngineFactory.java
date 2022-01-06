/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.automation.jythonscripting;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.module.script.AbstractScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * This is an implementation of {@link ScriptEngineFactory} for Jython.
 *
 * @author Scott Rushworth - Initial contribution
 * @author Wouter Born - Initial contribution
 */
@Component(service = ScriptEngineFactory.class)
@NonNullByDefault
public class JythonScriptEngineFactory extends AbstractScriptEngineFactory {

    private static final String PYTHON_CACHEDIR = "python.cachedir";
    private static final String PYTHON_HOME = "python.home";
    private static final String PYTHON_PATH = "python.path";

    private static final String DEFAULT_PYTHON_PATH = Paths
            .get(OpenHAB.getConfigFolder(), "automation", "lib", "python").toString();

    private static final String SCRIPT_TYPE = "py";
    private static final javax.script.ScriptEngineManager ENGINE_MANAGER = new javax.script.ScriptEngineManager();

    @Activate
    public JythonScriptEngineFactory() {
        logger.debug("Loading JythonScriptEngineFactory");

        String pythonHome = JythonScriptEngineFactory.class.getProtectionDomain().getCodeSource().getLocation()
                .toString().replace("file:", "");
        System.setProperty(PYTHON_HOME, pythonHome);

        String existingPythonPath = System.getProperty(PYTHON_PATH);
        if (existingPythonPath == null || existingPythonPath.isEmpty()) {
            System.setProperty(PYTHON_PATH, DEFAULT_PYTHON_PATH);
        } else if (!existingPythonPath.contains(DEFAULT_PYTHON_PATH)) {
            Set<String> newPythonPathList = new TreeSet<>(Arrays.asList(existingPythonPath.split(File.pathSeparator)));
            newPythonPathList.add(DEFAULT_PYTHON_PATH);
            System.setProperty(PYTHON_PATH, String.join(File.pathSeparator, newPythonPathList));
        }

        System.setProperty(PYTHON_CACHEDIR, Paths
                .get(OpenHAB.getUserDataFolder(), "cache", JythonScriptEngineFactory.class.getPackageName(), "cachedir")
                .toString());

        logPythonPaths();
    }

    private void logPythonPaths() {
        logger.trace("{}: {}, {}: {}, {}: {}", //
                PYTHON_HOME, System.getProperty(PYTHON_HOME), //
                PYTHON_PATH, System.getProperty(PYTHON_PATH), //
                PYTHON_CACHEDIR, System.getProperty(PYTHON_CACHEDIR));
    }

    @Override
    public List<String> getScriptTypes() {
        List<String> scriptTypes = new ArrayList<>();

        for (javax.script.ScriptEngineFactory factory : ENGINE_MANAGER.getEngineFactories()) {
            List<String> extensions = factory.getExtensions();

            if (extensions.contains(SCRIPT_TYPE)) {
                scriptTypes.addAll(extensions);
                scriptTypes.addAll(factory.getMimeTypes());
            }
        }
        return scriptTypes;
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        ScriptEngine scriptEngine = ENGINE_MANAGER.getEngineByExtension(scriptType);
        if (scriptEngine == null) {
            scriptEngine = ENGINE_MANAGER.getEngineByMimeType(scriptType);
        }
        if (scriptEngine == null) {
            scriptEngine = ENGINE_MANAGER.getEngineByName(scriptType);
        }
        return scriptEngine;
    }

    @Deactivate
    public void removePythonPath() {
        logger.debug("Unloading JythonScriptEngineFactory");

        String existingPythonPath = System.getProperty(PYTHON_PATH);
        if (existingPythonPath != null && existingPythonPath.contains(DEFAULT_PYTHON_PATH)) {
            Set<String> newPythonPathList = new TreeSet<>(Arrays.asList(existingPythonPath.split(File.pathSeparator)));
            newPythonPathList.remove(DEFAULT_PYTHON_PATH);
            System.setProperty(PYTHON_PATH, String.join(File.pathSeparator, newPythonPathList));
        }

        System.clearProperty(PYTHON_HOME);
        System.clearProperty(PYTHON_CACHEDIR);

        logPythonPaths();
    }
}
