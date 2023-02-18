package org.smslib.pduUtils.gsm3040;

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
 * Extracted from SMSLib
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
