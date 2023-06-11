/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.lametrictime.api.local.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Frame
{
    private String icon;
    private String text;
    @SerializedName("goalData")
    private GoalData goalData;
    @SerializedName("chartData")
    private List<Integer> chartData;
    private Integer index;

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public Frame withIcon(String icon)
    {
        this.icon = icon;
        return this;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public Frame withText(String text)
    {
        this.text = text;
        return this;
    }

    public GoalData getGoalData()
    {
        return goalData;
    }

    public void setGoalData(GoalData goalData)
    {
        this.goalData = goalData;
    }

    public Frame withGoalData(GoalData goalData)
    {
        this.goalData = goalData;
        return this;
    }

    public List<Integer> getChartData()
    {
        return chartData;
    }

    public void setChartData(List<Integer> chartData)
    {
        this.chartData = chartData;
    }

    public Frame withChartData(List<Integer> chartData)
    {
        this.chartData = chartData;
        return this;
    }

    public Integer getIndex()
    {
        return index;
    }

    public void setIndex(Integer index)
    {
        this.index = index;
    }

    public Frame withIndex(Integer index)
    {
        this.index = index;
        return this;
    }

    @Override
    public String toString()
    {
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
