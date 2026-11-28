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
package org.openhab.binding.knx.internal.client;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler.CommandExtensionData;
import org.openhab.binding.knx.internal.monitor.ConnectionKind;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;

import io.calimero.link.KNXLinkClosedException;
import io.calimero.link.KNXNetworkLink;
import io.calimero.link.NetworkLinkListener;
import io.calimero.mgmt.TransportLayerImpl;
import io.calimero.secure.Keyring;
import io.calimero.secure.Security;

/**
 * A real client on a stand-in link, without the pause before connecting, that remembers the secure layers it built.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class MonitorTestClient extends AbstractKNXClient {
    static final String KEYRING = "openhab6.knxkeys";
    static final String KEYRING_PASSWORD = "habopen";

    private final Supplier<KNXNetworkLink> links;
    final List<RecordingSecureManagement> secureLayers = new CopyOnWriteArrayList<>();

    public MonitorTestClient(ScheduledExecutorService scheduler, TelegramMonitor monitor,
            Supplier<KNXNetworkLink> links) {
        this(scheduler, monitor, links, new TreeMap<>());
    }

    /**
     * @param readRetriesLimit how often a read without answer is sent in all
     */
    public MonitorTestClient(ScheduledExecutorService scheduler, TelegramMonitor monitor,
            Supplier<KNXNetworkLink> links, int readRetriesLimit) {
        super(0, new ThingUID("knx", "ip", "test"), 1, 50, readRetriesLimit, scheduler,
                new CommandExtensionData(new TreeMap<>()), monitor, keyringSecurity(), () -> null, new NoStatus());
        this.links = links;
    }

    MonitorTestClient(ScheduledExecutorService scheduler, TelegramMonitor monitor, Supplier<KNXNetworkLink> links,
            SortedMap<String, Long> unknownGA) {
        super(0, new ThingUID("knx", "ip", "test"), 1, 50, 1, scheduler, new CommandExtensionData(unknownGA), monitor,
                keyringSecurity(), () -> null, new NoStatus());
        this.links = links;
    }

    @Override
    protected void pauseBeforeConnecting() {
    }

    @Override
    protected KNXNetworkLink establishConnection() {
        return links.get();
    }

    @Override
    protected ConnectionKind connectionKind() {
        return ConnectionKind.TUNNEL;
    }

    @Override
    CustomSecureManagement newSecureManagement(TransportLayerImpl transportLayer) throws KNXLinkClosedException {
        RecordingSecureManagement sal = new RecordingSecureManagement(transportLayer, openhabSecurity);
        secureLayers.add(sal);
        return sal;
    }

    static Security keyringSecurity() {
        Security security = Security.newSecurity();
        security.useKeyring(loadKeyring(), KEYRING_PASSWORD.toCharArray());
        return security;
    }

    @SuppressWarnings("null")
    static Keyring loadKeyring() {
        return Keyring.load(
                MonitorTestClient.class.getClassLoader().getResource("misc" + File.separator + KEYRING).toString());
    }

    /** Knows which listeners are on it. */
    static class RecordingSecureManagement extends CustomSecureManagement {
        final Set<NetworkLinkListener> listeners = new java.util.concurrent.CopyOnWriteArraySet<>();

        RecordingSecureManagement(TransportLayerImpl transportLayer, Security security) throws KNXLinkClosedException {
            super(transportLayer, security);
        }

        int failureCounterOf(int errorType) {
            return failureCounter(errorType);
        }

        @Override
        public void addListener(@Nullable NetworkLinkListener l) {
            super.addListener(l);
            if (l != null) {
                listeners.add(l);
            }
        }

        @Override
        public void removeListener(@Nullable NetworkLinkListener l) {
            super.removeListener(l);
            listeners.remove(l);
        }
    }

    private static class NoStatus implements StatusUpdateCallback {
        @Override
        public void updateStatus(ThingStatus status) {
        }

        @Override
        public void updateStatus(ThingStatusDetail statusDetail, String description) {
        }

        @Override
        public void updateStatus(ThingStatus status, @Nullable String description) {
        }
    }
}
