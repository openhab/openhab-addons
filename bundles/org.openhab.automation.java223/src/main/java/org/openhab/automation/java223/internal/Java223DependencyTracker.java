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
package org.openhab.automation.java223.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.java223.common.Java223Constants;
import org.openhab.core.automation.module.script.rulesupport.loader.AbstractScriptDependencyTracker;
import org.openhab.core.service.WatchService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Tracks java module dependencies
 * Registering to the watch service is delayed because the bundle needs the Java223ScriptEngineFactory to go first.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@Component(service = Java223DependencyTracker.class)
@NonNullByDefault
public class Java223DependencyTracker extends AbstractScriptDependencyTracker {

    private final WatchService watchService;

    @Activate
    public Java223DependencyTracker(@Reference(target = WatchService.CONFIG_WATCHER_FILTER) WatchService watchService) {
        super(watchService, Java223Constants.LIB_DIR.toString());
        this.watchService = watchService;
        // Little hack: this instance was registered to the watch service inside super(). But we unregister it because
        // we want other services to be notified first.
        // We will re-register EXPLICITLY later (see @finalizeInitialisation)
        watchService.unregisterListener(this);
    }

    public void finalizeInitialisation() {
        watchService.registerListener(this, this.libraryPath);
    }

    @Deactivate
    @Override
    public void deactivate() {
        super.deactivate();
        watchService.unregisterListener(this);
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "removeChangeTracker")
    public void addChangeTracker(Listener listener) {
        super.addChangeTracker(listener);
    }

    @Override
    public void removeChangeTracker(Listener listener) {
        super.removeChangeTracker(listener);
    }
}
