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
package org.openhab.automation.jsscripting.internal.fs.watch;

import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.openhab.core.service.ReadyService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Monitors <openHAB-conf>/automation/js for Javascript files & libraries.
 *
 * This class is required to ensure that the *order* of set up is correct. Specifically, the dependency tracker must
 * be activated after the script file watcher. This is because AbstractWatchService only allows a single service to
 * watch a single directory, and given that the watchers are nested and the last registration wins, the one we want to
 * monitor the libraries must be registered last.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@Component(immediate = true, service = JSFileWatcher.class)
public class JSFileWatcher {

    private final JSScriptFileWatcher jsScriptFileWatcher;
    private final JSDependencyTracker jsDependencyTracker;

    @Activate
    public JSFileWatcher(final @Reference ScriptEngineManager manager, final @Reference ReadyService readyService) {
        jsDependencyTracker = new JSDependencyTracker();
        jsScriptFileWatcher = new JSScriptFileWatcher(manager, readyService, jsDependencyTracker);
    }

    @Activate
    public void activate() {
        jsScriptFileWatcher.activate();
        jsDependencyTracker.activate();
        jsDependencyTracker.addChangeTracker(jsScriptFileWatcher);
    }

    @Deactivate
    void deactivate() {
        jsDependencyTracker.removeChangeTracker(jsScriptFileWatcher);
        jsDependencyTracker.deactivate();
        jsScriptFileWatcher.deactivate();
    }
}
