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
package org.openhab.io.imperihome.internal.handler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.persistence.PersistenceServiceRegistry;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.imperihome.internal.model.HistoryItem;
import org.openhab.io.imperihome.internal.model.HistoryList;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;
import org.openhab.io.imperihome.internal.processor.DeviceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device history request handler.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class DeviceHistoryHandler {

    private static final String CHARSET = "UTF-8";

    private final Logger logger = LoggerFactory.getLogger(DeviceHistoryHandler.class);

    private final DeviceRegistry deviceRegistry;
    private final PersistenceServiceRegistry persistenceServiceRegistry;

    public DeviceHistoryHandler(DeviceRegistry deviceRegistry, PersistenceServiceRegistry persistenceServiceRegistry) {
        this.deviceRegistry = deviceRegistry;
        this.persistenceServiceRegistry = persistenceServiceRegistry;
    }

    public HistoryList handle(HttpServletRequest req, Matcher urlMatcher) {
        String deviceId, field;
        long start, end;
        try {
            deviceId = URLDecoder.decode(urlMatcher.group(1), CHARSET);
            field = URLDecoder.decode(urlMatcher.group(2), CHARSET);
            start = Long.parseLong(urlMatcher.group(3));
            end = Long.parseLong(urlMatcher.group(4));
        } catch (UnsupportedEncodingException | NumberFormatException e) {
            throw new RuntimeException("Could not decode request params", e);
        }

        logger.debug("History request for device {}, field {}: {}-{}", deviceId, field, start, end);

        AbstractDevice device = deviceRegistry.getDevice(deviceId);
        if (device == null) {
            logger.warn("Received history request for unknown device: {}", urlMatcher.group(0));
            return null;
        }

        PersistenceService persistence = persistenceServiceRegistry.getDefault();
        if (persistence == null) {
            logger.warn("Could not retrieve default persistence service; can't serve history request");
            return null;
        }
        if (!(persistence instanceof QueryablePersistenceService)) {
            logger.warn("Default persistence service is not queryable; can't serve history request");
            return null;
        }

        return serveHistory(device, (QueryablePersistenceService) persistence, start, end);
    }

    private HistoryList serveHistory(AbstractDevice device, QueryablePersistenceService persistence, long start,
            long end) {
        logger.info("Querying persistence for history of Item {}, from {} to {}", device.getItemName(), start, end);

        FilterCriteria criteria = new FilterCriteria().setItemName(device.getItemName()).setBeginDate(new Date(start))
                .setEndDate(new Date(end));

        List<HistoryItem> resultItems = new LinkedList<>();
        Iterable<HistoricItem> historicItems = persistence.query(criteria);

        Iterator<HistoricItem> iterator = historicItems.iterator();
        if (!iterator.hasNext()) {
            logger.info("Persistence returned no results for history query");
        } else {
            while (iterator.hasNext()) {
                HistoricItem historicItem = iterator.next();
                State state = historicItem.getState();
                if (state instanceof DecimalType) {
                    Number value = ((DecimalType) state).toBigDecimal();
                    resultItems.add(new HistoryItem(historicItem.getTimestamp(), value));
                }
            }

            if (resultItems.isEmpty()) {
                logger.warn(
                        "Persistence returned results for history query, but could not be interpreted as DecimalTypes");
            }
        }

        return new HistoryList(resultItems);
    }

}
