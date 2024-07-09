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
package org.openhab.binding.siemenshvac.internal.handler;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.converter.ConverterException;
import org.openhab.binding.siemenshvac.internal.converter.ConverterFactory;
import org.openhab.binding.siemenshvac.internal.converter.ConverterTypeException;
import org.openhab.binding.siemenshvac.internal.converter.TypeConverter;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataDataPoint;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataRegistry;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacCallback;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacConnector;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacRequestListener.ErrorSource;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link SiemensHvacHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent ARNAL - Initial contribution
 */
@NonNullByDefault
public class SiemensHvacHandlerImpl extends BaseThingHandler {

    private Lock lockObj = new ReentrantLock();

    private final Logger logger = LoggerFactory.getLogger(SiemensHvacHandlerImpl.class);

    private @Nullable ScheduledFuture<?> pollingJob = null;

    private final @Nullable SiemensHvacConnector hvacConnector;
    private final @Nullable SiemensHvacMetadataRegistry metaDataRegistry;
    private final ChannelTypeRegistry channelTypeRegistry;

    private long lastWrite = 0;

    public SiemensHvacHandlerImpl(Thing thing, @Nullable SiemensHvacConnector hvacConnector,
            @Nullable SiemensHvacMetadataRegistry metaDataRegistry, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);

        this.hvacConnector = hvacConnector;
        this.metaDataRegistry = metaDataRegistry;
        this.channelTypeRegistry = channelTypeRegistry;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> lcPollingJob = pollingJob;
        if (lcPollingJob != null) {
            lcPollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private void pollingCode() {
        Bridge lcBridge = getBridge();

        if (lcBridge == null) {
            return;
        }

        if (lcBridge.getStatus() == ThingStatus.OFFLINE) {
            if (!ThingStatusDetail.COMMUNICATION_ERROR.equals(lcBridge.getStatusInfo().getStatusDetail())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                return;
            }
        }

        if (lcBridge.getStatus() != ThingStatus.ONLINE) {
            if (!ThingStatusDetail.COMMUNICATION_ERROR.equals(lcBridge.getStatusInfo().getStatusDetail())) {
                logger.debug("Bridge is not ready, don't enter polling for now!");
                return;
            }
        }

        long start = System.currentTimeMillis();
        var chList = this.getThing().getChannels();

        SiemensHvacConnector lcHvacConnector = hvacConnector;
        if (lcHvacConnector != null) {
            int previousRequestCount = lcHvacConnector.getRequestCount();
            int previousErrorCount = lcHvacConnector.getErrorCount();

            logger.debug("readChannels:");
            for (Channel channel : chList) {
                readChannel(channel);
            }

            logger.debug("WaitAllPendingRequest:Start waiting()");
            lcHvacConnector.waitAllPendingRequest();
            long end = System.currentTimeMillis();
            logger.debug("WaitAllPendingRequest:All request done(): {}", (end - start) / 1000.00);

            int newRequestCount = lcHvacConnector.getRequestCount();
            int newErrorCount = lcHvacConnector.getErrorCount();

            int requestCount = newRequestCount - previousRequestCount;
            int errorCount = newErrorCount - previousErrorCount;

            double errorRate = (double) errorCount / requestCount * 100.00;

            if (errorRate > 50) {
                SiemensHvacBridgeThingHandler bridgeHandler = (SiemensHvacBridgeThingHandler) lcBridge.getHandler();

                if (lcHvacConnector.getErrorSource() == ErrorSource.ErrorBridge) {
                    if (bridgeHandler != null) {
                        bridgeHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                String.format("Communication ErrorRate to gateway is too high: %f", errorRate));
                    }
                } else if (lcHvacConnector.getErrorSource() == ErrorSource.ErrorThings) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            String.format("Communication ErrorRate to thing is too high: %f", errorRate));
                }
            } else {
                updateStatus(ThingStatus.ONLINE);

                SiemensHvacBridgeThingHandler bridgeHandler = (SiemensHvacBridgeThingHandler) lcBridge.getHandler();

                // Automatically recover from communication errors if errorRate is ok.
                if (bridgeHandler != null) {
                    if (ThingStatusDetail.COMMUNICATION_ERROR
                            .equals(bridgeHandler.getThing().getStatusInfo().getStatusDetail())) {
                        bridgeHandler.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
                    }
                }
            }

