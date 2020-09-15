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
package org.openhab.binding.senechome.internal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;

/**
 * The {@link SenecHomeApiFactory} provides http client based instances to access the web api from senec devices
 *
 * @author Steven Schwarznau - Initial contribution
 */
@NonNullByDefault
@Component(service = SenecHomeApiFactory.class)
public class SenecHomeApiFactory {

    private HttpClient httpClient;

    @Activate
    public SenecHomeApiFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    public SenecHomeApi getHttpApi(SenecHomeConfigurationDTO config, Gson gson) {
        return new SenecHomeApi(this.httpClient, gson, config);
    }
}
