/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal.discovery;

import static org.openhab.binding.velux.internal.VeluxBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.VeluxBindingProperties;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProductSerialNo;
import org.openhab.binding.velux.internal.things.VeluxScene;
import org.openhab.binding.velux.internal.utils.Localization;
import org.openhab.binding.velux.internal.utils.ManifestInformation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxDiscoveryService} is responsible for discovering actuators and scenes on the current Velux Bridge.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
//
// To-be-discussed: check whether an immediate activation is preferable.
// Might be activated by:
// @Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.velux")
//
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.velux")
public class VeluxDiscoveryService extends AbstractDiscoveryService implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(VeluxDiscoveryService.class);

    // Class internal

    private static final int DISCOVER_TIMEOUT_SECONDS = 300;

    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;
    private Localization localization = Localization.UNKNOWN;
    private static @Nullable VeluxBridgeHandler bridgeHandler = null;

    // Private

    private void updateLocalization() {
        if (localization == Localization.UNKNOWN && localeProvider != null && i18nProvider != null) {
            logger.trace("updateLocalization(): creating Localization based on locale={},translation={}).",
                    localeProvider, i18nProvider);
            localization = new Localization(localeProvider, i18nProvider);
        }
    }

    /**
     * Constructor
     * <P>
     * Initializes the {@link VeluxDiscoveryService} without any further information.
     */
    public VeluxDiscoveryService() {
        super(VeluxBindingConstants.SUPPORTED_THINGS_ITEMS, DISCOVER_TIMEOUT_SECONDS);
        logger.trace("VeluxDiscoveryService(without Bridge) just initialized.");
    }

    @Reference
    protected void setLocaleProvider(final LocaleProvider givenLocaleProvider) {
        logger.trace("setLocaleProvider(): provided locale={}.", givenLocaleProvider);
        localeProvider = givenLocaleProvider;
        updateLocalization();
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider givenI18nProvider) {
        logger.trace("setTranslationProvider(): provided translation={}.", givenI18nProvider);
        i18nProvider = givenI18nProvider;
        updateLocalization();
    }

    /**
     * Constructor
     * <P>
     * Initializes the {@link VeluxDiscoveryService} with a reference to the well-prepared environment with a
     * {@link VeluxBridgeHandler}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @param localizationHandler Initialized localization handler.
     */
    public VeluxDiscoveryService(VeluxBridgeHandler bridge, Localization localizationHandler) {
        super(VeluxBindingConstants.SUPPORTED_THINGS_ITEMS, DISCOVER_TIMEOUT_SECONDS);
        logger.trace("VeluxDiscoveryService(bridge={},locale={},i18n={}) just initialized.", bridge, localeProvider,
                i18nProvider);

        if (bridgeHandler == null) {
            logger.trace("VeluxDiscoveryService(): registering bridge {} for lateron use for Discovery.", bridge);
        } else if (!bridge.equals(bridgeHandler)) {
            logger.trace("VeluxDiscoveryService(): replacing already registered bridge {} by {}.", bridgeHandler,
                    bridge);
        }
        bridgeHandler = bridge;
        localization = localizationHandler;
    }

    /**
     * Constructor
     * <P>
     * Initializes the {@link VeluxDiscoveryService} with a reference to the well-prepared environment with a
     * {@link VeluxBridgeHandler}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @param locationProvider Provider for a location.
     * @param localeProvider Provider for a locale.
     * @param i18nProvider Provider for the internationalization.
     */
    public VeluxDiscoveryService(VeluxBridgeHandler bridge, LocationProvider locationProvider,
            LocaleProvider localeProvider, TranslationProvider i18nProvider) {
        this(bridge, new Localization(localeProvider, i18nProvider));
        logger.trace("VeluxDiscoveryService(bridge={},locale={},i18n={}) finished.", bridge, localeProvider,
                i18nProvider);
    }

    @Override
    public void deactivate() {
        logger.trace("deactivate() called.");
        super.deactivate();
    }

    @Override
    protected synchronized void startScan() {
        logger.trace("startScan() called.");

        logger.debug("startScan(): creating a thing of type binding.");
        ThingUID thingUID = new ThingUID(THING_TYPE_BINDING, "org_openhab_binding_velux");
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withProperty(VeluxBindingProperties.PROPERTY_BINDING_BUNDLEVERSION,
                        ManifestInformation.getBundleVersion())
                .withLabel(localization.getText("discovery.velux.binding...label")).build();
        logger.debug("startScan(): registering new thing {}.", discoveryResult);
        thingDiscovered(discoveryResult);

        if (bridgeHandler == null) {
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
        // Just for avoidance of Potential null pointer access
        VeluxBridgeHandler bridgeHandlerX = bridgeHandler;
        if (bridgeHandlerX == null) {
            logger.debug("discoverScenes(): VeluxDiscoveryService.bridgeHandler not initialized, aborting discovery.");
            return;
        }
        ThingUID bridgeUID = bridgeHandlerX.getThing().getUID();
        logger.debug("discoverScenes(): discovering all scenes.");
        for (VeluxScene scene : bridgeHandlerX.existingScenes().values()) {
            String sceneName = scene.getName().toString();
            logger.trace("discoverScenes(): found scene {}.", sceneName);

            String label = sceneName.replaceAll("\\P{Alnum}", "_");
            logger.trace("discoverScenes(): using label {}.", label);

            ThingTypeUID thingTypeUID = THING_TYPE_VELUX_SCENE;
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, label);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperty(VeluxBindingProperties.PROPERTY_SCENE_NAME, sceneName)
                    .withRepresentationProperty(VeluxBindingProperties.PROPERTY_SCENE_NAME).withBridge(bridgeUID)
                    .withLabel(label).build();
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
        // Just for avoidance of Potential null pointer access
        VeluxBridgeHandler bridgeHandlerX = bridgeHandler;
        if (bridgeHandlerX == null) {
            logger.debug("discoverScenes() VeluxDiscoveryService.bridgeHandlerR not initialized, aborting discovery.");
            return;
        }
        ThingUID bridgeUID = bridgeHandlerX.getThing().getUID();
        logger.debug("discoverProducts(): discovering all actuators.");
        for (VeluxProduct product : bridgeHandlerX.existingProducts().values()) {
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
            ThingTypeUID thingTypeUID;
            boolean isInverted = false;
            logger.trace("discoverProducts() dealing with {} (type {}).", product, product.getProductType());
            switch (product.getProductType()) {
                case SLIDER_WINDOW:
                    logger.trace("discoverProducts(): creating window.");
                    thingTypeUID = THING_TYPE_VELUX_WINDOW;
                    isInverted = true;
                    break;

                case SLIDER_SHUTTER:
                    logger.trace("discoverProducts(): creating rollershutter.");
                    thingTypeUID = THING_TYPE_VELUX_ROLLERSHUTTER;
                    break;

                default:
                    logger.trace("discoverProducts(): creating actuator.");
                    thingTypeUID = THING_TYPE_VELUX_ACTUATOR;
            }
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, label);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperty(VeluxBindingProperties.CONFIG_ACTUATOR_SERIALNUMBER, identifier)
                    .withProperty(VeluxBindingProperties.PROPERTY_ACTUATOR_NAME, actuatorName)
                    .withProperty(VeluxBindingProperties.PROPERTY_ACTUATOR_INVERTED, isInverted)
                    .withRepresentationProperty(VeluxBindingProperties.CONFIG_ACTUATOR_SERIALNUMBER)
                    .withBridge(bridgeUID).withLabel(actuatorName).build();
            logger.debug("discoverProducts(): registering new thing {}.", discoveryResult);
            thingDiscovered(discoveryResult);
        }
        logger.trace("discoverProducts() finished.");
    }
}
