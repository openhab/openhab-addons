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
package org.openhab.automation.jsscripting.internal;

import java.util.HashMap;
import java.util.Map;

import org.openhab.automation.jsscripting.internal.threading.ThreadsafeTimers;

/**
 * Abstraction layer to collect all features injected into the JS runtime during the context creation.
 *
 * @author Florian Hotze - Initial contribution
 */
public class JSRuntimeFeatures {
    /**
     * All elements of this Map are injected into the JS runtime using their key as the name.
     */
    private final Map<String, Object> features = new HashMap<>();
    private final Object lock;
    public final ThreadsafeTimers threadsafeTimers;

    JSRuntimeFeatures(Object lock) {
        this.lock = lock;
        this.threadsafeTimers = new ThreadsafeTimers(lock);

        features.put("ThreadsafeTimers", threadsafeTimers);
    }

    /**
     * Get the features that are to be injected into the JS runtime during context creation.
     * 
     * @return the runtime features
     */
    public Map<String, Object> getFeatures() {
        return features;
    }

    /**
     * Un-initialization hook, called when the engine is closed.
     * Use this method to clean up resources or cancel operations that were created by the JS runtime.
     */
    public void close() {
        threadsafeTimers.clearAll();
    }
}
