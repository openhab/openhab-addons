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
package org.openhab.binding.velux.discovery;

import static org.openhab.binding.velux.VeluxBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.velux.VeluxBindingConstants;
import org.openhab.binding.velux.VeluxBindingProperties;
import org.openhab.binding.velux.handler.VeluxBridgeHandler;
import org.openhab.binding.velux.internal.utils.ManifestInformation;
import org.openhab.binding.velux.things.VeluxProduct;
import org.openhab.binding.velux.things.VeluxProductSerialNo;
import org.openhab.binding.velux.things.VeluxScene;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxDiscoveryService} is responsible for discovering scenes on
 * the current Velux Bridge.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
// ToDo: check whether an immediate activation is preferable.
// Might be activated by:
// @Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.velux")
//
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.velux")
public class VeluxDiscoveryService extends AbstractDiscoveryService implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(VeluxDiscoveryService.class);

    private static final int DISCOVER_TIMEOUT_SECONDS = 300;

    private static @Nullable VeluxBridgeHandler bridgeHandler = null;

    /**
     * Initializes the {@link VeluxDiscoveryService} without a {@link VeluxBridgeHandler}.
     * <P>
     * This is the main entry point for the OSGI DiscoveryService.
     *
     */
    public VeluxDiscoveryService() {
        super(VeluxBindingConstants.SUPPORTED_THINGS_ITEMS, DISCOVER_TIMEOUT_SECONDS);
        logger.trace("VeluxDiscoveryService(without Bridge) just initialized.");
    }

    /**
     * Initializes the {@link VeluxDiscoveryService} with a reference to the well-prepared environment with a
     * {@link VeluxBridgeHandler}.
     *
     * @param bridge Initialized Velux bridge handler.
     */
    public VeluxDiscoveryService(VeluxBridgeHandler bridge) {
        super(VeluxBindingConstants.SUPPORTED_THINGS_ITEMS, DISCOVER_TIMEOUT_SECONDS);
        logger.trace("VeluxDiscoveryService(bridge={}) just initialized.", bridge);

        synchronized (this) {
            if (bridgeHandler == null) {
                logger.trace("VeluxDiscoveryService(): registering bridge {} for lateron use for Discovery.", bridge);
            } else if (!bridge.equals(bridgeHandler)) {
                logger.trace("VeluxDiscoveryService(): replacing already registered bridge {} by {}.", bridgeHandler,
                        bridge);
            }
            bridgeHandler = bridge;
        }
    }

    @Override
    public void deactivate() {
        logger.trace("deactivate() called.");
        super.deactivate();
    }

    @Override
    protected synchronized void startScan() {
        logger.trace("startScan() called.");

        if (bridgeHandler == null) {
            logger.debug("startScan(): creating a thing of type binding.");
            ThingUID thingUID = new ThingUID(THING_TYPE_BINDING, "org_openhab_binding_velux");
            // @formatter:off
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withProperty(VeluxBindingProperties.PROPERTY_BINDING_BUNDLEVERSION, ManifestInformation.getBundleVersion())
                    // TODO: should work with:
                    // .withLabel("@text/binding.velux.name")
                    .withLabel("Velux Binding")
                    .build();
            // @formatter:on
            logger.debug("startScan(): registering new thing {}.", discoveryResult);
            thingDiscovered(discoveryResult);
            logger.debug("startScan(): VeluxDiscoveryService cannot proceed due to missing Velux bridge.");
        } else {
            logger.debug("startScan(): Starting Velux discovery scan for scenes and actuators.");
            discoverScenes();
            discoverProducts();
        }
        logger.trace("startScan() done.");
    }

    @Override
    public synchronized void stopScan() {
        logger.trace("stopScan() called.");
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
        logger.trace("stopScan() done.");
    }

    @Override
    public void run() {
        logger.trace("run() called.");
    }

    /**
     * Discover the gateway-defined scenes.
     */
    private void discoverScenes() {
        logger.trace("discoverScenes() called.");
        if (bridgeHandler == null) {
            logger.debug("discoverScenes(): VeluxDiscoveryService.bridgeHandler not initialized, aborting discovery.");
            return;
        }
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        logger.debug("discoverScenes(): discovering all scenes.");
        for (VeluxScene scene : bridgeHandler.existingScenes().values()) {
            String sceneName = scene.getName().toString();
            logger.trace("discoverScenes(): found scene {}.", sceneName);

            String label = sceneName.replaceAll("\\P{Alnum}", "_");
            logger.trace("discoverScenes(): using label {}.", label);

            ThingUID thingUID = new ThingUID(THING_TYPE_VELUX_SCENE, bridgeUID, label);
            @SuppressWarnings("deprecation")
            ThingTypeUID thingTypeUID = thingUID.getThingTypeUID();

            // @formatter:off
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withThingType(thingTypeUID)
                    .withProperty(VeluxBindingProperties.PROPERTY_SCENE_NAME, sceneName)
                    .withRepresentationProperty(VeluxBindingProperties.PROPERTY_SCENE_NAME)
                    .withBridge(bridgeUID)
                    .withLabel(label)
                    .build();
            // @formatter:on
            logger.debug("discoverScenes(): registering new thing {}.", discoveryResult);
            thingDiscovered(discoveryResult);
        }
        logger.trace("discoverScenes() finished.");
    }

    /**
     * Discover the gateway-defined products/actuators.
     */
    private void discoverProducts() {
        logger.trace("discoverProducts() called.");
        if (bridgeHandler == null) {
            logger.debug("discoverScenes() VeluxDiscoveryService.bridgeHandler not initialized, aborting discovery.");
            return;
        }
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        logger.debug("discoverProducts(): discovering all actuators.");
        for (VeluxProduct product : bridgeHandler.existingProducts().values()) {
            String serialNumber = product.getSerialNumber();
            String actuatorName = product.getProductName().toString();
            logger.trace("discoverProducts() found actuator {} (name {}).", serialNumber, actuatorName);
            String identifier;
            if (serialNumber.equals(VeluxProductSerialNo.UNKNOWN)) {
                identifier = actuatorName;
            } else {
                identifier = serialNumber;
            }
            String label = actuatorName.replaceAll("\\P{Alnum}", "_");
            logger.trace("discoverProducts(): using label {}.", label);
            ThingUID thingUID = null;
            boolean isInverted = false;
            logger.trace("discoverProducts() dealing with {} (type {}).", product, product.getProductType());
            switch (product.getProductType()) {
                case SLIDER_WINDOW:
                    logger.trace("discoverProducts(): creating window.");
                    thingUID = new ThingUID(THING_TYPE_VELUX_WINDOW, bridgeUID, label);
                    isInverted = true;
                    break;

                case SLIDER_SHUTTER:
                    logger.trace("discoverProducts(): creating rollershutter.");
                    thingUID = new ThingUID(THING_TYPE_VELUX_ROLLERSHUTTER, bridgeUID, label);
                    break;

                default:
                    logger.trace("discoverProducts(): creating actuator.");
                    thingUID = new ThingUID(THING_TYPE_VELUX_ACTUATOR, bridgeUID, label);
            }
            @SuppressWarnings("deprecation")
            ThingTypeUID thingTypeUID = thingUID.getThingTypeUID();

            // @formatter:off
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withThingType(thingTypeUID)
                    .withProperty(VeluxBindingProperties.PROPERTY_ACTUATOR_SERIALNUMBER, identifier)
                    .withProperty(VeluxBindingProperties.PROPERTY_ACTUATOR_NAME, actuatorName)
                    .withProperty(VeluxBindingProperties.PROPERTY_ACTUATOR_INVERTED, isInverted)
                    .withRepresentationProperty(VeluxBindingProperties.PROPERTY_ACTUATOR_SERIALNUMBER)
                    .withBridge(bridgeUID)
                    .withLabel(actuatorName)
                    .build();
            // @formatter:on
            logger.debug("discoverProducts(): registering new thing {}.", discoveryResult);
            thingDiscovered(discoveryResult);
        }
        logger.trace("discoverProducts() finished.");
    }

}
