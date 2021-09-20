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
package org.openhab.automation.jsscriptingnashorn.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.module.script.AbstractScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.osgi.service.component.annotations.Component;

/**
 * This is an implementation of a {@link ScriptEngineFactory} for Nashorn.
 *
 * @author Wouter Born - Initial contribution
 */
@Component(service = ScriptEngineFactory.class)
@NonNullByDefault
public class NashornScriptEngineFactory extends AbstractScriptEngineFactory {

    private static final String NASHORN_ARGS = "nashorn.args";

    private final org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory factory = new org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory();

    private final List<String> scriptTypes = (List<String>) Stream.of(factory.getExtensions(), factory.getMimeTypes())
            .flatMap(List::stream) //
            .collect(Collectors.toUnmodifiableList());

    public NashornScriptEngineFactory() {
        removeUnsupportedNashornArgs();
    }

    private void removeUnsupportedNashornArgs() {
        String property = System.getProperty(NASHORN_ARGS, "");
        property = property.replaceAll("--no-deprecation-warning", "").trim();
        System.setProperty(NASHORN_ARGS, property);
    }

    @Override
    public List<String> getScriptTypes() {
        return scriptTypes;
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        Set<String> expressions = new HashSet<>();

        for (Entry<String, Object> entry : scopeValues.entrySet()) {
            scriptEngine.put(entry.getKey(), entry.getValue());
            if (entry.getValue() instanceof Class) {
                expressions.add(String.format("%s = %<s.static;", entry.getKey()));
            }
        }
        String scriptToEval = String.join("\n", expressions);
        try {
            scriptEngine.eval(scriptToEval);
        } catch (ScriptException ex) {
            logger.error("ScriptException while importing scope: {}", ex.getMessage());
        }
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        return scriptTypes.contains(scriptType)
                ? factory.getScriptEngine(NashornScriptEngineFactory.class.getClassLoader())
                : null;
    }
}
