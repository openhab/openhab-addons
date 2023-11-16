package org.smslib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.DeviceInformation.Modes;
import org.smslib.Modem.Status;
import org.smslib.message.DeliveryReportMessage;
import org.smslib.message.InboundBinaryMessage;
import org.smslib.message.InboundMessage;
import org.smslib.message.Payload;
import org.smslib.pduUtils.gsm3040.Pdu;
import org.smslib.pduUtils.gsm3040.PduParser;
import org.smslib.pduUtils.gsm3040.PduUtils;
import org.smslib.pduUtils.gsm3040.SmsDeliveryPdu;
import org.smslib.pduUtils.gsm3040.SmsStatusReportPdu;

/**
 *
 * Poll the modem to check for new received messages
 * (sms or delivery report)
 *
 * Extracted from SMSLib
 */
@NonNullByDefault
public class MessageReader extends Thread {
    static Logger logger = LoggerFactory.getLogger(MessageReader.class);

    Modem modem;

    private static int HOURS_TO_RETAIN_ORPHANED_MESSAGE_PARTS = 72;

    public MessageReader(Modem modem) {
        this.modem = modem;
    }

    @Override
    public void run() {
        logger.debug("Started!");
        if (this.modem.getStatus() == Status.Started) {
            try {
                this.modem.getModemDriver().lock();
                ArrayList<InboundMessage> messageList = new ArrayList<InboundMessage>();
                try {
                    for (int i = 0; i < (this.modem.getModemDriver().getMemoryLocations().length() / 2); i++) {
                        String memLocation = this.modem.getModemDriver().getMemoryLocations().substring((i * 2),
                                (i * 2) + 2);
                        String data = this.modem.getModemDriver().atGetMessages(memLocation).getResponseData();
                        if (data.length() > 0) {
                            messageList.addAll((this.modem.getDeviceInformation().getMode() == Modes.PDU
                                    ? parsePDU(data, memLocation)
                                    : parseTEXT(data, memLocation)));
                        }
                    }
                } finally {
                    this.modem.getModemDriver().unlock();
                }
                for (InboundMessage message : messageList) {
                    processMessage(message);
                }

            } catch (CommunicationException | IOException e) {
                logger.error("Unhandled exception while trying to read new messages", e);
                modem.error();
            }
        }
        logger.debug("Stopped!");
    }

    private ArrayList<InboundMessage> parsePDU(String data, String memLocation) throws IOException {
        ArrayList<InboundMessage> messageList = new ArrayList<>();
        List<List<InboundMessage>> mpMsgList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(data));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            PduParser parser = new PduParser();
            int i = line.indexOf(':');
            int j = line.indexOf(',');
            if (j == -1) {
                logger.error("Bad PDU announce : {}", line);
                continue;
            }
            int memIndex = Integer.parseInt(line.substring(i + 1, j).trim());
            i = line.lastIndexOf(',');
            j = line.length();
            int pduSize = Integer.parseInt(line.substring(i + 1, j).trim());
            String pduString = reader.readLine().trim();
            if ((pduSize > 0) && ((pduSize * 2) == pduString.length())) {
                pduString = "00" + pduString;
            }
            Pdu pdu = parser.parsePdu(pduString);
            if (pdu instanceof SmsDeliveryPdu deliveryPdu) {
                logger.debug("PDU = {}", pdu.toString());
                InboundMessage msg = null;
                if (pdu.isBinary()) {
                    msg = new InboundBinaryMessage(deliveryPdu, memLocation, memIndex);
                } else {
                    msg = new InboundMessage(deliveryPdu, memLocation, memIndex);
                }
                msg.setGatewayId(this.modem.getGatewayId());
                msg.setGatewayId(this.modem.getGatewayId());
                logger.debug("IN-DTLS: MI:{} REF:{} MAX:{} SEQ:{}", msg.getMemIndex(), msg.getMpRefNo(),
                        msg.getMpMaxNo(), msg.getMpSeqNo());
                if (msg.getMpRefNo() == 0) {
                    messageList.add(msg);
                } else {
                    // multi-part message
                    int k, l;
                    List<InboundMessage> tmpList;
                    InboundMessage listMsg;
                    boolean found, duplicate;
                    found = false;
                    for (k = 0; k < mpMsgList.size(); k++) {
                        // List of List<InboundMessage>
                        tmpList = mpMsgList.get(k);
                        listMsg = tmpList.get(0);
                        // check if current message list is for this message
                        if (listMsg.getMpRefNo() == msg.getMpRefNo()) {
                            duplicate = false;
                            // check if the message is already in the message list
                            for (l = 0; l < tmpList.size(); l++) {
                                listMsg = tmpList.get(l);
                                if (listMsg.getMpSeqNo() == msg.getMpSeqNo()) {
                                    duplicate = true;
                                    break;
                                }
                            }
                            if (!duplicate) {
                                tmpList.add(msg);
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // no existing list present for this message
                        // add one
                        tmpList = new ArrayList<>();
                        tmpList.add(msg);
                        mpMsgList.add(tmpList);
                    }
                }
            } else if (pdu instanceof SmsStatusReportPdu statusReportPdu) {
                DeliveryReportMessage msg;
                msg = new DeliveryReportMessage(statusReportPdu, memLocation, memIndex);
                msg.setGatewayId(this.modem.getGatewayId());
                messageList.add(msg);
            }
        }
        checkMpMsgList(messageList, mpMsgList);
        List<InboundMessage> tmpList;
        for (int k = 0; k < mpMsgList.size(); k++) {
            tmpList = mpMsgList.get(k);
            tmpList.clear();
        }
        mpMsgList.clear();
        return messageList;
    }

