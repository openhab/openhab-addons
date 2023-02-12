/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataDataPoint;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataRegistry;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacCallback;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacConnector;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link SiemensHvacHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent ARNAL - Initial contribution
 */
@NonNullByDefault
public class SiemensHvacHandlerImpl extends BaseThingHandler implements SiemensHvacHandler {

    private Lock lockObj = new ReentrantLock();

    private final Logger logger = LoggerFactory.getLogger(SiemensHvacHandlerImpl.class);

    private @Nullable ScheduledFuture<?> pollingJob = null;

    private final @Nullable SiemensHvacConnector hvacConnector;
    private final @Nullable SiemensHvacMetadataRegistry metaDataRegistry;
    private final ChannelTypeRegistry channelTypeRegistry;

    public SiemensHvacHandlerImpl(Thing thing, @Nullable SiemensHvacConnector hvacConnector,
            @Nullable SiemensHvacMetadataRegistry metaDataRegistry, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);

        this.hvacConnector = hvacConnector;
        this.metaDataRegistry = metaDataRegistry;
        this.channelTypeRegistry = channelTypeRegistry;

        logger.debug("===========================================================");
        logger.debug("Siemens HVac");
        logger.debug("===========================================================");
    }

    @Override
    public void initialize() {

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            boolean thingReachable = true;
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> lcPollingJob = pollingJob;
        if (lcPollingJob != null) {
            lcPollingJob.cancel(true);
        }
    }

    private void pollingCode() {
        var chList = this.getThing().getChannels();
        for (Channel channel : chList) {
            ReadChannel(channel);
        }
    }

    private void ReadChannel(Channel channel) {
        logger.debug("{}", channel.getDescription());

        ThingHandlerCallback cb = this.getCallback();
        boolean isLink = false;

        if (cb != null) {
            isLink = cb.isChannelLinked(channel.getUID());
        }

        if (!isLink) {
            return;
        }

        ChannelType tp = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());

        if (tp == null) {
            return;
        }

        String Id = channel.getProperties().get("id");
        String uid = channel.getUID().getId();
        String type = tp.getItemType();

        if (Id == null) {
            Id = (String) channel.getConfiguration().getProperties().get("id");
        }

        if (Id == null) {
            logger.debug("poolingCode : Id is null {} ", channel);
            return;
        }
        if (type == null) {
            logger.debug("poolingCode : type is null {} ", channel);
            return;
        }
        ReadDp(Id, uid, type, true);
        logger.debug("{}", isLink);
    }

    public void DecodeReadDp(@Nullable JsonObject response, @Nullable String uid, @Nullable String dp,
            @Nullable String type) {
        if (response != null && response.has("Data")) {
            JsonObject subResult = (JsonObject) response.get("Data");

            String updateKey = "" + uid;
            String typer = "";
            JsonElement value = null;
            JsonElement enumValue = null;

            if (subResult.has("Type")) {
                typer = subResult.get("Type").getAsString().trim();
            }
            if (subResult.has("Value")) {
                value = subResult.get("Value");
            }
            if (subResult.has("EnumValue")) {
                enumValue = subResult.get("EnumValue");
            }

            if (value == null) {
                return;
            }

            if (type == null) {
                logger.debug("siemensHvac:ReadDP:null type {}", dp);
            }

            if (("Numeric").equals(typer)) {
                updateState(updateKey, new DecimalType(value.getAsDouble()));
            } else if ("Enumeration".equals(typer)) {
                if (enumValue != null) {
                    updateState(updateKey, new DecimalType(enumValue.getAsInt()));
                }
            } else if ("Text".equals(typer)) {
                updateState(updateKey, new StringType(value.getAsString()));
            } else if ("RadioButton".equals(typer)) {
                updateState(updateKey, new StringType(value.getAsString()));
            } else if ("DayOfTime".equals(typer) || "DateTime".equals(typer)) {
                try {
                    SimpleDateFormat dtf = new SimpleDateFormat("EEEE, d. MMMM yyyy hh:mm"); // first example
                    ZonedDateTime zdt = dtf.parse(value.getAsString()).toInstant().atZone(ZoneId.systemDefault());
                    updateState(updateKey, new DateTimeType(zdt));
                } catch (ParseException ex) {
                    logger.debug("error decoding date!");
                }
            } else {
                updateState(updateKey, new StringType(value.getAsString()));
            }

        }
    }

    private void ReadDp(String dp, String uid, String type, boolean async) {
        SiemensHvacConnector lcHvacConnector = hvacConnector;

        if ("-1".equals(dp)) {
            return;
        }

        try {
            lockObj.lock();
            logger.debug("Start read : {}", dp);
            String request = "api/menutree/read_datapoint.json?Id=" + dp;

            logger.debug("siemensHvac:ReadDp:DoRequest(): {}", request);

            if (async) {
                if (lcHvacConnector != null) {
                    lcHvacConnector.DoRequest(request, new SiemensHvacCallback() {

                        @Override
                        public void execute(java.net.URI uri, int status, @Nullable Object response) {
                            if (response instanceof JsonObject) {
                                DecodeReadDp((JsonObject) response, uid, dp, type);
                            }
                        }
                    });
                }
            } else {
                if (lcHvacConnector != null) {
                    JsonObject js = lcHvacConnector.DoRequest(request, null);
                    DecodeReadDp(js, uid, dp, type);
                }
            }
        } catch (Exception e) {
            logger.error("siemensHvac:ReadDp:Error during dp reading: {} ; {}", dp, e.getLocalizedMessage());
            // Reset sessionId so we redone _auth on error
        } finally {
            logger.debug("End read : {}", dp);
            lockObj.unlock();

        }
    }

    private void WriteDp(String dp, Type dpVal, String type) {
        SiemensHvacConnector lcHvacConnector = hvacConnector;
        if ("-1".equals(dp)) {
            return;
        }

        try {
            lockObj.lock();
            String valUpdate = "0";
            String valUpdateEnum = "";

            if (dpVal instanceof PercentType) {
                PercentType pct = (PercentType) dpVal;
                valUpdate = pct.toString();
            } else if (dpVal instanceof DecimalType) {
                DecimalType bdc = (DecimalType) dpVal;
                valUpdate = bdc.toString();
            } else if (dpVal instanceof StringType) {
                StringType bdc = (StringType) dpVal;
                valUpdate = bdc.toString();

                if (("Enumeration").equals(type)) {
                    String[] valuesUpdateDp = valUpdate.split(":");
                    valUpdateEnum = valuesUpdateDp[0];

                    // For enumeration, we always update using the raw value
                    valUpdate = valUpdateEnum;
                }
            }

            String request = String.format("api/menutree/write_datapoint.json?Id=%s&Value=%s&Type=%s", dp, valUpdate,
                    type);

            if (lcHvacConnector != null) {
                logger.debug("Write request for : {} ", valUpdate);
                JsonObject response = lcHvacConnector.DoRequest(request, null);

                logger.debug("Write request response : {} ", response);
            }

        } catch (Exception e) {
            logger.error("siemensHvac:ReadDp:Error during dp reading: {} ; {}", dp, e.getLocalizedMessage());
            // Reset sessionId so we redone _auth on error
        } finally {
            logger.debug("End write : {}", dp);
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

            if (command instanceof DecimalType) {
                DecimalType bdc = (DecimalType) command;
                double v1 = bdc.doubleValue();
                if (step != null) {
                    int divider = 1;
                    if (step.floatValue() == 0.5) {
                        divider = 2;
                    } else if (step.floatValue() == 0.1) {
                        divider = 10;
                    } else if (step.floatValue() == 0.01) {
                        divider = 100;
                    }
                    v1 = v1 * divider;
                    v1 = Math.floor(v1);
                    v1 = v1 / divider;
                }

                if (minb != null && v1 < minb.floatValue()) {
                    v1 = minb.floatValue();
                }
                if (maxb != null && v1 > maxb.floatValue()) {
                    v1 = maxb.floatValue();
                }

                result = new DecimalType("" + v1);

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
                ReadChannel(channel);
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

            if (command instanceof State) {

                commandVar = applyState(tp, commandVar);
                State state = (State) commandVar;

                this.updateState(channelUID, state);
            }

            if (id != null && type != null) {
                WriteDp(id, commandVar, dptType);
            }
        }
    }
}
