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

package org.openhab.binding.smsmodem.internal.smslib.message;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smsmodem.internal.smslib.pduUtils.gsm3040.SmsStatusReportPdu;

/**
 *
 * Extracted from SMSLib
 *
 * @author Gwendal ROULLEAU - Initial contribution, extracted from SMSLib
 */
@NonNullByDefault
public class DeliveryReportMessage extends InboundMessage {
    private static final long serialVersionUID = 1L;

    public enum DeliveryStatus {
        Unknown("U"),
        Pending("P"),
        Failed("F"),
        Delivered("D"),
        Expired("X"),
        Error("E");

        private final String shortString;

        private DeliveryStatus(String shortString) {
            this.shortString = shortString;
        }

        public String toShortString() {
            return this.shortString;
        }
    }

    DeliveryStatus deliveryStatus = DeliveryStatus.Unknown;

    @Nullable
    String originalOperatorMessageId;

    @Nullable
    Date originalReceivedDate;

    public DeliveryReportMessage() {
        super(Type.StatusReport, "", 0);
    }

    public DeliveryReportMessage(SmsStatusReportPdu pdu, String memLocation, int memIndex) {
        super(Type.StatusReport, memLocation, memIndex);
        setOriginalOperatorMessageId(String.valueOf(pdu.getMessageReference()));
        String address = pdu.getAddress();
        if (address == null) {
            throw new IllegalArgumentException("Recipient address cannot be null");
        }
        this.recipientAddress = new MsIsdn(address);
        Date timestamp = pdu.getTimestamp();
        if (timestamp == null) {
            throw new IllegalArgumentException("Cannot get timestamp for delivery report message");
        }
        setSentDate(timestamp);
        Date dischargeTime = pdu.getDischargeTime();
        if (dischargeTime == null) {
            throw new IllegalArgumentException("Cannot get discharge time for delivery report message");
        }
        setOriginalReceivedDate(dischargeTime);
        int i = pdu.getStatus();
        setPayload(new Payload(""));
        if ((i & 0x60) == 0) {
            this.deliveryStatus = DeliveryStatus.Delivered;
        } else if ((i & 0x20) == 0x20) {
            this.deliveryStatus = DeliveryStatus.Pending;
        } else if ((i & 0x40) == 0x40) {
            this.deliveryStatus = DeliveryStatus.Expired;
        } else if ((i & 0x60) == 0x60) {
            this.deliveryStatus = DeliveryStatus.Expired;
        } else {
            this.deliveryStatus = DeliveryStatus.Error;
        }
    }

    public DeliveryReportMessage(String messageId, String recipientAddress, String memLocation, int memIndex,
            Date originalSentDate, Date receivedDate) {
        super(Type.StatusReport, memLocation, memIndex);
        setOriginalOperatorMessageId(messageId);
        this.recipientAddress = new MsIsdn(recipientAddress);
        setSentDate(originalSentDate);
        setOriginalReceivedDate(receivedDate);
        this.deliveryStatus = DeliveryStatus.Unknown;
    }

    public DeliveryStatus getDeliveryStatus() {
        return this.deliveryStatus;
    }

    public @Nullable String getOriginalOperatorMessageId() {
        return this.originalOperatorMessageId;
    }

    public void setOriginalOperatorMessageId(String originalOperatorMessageId) {
        this.originalOperatorMessageId = originalOperatorMessageId;
    }

    public @Nullable Date getOriginalReceivedDate() {
        Date finalOriginalReceivedDate = originalReceivedDate;
        return finalOriginalReceivedDate == null ? null : new Date(finalOriginalReceivedDate.getTime());
    }

    public void setOriginalReceivedDate(Date originalReceivedDate) {
        this.originalReceivedDate = new Date(originalReceivedDate.getTime());
    }

    @Override
    public String getSignature() {
        return hashSignature(String.format("%s-%s-%s-%s", getOriginatorAddress(), getOriginalOperatorMessageId(),
                getOriginalReceivedDate(), getDeliveryStatus()));
    }

    @Override
    public String toShortString() {
        return String.format("[%s @ %s = %s @ %s]", getId(), getRecipientAddress(), getDeliveryStatus(),
                getOriginalReceivedDate());
    }
}