            lcHvacConnector.displayRequestStats();
        }
    }

    private void readChannel(Channel channel) {
        ThingHandlerCallback cb = this.getCallback();
        boolean isLink = false;

        if (cb != null) {
            isLink = cb.isChannelLinked(channel.getUID());
        }

        if (!isLink) {
            return;
        }

        logger.debug("readChannel: {}", channel.getDescription());

        ChannelType tp = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());

        if (tp == null) {
            return;
        }

        String id = channel.getProperties().get("id");
        String uid = channel.getUID().getId();
        String type = tp.getItemType();

        if (id == null) {
            id = (String) channel.getConfiguration().getProperties().get("id");
        }

        if (id == null) {
            logger.debug("pollingCode: Id is null {} ", channel);
            return;
        }
        if (type == null) {
            logger.debug("pollingCode: type is null {} ", channel);
            return;
        }

        readDp(id, uid, tp, type, true);
    }

    public void decodeReadDp(@Nullable JsonObject response, @Nullable String uid, @Nullable String dp, ChannelType tp,
            @Nullable String type) {
        SiemensHvacMetadataRegistry lcMetaDataRegistry = metaDataRegistry;
        if (lcMetaDataRegistry == null) {
            return;
        }

        if (response != null && response.has("Data")) {
            JsonObject subResult = (JsonObject) response.get("Data");

            String updateKey = "" + uid;

            String typer = null;

            if (subResult.has("Type")) {
                typer = subResult.get("Type").getAsString().trim();
            }

            try {
                if (typer != null) {
                    TypeConverter converter = ConverterFactory.getConverter(typer);

                    Locale local = lcMetaDataRegistry.getUserLocale();
                    if (local == null) {
                        local = Locale.getDefault();
                    }

                    State state = converter.convertFromBinding(subResult, tp, local);
                    updateState(updateKey, state);
                }
            } catch (ConverterTypeException ex) {
                logger.warn("{}, for uid : {}, please check the item type", ex.getMessage(), uid);
            } catch (ConverterException ex) {
                logger.warn("{}, for uid: {}, please check the item type", ex.getMessage(), uid);
            }

        }
    }

    private void readDp(String dp, String uid, ChannelType tp, String type, boolean async) {
        SiemensHvacConnector lcHvacConnector = hvacConnector;

        if ("-1".equals(dp)) {
            return;
        }

        try {
            lockObj.lock();

            logger.trace("Start read: {}", dp);
            String request = "api/menutree/read_datapoint.json?Id=" + dp;

            logger.debug("siemensHvac:ReadDp:DoRequest(): {}", request);

            if (async) {
                if (lcHvacConnector != null) {
                    lcHvacConnector.doRequest(request, new SiemensHvacCallback() {

                        @Override
                        public void execute(java.net.URI uri, int status, @Nullable Object response) {
                            // prevent async read if we just write so we have no overlaps
                            long now = System.currentTimeMillis();
                            if (now - lastWrite < 5000) {
                                return;
                            }

                            logger.trace("End read: {}", dp);

                            if (response instanceof JsonObject jsonResponse) {
                                decodeReadDp(jsonResponse, uid, dp, tp, type);
                            }
                        }
                    });
                }
            } else {
                if (lcHvacConnector != null) {
                    JsonObject js = lcHvacConnector.doRequest(request);
                    decodeReadDp(js, uid, dp, tp, type);
                }
            }
        } finally {
            logger.trace("End read: {}", dp);
            lockObj.unlock();
        }
    }

    private void writeDp(String dp, Type dpVal, ChannelType tp, String type) {
        SiemensHvacConnector lcHvacConnector = hvacConnector;

        if (lcHvacConnector != null) {
            lcHvacConnector.displayRequestStats();
        }

        if ("-1".equals(dp)) {
            return;
        }

        try {
            lockObj.lock();
            logger.trace("Start write: {}", dp);
            lastWrite = System.currentTimeMillis();

            Object valUpdate = "0";

            try {
                TypeConverter converter = ConverterFactory.getConverter(type);

                valUpdate = converter.convertToBinding(dpVal, tp);
                if (valUpdate != null) {
                    String request = String.format("api/menutree/write_datapoint.json?Id=%s&Value=%s&Type=%s", dp,
                            valUpdate, type);

                    if (lcHvacConnector != null) {
                        logger.trace("Write request for: {} ", valUpdate);
                        JsonObject response = lcHvacConnector.doRequest(request);

                        logger.trace("Write request response: {} ", response);
                    }

                } else {
                    logger.debug("Failed to get converted state from datapoint '{}'", dp);
                }
            } catch (ConverterTypeException ex) {
                logger.warn("{}, please check the item type and the commands in your scripts", ex.getMessage());
            } catch (ConverterException ex) {
                logger.warn("{}, please check the item type and the commands in your scripts", ex.getMessage());
            }
        } finally {
            logger.debug("End write: {}", dp);
            lockObj.unlock();
        }
    }

    private Command applyState(ChannelType tp, Command command) {
        StateDescription sd = tp.getState();
        Command result = command;

        if (sd != null) {
            BigDecimal maxb = sd.getMaximum();
            BigDecimal minb = sd.getMinimum();
            BigDecimal step = sd.getStep();
            boolean doMods = false;

            if (command instanceof DecimalType decimalCommand) {
                double v1 = decimalCommand.doubleValue();

                if (step != null) {
                    doMods = true;
                    int divider = 1;

                    if (step.doubleValue() == 0.5) {
                        divider = 2;
                    } else if (step.doubleValue() == 0.1) {
                        divider = 10;
                    } else if (step.doubleValue() == 0.02) {
                        divider = 50;
                    } else if (step.doubleValue() == 0.01) {
                        divider = 100;
                    }
                    v1 = v1 * divider;
                    v1 = Math.floor(v1);
                    v1 = v1 / divider;
                }

                if (minb != null && v1 < minb.floatValue()) {
                    doMods = true;
                    v1 = minb.floatValue();
                }
                if (maxb != null && v1 > maxb.floatValue()) {
                    doMods = true;
                    v1 = maxb.floatValue();
                }

                if (doMods) {
                    result = new DecimalType(v1);
                }
            }
        }
        return result;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SiemensHvacMetadataRegistry lcMetaDataRegistry = metaDataRegistry;
        logger.debug("handleCommand");
        if (command instanceof RefreshType) {
            var channel = this.getThing().getChannel(channelUID);
            if (channel != null) {
                readChannel(channel);
            }
        } else {
            Channel channel = getThing().getChannel(channelUID);
            if (channel == null) {
                return;
            }

            Command commandVar = command;
            ChannelType tp = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());

            if (tp == null) {
                return;
            }

            String type = tp.getItemType();
            String dptType = "";
            String id = channel.getProperties().get("id");
            SiemensHvacMetadataDataPoint md = null;

            if (id == null) {
                id = (String) channel.getConfiguration().getProperties().get("id");
            }

            if (lcMetaDataRegistry != null) {
                md = (SiemensHvacMetadataDataPoint) lcMetaDataRegistry.getDptMap(id);
                if (md != null) {
                    id = "" + md.getId();
                    dptType = md.getDptType();
                }
            }

            if (command instanceof State commandState) {
                commandVar = applyState(tp, commandVar);
                this.updateState(channelUID, commandState);
            }

            if (id != null && type != null) {
                writeDp(id, commandVar, tp, dptType);
            }
        }
    }
}
