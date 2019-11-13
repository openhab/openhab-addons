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
import static org.openhab.binding.shelly.internal.api.ShellyApiJson.*;

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
    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            logger.debug("Shelly Discoverxy service activated");
            Validate.notNull(componentContext);
            Validate.notNull(bindingConfig);
            bindingConfig.updateFromProperties(componentContext.getProperties());
        } catch (RuntimeException e) {
            logger.warn("Exception on ShellyDiscoveryParticipant(): {} ({})", e.getClass(), e.getMessage());
        }
    }

    @Modified
    @SuppressWarnings("null")
    protected void modified(ComponentContext componentContext) {
        try {
            logger.debug("Shelly Binding Configuration refreshed");
            bindingConfig.updateFromProperties(componentContext.getProperties());
        } catch (RuntimeException e) {
            logger.warn("Exception on ShellyDiscoveryParticipant.modified(): {} ({})", e.getClass(), e.getMessage());
        }
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
                logger.warn("Discovered Shelly device {} doesn't have an IP address, service-info={}", name, service);
                return null;
            }
            logger.info("Shelly device discovered: IP-Adress={}, name={}", address, name);

            ShellyThingConfiguration config = new ShellyThingConfiguration();
            if (handlerFactory != null) {
                bindingConfig = handlerFactory.getBindingConfig();
                Validate.notNull(bindingConfig);
            }
            config.deviceIp = address;
            config.userId = bindingConfig.defaultUserId;
            config.password = bindingConfig.defaultPassword;

            // Get device settings
            ShellyHttpApi api = new ShellyHttpApi(config);
            thingType = StringUtils.substringBeforeLast(name, "-");

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
            } catch (IOException e) {
                if (e.getMessage().contains(HTTP_401_UNAUTHORIZED)) {
                    logger.warn("Device {} ({}) reported 'Access defined' (userid/password mismatch).", name, address);
                    logger.info(
                            "You could set a default userid and passowrd in the binding config and re-discover devices");
                    logger.info(
                            "or you need to disable device protection (userid/password) in the Shelly App for device discovery.");
                    logger.info(
                            "Once the device is discoverd you could set the userid/password and re-enable device protection in the Shelly App.");
                    name = (name + "-" + address).replace('.', '-');
                    thingUID = new ThingUID(THING_TYPE_SHELLYPROTECTED, name);
                } else {
                    logger.warn("Device discovery failed for device {}, IP {}: {} ({})", name, address, e.getMessage(),
                            e.getClass());
                }
            }

            if (thingUID == null) {
                // get thing type from device name
                thingUID = this.getThingUID(name, mode);
            }
            Validate.notNull(thingUID, "Discovery: thingUID must not be null!");

            addProperty(properties, PROPERTY_MODEL_ID, model);
            addProperty(properties, PROPERTY_THINGTYPE, thingUID.getId());
            addProperty(properties, CONFIG_DEVICEIP, address);
            addProperty(properties, PROPERTY_SERVICE_NAME, service.getName());

            logger.debug("Adding Shelly thing, UID={}", thingUID.getAsString());
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel(name + " - " + address)
                    .withRepresentationProperty(name).build();
        } catch (RuntimeException e) {
            logger.warn("Device discovery failed for device {}, IP {}, service={}: {} ({})", name, address, name,
                    e.getMessage(), e.getClass());
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
        return getThingUID(service.getName().toLowerCase(), "");
    }

    @Nullable
    public ThingUID getThingUID(String name, String mode) {
        String devid = StringUtils.substringAfterLast(name, "-");

        if (name.startsWith("shelly1pm")) {
            return new ThingUID(THING_TYPE_SHELLY1PM, devid);
        }
        if (name.startsWith("shellyem")) {
            return new ThingUID(THING_TYPE_SHELLYEM, devid);
        }
        if (name.startsWith("shelly1")) {
            return new ThingUID(THING_TYPE_SHELLY1, devid);
        }
        if (name.startsWith("shellyswitch25")) { // Shelly v2.5
            if (mode.equals(SHELLY_MODE_RELAY)) {
                return new ThingUID(THING_TYPE_SHELLY25_RELAY, devid);
            }
            if (mode.equals(SHELLY_MODE_ROLLER)) {
                return new ThingUID(THING_TYPE_SHELLY25_ROLLER, devid);
            }
        }
        if (name.startsWith("shellyswitch")) { // Shelly v2
            if (mode.equals(SHELLY_MODE_RELAY)) {
                return new ThingUID(THING_TYPE_SHELLY2_RELAY, devid);
            }
            if (mode.equals(SHELLY_MODE_ROLLER)) {
                return new ThingUID(THING_TYPE_SHELLY2_ROLLER, devid);
            }
        }
        if (name.startsWith("shelly4pro")) {
            return new ThingUID(THING_TYPE_SHELLY4PRO, devid);
        }
        if (name.startsWith("shellyplug-s")) {
            return new ThingUID(THING_TYPE_SHELLYPLUGS, devid);
        }
        if (name.startsWith("shellyplug")) {
            return new ThingUID(THING_TYPE_SHELLYPLUG, devid);
        }
        if (name.startsWith("shellybulb")) {
            return new ThingUID(THING_TYPE_SHELLYBULB, devid);
        }
        if (name.startsWith("shellysense")) {
            return new ThingUID(THING_TYPE_SHELLYSENSE, devid);
        }
        if (name.startsWith("shellyht")) {
            return new ThingUID(THING_TYPE_SHELLYHT, devid);
        }
        if (name.startsWith("shellysmoke")) {
            return new ThingUID(THING_TYPE_SHELLYSMOKE, devid);
        }
        if (name.startsWith("shellyflood")) {
            return new ThingUID(THING_TYPE_SHELLYFLOOD, devid);
        }
        if (name.startsWith("shellyrgbw2")) {
            if (mode.equals(SHELLY_MODE_COLOR)) {
                return new ThingUID(THING_TYPE_SHELLYRGBW2_COLOR, devid);
            }
            if (mode.equals(SHELLY_MODE_WHITE)) {
                return new ThingUID(THING_TYPE_SHELLYRGBW2_WHITE, devid);
            }
        }
        if (name.startsWith("shellydimmer")) {
            return new ThingUID(THING_TYPE_SHELLYDIMMER, devid);
        }

        logger.info("Unsupported Shelly Device discovered: {} (mode {})", name, mode);
        return null;

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
