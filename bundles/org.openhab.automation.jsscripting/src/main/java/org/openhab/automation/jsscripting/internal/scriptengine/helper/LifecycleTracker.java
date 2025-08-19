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
package org.openhab.automation.jsscripting.internal.scriptengine.helper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * LifecycleTracker implementation
 *
 * <p>
 * We can't use core's lifecycle tracker for JS Scripting, because its dispose hooks are called after the engine has
 * been closed (which will not work).
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class LifecycleTracker {
    private List<Runnable> disposables = new ArrayList<>();

    public void addDisposeHook(Runnable disposable) {
        disposables.add(disposable);
    }

    public void dispose() {
        for (Runnable disposable : disposables) {
            disposable.run();
        }
    }
}
