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
package org.openhab.binding.bmwcardata.internal.discovery;

import static org.openhab.binding.bmwcardata.internal.BMWCarDataBindingConstants.*;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataApi;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataException;
import org.openhab.binding.bmwcardata.internal.api.dto.BasicData;
import org.openhab.binding.bmwcardata.internal.api.dto.VehicleMapping;
import org.openhab.binding.bmwcardata.internal.handler.BMWCarDataAccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BMWCarDataVehicleDiscoveryService} discovers the vehicles mapped to a CarData account. As each scan
 * costs one request of the daily quota, scans run only on demand and after the authorization of an account
 * without vehicles.
 *
 * @author Michael Weger - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = BMWCarDataVehicleDiscoveryService.class)
@NonNullByDefault
public class BMWCarDataVehicleDiscoveryService extends AbstractThingHandlerDiscoveryService<BMWCarDataAccountHandler> {

    private static final int DISCOVERY_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(BMWCarDataVehicleDiscoveryService.class);

    public BMWCarDataVehicleDiscoveryService() {
        super(BMWCarDataAccountHandler.class, Set.of(THING_TYPE_VEHICLE), DISCOVERY_TIMEOUT_SECONDS, false);
    }

    // the providers are required to resolve the @text label of the discovery results
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
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        thingHandler.setDiscoveryService(null);
    }

    @Override
    protected void startScan() {
        discoverVehicles();
    }

    /**
     * Retrieves the vehicles of the account and adds the primary vehicles to the inbox. Secondary mappings do not
     * provide telematic data.
     */
    public void discoverVehicles() {
        List<VehicleMapping> mappings;
        try {
            mappings = thingHandler.fetchVehicleMappings();
        } catch (BMWCarDataException e) {
            logger.debug("Vehicle discovery failed: {}", e.getMessage());
            return;
        }
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        for (VehicleMapping mapping : mappings) {
            String vin = mapping.vin;
            if (vin == null || !BMWCarDataApi.isValidVin(vin)
                    || !VehicleMapping.MAPPING_TYPE_PRIMARY.equals(mapping.mappingType)) {
                continue;
            }
            @Nullable
            BasicData basicData = fetchBasicData(vin);
            thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_VEHICLE, bridgeUID, vin))
                    .withBridge(bridgeUID).withProperty(CONFIG_VIN, vin).withRepresentationProperty(CONFIG_VIN)
                    .withLabel(label(vin, basicData)).build());
        }
    }

    /**
     * @return the basic data of the vehicle, which the vehicle Thing reuses, or {@code null} if it is not available
     */
    private @Nullable BasicData fetchBasicData(String vin) {
        try {
            return thingHandler.fetchBasicData(vin);
        } catch (BMWCarDataException e) {
            logger.debug("Basic data for discovery not available: {}", e.getMessage());
            return null;
        }
    }

    private String label(String vin, @Nullable BasicData basicData) {
        String label = "";
        boolean telematicsCapable = true; // assume true, as the vehicle is primary and should be telematics capable
        if (basicData != null) {
            String brand = basicData.brand;
            if (brand != null && !brand.isBlank()) {
                label += brand;
            }
            String model = basicData.modelName;
            if (model != null && !model.isBlank()) {
                if (!label.isEmpty()) {
                    label += " ";
                }
                label += model;
            }
            telematicsCapable = Boolean.TRUE.equals(basicData.isTelematicsCapable);
        }
        if (!label.isEmpty()) {
            label += " (" + vin + ")";
        } else {
            label = vin;
        }
        if (!telematicsCapable) {
            logger.info("Discovered vehicle {} is not capable to send data", vin);
            label = "@text/discovery.vehicle.no-telematics.label [\"" + label + "\"]";
        }
        return label;
    }
}
