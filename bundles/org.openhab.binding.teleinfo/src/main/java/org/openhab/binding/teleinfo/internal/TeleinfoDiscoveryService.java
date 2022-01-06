/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teleinfo.internal.data.Frame;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractControllerHandler;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoControllerHandlerListener;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.Label;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoDiscoveryService} class is the service to discover a skeleton for controller handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoDiscoveryService extends AbstractDiscoveryService
        implements TeleinfoControllerHandlerListener, ThingHandlerService, DiscoveryService {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_HC_CBEMM_ELECTRICITY_METER_TYPE_UID,
            THING_BASE_CBEMM_ELECTRICITY_METER_TYPE_UID, THING_TEMPO_CBEMM_ELECTRICITY_METER_TYPE_UID,
            THING_EJP_CBEMM_ELECTRICITY_METER_TYPE_UID, THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID,
            THING_BASE_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID, THING_TEMPO_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID,
            THING_EJP_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID, THING_HC_CBETM_ELECTRICITY_METER_TYPE_UID,
            THING_BASE_CBETM_ELECTRICITY_METER_TYPE_UID, THING_TEMPO_CBETM_ELECTRICITY_METER_TYPE_UID,
            THING_EJP_CBETM_ELECTRICITY_METER_TYPE_UID, THING_LSMT_PROD_ELECTRICITY_METER_TYPE_UID,
            THING_LSMT_ELECTRICITY_METER_TYPE_UID, THING_LSMM_PROD_ELECTRICITY_METER_TYPE_UID,
            THING_LSMM_ELECTRICITY_METER_TYPE_UID);

    private static final int SCAN_DURATION_IN_S = 60;

    private final Logger logger = LoggerFactory.getLogger(TeleinfoDiscoveryService.class);
    private @Nullable TeleinfoAbstractControllerHandler controllerHandler;

    public TeleinfoDiscoveryService() {
        super(SCAN_DURATION_IN_S);
    }

    public TeleinfoDiscoveryService(TeleinfoAbstractControllerHandler controllerHandler) {
        super(SCAN_DURATION_IN_S);
        this.controllerHandler = controllerHandler;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void activate() {
        TeleinfoAbstractControllerHandler controllerHandlerRef = controllerHandler;
        if (controllerHandlerRef != null) {
            logger.debug("Teleinfo discovery: Activate {}", controllerHandlerRef.getThing().getUID());
        } else {
            logNullControllerHandler();
        }
    }

    @Override
    public void deactivate() {
        TeleinfoAbstractControllerHandler controllerHandlerRef = controllerHandler;
        if (controllerHandlerRef != null) {
            logger.debug("Teleinfo discovery: Deactivate {}", controllerHandlerRef.getThing().getUID());
        } else {
            logNullControllerHandler();
        }
    }

    @Override
    protected void startScan() {
        TeleinfoAbstractControllerHandler controllerHandlerRef = controllerHandler;
        if (controllerHandlerRef != null) {
            logger.debug("Teleinfo discovery: Start {}", controllerHandlerRef.getThing().getUID());

            // Start the search for new devices
            controllerHandlerRef.addListener(this);
        } else {
            logNullControllerHandler();
        }
    }

    @Override
    public synchronized void abortScan() {
        TeleinfoAbstractControllerHandler controllerHandlerRef = controllerHandler;
        if (controllerHandlerRef != null) {
            logger.debug("Teleinfo discovery: Abort {}", controllerHandlerRef.getThing().getUID());
            controllerHandlerRef.removeListener(this);
            super.abortScan();
        } else {
            logNullControllerHandler();
        }
    }

    @Override
    protected synchronized void stopScan() {
        TeleinfoAbstractControllerHandler controllerHandlerRef = controllerHandler;
        if (controllerHandlerRef != null) {
            logger.debug("Teleinfo discovery: Stop {}", controllerHandlerRef.getThing().getUID());
            controllerHandlerRef.removeListener(this);
            super.stopScan();
        } else {
            logNullControllerHandler();
        }
    }

    @Override
    public void onFrameReceived(Frame frame) {
        detectNewElectricityMeterFromReceivedFrame(frame);
    }

    private void detectNewElectricityMeterFromReceivedFrame(final Frame frameSample) {
        TeleinfoAbstractControllerHandler controllerHandlerRef = controllerHandler;
        if (controllerHandlerRef != null) {
            logger.debug("New eletricity meter detection from frame {}", frameSample);
            if (frameSample.get(Label.ADCO) == null && frameSample.get(Label.ADSC) == null) {
                throw new IllegalStateException("Missing ADCO or ADSC key");
            }

            String adco = frameSample.get(Label.ADCO) != null ? frameSample.get(Label.ADCO)
                    : frameSample.get(Label.ADSC);
            if (adco != null) {
                ThingUID thingUID = new ThingUID(getThingTypeUID(frameSample), adco,
                        controllerHandlerRef.getThing().getUID().getId());

                final Map<String, Object> properties = getThingProperties(adco);
                final String representationProperty = THING_ELECTRICITY_METER_PROPERTY_ADCO;
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withLabel("Teleinfo ADCO/ADSC " + adco).withThingType(getThingTypeUID(frameSample))
                        .withBridge(controllerHandlerRef.getThing().getUID())
                        .withRepresentationProperty(representationProperty).build();

                thingDiscovered(discoveryResult);
            }
        } else {
            logNullControllerHandler();
        }
    }

    private ThingTypeUID getThingTypeUID(final Frame teleinfoFrame) {
        ThingTypeUID thingTypeUID;
        try {
            thingTypeUID = teleinfoFrame.getType().getThingTypeUid();
        } catch (InvalidFrameException e) {
            throw new IllegalStateException("Frame type can not be evaluated");
        }
        if (thingTypeUID != null) {
            return thingTypeUID;
        } else {
            throw new IllegalStateException("Teleinfo frame type not supported: " + teleinfoFrame.getClass());
        }
    }

    private Map<String, Object> getThingProperties(String adco) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(THING_ELECTRICITY_METER_PROPERTY_ADCO, adco);

        return properties;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof TeleinfoAbstractControllerHandler) {
            controllerHandler = (TeleinfoAbstractControllerHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return controllerHandler;
    }

    private void logNullControllerHandler() {
        logger.warn("Null controller handler");
    }
}
