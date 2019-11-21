/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.semp.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SEMP consumers identification
 *
 * @author Markus Eckhardt - Initial Contribution
 *
 */
public class SEMPItemCommunication {
    private final Logger logger = LoggerFactory.getLogger(SEMPItemCommunication.class);

    public boolean itemsToDevice(Map<String, SEMPConsumer> consumerMap, String deviceID,
            Map<String, QueryablePersistenceService> persistenceServices) {
        List<Double> historicValues;
        for (SEMPConsumer consumer : consumerMap.values()) {
            if (!consumer.getIdentification().getDeviceId().equals(deviceID) && !"".equals(deviceID)) {
                continue;
            }
            if (consumer.getControlItem() == null) {
                logger.error("No control item for group {} set", consumer.getGroupItem().getName());
                continue;
            }
            logger.debug("Reading items state, Group: {}", consumer.getGroupItem().getName());
            boolean isConnected = true;
            boolean isListening = true;
            if (consumer.getConnectionItem() != null) {
                logger.debug("Connection Item availible, state: {}", consumer.getConnectionItem().getState());
                if (consumer.getConnectionItem().getState() == OpenClosedType.OPEN) {
                    isConnected = false;
                    isListening = false;
                }
            }
            if (consumer.getListeningItem() != null) {
                logger.debug("Listening Item availible, state: {}", consumer.getListeningItem().getState());
                if (consumer.getListeningItem().getState() == OpenClosedType.OPEN) {
                    isListening = false;
                }
            }
            State itemState = consumer.getControlItem().getState();
            Date currentDate = new Date();
            Long timestamp = currentDate.getTime() / 1000;
            consumer.getDeviceStatus().setTimestamp(timestamp);
            consumer.getDeviceStatus().setEMSignalsAccepted(isListening);
            if (!isConnected) {
                consumer.getDeviceStatus().setStatus("Offline");
                logger.debug("STATE: Offline");
            } else {
                if (itemState instanceof OnOffType) {
                    String state = (((OnOffType) itemState) == OnOffType.ON ? "On" : "Off");
                    consumer.getDeviceStatus().setStatus(state);
                    logger.debug("Item: {} STATE: {}", consumer.getControlItem().getName(), state);
                } else {
                    logger.error("Item {} from group {} is not from OnOffType type, type is: {} state is: {}",
                            consumer.getControlItem().getName(), consumer.getGroupItem().getName(),
                            consumer.getControlItem().getType(), consumer.getControlItem().getState());
                    return false;
                }
                if (consumer.getEnergyItem() != null) {
                    itemState = consumer.getEnergyItem().getState();
                    if ("On".equals(consumer.getDeviceStatus().getStatus())) {
                        if (itemState instanceof DecimalType) {
                            double state = (((DecimalType) itemState).doubleValue());
                            consumer.getDeviceStatus().setAveragePower(state);
                            consumer.getDeviceStatus().setMinPower(state);
                            consumer.getDeviceStatus().setMaxPower(state);
                            consumer.getDeviceStatus().setAveragingInterval(60);
                        } else {
                            logger.error("Item is not from DecimalType type ");
                            return false;
                        }
                    } else {
                        if (itemState instanceof DecimalType) {
                            consumer.getDeviceStatus().setAveragePower(0.0);
                            consumer.getDeviceStatus().setMinPower(0.0);
                            consumer.getDeviceStatus().setMaxPower(0.0);
                            consumer.getDeviceStatus().setAveragingInterval(60);
                        } else {
                            logger.error("Item is not from DecimalType type ");
                            return false;
                        }
                    }
                    if (isPersistanceAvailible(consumer.getEnergyItem(), persistenceServices)) {
                        logger.debug("Persistence availible");
                        consumer.getDeviceHistoryStatus().clear();
                        for (int i = 0; i < 10; i++) {
                            Double sum = 0.0;
                            SEMPDeviceStatus historyStatus = new SEMPDeviceStatus();
                            historicValues = getHistoryValue(consumer.getEnergyItem(), persistenceServices,
                                    (timestamp - (i + 1) * consumer.getDeviceStatus().getAveragingInterval()) * 1000,
                                    (timestamp - i * consumer.getDeviceStatus().getAveragingInterval()) * 1000);
                            if (historicValues == null || historicValues.size() <= 0) {
                                continue;
                            }
                            historyStatus.setAveragingInterval(consumer.getDeviceStatus().getAveragingInterval());
                            historyStatus
                                    .setTimestamp(timestamp - i * consumer.getDeviceStatus().getAveragingInterval());
                            for (int j = 0; j < historicValues.size(); j++) {
                                logger.debug("SIZE: {} TIME: {}, VAL: {}", historicValues.size(), i, j);
                                sum += historicValues.get(j);
                            }
                            historyStatus.setMinPower(Collections.min(historicValues));
                            historyStatus.setMaxPower(Collections.max(historicValues));
                            historyStatus.setAveragePower(sum / historicValues.size());
                            historyStatus.setStdDevPower(calculateStandardDeviation(historicValues));
                            consumer.getDeviceHistoryStatus().add(historyStatus);
                            logger.debug("Avarage: {}: {}", i, sum / historicValues.size());
                        }
                        consumer.hasHistory = true;
                    } else {
                        logger.debug("Persistence for group {} NOT availible", consumer.getGroupItem().getName());
                    }
                } else {
                    logger.debug("Energy item for group {} NOT availible", consumer.getGroupItem().getName());
                }
            }
        }
        return true;
    }

