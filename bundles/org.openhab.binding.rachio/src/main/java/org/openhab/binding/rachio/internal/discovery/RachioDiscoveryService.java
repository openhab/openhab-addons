/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.discovery;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.rachio.internal.RachioConfiguration;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RachioDiscoveryService} discovers all devices/zones reported by the Rachio Cloud. This requires the api
 * key to get access to the cloud data.
 *
 * @author Markus Michels - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "binding.rachio")
@NonNullByDefault
public class RachioDiscoveryService extends AbstractDiscoveryService {
    private final Logger        logger        = LoggerFactory.getLogger(RachioDiscoveryService.class);
    private RachioConfiguration bindingConfig = new RachioConfiguration();
    private boolean             scanning      = false;

    @Nullable
    private RachioApi           rachioApi;

    @Nullable
    private RachioBridgeHandler cloudHandler;

    /**
     * Activate the bundle: save properties
     *
     * @param componentContext
     * @param configProperties set of properties from cfg (use same names as in thing config)
     */
    @Override
    @Activate
    protected void activate(@Nullable Map<String, @Nullable Object> configProperties) {
        logger.debug("Rachio: Activate HandlerFactory, configurarion (services/binding." + BINDING_ID + ".cfg):");
        bindingConfig.updateConfig(configProperties);
    }

    public RachioDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, BINDING_DISCOVERY_TIMEOUT, true);
        String uids = SUPPORTED_THING_TYPES_UIDS.toString();
        logger.debug("Rachio: thing types: {} registered.", uids);
    }

    public void setCloudHandler(final RachioBridgeHandler cloudHandler) {
        Validate.notNull(cloudHandler, "Invalid RachioCloudHandler");
        this.cloudHandler = cloudHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting background discovery for new Rachio controllers");
        startScan();
    }

    @SuppressWarnings("null")
    @Override
    protected synchronized void startScan() {
        try {
            synchronized (this) {
                if (scanning) {
                    logger.debug("RachioDiscovery: Already discoverying");
                    return;
                }
                scanning = true;
            }

            logger.debug("Starting scan for new Rachio controllers");
            @Nullable
            HashMap<String, RachioDevice> deviceList = null;
            ThingUID bridgeUID;
            Validate.notNull(rachioApi);
            if (cloudHandler == null) {
                String apikey = bindingConfig.apikey;
                if (apikey.equals("")) {
                    logger.debug("RachioDiscovery: API not yet initialized");
                    return;
                }
                bridgeUID = new ThingUID(BINDING_ID, "cloud", apikey);
                rachioApi = new RachioApi("");
                rachioApi.initialize(apikey, bridgeUID);
                deviceList = rachioApi.getDevices();
                if (deviceList != null) {
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    Map<String, Object> bridgeProp = (Map) fillProperties(apikey);
                    DiscoveryResult bridgeResult = DiscoveryResultBuilder.create(bridgeUID).withProperties(bridgeProp)
                            .withBridge(bridgeUID).withLabel("Rachio Cloud").build();
                    thingDiscovered(bridgeResult);
                }
            } else {
                deviceList = cloudHandler.getDevices();
                bridgeUID = cloudHandler.getThing().getUID();
            }
            if (deviceList == null) {
                logger.debug("RachioDiscovery: Rachio Cloud access not initialized yet!");
                return;
            }
            logger.debug("RachioDiscovery: Found {} devices.", deviceList.size());
            for (HashMap.Entry<String, RachioDevice> de : deviceList.entrySet()) {
                RachioDevice dev = de.getValue();
                logger.debug("RachioDiscovery: Check Rachio device with ID '{}'", dev.id);

                // register thing if it not already exists
                ThingUID devThingUID = new ThingUID(THING_TYPE_DEVICE, bridgeUID, dev.getThingID());
                dev.setUID(bridgeUID, devThingUID);
                if ((cloudHandler == null) || (cloudHandler.getThingByUID(devThingUID) == null)) {
                    logger.info("RachioDiscovery: New Rachio device discovered: '{}' (id {}), S/N={}, MAC={}", dev.name,
                            dev.id, dev.serialNumber, dev.macAddress);
                    logger.debug("  latitude={}, longitude={}", dev.latitude, dev.longitude);
                    logger.info("   device status={}, paused/sleep={}, on={}", dev.status, dev.getSleepMode(),
                            dev.getEnabled());
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    Map<String, Object> properties = (Map) dev.fillProperties();
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(devThingUID)
                            .withProperties(properties).withBridge(bridgeUID).withLabel(dev.getThingName()).build();
                    thingDiscovered(discoveryResult);
                } // if (cloudHandler.getThingByUID(dev_thingUID) == null)

                HashMap<String, RachioZone> zoneList = dev.getZones();
                logger.info("RachioDiscovery: Found {} zones for this device.", zoneList.size());
                for (HashMap.Entry<String, RachioZone> ze : zoneList.entrySet()) {
                    RachioZone zone = ze.getValue();
                    logger.debug("RachioDiscovery: Checking zone with ID '{}'", zone.id);

                    // register thing if it not already exists
                    ThingUID zoneThingUID = new ThingUID(THING_TYPE_ZONE, bridgeUID, zone.getThingID());
                    zone.setUID(devThingUID, zoneThingUID);
                    if ((cloudHandler == null) || (cloudHandler.getThingByUID(zoneThingUID) == null)) {
                        logger.info("RachioDiscovery: Zone#{} '{}' (id={}) added, enabled={}", zone.zoneNumber,
                                zone.name, zone.id, zone.getEnabled());

                        if (zone.getEnabled() == OnOffType.ON) {
                            @SuppressWarnings({ "unchecked", "rawtypes" })
                            Map<String, Object> zproperties = (Map) zone.fillProperties();
                            DiscoveryResult zoneDiscoveryResult = DiscoveryResultBuilder.create(zoneThingUID)
                                    .withProperties(zproperties).withBridge(bridgeUID)
                                    .withLabel(dev.name + "[" + zone.zoneNumber + "]: " + zone.name).build();
                            thingDiscovered(zoneDiscoveryResult);
                        } else {
                            logger.info("RachioDiscovery: Zone#{} '{}' is disabled, skip thing creation", zone.name,
                                    zone.id);
                        }
                    }
                }
            }
            logger.info("{}  Rachio controller initialized.", deviceList.size());

            stopScan();
        } catch (RachioApiException e) {
            logger.error("RachioDiscovery: Unexpected error while discovering Rachio devices/zones: {}", e.toString());
        } catch (RuntimeException e) {
            logger.error("RachioDiscovery: Unexpected error while discovering Rachio devices/zones: {}",
                    e.getMessage());
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        scanning = false;
        logger.debug("RachioDiscervery: discovery done.");
    }

    private Map<String, String> fillProperties(String id) {
        Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, BINDING_VENDOR);
        properties.put(PROPERTY_APIKEY, id);
        properties.put(PROPERTY_EXT_ID, id);
        properties.put(PROPERTY_NAME, "Rachio Cloud Connector");
        return properties;
    }

}
