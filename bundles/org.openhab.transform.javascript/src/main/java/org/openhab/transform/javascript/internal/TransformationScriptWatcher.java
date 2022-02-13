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
package org.openhab.transform.javascript.internal;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;

import org.openhab.core.OpenHAB;
import org.openhab.core.service.AbstractWatchService;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TransformationScriptWatcher} watches the transformation directory for files. If a deleted/modified file is
 * detected, the script is passed to the {@link JavaScriptEngineManager}.
 *
 * @author Thomas Kordelle - Initial contribution
 * @author Thomas Kordelle - pre compiled scripts
 */
@Component
public class TransformationScriptWatcher extends AbstractWatchService {
    public static final String TRANSFORM_FOLDER = OpenHAB.getConfigFolder() + File.separator
            + TransformationService.TRANSFORM_FOLDER_NAME;

    private final Logger logger = LoggerFactory.getLogger(TransformationScriptWatcher.class);

    private final JavaScriptEngineManager manager;

    @Activate
    public TransformationScriptWatcher(final @Reference JavaScriptEngineManager manager) {
        super(TRANSFORM_FOLDER);
        this.manager = manager;
    }

    @Override
    public void activate() {
        super.activate();
    }

    @Override
    protected boolean watchSubDirectories() {
        return true;
    }

    @Override
    protected Kind<?>[] getWatchEventKinds(Path directory) {
        return new Kind<?>[] { ENTRY_DELETE, ENTRY_MODIFY };
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void processWatchEvent(WatchEvent<?> event, Kind<?> kind, Path path) {
        logger.debug("New watch event {} for path {}.", kind, path);

        if (kind == OVERFLOW) {
            return;
        }

        final WatchEvent<Path> ev = (WatchEvent<Path>) event;
        final Path filename = ev.context();

        logger.debug("Reloading javascript file {}.", filename);

        manager.removeFromCache(filename.toString());
    }
}
