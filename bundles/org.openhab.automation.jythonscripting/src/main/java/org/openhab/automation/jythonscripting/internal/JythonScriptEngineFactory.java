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
package org.openhab.automation.jythonscripting.internal;

import java.io.File;
import java.nio.file.Paths;
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
 * @author Holger Hees - Further development
 */
@Component(service = ScriptEngineFactory.class)
@NonNullByDefault
public class JythonScriptEngineFactory extends AbstractScriptEngineFactory {

    private static final String PYTHON_HOME = "python.home";
    private static final String PYTHON_HOME_PATH = JythonScriptEngineFactory.class.getProtectionDomain().getCodeSource()
            .getLocation().toString().replace("file:", "");

    private static final String PYTHON_PATH = "python.path";
    private static final String PYTHON_DEFAULT_PATH = Paths
            .get(OpenHAB.getConfigFolder(), "automation", "jython", "lib").toString();

    private static final String PYTHON_CACHEDIR = "python.cachedir";
    private static final String PYTHON_CACHEDIR_PATH = Paths
            .get(OpenHAB.getUserDataFolder(), "cache", JythonScriptEngineFactory.class.getPackageName(), "cachedir")
            .toString();

    private static final org.python.jsr223.PyScriptEngineFactory factory = new org.python.jsr223.PyScriptEngineFactory();

    public static final String SCRIPT_TYPE = "application/x-python2";
    private final List<String> scriptTypes = Arrays.asList("jythonpy", SCRIPT_TYPE);

    @Activate
    public JythonScriptEngineFactory() {
        logger.debug("Loading JythonScriptEngineFactory");

        System.setProperty(PYTHON_HOME, PYTHON_HOME_PATH);

        Set<String> pythonPathList = new TreeSet<>(Arrays.asList(PYTHON_DEFAULT_PATH));
        String existingPythonPath = System.getProperty(PYTHON_PATH);
        if (existingPythonPath != null && !existingPythonPath.isEmpty()) {
            pythonPathList.addAll(Arrays.asList(existingPythonPath.split(File.pathSeparator)));
        }
        System.setProperty(PYTHON_PATH, String.join(File.pathSeparator, pythonPathList));

        System.setProperty(PYTHON_CACHEDIR, PYTHON_CACHEDIR_PATH);

        logPythonPaths();
    }

    @Deactivate
    public void cleanup() {
        logger.debug("Unloading JythonScriptEngineFactory");

        System.clearProperty(PYTHON_HOME);

        String existingPythonPath = System.getProperty(PYTHON_PATH);
        if (existingPythonPath != null && !existingPythonPath.isEmpty()) {
            Set<String> newPythonPathList = new TreeSet<>(Arrays.asList(existingPythonPath.split(File.pathSeparator)));
            newPythonPathList.remove(PYTHON_DEFAULT_PATH);
            System.setProperty(PYTHON_PATH, String.join(File.pathSeparator, newPythonPathList));
        }

        System.clearProperty(PYTHON_CACHEDIR);

        logPythonPaths();
    }

    @Override
    public List<String> getScriptTypes() {
        return scriptTypes;
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        if (!scriptTypes.contains(scriptType)) {
            return null;
        }
        return factory.getScriptEngine();
    }

    private void logPythonPaths() {
        logger.trace("{}: {}, {}: {}, {}: {}", //
                PYTHON_HOME, System.getProperty(PYTHON_HOME), //
                PYTHON_PATH, System.getProperty(PYTHON_PATH), //
                PYTHON_CACHEDIR, System.getProperty(PYTHON_CACHEDIR));
    }
}
