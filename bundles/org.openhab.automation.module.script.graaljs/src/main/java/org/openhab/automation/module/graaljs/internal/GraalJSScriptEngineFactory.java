/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.automation.module.graaljs.internal;

import com.oracle.truffle.js.scriptengine.GraalJSEngineFactory;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Context;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.*;
import java.util.Map.Entry;

/**
 * An implementation of {@link ScriptEngineFactory} with customizations for GraalJS ScriptEngines.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
@Component(service = ScriptEngineFactory.class)
public final class GraalJSScriptEngineFactory implements ScriptEngineFactory {

    private final Logger logger = LoggerFactory.getLogger(GraalJSScriptEngineFactory.class);

    /* only used for providing script types, not instantiating the engine */
    private GraalJSEngineFactory engineFactory;

    @Activate
    public GraalJSScriptEngineFactory(){
        engineFactory = new GraalJSEngineFactory();
    }

    @Override
    public List<String> getScriptTypes(){
        List<String> scriptTypes = new ArrayList<>();

        scriptTypes.addAll(engineFactory.getMimeTypes());
        scriptTypes.addAll(engineFactory.getExtensions());

        return Collections.unmodifiableList(scriptTypes);
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
        //create context with full access + nashorn compatibility
        return GraalJSScriptEngine.create(null,
                Context.newBuilder("js")
                        .allowExperimentalOptions(true)
                        .allowAllAccess(true)
                        .option("js.syntax-extensions", "true"));
    }
}
