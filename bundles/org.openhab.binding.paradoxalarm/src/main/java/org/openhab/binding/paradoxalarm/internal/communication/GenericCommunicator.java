/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxRuntimeException;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GenericCommunicator} Used for the common communication logic for all types of panels.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class GenericCommunicator extends AbstractCommunicator implements IResponseReceiver {

    private final Logger logger = LoggerFactory.getLogger(GenericCommunicator.class);

    private final byte[] pcPasswordBytes;
    private byte[] panelInfoBytes;
    private boolean isEncrypted;
    private final String password;

    public GenericCommunicator(String ipAddress, int tcpPort, String ip150Password, String pcPassword,
            ScheduledExecutorService scheduler, boolean useEncryption) throws UnknownHostException, IOException {
        super(ipAddress, tcpPort, scheduler);
        this.isEncrypted = useEncryption;
        logger.debug("Use encryption={}", isEncrypted);
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
    protected void receiveEpromResponse(IResponse response) {
        // Nothing to do here. Override in particular implementation class.
    }

    @Override
    protected void receiveRamResponse(IResponse response) {
        // Nothing to do here. Override in particular implementation class.
    }

    public void refreshMemoryMap() {
        // Nothing to do here. Override in particular implementation class.
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

    @Override
    public boolean isEncrypted() {
        return isEncrypted;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void receiveResponse(IResponse response, IParadoxInitialLoginCommunicator communicator) {
        IRequest request = response.getRequest();
        logger.trace("Handling response for request={}", request);
        ParadoxUtil.printPacket("Full packet", response.getPacketBytes());
        RequestType type = request.getType();
        if (type == RequestType.RAM) {
            receiveRamResponse(response);
        } else if (type == RequestType.EPROM) {
            receiveEpromResponse(response);
        } else {
            logger.debug("Probably wrong sender in the request. Request type is not one of the supported methods.");
        }
    }
}
