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
package org.openhab.binding.hdpowerview.internal.api.responses;

import java.util.List;
import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.SurveyData;

import com.google.gson.annotations.SerializedName;

/**
 * Survey data of a single Shade, as returned by an HD PowerView hub
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class Survey {
    @SerializedName("shade_id")
    public int shadeId;
    @Nullable
    @SerializedName("survey")
    public List<SurveyData> surveyData;

    @Override
    public String toString() {
        List<SurveyData> surveyData = this.surveyData;
        if (surveyData == null) {
            return "{}";
        }
        StringJoiner joiner = new StringJoiner(", ");
        surveyData.forEach(data -> joiner.add(data.toString()));
        return joiner.toString();
    }
}
