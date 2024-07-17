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
package org.openhab.binding.smaenergymeter.internal.packet;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smaenergymeter.internal.SMAEnergyMeterBindingConstants;
import org.openhab.binding.smaenergymeter.internal.packet.PacketListener.ReceivingTask;
import org.openhab.core.common.ThreadPoolManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of packet listener registry which manage multicast sockets.
 *
 * @author Łukasz Dywicki - Initial contribution
 */

@NonNullByDefault
@Component
public class DefaultPacketListenerRegistry implements PacketListenerRegistry {

    private final Logger logger = LoggerFactory.getLogger(DefaultPacketListenerRegistry.class);
    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool("OH-binding-" + SMAEnergyMeterBindingConstants.BINDING_ID + "-listener");
    private final Map<String, PacketListener> listeners = new ConcurrentHashMap<>();

    @Override
    public PacketListener getListener(String group, int port) throws IOException {
        String identifier = group + ":" + port;
        PacketListener listener = listeners.get(identifier);
        if (listener == null) {
            listener = new PacketListener(this, group, port);
            listeners.put(identifier, listener);
        }
        return listener;
    }

    @Deactivate
    protected void shutdown() throws IOException {
        for (Entry<String, PacketListener> entry : listeners.entrySet()) {
            try {
                entry.getValue().close();
            } catch (IOException e) {
                logger.warn("Multicast socket {} failed to terminate", entry.getKey(), e);
            }
        }
        scheduler.shutdownNow();
    }

    public ScheduledFuture<?> addTask(ReceivingTask runnable) {
        return scheduler.scheduleWithFixedDelay(runnable, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void execute(ReceivingTask receivingTask) {
        scheduler.execute(receivingTask);
    }

    public void close(String group, int port) {
        String listenerId = group + ":" + port;
        PacketListener listener = listeners.remove(listenerId);
        if (listener != null) {
            try {
                listener.close();
            } catch (IOException e) {
                logger.warn("Multicast socket {} failed to terminate", listenerId, e);
            }
        }
    }
}
