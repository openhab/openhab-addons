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

package org.openhab.binding.smsmodem.internal.smslib.pduUtils.gsm3040;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

//PduUtils Library - A Java library for generating GSM 3040 Protocol Data Units (PDUs)
//
//Copyright (C) 2008, Ateneo Java Wireless Competency Center/Blueblade Technologies, Philippines.
//PduUtils is distributed under the terms of the Apache License version 2.0
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

/**
 *
 * Extracted from SMSLib
 *
 * @author Gwendal ROULLEAU - Initial contribution, extracted from SMSLib
 */
@NonNullByDefault
public class SmsDeliveryPdu extends Pdu {
    // can only create via the factory
    SmsDeliveryPdu() {
    }

    // ==================================================
    // TIMESTAMP
    // ==================================================
    private @Nullable Calendar timestamp;

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public @Nullable Date getTimestamp() {
        Calendar timestampFinal = this.timestamp;
        return timestampFinal == null ? null : timestampFinal.getTime();
    }
}
