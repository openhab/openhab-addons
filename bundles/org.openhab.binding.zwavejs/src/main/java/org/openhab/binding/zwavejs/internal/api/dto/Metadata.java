/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwavejs.internal.api.dto;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * @author Leo Siepel - Initial contribution
 */
public class Metadata {
    public Object comments;
    public MetadataType type;
    public boolean readable;
    public boolean writeable;
    public String label;
    public List<String> valueChangeOptions;
    public boolean stateful;
    public boolean secret;
    public CcSpecific ccSpecific;
    public String unit;
    public Map<String, String> states;
    @SerializedName("default")
    public Object defaultValue;
    public Integer min;
    public Long max;
    public Integer steps;
    public int valueSize;
    public int format;
    public boolean allowManualEntry;
    public boolean isFromConfig;
    public String description;
}
