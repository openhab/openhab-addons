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
import org.openhab.binding.teleinfo.internal.reader.cbemm.FrameCbemmBaseOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.FrameCbemmEjpOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.FrameCbemmHcOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.FrameCbemmTempoOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc.FrameCbemmEvolutionIccBaseOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc.FrameCbemmEvolutionIccEjpOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc.FrameCbemmEvolutionIccHcOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc.FrameCbemmEvolutionIccTempoOption;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmLongBaseOption;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmLongEjpOption;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmLongHcOption;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmLongTempoOption;
import org.openhab.binding.teleinfo.internal.reader.common.FrameAdco;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoDiscoveryService} class is the service to discover a skeleton for controller handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
// @Component(service = DiscoveryService.class, immediate = false, configurationPid = "discovery.teleinfo")
public class TeleinfoDiscoveryService extends AbstractDiscoveryService implements TeleinfoControllerHandlerListener {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_HC_CBEMM_ELECTRICITY_METER_TYPE_UID,
            THING_BASE_CBEMM_ELECTRICITY_METER_TYPE_UID, THING_TEMPO_CBEMM_ELECTRICITY_METER_TYPE_UID,
            THING_EJP_CBEMM_ELECTRICITY_METER_TYPE_UID, THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID,
            THING_BASE_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID, THING_TEMPO_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID,
            THING_EJP_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID, THING_HC_CBETM_ELECTRICITY_METER_TYPE_UID,
            THING_BASE_CBETM_ELECTRICITY_METER_TYPE_UID, THING_TEMPO_CBETM_ELECTRICITY_METER_TYPE_UID,
            THING_EJP_CBETM_ELECTRICITY_METER_TYPE_UID).collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(TeleinfoDiscoveryService.class);
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
        detectNewElectricityMeterFromReceivedFrame(frame);
    }

    private void detectNewElectricityMeterFromReceivedFrame(final Frame frameSample) {
        logger.debug("New eletricity meter detection from frame {}", frameSample.getId());
        if (frameSample instanceof FrameAdco == false) {
            throw new IllegalStateException("Teleinfo frame type not supported: " + frameSample.getClass());
        }
        final FrameAdco frameAdco = (FrameAdco) frameSample;

        ThingUID thingUID = getThingUID(frameAdco);

        final Map<String, Object> properties = getThingProperties(thingUID.getThingTypeUID(), frameAdco);
        final String representationProperty = getRepresentationProperty(thingUID.getThingTypeUID(), frameAdco);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withLabel("Teleinfo ADCO " + frameAdco.getAdco()).withThingType(getThingTypeUID(frameAdco))
                .withBridge(controllerHandler.getThing().getUID()).withRepresentationProperty(representationProperty)
                .build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID getThingUID(final Frame teleinfoFrame) {
        if (teleinfoFrame instanceof FrameAdco == false) {
            throw new IllegalStateException("Teleinfo frame type not supported: " + teleinfoFrame.getClass());
        }
        final FrameAdco frameAdco = (FrameAdco) teleinfoFrame;

        return new ThingUID(getThingTypeUID(teleinfoFrame), frameAdco.getAdco(),
                controllerHandler.getThing().getUID().getId());
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

    private Map<String, Object> getThingProperties(final ThingTypeUID thingTypeId, final Frame teleinfoFrame) {
        Map<String, Object> properties = new HashMap<String, Object>();
        if (teleinfoFrame instanceof FrameAdco) {
            final FrameAdco frameAdco = (FrameAdco) teleinfoFrame;

            if (THING_BASE_CBEMM_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
                properties.put(THING_BASE_CBEMM_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());
            } else if (THING_HC_CBEMM_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
                properties.put(THING_HC_CBEMM_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());
            } else if (THING_TEMPO_CBEMM_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
                properties.put(THING_TEMPO_CBEMM_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());
            } else if (THING_EJP_CBEMM_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
                properties.put(THING_EJP_CBEMM_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());
            } else if (THING_BASE_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
                properties.put(THING_BASE_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());
            } else if (THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
                properties.put(THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());
            } else if (THING_TEMPO_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
                properties.put(THING_TEMPO_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());
            } else if (THING_EJP_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
                properties.put(THING_EJP_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());
            } else if (THING_BASE_CBETM_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
                properties.put(THING_BASE_CBETM_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());
            } else if (THING_HC_CBETM_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
                properties.put(THING_HC_CBETM_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());
            } else if (THING_TEMPO_CBETM_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
                properties.put(THING_TEMPO_CBETM_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());
            } else if (THING_EJP_CBETM_ELECTRICITY_METER_TYPE_UID.equals(thingTypeId)) {
                properties.put(THING_EJP_CBETM_ELECTRICITY_METER_PROPERTY_ADCO, frameAdco.getAdco());
            }

            return properties;
        }

        throw new IllegalStateException("Teleinfo frame type not supported: " + teleinfoFrame.getClass());
    }

    private String getRepresentationProperty(final ThingTypeUID thingTypeId, final Frame teleinfoFrame) {
        if (teleinfoFrame instanceof FrameAdco == false) {
            throw new IllegalStateException("Teleinfo frame type not supported: " + teleinfoFrame.getClass());
        }
        final FrameAdco frameAdco = (FrameAdco) teleinfoFrame;

        return frameAdco.getAdco() + AbstractUID.SEPARATOR + thingTypeId.getId();
    }
}
