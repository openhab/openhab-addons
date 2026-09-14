/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lghorizon.internal.api.dto;

import java.util.Map;

import com.google.gson.JsonObject;

/**
 * The {@code /config-service/conf/web/backoffice.json} document. It is a flat map of service-name to an object
 * containing (at least) a {@code URL} field, e.g. {@code personalizationService.URL}, {@code mqttBroker.URL}, etc.
 * <p>
 * Modelled as a raw {@link JsonObject} because the set of services differs slightly per provider and new ones are added
 * over time.
 *
 * @author Mark - Initial contribution
 */
public class ServiceConfigDto {

    private final Map<String, JsonObject> services;

    public ServiceConfigDto(Map<String, JsonObject> services) {
        this.services = services;
    }

    public String getServiceUrl(String serviceName) throws IllegalArgumentException {
        JsonObject service = services.get(serviceName);
        if (service == null || !service.has("URL")) {
            throw new IllegalArgumentException("Service URL for '" + serviceName + "' not found in configuration");
        }
        String url = service.get("URL").getAsString();
        // Workaround for a historically broken Ziggo NL EPG host.
        if (url.contains("static.spark.ziggogo.tv")) {
            url = url.replace("static.spark.ziggogo.tv", "staticqbr-prod-nl.gnp.cloud.ziggogo.tv");
        }
        return url;
    }

    @Override
    public String toString() {
        return "ServiceConfigDto [services=" + services + "]";
    }
}
