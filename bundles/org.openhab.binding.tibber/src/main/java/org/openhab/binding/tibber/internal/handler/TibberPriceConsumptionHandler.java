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
package org.openhab.binding.tibber.internal.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * The {@link TibberPriceConsumptionHandler} class contains fields mapping price info parameters.
 *
 * @author Stian Kjoglum - Initial contribution
 */
public class TibberPriceConsumptionHandler {

    public InputStream connectionInputStream(String homeId) {
        String connectionquery = "{\"query\": \"{viewer {home (id: \\\"" + homeId + "\\\") {id }}}\"}";
        InputStream myInputStream = new ByteArrayInputStream(connectionquery.getBytes(Charset.forName("UTF-8")));
        return myInputStream;
    }

    public InputStream getInputStream(String homeId) {
        String query = "{\"query\": \"{viewer {home (id: \\\"" + homeId
                + "\\\") {currentSubscription {priceInfo {current {total startsAt }}}}}}\"}";
        InputStream myInputStream = new ByteArrayInputStream(query.getBytes(Charset.forName("UTF-8")));
        return myInputStream;
    }

    public InputStream getRealtimeInputStream(String homeId) {
        String realtimeenabledquery = "{\"query\": \"{viewer {home (id: \\\"" + homeId
                + "\\\") {features {realTimeConsumptionEnabled }}}}\"}";
        InputStream myInputStream = new ByteArrayInputStream(realtimeenabledquery.getBytes(Charset.forName("UTF-8")));
        return myInputStream;
    }

    public InputStream getDailyInputStream(String homeId) {
        String dailyquery = "{\"query\": \"{viewer {home (id: \\\"" + homeId
                + "\\\") {daily: consumption(resolution: DAILY, last: 1) {nodes {from to cost unitPrice consumption consumptionUnit}}}}}\"}";
        InputStream myInputStream = new ByteArrayInputStream(dailyquery.getBytes(Charset.forName("UTF-8")));
        return myInputStream;
    }

    public InputStream getHourlyInputStream(String homeId) {
        String hourlyquery = "{\"query\": \"{viewer {home (id: \\\"" + homeId
                + "\\\") {hourly: consumption(resolution: HOURLY, last: 1) {nodes {from to cost unitPrice consumption consumptionUnit}}}}}\"}";
        InputStream myInputStream = new ByteArrayInputStream(hourlyquery.getBytes(Charset.forName("UTF-8")));
        return myInputStream;
    }
}
