/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.yamlcomposer.internal;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IncludeRegistry} manages a bidirectional association between
 * main files and the include files they reference.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class IncludeRegistry {
    private final Map<Path, Set<Path>> mainToIncludes = new HashMap<>();
    private final Map<Path, Set<Path>> includeToMains = new HashMap<>();

    @SuppressWarnings("null")
    public synchronized void registerInclude(Path mainFile, Path include) {
        mainToIncludes.computeIfAbsent(mainFile, key -> new HashSet<>()).add(include);
        includeToMains.computeIfAbsent(include, key -> new HashSet<>()).add(mainFile);
    }

    public synchronized void removeMain(Path mainFile) {
        Set<Path> includes = mainToIncludes.remove(mainFile);
        if (includes == null) {
            return;
        }
        includes.forEach(include -> {
            Set<Path> mains = includeToMains.get(include);
            if (mains != null) {
                mains.remove(mainFile);
                if (mains.isEmpty()) {
                    includeToMains.remove(include);
                }
            }
        });
    }

    public synchronized Set<Path> getMainsForInclude(Path include) {
        return new HashSet<>(includeToMains.getOrDefault(include, Set.of()));
    }

    public synchronized boolean hasInclude(Path include) {
        Set<Path> mains = includeToMains.get(include);
        return mains != null && !mains.isEmpty();
    }

    public synchronized void clear() {
        mainToIncludes.clear();
        includeToMains.clear();
    }
}
