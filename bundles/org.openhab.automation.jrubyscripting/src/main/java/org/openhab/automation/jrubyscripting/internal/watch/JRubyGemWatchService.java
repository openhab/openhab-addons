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
package org.openhab.automation.jrubyscripting.internal.watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.service.AbstractWatchService;

/**
 * Watches a gem home
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class JRubyGemWatchService extends AbstractWatchService {

    private static final String GEMSPEC = ".gemspec";

    private JRubyDependencyTracker dependencyTracker;

    JRubyGemWatchService(String path, JRubyDependencyTracker dependencyTracker) {
        super(path);
        this.dependencyTracker = dependencyTracker;
    }

    @Override
    protected boolean watchSubDirectories() {
        return true;
    }

    @Override
    protected WatchEvent.Kind<?> @Nullable [] getWatchEventKinds(Path path) {
        return new WatchEvent.Kind<?>[] { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY };
    }

    @Override
    protected void processWatchEvent(WatchEvent<?> watchEvent, WatchEvent.Kind<?> kind, Path path) {
        String file = path.toFile().getName();
        if (file.endsWith(GEMSPEC)) {
            // This seems really lazy, but you can't definitively tell the name
            // of a gem from the gemspec's filename. It's simply too ambiguous with version
            // numbers and platforms allowed to have `-` and `_` characters as well. RubyGems
            // doesn't do it either - it either has the name already, and searches for
            // `<name>-*.gemspec`, or it completely lists the all files on disk. Either way
            // it then executes the gemspec to get full details. We can't do that here in
            // pure Java and without a JRubyEngine available. So just punt and invalidate
            // _all_ subsets of hyphens. Worst case we invalidate a "parent" gem that didn't
            // need to be invalidated, but oh well, that just means a script reloads sometimes
            // when it didn't absolutely need to.
            String[] parts = file.split("-");
            for (int i = 0; i < parts.length - 1; ++i) {
                dependencyTracker.dependencyChanged("gem:" + String.join("-", Arrays.copyOf(parts, i + 1)));
            }
        }
    }
}
