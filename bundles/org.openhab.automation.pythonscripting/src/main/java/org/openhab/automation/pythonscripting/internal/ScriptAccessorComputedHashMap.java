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

import java.util.Map;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyHashMap;
import org.openhab.core.automation.module.script.ScriptExtensionAccessor;

/**
 *
 * @author Jeff James - initial contribution
 */
class ScriptAccessorComputedHashMap implements ProxyHashMap {
    private final ScriptExtensionAccessor scriptExtensionAccessor;
    private final Map<String, Object> allValues;

    public ScriptAccessorComputedHashMap(ScriptExtensionAccessor scriptExtensionAccessor, String scriptIdentifier) {
        this.scriptExtensionAccessor = scriptExtensionAccessor;
        this.allValues = scriptExtensionAccessor.findDefaultPresets(scriptIdentifier);
    }

    @Override
    public Object getHashEntriesIterator() {
        return null;
    }

    @Override
    public long getHashSize() {
        return allValues.size();
    }

    @Override
    public Object getHashValue(Value key) {
        return allValues.get(key.asString());
    }

    @Override
    public boolean hasHashEntry(Value key) {
        return allValues.containsKey(key.asString());
    }

    @Override
    public void putHashEntry(Value key, Value value) {
        throw new UnsupportedOperationException();
    }
    
}