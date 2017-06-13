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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.stiebelheatpump.internal.CommunicationService;
import org.openhab.binding.stiebelheatpump.protocol.DataParser;
import org.openhab.binding.stiebelheatpump.protocol.RecordDefinition;
import org.openhab.binding.stiebelheatpump.protocol.RecordDefinition.Type;
import org.openhab.binding.stiebelheatpump.protocol.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Request versionRequest;
    private Request timeRequest;

    /** cyclic pooling of data from heat pump */
    ScheduledFuture<?> refreshJob;
    /** cyclic update of time in the heat pump */
    ScheduledFuture<?> timeRefreshJob;

    public stiebelheatpumpHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channelUID {}", command, channelUID);

        if (command.toString().equals("REFRESH")) {
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
            Map<String, String> data = communicationService.setData(command.toString(), channelUID.getId(),
                    heatPumpSettingConfiguration);

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

            if (communicationInUse) {
                return;
            }
            communicationInUse = true;

            CommunicationService communicationService = null;
            try {
                communicationService = new CommunicationService(heatPumpConfiguration, port, baudRate, waitingTime);
                Map<String, String> data = communicationService.getRequestData(heatPumpSensorConfiguration);
                updateCannels(data);
                if (heatPumpSensorConfiguration.size() == 1) {
                    Thread.sleep(waitingTime);
                }
                data = communicationService.getRequestData(heatPumpStatusConfiguration);
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
        }, refresh.intValue() / 10, refresh.intValue(), TimeUnit.SECONDS);
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
                if (time.isEmpty()) {
                }
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
     * This method reads initially all information from the heat pump It read
     * the configuration file and loads all defined record definitions of sensor
     * data, status information , actual time settings and setting parameter
     * values.
     *
     * @return true if heat pump information could be successfully read
     */
    private boolean getInitialHeatPumpSettings() {

        String thingVersion = getThing().getProperties().get("firmware");
        String version = "";

        ThingType thingType = TypeResolver.resolve(getThing().getThingTypeUID());
        List<ChannelGroupDefinition> channelGroups = thingType.getChannelGroupDefinitions();

        for (ChannelGroupDefinition channelGroup : channelGroups) {
            ChannelGroupTypeUID ChannelGroupTypeUID = channelGroup.getTypeUID();
            ChannelGroupType channelGroupType = TypeResolver.resolve(ChannelGroupTypeUID);

            List<ChannelDefinition> channelDefinitions = channelGroupType.getChannelDefinitions();
            String requestDescription = channelGroupType.getDescription();
            String requestName = channelGroup.getId();

            for (ChannelDefinition channelDefinition : channelDefinitions) {
                RecordDefinition record = new RecordDefinition();
                DataParser parser = new DataParser();

                byte requestByte = parser
                        .shortToByte(Short.decode(channelDefinition.getProperties().get("requestByte")))[0];

                try {
                    record.setName(channelDefinition.getId());
                    record.setDataType((Type.valueOf(channelDefinition.getProperties().get("dataType"))));
                    record.setPosition(Integer.parseInt(channelDefinition.getProperties().get("position")));
                    record.setLength(Integer.parseInt(channelDefinition.getProperties().get("length")));
                    record.setScale(Double.parseDouble(channelDefinition.getProperties().get("scale")));
                } catch (Exception e) {
                    logger.debug(
                            "Exception occurred during getting inital setting from thing channelgroup configuration {}: {}",
                            channelGroupType.toString(), e.getMessage());
                    return false;
                }

                if (channelDefinition.getProperties().containsKey("min")) {
                    record.setMin(Integer.parseInt(channelDefinition.getProperties().get("min")));
                }
                if (channelDefinition.getProperties().containsKey("max")) {
                    record.setMax(Integer.parseInt(channelDefinition.getProperties().get("max")));
                }
                if (channelDefinition.getProperties().containsKey("step")) {
                    record.setStep(Double.parseDouble(channelDefinition.getProperties().get("step")));
                }
                if (channelDefinition.getProperties().containsKey("bitPosition")) {
                    record.setBitPosition(Integer.parseInt(channelDefinition.getProperties().get("bitPosition")));
                }
                if (channelDefinition.getProperties().containsKey("unit")) {
                    record.setUnit(channelDefinition.getProperties().get("unit"));
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
