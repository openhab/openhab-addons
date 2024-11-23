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
package org.openhab.binding.tibber.internal.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TibberPriceConsumptionHandler} class contains fields mapping price info parameters.
 *
 * @author Stian Kjoglum - Initial contribution
 */
@NonNullByDefault
public class TibberPriceConsumptionHandler {

    public InputStream connectionInputStream(String homeId) {
        String connectionquery = "{\"query\": \"{viewer {home (id: \\\"" + homeId + "\\\") {id }}}\"}";
        return new ByteArrayInputStream(connectionquery.getBytes(StandardCharsets.UTF_8));
    }

    public InputStream getInputStream(String homeId) {
        String query = "{\"query\": \"{viewer {home (id: \\\"" + homeId
                + "\\\") {currentSubscription {priceInfo {current {total startsAt level } tomorrow { startsAt total } today { startsAt total }}} daily: consumption(resolution: DAILY, last: 1) {nodes {from to cost unitPrice consumption consumptionUnit}} hourly: consumption(resolution: HOURLY, last: 1) {nodes {from to cost unitPrice consumption consumptionUnit}}}}}\"}";
        return new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
    }

    public InputStream getRealtimeInputStream(String homeId) {
        String realtimeenabledquery = "{\"query\": \"{viewer {home (id: \\\"" + homeId
                + "\\\") {features {realTimeConsumptionEnabled }}}}\"}";
        return new ByteArrayInputStream(realtimeenabledquery.getBytes(StandardCharsets.UTF_8));
    }

    public InputStream getWebsocketUrl() {
        String websocketquery = "{\"query\": \"{viewer {websocketSubscriptionUrl }}\"}";
        return new ByteArrayInputStream(websocketquery.getBytes(StandardCharsets.UTF_8));
    }
}
