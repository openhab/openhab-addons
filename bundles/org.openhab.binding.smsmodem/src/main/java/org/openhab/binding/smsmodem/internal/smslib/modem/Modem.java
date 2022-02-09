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

package org.openhab.binding.smsmodem.internal.smslib.modem;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smsmodem.internal.smslib.callback.IDeviceInformationListener;
import org.openhab.binding.smsmodem.internal.smslib.callback.IInboundOutboundMessageListener;
import org.openhab.binding.smsmodem.internal.smslib.callback.IModemStatusListener;
import org.openhab.binding.smsmodem.internal.smslib.message.DeliveryReportMessage;
import org.openhab.binding.smsmodem.internal.smslib.message.InboundMessage;
import org.openhab.binding.smsmodem.internal.smslib.message.MsIsdn;
import org.openhab.binding.smsmodem.internal.smslib.message.OutboundMessage;
import org.openhab.binding.smsmodem.internal.smslib.message.OutboundMessage.FailureCause;
import org.openhab.binding.smsmodem.internal.smslib.message.OutboundMessage.SentStatus;
import org.openhab.binding.smsmodem.internal.smslib.message.Payload;
import org.openhab.binding.smsmodem.internal.smslib.message.Payload.Type;
import org.openhab.binding.smsmodem.internal.smslib.modem.Capabilities.Caps;
import org.openhab.binding.smsmodem.internal.smslib.modem.DeviceInformation.Modes;
import org.openhab.binding.smsmodem.internal.smslib.modem.driver.AbstractModemDriver;
import org.openhab.binding.smsmodem.internal.smslib.modem.driver.CommunicationException;
import org.openhab.binding.smsmodem.internal.smslib.modem.driver.IPModemDriver;
import org.openhab.binding.smsmodem.internal.smslib.modem.driver.JSerialModemDriver;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The Modem class is an abstraction, central to all operations
 *
 * @author Gwendal ROULLEAU - Initial contribution, extracted from SMSLib
 */
@NonNullByDefault
public class Modem {
    static Logger logger = LoggerFactory.getLogger(Modem.class);

    public enum Status {
        Starting,
        Started,
        Stopping,
        Stopped,
        Error
    }

    AbstractModemDriver modemDriver;

    String simPin;
    MsIsdn smscNumber;
    protected String operatorId = "";
    String gatewayId = "";
    String description = "";

    private ScheduledExecutorService scheduledService;
    @Nullable
    ScheduledFuture<?> messageReader;
    MessageSender messageSender;
    Queue<OutboundMessage> messageQueue = new ConcurrentLinkedQueue<>();
    HashSet<String> readMessagesSet;

    Status status = Status.Stopped;
    Lock startAndStoplock = new ReentrantLock();

    int multipartReferenceNo = 0;

    Capabilities capabilities = new Capabilities();
    DeviceInformation deviceInformation = new DeviceInformation();
    private @Nullable IModemStatusListener modemStatusCallback = null;
    private @Nullable IInboundOutboundMessageListener messageCallback = null;

    private Random randomizer = new Random();

    private AtomicBoolean isStopping = new AtomicBoolean(false);
    private AtomicBoolean isStarting = new AtomicBoolean(false);

    /**
     * Time between sending messages (ms)
     */
    private int gatewayDispatcherYield = 100;

    /**
     * Time between polling for new messages (ms)
     */
    public int modemPollingInterval = 15;

    public Modem(SerialPortManager serialPortManager, String address, int port, String simPin,
            ScheduledExecutorService scheduledService, Integer pollingInterval, Integer delayBetweenSend) {
        this.gatewayId = address + "-" + port;
        this.scheduledService = scheduledService;
        this.modemPollingInterval = pollingInterval;
        this.gatewayDispatcherYield = delayBetweenSend;
        setDescription("GSM Modem " + address + "/" + port);

        Capabilities caps = new Capabilities();
        caps.set(Caps.CanSendMessage);
        caps.set(Caps.CanSendBinaryMessage);
        caps.set(Caps.CanSendUnicodeMessage);
        caps.set(Caps.CanSendWapMessage);
        caps.set(Caps.CanSendFlashMessage);
        caps.set(Caps.CanSendPortInfo);
        caps.set(Caps.CanSplitMessages);
        caps.set(Caps.CanRequestDeliveryStatus);
        setCapabilities(caps);
        if (isPortAnIpAddress(address)) {
            this.modemDriver = new IPModemDriver(this, address, port);
        } else {
            this.modemDriver = new JSerialModemDriver(serialPortManager, this, address, port);
        }
        this.simPin = simPin;
        this.smscNumber = new MsIsdn();
        this.readMessagesSet = new HashSet<>();
        this.messageSender = new MessageSender(String.format("Gateway Dispatcher 1 [%s]", this.gatewayId), messageQueue,
                this, gatewayDispatcherYield);
    }

