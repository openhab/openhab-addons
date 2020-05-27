/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxException;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxRuntimeException;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GenericCommunicator} Used for the common communication logic for all types of panels.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class GenericCommunicator extends AbstractCommunicator implements IParadoxInitialLoginCommunicator {

    private final Logger logger = LoggerFactory.getLogger(GenericCommunicator.class);

    private final String password;
    private final byte[] pcPasswordBytes;
    private byte[] panelInfoBytes;

    public GenericCommunicator(String ipAddress, int tcpPort, String ip150Password, String pcPassword,
            ScheduledExecutorService scheduler) throws UnknownHostException, IOException {
        super(ipAddress, tcpPort, scheduler);
        this.password = ip150Password;
        this.pcPasswordBytes = ParadoxUtil.stringToBCD(pcPassword);
    }

    @Override
    public synchronized void startLoginSequence() {
        logger.debug("Login sequence started");

        if (isOnline()) {
            logger.debug("Already logged on. No action needed. Returning.");
            return;
        }

        if (socket.isClosed()) {
            try {
                initializeSocket();
            } catch (IOException e) {
                throw new ParadoxRuntimeException(e);
            }
        }

        CommunicationState.login(this);
    }

    @Override
    protected void handleReceivedPacket(IResponse response) {
        retryCounter = 0;
        IRequest request = response.getRequest();
        logger.trace("Handling response for request={}", request);

        RequestType type = request.getType();
        // Send back the response to proper receive methods
        switch (type) {
            case LOGON_SEQUENCE:
                CommunicationState logonSequenceSender = ((LogonRequest) request).getLogonSequenceSender();
                logonSequenceSender.receiveResponse(this, response);
                break;
            case RAM:
                try {
                    receiveRamResponse(response);
                } catch (ParadoxException e) {
                    RamRequest ramRequest = (RamRequest) request;
                    logger.debug("Unable to retrieve RAM message for memory page={}", ramRequest.getRamBlockNumber());
                }
                break;
            case EPROM:
                try {
                    receiveEpromResponse(response);
                } catch (ParadoxException e) {
                    EpromRequest epromRequest = (EpromRequest) request;
                    logger.debug("Unable to retrieve EPROM message for entity Type={}, Id={}",
                            epromRequest.getEntityType(), epromRequest.getEntityId());
                }
                break;
        }
    }

    @Override
    public byte[] getPanelInfoBytes() {
        return panelInfoBytes;
    }

    @Override
    public void setPanelInfoBytes(byte[] panelInfoBytes) {
        this.panelInfoBytes = panelInfoBytes;
    }

    @Override
    public byte[] getPcPasswordBytes() {
        return pcPasswordBytes;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    protected void receiveEpromResponse(IResponse response) throws ParadoxException {
        // Nothing to do here. Override in sub class.
    }

    @Override
    protected void receiveRamResponse(IResponse response) throws ParadoxException {
        // Nothing to do here. Override in sub class.
    }

    public void refreshMemoryMap() {
        // Nothing to do here. Override in sub class.
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public void setListeners(Collection<IDataUpdateListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void updateListeners() {
        if (listeners != null && !listeners.isEmpty()) {
            listeners.forEach(IDataUpdateListener::update);
        }
    }
}
