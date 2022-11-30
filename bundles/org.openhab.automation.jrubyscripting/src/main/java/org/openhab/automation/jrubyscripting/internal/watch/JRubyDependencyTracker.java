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
package org.openhab.automation.jrubyscripting.internal.watch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.jrubyscripting.internal.JRubyScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.service.AbstractWatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks Ruby dependencies
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class JRubyDependencyTracker implements ScriptDependencyTracker {
    private final Logger logger = LoggerFactory.getLogger(JRubyDependencyTracker.class);

    private final Set<ScriptDependencyTracker.Listener> dependencyChangeListeners = ConcurrentHashMap.newKeySet();

    private final BidiSetBag<String, String> scriptToLibs = new BidiSetBag<>();

    private final JRubyScriptEngineFactory scriptEngineFactory;
    private final List<AbstractWatchService> dependencyWatchServices = new ArrayList<>();

    public JRubyDependencyTracker(final JRubyScriptEngineFactory scriptEngineFactory) {
        this.scriptEngineFactory = scriptEngineFactory;
    }

    public void activate() {
        String gemHome = scriptEngineFactory.getGemHome();
        if (!gemHome.isEmpty()) {
            dependencyWatchServices.add(new JRubyGemWatchService(gemHome, this));
        }
        for (String libPath : scriptEngineFactory.getRubyLibPaths()) {
            dependencyWatchServices.add(new JRubyLibWatchService(libPath, this));
        }
        for (AbstractWatchService dependencyWatchService : dependencyWatchServices) {
            dependencyWatchService.activate();
        }
    }

    public void deactivate() {
        for (AbstractWatchService dependencyWatchService : dependencyWatchServices) {
            dependencyWatchService.deactivate();
        }
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
