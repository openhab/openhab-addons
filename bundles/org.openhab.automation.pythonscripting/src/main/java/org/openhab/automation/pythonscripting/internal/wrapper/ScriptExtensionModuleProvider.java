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
package org.openhab.automation.pythonscripting.internal.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.graalvm.polyglot.Context;
import org.openhab.core.automation.module.script.ScriptExtensionAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class providing script extensions via CommonJS modules (with module name `@runtime`).
 *
 * @author Jonathan Gilbert - Initial contribution
 * @author Florian Hotze - Pass in lock object for multi-thread synchronization; Switch to {@link Lock} for multi-thread
 *         synchronization
 */

@NonNullByDefault
public class ScriptExtensionModuleProvider {

    public static final String IMPORT_PROXY_NAME = "__import_proxy__";

    public static final String OENHAB_MODULE_PREFIX = "org.openhab";
    public static final String SCOPE_MODULE_PREFIX = "scope";

    private final Logger logger = LoggerFactory.getLogger(ScriptExtensionModuleProvider.class);

    private Map<String, Object> globals = new HashMap<String, Object>();

    public ScriptExtensionModuleProvider() {
    }

    public ModuleLocator locatorFor(Context ctx, String engineIdentifier,
            ScriptExtensionAccessor scriptExtensionAccessor) {
        return (name, fromlist) -> {
            Map<String, Object> symbols = new HashMap<String, Object>();
            if (name.startsWith(OENHAB_MODULE_PREFIX)) {
                List<String> class_list = new ArrayList<String>();
                if (fromlist.size() > 0 && fromlist.contains("*")) {
                    logger.error("Wildcard support of java packages not supported");
                } else {
                    if (fromlist.size() == 0) {
                        class_list.add(name);
                    } else {
                        for (String _from : fromlist) {
                            class_list.add(name + "." + _from);
                        }
                    }
                }
                symbols.put("class_list", class_list);
            } else if (name.startsWith(SCOPE_MODULE_PREFIX)) {
                String[] segments = name.split("\\.");
                if (name.equals(SCOPE_MODULE_PREFIX)) {
                    Map<String, Object> _symbols = new HashMap<String, Object>(this.globals); // scriptExtensionAccessor.findDefaultPresets(engineIdentifier);

                    if (fromlist.size() == 0 || fromlist.contains("*")) {
                        symbols = _symbols;
                    } else {
                        symbols = _symbols.entrySet().stream().filter(x -> fromlist.contains(x.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                        List<String> _fromlist = fromlist.stream().filter(x -> !_symbols.containsKey(x)).toList();

                        if (_fromlist.size() > 0) {
                            for (String _from : _fromlist) {
                                Map<String, Object> _from_symbols = scriptExtensionAccessor.findPreset(_from,
                                        engineIdentifier);
                                if (_from_symbols.size() > 0) {
                                    symbols.put(_from, _from_symbols);
                                }
                            }
                        }
                    }
                } else {
                    return scriptExtensionAccessor.findPreset(segments[1], engineIdentifier);
                }
            }
            return symbols;
        };
    }

    public void put(String key, Object value) {
        this.globals.put(key, value);
    }

    // private Value toValue(Context ctx, Map<String, Object> map) {
    // try {
    // ctx.eval(Source.newBuilder( // convert to Map to Python Object
    // GraalPythonScriptEngine.LANGUAGE_ID, //
    // "import polyglot\n" //
    // + "@polyglot.export_value\n" //
    // + "def convert(map):\n" //
    // + " obj = {}\n" //
    // + " for prop in map.keySet():\n" //
    // + " obj[prop] = map[prop]\n" //
    // + " return obj\n" //
    // + "",
    // "<generated>").build());
    // return ctx.getPolyglotBindings().getMember("convert").execute(map);
    // } catch (IOException e) {
    // throw new IllegalArgumentException("Failed to generate exports", e);
    // }
    // }
}
