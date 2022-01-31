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
package org.openhab.binding.dali.internal.handler;

import static org.openhab.binding.dali.internal.DaliBindingConstants.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dali.internal.protocol.DaliAddress;
import org.openhab.binding.dali.internal.protocol.DaliDAPCCommand;
import org.openhab.binding.dali.internal.protocol.DaliResponse;
import org.openhab.binding.dali.internal.protocol.DaliResponse.NumericMask;
import org.openhab.binding.dali.internal.protocol.DaliStandardCommand;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DaliRgbHandler} handles commands for things of type RGB.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliRgbHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(DaliRgbHandler.class);
    private @Nullable List<Integer> outputs;

    public DaliRgbHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();

        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else {
            updateStatus(ThingStatus.ONLINE);
        }

        outputs = List.of(((BigDecimal) this.thing.getConfiguration().get(TARGET_ID_R)).intValueExact(),
                ((BigDecimal) this.thing.getConfiguration().get(TARGET_ID_G)).intValueExact(),
                ((BigDecimal) this.thing.getConfiguration().get(TARGET_ID_B)).intValueExact());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (CHANNEL_COLOR.equals(channelUID.getId())) {
                boolean queryDeviceState = false;

                if (command instanceof HSBType) {
                    PercentType[] rgb = ((HSBType) command).toRGB();

                    for (int i = 0; i < 3; i++) {
                        byte dimmValue = (byte) ((rgb[i].floatValue() * DALI_SWITCH_100_PERCENT) / 100);
                        getBridgeHandler().sendCommand(
                                new DaliDAPCCommand(DaliAddress.createShortAddress(outputs.get(i)), dimmValue));
                    }
                } else if (command instanceof OnOffType) {
                    if ((OnOffType) command == OnOffType.ON) {
                        for (Integer output : outputs) {
                            getBridgeHandler().sendCommand(new DaliDAPCCommand(DaliAddress.createShortAddress(output),
                                    (byte) DALI_SWITCH_100_PERCENT));
                        }
                    } else {
                        for (Integer output : outputs) {
                            getBridgeHandler().sendCommand(
                                    DaliStandardCommand.createOffCommand(DaliAddress.createShortAddress(output)));
                        }
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    if ((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE) {
                        for (Integer output : outputs) {
                            getBridgeHandler().sendCommand(
                                    DaliStandardCommand.createUpCommand(DaliAddress.createShortAddress(output)));
                        }
                    } else {
                        for (Integer output : outputs) {
                            getBridgeHandler().sendCommand(
                                    DaliStandardCommand.createDownCommand(DaliAddress.createShortAddress(output)));
                        }
                    }

                    queryDeviceState = true;
                } else if (command instanceof RefreshType) {
                    queryDeviceState = true;
                }

                if (queryDeviceState) {
                    CompletableFuture<@Nullable NumericMask> responseR = getBridgeHandler()
                            .sendCommandWithResponse(
                                    DaliStandardCommand.createQueryActualLevelCommand(
                                            DaliAddress.createShortAddress(outputs.get(0))),
                                    DaliResponse.NumericMask.class);
                    CompletableFuture<@Nullable NumericMask> responseG = getBridgeHandler()
                            .sendCommandWithResponse(
                                    DaliStandardCommand.createQueryActualLevelCommand(
                                            DaliAddress.createShortAddress(outputs.get(1))),
                                    DaliResponse.NumericMask.class);
                    CompletableFuture<@Nullable NumericMask> responseB = getBridgeHandler()
                            .sendCommandWithResponse(
                                    DaliStandardCommand.createQueryActualLevelCommand(
                                            DaliAddress.createShortAddress(outputs.get(2))),
                                    DaliResponse.NumericMask.class);

                    CompletableFuture.allOf(responseR, responseG, responseB).thenAccept(x -> {
                        @Nullable
                        NumericMask r = responseR.join(), g = responseG.join(), b = responseB.join();
                        if (r != null && !r.mask && g != null && !g.mask && b != null && !b.mask) {
                            Integer rValue = r.value != null ? r.value : 0;
                            Integer gValue = g.value != null ? g.value : 0;
                            Integer bValue = b.value != null ? b.value : 0;
                            updateState(channelUID,
                                    HSBType.fromRGB((int) (rValue.floatValue() * 255 / DALI_SWITCH_100_PERCENT),
                                            (int) (gValue.floatValue() * 255 / DALI_SWITCH_100_PERCENT),
                                            (int) (bValue.floatValue() * 255 / DALI_SWITCH_100_PERCENT)));
                        }
                    }).exceptionally(e -> {
                        logger.warn("Error querying device status: {}", e.getMessage());
                        return null;
                    });
                }
            }
        } catch (DaliException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected DaliserverBridgeHandler getBridgeHandler() throws DaliException {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            throw new DaliException("No bridge was found");
        }

        BridgeHandler handler = bridge.getHandler();
        if (handler == null) {
            throw new DaliException("No handler was found");
        }

        return (DaliserverBridgeHandler) handler;
    }
}
