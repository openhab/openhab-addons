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
package org.openhab.binding.unifi.internal.api.dto;

import java.time.Instant;

import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.util.UniFiTimestampDeserializer;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link UniFiVoucher} is the base data model for a guest network voucher
 *
 * @author Mark Herwege - Initial contribution
 */
public class UniFiVoucher implements HasId {

    private final transient UniFiControllerCache cache;

    @SerializedName("_id")
    private String id;

    private String siteId;

    private String code;
    @JsonAdapter(UniFiTimestampDeserializer.class)
    private Instant createTime;
    private Integer duration;
    private Integer quota;
    private Integer used;
    private Integer qosUsageQuota;
    private Integer qosRateMaxUp;
    private Integer qosRateMaxDown;
    private boolean qosOverwrite;
    private String note;
    private String status;

    public UniFiVoucher(final UniFiControllerCache cache) {
        this.cache = cache;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public Integer getQuota() {
        return quota;
    }

    public Integer getUsed() {
        return used;
    }

    public Integer getQosUsageQuota() {
        return qosUsageQuota;
    }

    public Integer getQosRateMaxUp() {
        return qosRateMaxUp;
    }

    public Integer getQosRateMaxDown() {
        return qosRateMaxDown;
    }

    public boolean isQosOverwrite() {
        return qosOverwrite;
    }

    public String getNote() {
        return note;
    }

    public String getStatus() {
        return status;
    }

    public UniFiSite getSite() {
        return cache.getSite(siteId);
    }

    @Override
    public String toString() {
        return String.format(
                """
                        UniFiVoucher{id: '%s', code: '%s', created: '%s', duration: '%s', quota: '%s', used: '%s', qosUsageQuota: '%s', \
                        qosRateMaxUp: '%s', qosRateMaxDown: '%s', qosOverwrite: '%s', note: '%s', status: '%s', site: %s}\
                        """,
                id, code, createTime, duration, quota, used, qosUsageQuota, qosRateMaxUp, qosRateMaxDown, qosOverwrite,
                note, status, getSite());
    }
}
