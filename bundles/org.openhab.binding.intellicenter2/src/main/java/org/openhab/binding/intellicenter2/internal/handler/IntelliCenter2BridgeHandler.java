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
package org.openhab.binding.intellicenter2.internal.handler;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intellicenter2.internal.IntelliCenter2Configuration;
import org.openhab.binding.intellicenter2.internal.discovery.IntelliCenter2DiscoveryService;
import org.openhab.binding.intellicenter2.internal.model.SystemInfo;
import org.openhab.binding.intellicenter2.internal.protocol.ICProtocol;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Handler for an IntelliCenter2 bridge, or system. This contains a connection to IntelliCenter.
 *
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class IntelliCenter2BridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(IntelliCenter2BridgeHandler.class);

    private final IntelliCenter2Configuration config;
    private final SettableFuture<ICProtocol> protocolFuture;

    @Nullable
    private SystemInfo systemInfo;

    public IntelliCenter2BridgeHandler(Bridge bridge) {
        this(bridge, null);
    }

    public IntelliCenter2BridgeHandler(Bridge bridge, @Nullable IntelliCenter2Configuration config) {
        super(bridge);
        this.config = config == null ? getConfigAs(IntelliCenter2Configuration.class) : config;
        this.protocolFuture = SettableFuture.create();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            try {
                logger.debug("Attempting bridge connection to {}", config);
                final ICProtocol protocol = new ICProtocol(config);
                protocolFuture.set(protocol);
                systemInfo = protocol.getSystemInfo();
                logger.debug("Connected to IntelliCenter2 {}", systemInfo);
                SystemInfo si = systemInfo;
                if (si != null) {
                    updateProperty(Thing.PROPERTY_MODEL_ID, "IntelliCenter");
                    updateProperty("mode", si.getMode());
                    updateProperty("propertyName", si.getPropertyName());
                    updateProperty("version", si.getVersion());
                    updateProperty("intellicenterVersion", si.getIntellicenterVersion());
                    if (!si.getIntellicenterVersion().equals("1.064")) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, String.format(
                                "Running a non-supported version of IntelliCenter: %s", si.getIntellicenterVersion()));
                        return;
                    }
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Unable to get system info from IntelliCenter2.");
                }
            } catch (UnknownHostException e) {
                updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("unknown host name: %s", config.hostname));
            } catch (IOException e) {
                updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("Unable to connect to host %s", config.hostname));
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public ListenableFuture<ICProtocol> getProtocolFuture() {
        return protocolFuture;
    }

    public ICProtocol getProtocol() {
        return Futures.getUnchecked(protocolFuture);
    }

    public Unit<Temperature> getTemperatureUnits() {
        if (systemInfo != null) {
            return systemInfo.isMetricSystem() ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT;
        }
        throw new IllegalStateException("systemInfo was not set yet.");
    }

    @Override
    public void dispose() {
        scheduler.execute(() -> {
            if (protocolFuture.isDone()) {
                try {
                    getProtocol().close();
                } catch (Exception ignored) {
                }
            }
            updateStatus(ThingStatus.OFFLINE);
        });
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(IntelliCenter2DiscoveryService.class);
    }
}
