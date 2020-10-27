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
package org.openhab.binding.withings.internal.api.sleep;

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.withings.internal.api.AbstractAPIHandler;
import org.openhab.binding.withings.internal.service.AccessTokenService;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class SleepHandler extends AbstractAPIHandler {

    private static final String MEASURE_API_URL = "https://wbsapi.withings.net/v2/sleep";

    public SleepHandler(AccessTokenService accessTokenService, HttpClient httpClient) {
        super(accessTokenService, httpClient);
    }

    public Optional<LatestSleepData> loadLatestSleepData() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("data_fields", "sleep_score");
        parameters.put("lastupdate", "");

        Optional<SleepResponse> sleepResponse = executePOSTRequest(MEASURE_API_URL, "getsummary", parameters,
                SleepResponse.class);

        if (sleepResponse.isPresent()) {
            SleepResponse.SleepBody body = sleepResponse.get().getBody();
            if (body != null) {
                List<SleepResponse.SleepSeries> sleepSeries = body.getSeries();
                if (sleepSeries != null && !sleepSeries.isEmpty()) {
                    sleepSeries.sort(Comparator.reverseOrder());

                    SleepResponse.SleepSeries latestSleepSeries = sleepSeries.get(0);
                    return Optional.of(new LatestSleepData(latestSleepSeries.getStartDate(),
                            latestSleepSeries.getEndDate(), latestSleepSeries.getData()));
                }
            }
        }
        return Optional.empty();
    }
}
