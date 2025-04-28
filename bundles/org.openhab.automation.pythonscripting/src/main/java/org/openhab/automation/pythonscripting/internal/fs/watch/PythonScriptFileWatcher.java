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

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.openhab.core.automation.module.script.rulesupport.loader.AbstractScriptFileWatcher;
import org.openhab.core.automation.module.script.rulesupport.loader.ScriptFileWatcher;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.StartLevelService;
import org.openhab.core.service.WatchService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Monitors {@code <openHAB-conf>/automation/python} for Python files, but not libraries
 *
 * @author Holger Hees - Initial contribution (Reused from jsscripting)
 */
@Component(immediate = true, service = { ScriptFileWatcher.class, ScriptDependencyTracker.Listener.class })
@NonNullByDefault
public class PythonScriptFileWatcher extends AbstractScriptFileWatcher {
    @Activate
    public PythonScriptFileWatcher(
            final @Reference(target = WatchService.CONFIG_WATCHER_FILTER) WatchService watchService,
            final @Reference ScriptEngineManager manager, final @Reference ReadyService readyService,
            final @Reference StartLevelService startLevelService) {
        super(watchService, manager, readyService, startLevelService,
                PythonScriptEngineFactory.PYTHON_DEFAULT_PATH.toString(), true);
    }

    @Override
    protected Optional<String> getScriptType(Path scriptFilePath) {
        if (!scriptFilePath.startsWith(PythonScriptEngineFactory.PYTHON_LIB_PATH)) {
            Optional<String> scriptFileSuffix = super.getScriptType(scriptFilePath);
            if (scriptFileSuffix.isPresent() && "py".equals(scriptFileSuffix.get())) {
                return Optional.of(PythonScriptEngineFactory.SCRIPT_TYPE);
            }
        }
        return Optional.empty();
    }
}
