/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.local.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Pojo for frame.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Frame {
    private String icon;
    private String text;
    @SerializedName("goalData")
    private GoalData goalData;
    @SerializedName("chartData")
    private List<Integer> chartData;
    private Integer index;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Frame withIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Frame withText(String text) {
        this.text = text;
        return this;
    }

    public GoalData getGoalData() {
        return goalData;
    }

    public void setGoalData(GoalData goalData) {
        this.goalData = goalData;
    }

    public Frame withGoalData(GoalData goalData) {
        this.goalData = goalData;
        return this;
    }

    public List<Integer> getChartData() {
        return chartData;
    }

    public void setChartData(List<Integer> chartData) {
        this.chartData = chartData;
    }

    public Frame withChartData(List<Integer> chartData) {
        this.chartData = chartData;
        return this;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Frame withIndex(Integer index) {
        this.index = index;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Frame [icon=");
        builder.append(icon);
        builder.append(", text=");
        builder.append(text);
        builder.append(", goalData=");
        builder.append(goalData);
        builder.append(", chartData=");
        builder.append(chartData);
        builder.append(", index=");
        builder.append(index);
        builder.append("]");
        return builder.toString();
    }
}
