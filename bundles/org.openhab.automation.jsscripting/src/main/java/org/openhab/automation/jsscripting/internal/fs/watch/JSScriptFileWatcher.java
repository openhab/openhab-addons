/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.Optional;

import org.openhab.automation.jsscripting.internal.GraalJSScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.openhab.core.automation.module.script.rulesupport.loader.ScriptFileReference;
import org.openhab.core.automation.module.script.rulesupport.loader.ScriptFileWatcher;
import org.openhab.core.service.ReadyService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Monitors <openHAB-conf>/automation/js for Javascript files
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@Component(immediate = true)
public class JSScriptFileWatcher extends ScriptFileWatcher {
    private static final String FILE_DIRECTORY = "automation" + File.separator + "js";

    @Activate
    public JSScriptFileWatcher(final @Reference ScriptEngineManager manager, final @Reference ReadyService readyService,
            final @Reference JSDependencyTracker jsDependencyTracker) {
        super(manager, jsDependencyTracker, readyService, FILE_DIRECTORY);
    }

    @Activate
    @Override
    public void activate() {
        super.activate();
    }

    @Deactivate
    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected boolean createAndLoad(ScriptFileReference ref) {
        return super.createAndLoad(new ScriptFileReference(ref.getScriptFileURL()) {
            @Override
            public Optional<String> getScriptType() {
                assert super.getScriptType().get().equalsIgnoreCase("js");
                return Optional.of(GraalJSScriptEngineFactory.MIME_TYPE);
            }
        });
    }
}
