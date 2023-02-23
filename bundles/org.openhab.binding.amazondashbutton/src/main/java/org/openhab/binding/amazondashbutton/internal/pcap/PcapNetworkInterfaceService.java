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
package org.openhab.binding.amazondashbutton.internal.pcap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openhab.core.common.ThreadPoolManager;
import org.pcap4j.core.PcapNetworkInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PcapNetworkInterfaceService} is a singleton which can be obtained by calling {@link #instance()}.
 * It provides all available {@link PcapNetworkInterface}s which are bound to an address. These network interfaces can
 * be retrieved by calling {@link #getNetworkInterfaces()}.
 *
 * Moreover the {@link PcapNetworkInterfaceService} provided the possibility to register a
 * {@link PcapNetworkInterfaceListener}s which are notified on new and removed {@link PcapNetworkInterface}s.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class PcapNetworkInterfaceService {

    private final Logger logger = LoggerFactory.getLogger(PcapNetworkInterfaceService.class);

    private static PcapNetworkInterfaceService instance = null;
    private static final String THREADPOOL_NAME = "pcapNetworkInterfaceService";

    private final Set<PcapNetworkInterfaceListener> listeners = new CopyOnWriteArraySet<>();
    private final Set<PcapNetworkInterfaceWrapper> pcapNetworkInterfaces = new CopyOnWriteArraySet<>();

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(THREADPOOL_NAME);

    private ScheduledFuture<?> future = null;

    private final Runnable pollingRunnable = () -> {
        synchronized (pcapNetworkInterfaces) {
            final Set<PcapNetworkInterfaceWrapper> determinedNetworkInterfaces = determineBoundNetworkInterfaces();
            final Set<PcapNetworkInterfaceWrapper> currentNetworkInterfaces = new HashSet<>(pcapNetworkInterfaces);

            final Set<PcapNetworkInterfaceWrapper> newNetworkInterfaces = new HashSet<>(determinedNetworkInterfaces);
            newNetworkInterfaces.removeIf(currentNetworkInterfaces::contains);

            final Set<PcapNetworkInterfaceWrapper> removedNetworkInterfaces = new HashSet<>(currentNetworkInterfaces);
            removedNetworkInterfaces.removeIf(determinedNetworkInterfaces::contains);

            pcapNetworkInterfaces.clear();
            pcapNetworkInterfaces.addAll(determinedNetworkInterfaces);

            for (PcapNetworkInterfaceWrapper pcapNetworkInterface : newNetworkInterfaces) {
                notifyNetworkInterfacesAdded(pcapNetworkInterface);
            }

            for (PcapNetworkInterfaceWrapper pcapNetworkInterface : removedNetworkInterfaces) {
                notifyNetworkInterfacesRemoved(pcapNetworkInterface);
            }
        }
    };

    private PcapNetworkInterfaceService() {
    }

    /**
     * Returns the {@link PcapNetworkInterfaceService} singleton instance.
     *
     * @return The {@link PcapNetworkInterfaceService} singleton
     */
    public static synchronized PcapNetworkInterfaceService instance() {
        if (instance == null) {
            instance = new PcapNetworkInterfaceService();
        }
        return instance;
    }

    /**
     * Returns a {@link Set} of {@link PcapNetworkInterface}s which are bound to an address.
     *
     * @return the network interface set
     */
    public Set<PcapNetworkInterfaceWrapper> getNetworkInterfaces() {
        synchronized (pcapNetworkInterfaces) {
            return Collections.unmodifiableSet(pcapNetworkInterfaces.stream().collect(Collectors.toSet()));
        }
    }

    /**
     * Registers the given {@link PcapNetworkInterfaceListener}. If it is already registered, this method returns
     * immediately.
     *
     * @param networkInterfaceListener The {@link PcapNetworkInterfaceListener} to be registered.
     */
    public void registerListener(PcapNetworkInterfaceListener networkInterfaceListener) {
        final boolean isAdded = listeners.add(networkInterfaceListener);
        if (isAdded) {
            updatePollingState();
        }
    }

    /**
     * Unregisters the given {@link PcapNetworkInterfaceListener}. If it is already unregistered, this method returns
     * immediately.
     *
     * @param networkInterfaceListener The {@link PcapNetworkInterfaceListener} to be unregistered.
     */
    public void unregisterListener(PcapNetworkInterfaceListener networkInterfaceListener) {
        final boolean isRemoved = listeners.remove(networkInterfaceListener);
        if (isRemoved) {
            updatePollingState();
        }
    }

    private void notifyNetworkInterfacesAdded(PcapNetworkInterfaceWrapper pcapNetworkInterface) {
        for (PcapNetworkInterfaceListener listener : listeners) {
            notifyNetworkInterfacesAdded(listener, pcapNetworkInterface);
        }
    }

    private void notifyNetworkInterfacesRemoved(PcapNetworkInterfaceWrapper pcapNetworkInterface) {
        for (PcapNetworkInterfaceListener listener : listeners) {
            notifyNetworkInterfacesRemoved(listener, pcapNetworkInterface);
        }
    }

    private void notifyNetworkInterfacesAdded(PcapNetworkInterfaceListener listener,
            PcapNetworkInterfaceWrapper pcapNetworkInterface) {
        try {
            listener.onPcapNetworkInterfaceAdded(pcapNetworkInterface);
        } catch (Exception e) {
            logger.error("An exception occurred while calling onPcapNetworkInterfaceAdded for {}", listener, e);
        }
    }

    private void notifyNetworkInterfacesRemoved(PcapNetworkInterfaceListener listener,
            PcapNetworkInterfaceWrapper pcapNetworkInterface) {
        try {
            listener.onPcapNetworkInterfaceRemoved(pcapNetworkInterface);
        } catch (Exception e) {
            logger.error("An exception occurred while calling onPcapNetworkInterfaceRemoved for {}", listener, e);
        }
    }

    /**
     * Returns all pcap network interfaces relying on {@link PcapUtil#getAllNetworkInterfaces()}. The list is filtered
     * as all interfaces which are not bound to an address are excluded.
     *
     * @return An {@link Iterable} of all {@link PcapNetworkInterfaceWrapper}s which are bound to an address
     */
    private Set<PcapNetworkInterfaceWrapper> determineBoundNetworkInterfaces() {
        final Set<PcapNetworkInterfaceWrapper> allNetworkInterfaces = new HashSet<>();
        PcapUtil.getAllNetworkInterfaces().forEach(allNetworkInterfaces::add);
        allNetworkInterfaces.removeIf(networkInterface -> {
            final boolean notSuitable = networkInterface.getAddresses().isEmpty();
            if (notSuitable) {
                logger.debug("{} is not a suitable network interfaces as no addresses are bound to it.",
                        networkInterface.getName());
            }
            return notSuitable;
        });
        return allNetworkInterfaces;
    }

    private void updatePollingState() {
        boolean isPolling = future != null;
        if (isPolling && listeners.isEmpty()) {
            future.cancel(true);
            future = null;
            return;
        }
        if (!isPolling && !listeners.isEmpty()) {
            future = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, 2, TimeUnit.SECONDS);
        }
    }
}
