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
package org.openhab.binding.sungrow.internal.client.dto;

import com.google.gson.annotations.SerializedName;

/**
 * @author Christian Kemper - Initial contribution
 */
public enum Language {

    @SerializedName("_zh_CN")
    CHINESE_SIMPLE,
    @SerializedName("_zh_TW")
    CHINESE_TRADITIONAL,
    @SerializedName("_en_US")
    ENGLISH,
    @SerializedName("_ja_JP")
    JAPANESE,
    @SerializedName("_es_ES")
    SPANISH,
    @SerializedName("_de_DE")
    GERMAN,
    @SerializedName("_pt_BR")
    BRAZILIAN,
    @SerializedName("_pt_BR")
    PORTUGUESE,
    @SerializedName("_fr_FR")
    FRENCH,
    @SerializedName("_it_IT")
    ITALIAN,
    @SerializedName("_ko_KR")
    KOREAN,
    @SerializedName("_nl_NL")
    DUTCH,
    @SerializedName("_pl_PL")
    POLISH,
    @SerializedName("_vi_VN")
    VIETNAMESE
}