    final public boolean start() {
        if (!isStarting.getAndSet(true)) {
            this.startAndStoplock.lock();
            try {
                if ((getStatus() == Status.Stopped) || (getStatus() == Status.Error)) {
                    try {
                        setStatus(Status.Starting);
                        logger.debug("Starting gateway: {}", toShortString());
                        this.modemDriver.lock();
                        try {
                            this.modemDriver.openPort();
                            this.modemDriver.initializeModem();
                            if (this.messageReader != null) {
                                messageReader.cancel(true);
                            }
                            this.messageReader = scheduledService.scheduleWithFixedDelay(new MessageReader(this), 15,
                                    modemPollingInterval, TimeUnit.SECONDS);
                            this.modemDriver.refreshRssi();
                            this.messageSender = new MessageSender(
                                    String.format("Gateway Dispatcher 1 [%s]", this.gatewayId), messageQueue, this,
                                    gatewayDispatcherYield);
                            startSendingQueue();
                            if (logger.isDebugEnabled()) {
                                logger.debug("Gateway: {}: {}, SL:{}, SIG: {} / {}", toShortString(),
                                        getDeviceInformation().toString(), this.modemDriver.getMemoryLocations(),
                                        this.modemDriver.getSignature(true), this.modemDriver.getSignature(false));
                            }
                        } finally {
                            this.modemDriver.unlock();
                        }
                        setStatus(Status.Started);
                    } catch (CommunicationException e) {
                        logger.error("Communication exception when trying to start", e);
                        try {
                            stop();
                        } finally {
                            setStatus(Status.Error);
                        }
                    }
                }
            } finally {
                this.startAndStoplock.unlock();
                this.isStarting.set(false);
            }
        }
        return (getStatus() == Status.Started);
    }

    final public boolean stop() {
        if (!isStopping.getAndSet(true)) {
            this.startAndStoplock.lock();
            try {
                if ((getStatus() == Status.Started) || (getStatus() == Status.Error)) {
                    setStatus(Status.Stopping);
                    logger.debug("Stopping gateway: {}", toShortString());
                    if (messageSender.isRunning()) {
                        this.messageSender.setInterrupt();
                    }
                    logger.warn("Gateway stopping, message not delivered : {}", this.messageQueue.size());
                    if (this.messageReader != null) {
                        this.messageReader.cancel(true);
                    }
                    this.modemDriver.lock();
                    try {
                        this.modemDriver.closePort();
                    } finally {
                        this.modemDriver.unlock();
                    }
                    setStatus(Status.Stopped);
                }
            } finally {
                this.startAndStoplock.unlock();
                isStopping.set(false);
            }
        }
        return (getStatus() == Status.Stopped);
    }

    final public void error() {
        this.stop();
        this.status = Status.Error;
    }

    final public boolean send(OutboundMessage message) throws CommunicationException {
        try {
            if (getStatus() != Status.Started) {
                logger.debug("Outbound message routed via non-started gateway: {} ({})", message.toShortString(),
                        getStatus());
                return false;
            }
            this.modemDriver.lock();
            try {
                if (getDeviceInformation().getMode() == Modes.PDU) {
                    List<String> pdus = message.getPdus(getSmscNumber(), getNextMultipartReferenceNo());
                    for (String pdu : pdus) {
                        int j = pdu.length() / 2 - 1;
                        int refNo = this.modemDriver.atSendPDUMessage(j, pdu);
                        if (refNo >= 0) {
                            message.setGatewayId(getGatewayId());
                            message.setSentDate(new Date());
                            message.getOperatorMessageIds().add(String.valueOf(refNo));
                            message.setSentStatus(SentStatus.Sent);
                            message.setFailureCause(FailureCause.None);
                        } else {
                            message.setSentStatus(SentStatus.Failed);
                            message.setFailureCause(FailureCause.GatewayFailure);
                        }
                    }
                } else {
                    MsIsdn recipientAddress = message.getRecipientAddress();
                    Payload payload = message.getPayload();
                    if (recipientAddress == null) {
                        throw new IllegalArgumentException("Recipient is null");
                    }
                    String text = payload.getText();
                    if (payload.getType() == Type.Binary || text == null) {
                        throw new IllegalArgumentException("Cannot send sms in binary format");
                    }
                    int refNo = this.modemDriver.atSendTEXTMessage(recipientAddress.getAddress(), text);
                    if (refNo >= 0) {
                        message.setGatewayId(getGatewayId());
                        message.setSentDate(new Date());
                        message.getOperatorMessageIds().add(String.valueOf(refNo));
                        message.setSentStatus(SentStatus.Sent);
                        message.setFailureCause(FailureCause.None);
                    } else {
                        message.setSentStatus(SentStatus.Failed);
                        message.setFailureCause(FailureCause.GatewayFailure);
                    }
                }
                if (message.getSentStatus() == SentStatus.Sent) {
                    getDeviceInformation().increaseTotalSent();
                } else {
                    getDeviceInformation().increaseTotalFailed();
                }
            } finally {
                this.modemDriver.unlock();
            }
            return message.getSentStatus() == SentStatus.Sent;
        } catch (CommunicationException e) {
            getDeviceInformation().increaseTotalFailures();
            throw e;
        }
    }

