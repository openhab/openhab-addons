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
package org.openhab.binding.siemenshvac.internal.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataDataPoint;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataRegistry;
import org.openhab.binding.siemenshvac.internal.config.SiemensHvacConfiguration;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacCallback;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacConnector;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacChannelGroupTypeProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacChannelTypeProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacConfigDescriptionProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacThingTypeProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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

    private final Logger logger = LoggerFactory.getLogger(SiemensHvacHandlerImpl.class);

    private @Nullable ScheduledFuture<?> pollingJob = null;

    private @Nullable SiemensHvacConfiguration config;
    private @Nullable SiemensHvacThingTypeProvider thingTypeProvider;
    private @Nullable SiemensHvacChannelTypeProvider channelTypeProvider;
    private @Nullable SiemensHvacChannelGroupTypeProvider channelGroupTypeProvider;
    private @Nullable SiemensHvacConfigDescriptionProvider configDescriptionProvider;
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

        config = getConfigAs(SiemensHvacConfiguration.class);
        var c1 = getThing().getConfiguration();
        var c2 = getBridge().getConfiguration();

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        pollingJob.cancel(true);
    }

    private void pollingCode() {

        var chList = this.getThing().getChannels();
        for (Channel ch : chList) {
            logger.debug(ch.getDescription());

            boolean isLink = this.getCallback().isChannelLinked(ch.getUID());

            if (!isLink) {
                continue;
            }

            if (channelTypeProvider == null) {
                return;
            }

            ChannelType tp = channelTypeProvider.getInternalChannelType(ch.getChannelTypeUID());

            String dptId = ch.getProperties().get("dptId");
            String groupId = ch.getProperties().get("groupdId");
            String label = ch.getLabel();
            String uid = ch.getUID().getId();
            String type = tp.getItemType();

            ReadDp(dptId, uid, type, false);
            logger.debug("" + isLink);
        }
    }

    public void DecodeReadDp(JsonObject response, @Nullable String uid, @Nullable String dp, @Nullable String type) {
        if (response != null && response.has("Data")) {
            JsonObject subResult = (JsonObject) response.get("Data");

            String updateKey = "" + uid;
            String typer = "";
            JsonElement value = null;
            JsonElement enumValue = null;
            String result = "";
            String unit = "";

            if (subResult.has("Type")) {
                typer = subResult.get("Type").getAsString().trim();
            }
            if (subResult.has("Value")) {
                value = subResult.get("Value");
            }
            if (subResult.has("EnumValue")) {
                enumValue = subResult.get("EnumValue");
            }
            if (subResult.has("Unit")) {
                unit = subResult.get("Unit").toString().trim();
            }

            if (value == null) {
                return;
            }

            if (type == null) {
                logger.debug("siemensHvac:ReadDP:null type" + dp);
            }
            if (typer == null) {
                logger.debug("siemensHvac:ReadDP:null typer" + dp);
            }

            if (typer.equals("Numeric")) {
                updateState(updateKey, new DecimalType(value.getAsDouble()));
            } else if (typer.equals("Enumeration")) {
                updateState(updateKey, new DecimalType(enumValue.getAsInt()));
            } else if (typer.equals("Text")) {
                updateState(updateKey, new StringType(value.getAsString()));
            } else if (typer.equals("RadioButton")) {
                updateState(updateKey, new StringType(value.getAsString()));
            } else if (typer.equals("DayOfTime") || typer.equals("DateTime")) {
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

    private void ReadDp(@Nullable String dp, String uid, @Nullable String type, boolean async) {
        if (dp.equals("-1")) {
            return;
        }

        try {
            String request = "api/menutree/read_datapoint.json?Id=" + dp;

            // logger.debug("siemensHvac:ReadDp:DoRequest():" + request);

            if (async) {
                hvacConnector.DoRequest(request, new SiemensHvacCallback() {

                    @Override
                    public void execute(java.net.URI uri, int status, @Nullable Object response) {
                        if (response instanceof JsonObject) {
                            DecodeReadDp((JsonObject) response, uid, dp, type);
                        }
                    }

                });
            } else {
                JsonObject js = hvacConnector.DoRequest(request, null);
                DecodeReadDp(js, uid, dp, type);
            }

        } catch (Exception e) {
            logger.error("siemensHvac:ReadDp:Error during dp reading: " + dp + " ; " + e.getLocalizedMessage());
            // Reset sessionId so we redone _auth on error
        }
    }

    private void WriteDp(@Nullable String dp, Type dpVal, @Nullable String type) {
        if (dp.equals("-1")) {
            return;
        }

        try {
            String valUpdate = "0";
            String valUpdateEnum = "";
            String valUpdateLabel = "";

            if (dpVal instanceof PercentType) {
                PercentType pct = (PercentType) dpVal;
                valUpdate = pct.toString();
            } else if (dpVal instanceof DecimalType) {
                DecimalType bdc = (DecimalType) dpVal;
                valUpdate = bdc.toString();
            } else if (dpVal instanceof StringType) {
                StringType bdc = (StringType) dpVal;
                valUpdate = bdc.toString();

                if (type.equals("Enumeration")) {
                    String[] valuesUpdateDp = valUpdate.split(":");
                    valUpdateEnum = valuesUpdateDp[0];
                    valUpdateLabel = valuesUpdateDp[1];

                    // For enumeration, we always update using the raw value
                    valUpdate = valUpdateEnum;
                }
            }

            SiemensHvacMetadataDataPoint md = (SiemensHvacMetadataDataPoint) metaDataRegistry.getDptMap(dp);
            String dptType = md.getDptType();

            String request = "api/menutree/write_datapoint.json?Id=" + dp + "&Value=" + valUpdate + "&Type=" + dptType;

            // logger.debug("siemensHvac:ReadDp:DoRequest():" + request);

            hvacConnector.DoRequest(request, new SiemensHvacCallback() {

                @Override
                public void execute(java.net.URI uri, int status, @Nullable Object response) {
                    if (response instanceof JsonObject) {
                        logger.debug("p1");
                    }
                }

            });

        } catch (Exception e) {
            logger.error("siemensHvac:ReadDp:Error during dp reading: " + dp + " ; " + e.getLocalizedMessage());
            // Reset sessionId so we redone _auth on error
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand");
        if (command instanceof RefreshType) {
            logger.debug("handleCommandRefresh");
        } else {

            Channel channel = getThing().getChannel(channelUID);

            ChannelType tp = channelTypeProvider.getInternalChannelType(channel.getChannelTypeUID());

            String dptId = channel.getProperties().get("dptId");
            String groupId = channel.getProperties().get("groupdId");
            String label = channel.getLabel();
            String uid = channel.getUID().getId();
            String type = tp.getItemType();

            WriteDp(dptId, command, type);
            ReadDp(dptId, uid, type, false);
        }
    }

}
