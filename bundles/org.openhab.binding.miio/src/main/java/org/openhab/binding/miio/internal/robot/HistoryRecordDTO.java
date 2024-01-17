/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.robot;

import java.math.BigDecimal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the history record message json structure
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class HistoryRecordDTO {

    @SerializedName("start")
    @Expose
    private String start;
    @SerializedName("begin")
    @Expose
    private String begin;
    @SerializedName("end")
    @Expose
    private String end;
    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("area")
    @Expose
    private BigDecimal area;
    @SerializedName("clean_time")
    @Expose
    private Integer cleanTime;
    @SerializedName("error")
    @Expose
    private Integer error;
    @SerializedName("finished")
    @Expose
    private Integer finished;
    @SerializedName("complete")
    @Expose
    private Integer complete;
    @SerializedName("start_type")
    @Expose
    private Integer startType;
    @SerializedName("clean_type")
    @Expose
    private Integer cleanType;
    @SerializedName("finish_reason")
    @Expose
    private Integer finishReason;
    @SerializedName("dust_collection_status")
    @Expose
    private Integer dustCollectionStatus;
    @SerializedName("map_flag")
    @Expose
    private Integer mapFlag;

    public final String getStart() {
        return start != null ? start : begin;
    }

    public final void setStart(String start) {
        this.start = start;
    }

    public final String getEnd() {
        return end;
    }

    public final void setEnd(String end) {
        this.end = end;
    }

    public final Integer getDuration() {
        return duration;
    }

    public final void setDuration(Integer duration) {
        this.duration = duration;
    }

    public final BigDecimal getArea() {
        return area;
    }

    public final void setArea(BigDecimal area) {
        this.area = area;
    }

    public final Integer getCleanTime() {
        return cleanTime;
    }

    public final void setCleanTime(Integer cleanTime) {
        this.cleanTime = cleanTime;
    }

    public final Integer getError() {
        return error;
    }

    public final void setError(Integer error) {
        this.error = error;
    }

    public final Integer getFinished() {
        return finished != null ? finished : complete;
    }

    public final void setFinished(Integer finished) {
        this.finished = finished;
    }

    public final Integer getStartType() {
        return startType;
    }

    public final void setStartType(Integer startType) {
        this.startType = startType;
    }

    public final Integer getCleanType() {
        return cleanType;
    }

    public final void setCleanType(Integer cleanType) {
        this.cleanType = cleanType;
    }

    public final Integer getFinishReason() {
        return finishReason;
    }

    public final void setFinishReason(Integer finishReason) {
        this.finishReason = finishReason;
    }

    public final Integer getDustCollectionStatus() {
        return dustCollectionStatus;
    }

    public final void setDustCollectionStatus(Integer dustCollectionStatus) {
        this.dustCollectionStatus = dustCollectionStatus;
    }

    public final Integer getMapFlag() {
        return mapFlag;
    }

    public final void setMapFlag(Integer mapFlag) {
        this.mapFlag = mapFlag;
    }
}
