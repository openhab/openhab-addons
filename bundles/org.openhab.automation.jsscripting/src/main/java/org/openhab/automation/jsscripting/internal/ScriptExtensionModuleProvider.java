/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.automation.jsscripting.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.openhab.automation.jsscripting.internal.threading.ThreadsafeWrappingScriptedAutomationManagerDelegate;
import org.openhab.core.automation.module.script.ScriptExtensionAccessor;
import org.openhab.core.automation.module.script.rulesupport.shared.ScriptedAutomationManager;

/**
 * Class providing script extensions via CommonJS modules (with module name `@runtime`).
 *
 * @author Jonathan Gilbert - Initial contribution
 * @author Florian Hotze - Pass in lock object for multi-thread synchronization; Switch to {@link Lock} for multi-thread
 *         synchronization
 */

@NonNullByDefault
public class ScriptExtensionModuleProvider {

    private static final String RUNTIME_MODULE_PREFIX = "@runtime";
    private static final String DEFAULT_MODULE_NAME = "Defaults";
    private final Lock lock;

    private final ScriptExtensionAccessor scriptExtensionAccessor;

    public ScriptExtensionModuleProvider(ScriptExtensionAccessor scriptExtensionAccessor, Lock lock) {
        this.scriptExtensionAccessor = scriptExtensionAccessor;
        this.lock = lock;
    }

    public ModuleLocator locatorFor(Context ctx, String engineIdentifier) {
        return name -> {
            String[] segments = name.split("/");
            if (segments[0].equals(RUNTIME_MODULE_PREFIX)) {
                if (segments.length == 1) {
                    return runtimeModule(DEFAULT_MODULE_NAME, engineIdentifier, ctx);
                } else {
                    return runtimeModule(segments[1], engineIdentifier, ctx);
                }
            }

            return Optional.empty();
        };
    }

    private Optional<Value> runtimeModule(String name, String scriptIdentifier, Context ctx) {
        Map<String, Object> symbols;

        if (DEFAULT_MODULE_NAME.equals(name)) {
            symbols = scriptExtensionAccessor.findDefaultPresets(scriptIdentifier);
        } else {
            symbols = scriptExtensionAccessor.findPreset(name, scriptIdentifier);
        }

        return Optional.of(symbols).map(this::processValues).map(v -> toValue(ctx, v));
    }

    private Value toValue(Context ctx, Map<String, Object> map) {
        try {
            return ctx.eval(Source.newBuilder( // convert to Map to JS Object
                    "js",
                    "(function (mapOfValues) {\n" + "let rv = {};\n" + "for (var key in mapOfValues) {\n"
                            + "    rv[key] = mapOfValues.get(key);\n" + "}\n" + "return rv;\n" + "})",
                    "<generated>").build()).execute(map);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to generate exports", e);
        }
    }

    /**
     * Some specific objects need wrapping when exposed to a GraalJS environment. This method does this.
     *
     * @param values the map of names to values of things to process
     * @return a map of the processed keys and values
     */
    private Map<String, Object> processValues(Map<String, Object> values) {
        Map<String, Object> rv = new HashMap<>(values);

        for (Map.Entry<String, Object> entry : rv.entrySet()) {
            if (entry.getValue() instanceof ScriptedAutomationManager) {
                entry.setValue(new ThreadsafeWrappingScriptedAutomationManagerDelegate(
                        (ScriptedAutomationManager) entry.getValue(), lock));
            }
        }

        return rv;
    }
}
