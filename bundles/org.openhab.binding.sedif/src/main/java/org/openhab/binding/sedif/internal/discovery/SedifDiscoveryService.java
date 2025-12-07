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
package org.openhab.binding.sedif.internal.discovery;

import static org.openhab.binding.sedif.internal.constants.SedifBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sedif.internal.api.SedifHttpApi;
import org.openhab.binding.sedif.internal.dto.Contract;
import org.openhab.binding.sedif.internal.dto.ContractDetail;
import org.openhab.binding.sedif.internal.dto.ContractDetail.CompteInfo;
import org.openhab.binding.sedif.internal.handler.BridgeSedifWebHandler;
import org.openhab.binding.sedif.internal.helpers.SedifListener;
import org.openhab.binding.sedif.internal.types.SedifException;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SedifDiscoveryService} class is the service to discover a skeleton for controller handlers.
 *
 * @author Laurent Arnal - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SedifDiscoveryService.class)
@NonNullByDefault
public class SedifDiscoveryService extends AbstractThingHandlerDiscoveryService<BridgeSedifWebHandler>
        implements SedifListener {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_METER);
    private static final int SCAN_DURATION_IN_S = 10;

    private final Logger logger = LoggerFactory.getLogger(SedifDiscoveryService.class);

    public SedifDiscoveryService() {
        super(BridgeSedifWebHandler.class, SUPPORTED_THING_TYPES, SCAN_DURATION_IN_S);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        super.setThingHandler(handler);
        thingHandler.addListener(this);
    }

    @Override
    public void deactivate() {
        thingHandler.removeListener(this);
        super.deactivate();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startScan() {
        logger.debug("Sedif discovery: Start {}", thingHandler.getThing().getUID());

        if (!(getThingHandler() instanceof BridgeSedifWebHandler bridgeHandler)) {
            return;
        }

        SedifHttpApi api = bridgeHandler.getSedifApi();
        Map<String, Contract> contracts = api.getAllContracts();
        for (Contract contract : contracts.values()) {
            detectNewWaterMeterFromContract(contract);
        }
    }

    @Override
    public synchronized void abortScan() {
        logger.debug("Sedif discovery: Abort {}", thingHandler.getThing().getUID());
        super.abortScan();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Sedif discovery: Stop {}", thingHandler.getThing().getUID());
        super.stopScan();
    }

    @Override
    public void onContractInit(Contract contract) {
        detectNewWaterMeterFromContract(contract);
    }

    private void detectNewWaterMeterFromContract(final Contract contract) {
        logger.trace("New water meter detection from contract {}", contract);

        if (!(getThingHandler() instanceof BridgeSedifWebHandler bridgeHandler)) {
            return;
        }

        SedifHttpApi api = bridgeHandler.getSedifApi();

        try {
            String contractId = contract.id;
            if (contractId != null) {
                ContractDetail contractDetail = api.getContractDetails(contractId);

                if (contractDetail != null) {
                    for (CompteInfo compteInfo : contractDetail.compteInfo) {
                        ThingTypeUID tpUid = THING_TYPE_METER;
                        ThingUID thingUID = new ThingUID(tpUid, compteInfo.numCompteur,
                                thingHandler.getThing().getUID().getId());

                        Map<String, Object> properties = new HashMap<>();
                        properties.put(PROPERTY_ELMA, compteInfo.eLma);
                        properties.put(PROPERTY_ELMB, compteInfo.eLmb);
                        properties.put(PROPERTY_NUM_METER, compteInfo.numCompteur);
                        properties.put(PROPERTY_ID_PDS, compteInfo.idPds);
                        properties.put(PROPERTY_CONTRACT_ID, Objects.requireNonNull(contract.name));

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                .withProperties(properties).withLabel("Water Meter " + compteInfo.numCompteur)
                                .withThingType(tpUid).withBridge(thingHandler.getThing().getUID())
                                .withRepresentationProperty(PROPERTY_NUM_METER).build();

                        thingDiscovered(discoveryResult);
                    }
                }
            }
        } catch (SedifException ex) {
            logger.debug("Unable to detect water meter for contract {}", contract, ex);
        }
    }
}
