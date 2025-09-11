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
package org.openhab.automation.jrubyscripting.internal.watch;

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.jrubyscripting.internal.JRubyScriptEngineConfiguration;
import org.openhab.automation.jrubyscripting.internal.JRubyScriptEngineFactory;
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
 * Monitors {@code <openHAB-conf>/automation/ruby} for Ruby files, but not libraries in lib or gems
 *
 * @author Cody Cutrer - Initial contribution
 * @author Jan N. Klug - Refactored to new WatchService
 */
@Component(immediate = true, service = { ScriptFileWatcher.class, JRubyScriptFileWatcher.class,
        ScriptDependencyTracker.Listener.class })
@NonNullByDefault
public class JRubyScriptFileWatcher extends AbstractScriptFileWatcher {
    private final JRubyScriptEngineFactory scriptEngineFactory;

    @Activate
    public JRubyScriptFileWatcher(final @Reference ScriptEngineManager manager,
            final @Reference ReadyService readyService, final @Reference StartLevelService startLevelService,
            final @Reference JRubyScriptEngineFactory scriptEngineFactory,
            final @Reference(target = WatchService.CONFIG_WATCHER_FILTER) WatchService watchService) {
        super(watchService, manager, readyService, startLevelService,
                JRubyScriptEngineConfiguration.HOME_PATH.toString(), true);

        this.scriptEngineFactory = scriptEngineFactory;
    }

    @Override
    protected Optional<String> getScriptType(Path scriptFilePath) {
        String path = scriptFilePath.toString();

        if (scriptEngineFactory.isFileInGemHome(path) || scriptEngineFactory.isFileInLoadPath(path)) {
            return Optional.empty();
        }

        return super.getScriptType(scriptFilePath).filter(type -> scriptEngineFactory.getScriptTypes().contains(type));
    }

    // Overriding to make it public, so it can be used in {@link JRubyConsoleCommandExtension}
    @Override
    public Path getWatchPath() {
        return super.getWatchPath();
    }
}
