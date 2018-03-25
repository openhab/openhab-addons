/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.resources;

import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.io.IOException;
import java.util.ArrayList;

import org.openhab.binding.heos.internal.api.HeosEventController;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosSendCommand} is responsibel to send a command
 * to the HEOS bridge
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosSendCommand {

    private Telnet client;
    private HeosJsonParser parser;
    private HeosResponse response;
    private HeosEventController eventController;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String command = "";

    public HeosSendCommand(Telnet client, HeosJsonParser parser, HeosResponse response,
            HeosEventController eventController) {
        this.client = client;
        this.parser = parser;
        this.response = response;
        this.eventController = eventController;
    }

    public synchronized boolean send(String command) throws ReadException, IOException {
        if (!isConnected()) {
            return false;
        }
        int sendTryCounter = 0;
        this.command = command;

        if (executeSendCommand()) {
            while (sendTryCounter < 1) {
                if (response.getEvent().getResult().equals(FAIL)) {
                    executeSendCommand();
                    ++sendTryCounter;
                }
                if (response.getEvent().getMessagesMap().get(COM_UNDER_PROCESS).equals(TRUE)) {
                    while (response.getEvent().getMessagesMap().get(COM_UNDER_PROCESS).equals(TRUE)) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            logger.debug("Interrupted Exception - Message: {}", e.getMessage());
                        }
                        ArrayList<String> readResultList = client.readLine(15000);

                        for (int i = 0; i < readResultList.size(); i++) {
                            parser.parseResult(readResultList.get(i));
                            eventController.handleEvent(0); // Important don't remove it. Costs you some live time... ;)
                        }
                    }
                } else {
                    return true;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method shall only be used if no response from network
     * is expected. Else the read buffer is not cleared
     *
     * @param command
     * @return true if send was successful
     */

    public boolean sendWithoutResponse(String command) {
        try {
            return client.send(command);
        } catch (IOException e) {
            logger.debug("IO Excecption - Message: {}", e.getMessage());
            return false;
        }
    }

    /*
     * It seems to be that sometime a command is still
     * in the reading line without being read out. This
     * shall be prevented with an Map which reads until no
     * End of line is detected.
     */

    private boolean executeSendCommand() throws ReadException, IOException {
        boolean sendSuccess = client.send(command);
        if (sendSuccess) {
            ArrayList<String> readResultList = client.readLine();

            for (int i = 0; i < readResultList.size(); i++) {
                parser.parseResult(readResultList.get(i));
                eventController.handleEvent(0);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean setTelnetClient(Telnet client) {
        this.client = client;
        return true;
    }

    public Telnet getTelnetClient() {
        return client;
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public boolean isConnectionAlive() {
        return client.isConnectionAlive();
    }
}
