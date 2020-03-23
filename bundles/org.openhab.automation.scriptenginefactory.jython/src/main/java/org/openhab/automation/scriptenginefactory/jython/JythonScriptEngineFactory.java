/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.automation.scriptenginefactory.jython;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.openhab.core.automation.module.script.AbstractScriptEngineFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * This is an implementation of {@link ScriptEngineFactory} for Jython.
 *
 * @author Scott Rushworth - Initial contribution
 */
@Component(service = org.openhab.core.automation.module.script.ScriptEngineFactory.class)
@NonNullByDefault
public class JythonScriptEngineFactory extends AbstractScriptEngineFactory {

    private static final String SCRIPT_TYPE = "py";
    private static javax.script.ScriptEngineManager ENGINE_MANAGER = new javax.script.ScriptEngineManager();
    private static final String DEFAULT_PYTHON_PATH = new StringBuilder(ConfigConstants.getConfigFolder())
            .append(File.separator).append("automation").append(File.separator).append("lib").append(File.separator)
            .append("python").toString();

    @Activate
    public JythonScriptEngineFactory() {
        logger.debug("Loading JythonScriptEngineFactory");
        String home = JythonScriptEngineFactory.class.getProtectionDomain().getCodeSource().getLocation().toString()
                .replace("file:", "");
        System.setProperty("python.home", home);

        String existingPythonPath = System.getProperty("python.path");
        if (existingPythonPath == null || existingPythonPath.isEmpty()) {
            System.setProperty("python.path", DEFAULT_PYTHON_PATH);
        } else if (!existingPythonPath.contains(DEFAULT_PYTHON_PATH)) {
            TreeSet<String> newPythonPathList = new TreeSet<>(
                    new ArrayList<String>(Arrays.asList(existingPythonPath.split(File.pathSeparator))));
            newPythonPathList.add(DEFAULT_PYTHON_PATH);
            String newPythonPath = String.join(File.pathSeparator, newPythonPathList);
            System.setProperty("python.path", newPythonPath);
        }

        StringBuilder newPythonCachedir = new StringBuilder(System.getenv("OPENHAB_USERDATA")).append(File.separator)
                .append("tmp");
        System.setProperty("python.cachedir", newPythonCachedir.toString());

        logger.trace("python.home [{}], python.path [{}], python.cachdir [{}]", System.getProperty("python.home"),
                System.getProperty("python.path"), System.getProperty("python.cachedir"));
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
        return Collections.unmodifiableList(scriptTypes);
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
    public void deactivate() {
        logger.debug("Unloading JythonScriptEngineFactory");
        String existingPythonPath = System.getProperty("python.path");
        if (existingPythonPath.contains(DEFAULT_PYTHON_PATH)) {
            TreeSet<String> newPythonPathList = new TreeSet<>(
                    new ArrayList<String>(Arrays.asList(existingPythonPath.split(File.pathSeparator))));
            newPythonPathList.remove(DEFAULT_PYTHON_PATH);
            String newPythonPath = String.join(File.pathSeparator, newPythonPathList);
            System.setProperty("python.path", newPythonPath);
        }
        logger.trace("python.home [{}], python.path [{}], python.cachdir [{}]", System.getProperty("python.home"),
                System.getProperty("python.path"), System.getProperty("python.cachedir"));
    }
}
