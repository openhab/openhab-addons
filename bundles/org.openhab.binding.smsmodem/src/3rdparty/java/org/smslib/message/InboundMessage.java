package org.smslib.message;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.pduUtils.gsm3040.PduUtils;
import org.smslib.pduUtils.gsm3040.SmsDeliveryPdu;

/**
 * Extracted from SMSLib
 */
@NonNullByDefault
public class InboundMessage extends AbstractMessage {
    static Logger logger = LoggerFactory.getLogger(InboundMessage.class);

    private static final long serialVersionUID = 1L;

    int memIndex;

    String memLocation;

    int mpRefNo;

    int mpMaxNo;

    int mpSeqNo;

    String mpMemIndex = "";

    @Nullable
    MsIsdn smscNumber;

    boolean endsWithMultiChar;

    public InboundMessage(SmsDeliveryPdu pdu, String memLocation, int memIndex) {
        super(Type.Inbound, new MsIsdn(pdu.getAddress()), null, null);
        this.memLocation = memLocation;
        this.memIndex = memIndex;
        this.mpRefNo = 0;
        this.mpMaxNo = 0;
        this.mpSeqNo = 0;
        setMpMemIndex(-1);
        int dcsEncoding = PduUtils.extractDcsEncoding(pdu.getDataCodingScheme());
        switch (dcsEncoding) {
            case PduUtils.DCS_ENCODING_7BIT:
                setEncoding(Encoding.Enc7);
                break;
            case PduUtils.DCS_ENCODING_8BIT:
                setEncoding(Encoding.Enc8);
                break;
            case PduUtils.DCS_ENCODING_UCS2:
                setEncoding(Encoding.EncUcs2);
                break;
            default:
                logger.error("Unknown DCS Encoding: {}", dcsEncoding);
        }
        Date timestamp = pdu.getTimestamp();
        if (timestamp != null) {
            setSentDate(timestamp);
        }
        this.smscNumber = new MsIsdn(pdu.getSmscAddress());
        setPayload(new Payload(pdu.getDecodedText()));
        if (pdu.isConcatMessage()) {
            this.mpRefNo = pdu.getMpRefNo();
            this.mpMaxNo = pdu.getMpMaxNo();
            this.mpSeqNo = pdu.getMpSeqNo();
        }
        if (pdu.isPortedMessage()) {
            setSourcePort(pdu.getSrcPort());
            setDestinationPort(pdu.getDestPort());
        }
        if (getEncoding() == Encoding.Enc7) {
            byte[] udData = pdu.getUDData();
            if (udData == null) {
                throw new IllegalArgumentException("Cannot encode udData to construct message");
            }
            byte[] temp = PduUtils.encodedSeptetsToUnencodedSeptets(udData);
            if (temp.length == 0) {
                this.endsWithMultiChar = false;
            } else if (temp[temp.length - 1] == 0x1b) {
                this.endsWithMultiChar = true;
            }
        }
    }

    public InboundMessage(String originator, String text, Date sentDate, String memLocation, int memIndex) {
        super(Type.Inbound, new MsIsdn(originator), null, new Payload(text));
        this.memLocation = memLocation;
        this.memIndex = memIndex;
        this.sentDate = new Date(sentDate.getTime());
    }

    public InboundMessage(Type type, String memLocation, int memIndex) {
        super(type, new MsIsdn(), null, null);
        this.memIndex = memIndex;
        this.memLocation = memLocation;
        this.mpRefNo = 0;
        this.mpMaxNo = 0;
        this.mpSeqNo = 0;
        setMpMemIndex(-1);
        this.smscNumber = new MsIsdn();
    }

    public int getMemIndex() {
        return this.memIndex;
    }

    public void setMemIndex(int memIndex) {
        this.memIndex = memIndex;
    }

    public String getMemLocation() {
        return this.memLocation;
    }

    public int getMpMaxNo() {
        return this.mpMaxNo;
    }

    public String getMpMemIndex() {
        return this.mpMemIndex;
    }

    public void setMpMemIndex(int myMpMemIndex) {
        if (myMpMemIndex == -1) {
            this.mpMemIndex = "";
        } else {
            this.mpMemIndex += (this.mpMemIndex.length() == 0 ? "" : ",") + myMpMemIndex;
        }
    }

    public int getMpRefNo() {
        return this.mpRefNo;
    }

    public int getMpSeqNo() {
        return this.mpSeqNo;
    }

    public void setMpSeqNo(int myMpSeqNo) {
        this.mpSeqNo = myMpSeqNo;
    }

    public boolean getEndsWithMultiChar() {
        return this.endsWithMultiChar;
    }

    public void setEndsWithMultiChar(boolean b) {
        this.endsWithMultiChar = b;
    }

    @Override
    public String getSignature() {
        return hashSignature(String.format("%s-%s-%s", getOriginatorAddress(), getSentDate(), payload.getText()));
    }

    @Override
    public String toShortString() {
        return String.format("[%s @ %s]", getId(), getOriginatorAddress());
    }
}
