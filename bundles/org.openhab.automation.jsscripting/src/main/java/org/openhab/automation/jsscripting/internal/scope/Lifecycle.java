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
package org.openhab.automation.jsscripting.internal.scope;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows scripts to register for lifecycle events
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public class Lifecycle implements ScriptDisposalAware {
    private static final Logger logger = LoggerFactory.getLogger(Lifecycle.class);
    public static final int DEFAULT_PRIORITY = 50;
    private List<Hook> listeners = new ArrayList<>();

    public void addDisposeHook(Consumer<Object> listener, int priority) {
        addListener(listener, priority);
    }

    public void addDisposeHook(Consumer<Object> listener) {
        addDisposeHook(listener, DEFAULT_PRIORITY);
    }

    private void addListener(Consumer<Object> listener, int priority) {
        listeners.add(new Hook(priority, listener));
    }

    @Override
    public void unload(String scriptIdentifier) {
        try {
            listeners.stream().sorted(Comparator.comparingInt(h -> h.priority))
                    .forEach(h -> h.fn.accept(scriptIdentifier));
        } catch (RuntimeException ex) {
            logger.warn("Script unloading halted due to exception in disposal: {}: {}", ex.getClass(), ex.getMessage());
        }
    }

    private static class Hook {
        public Hook(int priority, Consumer<Object> fn) {
            this.priority = priority;
            this.fn = fn;
        }

        int priority;
        Consumer<Object> fn;
    }
}