    private ArrayList<InboundMessage> parseTEXT(String data, String memLocation) throws IOException {
        ArrayList<InboundMessage> messageList = new ArrayList<>();
        BufferedReader reader;
        String line;
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        String myData = data;
        myData = myData.replaceAll("\\s+OK\\s+", "\nOK");
        myData = myData.replaceAll("$", "\n");
        logger.debug(myData);
        reader = new BufferedReader(new StringReader(myData));
        for (;;) {
            line = reader.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            if (line.length() > 0) {
                break;
            }
        }
        while (true) {
            if (line == null) {
                break;
            }
            if (line.length() <= 0 || "OK".equalsIgnoreCase(line)) {
                break;
            }
            int i = line.indexOf(':');
            int j = line.indexOf(',');
            int memIndex = Integer.parseInt(line.substring(i + 1, j).trim());
            StringTokenizer tokens = new StringTokenizer(line, ",");
            tokens.nextToken();
            tokens.nextToken();
            String tmpLine = "";
            if (Character.isDigit(tokens.nextToken().trim().charAt(0))) {
                line = line.replaceAll(",,", ", ,");
                tokens = new StringTokenizer(line, ",");
                tokens.nextToken();
                tokens.nextToken();
                tokens.nextToken();
                String messageId = tokens.nextToken();
                String recipient = tokens.nextToken().replaceAll("\"", "");
                String dateStr = tokens.nextToken().replaceAll("\"", "");
                if (dateStr.indexOf('/') == -1) {
                    dateStr = tokens.nextToken().replaceAll("\"", "");
                }
                cal1.set(Calendar.YEAR, 2000 + Integer.parseInt(dateStr.substring(0, 2)));
                cal1.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(3, 5)) - 1);
                cal1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr.substring(6, 8)));
                dateStr = tokens.nextToken().replaceAll("\"", "");
                cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateStr.substring(0, 2)));
                cal1.set(Calendar.MINUTE, Integer.parseInt(dateStr.substring(3, 5)));
                cal1.set(Calendar.SECOND, Integer.parseInt(dateStr.substring(6, 8)));
                dateStr = tokens.nextToken().replaceAll("\"", "");
                cal2.set(Calendar.YEAR, 2000 + Integer.parseInt(dateStr.substring(0, 2)));
                cal2.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(3, 5)) - 1);
                cal2.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr.substring(6, 8)));
                dateStr = tokens.nextToken().replaceAll("\"", "");
                cal2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateStr.substring(0, 2)));
                cal2.set(Calendar.MINUTE, Integer.parseInt(dateStr.substring(3, 5)));
                cal2.set(Calendar.SECOND, Integer.parseInt(dateStr.substring(6, 8)));
                DeliveryReportMessage msg;
                msg = new DeliveryReportMessage(messageId, recipient, memLocation, memIndex, cal1.getTime(),
                        cal2.getTime());
                msg.setGatewayId(this.modem.getGatewayId());
                messageList.add(msg);
            } else {
                line = line.replaceAll(",,", ", ,");
                tokens = new StringTokenizer(line, ",");
                tokens.nextToken();
                tokens.nextToken();
                String originator = tokens.nextToken().replaceAll("\"", "");
                tokens.nextToken();
                String dateStr = tokens.nextToken().replaceAll("\"", "");
                cal1.set(Calendar.YEAR, 2000 + Integer.parseInt(dateStr.substring(0, 2)));
                cal1.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(3, 5)) - 1);
                cal1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr.substring(6, 8)));
                dateStr = tokens.nextToken().replaceAll("\"", "");
                cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateStr.substring(0, 2)));
                cal1.set(Calendar.MINUTE, Integer.parseInt(dateStr.substring(3, 5)));
                cal1.set(Calendar.SECOND, Integer.parseInt(dateStr.substring(6, 8)));
                String msgText = "";
                while (true) {
                    tmpLine = reader.readLine();
                    if (tmpLine == null) {
                        break;
                    }
                    if (tmpLine.startsWith("+CMGL")) {
                        break;
                    }
                    if (tmpLine.startsWith("+CMGR")) {
                        break;
                    }
                    msgText += (msgText.length() == 0 ? "" : "\n") + tmpLine;
                }
                InboundMessage msg = new InboundMessage(originator, msgText.trim(), cal1.getTime(), memLocation,
                        memIndex);
                msg.setGatewayId(this.modem.getGatewayId());
                messageList.add(msg);
            }
            while (true) {
                // line = reader.readLine();
                line = ((tmpLine == null || tmpLine.length() == 0) ? reader.readLine() : tmpLine);
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.length() > 0) {
                    break;
                }
            }
        }
        reader.close();
        return messageList;
    }

    private void checkMpMsgList(Collection<InboundMessage> msgList, List<List<InboundMessage>> mpMsgList) {
        int k, l, m;
        List<InboundMessage> tmpList;
        InboundMessage listMsg, mpMsg;
        boolean found;
        mpMsg = null;
        logger.debug("CheckMpMsgList(): MAINLIST: {}", mpMsgList.size());
        for (k = 0; k < mpMsgList.size(); k++) {
            tmpList = mpMsgList.get(k);
            logger.debug("CheckMpMsgList(): SUBLIST[{}]: ", tmpList.size());
            listMsg = tmpList.get(0);
            found = false;
            if (listMsg.getMpMaxNo() == tmpList.size()) {
                found = true;
                for (l = 0; l < tmpList.size(); l++) {
                    for (m = 0; m < tmpList.size(); m++) {
                        listMsg = tmpList.get(m);
                        if (listMsg.getMpSeqNo() == (l + 1)) {
                            if (listMsg.getMpSeqNo() == 1) {
                                mpMsg = listMsg;
                                mpMsg.setMpMemIndex(mpMsg.getMemIndex());
                                if (listMsg.getMpMaxNo() == 1) {
                                    msgList.add(mpMsg);
                                }
                            } else {
                                if (mpMsg != null) {
                                    String textToAdd = listMsg.getPayload().getText();
                                    if (mpMsg.getEndsWithMultiChar()) {
                                        if (textToAdd == null) {
                                            throw new UnrecoverableSmslibException("Cannot add text to message");
                                        }
                                        // adjust first char of textToAdd
                                        logger.debug("Adjusting dangling multi-char: {}  --> {}", textToAdd.charAt(0),
                                                PduUtils.getMultiCharFor(textToAdd.charAt(0)));
                                        textToAdd = PduUtils.getMultiCharFor(textToAdd.charAt(0))
                                                + textToAdd.substring(1);
                                    }
                                    mpMsg.setEndsWithMultiChar(listMsg.getEndsWithMultiChar());
                                    mpMsg.setPayload(new Payload(mpMsg.getPayload().getText() + textToAdd));
                                    // }
                                    mpMsg.setMpSeqNo(listMsg.getMpSeqNo());
                                    mpMsg.setMpMemIndex(listMsg.getMemIndex());
                                    if (listMsg.getMpSeqNo() == listMsg.getMpMaxNo()) {
                                        mpMsg.setMemIndex(-1);
                                        msgList.add(mpMsg);
                                        mpMsg = null;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                tmpList.clear();
                tmpList = null;
            }
            if (found) {
                mpMsgList.remove(k);
                k--;
            }
        }
        // Check the remaining parts for "orphaned" status
        for (List<InboundMessage> remainingList : mpMsgList) {
            for (InboundMessage msg : remainingList) {
                Date sentDate = msg.getSentDate();
                if (sentDate == null || getAgeInHours(sentDate) > HOURS_TO_RETAIN_ORPHANED_MESSAGE_PARTS) {
                    try {
                        this.modem.delete(msg);
                    } catch (CommunicationException e) {
                        logger.error("Could not delete orphaned message: {}", msg.toString(), e);
                    }
                }
            }
        }
    }

    private static int getAgeInHours(Date fromDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date());
        long now = cal.getTimeInMillis();
        cal.setTime(fromDate);
        long past = cal.getTimeInMillis();
        return (int) ((now - past) / (60 * 60 * 1000));
    }

    private void processMessage(InboundMessage message) {
        String messageSignature = message.getSignature();
        if (!this.modem.getReadMessagesSet().contains(messageSignature)) {
            this.modem.getDeviceInformation().increaseTotalReceived();
            if (message instanceof DeliveryReportMessage deliveryReportMessage) {
                modem.processDeliveryReport(deliveryReportMessage);
            } else {
                modem.processMessage(message);
            }
            this.modem.getReadMessagesSet().add(messageSignature);
        }
    }
}
