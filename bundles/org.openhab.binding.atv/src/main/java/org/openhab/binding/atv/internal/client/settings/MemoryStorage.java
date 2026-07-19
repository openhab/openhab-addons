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
package org.openhab.binding.atv.internal.client.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.conf.AtvConfig;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.exceptions.DeviceIdMissingError;

/**
 * Memory based storage module. Everything stored with it is forgotten when the process exits.
 *
 * <p>
 * Since {@link Settings} is immutable, {@link #updateSettings} replaces the stored entry instead of mutating it
 * in place.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MemoryStorage implements Storage {

    private final List<Settings> settings = new ArrayList<>();

    @Override
    public synchronized List<Settings> settings() {
        return List.copyOf(settings);
    }

    @Override
    public CompletableFuture<Void> save() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> load() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public synchronized CompletableFuture<Settings> getSettings(AtvConfig config) {
        List<String> identifiers = config.allIdentifiers();
        if (identifiers.isEmpty()) {
            return CompletableFuture
                    .failedFuture(new DeviceIdMissingError("no identifier for device " + config.name()));
        }

        int index = findSettings(identifiers);
        if (index >= 0) {
            return CompletableFuture.completedFuture(settings.get(index));
        }

        // No settings found: create new ones seeded from the configuration
        Settings created = updateFromConfig(config, Settings.ofDefaults());
        settings.add(created);
        return CompletableFuture.completedFuture(created);
    }

    @Override
    public synchronized CompletableFuture<Boolean> removeSettings(Settings settings) {
        return CompletableFuture.completedFuture(this.settings.remove(settings));
    }

    @Override
    public synchronized CompletableFuture<Void> updateSettings(AtvConfig config) {
        List<String> identifiers = config.allIdentifiers();
        if (identifiers.isEmpty()) {
            return CompletableFuture
                    .failedFuture(new DeviceIdMissingError("no identifier for device " + config.name()));
        }

        int index = findSettings(identifiers);
        if (index >= 0) {
            settings.set(index, updateFromConfig(config, settings.get(index)));
        } else {
            settings.add(updateFromConfig(config, Settings.ofDefaults()));
        }
        return CompletableFuture.completedFuture(null);
    }

    private int findSettings(List<String> identifiers) {
        for (int i = 0; i < settings.size(); i++) {
            ProtocolSettings protocols = settings.get(i).protocols();
            if (identifiers.contains(protocols.airplay().identifier())
                    || identifiers.contains(protocols.companion().identifier())
                    || identifiers.contains(protocols.mrp().identifier())
                    || identifiers.contains(protocols.raop().identifier())) {
                return i;
            }
        }
        return -1;
    }

    // The config is the source of truth, so identifier/credentials/password are overwritten even when absent.
    private static Settings updateFromConfig(AtvConfig config, Settings settings) {
        ProtocolSettings protocols = settings.protocols();
        for (BaseService service : config.services()) {
            String identifier = service.identifier().orElse(null);
            String credentials = service.credentials().orElse(null);
            String password = service.password().orElse(null);
            protocols = switch (service.protocol()) {
                case AirPlay -> protocols.withAirplay(
                        new AirPlaySettings(identifier, credentials, password, protocols.airplay().mrpTunnel()));
                case Companion -> protocols.withCompanion(new CompanionSettings(identifier, credentials));
                case MRP -> protocols.withMrp(new MrpSettings(identifier, credentials));
                case RAOP -> protocols.withRaop(
                        new RaopSettings(identifier, credentials, password, protocols.raop().protocolVersion(),
                                protocols.raop().timingPort(), protocols.raop().controlPort()));
                default -> protocols;
            };
        }
        return settings.withProtocols(protocols);
    }

    @Override
    public String toString() {
        return "MemoryStorage";
    }
}
