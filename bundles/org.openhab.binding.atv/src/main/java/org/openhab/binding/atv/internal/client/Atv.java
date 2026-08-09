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
package org.openhab.binding.atv.internal.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.jmdns.JmDNS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.conf.AtvConfig;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.conf.Service;
import org.openhab.binding.atv.internal.client.core.AppleTVRelay;
import org.openhab.binding.atv.internal.client.core.AtvRuntime;
import org.openhab.binding.atv.internal.client.core.Core;
import org.openhab.binding.atv.internal.client.core.CoreStateDispatcher;
import org.openhab.binding.atv.internal.client.core.DeviceLoop;
import org.openhab.binding.atv.internal.client.core.ListenerRegistry;
import org.openhab.binding.atv.internal.client.core.ProtocolModule;
import org.openhab.binding.atv.internal.client.core.ProtocolStateDispatcher;
import org.openhab.binding.atv.internal.client.core.SetupData;
import org.openhab.binding.atv.internal.client.core.TakeoverMethod;
import org.openhab.binding.atv.internal.client.dto.ConnectOptions;
import org.openhab.binding.atv.internal.client.dto.PairOptions;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.dto.ScanOptions;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionFailedError;
import org.openhab.binding.atv.internal.client.exceptions.DeviceIdMissingError;
import org.openhab.binding.atv.internal.client.exceptions.NoServiceError;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;
import org.openhab.binding.atv.internal.client.protocols.airplay.AirPlay;
import org.openhab.binding.atv.internal.client.protocols.companion.CompanionProtocolModule;
import org.openhab.binding.atv.internal.client.protocols.mrp.Mrp;
import org.openhab.binding.atv.internal.client.protocols.raop.Raop;
import org.openhab.binding.atv.internal.client.scan.JmdnsScanner;
import org.openhab.binding.atv.internal.client.scan.ScanOrchestrator;
import org.openhab.binding.atv.internal.client.scan.ScanProtocols;
import org.openhab.binding.atv.internal.client.scan.UnicastScanner;
import org.openhab.binding.atv.internal.client.settings.MemoryStorage;
import org.openhab.binding.atv.internal.client.settings.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main routines for interacting with an Apple TV: the top level {@link #scan(ScanOptions)},
 * {@link #connect(AtvConfig, ConnectOptions)} and
 * {@link #pair(AtvConfig, Protocol, PairOptions)} entry points.
 *
 * <p>
 * The protocol registry iterates in a fixed order — AirPlay, Companion, MRP, RAOP —
 * which determines the order protocols are set up in during connect.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Atv {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atv.class);

    /**
     * Protocol modules in setup order.
     */
    private static final Map<Protocol, ProtocolModule> PROTOCOLS;

    static {
        Map<Protocol, ProtocolModule> protocols = new LinkedHashMap<>();
        protocols.put(Protocol.AirPlay, AirPlay.MODULE);
        protocols.put(Protocol.Companion, CompanionProtocolModule.MODULE);
        protocols.put(Protocol.MRP, Mrp.MODULE);
        protocols.put(Protocol.RAOP, Raop.MODULE);
        PROTOCOLS = Collections.unmodifiableMap(protocols);
    }

    private Atv() {
    }

    /**
     * Returns the protocol module registry in its fixed iteration order.
     *
     * @return unmodifiable protocol-to-module map
     */
    public static Map<Protocol, ProtocolModule> protocols() {
        return PROTOCOLS;
    }

    /**
     * Scans for Apple TVs on the network and returns their configurations.
     *
     * <p>
     * Devices that are not ready (no service with an identifier) are filtered out, an
     * identifier restriction is applied when given, and stored settings are applied to
     * every returned configuration.
     *
     * @param options scan options
     * @return future completing with the discovered configurations
     */
    public static CompletableFuture<List<AtvConfig>> scan(ScanOptions options) {
        Scanner scanner = options.scanner() != null ? options.scanner() : Atv::discover;
        Storage storage = options.storage() != null ? options.storage() : new MemoryStorage();

        return scanner.discover(options).thenCompose(devices -> {
            List<AtvConfig> filtered = ScanOrchestrator.filterDevices(devices, options.identifiers());

            CompletableFuture<Void> settingsChain = CompletableFuture.completedFuture(null);
            for (AtvConfig device : filtered) {
                settingsChain = settingsChain
                        .thenCompose(ignore -> storage.getSettings(device).thenAccept(device::apply));
            }
            return settingsChain.thenApply(ignore -> filtered);
        });
    }

    /**
     * Default scanner selection: unicast scanning when hosts are given, multicast browsing
     * otherwise. Multicast browsing creates one jmDNS instance per local private (site-local)
     * IPv4 address, so devices on any interface are found.
     */
    private static CompletableFuture<List<AtvConfig>> discover(ScanOptions options) {
        ScanOrchestrator orchestrator;
        Runnable cleanup = () -> {
        };
        try {
            if (!options.hosts().isEmpty()) {
                List<InetAddress> hosts = new ArrayList<>();
                for (String host : options.hosts()) {
                    hosts.add(InetAddress.getByName(host));
                }
                orchestrator = new UnicastScanner(hosts, options.knock());
            } else {
                List<JmDNS> instances = createJmdnsInstances();
                orchestrator = new JmdnsScanner(instances, false);
                cleanup = () -> {
                    for (JmDNS jmdns : instances) {
                        try {
                            jmdns.close();
                        } catch (IOException e) {
                            LOGGER.debug("Failed to close jmDNS instance", e);
                        }
                    }
                };
            }
        } catch (UnknownHostException e) {
            return CompletableFuture.failedFuture(new ConnectionFailedError("invalid host", e));
        } catch (IOException e) {
            return CompletableFuture.failedFuture(new ConnectionFailedError("failed to set up scanner", e));
        }

        ScanProtocols.registerAll(orchestrator, options.protocols());

        Runnable finalCleanup = cleanup;
        return orchestrator.discover(options.timeout()).whenComplete((result, error) -> finalCleanup.run())
                .thenApply(devices -> List.copyOf(devices.values()));
    }

    /**
     * Creates one jmDNS instance per up, non-loopback, private (site-local) IPv4 address of
     * the local host, falling back to a default-bound instance when none is usable.
     * Per-address failures are logged at debug level and otherwise ignored.
     *
     * @return jmDNS instances to browse with (at least one)
     * @throws IOException if not even a fallback instance can be created
     */
    private static List<JmDNS> createJmdnsInstances() throws IOException {
        List<JmDNS> instances = new ArrayList<>();
        try {
            var interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                var addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!(address instanceof java.net.Inet4Address) || address.isLoopbackAddress()
                            || !address.isSiteLocalAddress()) {
                        continue;
                    }
                    try {
                        instances.add(JmDNS.create(address));
                    } catch (IOException e) {
                        LOGGER.debug("Failed to set up jmDNS on {}", address, e);
                    }
                }
            }
        } catch (java.net.SocketException e) {
            LOGGER.debug("Failed to enumerate network interfaces", e);
        }
        if (instances.isEmpty()) {
            instances.add(JmDNS.create());
        }
        return instances;
    }

    /**
     * Connects to a device based on a configuration.
     *
     * <p>
     * Loads settings from storage, applies them to a deep copy of the configuration,
     * builds the relay and sets up every enabled protocol in registry order. The returned
     * device is connected; close it with {@link AppleTV#close()}.
     *
     * @param config device configuration
     * @param options connect options
     * @return future completing with the connected device; fails with
     *         {@link NoServiceError} when the configuration has no services and
     *         {@link DeviceIdMissingError} when it has no identifier
     */
    public static CompletableFuture<AppleTV> connect(AtvConfig config, ConnectOptions options) {
        if (config.services().isEmpty()) {
            return CompletableFuture.failedFuture(new NoServiceError("no service to connect to"));
        }
        if (config.identifier().isEmpty()) {
            return CompletableFuture.failedFuture(new DeviceIdMissingError("no device identifier"));
        }

        AtvRuntime runtime = options.runtime() != null ? options.runtime() : AtvRuntime.defaultRuntime();
        Storage storage = options.storage() != null ? options.storage() : new MemoryStorage();

        LOGGER.trace("Loading settings from {}", storage);
        return storage.getSettings(config).thenCompose(settings -> {
            AtvConfig configCopy = deepCopy(config);
            configCopy.apply(settings);

            DeviceLoop loop = runtime.newDeviceLoop();
            CoreStateDispatcher coreDispatcher = new CoreStateDispatcher(loop);
            AppleTVRelay atv = new AppleTVRelay(configCopy, settings, coreDispatcher, loop);

            // The relay joins the registry handed to every protocol core and relays
            // events (exactly once) to externally registered listeners.
            ListenerRegistry<DeviceListener> deviceListener = new ListenerRegistry<>(loop);
            deviceListener.add(atv);

            try {
                for (Map.Entry<Protocol, ProtocolModule> entry : PROTOCOLS.entrySet()) {
                    Protocol protocol = entry.getKey();
                    if (options.protocol() != null && options.protocol() != protocol) {
                        continue;
                    }
                    BaseService service = configCopy.getService(protocol).orElse(null);
                    if (service == null || !service.enabled()) {
                        if (service != null) {
                            LOGGER.debug("Ignore {} as it is disabled", protocol);
                        }
                        continue;
                    }

                    // Bind the protocol argument so the protocol does not have to deal with it
                    TakeoverMethod takeover = interfaces -> atv.takeover(protocol, interfaces);

                    // Core provides core access with a protocol specific twist
                    Core core = new Core(runtime, loop, configCopy, service, settings, deviceListener,
                            new ProtocolStateDispatcher(protocol, coreDispatcher), takeover);

                    for (SetupData setupData : entry.getValue().setup(core)) {
                        atv.addProtocol(setupData);
                    }
                }
            } catch (RuntimeException e) {
                // Release the loop resources created for this connection attempt
                loop.shutdown();
                return CompletableFuture.failedFuture(e);
            }

            return atv.connect().<AppleTV> thenApply(ignore -> atv).whenComplete((result, error) -> {
                if (error != null) {
                    loop.shutdown();
                }
            });
        });
    }

    /**
     * Pairs a protocol for an Apple TV.
     *
     * <p>
     * The pairing handler operates on the service instance of the given configuration
     * (not a copy), so obtained credentials are visible on the caller's configuration
     * after {@code finish()}.
     *
     * @param config device configuration
     * @param protocol protocol to pair
     * @param options pair options including handler settings like {@code name}
     * @return future completing with the pairing handler; fails with
     *         {@link NoServiceError} when the configuration has no service for the
     *         protocol and {@link NotSupportedError} when no implementation exists for it
     */
    public static CompletableFuture<PairingHandler> pair(AtvConfig config, Protocol protocol, PairOptions options) {
        BaseService service = config.getService(protocol).orElse(null);
        if (service == null) {
            return CompletableFuture.failedFuture(new NoServiceError("no service available for " + protocol));
        }

        ProtocolModule module = PROTOCOLS.get(protocol);
        if (module == null) {
            return CompletableFuture.failedFuture(new NotSupportedError("missing implementation for " + protocol));
        }

        AtvRuntime runtime = options.runtime() != null ? options.runtime() : AtvRuntime.defaultRuntime();
        Storage storage = options.storage() != null ? options.storage() : new MemoryStorage();

        return storage.getSettings(config).thenApply(settings -> {
            DeviceLoop loop = runtime.newDeviceLoop();
            Core core = new Core(runtime, loop, deepCopy(config), service, settings, new ListenerRegistry<>(loop),
                    new ProtocolStateDispatcher(protocol, new CoreStateDispatcher(loop)), TakeoverMethod.NO_OP);
            return module.pair(core, options.pairingOptions());
        });
    }

    /**
     * Returns a deep copy of a configuration: services are copied so pairing and settings
     * application never mutate the caller's instance.
     */
    private static AtvConfig deepCopy(AtvConfig config) {
        AtvConfig copy = new AtvConfig(config.address(), config.name(), config.deepSleep(), config.properties(),
                config.deviceInfo());
        for (BaseService service : config.services()) {
            copy.addService(Service.copyOf(service));
        }
        return copy;
    }
}
