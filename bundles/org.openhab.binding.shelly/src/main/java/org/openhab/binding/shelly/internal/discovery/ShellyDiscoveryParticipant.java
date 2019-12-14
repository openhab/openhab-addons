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
package org.openhab.binding.shelly.internal.discovery;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_MODEL_ID;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api.ShellyHttpApi;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyBaseHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class identifies Shelly devices by their mDNS service information.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, immediate = true)
public class ShellyDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(ShellyDiscoveryParticipant.class);
    private @Nullable ShellyBindingConfiguration bindingConfig = new ShellyBindingConfiguration();
    private @Nullable ShellyHandlerFactory handlerFactory;

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    /**
     * Called at the service activation.
     *
     * @param componentContext
     */
    @SuppressWarnings("null")
    @Activate
    protected void activate(ComponentContext componentContext) {
        logger.debug("Shelly Discovery service activated");
        Validate.notNull(componentContext);
        Validate.notNull(bindingConfig);
        bindingConfig.updateFromProperties(componentContext.getProperties());
    }

    @Modified
    @SuppressWarnings("null")
    protected void modified(ComponentContext componentContext) {
        logger.debug("Shelly Binding Configuration refreshed");
        bindingConfig.updateFromProperties(componentContext.getProperties());
    }

    @SuppressWarnings("null")
    @Nullable
    @Override
    public DiscoveryResult createResult(@Nullable ServiceInfo service) {
        Validate.notNull(bindingConfig);
        if ((service == null) || !service.getName().startsWith("shelly")) {
            return null;
        }

        String address = "";
        String name = "";
        String mode = "";
        String model = "unknown";
        String thingType = "";
        ThingUID thingUID = null;
        ShellyDeviceProfile profile = null;
        Map<String, Object> properties = new HashMap<String, Object>();

        try {
            name = service.getName().toLowerCase();
            address = StringUtils.substringBetween(service.toString(), "/", ":80");
            if (address == null) {
                logger.debug("Shelly device {} discovered with empty IP address", name);
                return null;
            }
            logger.debug("Shelly device discovered: IP-Adress={}, name={}", address, name);

            ShellyThingConfiguration config = new ShellyThingConfiguration();
            if (handlerFactory != null) {
                bindingConfig = handlerFactory.getBindingConfig();
            }

            // Get device settings
            Validate.notNull(bindingConfig);
            config.deviceIp = address;
            config.userId = bindingConfig.defaultUserId;
            config.password = bindingConfig.defaultPassword;
            ShellyHttpApi api = new ShellyHttpApi(config);

            try {
                profile = api.getDeviceProfile(thingType);
                Validate.notNull(profile);
                logger.debug("Shelly settings : {}", profile.settingsJson);
                Validate.notNull(profile, "Unable to get device profile: ");
                model = profile.settings.device.type;
                mode = profile.mode;

                properties = ShellyBaseHandler.fillDeviceProperties(profile);
                logger.trace("name={}, thingType={}, deviceType={}, mode={}", name, thingType, profile.deviceType,
                        mode.isEmpty() ? "<standard>" : mode);

                // get thing type from device name
                thingUID = ShellyThingCreator.getThingUID(name, mode, false);
            } catch (IOException e) {
                if (e.getMessage().contains(APIERR_HTTP_401_UNAUTHORIZED)) {
                    logger.warn("Device {} ({}) reported 'Access defined' (userid/password mismatch).", name, address);

                    // create shellyunknown thing - will be changed during thing initialization with valid credentials
                    thingUID = ShellyThingCreator.getThingUID(name, mode, true);
                } else {
                    logger.warn("Device discovery failed for device {}, IP {}: {} ({})\n{}", name, address,
                            e.getMessage(), e.getClass(), e.getStackTrace());
                }
            }

            if (thingUID != null) {
                addProperty(properties, CONFIG_DEVICEIP, address);
                addProperty(properties, PROPERTY_MODEL_ID, model);
                addProperty(properties, PROPERTY_SERVICE_NAME, name);
                addProperty(properties, PROPERTY_DEV_TYPE, thingType);
                addProperty(properties, PROPERTY_DEV_MODE, mode);

                logger.debug("Adding Shelly thing, UID={}", thingUID.getAsString());
                return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withLabel(name + " - " + address).withRepresentationProperty(name).build();
            }
        } catch (NullPointerException e) {
            logger.warn("Device discovery failed for device {}, IP {}, service={}: {}", name, address, name,
                    e.getMessage(), e);
        }
        return null;
    }

    private void addProperty(Map<String, Object> properties, String key, @Nullable String value) {
        properties.put(key, value != null ? value : "");
    }

    @Nullable
    @Override
    public ThingUID getThingUID(@Nullable ServiceInfo service) {
        logger.debug("ServiceInfo {}", service);
        Validate.notNull(service);
        return ShellyThingCreator.getThingUID(service.getName().toLowerCase(), "", false);
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setShellyHandlerFactory(ShellyHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
        logger.trace("Discovery: HandlerFactory bound");
    }

    public void unsetShellyHandlerFactory(ShellyHandlerFactory handlerFactory) {
        this.handlerFactory = null;
        logger.trace("Discovery: HandlerFactory unbound");
    }
}
