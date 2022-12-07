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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.service.AbstractWatchService;

/**
 * Watches a Ruby lib dir
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class JRubyLibWatchService extends AbstractWatchService {
    private JRubyDependencyTracker dependencyTracker;

    JRubyLibWatchService(String path, JRubyDependencyTracker dependencyTracker) {
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
        File file = path.toFile();
        if (!file.isHidden() && (kind.equals(ENTRY_DELETE)
                || (file.canRead() && (kind.equals(ENTRY_CREATE) || kind.equals(ENTRY_MODIFY))))) {
            dependencyTracker.dependencyChanged(file.getPath());
        }
    }
}
