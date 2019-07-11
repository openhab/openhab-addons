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
package org.openhab.binding.teleinfo.internal;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.common.AbstractUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractControllerHandler;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoControllerHandlerListener;
import org.openhab.binding.teleinfo.internal.reader.Frame;
import org.openhab.binding.teleinfo.internal.reader.FrameOptionBase;
import org.openhab.binding.teleinfo.internal.reader.FrameOptionEjp;
import org.openhab.binding.teleinfo.internal.reader.FrameOptionHeuresCreuses;
import org.openhab.binding.teleinfo.internal.reader.FrameOptionTempo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoDiscoveryService} class is the service to discover a skeleton for controller handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
// @Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.teleinfo")
public class TeleinfoDiscoveryService extends AbstractDiscoveryService implements TeleinfoControllerHandlerListener {

    private final Logger logger = LoggerFactory.getLogger(TeleinfoDiscoveryService.class);
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .of(THING_HCHP_ELECTRICITY_METER_TYPE_UID, THING_BASE_ELECTRICITY_METER_TYPE_UID,
                    THING_TEMPO_ELECTRICITY_METER_TYPE_UID, THING_EJP_ELECTRICITY_METER_TYPE_UID)
            .collect(Collectors.toSet());

    private final TeleinfoAbstractControllerHandler controllerHandler;

    public TeleinfoDiscoveryService(TeleinfoAbstractControllerHandler controllerHandler, int timeout) {
        super(timeout);
        this.controllerHandler = controllerHandler;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    public void activate() {
        logger.debug("Teleinfo discovery: Active {}", controllerHandler.getThing().getUID());
    }

    @Override
    public void deactivate() {
        logger.debug("Teleinfo discovery: Deactivate {}", controllerHandler.getThing().getUID());
    }

    @Override
    protected void startScan() {
        logger.debug("Teleinfo discovery: Start {}", controllerHandler.getThing().getUID());

        // Start the search for new devices
        controllerHandler.addListener(this);
    }

    @Override
    public synchronized void abortScan() {
        logger.debug("Teleinfo discovery: Abort {}", controllerHandler.getThing().getUID());
        controllerHandler.removeListener(this);
        super.abortScan();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Teleinfo discovery: Stop {}", controllerHandler.getThing().getUID());
        controllerHandler.removeListener(this);
        super.stopScan();
    }

    @Override
    public void onFrameReceived(@NonNull TeleinfoAbstractControllerHandler controllerHandler, @NonNull Frame frame) {
        onElectricityMeterDiscovered(frame);
    }

    private void onElectricityMeterDiscovered(final Frame frameSample) {
        ThingUID thingUID = getThingUID(frameSample);
        logger.debug("Electricity meter discovered: {}", thingUID);

        final Map<String, Object> properties = getThingProperties(thingUID.getThingTypeUID(), frameSample);
        final String representationProperty = getRepresentationProperty(thingUID.getThingTypeUID(), frameSample);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withLabel(frameSample.getADCO()).withThingType(getThingTypeUID(frameSample))
                .withBridge(controllerHandler.getThing().getUID()).withRepresentationProperty(representationProperty)
                .build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID getThingUID(final Frame teleinfoFrame) {
        return new ThingUID(getThingTypeUID(teleinfoFrame), teleinfoFrame.getADCO(),
                controllerHandler.getThing().getUID().getId());
    }

    private ThingTypeUID getThingTypeUID(final Frame teleinfoFrame) {
        if (teleinfoFrame instanceof FrameOptionHeuresCreuses) {
            return THING_HCHP_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameOptionBase) {
            return THING_BASE_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameOptionEjp) {
            return THING_EJP_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameOptionTempo) {
            return THING_TEMPO_ELECTRICITY_METER_TYPE_UID;
        } else {
            throw new IllegalStateException("Teleinfo frame type not supported: " + teleinfoFrame.getClass());
        }
    }

    private Map<String, Object> getThingProperties(final ThingTypeUID thingTypeId, final Frame teleinfoFrame) {
        Map<String, Object> properties = new HashMap<String, Object>();
        if (THING_BASE_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
            properties.put(THING_BASE_ELECTRICITY_METER_PROPERTY_ADCO, teleinfoFrame.getADCO());
        } else if (THING_HCHP_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
            properties.put(THING_HCHP_ELECTRICITY_METER_PROPERTY_ADCO, teleinfoFrame.getADCO());
        } else if (THING_TEMPO_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
            properties.put(THING_TEMPO_ELECTRICITY_METER_PROPERTY_ADCO, teleinfoFrame.getADCO());
        } else if (THING_EJP_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
            properties.put(THING_EJP_ELECTRICITY_METER_PROPERTY_ADCO, teleinfoFrame.getADCO());
        } else {
            throw new IllegalStateException("Teleinfo frame type not supported: " + teleinfoFrame.getClass());
        }

        return properties;
    }

    private String getRepresentationProperty(final ThingTypeUID thingTypeId, final Frame teleinfoFrame) {
        return teleinfoFrame.getADCO() + AbstractUID.SEPARATOR + thingTypeId.getId();
    }
}
