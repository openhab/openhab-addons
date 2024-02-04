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
package org.openhab.binding.teleinfo.internal;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.teleinfo.internal.data.Frame;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractControllerHandler;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoControllerHandlerListener;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.Label;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoDiscoveryService} class is the service to discover a skeleton for controller handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = TeleinfoDiscoveryService.class)
@NonNullByDefault
public class TeleinfoDiscoveryService extends AbstractThingHandlerDiscoveryService<TeleinfoAbstractControllerHandler>
        implements TeleinfoControllerHandlerListener {

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

    public TeleinfoDiscoveryService() {
        super(TeleinfoAbstractControllerHandler.class, SCAN_DURATION_IN_S);
    }

    public TeleinfoDiscoveryService(TeleinfoAbstractControllerHandler controllerHandler) {
        this();
        setThingHandler(controllerHandler);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Teleinfo device background discovery");
        thingHandler.addListener(this);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop Teleinfo device background discovery");
        thingHandler.removeListener(this);
    }

    @Override
    protected void startScan() {
        logger.debug("Teleinfo discovery: Start {}", thingHandler.getThing().getUID());

        // Start the search for new devices
        thingHandler.addListener(this);
    }

    @Override
    public synchronized void abortScan() {
        logger.debug("Teleinfo discovery: Abort {}", thingHandler.getThing().getUID());
        thingHandler.removeListener(this);
        super.abortScan();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Teleinfo discovery: Stop {}", thingHandler.getThing().getUID());
        thingHandler.removeListener(this);
        super.stopScan();
    }

    @Override
    public void onFrameReceived(Frame frame) {
        detectNewElectricityMeterFromReceivedFrame(frame);
    }

    private void detectNewElectricityMeterFromReceivedFrame(final Frame frameSample) {
        logger.debug("New eletricity meter detection from frame {}", frameSample);
        if (frameSample.get(Label.ADCO) == null && frameSample.get(Label.ADSC) == null) {
            throw new IllegalStateException("Missing ADCO or ADSC key");
        }

        String adco = frameSample.get(Label.ADCO) != null ? frameSample.get(Label.ADCO) : frameSample.get(Label.ADSC);
        if (adco != null) {
            ThingUID thingUID = new ThingUID(getThingTypeUID(frameSample), adco,
                    thingHandler.getThing().getUID().getId());

            final Map<String, Object> properties = getThingProperties(adco);
            final String representationProperty = THING_ELECTRICITY_METER_PROPERTY_ADCO;
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withLabel("Teleinfo ADCO/ADSC " + adco).withThingType(getThingTypeUID(frameSample))
                    .withBridge(thingHandler.getThing().getUID()).withRepresentationProperty(representationProperty)
                    .build();

            thingDiscovered(discoveryResult);
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
}