    final public boolean delete(InboundMessage message) throws CommunicationException {
        if (getStatus() != Status.Started) {
            if (logger.isDebugEnabled()) {
                logger.debug("Delete message via non-started gateway: {} ({})", message.toShortString(), getStatus());
            }
            return false;
        }

        this.modemDriver.lock();
        try {
            this.readMessagesSet.remove(message.getSignature());
            if (message.getMemIndex() >= 0) {
                return this.modemDriver.atDeleteMessage(message.getMemLocation(), message.getMemIndex()).isResponseOk();
            }
            if ((message.getMemIndex() == -1) && (message.getMpMemIndex().length() > 0)) {
                StringTokenizer tokens = new StringTokenizer(message.getMpMemIndex(), ",");
                while (tokens.hasMoreTokens()) {
                    this.modemDriver.atDeleteMessage(message.getMemLocation(), Integer.valueOf(tokens.nextToken()));
                }
                return true;
            }
            return false;
        } finally {
            this.modemDriver.unlock();
        }
    }

    public boolean queue(OutboundMessage message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Queue: {}", message.toShortString());
        }
        boolean added = messageQueue.add(message);
        if (messageCallback != null) {
            messageCallback.messageSent(message);
        }
        startSendingQueue();
        return added;
    }

    private void startSendingQueue() {
        if (messageQueue.size() > 0 && (!this.messageSender.isRunning())) {
            this.scheduledService.execute(messageSender);
        }
    }

    public DeviceInformation getDeviceInformation() {
        return this.deviceInformation;
    }

    public AbstractModemDriver getModemDriver() {
        return this.modemDriver;
    }

    public String getSimPin() {
        return this.simPin;
    }

    public MsIsdn getSmscNumber() {
        return this.smscNumber;
    }

    public void setSmscNumber(MsIsdn smscNumber) {
        this.smscNumber = smscNumber;
    }

    public HashSet<String> getReadMessagesSet() {
        return this.readMessagesSet;
    }

    private void setStatus(Status status) {
        Status oldStatus = this.status;
        this.status = status;
        Status newStatus = this.status;
        if (modemStatusCallback != null) {
            modemStatusCallback.processStatusCallback(oldStatus, newStatus);
        }
    }

    protected int getNextMultipartReferenceNo() {
        if (this.multipartReferenceNo == 0) {
            this.multipartReferenceNo = this.randomizer.nextInt();
            if (this.multipartReferenceNo < 0) {
                this.multipartReferenceNo *= -1;
            }
            this.multipartReferenceNo %= 65536;
        }
        this.multipartReferenceNo = (this.multipartReferenceNo + 1) % 65536;
        return this.multipartReferenceNo;
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer(1024);
        b.append("== GATEWAY ========================================================================%n");
        b.append(String.format("Gateway ID:  %s%n", getGatewayId()));
        b.append(String.format("-- Capabilities --%n"));
        b.append(capabilities.toString());
        b.append(String.format("-- Settings --%n"));
        b.append("== GATEWAY END ========================================================================%n");
        return b.toString();
    }

    public String toShortString() {
        return getGatewayId() + String.format(" [%s]", this.modemDriver.getPortInfo());
    }

    private boolean isPortAnIpAddress(String address) {
        try {
            InetAddress.getByName(address);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public void registerStatusListener(@Nullable IModemStatusListener smsModemStatusCallback) {
        this.modemStatusCallback = smsModemStatusCallback;
    }

    public void registerMessageListener(@Nullable IInboundOutboundMessageListener messageCallback) {
        this.messageCallback = messageCallback;
    }

    public void registerInformationListener(@Nullable IDeviceInformationListener deviceInformationListener) {
        this.deviceInformation.setDeviceInformationListener(deviceInformationListener);
    }

    public void processMessage(InboundMessage message) {
        if (messageCallback != null) {
            this.messageCallback.messageReceived(message);
        }
    }

    public void processMessageSent(OutboundMessage message) {
        if (messageCallback != null) {
            this.messageCallback.messageSent(message);
        }
    }

    public void processDeliveryReport(DeliveryReportMessage message) {
        if (messageCallback != null) {
            this.messageCallback.messageDelivered(message);
        }
    }

    public Status getStatus() {
        return this.status;
    }

    public final String getGatewayId() {
        return this.gatewayId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
    }
}
