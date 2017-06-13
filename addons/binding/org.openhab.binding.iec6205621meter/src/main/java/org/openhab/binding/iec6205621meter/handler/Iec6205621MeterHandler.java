/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.iec6205621meter.handler;

import static org.openhab.binding.iec6205621meter.Iec6205621MeterBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openmuc.j62056.Connection;
import org.openmuc.j62056.DataSet;

//import org.openmuc.j62056.Iec21Port;
//import org.openmuc.j62056.DataMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Iec6205621MeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Peter Kreutzer - Initial contribution
 */
public class Iec6205621MeterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(Iec6205621MeterHandler.class);

    /** port of interface to meter */
    private String port;
    /** baudRateChangeDelay of interface to meter */
    private int baudRateChangeDelay;
    /** Baud rate change of interface to meter */
    private boolean echoHandling;
    /** initMessage extra pre-init bytes */
    private byte[] initMessage;
    /** refresh interval */
    private BigDecimal refresh = new BigDecimal(0);

    /** list of channelUID to obis pairs */
    private Map<String, ChannelUID> channelUIDMap = new HashMap<String, ChannelUID>();

    /** list of obis data from meter */
    private Map<String, DataSet> dataSetMap = new LinkedHashMap<String, DataSet>();

    ScheduledFuture<?> refreshJob;

    int counter = 0;

    public Iec6205621MeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void initialize() {

        try {
            logger.debug("Initializing IEC 62056-21 handler for '{}'.", getThing().getUID().toString());

            Configuration config = getThing().getConfiguration();
            port = (String) config.get(PROPERTY_PORT);
            baudRateChangeDelay = ((BigDecimal) config.get(PROPERTY_BAUDRATECHANGEDELAY)).intValueExact();
            echoHandling = (boolean) config.get(PROPERTY_ECHOHANDLING);
            String initMessageValue = Objects.toString(config.get(PROPERTY_INITMESSAGE), null);
            initMessage = initMessageValue != null ? DatatypeConverter.parseHexBinary(initMessageValue) : null;
            refresh = (BigDecimal) config.get(PROPERTY_REFRESH);

            logger.debug(
                    "Initializing IEC 62056-21 handler '{}' with configuration: port '{}', baudRateChangeDelay {}, echoHandling {} , init {}, refresh {}.",
                    getThing().getUID().toString(), port, baudRateChangeDelay, echoHandling, refresh);

            boolean success = updateMeterData();

            if (!success) {
                String msg = "Could not get data from IEC 62056-21 meter connected to " + port;
                logger.error(msg);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            } else {
                String modelType = (String) dataSetMap.keySet().toArray()[0];
                ChannelUID typeChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_TYPE);
                dataSetMap.remove(modelType);

                if (thing.getChannels().size() == 3) {
                    for (Entry<String, DataSet> entry : dataSetMap.entrySet()) {
                        String obis = entry.getKey();
                        DataSet data = entry.getValue();
                        String obisValue = data.getValue();
                        String obisUnit = data.getUnit();
                        thingStructureChanged(obis, obisValue, obisUnit);
                    }
                } else {
                    List<Channel> channels = thing.getChannels();
                    for (Channel channel : channels) {
                        ChannelUID channelUID = channel.getUID();
                        String obis = channel.getProperties().get("obis");
                        if (dataSetMap.containsKey(obis)) {
                            channelUIDMap.put(obis, channelUID);
                        }
                    }
                }
                updateStatus(ThingStatus.ONLINE);
                updateProperty(Thing.PROPERTY_MODEL_ID, modelType);
                updateState(typeChannelUID, new StringType(modelType));

                startAutomaticRefresh();
            }

        } catch (Exception ex) {
            String msg = "Error occurred while initializing IEC 62056-21 handler: " + ex.getMessage();
            logger.error(msg, ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
        }
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
    }

    private void startAutomaticRefresh() {
        refreshJob = scheduler.scheduleAtFixedRate(() -> {
            try {
                boolean success = updateMeterData();
                if (success) {
                    updateStatus(ThingStatus.ONLINE);
                    for (Entry<String, DataSet> entry : dataSetMap.entrySet()) {
                        String obis = entry.getKey();
                        DataSet data = entry.getValue();
                        String obisValue = data.getValue();
                        String obisUnit = data.getUnit();

                        if (obisUnit.isEmpty()) {
                            updateState(channelUIDMap.get(obis), new StringType(obisValue));
                        } else {
                            updateState(channelUIDMap.get(obis), new DecimalType(obisValue));
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }, refresh.intValue(), refresh.intValue(), TimeUnit.SECONDS);
    }

    /**
     * updates the data from meter
     *
     * @return true if received data are not empty.
     */
    private boolean updateMeterData() {

        dataSetMap = read(port, initMessage, echoHandling, baudRateChangeDelay);

        if (dataSetMap.isEmpty()) {
            return false;
        }

        return true;
    }

    // /**
    // * Reads data from meter
    // *
    // * @return a map of DataSet objects with the obis as key.
    // */
    // private Map<String, DataSet> read(String port, byte[] initMessage, boolean echoHandling, int baudRateChangeDelay)
    // {
    //
    // Map<String, DataSet> dataSetMap = new LinkedHashMap<String, DataSet>();
    // Iec21Port iec21Port = null;
    // try {
    // try {
    // iec21Port = new Iec21Port.Builder(port).setBaudRateChangeDelay(baudRateChangeDelay)
    // .enableVerboseMode(true).buildAndOpen();
    // DataMessage dataMessage = iec21Port.read();
    //
    // logger.debug(dataMessage.toString());
    // } catch (IOException e) {
    // logger.error("IOException while trying to read: {}", e.getMessage());
    // }
    // } finally {
    // if (iec21Port != null) {
    // iec21Port.close();
    // }
    // }
    // return dataSetMap;
    // }

    /**
     * Reads data from meter
     *
     * @return a map of DataSet objects with the obis as key.
     */
    private Map<String, DataSet> read(String port, byte[] initMessage, boolean echoHandling, int baudRateChangeDelay) {

        Map<String, DataSet> dataSetMap = new LinkedHashMap<String, DataSet>();

        Connection connection = new Connection(port, initMessage, echoHandling, baudRateChangeDelay);
        try {
            try {
                connection.open();
            } catch (IOException e) {
                logger.error("Failed to open serial port {}: {}", port, e.getMessage());
                return dataSetMap;
            }

            List<DataSet> dataSets = null;
            try {
                dataSets = connection.read();
                for (DataSet dataSet : dataSets) {
                    logger.debug("DataSet: {};{};{}", dataSet.getId(), dataSet.getValue(), dataSet.getUnit());
                    dataSetMap.put(dataSet.getId(), dataSet);
                }
            } catch (IOException e) {
                logger.error("IOException while trying to read: {}", e.getMessage());
            } catch (TimeoutException e) {
                logger.error("Read attempt timed out");
            }
        } finally {
            connection.close();
        }
        return dataSetMap;
    }

    /**
     * Creates a new channels for a meter thing
     * based on the obis information give number or text channels are created
     * obis which do not have unit are considered as text channels
     *
     * @param obisUnit
     * @param obisValue
     * @param obis
     *
     */
    protected void thingStructureChanged(String obis, String obisValue, String obisUnit) {
        ThingBuilder thingBuilder = editThing();

        String newObisID = obis.replaceAll("[^a-zA-Z0-9]+", "_");
        String channelUID = getThing().getUID().toString() + ":" + newObisID;
        String itemType = "Number";

        Configuration configuration = new Configuration();

        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_NUMBER);
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("obis", obis);

        if (obisUnit.isEmpty()) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_TEXT);
            itemType = "String";
        } else {
            properties.put("category", "Energy");
            configuration.put("state.pattern", "%d " + obisUnit);
        }

        Channel channel = ChannelBuilder.create(new ChannelUID(channelUID), itemType).withLabel(obis)
                .withDescription("OBIS data -> OBIS: " + obis + ", value: " + obisValue + ", unit: " + obisUnit)
                .withProperties(properties).withConfiguration(configuration).withType(channelTypeUID).build();

        ChannelUID newUID = channel.getUID();
        channelUIDMap.put(obis, newUID);
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());

        if (obisUnit.isEmpty()) {
            updateState(newUID, new StringType(obisValue));
        } else {
            updateState(newUID, new DecimalType(obisValue));
        }
    }
}
