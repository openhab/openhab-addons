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
package org.openhab.binding.intellicenter2.internal.discovery;

import static com.google.common.util.concurrent.Futures.getUnchecked;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.THING_TYPE_FEATURE;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.THING_TYPE_LIGHT;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.THING_TYPE_POOL;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.THING_TYPE_PUMP;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intellicenter2.internal.IntelliCenter2HandlerFactory;
import org.openhab.binding.intellicenter2.internal.handler.IntelliCenter2BridgeHandler;
import org.openhab.binding.intellicenter2.internal.model.Body;
import org.openhab.binding.intellicenter2.internal.model.Circuit;
import org.openhab.binding.intellicenter2.internal.model.GetConfiguration;
import org.openhab.binding.intellicenter2.internal.model.GetHardwareDefinition;
import org.openhab.binding.intellicenter2.internal.model.GetHardwareDefinition.Argument;
import org.openhab.binding.intellicenter2.internal.model.Panel;
import org.openhab.binding.intellicenter2.internal.model.Pump;
import org.openhab.binding.intellicenter2.internal.model.ResponseModel;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.ICProtocol;
import org.openhab.binding.intellicenter2.internal.protocol.ICResponse;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * Discovers Things from GetHardwareDefinition queries.
 *
 * @author Valdis Rigdon - Initial contribution
 *
 * @see GetHardwareDefinition
 */
@SuppressWarnings({ "UnstableApiUsage", "PMD.ForbiddenPackageUsageCheck" })
@NonNullByDefault
public class IntelliCenter2DiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(IntelliCenter2DiscoveryService.class);
    @Nullable
    private IntelliCenter2BridgeHandler bridgeHandler;

    public IntelliCenter2DiscoveryService() {
        super(IntelliCenter2HandlerFactory.SUPPORTED_THING_TYPES_UIDS, 30, false);
    }

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof IntelliCenter2BridgeHandler) {
            this.bridgeHandler = (IntelliCenter2BridgeHandler) thingHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void activate() {
        super.activate(new Hashtable<>());
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    @VisibleForTesting
    public void startScan() {
        if (bridgeHandler == null || !bridgeHandler.getProtocolFuture().isDone()) {
            return;
        }
        startScan(bridgeHandler.getProtocol());
    }

    private void startScan(final ICProtocol protocol) {
        for (Argument discoveryArg : Argument.values()) {
            logger.info("Looking for devices with argument {}", discoveryArg);
            final Future<ICResponse> future = protocol.submit(discoveryArg.getRequest());
            try {
                final GetHardwareDefinition hardware = new GetHardwareDefinition(future.get());
                for (Panel panel : hardware.getPanels()) {
                    if (discoveryArg == Argument.DEFAULT) {
                        for (final Body body : panel.getBodies()) {
                            // refresh the body to ensure that we have all the fields we want
                            final Body refreshedBody = new Body(
                                    getUnchecked(protocol.submit(body.createRefreshRequest())).getObjectList().get(0));
                            if (refreshedBody.isEnabled()) {
                                discoverPool(refreshedBody);
                            }
                        }
                    } else if (discoveryArg == Argument.CIRCUITS) {
                        for (Circuit circuit : panel.getCircuits()) {
                            if (circuit.isFeature()) {
                                discoverFeature(circuit);
                            }
                            if ("INTELLI".equals(circuit.getSubType())) {
                                discoverLight(circuit);
                            }
                        }
                    } else if (discoveryArg == Argument.PUMPS) {
                        for (Pump pump : panel.getPumps()) {
                            final Pump refreshed = new Pump(
                                    getUnchecked(protocol.submit(pump.createRefreshRequest())).getObjectList().get(0));
                            discoverPump(refreshed);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Unable to discover IntelliCenter2 hardware for {}", discoveryArg, e);
            }
        }
        try {
            final Future<ICResponse> future = protocol.submit(GetConfiguration.REQUEST);
            final GetConfiguration configuration = new GetConfiguration(future.get());
            for (Circuit featureCircuit : configuration.getFeatureCircuits()) {
                final Circuit refreshedCircuit = new Circuit(
                        getUnchecked(protocol.submit(featureCircuit.createRefreshRequest())).getObjectList().get(0));
                if (refreshedCircuit.isFeature()) {
                    discoverFeature(refreshedCircuit);
                }
            }
        } catch (Exception e) {
            logger.error("Unable to discover IntelliCenter2 hardware via GetConfiguration", e);
        }
    }

    private void discoverLight(Circuit circuit) {
        discoveryResult(THING_TYPE_LIGHT, circuit);
    }

    private void discoverFeature(Circuit circuit) {
        discoveryResult(THING_TYPE_FEATURE, circuit);
    }

    private void discoverPump(Pump pump) {
        discoveryResult(THING_TYPE_PUMP, pump);
    }

    private void discoverPool(Body body) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put(Attribute.VOL.name(), body.getVolume());
        discoveryResult(THING_TYPE_POOL, body, properties);
    }

    private void discoveryResult(ThingTypeUID bindingId, ResponseModel model) {
        discoveryResult(bindingId, model, new HashMap<>());
    }

    private void discoveryResult(ThingTypeUID bindingId, ResponseModel model, final Map<String, Object> properties) {
        logger.debug("Discovered object {}", model);
        if (bridgeHandler == null) {
            logger.error("discovered result with a null bridgeHandler {} {}", bindingId, model);
            return;
        }
        final ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        final ThingUID uid = new ThingUID(bindingId, bridgeUID, model.getObjectName());

        properties.put("vendor", "Pentair");
        properties.put("model", "IntelliCenter");
        properties.put(Attribute.OBJNAM.name(), model.getObjectName());
        properties.put(Attribute.SUBTYP.name(), model.getSubType());
        properties.put(Attribute.SNAME.name(), model.getSname());

        final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withLabel(model.getSname()).withProperties(properties)
                .withRepresentationProperty(Attribute.OBJNAM.name()).build();

        thingDiscovered(result);
    }
}
