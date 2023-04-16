/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.core.service.WatchService.Kind.*;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.service.WatchService;
import org.openhab.core.service.WatchService.Kind;

/**
 * Watches a Ruby lib dir
 *
 * @author Cody Cutrer - Initial contribution
 * @author Jan N. Klug - Refactored to new WatchService
 */
@NonNullByDefault
public class JRubyLibWatchService implements JRubyWatchService, WatchService.WatchEventListener {
    private final JRubyDependencyTracker dependencyTracker;
    private final WatchService watchService;
    private final List<Path> paths;

    JRubyLibWatchService(WatchService watchService, List<Path> paths, JRubyDependencyTracker dependencyTracker) {
        this.watchService = watchService;
        this.dependencyTracker = dependencyTracker;
        this.paths = paths;
    }

    @Override
    public void activate() {
        watchService.registerListener(this, paths);
    }

    @Override
    public void deactivate() {
        watchService.unregisterListener(this);
    }

    @Override
    public void processWatchEvent(Kind kind, Path path) {
        File file = path.toFile();
        if (!file.isHidden() && (kind == DELETE || (file.canRead() && (kind == CREATE || kind == MODIFY)))) {
            dependencyTracker.dependencyChanged(file.getPath());
        }
    }
}
