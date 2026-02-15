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
package org.openhab.binding.viessmann.internal.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.viessmann.internal.dto.features.FeatureCommands;
import org.openhab.binding.viessmann.internal.util.ViessmannUtil;

/**
 * Superclass for all Thing message types.
 * 
 * @author Ronny Grun - Initial contribution
 */
public class ThingMessageDTO {
    private String type;
    private String channelType;
    private String uom;
    private String value;
    private String featureClear;
    private String featureName;
    private String featureDescription;
    private String deviceId;
    private String suffix;
    private String unit;
    private final Map<String, String> properties = new HashMap<>();
    public FeatureCommands commands;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getChannelType() {
        return channelType;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFeature() {
        if (suffix.isEmpty()) {
            return featureClear;
        } else {
            return featureClear + "#" + suffix;
        }
    }

    public String getFeatureClear() {
        return featureClear;
    }

    public void setFeatureClear(String featureClear) {
        this.featureClear = featureClear;
    }

    public String getFeatureName() {
        if (suffix.isEmpty() || "schedule".equals(suffix)) {
            return featureName;
        }
        return featureName + " " + suffix;
    }

    public String getFeatureNameClear() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureDescription() {
        return featureDescription;
    }

    public void setFeatureDescription(String featureDescription) {
        this.featureDescription = featureDescription;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public FeatureCommands getCommands() {
        return commands;
    }

    public void setCommands(FeatureCommands commands) {
        this.commands = commands;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        if ("value".equals(suffix) || "name".equals(suffix) || "entries".equals(suffix)
                || "overlapAllowed".equals(suffix)) {
            this.suffix = "";
        } else {
            this.suffix = suffix;
        }
    }

    public String getChannelId() {
        StringBuilder sb = new StringBuilder();
        sb.append(featureClear.replace(".", "-"));
        if (!suffix.isEmpty()) {
            sb.append("#").append(suffix);
        }
        return ViessmannUtil.camelToHyphen(sb.toString());
    }

    public String getSubChannelId() {
        StringBuilder sb = new StringBuilder();
        String f = featureClear.replace(".", ";");
        String[] parts = f.split(";");
        int count = 0;
        for (String str : parts) {
            if (count != 0) {
                sb.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));
            } else {
                sb.append(str);
            }
            count++;
        }
        sb.append("#").append(suffix);
        return sb.toString();
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getCircuitId() {
        String circuitId = "";
        Pattern pattern = Pattern.compile("(\\.[0-3])");
        Matcher matcher = pattern.matcher(featureClear);
        if (matcher.find()) {
            circuitId = matcher.group(0);
            circuitId = circuitId.replace(".", "");
        }
        return circuitId;
    }
}
