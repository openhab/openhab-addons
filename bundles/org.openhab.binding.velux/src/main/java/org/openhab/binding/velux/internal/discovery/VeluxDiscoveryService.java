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
package org.openhab.binding.velux.internal.discovery;

import static org.openhab.binding.velux.internal.VeluxBindingConstants.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.VeluxBindingProperties;
import org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProductSerialNo;
import org.openhab.binding.velux.internal.things.VeluxScene;
import org.openhab.binding.velux.internal.utils.Localization;
import org.openhab.binding.velux.internal.utils.ManifestInformation;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxDiscoveryService} is responsible for discovering actuators and scenes on the current Velux Bridge.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.velux")
public class VeluxDiscoveryService extends AbstractDiscoveryService implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(VeluxDiscoveryService.class);

    // Class internal

    private static final int DISCOVER_TIMEOUT_SECONDS = 60;

    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;
    private Localization localization = Localization.UNKNOWN;
    private final Set<VeluxBridgeHandler> bridgeHandlers = new HashSet<>();

    @Nullable
    private ScheduledFuture<?> backgroundTask = null;

    // Private

    private void updateLocalization() {
        if (Localization.UNKNOWN.equals(localization) && (localeProvider != null) && (i18nProvider != null)) {
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
        super(VeluxBindingConstants.DISCOVERABLE_THINGS, DISCOVER_TIMEOUT_SECONDS);
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
     * @param localizationHandler Initialized localization handler.
     */
    public VeluxDiscoveryService(Localization localizationHandler) {
        super(VeluxBindingConstants.DISCOVERABLE_THINGS, DISCOVER_TIMEOUT_SECONDS);
        logger.trace("VeluxDiscoveryService(locale={},i18n={}) just initialized.", localeProvider, i18nProvider);
        localization = localizationHandler;
    }

    /**
     * Constructor
     * <P>
     * Initializes the {@link VeluxDiscoveryService} with a reference to the well-prepared environment with a
     * {@link VeluxBridgeHandler}.
     *
     * @param locationProvider Provider for a location.
     * @param localeProvider Provider for a locale.
     * @param i18nProvider Provider for the internationalization.
     */
    public VeluxDiscoveryService(LocationProvider locationProvider, LocaleProvider localeProvider,
            TranslationProvider i18nProvider) {
        this(new Localization(localeProvider, i18nProvider));
        logger.trace("VeluxDiscoveryService(locale={},i18n={}) finished.", localeProvider, i18nProvider);
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
                .withRepresentationProperty(VeluxBindingProperties.PROPERTY_BINDING_BUNDLEVERSION)
                .withLabel(localization.getText("discovery.velux.binding...label")).build();
        logger.debug("startScan(): registering new thing {}.", discoveryResult);
        thingDiscovered(discoveryResult);

        scheduler.execute(() -> {
            discoverBridges();
        });

        if (bridgeHandlers.isEmpty()) {
            logger.debug("startScan(): VeluxDiscoveryService cannot proceed due to missing Velux bridge(s).");
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
        for (VeluxBridgeHandler bridgeHandlerX : bridgeHandlers) {
            ThingUID bridgeUID = bridgeHandlerX.getThing().getUID();
            logger.debug("discoverScenes(): discovering all scenes on bridge {}.", bridgeUID);
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
        }
        logger.trace("discoverScenes() finished.");
    }

    /**
     * Discover the gateway-defined products/actuators.
     */
    private void discoverProducts() {
        logger.trace("discoverProducts() called.");
        for (VeluxBridgeHandler bridgeHandlerX : bridgeHandlers) {
            ThingUID bridgeUID = bridgeHandlerX.getThing().getUID();
            logger.debug("discoverProducts(): discovering all actuators on bridge {}.", bridgeUID);
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
        }
        logger.trace("discoverProducts() finished.");
    }

    /**
     * Add a {@link VeluxBridgeHandler} to the {@link VeluxDiscoveryService}
     *
     * @param bridge Velux bridge handler.
     * @return true if the bridge was added, or false if it was already present
     */
    public boolean addBridge(VeluxBridgeHandler bridge) {
        if (!bridgeHandlers.contains(bridge)) {
            logger.trace("VeluxDiscoveryService(): registering bridge {} for discovery.", bridge);
            bridgeHandlers.add(bridge);
            return true;
        }
        logger.trace("VeluxDiscoveryService(): bridge {} already registered for discovery.", bridge);
        return false;
    }

    /**
     * Remove a {@link VeluxBridgeHandler} from the {@link VeluxDiscoveryService}
     *
     * @param bridge Velux bridge handler.
     * @return true if the bridge was removed, or false if it was not present
     */
    public boolean removeBridge(VeluxBridgeHandler bridge) {
        return bridgeHandlers.remove(bridge);
    }

    /**
     * Check if the {@link VeluxDiscoveryService} list of {@link VeluxBridgeHandler} is empty
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return bridgeHandlers.isEmpty();
    }

    /**
     * Discover any bridges on the network that are not yet instantiated.
     */
    private void discoverBridges() {
        // discover the list of IP addresses of bridges on the network
        Set<String> foundBridgeIpAddresses = VeluxBridgeFinder.discoverIpAddresses(scheduler);
        // publish discovery results
        for (String ipAddr : foundBridgeIpAddresses) {
            ThingUID thingUID = new ThingUID(THING_TYPE_BRIDGE, ipAddr.replace(".", "_"));
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_BRIDGE)
                    .withProperty(VeluxBridgeConfiguration.BRIDGE_IPADDRESS, ipAddr)
                    .withRepresentationProperty(VeluxBridgeConfiguration.BRIDGE_IPADDRESS)
                    .withLabel(String.format("Velux Bridge (%s)", ipAddr)).build();
            thingDiscovered(result);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("startBackgroundDiscovery() called.");
        ScheduledFuture<?> task = this.backgroundTask;
        if (task == null || task.isCancelled()) {
            this.backgroundTask = scheduler.scheduleWithFixedDelay(this::startScan, 10, 600, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.trace("stopBackgroundDiscovery() called.");
        ScheduledFuture<?> task = this.backgroundTask;
        if (task != null) {
            task.cancel(true);
        }
    }
}
