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
package org.openhab.binding.sony.internal;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.openhab.binding.sony.internal.net.NetUtil;
import org.openhab.binding.sony.internal.providers.SonyDefinitionProvider;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class provides all the base functionality for discovery participants
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractDiscoveryParticipant {

    /** The logger */
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The service this discovery participant is for */
    private final String service;

    /** The sony definition provider */
    private final SonyDefinitionProvider sonyDefinitionProvider;

    /** Whether discovery is enabled */
    private final AtomicBoolean discoveryEnabled = new AtomicBoolean();

    /**
     * Construct the participant from the given service
     *
     * @param service a non-null, non-empty service
     * @param sonyDefinitionProvider a non-null sony definition provider
     */
    protected AbstractDiscoveryParticipant(final String service, final SonyDefinitionProvider sonyDefinitionProvider) {
        Validate.notEmpty(service, "service cannot be empty");
        Objects.requireNonNull(sonyDefinitionProvider, "sonyDefinitionProvider cannot be null");

        this.service = service;
        this.sonyDefinitionProvider = sonyDefinitionProvider;
    }

    /**
     * Returns a list of thing type uids supported by this discovery implementation
     *
     * @return a non-null, never empty list of supported thing type uids
     */
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        final Set<ThingTypeUID> uids = new HashSet<ThingTypeUID>();

        // Add the generic one
        uids.add(new ThingTypeUID(SonyBindingConstants.BINDING_ID, service));

        // Add any specific ones
        for (final ThingType tt : sonyDefinitionProvider.getThingTypes(null)) {
            uids.add(tt.getUID());
        }
        return uids;
    }

    /**
     * Determines if the remote device is a sony device (based on it's manufacturer)
     *
     * @param device a non-null device
     * @return true if it's a sony device, false otherwise
     */
    protected static boolean isSonyDevice(final RemoteDevice device) {
        Objects.requireNonNull(device, "device cannot be null");

        final DeviceDetails details = device.getDetails();
        final String manufacturer = details == null || details.getManufacturerDetails() == null ? null
                : details.getManufacturerDetails().getManufacturer();
        return details != null
                && StringUtils.containsIgnoreCase(manufacturer, SonyBindingConstants.SONY_REMOTEDEVICE_ID);
    }

    /**
     * Get model name for the remove device
     *
     * @param device a non-null device
     * @return the model name or null if none found
     */
    protected static @Nullable String getModelName(final RemoteDevice device) {
        Objects.requireNonNull(device, "device cannot be null");

        final DeviceDetails details = device.getDetails();
        if (details == null) {
            return null;
        }

        final String modelName = details.getModelDetails() == null ? null : details.getModelDetails().getModelName();
        if (modelName != null && StringUtils.isNotEmpty(modelName) && SonyUtil.isValidModelName(modelName)) {
            return modelName;
        }

        final String friendlyName = details.getFriendlyName();
        if (friendlyName != null && StringUtils.isNotEmpty(friendlyName) && SonyUtil.isValidModelName(friendlyName)) {
            return friendlyName;
        }

        return StringUtils.isNotEmpty(friendlyName) ? friendlyName : modelName;
    }

    /**
     * Get model description for the remove device
     *
     * @param device a non-null device
     * @return the model description or null if none found
     */
    protected static @Nullable String getModelDescription(final RemoteDevice device) {
        Objects.requireNonNull(device, "device cannot be null");

        final DeviceDetails details = device.getDetails();
        return details == null || details.getModelDetails() == null ? null
                : details.getModelDetails().getModelDescription();
    }

    /**
     * Get's the mac address of the remote device (or if the UID is a mac address)
     *
     * @param identity a non-null identity
     * @param uid a non-null tyhing UID
     * @return a valid mac address if found, null if not
     */
    protected static @Nullable String getMacAddress(final RemoteDeviceIdentity identity, final ThingUID uid) {
        Objects.requireNonNull(identity, "identity cannot be null");
        Objects.requireNonNull(uid, "uid cannot be null");

        final String wolMacAddress = NetUtil.getMacAddress(identity.getWakeOnLANBytes());
        if (NetUtil.isMacAddress(wolMacAddress)) {
            return wolMacAddress;
        } else if (NetUtil.isMacAddress(uid.getId())) {
            return uid.getId();
        }
        return null;
    }

    /**
     * Gets the label from the remote device
     *
     * @param device a non-null, non-empty device
     * @param suffix a non-null, non-empty suffix
     * @return the label for the device
     */
    protected static String getLabel(final RemoteDevice device, final String suffix) {
        Objects.requireNonNull(device, "device cannot be null");
        Validate.notEmpty(suffix, "suffix cannot be empty");

        final String modelName = getModelName(device);
        final String friendlyName = device.getDetails().getFriendlyName();

        final StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotEmpty(friendlyName)) {
            sb.append(friendlyName);
        } else if (StringUtils.isNotEmpty(modelName)) {
            sb.append(modelName);
        } else {
            sb.append(device.getDisplayString());
        }

        return sb.toString();
    }

    /**
     * Determines if there is a scalar thing type defined for the device
     *
     * @param device the non-null device
     * @return true if found, false otherwise
     */
    protected boolean isScalarThingType(final RemoteDevice device) {
        Objects.requireNonNull(device, "device cannot be null");
        return getThingTypeUID(SonyBindingConstants.SCALAR_THING_TYPE_PREFIX, getModelName(device)) != null;
    }

    /**
     * Returns the thing type related to the model name. This will search the registry for a thing type that is
     * specific to the model. If found, that thing type will be used. If not found, the generic scalar thing type will
     * be used
     *
     * @param modelName a possibly null, possibly empty model name
     * @return a ThingTypeUID if found, null if not
     */
    protected @Nullable ThingTypeUID getThingTypeUID(final @Nullable String modelName) {
        return getThingTypeUID(service, modelName);
    }

    /**
     * Returns the thing type related to the service/model name. This will search the registry for a thing type that is
     * specific to the model. If found, that thing type will be used. If not found, the generic scalar thing type will
     * be used
     *
     * @param service a non-null, non-empty service
     * @param modelName a possibly null, possibly empty model name
     * @return a ThingTypeUID if found, null if not
     */
    private @Nullable ThingTypeUID getThingTypeUID(final String service, final @Nullable String modelName) {
        Validate.notEmpty(service, "service cannot be empty");

        if (modelName == null || StringUtils.isEmpty(modelName)) {
            logger.debug("Emtpy model name!");
            return null;
        }

        for (final ThingType tt : sonyDefinitionProvider.getThingTypes(Locale.getDefault())) {
            if (SonyUtil.isModelMatch(tt.getUID(), service, modelName)) {
                logger.debug("Using specific thing type for {}: {}", modelName, tt);
                return tt.getUID();
            }
        }

        logger.debug("No specific thing type found for {}", modelName);
        return null;
    }

    /**
     * Determines if discovery is enabled or not
     * 
     * @return true if enabled, false otherwise
     */
    protected boolean isDiscoveryEnabled() {
        return discoveryEnabled.get();
    }

    /**
     * Abstract function to determine the default for enabling discovery
     * 
     * @return true to enable by default, false otherwise
     */
    protected abstract boolean getDiscoveryEnableDefault();

    @Activate
    protected void activate(final ComponentContext componentContext) {
        activateOrModifyService(componentContext);
    }

    @Modified
    protected void modified(final ComponentContext componentContext) {
        activateOrModifyService(componentContext);
    }

    /**
     * Helper method to determine if discovery is enabled via a configuration file
     * 
     * @param componentContext a non-null component context
     */
    private void activateOrModifyService(final ComponentContext componentContext) {
        Objects.requireNonNull(componentContext, "componentContext cannot be null");

        final Dictionary<String, @Nullable Object> properties = componentContext.getProperties();
        final String discoveryEnabled = (String) properties.get("background");
        if (discoveryEnabled == null || StringUtils.isEmpty(discoveryEnabled)) {
            this.discoveryEnabled.set(getDiscoveryEnableDefault());
        } else {
            this.discoveryEnabled.set(Boolean.valueOf(discoveryEnabled));
        }
    }
}
