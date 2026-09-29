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
package org.openhab.io.yamlcomposer.internal.dynamic;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Registry that manages dynamic source providers, tracks which files depend on source registries,
 * and evaluates entity change events to trigger targeted file recompilations.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@Component(service = DynamicSourceRegistry.class)
public class DynamicSourceRegistry {
    private static final String THREAD_POOL_NAME = "yamlcomposer";
    private static final long DEFAULT_RECOMPILE_DEBOUNCE_DELAY_MS = 3000;

    private final Map<String, DynamicSourceProvider<?>> sourceProviders;
    private final Map<String, Set<Path>> dependentFilesBySource = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final long debounceDelayMs;
    private final Object recompileLock = new Object();
    private final Map<Path, RecompileTask> pendingRecompiles = new HashMap<>();
    private volatile Consumer<Path> onFileRecompileListener = path -> {
    };

    private class RecompileTask implements Runnable {
        private final Path sourcePath;
        private @Nullable ScheduledFuture<?> future;

        RecompileTask(Path sourcePath) {
            this.sourcePath = sourcePath;
        }

        void schedule(ScheduledExecutorService scheduler, long delayMs) {
            this.future = scheduler.schedule(this, delayMs, TimeUnit.MILLISECONDS);
        }

        void cancel() {
            if (future != null) {
                future.cancel(false);
            }
        }

        @Override
        public void run() {
            Consumer<Path> listener;
            synchronized (recompileLock) {
                if (!Objects.equals(pendingRecompiles.get(sourcePath), this)) {
                    return;
                }
                pendingRecompiles.remove(sourcePath);
                listener = onFileRecompileListener;
            }
            listener.accept(sourcePath);
        }
    }

    @Activate
    public DynamicSourceRegistry( //
            @Reference ItemRegistrySourceProvider itemSourceProvider, //
            @Reference ThingRegistrySourceProvider thingSourceProvider) {

        this(Map.of( //
                itemSourceProvider.getSourceName(), itemSourceProvider, //
                thingSourceProvider.getSourceName(), thingSourceProvider //
        ), //
                ThreadPoolManager.getScheduledPool(THREAD_POOL_NAME), //
                DEFAULT_RECOMPILE_DEBOUNCE_DELAY_MS);
    }

    /** Package-private constructor for testing with custom provider maps and debounce delays. */
    DynamicSourceRegistry(Map<String, DynamicSourceProvider<?>> sourceProviders, long debounceDelayMs) {
        this(sourceProviders, ThreadPoolManager.getScheduledPool(THREAD_POOL_NAME), debounceDelayMs);
    }

    private DynamicSourceRegistry(Map<String, DynamicSourceProvider<?>> providers, ScheduledExecutorService scheduler,
            long debounceDelayMs) {

        this.sourceProviders = Map.copyOf(providers);
        this.scheduler = scheduler;
        this.debounceDelayMs = debounceDelayMs;

        for (DynamicSourceProvider<?> provider : sourceProviders.values()) {
            provider.setOnChangeListener(this::handleEntityChange);
        }
    }

    /** Sets the consumer to be notified when a specific file needs recompilation due to an entity change. */
    public void setOnFileRecompileListener(Consumer<Path> listener) {
        this.onFileRecompileListener = listener;
    }

    /** Returns true if this registry supports the given ALL CAPS source name (e.g., "ITEMS", "THINGS"). */
    public boolean supportsSource(String source) {
        return sourceProviders.containsKey(source);
    }

    /** Retrieves a point-in-time snapshot map for the given dynamic source name (e.g., "ITEMS", "THINGS"). */
    public @Nullable Map<String, Map<String, @Nullable Object>> getSourceMap(String source) {
        DynamicSourceProvider<?> provider = sourceProviders.get(source);
        if (provider == null) {
            return null;
        }
        return provider.getSourceMap();
    }

    /** Registers a file's runtime dependency on a dynamic source registry (e.g., "ITEMS", "THINGS"). */
    public void registerDependency(Path path, String source) {
        Objects.requireNonNull(dependentFilesBySource.computeIfAbsent(source, k -> ConcurrentHashMap.newKeySet()))
                .add(path);
    }

    /** Removes tracked dynamic source requirements for a deleted or unmodified file. */
    public void unregisterFileSources(Path sourcePath) {
        dependentFilesBySource.values().forEach(paths -> paths.remove(sourcePath));
        cancelPendingRecompile(sourcePath);
    }

    /** Clears all tracked file source registrations. */
    public void clear() {
        dependentFilesBySource.clear();
        cancelAllPendingRecompiles();
    }

    private void handleEntityChange(EntityChange change) {
        Set<Path> dependentFiles = dependentFilesBySource.get(change.source());
        if (dependentFiles != null) {
            for (Path sourcePath : dependentFiles) {
                scheduleRecompile(sourcePath);
            }
        }
    }

    private void scheduleRecompile(Path sourcePath) {
        synchronized (recompileLock) {
            cancelPendingRecompile(sourcePath);
            if (debounceDelayMs <= 0) {
                onFileRecompileListener.accept(sourcePath);
                return;
            }

            RecompileTask task = new RecompileTask(sourcePath);
            task.schedule(scheduler, debounceDelayMs);
            pendingRecompiles.put(sourcePath, task);
        }
    }

    private void cancelPendingRecompile(Path sourcePath) {
        synchronized (recompileLock) {
            RecompileTask task = pendingRecompiles.remove(sourcePath);
            if (task != null) {
                task.cancel();
            }
        }
    }

    private void cancelAllPendingRecompiles() {
        synchronized (recompileLock) {
            pendingRecompiles.values().forEach(task -> task.cancel());
            pendingRecompiles.clear();
        }
    }

    @Deactivate
    public void deactivate() {
        synchronized (recompileLock) {
            cancelAllPendingRecompiles();
            dependentFilesBySource.clear();
            sourceProviders.values().forEach(provider -> provider.setOnChangeListener(change -> {
            }));
            onFileRecompileListener = path -> {
            };
        }
    }
}
