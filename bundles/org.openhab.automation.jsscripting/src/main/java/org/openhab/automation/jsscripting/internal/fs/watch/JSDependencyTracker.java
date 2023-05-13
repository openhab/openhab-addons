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
package org.openhab.automation.jsscripting.internal.fs.watch;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.rulesupport.loader.AbstractScriptDependencyTracker;
import org.openhab.core.service.WatchService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Tracks JS module dependencies
 *
 * @author Jonathan Gilbert - Initial contribution
 * @author Jan N. Klug - Refactored to new WatchService
 */
@Component(service = JSDependencyTracker.class)
@NonNullByDefault
public class JSDependencyTracker extends AbstractScriptDependencyTracker {

    private static final String LIB_PATH = String.join(File.separator, "automation", "js", "node_modules");

    @Activate
    public JSDependencyTracker(@Reference(target = WatchService.CONFIG_WATCHER_FILTER) WatchService watchService) {
        super(watchService, LIB_PATH);
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "removeChangeTracker")
    public void addChangeTracker(ScriptDependencyTracker.Listener listener) {
        super.addChangeTracker(listener);
    }

    public void removeChangeTracker(ScriptDependencyTracker.Listener listener) {
        super.removeChangeTracker(listener);
    }
}
