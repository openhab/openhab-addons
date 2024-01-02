/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.jrubyscripting.internal.JRubyScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.service.WatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks Ruby dependencies
 *
 * @author Cody Cutrer - Initial contribution
 * @author Jan N. Klug - Refactored to new WatchService
 */
@NonNullByDefault
public class JRubyDependencyTracker implements ScriptDependencyTracker {
    private final Logger logger = LoggerFactory.getLogger(JRubyDependencyTracker.class);

    private final Set<ScriptDependencyTracker.Listener> dependencyChangeListeners = ConcurrentHashMap.newKeySet();

    private final BidiSetBag<String, String> scriptToLibs = new BidiSetBag<>();

    private final JRubyScriptEngineFactory scriptEngineFactory;
    private final List<JRubyWatchService> dependencyWatchServices = new ArrayList<>();
    private final WatchService watchService;

    public JRubyDependencyTracker(final WatchService watchService, final JRubyScriptEngineFactory scriptEngineFactory) {
        this.watchService = watchService;
        this.scriptEngineFactory = scriptEngineFactory;
    }

    public void activate() {
        String gemHome = scriptEngineFactory.getGemHome();
        if (!gemHome.isEmpty()) {
            dependencyWatchServices.add(new JRubyGemWatchService(watchService, gemHome, this));
        }
        List<Path> libPaths = scriptEngineFactory.getRubyLibPaths().stream().map(Path::of).toList();
        dependencyWatchServices.add(new JRubyLibWatchService(watchService, libPaths, this));

        dependencyWatchServices.forEach(JRubyWatchService::activate);
    }

    public void deactivate() {
        dependencyWatchServices.forEach(JRubyWatchService::deactivate);
        dependencyWatchServices.clear();
    }

    void dependencyChanged(String dependency) {
        Set<String> scripts = new HashSet<>(scriptToLibs.getKeys(dependency)); // take a copy as it will change as we
        logger.debug("{} changed; reimporting {} scripts...", dependency, scripts.size());
        for (String scriptUrl : scripts) {
            for (ScriptDependencyTracker.Listener listener : dependencyChangeListeners) {
                try {
                    listener.onDependencyChange(scriptUrl);
                } catch (Exception e) {
                    logger.warn("Failed to notify tracker of dependency change: {}: {}", e.getClass(), e.getMessage());
                }
            }
        }
    }

    @Override
    public Consumer<String> getTracker(String scriptId) {
        return dependencyPath -> startTracking(scriptId, dependencyPath);
    }

    @Override
    public void removeTracking(String scriptId) {
        scriptToLibs.removeKey(scriptId);
    }

    protected void startTracking(String scriptId, String libPath) {
        scriptToLibs.put(scriptId, libPath);
    }

    public void addChangeTracker(ScriptDependencyTracker.Listener listener) {
        logger.trace("adding change tracker listener {}", listener);
        dependencyChangeListeners.add(listener);
    }

    public void removeChangeTracker(ScriptDependencyTracker.Listener listener) {
        logger.trace("removing change tracker listener {}", listener);
        dependencyChangeListeners.remove(listener);
    }
}
