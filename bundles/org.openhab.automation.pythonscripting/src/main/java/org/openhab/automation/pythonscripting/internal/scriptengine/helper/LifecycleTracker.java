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
package org.openhab.automation.pythonscripting.internal.scriptengine.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * LifecycleTracker implementation
 *
 * @author Holger Hees - Initial contribution
 */
public class LifecycleTracker {
    List<Function<Object[], Object>> disposables = new ArrayList<>();

    public void addDisposeHook(Function<Object[], Object> disposable) {
        disposables.add(disposable);
    }

    public void dispose() {
        for (Function<Object[], Object> disposable : disposables) {
            disposable.apply(null);
        }
    }
}
