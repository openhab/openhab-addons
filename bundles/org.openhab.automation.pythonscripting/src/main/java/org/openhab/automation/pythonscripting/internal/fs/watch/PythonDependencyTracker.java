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
package org.openhab.automation.pythonscripting.internal.fs.watch;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngineFactory;
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
 * Tracks Python module dependencies
 *
 * @author Holger Hees - Initial contribution (Reused from jsscripting)
 */
@Component(service = PythonDependencyTracker.class)
@NonNullByDefault
public class PythonDependencyTracker extends AbstractScriptDependencyTracker {

    @Activate
    public PythonDependencyTracker(@Reference(target = WatchService.CONFIG_WATCHER_FILTER) WatchService watchService) {
        super(watchService, PythonScriptEngineFactory.PYTHON_LIB_PATH.toString());
    }

    @Deactivate
    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "removeChangeTracker")
    public void addChangeTracker(ScriptDependencyTracker.Listener listener) {
        super.addChangeTracker(listener);
    }

    @Override
    public void removeChangeTracker(ScriptDependencyTracker.Listener listener) {
        super.removeChangeTracker(listener);
    }
}
