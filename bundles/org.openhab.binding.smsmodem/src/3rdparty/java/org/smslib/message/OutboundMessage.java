package org.smslib.message;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.smslib.UnrecoverableSmslibException;
import org.smslib.pduUtils.gsm3040.PduFactory;
import org.smslib.pduUtils.gsm3040.PduGenerator;
import org.smslib.pduUtils.gsm3040.PduUtils;
import org.smslib.pduUtils.gsm3040.SmsSubmitPdu;
import org.smslib.pduUtils.gsm3040.ie.InformationElementFactory;

/**
 * Extracted from SMSLib
 */
@NonNullByDefault
public class OutboundMessage extends AbstractMessage {
    private static final long serialVersionUID = 1L;

    public enum SentStatus {
        Sent("S"),
        Unsent("U"),
        Queued("Q"),
        Failed("F");

        private final String shortString;

        private SentStatus(String shortString) {
            this.shortString = shortString;
        }

        public String toShortString() {
            return this.shortString;
        }
    }

    public enum FailureCause {
        None("00"),
        BadNumber("01"),
        BadFormat("02"),
        GatewayFailure("03"),
        AuthFailure("04"),
        NoCredit("05"),
        OverQuota("06"),
        NoRoute("07"),
        Unavailable("08"),
        HttpError("09"),
        UnknownFailure("10"),
        Cancelled("11"),
        NoService("12"),
        MissingParms("13");

        private final String shortString;

        private FailureCause(String shortString) {
            this.shortString = shortString;
        }

        public String toShortString() {
            return this.shortString;
        }
    }

    SentStatus sentStatus = SentStatus.Unsent;

    FailureCause failureCause = FailureCause.None;

    List<String> operatorMessageIds = new ArrayList<>();

    boolean requestDeliveryReport = false;

    public OutboundMessage() {
    }

    public OutboundMessage(MsIsdn originatorAddress, MsIsdn recipientAddress, Payload payload) {
        super(Type.Outbound, originatorAddress, recipientAddress, payload);
    }

    public OutboundMessage(String recipientAddress, String text) {
        this(new MsIsdn(""), new MsIsdn(recipientAddress), new Payload(text));
    }

    public SentStatus getSentStatus() {
        return this.sentStatus;
    }

    public void setSentStatus(SentStatus sentStatus) {
        this.sentStatus = sentStatus;
    }

    public FailureCause getFailureCause() {
        return this.failureCause;
    }

    public void setFailureCause(FailureCause failureCode) {
        this.failureCause = failureCode;
    }

    public List<String> getOperatorMessageIds() {
        return this.operatorMessageIds;
    }

    public boolean getRequestDeliveryReport() {
        return this.requestDeliveryReport;
    }

    public void setRequestDeliveryReport(boolean requestDeliveryReport) {
        this.requestDeliveryReport = requestDeliveryReport;
    }

    @Override
    public String toShortString() {
        return String.format("[%s @ %s]", getId(), getRecipientAddress());
    }

    public List<String> getPdus(MsIsdn smscNumber, int mpRefNo) {
        PduGenerator pduGenerator = new PduGenerator();
        SmsSubmitPdu pdu = createPduObject(getRequestDeliveryReport());
        initPduObject(pdu, smscNumber);
        return pduGenerator.generatePduList(pdu, mpRefNo);
    }

    protected SmsSubmitPdu createPduObject(boolean extRequestDeliveryReport) {
        return (extRequestDeliveryReport ? PduFactory.newSmsSubmitPdu(PduUtils.TP_SRR_REPORT | PduUtils.TP_VPF_INTEGER)
                : PduFactory.newSmsSubmitPdu());
    }

    protected void initPduObject(SmsSubmitPdu pdu, MsIsdn smscNumber) {
        if ((getSourcePort() > -1) && (getDestinationPort() > -1)) {
            pdu.addInformationElement(
                    InformationElementFactory.generatePortInfo(getDestinationPort(), getSourcePort()));
        }
        String smscNumberForLengthCheck = smscNumber.getAddress();
        pdu.setSmscInfoLength(
                1 + (smscNumberForLengthCheck.length() / 2) + ((smscNumberForLengthCheck.length() % 2 == 1) ? 1 : 0));
        pdu.setSmscAddress(smscNumber.getAddress());
        pdu.setSmscAddressType(PduUtils.getAddressTypeFor(smscNumber));
        pdu.setMessageReference(0);
        MsIsdn finalRecipientAddress = recipientAddress;
        if (finalRecipientAddress == null) {
            throw new UnrecoverableSmslibException("Recipient adress cannot be null");
        }
        pdu.setAddress(finalRecipientAddress);
        MsIsdn recipientAddressFinal = this.recipientAddress;
        if (recipientAddressFinal == null) {
            throw new UnrecoverableSmslibException("Cannot set address type with no recipient");
        }
        pdu.setAddressType(PduUtils.getAddressTypeFor(recipientAddressFinal));
        pdu.setProtocolIdentifier(0);
        if (!pdu.isBinary()) {
            int dcs = 0;
            if (getEncoding() == Encoding.Enc7) {
                dcs = PduUtils.DCS_ENCODING_7BIT;
            } else if (getEncoding() == Encoding.Enc8) {
                dcs = PduUtils.DCS_ENCODING_8BIT;
            } else if (getEncoding() == Encoding.EncUcs2) {
                dcs = PduUtils.DCS_ENCODING_UCS2;
            } else if (getEncoding() == Encoding.EncCustom) {
                dcs = PduUtils.DCS_ENCODING_7BIT;
            }
            if (getDcsClass() == DcsClass.Flash) {
                dcs = dcs | PduUtils.DCS_MESSAGE_CLASS_FLASH;
            } else if (getDcsClass() == DcsClass.Me) {
                dcs = dcs | PduUtils.DCS_MESSAGE_CLASS_ME;
            } else if (getDcsClass() == DcsClass.Sim) {
                dcs = dcs | PduUtils.DCS_MESSAGE_CLASS_SIM;
            } else if (getDcsClass() == DcsClass.Te) {
                dcs = dcs | PduUtils.DCS_MESSAGE_CLASS_TE;
            }
            pdu.setDataCodingScheme(dcs);
        }
        pdu.setValidityPeriod(0);
        if (getEncoding() == Encoding.Enc8) {
            byte[] bytes = getPayload().getBytes();
            if (bytes == null) {
                throw new UnrecoverableSmslibException("Cannot init pdu object, wrong payload");
            }
            pdu.setDataBytes(bytes);
        } else {
            String text = getPayload().getText();
            if (text == null) {
                throw new UnrecoverableSmslibException("Cannot init pdu object, wrong payload");
            }
            pdu.setDecodedText(text);
        }
    }

    @Override
    public String getSignature() {
        return hashSignature(String.format("%s-%s", getRecipientAddress(), getId()));
    }
}
