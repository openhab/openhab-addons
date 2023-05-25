/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.romyrobot.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.romyrobot.internal.RomyRobotConfiguration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link RomyApiFactory} class is used for creating instances of
 * the Romy API classes to interact with the RomyRobot HTTP API.
 *
 * @author Bernhard Kreuz - Initial contribution
 */
@Component(service = RomyApiFactory.class)
@NonNullByDefault
public class RomyApiFactory {
    private HttpClient httpClient;

    @Activate
    public RomyApiFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    public RomyApi getHttpApi(RomyRobotConfiguration config) {
        return new RomyApiAGON_1_2_4_release_3_14_3068(this.httpClient, config);
    }
}
