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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.openhab.io.semp.internal.SEMPConstants.SEMPMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for SSDP/SEMP Informations
 *
 * @author Markus Eckhardt - Initial Contribution
 *
 */
@SuppressWarnings("serial")
public class SEMPCommandServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(SEMPCommandServlet.class);
    private static final String PATH_SEMP = "/semp";
    private static final String SUPPORTED_GROUP_TAG = "Consumer";
    private static final List<String> SUPPORTED_ITEM_TYPES = Arrays.asList("Switch", "Number", "Contact");
    private static final List<String> SUPPORTED_CONTROL_TYPES = Arrays.asList("Switch");
    private static final List<String> SUPPORTED_ENERGY_TYPES = Arrays.asList("Number");
    private static final List<String> SUPPORTED_INPUT_TYPES = Arrays.asList("Contact");
    private static final String TAG_INPUT_IS_CONNECTED = "Connected";
    private static final String TAG_INPUT_IS_LISTENING = "Listening";
    private static final String SEMP_DEVICE_ID = "DeviceId";

    private EventPublisher eventPublisher;
    private ItemRegistry itemRegistry;
    private Map<String, QueryablePersistenceService> persistenceServices = new HashMap<>();
    private Map<String, SEMPConsumer> consumerMap = Collections
            .synchronizedMap(new LinkedHashMap<String, SEMPConsumer>());
    private SEMPItemCommunication itemCommunication = new SEMPItemCommunication();
    private SEMPXmlTools xmlTools = new SEMPXmlTools();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message = null;
        String path = req.getRequestURI();
        logger.debug("command {}: {} {} {}", req.getRemoteAddr(), req.getMethod(), path, req.getQueryString());
        if (path.startsWith(PATH_SEMP)) {
            String deviceID = "";
            determineSEMPConsumers();
            // remove baseURI and remove trailing '/' if existing
            String cmd = path.substring(PATH_SEMP.length());
            if (cmd.endsWith("/")) {
                cmd = cmd.substring(0, cmd.length() - 1);
            }
            if (req.getQueryString() != null && req.getQueryString().startsWith(SEMP_DEVICE_ID)) {
                deviceID = req.getQueryString().substring(SEMP_DEVICE_ID.length() + 1);
            }
            if (req.getMethod().equals(HttpMethod.GET)) {
                logger.debug("Com: {} Querry: {} Consumers: {}", cmd, req.getQueryString(), consumerMap.keySet());
                if (!itemCommunication.itemsToDevice(consumerMap, deviceID, persistenceServices)) {
                    logger.error("Error in item determination");
                    resp.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
                    return;
                }
                if ("".equals(cmd)) {
                    logger.debug("Req /SEMP");
                    message = xmlTools.createXMLMessage(Arrays.asList(SEMPMessageType.MSG_DEVICE_INFO,
                            SEMPMessageType.MSG_DEVICE_STATUS, SEMPMessageType.MSG_TIMEFRAME), consumerMap, deviceID);
                }
                if ("/DeviceInfo".equals(cmd)) {
                    message = xmlTools.createXMLMessage(Arrays.asList(SEMPMessageType.MSG_DEVICE_INFO), consumerMap,
                            deviceID);
                }
                if ("/DeviceStatus".equals(cmd)) {
                    message = xmlTools.createXMLMessage(Arrays.asList(SEMPMessageType.MSG_DEVICE_STATUS), consumerMap,
                            deviceID);
                }
                if ("/PlanningRequest".equals(cmd)) {
                    message = xmlTools.createXMLMessage(Arrays.asList(SEMPMessageType.MSG_TIMEFRAME), consumerMap,
                            deviceID);
                }
                if (message != null) {
                    resp.setContentType("application/xml");
                    try (PrintWriter out = resp.getWriter()) {
                        logger.debug("Device /semp: {}", message);
                        out.write(message);
                        if (out.checkError()) {
                            logger.error("Network write error");
                        }
                    }
                    resp.setStatus(Response.Status.OK.getStatusCode());
                }
            } else if (req.getMethod().equals(HttpMethod.POST)) {
                ServletInputStream xmlStream = req.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[10240];
                int len;
                while ((len = xmlStream.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                deviceID = xmlTools.getXMLValue(new ByteArrayInputStream(baos.toByteArray()),
                        "/EM2Device/DeviceControl/DeviceId");
                logger.debug("deviceID: {}", deviceID);
                String val = xmlTools.getXMLValue(new ByteArrayInputStream(baos.toByteArray()),
                        "/EM2Device/DeviceControl/On");
                if (deviceID == null || val == null) {
                    resp.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
                    return;
                }
                logger.debug("Val: {}", val);
                synchronized (consumerMap) {
                    if (!itemCommunication.deviceToItems(consumerMap, Boolean.parseBoolean(val), deviceID,
                            eventPublisher)) {
                        resp.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
                        return;
                    }
                }
                resp.setStatus(Response.Status.OK.getStatusCode());
            }
        }
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    public void addPersistenceService(PersistenceService service) {
        if (service instanceof QueryablePersistenceService) {
            persistenceServices.put(service.getId(), (QueryablePersistenceService) service);
        }
    }

    public void removePersistenceService(PersistenceService service) {
        persistenceServices.remove(service.getId());
    }

    private void determineSEMPConsumers() {
        List<String> actConsumers = new ArrayList<String>();
        List<String> toRemove = new ArrayList<String>();

        for (Item item : itemRegistry.getItemsByTagAndType("Group", SUPPORTED_GROUP_TAG)) {
            logger.debug("Group: {} : {} :{}", item.getName(), item.getLabel(), item.getType());
            actConsumers.add(item.getName());
            SEMPConsumer consumer = new SEMPConsumer();
            if (!consumerMap.containsKey(item.getName())) {
                consumerMap.put(item.getName(), consumer);
            }
            consumerMap.get(item.getName()).setGroupItem(item);
        }
        for (String consumer : consumerMap.keySet()) {
            if (!actConsumers.contains(consumer)) {
                toRemove.add(consumer);
            } else {
                consumerMap.get(consumer).unsetItems();
            }
        }
        for (String consumer : toRemove) {
            consumerMap.remove(consumer);
        }
        for (String type : SUPPORTED_ITEM_TYPES) {
            for (Item item : itemRegistry.getItemsOfType(type)) {
                for (String groupName : item.getGroupNames()) {
                    if (consumerMap.containsKey(groupName)) {
                        logger.debug("Item: {} Group: {}", item.getName(), groupName);
                        SEMPConsumer consumer = consumerMap.get(groupName);
                        if (SUPPORTED_CONTROL_TYPES.contains(type)) {
                            consumer.setControlItem(item);
                        }
                        if (SUPPORTED_ENERGY_TYPES.contains(type)) {
                            consumer.setEnergyItem(item);
                        }
                        if (SUPPORTED_INPUT_TYPES.contains(type)) {
                            for (String inputType : item.getTags()) {
                                if (TAG_INPUT_IS_CONNECTED.equals(inputType)) {
                                    logger.debug("Connected Item: {}", item.getName());
                                    consumer.setConnectionItem(item);
                                }
                                if (TAG_INPUT_IS_LISTENING.equals(inputType)) {
                                    logger.debug("Listening Item: {}", item.getName());
                                    consumer.setListeningItem(item);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (String consumer : consumerMap.keySet()) {
            consumerMap.get(consumer).getTimeFrames().clear();
            consumerMap.get(consumer).getDaysOfTheWeek().clear();
            if (!consumerMap.get(consumer).checkItemsTags()) {
                consumerMap.remove(consumer);
            } else {
                consumerMap.get(consumer).setDefaultTags();
            }
        }
    }
}
