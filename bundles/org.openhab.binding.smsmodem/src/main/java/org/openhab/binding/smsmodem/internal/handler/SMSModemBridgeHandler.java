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
package org.openhab.binding.smsmodem.internal.handler;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smsmodem.internal.SMSConversationDiscoveryService;
import org.openhab.binding.smsmodem.internal.SMSModemBindingConstants;
import org.openhab.binding.smsmodem.internal.SMSModemBridgeConfiguration;
import org.openhab.binding.smsmodem.internal.actions.SMSModemActions;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.CommunicationException;
import org.smslib.Modem;
import org.smslib.Modem.Status;
import org.smslib.callback.IDeviceInformationListener;
import org.smslib.callback.IInboundOutboundMessageListener;
import org.smslib.callback.IModemStatusListener;
import org.smslib.message.DeliveryReportMessage;
import org.smslib.message.InboundMessage;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundMessage;
import org.smslib.message.Payload;
import org.smslib.message.Payload.Type;

/**
 * The {@link SMSModemBridgeHandler} is responsible for handling
 * communication with the modem.
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@NonNullByDefault
public class SMSModemBridgeHandler extends BaseBridgeHandler
        implements IModemStatusListener, IInboundOutboundMessageListener, IDeviceInformationListener {

    public static final ThingTypeUID SUPPORTED_THING_TYPES_UIDS = SMSModemBindingConstants.SMSMODEMBRIDGE_THING_TYPE;

    private final Logger logger = LoggerFactory.getLogger(SMSModemBridgeHandler.class);

    private Set<SMSConversationHandler> childHandlers = new HashSet<>();

    private SerialPortManager serialPortManager;

    /**
     * The smslib object responsible for the serial communication with the modem
     */
    private @NonNullByDefault({}) Modem modem;

    /**
     * A scheduled watchdog check
     */
    private @NonNullByDefault({}) ScheduledFuture<?> checkScheduled;

    // we keep a list of msisdn sender for autodiscovery
    private Set<String> senderMsisdn = new HashSet<String>();
    private @Nullable SMSConversationDiscoveryService discoveryService;

    private boolean shouldRun = false;

    @Override
    public void dispose() {
        shouldRun = false;
        checkScheduled.cancel(true);
        scheduler.execute(modem::stop);
        modem.registerStatusListener(null);
        modem.registerMessageListener(null);
        modem.registerInformationListener(null);
        modem = null;
    }

    public SMSModemBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        scheduler.execute(() -> {
            modem.stop();
            checkAndStartModemIfNeeded();
        });
    }

    @Override
    public void initialize() {
        shouldRun = true;
        if (checkScheduled == null || (checkScheduled.isDone()) && this.shouldRun) {
            checkScheduled = scheduler.scheduleWithFixedDelay(this::checkAndStartModemIfNeeded, 0, 15,
                    TimeUnit.SECONDS);
        }
    }

    private void checkAndStartModemIfNeeded() {
        try {
            if (shouldRun && !isRunning()) {
                logger.debug("Initializing smsmodem");
                // ensure the underlying modem is stopped before trying to (re)starting it :
                SMSModemBridgeConfiguration config = getConfigAs(SMSModemBridgeConfiguration.class);
                if (modem != null) {
                    modem.stop();
                } else {
                    modem = new Modem(serialPortManager, resolveEventualSymbolicLink(config.serialPortOrIP),
                            Integer.valueOf(config.baudOrNetworkPort), config.simPin, scheduler, config.pollingInterval,
                            config.delayBetweenSend);
                }
                checkParam(config);
                logger.debug("Now trying to start SMSModem {}/{}", config.serialPortOrIP, config.baudOrNetworkPort);
                modem.registerStatusListener(this);
                modem.registerMessageListener(this);
                modem.registerInformationListener(this);
                modem.start();
                logger.debug("SMSModem {}/{} started", config.serialPortOrIP, config.baudOrNetworkPort);
            }
        } catch (ModemConfigurationException e) {
            String message = e.getMessage();
            logger.error(message, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        }
    }

    private void checkParam(SMSModemBridgeConfiguration config) throws ModemConfigurationException {
        String realSerialPortOrIP = resolveEventualSymbolicLink(config.serialPortOrIP);
        SerialPortIdentifier identifier = serialPortManager.getIdentifier(realSerialPortOrIP);
        if (identifier == null) {
            try {
                InetAddress inetAddress = InetAddress.getByName(realSerialPortOrIP);
                realSerialPortOrIP = inetAddress.getHostAddress();

                // test reachable address :
                try (Socket s = new Socket(realSerialPortOrIP, config.baudOrNetworkPort)) {
                }

            } catch (IOException | NumberFormatException ex) {
                // no serial port and no ip
                throw new ModemConfigurationException(realSerialPortOrIP + " with " + config.baudOrNetworkPort
                        + " is not a serial port/baud or a reachable address/port", ex);
            }
        }
    }

    private String resolveEventualSymbolicLink(String serialPortOrIp) {
        String keepResult = serialPortOrIp;
        Path maybePath = Paths.get(serialPortOrIp);
        File maybeFile = maybePath.toFile();
        if (maybeFile.exists() && Files.isSymbolicLink(maybePath)) {
            try {
                maybePath = maybePath.toRealPath();
                keepResult = maybePath.toAbsolutePath().toString();
            } catch (IOException e) {
            } // nothing to do, not a valid symbolic link, return
        }
        return keepResult;
    }

    public boolean isRunning() {
        return modem != null && (modem.getStatus() == Status.Started || modem.getStatus() == Status.Starting);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof SMSConversationHandler) {
            childHandlers.add((SMSConversationHandler) childHandler);
        } else {
            logger.error("The SMSModemBridgeHandler can only handle SMSConversation as child");
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        childHandlers.remove(childHandler);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void messageReceived(InboundMessage message) {
        String sender = message.getOriginatorAddress().getAddress();
        Payload payload = message.getPayload();
        String messageText;
        if (payload.getType().equals(Type.Text)) {
            String text = payload.getText();
            if (text != null) {
                messageText = text;
            } else {
                logger.error("Message has no payload !");
                return;
            }
        } else {
            byte[] bytes = payload.getBytes();
            if (bytes != null) {
                logger.warn("Message payload in binary format. Don't know how to handle it. Please report it.");
                messageText = bytes.toString();
            } else {
                logger.error("Message has no payload !");
                return;
            }
        }
        logger.debug("Receiving new message from {} : {}", sender, messageText);

        // dispatch to conversation :
        for (SMSConversationHandler child : childHandlers) {
            child.checkAndReceive(sender, messageText);
        }

        // channel trigger
        String recipientAndMessage = sender + "|" + messageText;
        triggerChannel(SMSModemBindingConstants.CHANNEL_TRIGGER_MODEM_RECEIVE, recipientAndMessage);

        // prepare discovery service
        senderMsisdn.add(sender);
        final SMSConversationDiscoveryService finalDiscoveryService = discoveryService;
        if (finalDiscoveryService != null) {
            finalDiscoveryService.buildByAutoDiscovery(sender);
        }
        try { // delete message on the sim
            modem.delete(message);
        } catch (CommunicationException e) {
            logger.error("Cannot delete message after receiving it !", e);
        }
    }

    /**
     * Send message
     *
     * @param recipient The recipient for the message
     * @param text The message content
     * @param deliveryReport If we should ask the network for a delivery report
     */
    public void send(String recipient, String text, boolean deliveryReport) {
        OutboundMessage out = new OutboundMessage(recipient, text);
        out.setRequestDeliveryReport(deliveryReport);
        logger.debug("Sending message to {}", recipient);
        modem.queue(out);
    }

    /**
     * Used by the scanning discovery service to create conversation
     *
     * @return All senders of the received messages since the last start
     */
    public Set<String> getAllSender() {
        return new HashSet<>(senderMsisdn);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SMSModemActions.class, SMSConversationDiscoveryService.class);
    }

    @Override
    public boolean processStatusCallback(Modem.Status oldStatus, Modem.Status newStatus) {
        switch (newStatus) {
            case Error:
                logger.error("SMSLib reported an error on the underlying modem {}", modem.getDescription());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                break;
            case Started:
                logger.debug("SMSLib reported the modem {} is started", modem.getDescription());
                updateStatus(ThingStatus.ONLINE);
                break;
            case Starting:
                logger.debug("SMSLib reported the modem {} is starting", modem.getDescription());
                updateStatus(ThingStatus.UNKNOWN);
                break;
            case Stopped:
                logger.debug("SMSLib reported the modem {} is stopped", modem.getDescription());
                if (thing.getStatus() != ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.OFFLINE);
                }
                break;
            case Stopping:
                logger.debug("SMSLib reported the modem {} is stopping", modem.getDescription());
                if (thing.getStatus() != ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.OFFLINE);
                }
                break;
        }
        return false;
    }

    public void setDiscoveryService(SMSConversationDiscoveryService smsConversationDiscoveryService) {
        this.discoveryService = smsConversationDiscoveryService;
    }

    @Override
    public void messageSent(OutboundMessage message) {
        DeliveryStatus sentStatus;
        switch (message.getSentStatus()) {
            case Failed:
                sentStatus = DeliveryStatus.FAILED;
                break;
            case Unsent:
            case Queued:
                sentStatus = DeliveryStatus.QUEUED;
                break;
            case Sent:
                sentStatus = DeliveryStatus.SENT;
                break;
            default: // shoult not happened
                sentStatus = DeliveryStatus.UNKNOWN;
                break;
        }
        // dispatch to conversation :
        MsIsdn recipientAddress = message.getRecipientAddress();
        if (recipientAddress != null) {
            String recipient = recipientAddress.getAddress();
            for (SMSConversationHandler child : childHandlers) {
                child.checkAndUpdateDeliveryStatus(recipient, sentStatus);
            }
        }
    }

    @Override
    public void messageDelivered(DeliveryReportMessage message) {
        DeliveryStatus sentStatus;
        switch (message.getDeliveryStatus()) {
            case Delivered:
                sentStatus = DeliveryStatus.DELIVERED;
                break;
            case Error:
            case Failed:
                sentStatus = DeliveryStatus.FAILED;
                break;
            case Expired:
                sentStatus = DeliveryStatus.EXPIRED;
                break;
            case Pending:
                sentStatus = DeliveryStatus.PENDING;
                break;
            case Unknown:
            default:
                sentStatus = DeliveryStatus.UNKNOWN;
                break;
        }
        MsIsdn recipientAddress = message.getRecipientAddress();
        if (recipientAddress != null) {
            String recipient = recipientAddress.getAddress();
            for (SMSConversationHandler child : childHandlers) {
                child.checkAndUpdateDeliveryStatus(recipient, sentStatus);
            }
        }
        try {
            modem.delete(message);
        } catch (CommunicationException e) {
            logger.error("Cannot delete delivery report after receiving it !", e);
        }
    }

    @Override
    public void setManufacturer(String manufacturer) {
        thing.setProperty(SMSModemBindingConstants.PROPERTY_MANUFACTURER, manufacturer);
    }

    @Override
    public void setModel(String model) {
        thing.setProperty(SMSModemBindingConstants.PROPERTY_MODEL, model);
    }

    @Override
    public void setSwVersion(String swVersion) {
        thing.setProperty(SMSModemBindingConstants.PROPERTY_SWVERSION, swVersion);
    }

    @Override
    public void setSerialNo(String serialNo) {
        thing.setProperty(SMSModemBindingConstants.PROPERTY_SERIALNO, serialNo);
    }

    @Override
    public void setImsi(String imsi) {
        thing.setProperty(SMSModemBindingConstants.PROPERTY_IMSI, imsi);
    }

    @Override
    public void setRssi(String rssi) {
        thing.setProperty(SMSModemBindingConstants.PROPERTY_RSSI, rssi);
    }

    @Override
    public void setMode(String mode) {
        thing.setProperty(SMSModemBindingConstants.PROPERTY_MODE, mode);
    }

    @Override
    public void setTotalSent(String totalSent) {
        thing.setProperty(SMSModemBindingConstants.PROPERTY_TOTALSENT, totalSent);
    }

    @Override
    public void setTotalFailed(String totalFailed) {
        thing.setProperty(SMSModemBindingConstants.PROPERTY_TOTALFAILED, totalFailed);
    }

    @Override
    public void setTotalReceived(String totalReceived) {
        thing.setProperty(SMSModemBindingConstants.PROPERTY_TOTALRECEIVED, totalReceived);
    }

    @Override
    public void setTotalFailures(String totalFailure) {
        thing.setProperty(SMSModemBindingConstants.PROPERTY_TOTALFAILURE, totalFailure);
    }
}
