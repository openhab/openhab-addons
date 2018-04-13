/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.stiebelheatpump.handler;

import static org.openhab.binding.stiebelheatpump.stiebelheatpumpBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.stiebelheatpump.stiebelheatpumpBindingConstants;
import org.openhab.binding.stiebelheatpump.internal.CommunicationService;
import org.openhab.binding.stiebelheatpump.protocol.DataParser;
import org.openhab.binding.stiebelheatpump.protocol.RecordDefinition;
import org.openhab.binding.stiebelheatpump.protocol.RecordDefinition.Type;
import org.openhab.binding.stiebelheatpump.protocol.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeResolver;

/**
 * The {@link stiebelheatpumpHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Peter Kreutzer - Initial contribution
 */
public class stiebelheatpumpHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(stiebelheatpumpHandler.class);

    /** port of interface to heat pump */
    private String port;
    /** baudRate of interface to heat pump */
    private int baudRate;
    /** waiting time between requests */
    private int waitingTime;
    /** refresh interval */
    private BigDecimal refresh = new BigDecimal(0);
    // ** indicates if the communication is currently in use by a call
    boolean communicationInUse = false;

    /** heat pump request definition */
    private List<Request> heatPumpConfiguration = new ArrayList<Request>();
    private List<Request> heatPumpSensorConfiguration = new ArrayList<Request>();
    private List<Request> heatPumpSettingConfiguration = new ArrayList<Request>();
    private List<Request> heatPumpStatusConfiguration = new ArrayList<Request>();
    private List<Request> heatPumpRefresh = new ArrayList<Request>();
    private Request versionRequest;
    private Request timeRequest;

    /** cyclic pooling of data from heat pump */
    ScheduledFuture<?> refreshJob;
    /** cyclic update of time in the heat pump */
    ScheduledFuture<?> timeRefreshJob;

    private ThingType thingType;

    public stiebelheatpumpHandler(Thing thing, ThingType thingType) {
        super(thing);
        this.thingType = thingType;

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channelUID {}", command, channelUID);

        if (command instanceof RefreshType) {
            // refresh is handled with scheduled polling of data
            return;
        }

        int retry = 0;
        while (communicationInUse & (retry < MAXRETRY)) {
            try {
                Thread.sleep(waitingTime);
            } catch (InterruptedException e) {
                logger.debug("Could not get access to heatpump ! : {}", e.toString());
            }
            retry++;
        }
        if (communicationInUse) {
            return;
        }
        communicationInUse = true;

        CommunicationService communicationService = null;
        try {
            communicationService = new CommunicationService(heatPumpConfiguration, port, baudRate, waitingTime);
            Map<String, String> data = new HashMap<String, String>();

            switch (channelUID.getId()) {
                case CHANNEL_SETTIME:
                    data = communicationService.setTime(timeRequest);
                    updateState(channelUID, OnOffType.OFF);
                    break;
                case CHANNEL_DUMPRESPONSE:
                    for (byte requestByte : DEBUGBYTES) {
                        communicationService.dumpResponse(requestByte);
                        Thread.sleep(waitingTime);
                    }
                    updateState(channelUID, OnOffType.OFF);
                    break;
                default:
                    String value = command.toString();
                    if (command instanceof OnOffType) {
                        // the command come from a switch type , we need to map ON and OFF to 0 and 1 values
                        if (command.equals(OnOffType.ON)) {
                            value = "1";
                        }
                        if (command.equals(OnOffType.OFF)) {
                            value = "0";
                        }
                    }

                    String channelType = getThing().getChannel(channelUID.getId()).getChannelTypeUID().toString();
                    if (channelType.equalsIgnoreCase(CHANNELTYPE_TIMESETTING)) {
                        int hours = 0;
                        int minutes = 0;
                        String[] parts = command.toString().split(Pattern.quote(":"), 2);
                        if (parts.length < 2) {
                            parts = new String[] { "0", "0" };
                        }
                        hours = Integer.parseInt(parts[0]);
                        minutes = Integer.parseInt(parts[1]);
                        if (hours > 23) {
                            hours = 23;
                            minutes = 59;
                        }
                        if (hours < 0) {
                            hours = 0;
                            minutes = 0;
                        }
                        if (minutes > 59 || hours < 0) {
                            minutes = 59;
                        }
                        value = String.valueOf(hours) + String.format("%02d", minutes);
                    }

                    communicationService.setData(value, channelUID.getId(), heatPumpRefresh);
                    break;
            }

            updateCannels(data);

        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        } finally {
            if (communicationService != null) {
                communicationService.finalizer();
                communicationInUse = false;
            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        String channelId = channelUID.getId();
        String[] parts = channelId.split(Pattern.quote(stiebelheatpumpBindingConstants.CHANNELGROUPSEPERATOR), 2);
        if (parts.length != 2) {
            logger.debug("Channel {} to link has invalid structure requestName#recordName", channelId);
            return;
        }
        String requestName = parts[0];
        String recordName = parts[1];

        // verify if channel is already in considered refresh request
        for (Request request : heatPumpRefresh) {
            if (request.getName().equals(requestName)) {
                logger.debug("Found valid record definition {} in request {}:{}", requestName, recordName);
                return;
            }
        }

        // record is currently not in the refresh list, add the request
        for (Request request : heatPumpConfiguration) {
            if (request.getName().equals(requestName)) {
                for (RecordDefinition record : request.getRecordDefinitions()) {
                    if (record.getName().equalsIgnoreCase(recordName)) {
                        logger.debug("Found valid record definition {} in request {}:{}", record.getName(),
                                request.getName(), request.getDescription());
                        heatPumpRefresh.add(request);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        String channelId = channelUID.getId();
        String[] parts = channelId.split(Pattern.quote(stiebelheatpumpBindingConstants.CHANNELGROUPSEPERATOR), 2);
        if (parts.length != 2) {
            logger.debug("Channel {} to unlink has invalid structure requestName#recordName", channelId);
        }
        String requestName = parts[0];
        String recordName = parts[1];

        for (Request request : heatPumpRefresh) {
            if (request.getName().equals(requestName)) {
                logger.debug("Found valid record definition {} in request {}:{} to be removed", requestName,
                        recordName);
                List<Channel> channels = getThing().getChannels();
                for (Channel channel : channels) {
                    String tmpChannelUID = channel.getUID().toString();
                    if (tmpChannelUID.startsWith(requestName)) {
                        // there is still a channel link in the thing which will require updates
                        logger.debug("Request {} will remain in refresh list as channel {} is still linked",
                                requestName, channel.getUID());
                        return;
                    }
                }
                // no channel found which belongs to same request, remove request
                heatPumpRefresh.remove(request);
                break;
            }
        }
    }

    @Override
    public void initialize() {

        String msg = "Error occurred while initializing stiebel heat pump handler! ";
        try {
            Configuration config = getThing().getConfiguration();
            port = (String) config.get(PROPERTY_PORT);
            baudRate = ((BigDecimal) config.get(PROPERTY_BAUDRATE)).intValueExact();
            refresh = (BigDecimal) config.get(PROPERTY_REFRESH);
            waitingTime = ((BigDecimal) config.get(PROPERTY_WAITINGTIME)).intValueExact();

            logger.debug(
                    "Initializing stiebel heat pump handler '{}' with configuration: port '{}', baudRate {}, refresh {}.",
                    getThing().getUID().toString(), port, baudRate, refresh);

            boolean success = getInitialHeatPumpSettings();
            if (success) {
                updateStatus(ThingStatus.ONLINE);
                startTimeRefresh();
                startAutomaticRefresh();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            }
        } catch (Exception ex) {
            msg = msg + " : " + ex.getMessage();
            logger.error(msg, ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
        }
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
    }

    /**
     * This method pools the meter data and updates the channels on a scheduler
     * once per refresh time defined in the thing properties
     */
    private void startAutomaticRefresh() {
        refreshJob = scheduler.scheduleAtFixedRate(() -> {

            if (heatPumpRefresh.isEmpty()) {
                logger.info("nothing to update, refresh list is empty");
                return;
            }

            if (communicationInUse) {
                return;
            }
            communicationInUse = true;

            CommunicationService communicationService = null;
            try {
                communicationService = new CommunicationService(heatPumpConfiguration, port, baudRate, waitingTime);
                Map<String, String> data = communicationService.getRequestData(heatPumpRefresh);
                updateCannels(data);
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            } finally {
                if (communicationService != null) {
                    communicationService.finalizer();
                    communicationInUse = false;
                }
            }
        }, 2, refresh.intValue(), TimeUnit.SECONDS);
    }

    /**
     * This method set the time in the heat pump to system time on a scheduler
     * once a week
     */
    private void startTimeRefresh() {
        timeRefreshJob = scheduler.scheduleAtFixedRate(() -> {
            if (communicationInUse) {
                return;
            }
            communicationInUse = true;

            CommunicationService communicationService = null;
            try {
                communicationService = new CommunicationService(heatPumpConfiguration, port, baudRate, waitingTime);
                Map<String, String> time = communicationService.setTime(timeRequest);
                updateCannels(time);

            } catch (Exception e) {
                logger.debug(e.getMessage());
            } finally {
                if (communicationService != null) {
                    communicationService.finalizer();
                    communicationInUse = false;
                }
            }
        }, refresh.intValue() + refresh.intValue() / 2, 7, TimeUnit.DAYS);
    }

    /**
     * This method reads initial information from the heat pump. It reads
     * the configuration file and loads all defined record definitions of sensor
     * data, status information , actual time settings and setting parameter
     * values for the thing type definition.
     *
     * @return true if heat pump information could be successfully connected and read
     */
    private boolean getInitialHeatPumpSettings() {
        String thingVersion = getThing().getProperties().get("firmware");
        String version = "";

        if (heatPumpConfiguration.isEmpty()) {

            List<ChannelGroupDefinition> channelGroups = thingType.getChannelGroupDefinitions();

            for (ChannelGroupDefinition channelGroup : channelGroups) {

                ChannelGroupTypeUID ChannelGroupTypeUID = channelGroup.getTypeUID();
                ChannelGroupType channelGroupType = TypeResolver.resolve(ChannelGroupTypeUID);

                List<ChannelDefinition> channelDefinitions = channelGroupType.getChannelDefinitions();
                String requestDescription = channelGroupType.getDescription();
                String requestName = channelGroup.getId();

                for (ChannelDefinition channelDefinition : channelDefinitions) {

                    if (!channelDefinition.getProperties().containsKey("requestByte")) {
                        // only iterate on channels with request property
                        continue;
                    }

                    RecordDefinition record = new RecordDefinition();
                    DataParser parser = new DataParser();

                    byte requestByte = parser
                            .shortToByte(Short.decode(channelDefinition.getProperties().get("requestByte")))[0];

                    ChannelTypeUID channelTypeUID = channelDefinition.getChannelTypeUID();

                    try {
                        ChannelType channelType = TypeResolver.resolve(channelTypeUID);
                        StateDescription state = channelType.getState();

                        record.setName(channelDefinition.getId());
                        record.setDataType((Type.valueOf(channelDefinition.getProperties().get("dataType"))));
                        record.setPosition(Integer.parseInt(channelDefinition.getProperties().get("position")));
                        record.setLength(Integer.parseInt(channelDefinition.getProperties().get("length")));
                        record.setScale(Double.parseDouble(channelDefinition.getProperties().get("scale")));

                        if (state != null) {
                            if (state.getMinimum() != null) {
                                record.setMin(state.getMinimum().intValueExact());
                            }
                            if (state.getMaximum() != null) {
                                record.setMax(state.getMaximum().intValueExact());
                            }
                            if (state.getStep() != null) {
                                record.setStep(state.getStep().doubleValue());
                            }
                        }
                        if (channelDefinition.getProperties().containsKey("bitPosition")) {
                            record.setBitPosition(
                                    Integer.parseInt(channelDefinition.getProperties().get("bitPosition")));
                        }

                    } catch (Exception e) {
                        logger.error(
                                "Exception occurred during getting inital setting from thing channelgroup {} channeldefiniton {} configuration : {}",
                                channelGroupType.toString(), channelDefinition.toString(), e.getMessage());
                        continue;
                    }

                    boolean notFound = true;
                    for (Request request : heatPumpConfiguration) {
                        if (request.getRequestByte() == requestByte) {
                            request.getRecordDefinitions().add(record);
                            notFound = false;
                        }
                    }
                    if (notFound) {
                        Request newRequest = new Request(requestName, requestDescription, requestByte);
                        newRequest.getRecordDefinitions().add(record);
                        heatPumpConfiguration.add(newRequest);
                    }
                }
            }

            categorizeHeatPumpConfiguration();
        }
        // get version information from the heat pump
        CommunicationService communicationService = null;
        try {
            communicationService = new CommunicationService(heatPumpConfiguration, port, baudRate, waitingTime);
            version = communicationService.getversion(versionRequest);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return false;
        } finally {
            if (communicationService != null) {
                communicationService.finalizer();
            }
        }

        logger.info("Heat pump has version {}", version);
        ChannelUID versionChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_VERSION);
        updateState(versionChannelUID, new StringType(version));

        if (!thingVersion.equals(version)) {
            logger.error("Thingtype version of heatpump {} is not the same as the heatpump version {}", thingVersion,
                    version);
            return false;
        }

        return true;
    }

    /**
     * This method updates the query data to the channels
     *
     * @param data
     *            Map<String, String> of data coming from heat pump
     */
    private void updateCannels(Map<String, String> data) {

        if (data.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : data.entrySet()) {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), entry.getKey());
            logger.debug("Data {} has value {}", entry.getKey(), entry.getValue());
            logger.debug("Update channel UID {} with {}", channelUID, entry.getValue());

            String channelType = getThing().getChannel(channelUID.getId()).getChannelTypeUID().toString();
            if (channelType.equalsIgnoreCase(CHANNELTYPE_TIMESETTING)) {
                String newTime = String.format("%04d", Integer.parseInt(entry.getValue()));
                newTime = new StringBuilder(newTime).insert(newTime.length() - 2, ":").toString();
                updateState(channelUID, new StringType(newTime));
                continue;
            }

            if (channelType.equalsIgnoreCase(CHANNELTYPE_SWITCHSETTING)) {
                switch (entry.getValue()) {
                    case "0":
                        updateState(channelUID, OnOffType.OFF);
                        break;
                    default:
                        updateState(channelUID, OnOffType.ON);
                        continue;
                }
            }

            updateState(channelUID, new DecimalType(entry.getValue()));
        }
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * This method categorize the heat pump configuration into setting, sensor
     * and status
     *
     * @return true if heat pump configuration for version could be found and
     *         loaded
     */
    private boolean categorizeHeatPumpConfiguration() {
        for (Request request : heatPumpConfiguration) {
            logger.debug("Request : Name -> {}, Description -> {} , RequestByte -> {}", request.getName(),
                    request.getDescription(),
                    DatatypeConverter.printHexBinary(new byte[] { request.getRequestByte() }));
            if (request.getName().equalsIgnoreCase("version")) {
                versionRequest = request;
                logger.debug("set version request : " + versionRequest.getDescription());
                continue;
            }

            if (request.getName().equalsIgnoreCase("time")) {
                timeRequest = request;
                logger.debug("set time request : " + timeRequest.getDescription());
                continue;
            }

            // group requests in different categories
            for (RecordDefinition record : request.getRecordDefinitions()) {
                if (record.getDataType() == Type.Settings && !heatPumpSettingConfiguration.contains(request)) {
                    heatPumpSettingConfiguration.add(request);
                }
                if (record.getDataType() == Type.Status && !heatPumpStatusConfiguration.contains(request)) {
                    heatPumpStatusConfiguration.add(request);
                }
                if (record.getDataType() == Type.Sensor && !heatPumpSensorConfiguration.contains(request)) {
                    heatPumpSensorConfiguration.add(request);
                }
            }
        }

        if (versionRequest == null || timeRequest == null) {
            logger.debug("version or time request could not be found in configuration");
            return false;
        }
        return true;
    }

}
