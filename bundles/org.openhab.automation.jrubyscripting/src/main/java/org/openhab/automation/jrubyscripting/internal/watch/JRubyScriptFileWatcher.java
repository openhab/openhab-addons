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

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.jrubyscripting.internal.JRubyScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.openhab.core.automation.module.script.rulesupport.loader.AbstractScriptFileWatcher;
import org.openhab.core.automation.module.script.rulesupport.loader.ScriptFileWatcher;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.StartLevelService;
import org.openhab.core.service.WatchService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors <openHAB-conf>/automation/ruby for Ruby files, but not libraries in lib or gems
 *
 * @author Cody Cutrer - Initial contribution
 * @author Jan N. Klug - Refactored to new WatchService
 */
@Component(immediate = true, service = { ScriptFileWatcher.class, ScriptDependencyTracker.Listener.class })
@NonNullByDefault
public class JRubyScriptFileWatcher extends AbstractScriptFileWatcher {
    private final Logger logger = LoggerFactory.getLogger(JRubyScriptFileWatcher.class);

    private static final String FILE_DIRECTORY = "automation" + File.separator + "ruby";

    private final JRubyScriptEngineFactory scriptEngineFactory;

    @Activate
    public JRubyScriptFileWatcher(final @Reference ScriptEngineManager manager,
            final @Reference ReadyService readyService, final @Reference StartLevelService startLevelService,
            final @Reference(target = "(" + Constants.SERVICE_PID
                    + "=org.openhab.automation.jrubyscripting)") ScriptEngineFactory scriptEngineFactory,
            final @Reference(target = WatchService.CONFIG_WATCHER_FILTER) WatchService watchService) {
        super(watchService, manager, readyService, startLevelService, FILE_DIRECTORY, true);

        this.scriptEngineFactory = (JRubyScriptEngineFactory) scriptEngineFactory;
    }

    @Override
    protected Optional<String> getScriptType(Path scriptFilePath) {
        String path = scriptFilePath.toString();

        if (scriptEngineFactory.isFileInGemHome(path) || scriptEngineFactory.isFileInLoadPath(path)) {
            return Optional.empty();
        }
        return super.getScriptType(scriptFilePath);
    }
}
