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
package org.openhab.binding.pulseaudio.internal;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioHandler;
import org.openhab.binding.pulseaudio.internal.items.SimpleProtocolTCPModule;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection to a pulseaudio Simple TCP Protocol
 *
 * @author Gwendal Roulleau - Initial contribution
 * @author Miguel Álvarez - Refactor some code from PulseAudioAudioSink here
 * @author Miguel Álvarez - Use socket per stream
 *
 */
@NonNullByDefault
public abstract class PulseaudioSimpleProtocolStream {

    private final Logger logger = LoggerFactory.getLogger(PulseaudioSimpleProtocolStream.class);

    protected final PulseaudioHandler pulseaudioHandler;
    protected final ScheduledExecutorService scheduler;
    protected boolean initialized = false;
    protected boolean closed = false;
    /**
     * Collect sockets by module id.
     */
    protected final Map<Integer, Socket> moduleSockets = new HashMap<>();
    /**
     * Collect created unused modules
     */
    protected final Set<ModuleCache> idleModules = new HashSet<>();
    /**
     * Collect modules in use by some stream
     */
    protected final Set<SimpleProtocolTCPModule> activeModules = new HashSet<>();

    public PulseaudioSimpleProtocolStream(PulseaudioHandler pulseaudioHandler, ScheduledExecutorService scheduler) {
        this.pulseaudioHandler = pulseaudioHandler;
        this.scheduler = scheduler;
    }

    /**
     * Load simple protocol instance
     *
     * @throws IOException
     * @throws InterruptedException when interrupted during the loading module wait
     */
    public AcquireModuleResult acquireSimpleProtocolModule(AudioFormat audioFormat)
            throws IOException, InterruptedException {
        if (closed) {
            throw new IOException("Resource is closed");
        }
        @Nullable
        SimpleProtocolTCPModule idleModule = null;
        synchronized (idleModules) {
            if (!initialized) {
                // remove pre-existent modules for device in port range
                pulseaudioHandler.clearSimpleProtocolTCPModules();
                initialized = true;
            }
            logger.debug("idle modules: {}", idleModules.size());
            var cachedModule = idleModules.stream() //
                    .filter(c -> c.audioFormat.equals(audioFormat)) //
                    .findFirst().orElse(null);
            if (cachedModule != null) {
                idleModule = cachedModule.module;
                logger.debug("Will try to reuse idle module {}", idleModule.getId());
                idleModules.remove(cachedModule);
            }
        }
        logger.debug("loading simple protocol tcp module");
        var optionalModule = pulseaudioHandler.loadSimpleProtocolModule(audioFormat, idleModule);
        if (optionalModule.isEmpty()) {
            return new AcquireModuleResult(Optional.empty(), () -> {
            });
        }
        var spModule = optionalModule.get();
        activeModules.add(spModule);
        return new AcquireModuleResult(optionalModule, () -> releaseModule(audioFormat, spModule));
    }

    /**
     * Connect to pulseaudio with the simple protocol
     *
     * @throws IOException
     */
    public Socket connectIfNeeded(SimpleProtocolTCPModule spModule) throws IOException {
        Socket spSocket = moduleSockets.get(spModule.getId());
        if (spSocket == null || !spSocket.isConnected() || spSocket.isClosed()) {
            logger.debug("Simple TCP Stream connecting to module {} in {}", spModule.getId(), getLabel(null));
            String host = pulseaudioHandler.getHost();
            var clientSocketFinal = new Socket(host, spModule.getPort());
            clientSocketFinal.setSoTimeout(pulseaudioHandler.getBasicProtocolSOTimeout());
            synchronized (moduleSockets) {
                Socket prevSocket = moduleSockets.put(spModule.getId(), clientSocketFinal);
                if (prevSocket != null) {
                    disconnect(prevSocket);
                }
            }
            return clientSocketFinal;
        }
        return spSocket;
    }

    private void releaseModule(AudioFormat audioFormat, SimpleProtocolTCPModule spModule) {
        logger.debug("releasing module: {}", spModule.getId());
        ArrayList<SimpleProtocolTCPModule> modulesToRemove = new ArrayList<>();
        activeModules.remove(spModule);
        int maxModules = pulseaudioHandler.getMaxIdleModules();
        if (!closed && maxModules > 0) {
            synchronized (idleModules) {
                logger.debug("keeping module {} idle", spModule.getId());
                while (idleModules.size() > maxModules - 1) {
                    var moduleCache = idleModules.iterator().next();
                    idleModules.remove(moduleCache);
                    modulesToRemove.add(moduleCache.module);
                }
                idleModules.add(new ModuleCache(audioFormat, spModule));
                logger.debug("idle modules: {}", idleModules.size());
                Socket spSocket = moduleSockets.remove(spModule.getId());
                if (spSocket != null) {
                    disconnect(spSocket);
                }
            }
        } else {
            modulesToRemove.add(spModule);
        }
        for (var module : modulesToRemove) {
            try {
                logger.debug("unloading module {}", module.getId());
                Socket spSocket = moduleSockets.remove(module.getId());
                if (spSocket != null) {
                    disconnect(spSocket);
                }
                pulseaudioHandler.unloadModule(module);
            } catch (IOException e) {
                logger.warn("IOException unloading module {}: {}", module.getId(), e.getMessage());
            }
        }
    }

    /**
     * Disconnect the socket from pulseaudio simple protocol
     */
    protected void disconnect(Socket spSocket) {
        if (spSocket.isClosed()) {
            return;
        }
        logger.debug("Simple TCP Stream disconnecting for {}", getLabel(null));
        try {
            spSocket.close();
        } catch (IOException ignored) {
        }
    }

    public void close() {
        closed = true;
        synchronized (moduleSockets) {
            for (var socket : moduleSockets.values()) {
                disconnect(socket);
            }
            moduleSockets.clear();
        }
        synchronized (idleModules) {
            for (var moduleCached : idleModules) {
                try {
                    pulseaudioHandler.unloadModule(moduleCached.module);
                } catch (IOException e) {
                    logger.warn("IOException unloading module {}: {}", moduleCached.module.getId(), e.getMessage());
                }
            }
            idleModules.clear();
        }
        synchronized (activeModules) {
            for (var module : activeModules) {
                try {
                    pulseaudioHandler.unloadModule(module);
                } catch (IOException e) {
                    logger.warn("IOException unloading module {}: {}", module.getId(), e.getMessage());
                }
            }
            activeModules.clear();
        }
    }

    public PercentType getVolume() {
        return new PercentType(pulseaudioHandler.getLastVolume());
    }

    public void setVolume(PercentType volume) {
        pulseaudioHandler.setVolume(volume.intValue());
    }

    public String getId() {
        return pulseaudioHandler.getThing().getUID().toString();
    }

    public String getLabel(@Nullable Locale locale) {
        var label = pulseaudioHandler.getThing().getLabel();
        return label != null ? label : pulseaudioHandler.getThing().getUID().getId();
    }

    private record ModuleCache(AudioFormat audioFormat, SimpleProtocolTCPModule module) {
    };

    public record AcquireModuleResult(Optional<SimpleProtocolTCPModule> module, Runnable releaseModule) {
    };
}
