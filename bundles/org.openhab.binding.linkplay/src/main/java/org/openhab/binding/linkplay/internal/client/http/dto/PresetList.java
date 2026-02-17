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
package org.openhab.binding.linkplay.internal.client.http.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Preset list.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class PresetList {
    public int presetNum;

    public List<Preset> presetList;

    public static class Preset {
        public int number;
        public String name;
        public String url;
        public String source;
        @SerializedName("picurl")
        public String picUrl;
    }
}
