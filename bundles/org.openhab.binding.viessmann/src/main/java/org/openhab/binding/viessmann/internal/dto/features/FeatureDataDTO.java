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
package org.openhab.binding.viessmann.internal.dto.features;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.viessmann.internal.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The {@link FeatureDataDTO} provides all data of a feature
 *
 * @author Ronny Grun - Initial contribution
 */
public class FeatureDataDTO {

    private final Logger logger = LoggerFactory.getLogger(FeatureDataDTO.class);

    public Integer apiVersion;
    public Boolean isEnabled;
    public Boolean isReady;
    public String gatewayId;
    public String feature;
    public String uri;
    public String deviceId;
    public String timestamp;
    public FeatureProperties properties;
    public Map<String, FeatureCommand> commands;
    public List<String> components = null;
    @JsonIgnore
    public String installationId;

    public void setInstallationId(String id) {
        this.installationId = id;
    }

    /**
     * @return formatted anonymised JSON representation of this DTO (null on error)
     */
    public @Nullable String toPrettyJson() {
        try {
            String json = JsonUtil.toPrettyJson(this);

            if (gatewayId != null) {
                json = json.replace("\"" + gatewayId + "\"", "\"{{gatewaySerial}}\"").replace("/" + gatewayId + "/",
                        "/{{gatewaySerial}}/");
            }
            if (installationId != null) {
                json = json.replace("/" + installationId + "/", "/{{installationId}}/");
            }
            return json;
        } catch (Exception e) {
            logger.debug("FeatureDataDTO serialization failed", e);
            return null;
        }
    }

    /**
     * @return formatted JSON representation of this DTO (null on error)
     */
    public @Nullable String toJson() {
        try {
            return JsonUtil.toJson(this);
        } catch (Exception e) {
            logger.debug("FeatureDataDTO serialization failed", e);
            return null;
        }
    }
}
