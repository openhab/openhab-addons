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
package org.openhab.binding.withings.internal.service.person;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.withings.internal.api.measure.LatestMeasureData;
import org.openhab.binding.withings.internal.api.measure.MeasuresHandler;
import org.openhab.binding.withings.internal.api.sleep.LatestSleepData;
import org.openhab.binding.withings.internal.api.sleep.SleepHandler;
import org.openhab.binding.withings.internal.service.AccessTokenService;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class PersonHandler {

    private final AccessTokenService accessTokenService;
    private final HttpClient httpClient;

    public PersonHandler(AccessTokenService accessTokenService, HttpClient httpClient) {
        this.accessTokenService = accessTokenService;
        this.httpClient = httpClient;
    }

    public Optional<Person> loadPerson() {
        MeasuresHandler measuresHandler = new MeasuresHandler(accessTokenService, httpClient);
        Optional<LatestMeasureData> measureData = measuresHandler.loadLatestMeasureData();

        SleepHandler sleepHandler = new SleepHandler(accessTokenService, httpClient);
        Optional<LatestSleepData> sleepData = sleepHandler.loadLatestSleepData();

        if (measureData.isPresent() || sleepData.isPresent()) {
            return Optional.of(new Person(measureData, sleepData));
        }
        return Optional.empty();
    }
}
