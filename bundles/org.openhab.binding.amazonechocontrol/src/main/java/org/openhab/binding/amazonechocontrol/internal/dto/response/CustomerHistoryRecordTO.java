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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link CustomerHistoryRecordTO} encapsulates
 *
 * @author Jan N. Klug - Initial contribution
 */
public class CustomerHistoryRecordTO {
    public String recordKey;
    public String recordType;
    public long timestamp;
    public String customerId;
    public Object device;
    public boolean isBinaryFeedbackProvided;
    public boolean isFeedbackPositive;
    public String utteranceType;
    public String domain;
    public String intent;
    public String skillName;
    public List<CustomerHistoryRecordVoiceTO> voiceHistoryRecordItems = List.of();
    public List<Object> personsInfo = List.of();

    @Override
    public @NonNull String toString() {
        return "CustomerHistoryRecordTO{recordKey='" + recordKey + "', recordType='" + recordType + "', timestamp="
                + timestamp + ", customerId='" + customerId + "', device=" + device + ", isBinaryFeedbackProvided="
                + isBinaryFeedbackProvided + ", isFeedbackPositive=" + isFeedbackPositive + ", utteranceType='"
                + utteranceType + "', domain='" + domain + "', intent='" + intent + "', skillName='" + skillName
                + "', voiceHistoryRecordItems=" + voiceHistoryRecordItems + ", personsInfo=" + personsInfo + "}";
    }
}
