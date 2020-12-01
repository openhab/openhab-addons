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
package org.openhab.binding.sony.internal.scalarweb;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.meta.RemoteService;
import org.jupnp.model.types.ServiceId;
import org.jupnp.model.types.UDN;
import org.openhab.binding.sony.internal.AbstractDiscoveryParticipant;
import org.openhab.binding.sony.internal.SonyBindingConstants;
import org.openhab.binding.sony.internal.UidUtils;
import org.openhab.binding.sony.internal.providers.SonyDefinitionProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This implementation of the {@link UpnpDiscoveryParticipant} provides discovery of Sony SCALAR protocol devices.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, configurationPid = "discovery.sony-scalar")
public class ScalarWebDiscoveryParticipant extends AbstractDiscoveryParticipant implements UpnpDiscoveryParticipant {
    // See footnotes in createResult for the purpose of this field
    private static final Map<ThingUID, @Nullable String> SCALARTOIRCC = new HashMap<>();

    /**
     * Constructs the participant
     * 
     * @param sonyDefinitionProvider a non-null sony definition provider
     */
    @Activate
    public ScalarWebDiscoveryParticipant(final @Reference SonyDefinitionProvider sonyDefinitionProvider) {
        super(SonyBindingConstants.SCALAR_THING_TYPE_PREFIX, sonyDefinitionProvider);
    }

    @Override
    protected boolean getDiscoveryEnableDefault() {
        return true;
    }

    @Override
    public @Nullable DiscoveryResult createResult(final RemoteDevice device) {
        Objects.requireNonNull(device, "device cannot be null");

        if (!isDiscoveryEnabled()) {
            return null;
        }

        /**
         * We need to handle a bunch of situations
         * 1. Scalar service with no IRCC at all
         * 2. IRCC service with no Scalar service
         * 3. SCALAR and IRCC service together
         * 4. SCALAR separate from the IRCC service
         * 4a. where the IRCC service comes in BEFORE the scalar service
         * 4b. where the IRCC service comes in AFTER the scalar service
         * 5. Both 3 and 4 (it advertises both together and separate)
         *
         * Since the configuration contains an IRCC URL, we need to handle an IRCC service being advertised separately
         * (and can come in before or after the scalar notification)
         *
         * In case of #3, we prefer the URL from the scalar one (and will ignore the IRCC one)
         *
         * If we receive an IRCC service with no scalar service (maybe #2, #4 or #5 scenarios)
         * -- Check to see if we have a prior scalar result (#4b or #5)
         * -- -- If we have a prior result and no URL - add our URL and create a new result with the IRCC url (#4b)
         * -- -- If we have a prior result and a URL - ignore the IRCC service (#5)
         * -- -- If no scalar service - save our URL for future results but do NOT create a result (#2, #4a or #5)
         *
         * -- If it's a SCALAR service (#1, #3, #4a, #5)
         * -- -- If we have no IRCC service and no saved IRCC url - create an entry (#1/#4b/#5)
         * -- -- If we have no IRCC service and a null saved IRCC url - use null (#1/#4b/#5)
         * -- -- If we have no IRCC service and a non-null saved IRCC url - use the url (#4a/#5)
         * -- -- If we have a IRCC service, use scalar URL as IRCC and save it (#3/#5)
         * -- -- Create a result
         */
        if (!isSonyDevice(device)) {
            return null;
        }

        final String modelName = getModelName(device);
        if (modelName == null || StringUtils.isEmpty(modelName)) {
            logger.debug("Found Sony device but it has no model name - ignoring: {}", device);
            return null;
        }

        final RemoteDeviceIdentity identity = device.getIdentity();

        final RemoteService irccService = device.findService(
                new ServiceId(SonyBindingConstants.SONY_SERVICESCHEMA, SonyBindingConstants.SONY_IRCCSERVICENAME));
        final RemoteService scalarWebService = device.findService(
                new ServiceId(SonyBindingConstants.SONY_SERVICESCHEMA, SonyBindingConstants.SONY_SCALARWEBSERVICENAME));

        if (irccService != null && scalarWebService == null) {
            final ThingUID thingUID = getThingUID(device, modelName);
            final String irccUrl = identity.getDescriptorURL().toString();

            if (SCALARTOIRCC.containsKey(thingUID)) {
                final String oldIrccUrl = SCALARTOIRCC.get(thingUID);
                if (oldIrccUrl == null) {
                    SCALARTOIRCC.put(thingUID, irccUrl);
                    return createResult(device, thingUID, irccUrl);
                }
            } else {
                SCALARTOIRCC.put(thingUID, irccUrl);
            }
            return null;
        }

        if (scalarWebService == null) {
            logger.debug("Found sony device but ignored because of no scalar service: {}", device);
            return null;
        }

        final ThingUID uid = getThingUID(device);
        if (uid == null) {
            // no need for log message as getThingUID spits them out
            return null;
        }

        String irccUrl = null;
        if (irccService == null) {
            if (SCALARTOIRCC.containsKey(uid)) {
                irccUrl = SCALARTOIRCC.get(uid);
            } else {
                SCALARTOIRCC.put(uid, null);
            }
        } else {
            irccUrl = identity.getDescriptorURL().toString();
            SCALARTOIRCC.put(uid, irccUrl);
        }

        return createResult(device, uid, irccUrl);
    }

