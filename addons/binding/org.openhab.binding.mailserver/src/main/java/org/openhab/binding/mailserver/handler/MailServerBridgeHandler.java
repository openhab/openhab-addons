/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mailserver.handler;

import static org.openhab.binding.mailserver.MailServerBindingConstants.LISTEN_PORT;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mailserver.internal.SmtpReceivedMessageHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.SMTPServer;

/**
 * {@link MailServerBridgeHandler} is the bridge handler listing for specific incoming mail and directing it
 * to the correct Thing.
 *
 * @author Jereme Guenther - Initial contribution
 */
public class MailServerBridgeHandler extends BaseBridgeHandler {

    private List<MailServerHandler> Inboxes = new CopyOnWriteArrayList<MailServerHandler>();

    private Logger logger = LoggerFactory.getLogger(MailServerBridgeHandler.class);
    private SMTPServer smtpServer;

    /**
     * Constructor
     *
     * @param bridge the bridge used by this thermostat
     */
    public MailServerBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        super.initialize();
        /*
         * Get config values
         */
        int listenPort = 25;
        try {
            Configuration config = this.getConfig();
            listenPort = ((BigDecimal) config.get(LISTEN_PORT)).intValue();
        } catch (NullPointerException e) {
            // keep default
        }

        // Try to start the SMTP server.
        try {

            Thing t = getThing();
            MailServerBridgeHandler h = (MailServerBridgeHandler) t.getHandler();

            SmtpReceivedMessageHandlerFactory myFactory = new SmtpReceivedMessageHandlerFactory();
            myFactory.h = h;
            smtpServer = new SMTPServer(myFactory);
            smtpServer.setPort(listenPort);

            logger.info("Starting SMTP Server on port {}", listenPort);
            smtpServer.start();

            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
            // keep default
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // there are no commmands for the bridge, return
        return;
    }

    @Override
    public void dispose() {
        try {
            smtpServer.stop();
            smtpServer = null;
        } catch (Exception ex) {
        }
        super.dispose();
    }

    /**
     * Method for handing out the received email. Called from @SmtpReceivedMessageHandler
     *
     * @param to
     * @param data
     */
    public void distributeRawMessageBodyData(String to, String data) {
        for (MailServerHandler box : Inboxes) {
            for (String address : to.split(";")) {
                if (!address.equals("") && box.ToAddress.equalsIgnoreCase(address)) {
                    box.parseRawMessageBodyData(data);
                }
            }
        }
    }

    /**
     * Registers @MailServerHandler listener that will fire the appropriate email arrives
     *
     * @param mailboxHandler the listener to register
     * @return if the listener was registered
     */
    public boolean registerMailListener(MailServerHandler mailboxHandler) {
        if (mailboxHandler == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null mailboxHandler.");
        }
        return Inboxes.add(mailboxHandler);
    }

    /**
     * Unrgisters @MailServerHandler listener
     *
     * @param mailboxHandler the listener to unregister
     * @return if the listener was unregistered
     */
    public boolean unregisterMailListener(MailServerHandler mailboxHandler) {
        return Inboxes.remove(mailboxHandler);
    }

}
