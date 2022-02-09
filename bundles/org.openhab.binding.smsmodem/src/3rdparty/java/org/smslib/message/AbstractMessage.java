package org.smslib.message;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.smslib.UnrecoverableSmslibException;
import org.smslib.message.OutboundMessage.SentStatus;

/**
 * Extracted from SMSLib
 */
@NonNullByDefault
public abstract class AbstractMessage implements Serializable {
    public enum Encoding {
        Enc7,
        Enc8,
        EncUcs2,
        EncCustom;
    }

    public enum DcsClass {
        None,
        Flash,
        Me,
        Sim,
        Te
    }

    public enum Type {
        Inbound,
        Outbound,
        StatusReport
    }

    private static final long serialVersionUID = 1L;

    Date creationDate = new Date();

    String id = UUID.randomUUID().toString();

    MsIsdn originatorAddress = new MsIsdn();

    @Nullable
    MsIsdn recipientAddress = new MsIsdn();

    Payload payload = new Payload("");

    Type type = Type.Inbound;

    Encoding encoding = Encoding.Enc7;

    DcsClass dcsClass = DcsClass.Sim;

    String gatewayId = "";

    int sourcePort = -1;

    int destinationPort = -1;

    @Nullable
    Date sentDate;

    public AbstractMessage() {
    }

    public AbstractMessage(Type type, MsIsdn originatorAddress, @Nullable MsIsdn recipientAddress,
            @Nullable Payload payload) {
        this.type = type;
        this.originatorAddress = originatorAddress;
        this.recipientAddress = recipientAddress;
        if (payload != null) {
            setPayload(payload);
        }
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public MsIsdn getOriginatorAddress() {
        return this.originatorAddress;
    }

    public @Nullable MsIsdn getRecipientAddress() {
        return this.recipientAddress;
    }

    public Payload getPayload() {
        return this.payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public Type getType() {
        return this.type;
    }

    public Encoding getEncoding() {
        return this.encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    public DcsClass getDcsClass() {
        return this.dcsClass;
    }

    public void setDcsClass(DcsClass dcsClass) {
        this.dcsClass = dcsClass;
    }

    public String getGatewayId() {
        return this.gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public int getSourcePort() {
        return this.sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public int getDestinationPort() {
        return this.destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public @Nullable Date getSentDate() {
        Date sentDateFinal = this.sentDate;
        return (sentDateFinal != null ? (Date) sentDateFinal.clone() : null);
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = new Date(sentDate.getTime());
    }

    public abstract String getSignature();

    public abstract String toShortString();

    public String hashSignature(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(s.getBytes(), 0, s.length());
            BigInteger i = new BigInteger(1, md.digest());
            return String.format("%1$032x", i);
        } catch (NoSuchAlgorithmException e) {
            throw new UnrecoverableSmslibException("Cannot find hash algorithm", e);
        }
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer(1024);
        b.append(String
                .format("%n== MESSAGE START ======================================================================%n"));
        b.append(String.format("CLASS: %s%n", this.getClass().toString()));
        b.append(String.format("Message ID: %s%n", getId()));
        b.append(String.format("Message Signature: %s%n", getSignature()));
        b.append(String.format("Via Gateway: %s%n", getGatewayId()));
        b.append(String.format("Creation Date: %s%n", getCreationDate()));
        b.append(String.format("Type: %s%n", getType()));
        b.append(String.format("Encoding: %s%n", getEncoding()));
        b.append(String.format("DCS Class: %s%n", getDcsClass()));
        b.append(String.format("Source Port: %s%n", getSourcePort()));
        b.append(String.format("Destination Port: %s%n", getDestinationPort()));
        b.append(String.format("Originator Address: %s%n", getOriginatorAddress()));
        b.append(String.format("Recipient Address: %s%n", getRecipientAddress()));
        b.append(String.format("Payload Type: %s%n", payload.getType()));
        b.append(String.format("Text payload: %s%n", payload.getText() == null ? "null" : payload.getText()));
        if (this instanceof InboundMessage) {
            b.append(String.format("Sent Date: %s%n", getSentDate()));
            b.append(String.format("Memory Storage Location: %s%n", ((InboundMessage) this).getMemLocation()));
            b.append(String.format("Memory Index: %d%n", ((InboundMessage) this).getMemIndex()));
            b.append(String.format("Memory MP Index: %s%n", ((InboundMessage) this).getMpMemIndex()));
        }
        if (this instanceof OutboundMessage) {
            b.append(String.format("Sent Date: %s%n",
                    (((OutboundMessage) this).getSentStatus() == SentStatus.Sent ? getSentDate() : "N/A")));
            String ids = "";
            for (String opId : ((OutboundMessage) this).getOperatorMessageIds()) {
                ids += (ids.length() == 0 ? opId : "," + opId);
            }
            b.append(String.format("Operator Message IDs: %s%n", ids));
            b.append(String.format("Status: %s%n", ((OutboundMessage) this).getSentStatus().toString()));
            b.append(String.format("Failure: %s%n", ((OutboundMessage) this).getFailureCause().toString()));
            b.append(String.format("Request Delivery Reports: %b%n",
                    ((OutboundMessage) this).getRequestDeliveryReport()));
        }
        if (this instanceof DeliveryReportMessage) {
            b.append(String.format("Original Operator Message Id: %s%n",
                    ((DeliveryReportMessage) this).getOriginalOperatorMessageId()));
            b.append(String.format("Delivery Date: %s%n", ((DeliveryReportMessage) this).getOriginalReceivedDate()));
            b.append(String.format("Delivery Status: %s%n", ((DeliveryReportMessage) this).getDeliveryStatus()));
        }
        b.append(String
                .format("== MESSAGE END ========================================================================%n"));
        return b.toString();
    }
}
