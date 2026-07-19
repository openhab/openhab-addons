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
package org.openhab.binding.atv.internal.client.protocols.mrp;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.PairingHandler;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.Features;
import org.openhab.binding.atv.internal.client.capability.Metadata;
import org.openhab.binding.atv.internal.client.capability.Power;
import org.openhab.binding.atv.internal.client.capability.PushUpdater;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.conf.Service;
import org.openhab.binding.atv.internal.client.core.Core;
import org.openhab.binding.atv.internal.client.core.ProtocolModule;
import org.openhab.binding.atv.internal.client.core.SetupData;
import org.openhab.binding.atv.internal.client.dto.DeviceModel;
import org.openhab.binding.atv.internal.client.dto.FeatureName;
import org.openhab.binding.atv.internal.client.dto.OperatingSystem;
import org.openhab.binding.atv.internal.client.dto.PairingRequirement;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.DeviceInfoMessageOuterClass.DeviceInfoMessage;
import org.openhab.binding.atv.internal.client.support.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module entry point for the MediaRemoteTV protocol (used by ATV4 and later).
 *
 * <p>
 * The relay consumes the returned {@link SetupData}; all entry points take the unified
 * {@link Core} context.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Mrp {

    /** Zeroconf service type announced by MRP devices. */
    public static final String MEDIAREMOTE_SERVICE = "_mediaremotetv._tcp.local";

    /** Device-info map key: operating system ({@code OperatingSystem}). */
    public static final String KEY_OPERATING_SYSTEM = "os";
    /** Device-info map key: operating system version ({@code String}). */
    public static final String KEY_VERSION = "version";
    /** Device-info map key: operating system build number ({@code String}). */
    public static final String KEY_BUILD_NUMBER = "build_number";
    /** Device-info map key: device model ({@code DeviceModel}). */
    public static final String KEY_MODEL = "model";
    /** Device-info map key: raw device model string ({@code String}). */
    public static final String KEY_RAW_MODEL = "raw_model";
    /** Device-info map key: MAC address ({@code String}). */
    public static final String KEY_MAC = "mac";

    private static final Logger LOGGER = LoggerFactory.getLogger(Mrp.class);
    private static final Pattern BUILD_BASE_PATTERN = Pattern.compile("^(\\d+)[A-Z]");

    /** Apple TV build number to version mapping. */
    private static final Map<String, String> VERSION_LIST = Map.ofEntries(Map.entry("17J586", "13.0"),
            Map.entry("17K82", "13.2"), Map.entry("17K449", "13.3"), Map.entry("17K795", "13.3.1"),
            Map.entry("17L256", "13.4"), Map.entry("17L562", "13.4.5"), Map.entry("17L570", "13.4.6"),
            Map.entry("17M61", "13.4.8"), Map.entry("18J386", "14.0"), Map.entry("18J400", "14.0.1"),
            Map.entry("18J411", "14.0.2"), Map.entry("18K57", "14.2"), Map.entry("18K561", "14.3"),
            Map.entry("18K802", "14.4"), Map.entry("18L204", "14.5"), Map.entry("18L569", "14.6"),
            Map.entry("18M60", "14.7"), Map.entry("19J346", "15.0"), Map.entry("19J572", "15.1"),
            Map.entry("19J581", "15.1.1"), Map.entry("19K53", "15.2"), Map.entry("19K547", "15.3"),
            Map.entry("19L440", "15.4"), Map.entry("19L452", "15.4.1"), Map.entry("19L570", "15.5"),
            Map.entry("19L580", "15.5.1"), Map.entry("19M65", "15.6"), Map.entry("20J373", "16.0"),
            Map.entry("20K71", "16.1"), Map.entry("20K80", "16.1.1"), Map.entry("20K362", "16.2"),
            Map.entry("20K650", "16.3"), Map.entry("20K661", "16.3.1"), Map.entry("20K672", "16.3.2"),
            Map.entry("20K680", "16.3.3"), Map.entry("20L497", "16.4"), Map.entry("20L498", "16.4.1"),
            Map.entry("20L563", "16.5"), Map.entry("20M73", "16.6"), Map.entry("22J354", "17.0"),
            Map.entry("21K69", "17.1"), Map.entry("21K365", "17.2"), Map.entry("21K646", "17.3"),
            Map.entry("21L227", "17.4"), Map.entry("21L569", "17.5"), Map.entry("21L580", "17.5.1"),
            Map.entry("21M71", "17.6"), Map.entry("21M80", "17.6.1"), Map.entry("22J357", "18.0"),
            Map.entry("22J580", "18.1"));

    /** Model identifier to device model mapping. */
    private static final Map<String, DeviceModel> MODEL_LIST = Map.ofEntries(
            Map.entry("AirPort4,107", DeviceModel.AirPortExpress),
            Map.entry("AirPort10,115", DeviceModel.AirPortExpressGen2),
            Map.entry("AppleTV1,1", DeviceModel.AppleTVGen1), Map.entry("AppleTV2,1", DeviceModel.Gen2),
            Map.entry("AppleTV3,1", DeviceModel.Gen3), Map.entry("AppleTV3,2", DeviceModel.Gen3),
            Map.entry("AppleTV5,3", DeviceModel.Gen4), Map.entry("AppleTV6,2", DeviceModel.Gen4K),
            Map.entry("AppleTV11,1", DeviceModel.AppleTV4KGen2), Map.entry("AppleTV14,1", DeviceModel.AppleTV4KGen3),
            Map.entry("AudioAccessory1,1", DeviceModel.HomePod), Map.entry("AudioAccessory1,2", DeviceModel.HomePod),
            Map.entry("AudioAccessory5,1", DeviceModel.HomePodMini),
            Map.entry("AudioAccessorySingle5,1", DeviceModel.HomePodMini),
            Map.entry("AudioAccessory6,1", DeviceModel.HomePodGen2));

    /**
     * Unified module singleton implementing the {@link ProtocolModule} contract by
     * delegating to the static entry points of this class; consumed by the Wave-6 relay.
     * MRP takes no pairing options.
     */
    public static final ProtocolModule MODULE = new ProtocolModule() {

        @Override
        public Protocol protocol() {
            return Protocol.MRP;
        }

        @Override
        public Set<String> scanServiceTypes() {
            return Set.of(MEDIAREMOTE_SERVICE);
        }

        @Override
        public Map<String, Object> deviceInfo(String serviceType, Map<String, String> properties) {
            return Mrp.deviceInfo(serviceType, properties);
        }

        @Override
        public void serviceInfo(Service service) {
            Mrp.serviceInfo(service);
        }

        @Override
        public Set<SetupData> setup(Core core) {
            return Mrp.setup(core);
        }

        @Override
        public PairingHandler pair(Core core, Map<String, Object> options) {
            return Mrp.pair(core);
        }
    };

    private Mrp() {
    }

    /**
     * Sets up a new MRP service over its own TCP connection.
     *
     * @param core core context
     * @return setup data for the relay (a single entry)
     */
    public static Set<SetupData> setup(Core core) {
        return Set.of(createWithConnection(core,
                new MrpConnection(core.address(), core.service().port(), core.deviceListenerProxy()), true));
    }

    /**
     * Sets up a new MRP service from an existing connection (used by the Wave-5 AirPlay
     * MRP tunnel with {@code requiresHeartbeat=false}).
     *
     * @param core core context
     * @param connection transport to run the protocol on
     * @param requiresHeartbeat whether periodic heartbeats should be enabled after connect
     * @return setup data for the relay
     */
    public static SetupData createWithConnection(Core core, AbstractMrpConnection connection,
            boolean requiresHeartbeat) {
        MrpProtocol protocol = new MrpProtocol(connection, core.service(), core.settings().info(), core.loop(),
                core.runtime());
        PlayerStateManager psm = new PlayerStateManager(protocol);

        MrpRemoteControl remoteControl = new MrpRemoteControl(psm, protocol);
        MrpMetadata metadata = new MrpMetadata(protocol, psm, core.deviceIdentifier(), core.runtime().clock(),
                core.loop());
        MrpPower power = new MrpPower(protocol, remoteControl);
        MrpPushUpdater pushUpdater = new MrpPushUpdater(metadata, psm, core.stateDispatcher(), core.loop());
        MrpAudio audio = new MrpAudio(protocol, core.stateDispatcher());
        MrpFeatures features = new MrpFeatures(psm, audio);

        Map<Class<?>, Object> interfaces = Map.of(RemoteControl.class, remoteControl, Metadata.class, metadata,
                Power.class, power, PushUpdater.class, pushUpdater, Features.class, features, Audio.class, audio);

        java.util.function.Supplier<CompletableFuture<Boolean>> connect = () -> protocol.start().thenApply(v -> {
            if (requiresHeartbeat) {
                protocol.enableHeartbeat();
            }
            return true;
        });

        Runnable close = () -> {
            pushUpdater.stop();
            protocol.stop();
        };

        java.util.function.Supplier<Map<String, Object>> deviceInfoSupplier = () -> {
            Map<String, Object> devinfo = deviceInfo(MEDIAREMOTE_SERVICE, core.service().properties());
            // Extract build number from DEVICE_INFO_MESSAGE from device
            protocol.deviceInfo().ifPresent(message -> {
                DeviceInfoMessage inner = (DeviceInfoMessage) MrpExtensions.extractInner(message);
                devinfo.put(KEY_BUILD_NUMBER, inner.getSystemBuildVersion());
                @Nullable
                String version = lookupVersion(inner.getSystemBuildVersion());
                if (version != null) {
                    devinfo.put(KEY_VERSION, version);
                }
                if (!inner.getModelID().isEmpty()) {
                    devinfo.put(KEY_RAW_MODEL, inner.getModelID());
                    devinfo.put(KEY_MODEL, lookupModel(inner.getModelID()));
                }
            });
            return devinfo;
        };

        // Features managed by this protocol
        Set<FeatureName> features_ = EnumSet.of(FeatureName.Artwork, FeatureName.VolumeDown, FeatureName.VolumeUp,
                FeatureName.SetVolume, FeatureName.Volume, FeatureName.App);
        features_.addAll(MrpFeatures.FEATURES_SUPPORTED);
        features_.addAll(MrpFeatures.FEATURE_COMMAND_MAP.keySet());
        features_.addAll(MrpFeatures.FIELD_FEATURES.keySet());

        return new SetupData(Protocol.MRP, connect, close, deviceInfoSupplier, interfaces, features_);
    }

    /**
     * Returns a pairing handler for the protocol.
     *
     * @param core core context
     * @return pairing handler
     */
    public static PairingHandler pair(Core core) {
        return new MrpPairingHandler(core.address(), core.service(), core.settings().info(), core.runtime());
    }

    /**
     * Result of parsing a discovered MRP mDNS service.
     *
     * @param name device name
     * @param service the parsed service
     */
    public record ScanResult(String name, Service service) {
    }

    /**
     * Parses and returns a new MRP service from mDNS data. The service is disabled when
     * tvOS 15 or later is detected, since MRP no longer works there.
     *
     * @param port announced port
     * @param properties mDNS service properties
     * @return device name and service
     */
    public static ScanResult mrpServiceHandler(int port, Map<String, String> properties) {
        Map<String, String> props = caseInsensitive(properties);
        boolean enabled = true;

        // Disable this service if tvOS version is >= 15 as it doesn't work anymore
        String build = props.getOrDefault("SystemBuildVersion", "");
        Matcher match = BUILD_BASE_PATTERN.matcher(build);
        if (match.find() && Integer.parseInt(match.group(1)) >= 19) {
            LOGGER.debug("Disabling MRP service since tvOS >= 15");
            enabled = false;
        }

        String name = props.getOrDefault("Name", "Unknown");
        // Unique id for _mediaremotetv._tcp.local
        String uniqueId = props.get("UniqueIdentifier");
        Service service = new Service(uniqueId, Protocol.MRP, port, properties, null, null, false,
                PairingRequirement.Unsupported, enabled);
        return new ScanResult(name, service);
    }

    /**
     * Returns device information from Zeroconf properties.
     *
     * @param serviceType Zeroconf service type
     * @param properties Zeroconf service properties
     * @return device information map keyed by the {@code KEY_*} constants
     */
    public static Map<String, Object> deviceInfo(String serviceType, Map<String, String> properties) {
        Map<String, String> props = caseInsensitive(properties);
        Map<String, Object> devinfo = new LinkedHashMap<>();
        String build = props.get("systembuildversion");
        if (build != null) {
            devinfo.put(KEY_BUILD_NUMBER, build);
            @Nullable
            String version = lookupVersion(build);
            if (version != null) {
                devinfo.put(KEY_VERSION, version);
            }
        }
        String mac = props.get("macaddress");
        if (mac != null) {
            devinfo.put(KEY_MAC, mac);
        }

        // MRP has only been seen on Apple TV and HomePod, which both run tvOS, so this
        // is an educated guess
        devinfo.put(KEY_OPERATING_SYSTEM, OperatingSystem.TvOS);
        return devinfo;
    }

    /**
     * Updates a service with pairing requirement information. Pairing has never been
     * enforced by MRP, but it is possible to pair when {@code allowpairing} is
     * {@code yes}.
     *
     * @param service the MRP service to update
     */
    public static void serviceInfo(Service service) {
        Map<String, String> props = caseInsensitive(service.properties());
        if (!service.enabled()) {
            service.setPairing(PairingRequirement.NotNeeded);
        } else if ("yes".equalsIgnoreCase(props.getOrDefault("allowpairing", "no"))) {
            service.setPairing(PairingRequirement.Optional);
        } else {
            service.setPairing(PairingRequirement.Disabled);
        }
    }

    /**
     * Looks up an operating system version from a build number.
     *
     * @param build build number, e.g. {@code 18M60}
     * @return version string or {@code null} when unknown
     */
    public static @Nullable String lookupVersion(String build) {
        if (build.isEmpty()) {
            return null;
        }
        String version = VERSION_LIST.get(build);
        if (version != null) {
            return version;
        }
        Matcher match = BUILD_BASE_PATTERN.matcher(build);
        if (match.find()) {
            // 17A123 corresponds to tvOS 13.x, 16A123 to tvOS 12.x and so on
            int base = Integer.parseInt(match.group(1));
            return (base - 4) + ".x";
        }
        return null;
    }

    /**
     * Looks up a device model from a model identifier.
     *
     * @param identifier model identifier, e.g. {@code AppleTV6,2}
     * @return device model, {@link DeviceModel#Unknown} when unknown
     */
    public static DeviceModel lookupModel(String identifier) {
        return MODEL_LIST.getOrDefault(identifier == null ? "" : identifier, DeviceModel.Unknown);
    }

    private static Map<String, String> caseInsensitive(Map<String, String> properties) {
        return new CaseInsensitiveMap<>(properties);
    }
}
