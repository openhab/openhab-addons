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
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacChannelTypeProvider;
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
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
@Component(immediate = true)
@NonNullByDefault
public class SiemensHvacHandlerImpl extends BaseThingHandler implements SiemensHvacHandler {

    private Lock lockObj = new ReentrantLock();

    private final Logger logger = LoggerFactory.getLogger(SiemensHvacHandlerImpl.class);

    private @Nullable ScheduledFuture<?> pollingJob = null;

    private @Nullable SiemensHvacChannelTypeProvider channelTypeProvider;
    private @Nullable SiemensHvacConnector hvacConnector;
    private @Nullable SiemensHvacMetadataRegistry metaDataRegistry;

    public SiemensHvacHandlerImpl(Thing thing) {
        super(thing);

        logger.debug("===========================================================");
        logger.debug("Siemens HVac");
        logger.debug("===========================================================");
    }

    @Reference
    public void setSiemensHvacConnector(@Nullable SiemensHvacConnector hvacConnector) {
        this.hvacConnector = hvacConnector;
    }

    public void unsetSiemensHvacConnector(SiemensHvacConnector hvacConnector) {
        this.hvacConnector = null;
    }

    @Reference
    public void setSiemensHvacMetadataRegistry(@Nullable SiemensHvacMetadataRegistry metaDataRegistry) {
        this.metaDataRegistry = metaDataRegistry;
    }

    public void unsetSiemensHvacMetadataRegistry(SiemensHvacMetadataRegistry metaDataRegistry) {
        this.metaDataRegistry = null;
    }

    @Reference
    public void setChannelTypeProvider(@Nullable SiemensHvacChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = channelTypeProvider;
    }

    public void unsetChannelTypeProvider(@Nullable SiemensHvacChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = null;
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
        SiemensHvacChannelTypeProvider lcChannelTypeProvider = channelTypeProvider;

        var chList = this.getThing().getChannels();
        for (Channel ch : chList) {
            logger.debug("{}", ch.getDescription());

            ThingHandlerCallback cb = this.getCallback();
            boolean isLink = false;

            if (cb != null) {
                isLink = cb.isChannelLinked(ch.getUID());
            }

            if (!isLink) {
                continue;
            }

            ChannelType tp = null;

            if (lcChannelTypeProvider != null) {
                tp = lcChannelTypeProvider.getInternalChannelType(ch.getChannelTypeUID());
            }

            if (tp == null) {
                continue;
            }

            String Id = ch.getProperties().get("id");
            String uid = ch.getUID().getId();
            String type = tp.getItemType();

            if (Id == null) {
                logger.debug("poolingCode : Id is null {} ", ch);
                continue;
            }
            if (type == null) {
                logger.debug("poolingCode : type is null {} ", ch);
                continue;
            }
            ReadDp(Id, uid, type, true);
            logger.debug("{}", isLink);
        }
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
            } else if (("Enumeration").equals(typer)) {
                if (enumValue != null) {
                    updateState(updateKey, new DecimalType(enumValue.getAsInt()));
                }
            } else if (("Text").equals(typer)) {
                updateState(updateKey, new StringType(value.getAsString()));
            } else if (("RadioButton").equals(typer)) {
                updateState(updateKey, new StringType(value.getAsString()));
            } else if (("DayOfTime").equals(typer) || ("DateTime").equals(typer)) {
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

        if (("-1").equals(dp)) {
            return;
        }

        try {
            lockObj.lock();
            logger.info("Start read : {}", dp);
            String request = "api/menutree/read_datapoint.json?Id=" + dp;

            // logger.debug("siemensHvac:ReadDp:DoRequest():" + request);

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
            logger.info("End read : {}", dp);
            lockObj.unlock();

        }
    }

    private void WriteDp(String dp, Type dpVal, String type) {
        SiemensHvacConnector lcHvacConnector = hvacConnector;
        if (("-1").equals(dp)) {
            return;
        }

        try {
            lockObj.lock();
            // logger.info("Start write :" + dp);
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

            String request = "api/menutree/write_datapoint.json?Id=" + dp + "&Value=" + valUpdate + "&Type=" + type;

            if (lcHvacConnector != null) {
                logger.info("Write request for : {} ", valUpdate);
                JsonObject response = lcHvacConnector.DoRequest(request, null);

                logger.info("Write request response : {} ", response);
                if (response instanceof JsonObject) {
                    logger.debug("p1");
                }
            }

        } catch (Exception e) {
            logger.error("siemensHvac:ReadDp:Error during dp reading: {} ; {}", dp, e.getLocalizedMessage());
            // Reset sessionId so we redone _auth on error
        } finally {
            logger.info("End write : {}", dp);
            lockObj.unlock();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SiemensHvacMetadataRegistry lcMetaDataRegistry = metaDataRegistry;
        SiemensHvacChannelTypeProvider lcChannelTypeProvider = channelTypeProvider;
        logger.debug("handleCommand");
        if (command instanceof RefreshType) {
            logger.debug("handleCommandRefresh");
        } else {

            Channel channel = getThing().getChannel(channelUID);
            if (channel == null) {
                return;
            }

            ChannelType tp = null;
            if (lcChannelTypeProvider != null) {
                tp = lcChannelTypeProvider.getInternalChannelType(channel.getChannelTypeUID());
            }

            if (tp == null) {
                return;
            }

            String dptId = channel.getProperties().get("dptId");
            String type = tp.getItemType();
            String dptType = "";
            String id = "";
            SiemensHvacMetadataDataPoint md = null;

            if (lcMetaDataRegistry != null) {
                md = (SiemensHvacMetadataDataPoint) lcMetaDataRegistry.getDptMap(dptId);
                if (md != null) {
                    id = "" + md.getId();
                    dptType = md.getDptType();
                }
            }

            if (command instanceof State) {
                State state = (State) command;
                this.updateState(channelUID, state);
            }

            if (dptId != null && type != null) {
                WriteDp(id, command, dptType);
            }
        }
    }
}