    public boolean deviceToItems(Map<String, SEMPConsumer> consumerMap, boolean onOffValue, String deviceID,
            EventPublisher eventPublisher) {
        String newState;
        for (SEMPConsumer consumer : consumerMap.values()) {
            if (!consumer.getIdentification().getDeviceId().equals(deviceID)) {
                continue;
            }
            State itemState = consumer.getControlItem().getState();
            if (itemState instanceof OnOffType) {
                if (onOffValue) {
                    newState = OnOffType.ON.toString();
                } else {
                    newState = OnOffType.OFF.toString();
                }
                Command command = TypeParser.parseCommand(consumer.getControlItem().getAcceptedCommandTypes(),
                        newState);
                eventPublisher.post(ItemEventFactory.createCommandEvent(consumer.getControlItem().getName(), command));
            } else {
                logger.error("Item is not from OnOffType type ");
                return false;
            }
            consumer.getDeviceStatus().setStatus(newState);
            Date currentDate = new Date();
            Long timeStamp = currentDate.getTime() / 1000;
            SEMPTimeFrame actTimeFrame = consumer.getCurrentTimeFrame(timeStamp);
            if (actTimeFrame != null) {
                if (OnOffType.OFF.toString().equals(newState)) {
                    if (!actTimeFrame.isTimestampActivatedSet()) {
                        return true;
                    }
                    Long currentRuntime = timeStamp - actTimeFrame.getTimestampActivated()
                            + actTimeFrame.getCurrentRuntime();
                    if (currentRuntime < actTimeFrame.getMaxRunningTime()) {
                        actTimeFrame.setCurrentRuntime(currentRuntime.intValue());
                    } else {
                        actTimeFrame.setCurrentRuntime(0);
                        actTimeFrame.setTimestampActivated(null);
                    }
                } else {
                    actTimeFrame.setTimestampActivated(timeStamp);
                }
            }
        }
        return true;
    }

    public boolean isPersistanceAvailible(Item item, Map<String, QueryablePersistenceService> persistenceServices) {
        // Fallback to first persistenceService from list
        if (!persistenceServices.entrySet().iterator().hasNext()) {
            logger.debug("No Persistence service found.");
            return false;
        }
        Date currentDate = new Date();
        long timeStamp = currentDate.getTime();

        FilterCriteria filter = new FilterCriteria().setItemName(item.getName()).setEndDate(new Date(timeStamp))
                .setBeginDate(new Date(timeStamp - 60000));

        Iterator<Entry<String, QueryablePersistenceService>> pit = persistenceServices.entrySet().iterator();
        QueryablePersistenceService persistenceService = pit.next().getValue();
        // Get the data from the persistence store
        Iterable<HistoricItem> result = persistenceService.query(filter);
        Iterator<HistoricItem> it = result.iterator();
        boolean forceStop = false;
        while (!forceStop && !it.hasNext()) {
            if (pit.hasNext()) {
                persistenceService = pit.next().getValue();
                result = persistenceService.query(filter);
            } else {
                logger.debug("No persisted data found for item {} found, From:  {}/{}, To: {}/{}", item,
                        timeStamp - 60000, new Date(timeStamp - 60000), timeStamp, new Date(timeStamp));
                // no persisted data found for this item in any of
                // the available persistence services
                forceStop = true;
                return false;
            }
        }
        return true;
    }

    private List<Double> getHistoryValue(Item item, Map<String, QueryablePersistenceService> persistenceServices,
            long begin, long end) {
        logger.debug("Querying persistence for history of Item {}, from {} to {}", item.getName(), begin, end);
        List<Double> values = new ArrayList<Double>();
        // Fallback to first persistenceService from list
        if (!persistenceServices.entrySet().iterator().hasNext()) {
            logger.debug("No Persistence service found.");
            return null;
        }

        FilterCriteria filter = new FilterCriteria().setItemName(item.getName()).setEndDate(new Date(end))
                .setBeginDate(new Date(begin));

        Iterator<Entry<String, QueryablePersistenceService>> pit = persistenceServices.entrySet().iterator();
        QueryablePersistenceService persistenceService = pit.next().getValue();
        // Get the data from the persistence store
        Iterable<HistoricItem> result = persistenceService.query(filter);
        Iterator<HistoricItem> it = result.iterator();
        boolean forceStop = false;
        while (!forceStop && !it.hasNext()) {
            if (pit.hasNext()) {
                persistenceService = pit.next().getValue();
                result = persistenceService.query(filter);
            } else {
                // no persisted data found for this item in any of
                // the available persistence services
                forceStop = true;
                return null;
            }
        }
        if (it.hasNext()) {
            logger.debug("persisted data for item {} found in service {}", item.getName(), persistenceService.getId());
        }

        while (it.hasNext()) {
            HistoricItem historicItem = it.next();
            logger.debug("Item: {}", historicItem.getState().toString());
            if (historicItem.getState().toString().isEmpty()) {
                continue;
            }
            values.add(Double.valueOf(historicItem.getState().toString()));
        }
        return values;
    }

    public Double calculateStandardDeviation(List<Double> numArray) {
        Double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.size();
        for (Double num : numArray) {
            sum += num;
        }
        Double mean = sum / length;
        for (Double num : numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        return Math.sqrt(standardDeviation / length);
    }
}
