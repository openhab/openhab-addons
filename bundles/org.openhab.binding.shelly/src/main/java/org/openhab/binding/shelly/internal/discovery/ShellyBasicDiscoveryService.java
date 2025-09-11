/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.discovery;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;
import static org.openhab.core.thing.Thing.*;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyApiResult;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api1.Shelly1HttpApi;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiRpc;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyBaseHandler;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device discovery creates a thing in the inbox for each vehicle
 * found in the data received from {@link ShellyBasicDiscoveryService}.
 *
 * @author Markus Michels - Initial Contribution
 *
 */
@NonNullByDefault
public class ShellyBasicDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(ShellyBasicDiscoveryService.class);

    private final BundleContext bundleContext;
    private final ShellyThingTable thingTable;
    private static final int TIMEOUT = 10;
    private @Nullable ServiceRegistration<?> discoveryService;

    public ShellyBasicDiscoveryService(BundleContext bundleContext, ShellyThingTable thingTable) {
        super(SUPPORTED_THING_TYPES, TIMEOUT);
        this.bundleContext = bundleContext;
        this.thingTable = thingTable;
    }

    public void registerDeviceDiscoveryService() {
        if (discoveryService == null) {
            discoveryService = bundleContext.registerService(DiscoveryService.class.getName(), this, new Hashtable<>());
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting BLU Discovery");
        thingTable.startScan();
    }

    public void discoveredResult(ThingTypeUID tuid, String model, String serviceName, String address,
            Map<String, Object> properties) {
        ThingUID uid = ShellyThingCreator.getThingUID(serviceName, model, "");
        logger.debug("Adding discovered thing with id {}", uid.toString());
        properties.put(PROPERTY_MAC_ADDRESS, address);
        String thingLabel = "Shelly BLU " + model + " (" + serviceName + ")";
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withRepresentationProperty(PROPERTY_DEV_NAME).withLabel(thingLabel).build();
        thingDiscovered(result);
    }

    public void discoveredResult(DiscoveryResult result) {
        thingDiscovered(result);
    }

    public void unregisterDeviceDiscoveryService() {
        ServiceRegistration<?> discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.unregister();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        unregisterDeviceDiscoveryService();
    }

    public static @Nullable DiscoveryResult createResult(boolean gen2, String hostname, String ipAddress,
            ShellyBindingConfiguration bindingConfig, HttpClient httpClient, ShellyTranslationProvider messages) {
        Logger logger = LoggerFactory.getLogger(ShellyBasicDiscoveryService.class);
        ThingUID thingUID = null;
        ShellyDeviceProfile profile;
        ShellySettingsDevice devInfo;
        ShellyApiInterface api = null;
        boolean auth = false;
        String mac = "";
        String model = "";
        String mode = "";
        String name = hostname;
        String deviceName = "";
        String thingType = "";
        Map<String, Object> properties = new TreeMap<>();

        try {
            ShellyThingConfiguration config = fillConfig(bindingConfig, ipAddress);
            api = gen2 ? new Shelly2ApiRpc(name, config, httpClient) : new Shelly1HttpApi(name, config, httpClient);
            api.initialize();
            devInfo = api.getDeviceInfo();
            mac = getString(devInfo.mac);
            model = getString(devInfo.type);
            auth = getBool(devInfo.auth);
            if (name.isEmpty() || name.startsWith(SERVICE_NAME_SHELLYPLUSRANGE_PREFIX)) {
                name = devInfo.hostname;
            }

            thingType = substringBeforeLast(name, "-");
            mode = devInfo.mode;
            profile = api.getDeviceProfile(ShellyThingCreator.getThingTypeUID(name, model, mode), devInfo);
            deviceName = profile.name;
            properties = ShellyBaseHandler.fillDeviceProperties(profile);

            // get thing type from device name
            thingUID = ShellyThingCreator.getThingUID(name, model, mode);
        } catch (ShellyApiException e) {
            ShellyApiResult result = e.getApiResult();
            if (result.isHttpAccessUnauthorized()) {
                // create shellyunknown thing - will be changed during thing initialization with valid credentials
                thingUID = ShellyThingCreator.getThingUIDForUnknown(name, model, mode);
            }
        } catch (IllegalArgumentException | IOException e) { // maybe some format description was buggy
            logger.debug("Discovery: Unable to discover thing", e);
        } finally {
            if (api != null) {
                api.close();
            }
        }

        if (thingUID != null) {
            addProperty(properties, PROPERTY_MAC_ADDRESS, mac);
            addProperty(properties, CONFIG_DEVICEIP, ipAddress);
            addProperty(properties, PROPERTY_MODEL_ID, model);
            addProperty(properties, PROPERTY_SERVICE_NAME, name);
            addProperty(properties, PROPERTY_DEV_NAME, deviceName);
            addProperty(properties, PROPERTY_DEV_TYPE, thingType);
            addProperty(properties, PROPERTY_DEV_GEN, gen2 ? "2" : "1");
            addProperty(properties, PROPERTY_DEV_MODE, mode);
            addProperty(properties, PROPERTY_DEV_AUTH, auth ? "yes" : "no");

            String thingLabel = deviceName.isEmpty() ? name + " - " + ipAddress
                    : deviceName + " (" + name + "@" + ipAddress + ")";
            logger.debug("{}: Adding Thing to Inbox (type {}, model {}, mode={})", name, thingType, model, mode);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel(thingLabel)
                    .withRepresentationProperty(PROPERTY_SERVICE_NAME).build();
        }

        return null;
    }

    public static ShellyThingConfiguration fillConfig(ShellyBindingConfiguration bindingConfig, String address)
            throws IOException {
        ShellyThingConfiguration config = new ShellyThingConfiguration();
        config.deviceIp = address;
        config.userId = bindingConfig.defaultUserId;
        config.password = bindingConfig.defaultPassword;
        return config;
    }

    private static void addProperty(Map<String, Object> properties, String key, @Nullable String value) {
        properties.put(key, value != null ? value : "");
    }
}
