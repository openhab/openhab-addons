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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.graalvm.polyglot.Context;
import org.openhab.core.automation.module.script.ScriptExtensionAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class providing script extensions via common modules
 *
 * @author Holger Hees - Initial contribution
 */

@NonNullByDefault
public class ScriptExtensionModuleProvider {

    public static final String IMPORT_PROXY_NAME = "__import_proxy__";

    public static final String OPENHAB_MODULE_PREFIX = "org.openhab";
    public static final String SCOPE_MODULE_PREFIX = "scope";

    private final Logger logger = LoggerFactory.getLogger(ScriptExtensionModuleProvider.class);

    private Map<String, Object> globals = new HashMap<>();

    public ModuleLocator locatorFor(Context ctx, String engineIdentifier,
            ScriptExtensionAccessor scriptExtensionAccessor) {
        return (name, fromlist) -> {
            Map<String, Object> symbols = new HashMap<>();
            if (name.startsWith(OPENHAB_MODULE_PREFIX)) {
                List<String> classList = new ArrayList<>();
                if (!fromlist.isEmpty() && fromlist.contains("*")) {
                    logger.error("Wildcard support of java packages not supported");
                } else {
                    if (fromlist.isEmpty()) {
                        classList.add(name);
                    } else {
                        for (String from : fromlist) {
                            classList.add(name + "." + from);
                        }
                    }
                }
                symbols.put("class_list", classList);
            } else if (name.startsWith(SCOPE_MODULE_PREFIX)) {
                String[] segments = name.split("\\.");
                if (name.equals(SCOPE_MODULE_PREFIX)) {
                    Map<String, Object> possibleSymbols = new HashMap<>(this.globals);

                    if (fromlist.isEmpty() || fromlist.contains("*")) {
                        symbols = possibleSymbols;
                    } else {
                        symbols = possibleSymbols.entrySet().stream().filter(x -> fromlist.contains(x.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                        List<String> filteredFromlist = fromlist.stream().filter(x -> !possibleSymbols.containsKey(x))
                                .toList();

                        if (!filteredFromlist.isEmpty()) {
                            for (String from : filteredFromlist) {
                                Map<String, Object> fromSymbols = scriptExtensionAccessor.findPreset(from,
                                        engineIdentifier);
                                if (!fromSymbols.isEmpty()) {
                                    symbols.put(from, fromSymbols);
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
}
