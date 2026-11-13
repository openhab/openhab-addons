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
package org.openhab.binding.atv.internal.client.protocols.companion;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.PairingHandler;
import org.openhab.binding.atv.internal.client.capability.Apps;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.Features;
import org.openhab.binding.atv.internal.client.capability.Keyboard;
import org.openhab.binding.atv.internal.client.capability.Power;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.capability.TouchGestures;
import org.openhab.binding.atv.internal.client.capability.UserAccounts;
import org.openhab.binding.atv.internal.client.conf.Service;
import org.openhab.binding.atv.internal.client.core.Core;
import org.openhab.binding.atv.internal.client.core.ProtocolModule;
import org.openhab.binding.atv.internal.client.core.SetupData;
import org.openhab.binding.atv.internal.client.dto.DeviceModel;
import org.openhab.binding.atv.internal.client.dto.PairingRequirement;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Companion protocol module entry point: {@link #setup(Core)} yields the {@link SetupData}
 * consumed by the device relay, {@link #pair(Core, String)} creates a pairing handler, and
 * {@link #scanServiceTypes()}/{@link #deviceInfo(Map)}/{@link #serviceInfo(Service)} provide
 * the scan metadata hooks.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CompanionProtocolModule {

    /** Zeroconf service type used by Companion. */
    public static final String SERVICE_TYPE = "_companion-link._tcp.local";

    /**
     * Observed values of rpfl (zeroconf):
     * 0x62792 → all on the same network (Unsupported/Mandatory),
     * 0x627B6/0xB67A2 → only devices in same home (Disabled).
     */
    public static final int PAIRING_DISABLED_MASK = 0x04;

    /**
     * Masking this bit of rpfl tells if pairing with PIN is supported; observed pairable
     * values are 0x367A2/0x36782 vs. non-pairable 0x20000/0x627B2/0x62792/0x30000.
     */
    public static final int PAIRING_WITH_PIN_SUPPORTED_MASK = 0x4000;

    /** {@code rpmd} model identifier lookup. */
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

    /** Device-info key for the raw model string. */
    public static final String RAW_MODEL = "raw_model";
    /** Device-info key for the parsed {@link DeviceModel}. */
    public static final String MODEL = "model";

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanionProtocolModule.class);

    /**
     * Singleton implementing the {@link ProtocolModule} contract by delegating to the
     * static entry points of this class.
     */
    public static final ProtocolModule MODULE = new ProtocolModule() {

        @Override
        public Protocol protocol() {
            return Protocol.Companion;
        }

        @Override
        public Set<String> scanServiceTypes() {
            return CompanionProtocolModule.scanServiceTypes();
        }

        @Override
        public Map<String, Object> deviceInfo(String serviceType, Map<String, String> properties) {
            return CompanionProtocolModule.deviceInfo(properties);
        }

        @Override
        public void serviceInfo(Service service) {
            CompanionProtocolModule.serviceInfo(service);
        }

        @Override
        public Set<SetupData> setup(Core core) {
            return CompanionProtocolModule.setup(core);
        }

        @Override
        public PairingHandler pair(Core core, Map<String, Object> options) {
            return CompanionProtocolModule.pair(core, (String) options.get("name"));
        }
    };

    private CompanionProtocolModule() {
    }

    /**
     * Zeroconf service types handled by this protocol.
     *
     * @return the service types
     */
    public static Set<String> scanServiceTypes() {
        return Set.of(SERVICE_TYPE);
    }

    /**
     * Returns device information from zeroconf properties.
     *
     * @param properties zeroconf service properties
     * @return device information fields ({@link #RAW_MODEL} and possibly {@link #MODEL})
     */
    public static Map<String, Object> deviceInfo(Map<String, String> properties) {
        Map<String, Object> devinfo = new LinkedHashMap<>();
        String rawModel = properties.get("rpmd");
        if (rawModel != null) {
            devinfo.put(RAW_MODEL, rawModel);
            DeviceModel model = MODEL_LIST.getOrDefault(rawModel, DeviceModel.Unknown);
            if (model != DeviceModel.Unknown) {
                devinfo.put(MODEL, model);
            }
        }
        return devinfo;
    }

    /**
     * Updates the service with the pairing requirement decoded from the {@code rpfl} flags.
     *
     * @param service the Companion service to update
     */
    public static void serviceInfo(Service service) {
        int flags = Integer.decode(service.properties().getOrDefault("rpfl", "0x0"));
        if ((flags & PAIRING_DISABLED_MASK) != 0) {
            service.setPairing(PairingRequirement.Disabled);
        } else if ((flags & PAIRING_WITH_PIN_SUPPORTED_MASK) != 0) {
            service.setPairing(PairingRequirement.Mandatory);
        } else {
            service.setPairing(PairingRequirement.Unsupported);
        }
    }

    /**
     * Sets up a new Companion service.
     *
     * <p>
     * Companion does not work without credentials, so an empty set is returned when the
     * service has none.
     *
     * @param core protocol context
     * @return setup data for the relay, or an empty set when credentials are missing
     */
    public static Set<SetupData> setup(Core core) {
        if (core.service().credentials().isEmpty()) {
            LOGGER.debug("Not adding Companion as credentials are missing");
            return Set.of();
        }

        CompanionApi api = new CompanionApi(core);
        CompanionPower power = new CompanionPower(api, core);

        Map<Class<?>, Object> interfaces = new LinkedHashMap<>();
        interfaces.put(Apps.class, new CompanionApps(api));
        interfaces.put(UserAccounts.class, new CompanionUserAccounts(api));
        interfaces.put(Features.class, new CompanionFeatures(api, power));
        interfaces.put(Power.class, power);
        interfaces.put(RemoteControl.class, new CompanionRemoteControl(api));
        interfaces.put(Audio.class, new CompanionAudio(api, core));
        interfaces.put(Keyboard.class, new CompanionKeyboard(api, core));
        interfaces.put(TouchGestures.class, new CompanionTouchGestures(api));

        return Set.of(new SetupData(Protocol.Companion,
                () -> api.connect().thenCompose(unused -> power.initialize()).thenApply(unused -> true),
                // Block on disconnect here instead of returning a future.
                () -> CompanionApi.join(api.disconnect()), () -> deviceInfo(core.service().properties()), interfaces,
                CompanionFeatures.SUPPORTED_FEATURES));
    }

    /**
     * Returns a pairing handler for the protocol.
     *
     * @param core protocol context
     * @param name display name registered on the device, or {@code null} for the default
     * @return pairing handler
     */
    public static PairingHandler pair(Core core, @Nullable String name) {
        return new CompanionPairingHandler(core, name);
    }
}
