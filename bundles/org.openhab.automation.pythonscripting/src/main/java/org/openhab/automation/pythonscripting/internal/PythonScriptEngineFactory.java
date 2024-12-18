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

import java.util.List;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.pythonscripting.internal.graal.GraalPythonScriptEngineFactory;
import org.openhab.core.automation.module.script.AbstractScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * This is an implementation of {@link ScriptEngineFactory} for Python.
 *
 * @author Holger Hees - Further development
 */
@Component(service = ScriptEngineFactory.class)
@NonNullByDefault
public class PythonScriptEngineFactory extends AbstractScriptEngineFactory {

    private static final GraalPythonScriptEngineFactory factory = new GraalPythonScriptEngineFactory();

    private final List<String> scriptTypes = (List<String>) Stream.of(factory.getExtensions(), factory.getMimeTypes())
            .flatMap(List::stream) //
            .toList();

    @Activate
    public PythonScriptEngineFactory() {
        logger.debug("Loading PythonScriptEngineFactory");
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
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        if (!scriptTypes.contains(scriptType)) {
            return null;
        }
        return factory.getScriptEngine();
    }
}
