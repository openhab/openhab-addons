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
import java.util.ListIterator;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * LifecycleTracker implementation
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class LifecycleTracker {
    private List<Runnable> disposables = new ArrayList<>();

    public void addDisposeHook(Runnable disposable) {
        disposables.add(disposable);
    }

    public void dispose() {
        ListIterator<Runnable> iter = disposables.listIterator();
        while (iter.hasNext()) {
            iter.next().run();
            iter.remove();
        }
    }
}
