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
package org.openhab.binding.miele.internal.discovery;

import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miele.internal.FullyQualifiedApplianceIdentifier;
import org.openhab.binding.miele.internal.api.dto.HomeDevice;
import org.openhab.binding.miele.internal.handler.DiscoveryListener;
import org.openhab.binding.miele.internal.handler.MieleApplianceHandler;
import org.openhab.binding.miele.internal.handler.MieleBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MieleApplianceDiscoveryService} tracks appliances that are
 * associated with the Miele@home gateway
 *
 * @author Karel Goderis - Initial contribution
 * @author Martin Lepsy - Added protocol information in order to support WiFi devices
 * @author Jacob Laursen - Fixed multicast and protocol support (ZigBee/LAN)
 */
@Component(scope = ServiceScope.PROTOTYPE, service = MieleApplianceDiscoveryService.class)
@NonNullByDefault
public class MieleApplianceDiscoveryService extends AbstractThingHandlerDiscoveryService<MieleBridgeHandler>
        implements DiscoveryListener {

    private final Logger logger = LoggerFactory.getLogger(MieleApplianceDiscoveryService.class);

    private static final int SEARCH_TIME_SECONDS = 60;

    public MieleApplianceDiscoveryService() {
        super(MieleBridgeHandler.class, MieleApplianceHandler.SUPPORTED_THING_TYPES, SEARCH_TIME_SECONDS, false);
    }

    @Reference(unbind = "-")
    public void bindTranslationProvider(TranslationProvider translationProvider) {
        this.i18nProvider = translationProvider;
    }

    @Reference(unbind = "-")
    public void bindLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    @Override
    public void initialize() {
        thingHandler.registerDiscoveryListener(this);
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now().toEpochMilli());
        thingHandler.unregisterDiscoveryListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return MieleApplianceHandler.SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {
        List<HomeDevice> appliances = thingHandler.getHomeDevicesEmptyOnFailure();
        for (HomeDevice appliance : appliances) {
            onApplianceAddedInternal(appliance);
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void onApplianceAdded(HomeDevice appliance) {
        onApplianceAddedInternal(appliance);
    }

    private void onApplianceAddedInternal(HomeDevice appliance) {
        ThingUID thingUID = getThingUID(appliance);
        if (thingUID == null) {
            logger.debug("Discovered an unsupported appliance of vendor '{}' with id {}", appliance.Vendor,
                    appliance.UID);
            return;
        }
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        Map<String, Object> properties = new HashMap<>(9);

        FullyQualifiedApplianceIdentifier applianceIdentifier = appliance.getApplianceIdentifier();
        String vendor = appliance.Vendor;
        if (vendor != null) {
            properties.put(Thing.PROPERTY_VENDOR, vendor);
        }
        properties.put(Thing.PROPERTY_MODEL_ID, appliance.getApplianceModel());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, appliance.getSerialNumber());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, appliance.getFirmwareVersion());
        String protocolAdapterName = appliance.ProtocolAdapterName;
        if (protocolAdapterName != null) {
            properties.put(PROPERTY_PROTOCOL_ADAPTER, protocolAdapterName);
        }
        properties.put(APPLIANCE_ID, applianceIdentifier.getApplianceId());
        String deviceClass = appliance.getDeviceClass();
        if (deviceClass != null) {
            properties.put(PROPERTY_DEVICE_CLASS, deviceClass);
        }
        String connectionType = appliance.getConnectionType();
        if (connectionType != null) {
            properties.put(PROPERTY_CONNECTION_TYPE, connectionType);
        }
        String connectionBaudRate = appliance.getConnectionBaudRate();
        if (connectionBaudRate != null) {
            properties.put(PROPERTY_CONNECTION_BAUD_RATE, connectionBaudRate);
        }

        String label = deviceClass != null
                ? "@text/discovery." + getThingTypeUidFromDeviceClass(deviceClass).getId() + ".label [\""
                        + appliance.getApplianceModel() + "\"]"
                : appliance.getApplianceModel();

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(bridgeUID).withLabel(label).withRepresentationProperty(APPLIANCE_ID).build();

        thingDiscovered(discoveryResult);
    }

    @Override
    public void onApplianceRemoved(HomeDevice appliance) {
        ThingUID thingUID = getThingUID(appliance);

        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }

    private @Nullable ThingUID getThingUID(HomeDevice appliance) {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        String deviceClass = appliance.getDeviceClass();
        if (deviceClass == null) {
            return null;
        }

        ThingTypeUID thingTypeUID = getThingTypeUidFromDeviceClass(deviceClass);

        if (getSupportedThingTypes().contains(thingTypeUID)) {
            return new ThingUID(thingTypeUID, bridgeUID, appliance.getApplianceIdentifier().getId());
        } else {
            return null;
        }
    }

    private ThingTypeUID getThingTypeUidFromDeviceClass(String deviceClass) {
        /*
         * Coffee machine CVA 6805 is reported as CoffeeSystem, but thing type is
         * coffeemachine. At least until it is known if any models are actually reported
         * as CoffeeMachine, we need this special mapping.
         */
        if (MIELE_DEVICE_CLASS_COFFEE_SYSTEM.equals(deviceClass)) {
            return THING_TYPE_COFFEEMACHINE;
        }

        String thingTypeId = deviceClass.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();

        return new ThingTypeUID(BINDING_ID, thingTypeId);
    }
}
