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
package org.openhab.binding.awtrixng.internal.dto;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * The payload of a pushed app. Every field is optional and omitted when null: AWTRIX NG validates a payload
 * all or nothing and rejects unknown keys, and durations are in milliseconds.
 *
 * @author Keith T. Garner - Initial contribution
 */
public class AppPayload {
    public JsonElement text;
    public String textCase;
    public String font;
    public String textColor;
    public Integer textBlinkMs;
    public Integer textFadeMs;
    public String textAlign;
    public Integer textOffsetX;
    public Boolean textInFront;
    public ScrollConfig scroll;
    public String icon;
    public String iconMode;
    public Integer iconOffsetX;
    public Integer iconGap;
    public List<PlacedIcon> icons;
    public Long durationMs;
    public Long lifetimeMs;
    public String lifetimeExpiry;
    public Integer repeat;
    public List<Integer> barChart;
    public List<Integer> lineChart;
    public Boolean chartAutoscale;
    public String chartColor;
    public Integer progress;
    public String progressColor;
    public String progressTrackColor;
    public String backgroundColor;
    public String effect;
    public Double effectSpeed;
    public JsonElement palette;
    public Boolean paletteBlend;
    public Integer paletteSpan;
    public Double paletteSpeed;
    public String overlay;
    public JsonArray draw;
}
