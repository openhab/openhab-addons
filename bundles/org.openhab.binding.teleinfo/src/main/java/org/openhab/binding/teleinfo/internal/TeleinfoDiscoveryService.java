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
package org.openhab.binding.teleinfo.internal;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.teleinfo.internal.dto.Frame;
import org.openhab.binding.teleinfo.internal.dto.cbemm.FrameCbemmBaseOption;
import org.openhab.binding.teleinfo.internal.dto.cbemm.FrameCbemmEjpOption;
import org.openhab.binding.teleinfo.internal.dto.cbemm.FrameCbemmHcOption;
import org.openhab.binding.teleinfo.internal.dto.cbemm.FrameCbemmTempoOption;
import org.openhab.binding.teleinfo.internal.dto.cbemm.evoicc.FrameCbemmEvolutionIccBaseOption;
import org.openhab.binding.teleinfo.internal.dto.cbemm.evoicc.FrameCbemmEvolutionIccEjpOption;
import org.openhab.binding.teleinfo.internal.dto.cbemm.evoicc.FrameCbemmEvolutionIccHcOption;
import org.openhab.binding.teleinfo.internal.dto.cbemm.evoicc.FrameCbemmEvolutionIccTempoOption;
import org.openhab.binding.teleinfo.internal.dto.cbetm.FrameCbetmLongBaseOption;
import org.openhab.binding.teleinfo.internal.dto.cbetm.FrameCbetmLongEjpOption;
import org.openhab.binding.teleinfo.internal.dto.cbetm.FrameCbetmLongHcOption;
import org.openhab.binding.teleinfo.internal.dto.cbetm.FrameCbetmLongTempoOption;
import org.openhab.binding.teleinfo.internal.dto.common.FrameAdco;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractControllerHandler;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoControllerHandlerListener;
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

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_HC_CBEMM_ELECTRICITY_METER_TYPE_UID,
            THING_BASE_CBEMM_ELECTRICITY_METER_TYPE_UID, THING_TEMPO_CBEMM_ELECTRICITY_METER_TYPE_UID,
            THING_EJP_CBEMM_ELECTRICITY_METER_TYPE_UID, THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID,
            THING_BASE_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID, THING_TEMPO_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID,
            THING_EJP_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID, THING_HC_CBETM_ELECTRICITY_METER_TYPE_UID,
            THING_BASE_CBETM_ELECTRICITY_METER_TYPE_UID, THING_TEMPO_CBETM_ELECTRICITY_METER_TYPE_UID,
            THING_EJP_CBETM_ELECTRICITY_METER_TYPE_UID).collect(Collectors.toSet());

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
    public void onFrameReceived(TeleinfoAbstractControllerHandler controllerHandler, Frame frame) {
        detectNewElectricityMeterFromReceivedFrame(frame);
    }

    private void detectNewElectricityMeterFromReceivedFrame(final Frame frameSample) {
        TeleinfoAbstractControllerHandler controllerHandlerRef = controllerHandler;
        if (controllerHandlerRef != null) {
            logger.debug("New eletricity meter detection from frame {}", frameSample.getId());
            if (!(frameSample instanceof FrameAdco)) {
                throw new IllegalStateException("Teleinfo frame type not supported: " + frameSample.getClass());
            }
            final FrameAdco frameAdco = (FrameAdco) frameSample;

            ThingUID thingUID = new ThingUID(getThingTypeUID(frameAdco), frameAdco.getAdco(),
                    controllerHandlerRef.getThing().getUID().getId());

            final Map<String, Object> properties = getThingProperties(frameAdco);
            final String representationProperty = THING_ELECTRICITY_METER_PROPERTY_ADCO;
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withLabel("Teleinfo ADCO " + frameAdco.getAdco()).withThingType(getThingTypeUID(frameAdco))
                    .withBridge(controllerHandlerRef.getThing().getUID())
                    .withRepresentationProperty(representationProperty).build();

            thingDiscovered(discoveryResult);
        } else {
            logNullControllerHandler();
        }
    }

    private ThingTypeUID getThingTypeUID(final Frame teleinfoFrame) {
        if (teleinfoFrame instanceof FrameCbemmHcOption) {
            return THING_HC_CBEMM_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameCbemmBaseOption) {
            return THING_BASE_CBEMM_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameCbemmEjpOption) {
            return THING_EJP_CBEMM_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameCbemmTempoOption) {
            return THING_TEMPO_CBEMM_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameCbemmEvolutionIccHcOption) {
            return THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameCbemmEvolutionIccBaseOption) {
            return THING_BASE_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameCbemmEvolutionIccEjpOption) {
            return THING_EJP_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameCbemmEvolutionIccTempoOption) {
            return THING_TEMPO_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameCbetmLongHcOption) {
            return THING_HC_CBETM_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameCbetmLongBaseOption) {
            return THING_BASE_CBETM_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameCbetmLongEjpOption) {
            return THING_EJP_CBETM_ELECTRICITY_METER_TYPE_UID;
        } else if (teleinfoFrame instanceof FrameCbetmLongTempoOption) {
            return THING_TEMPO_CBETM_ELECTRICITY_METER_TYPE_UID;
        } else {
            throw new IllegalStateException("Teleinfo frame type not supported: " + teleinfoFrame.getClass());
        }
    }

    private Map<String, Object> getThingProperties(final Frame teleinfoFrame) {
        Map<String, Object> properties = new HashMap<String, Object>();
        if (teleinfoFrame instanceof FrameAdco) {
            final FrameAdco frameAdco = (FrameAdco) teleinfoFrame;
            properties.put(THING_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());

            return properties;
        }

        throw new IllegalStateException("Teleinfo frame type not supported: " + teleinfoFrame.getClass());
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