    @Override
    public @Nullable ThingUID getThingUID(final RemoteDevice device) {
        Objects.requireNonNull(device, "device cannot be null");

        if (!isDiscoveryEnabled()) {
            return null;
        }

        if (isSonyDevice(device)) {
            final String modelName = getModelName(device);
            if (modelName == null || StringUtils.isEmpty(modelName)) {
                logger.debug("Found Sony device but it has no model name - ignoring");
                return null;
            }

            final RemoteService scalarWebService = device.findService(new ServiceId(
                    SonyBindingConstants.SONY_SERVICESCHEMA, SonyBindingConstants.SONY_SCALARWEBSERVICENAME));
            if (scalarWebService != null) {
                final RemoteDeviceIdentity identity = device.getIdentity();
                if (identity != null) {
                    final UDN udn = device.getIdentity().getUdn();
                    logger.debug("Found Sony WebScalarAPI service: {}", udn);
                    final ThingTypeUID modelUID = getThingTypeUID(modelName);
                    return UidUtils.createThingUID(modelUID == null ? ScalarWebConstants.THING_TYPE_SCALAR : modelUID,
                            udn);
                } else {
                    logger.debug("Found Sony WebScalarAPI service but it had no identity!");
                }
            }
        }

        return null;
    }

    /**
     * Helper method to create a result from the device, uid and possibly irccurl
     *
     * @param device a non-null device
     * @param uid a non-null thing uid
     * @param irccUrl a possibly null, possibly empty irccurl
     * @return a non-null result
     */
    private DiscoveryResult createResult(final RemoteDevice device, final ThingUID uid,
            final @Nullable String irccUrl) {
        Objects.requireNonNull(device, "device cannot be null");
        Objects.requireNonNull(uid, "uid cannot be null");

        final RemoteDeviceIdentity identity = device.getIdentity();
        final URL scalarURL = identity.getDescriptorURL();

        final ScalarWebConfig config = new ScalarWebConfig();
        config.setDeviceAddress(scalarURL.toString());
        config.setIrccUrl(irccUrl == null ? "" : irccUrl);

        config.setDiscoveredCommandsMapFile("scalar-" + uid.getId() + ".map");
        config.setDiscoveredMacAddress(getMacAddress(identity, uid));
        config.setDiscoveredModelName(getModelName(device));

        final String thingId = UidUtils.getThingId(identity.getUdn());
        return DiscoveryResultBuilder.create(uid).withProperties(config.asProperties())
                .withProperty("ScalarUDN", StringUtils.defaultIfEmpty(thingId, uid.getId()))
                .withRepresentationProperty("ScalarUDN").withLabel(getLabel(device, "Scalar")).build();
    }

    /**
     * Helper method to get a thing UID from the device and model name
     * 
     * @param device a non-null device
     * @param modelName a non-null, non-empty modelname
     * @return
     */
    private ThingUID getThingUID(final RemoteDevice device, final String modelName) {
        Objects.requireNonNull(device, "device cannot be null");
        Validate.notEmpty(modelName, "modelName cannot be empty");

        final UDN udn = device.getIdentity().getUdn();
        final ThingTypeUID modelUID = getThingTypeUID(modelName);
        return UidUtils.createThingUID(modelUID == null ? ScalarWebConstants.THING_TYPE_SCALAR : modelUID, udn);
    }
}
