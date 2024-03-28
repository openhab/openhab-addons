/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDataType;

/**
 * The {@link FeatureDefinition} defines the feature definitions extracted from the capability files in
 * the MonitoringValue/Value session. All features are read-only by default. The factory must change-it if
 * a specific one can be represented by a Writable Channel.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class FeatureDefinition {
    public static final FeatureDefinition NULL_DEFINITION = new FeatureDefinition();
    String name = "";
    String label = "";
    Boolean readOnly = true;
    FeatureDataType dataType = FeatureDataType.UNDEF;
    Map<String, String> valuesMapping = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public FeatureDataType getDataType() {
        return dataType;
    }

    public Boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setDataType(FeatureDataType dataType) {
        this.dataType = dataType;
    }

    public Map<String, String> getValuesMapping() {
        return valuesMapping;
    }

    public void setValuesMapping(Map<String, String> valuesMapping) {
        this.valuesMapping = valuesMapping;
    }
}
