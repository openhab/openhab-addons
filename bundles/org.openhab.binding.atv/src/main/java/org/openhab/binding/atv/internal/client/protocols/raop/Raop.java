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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.time.Clock;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.PairingHandler;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.Features;
import org.openhab.binding.atv.internal.client.capability.Metadata;
import org.openhab.binding.atv.internal.client.capability.PushUpdater;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.capability.Stream;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.conf.Service;
import org.openhab.binding.atv.internal.client.core.Core;
import org.openhab.binding.atv.internal.client.core.ProtocolModule;
import org.openhab.binding.atv.internal.client.core.SetupData;
import org.openhab.binding.atv.internal.client.dto.DeviceModel;
import org.openhab.binding.atv.internal.client.dto.FeatureName;
import org.openhab.binding.atv.internal.client.dto.OperatingSystem;
import org.openhab.binding.atv.internal.client.dto.PairingRequirement;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.protocols.airplay.AirPlayPairingHandler;
import org.openhab.binding.atv.internal.client.protocols.airplay.AirPlayUtils;
import org.openhab.binding.atv.internal.client.protocols.mrp.Mrp;

/**
 * Module entry point for audio streaming using the Remote Audio Output Protocol (RAOP).
 *
 * <p>
 * The relay consumes the returned {@link SetupData}; all entry points take the unified
 * {@link Core} context. Streaming timing knobs and a randomness source are accepted by a
 * {@link #setup(Core, StreamTiming, Random)} overload for tests.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Raop {

    /** Zeroconf service type announced by RAOP receivers. */
    public static final String RAOP_SERVICE = "_raop._tcp.local";

    /** Zeroconf service type announced by AirPort Express devices. */
    public static final String AIRPORT_SERVICE = "_airport._tcp.local";

    /** Device-info map key: operating system ({@code OperatingSystem}). */
    public static final String KEY_OPERATING_SYSTEM = "os";
    /** Device-info map key: operating system version ({@code String}). */
    public static final String KEY_VERSION = "version";
    /** Device-info map key: device model ({@code DeviceModel}). */
    public static final String KEY_MODEL = "model";
    /** Device-info map key: raw device model string ({@code String}). */
    public static final String KEY_RAW_MODEL = "raw_model";
    /** Device-info map key: MAC address ({@code String}). */
    public static final String KEY_MAC = "mac";

    /** Model identifier patterns that indicate macOS. */
    private static final List<Pattern> OS_IDENTIFIER_FORMATS = List.of(Pattern.compile("MacBookAir\\d+,\\d+"),
            Pattern.compile("iMac\\d+,\\d+"), Pattern.compile("Macmini\\d+,\\d+"),
            Pattern.compile("MacBookPro\\d+,\\d+"), Pattern.compile("Mac\\d+,\\d+"),
            Pattern.compile("MacPro\\d+,\\d+"));

    /**
     * Unified module singleton implementing the {@link ProtocolModule} contract by
     * delegating to the static entry points of this class; consumed by the Wave-6 relay.
     * The pairing option {@code "name"} selects our display name during HAP pairing.
     */
    public static final ProtocolModule MODULE = new ProtocolModule() {

        @Override
        public Protocol protocol() {
            return Protocol.RAOP;
        }

        @Override
        public Set<String> scanServiceTypes() {
            return Set.of(RAOP_SERVICE, AIRPORT_SERVICE);
        }

        @Override
        public Map<String, Object> deviceInfo(String serviceType, Map<String, String> properties) {
            return Raop.deviceInfo(serviceType, properties);
        }

        @Override
        public void serviceInfo(Service service) {
            Raop.serviceInfo(service, null);
        }

        @Override
        public Set<SetupData> setup(Core core) {
            return Raop.setup(core);
        }

        @Override
        public PairingHandler pair(Core core, Map<String, Object> options) {
            return Raop.pair(core, options);
        }
    };

    private Raop() {
    }

    /**
     * Converts an RAOP service name ({@code MAC@Name}) to a device name.
     *
     * @param serviceName the mDNS service name
     * @return the device name
     */
    public static String raopNameFromServiceName(String serviceName) {
        String[] split = serviceName.split("@", 2);
        return split.length == 2 ? split[1] : split[0];
    }

    /**
     * Result of parsing a discovered RAOP mDNS service.
     *
     * @param name device name
     * @param service the parsed service
     */
    public record ScanResult(String name, Service service) {
    }

    /**
     * Parses and returns a new RAOP service. The unique identifier is the MAC part of the
     * {@code MAC@Name} service name.
     *
     * @param serviceName the mDNS service name (format {@code MAC@Name})
     * @param port announced port
     * @param properties mDNS service properties
     * @return device name and service
     */
    public static ScanResult raopServiceHandler(String serviceName, int port, Map<String, String> properties) {
        String name = raopNameFromServiceName(serviceName);
        String uniqueId = serviceName.split("@", 2)[0];
        Service service = new Service(uniqueId, Protocol.RAOP, port, properties, null, null, false,
                PairingRequirement.Unsupported, true);
        return new ScanResult(name, service);
    }

    /**
     * Returns device information from Zeroconf properties.
     *
     * @param serviceType Zeroconf service type the properties were announced under
     * @param properties Zeroconf service properties
     * @return device information map keyed by the {@code KEY_*} constants
     */
    public static Map<String, Object> deviceInfo(String serviceType, Map<String, String> properties) {
        Map<String, Object> devinfo = new LinkedHashMap<>();
        String am = properties.get("am");
        if (am != null) {
            devinfo.put(KEY_RAW_MODEL, am);
            DeviceModel model = Mrp.lookupModel(am);
            if (model != DeviceModel.Unknown) {
                devinfo.put(KEY_MODEL, model);
            }
            OperatingSystem operatingSystem = lookupOs(am);
            if (operatingSystem != OperatingSystem.Unknown) {
                devinfo.put(KEY_OPERATING_SYSTEM, operatingSystem);
            }
        }
        String ov = properties.get("ov");
        if (ov != null) {
            devinfo.put(KEY_VERSION, ov);
        }

        // This comes from _airport._tcp.local and belongs to AirPort Expresses
        String wama = properties.get("wama");
        if (wama != null) {
            Map<String, String> props = new LinkedHashMap<>();
            for (String prop : ("macaddress=" + wama).split(",")) {
                String[] keyValue = prop.split("=", 2);
                if (keyValue.length == 2) {
                    props.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            String macaddress = props.get("macaddress");
            if (!devinfo.containsKey(KEY_MAC) && macaddress != null) {
                devinfo.put(KEY_MAC, macaddress.replace("-", ":").toUpperCase(Locale.ROOT));
            }
            String syVs = props.get("syVs");
            if (syVs != null) {
                devinfo.put(KEY_VERSION, syVs);
            }
        }
        return devinfo;
    }

    /**
     * Looks up the operating system from a model identifier string: only macOS
     * identifiers are recognized, everything else is unknown.
     *
     * @param identifier model identifier, e.g. {@code "Macmini9,1"}
     * @return the operating system
     */
    public static OperatingSystem lookupOs(String identifier) {
        for (Pattern pattern : OS_IDENTIFIER_FORMATS) {
            if (pattern.matcher(identifier).lookingAt()) {
                return OperatingSystem.MacOS;
            }
        }
        return OperatingSystem.Unknown;
    }

    /**
     * Updates a service with pairing requirement information. Access control information
     * from a sibling AirPlay service takes precedence; the
     * general AirPlay heuristics apply otherwise.
     *
     * @param service the RAOP service to update
     * @param airplayService the AirPlay service of the same device, or {@code null}
     */
    public static void serviceInfo(Service service, @Nullable BaseService airplayService) {
        if (airplayService != null && "1".equals(airplayService.properties().getOrDefault("acl", "0"))) {
            // Access control might say that pairing is not possible, e.g. only devices
            // belonging to the same home (not handled here)
            service.setPairing(PairingRequirement.Disabled);
        } else if (airplayService != null && "2".equals(airplayService.properties().getOrDefault("act", "0"))) {
            // Similarly to ACL, we can have an access control type we do not support,
            // e.g. "2" which corresponds to "Current User". So we need to filter that.
            service.setPairing(PairingRequirement.Unsupported);
        } else {
            // Same behavior as for AirPlay expected, so reusing that here
            AirPlayUtils.updateServiceDetails(service);
        }
    }

    /**
     * Sets up a new RAOP service.
     *
     * @param core core context
     * @return setup data for the relay (a single entry)
     */
    public static Set<SetupData> setup(Core core) {
        return setup(core, StreamTiming.realTime(), new Random());
    }

    /**
     * Sets up a new RAOP service with explicit streaming knobs; used by tests to speed up
     * or determinize streaming.
     *
     * @param core core context
     * @param timing streaming timing knobs
     * @param rng randomness source for stream randomization
     * @return setup data for the relay (a single entry)
     */
    public static Set<SetupData> setup(Core core, StreamTiming timing, Random rng) {
        Clock clock = core.runtime().clock();
        RaopPlaybackManager playbackManager = new RaopPlaybackManager(core.address(), core.service(), core.settings(),
                timing, clock, rng);
        RaopMetadata metadata = new RaopMetadata(playbackManager);
        RaopPushUpdater pushUpdater = new RaopPushUpdater(metadata, core.stateDispatcher());

        // Listener for RAOP state changes. Triggers run inline on the streaming thread,
        // preserving the natural event order.
        RaopListener raopListener = new RaopListener() {

            @Override
            public void playing(PlaybackInfo playbackInfo) {
                playbackManager.setPlaybackInfo(playbackInfo);
                trigger();
            }

            @Override
            public void stopped() {
                playbackManager.setPlaybackInfo(null);
                trigger();
            }

            private void trigger() {
                if (pushUpdater.active()) {
                    pushUpdater.stateUpdated();
                }
            }
        };

        RaopAudio raopAudio = new RaopAudio(playbackManager, core.stateDispatcher());

        Map<Class<?>, Object> interfaces = Map.of(Stream.class,
                new RaopStream(core.service(), raopListener, raopAudio, playbackManager, core.takeover()),
                Features.class, new RaopFeatures(playbackManager), PushUpdater.class, pushUpdater, Metadata.class,
                metadata, Audio.class, raopAudio, RemoteControl.class, new RaopRemoteControl(playbackManager));

        Supplier<CompletableFuture<Boolean>> connect = () -> CompletableFuture.completedFuture(true);

        // Sessions are torn down per stream, so this is mostly defensive: release any
        // lingering session
        Runnable close = playbackManager::teardown;

        // Read per-service-type scan properties from core.config.properties; fall back to
        // the RAOP service's own properties when the configuration was built without scan
        // data (manual/test configurations), so device info stays useful.
        Supplier<Map<String, Object>> deviceInfoSupplier = () -> {
            Map<String, Object> devinfo = new LinkedHashMap<>();
            Map<String, Map<String, String>> allProperties = !core.config().properties().isEmpty()
                    ? core.config().properties()
                    : Map.of(RAOP_SERVICE, core.service().properties());
            for (String serviceType : MODULE.scanServiceTypes()) {
                Map<String, String> properties = allProperties.get(serviceType);
                if (properties != null) {
                    devinfo.putAll(deviceInfo(serviceType, properties));
                }
            }
            return devinfo;
        };

        Set<FeatureName> features = EnumSet.of(FeatureName.StreamFile, FeatureName.PushUpdates, FeatureName.Artist,
                FeatureName.Album, FeatureName.Title, FeatureName.Position, FeatureName.TotalTime,
                FeatureName.SetVolume, FeatureName.Volume, FeatureName.VolumeUp, FeatureName.VolumeDown,
                FeatureName.Stop, FeatureName.Pause);

        return Set.of(new SetupData(Protocol.RAOP, connect, close, deviceInfoSupplier, interfaces, features));
    }

    /**
     * Returns a pairing handler for the protocol: RAOP reuses the AirPlay pairing handler
     * with the protocol version from parsers/settings.
     *
     * @param core core context
     * @param options pairing options ({@code "name"} selects the display name)
     * @return pairing handler
     */
    public static PairingHandler pair(Core core, Map<String, Object> options) {
        Object name = options == null ? null : options.get("name");
        return new AirPlayPairingHandler(core.service(), core.address(),
                AirPlayUtils.getProtocolVersion(core.service(), core.settings().protocols().raop().protocolVersion()),
                name == null ? null : name.toString());
    }
}
