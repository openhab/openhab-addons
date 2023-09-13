/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.pioneeravr.internal.protocol;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.pioneeravr.internal.protocol.ParameterizedCommand.ParameterizedCommandType;
import org.openhab.binding.pioneeravr.internal.protocol.SimpleCommand.SimpleCommandType;
import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrCommand;
import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrConnection;
import org.openhab.binding.pioneeravr.internal.protocol.avr.CommandTypeNotSupportedException;
import org.openhab.binding.pioneeravr.internal.protocol.event.AvrDisconnectionEvent;
import org.openhab.binding.pioneeravr.internal.protocol.event.AvrDisconnectionListener;
import org.openhab.binding.pioneeravr.internal.protocol.event.AvrStatusUpdateEvent;
import org.openhab.binding.pioneeravr.internal.protocol.event.AvrUpdateListener;
import org.openhab.binding.pioneeravr.internal.protocol.utils.VolumeConverter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A class that wraps the communication to Pioneer AVR devices by using Input/Ouptut streams.
 *
 * see {@link http ://www.pioneerelectronics.com/StaticFiles/PUSA/Files/Home%20Custom %20Install/VSX-1120-K-RS232.PDF}
 * for the protocol specs
 *
 * Based on the Onkyo binding by Pauli Anttila and others.
 *
 * @author Antoine Besnard - Initial contribution
 * @author Rainer Ostendorf - Initial contribution
 * @author Leroy Foerster - Listening Mode, Playing Listening Mode
 */
public abstract class StreamAvrConnection implements AvrConnection {

    private final Logger logger = LoggerFactory.getLogger(StreamAvrConnection.class);

    // The maximum time to wait incoming messages.
    private static final Integer READ_TIMEOUT = 1000;

    private List<AvrUpdateListener> updateListeners;
    private List<AvrDisconnectionListener> disconnectionListeners;

    private IpControlInputStreamReader inputStreamReader;
    private DataOutputStream outputStream;

    public StreamAvrConnection() {
        this.updateListeners = new ArrayList<>();
        this.disconnectionListeners = new ArrayList<>();
    }

    @Override
    public void addUpdateListener(AvrUpdateListener listener) {
        synchronized (updateListeners) {
            updateListeners.add(listener);
        }
    }

    @Override
    public void addDisconnectionListener(AvrDisconnectionListener listener) {
        synchronized (disconnectionListeners) {
            disconnectionListeners.add(listener);
        }
    }

    @Override
    public boolean connect() {
        if (!isConnected()) {
            try {
                openConnection();

                // Start the inputStream reader.
                inputStreamReader = new IpControlInputStreamReader(getInputStream());
                inputStreamReader.start();

                // Get Output stream
                outputStream = new DataOutputStream(getOutputStream());
            } catch (IOException ioException) {
                logger.debug("Can't connect to {}. Cause: {}", getConnectionName(), ioException.getMessage());
            }
        }
        return isConnected();
    }

    /**
     * Open the connection to the AVR.
     *
     * @throws IOException
     */
    protected abstract void openConnection() throws IOException;

    /**
     * Return the inputStream to read responses.
     *
     * @return
     * @throws IOException
     */
    protected abstract InputStream getInputStream() throws IOException;

    /**
     * Return the outputStream to send commands.
     *
     * @return
     * @throws IOException
     */
    protected abstract OutputStream getOutputStream() throws IOException;

    @Override
    public void close() {
        if (inputStreamReader != null) {
            // This method block until the reader is really stopped.
            inputStreamReader.stopReader();
            inputStreamReader = null;
            logger.debug("Stream reader stopped!");
        }
    }

    /**
     * Sends to command to the receiver. It does not wait for a reply.
     *
     * @param ipControlCommand
     *            the command to send.
     **/
    protected boolean sendCommand(AvrCommand ipControlCommand) {
        boolean isSent = false;
        if (connect()) {
            String command = ipControlCommand.getCommand();
            try {
                if (logger.isTraceEnabled()) {
                    logger.trace("Sending {} bytes: {}", command.length(), HexUtils.bytesToHex(command.getBytes()));
                }
                outputStream.writeBytes(command);
                outputStream.flush();
                isSent = true;
            } catch (IOException ioException) {
                logger.error("Error occurred when sending command", ioException);
                // If an error occurs, close the connection
                close();
            }

            logger.debug("Command sent to AVR @{}: {}", getConnectionName(), command);
        }

        return isSent;
    }

    @Override
    public boolean sendPowerQuery(int zone) {
        return sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.POWER_QUERY, zone));
    }

    @Override
    public boolean sendVolumeQuery(int zone) {
        return sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.VOLUME_QUERY, zone));
    }

    @Override
    public boolean sendMuteQuery(int zone) {
        return sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.MUTE_QUERY, zone));
    }

    @Override
    public boolean sendInputSourceQuery(int zone) {
        return sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.INPUT_QUERY, zone));
    }

    @Override
    public boolean sendListeningModeQuery(int zone) {
        return sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.LISTENING_MODE_QUERY, zone));
    }

    @Override
    public boolean sendMCACCMemoryQuery() {
        return sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.MCACC_MEMORY_QUERY));
    }

    @Override
    public boolean sendPowerCommand(Command command, int zone) throws CommandTypeNotSupportedException {
        AvrCommand commandToSend = null;

        if (command == OnOffType.ON) {
            commandToSend = RequestResponseFactory.getIpControlCommand(SimpleCommandType.POWER_ON, zone);
            // Send the first Power ON command.
            sendCommand(commandToSend);

            // According to the Pioneer Specs, the first request only wakeup the
            // AVR CPU, the second one Power ON the AVR. Still according to the Pioneer Specs, the second
            // request has to be delayed of 100 ms.
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } else if (command == OnOffType.OFF) {
            commandToSend = RequestResponseFactory.getIpControlCommand(SimpleCommandType.POWER_OFF, zone);
        } else {
            throw new CommandTypeNotSupportedException("Command type not supported.");
        }

        return sendCommand(commandToSend);
    }

    @Override
    public boolean sendVolumeCommand(Command command, int zone) throws CommandTypeNotSupportedException {
        boolean commandSent = false;

        // The OnOffType for volume is equal to the Mute command
        if (command instanceof OnOffType) {
            commandSent = sendMuteCommand(command, zone);
        } else {
            AvrCommand commandToSend = null;

            if (command == IncreaseDecreaseType.DECREASE) {
                commandToSend = RequestResponseFactory.getIpControlCommand(SimpleCommandType.VOLUME_DOWN, zone);
            } else if (command == IncreaseDecreaseType.INCREASE) {
                commandToSend = RequestResponseFactory.getIpControlCommand(SimpleCommandType.VOLUME_UP, zone);
            } else if (command instanceof PercentType percentCommand) {
                String ipControlVolume = VolumeConverter
                        .convertFromPercentToIpControlVolume(percentCommand.doubleValue(), zone);
                commandToSend = RequestResponseFactory.getIpControlCommand(ParameterizedCommandType.VOLUME_SET, zone)
                        .setParameter(ipControlVolume);
            } else if (command instanceof DecimalType decimalCommand) {
                String ipControlVolume = VolumeConverter.convertFromDbToIpControlVolume(decimalCommand.doubleValue(),
                        zone);
                commandToSend = RequestResponseFactory.getIpControlCommand(ParameterizedCommandType.VOLUME_SET, zone)
                        .setParameter(ipControlVolume);
            } else {
                throw new CommandTypeNotSupportedException("Command type not supported.");
            }

            commandSent = sendCommand(commandToSend);
        }
        return commandSent;
    }

    @Override
    public boolean sendInputSourceCommand(Command command, int zone) throws CommandTypeNotSupportedException {
        AvrCommand commandToSend = null;

        if (command == IncreaseDecreaseType.INCREASE) {
            commandToSend = RequestResponseFactory.getIpControlCommand(SimpleCommandType.INPUT_CHANGE_CYCLIC, zone);
        } else if (command == IncreaseDecreaseType.DECREASE) {
            commandToSend = RequestResponseFactory.getIpControlCommand(SimpleCommandType.INPUT_CHANGE_REVERSE, zone);
        } else if (command instanceof StringType stringCommand) {
            String inputSourceValue = stringCommand.toString();
            commandToSend = RequestResponseFactory.getIpControlCommand(ParameterizedCommandType.INPUT_CHANNEL_SET, zone)
                    .setParameter(inputSourceValue);
        } else {
            throw new CommandTypeNotSupportedException("Command type not supported.");
        }

        return sendCommand(commandToSend);
    }

    @Override
    public boolean sendListeningModeCommand(Command command, int zone) throws CommandTypeNotSupportedException {
        AvrCommand commandToSend = null;

        if (command == IncreaseDecreaseType.INCREASE) {
            commandToSend = RequestResponseFactory.getIpControlCommand(SimpleCommandType.LISTENING_MODE_CHANGE_CYCLIC,
                    zone);
        } else if (command instanceof StringType stringCommand) {
            String listeningModeValue = stringCommand.toString();
            commandToSend = RequestResponseFactory
                    .getIpControlCommand(ParameterizedCommandType.LISTENING_MODE_SET, zone)
                    .setParameter(listeningModeValue);
        } else {
            throw new CommandTypeNotSupportedException("Command type not supported.");
        }

        return sendCommand(commandToSend);
    }

    @Override
    public boolean sendMuteCommand(Command command, int zone) throws CommandTypeNotSupportedException {
        AvrCommand commandToSend = null;

        if (command == OnOffType.ON) {
            commandToSend = RequestResponseFactory.getIpControlCommand(SimpleCommandType.MUTE_ON, zone);
        } else if (command == OnOffType.OFF) {
            commandToSend = RequestResponseFactory.getIpControlCommand(SimpleCommandType.MUTE_OFF, zone);
        } else {
            throw new CommandTypeNotSupportedException("Command type not supported.");
        }

        return sendCommand(commandToSend);
    }

    @Override
    public boolean sendMCACCMemoryCommand(Command command) throws CommandTypeNotSupportedException {
        AvrCommand commandToSend = null;

        if (command == IncreaseDecreaseType.INCREASE) {
            commandToSend = RequestResponseFactory.getIpControlCommand(SimpleCommandType.MCACC_MEMORY_CHANGE_CYCLIC);
        } else if (command instanceof StringType stringCommand) {
            String MCACCMemoryValue = stringCommand.toString();
            commandToSend = RequestResponseFactory.getIpControlCommand(ParameterizedCommandType.MCACC_MEMORY_SET)
                    .setParameter(MCACCMemoryValue);
        } else {
            throw new CommandTypeNotSupportedException("Command type not supported.");
        }

        return sendCommand(commandToSend);
    }

    /**
     * Read incoming data from the AVR and notify listeners for dataReceived and disconnection.
     *
     * @author Antoine Besnard
     *
     */
    private class IpControlInputStreamReader extends Thread {

        private BufferedReader bufferedReader;

        private volatile boolean stopReader;

        // This latch is used to block the stop method until the reader is really stopped.
        private CountDownLatch stopLatch;

        /**
         * Construct a reader that read the given inputStream
         *
         * @param ipControlSocket
         * @throws IOException
         */
        public IpControlInputStreamReader(InputStream inputStream) {
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            this.stopLatch = new CountDownLatch(1);

            this.setDaemon(true);
            this.setName("IpControlInputStreamReader-" + getConnectionName());
        }

        @Override
        public void run() {
            try {
                while (!stopReader && !Thread.currentThread().isInterrupted()) {
                    String receivedData = null;
                    try {
                        receivedData = bufferedReader.readLine();
                    } catch (SocketTimeoutException e) {
                        // Do nothing. Just happen to allow the thread to check if it has to stop.
                    }

                    if (receivedData != null) {
                        logger.debug("Data received from AVR @{}: {}", getConnectionName(), receivedData);
                        AvrStatusUpdateEvent event = new AvrStatusUpdateEvent(StreamAvrConnection.this, receivedData);
                        synchronized (updateListeners) {
                            for (AvrUpdateListener pioneerAvrEventListener : updateListeners) {
                                pioneerAvrEventListener.statusUpdateReceived(event);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.warn("The AVR @{} is disconnected.", getConnectionName(), e);
                AvrDisconnectionEvent event = new AvrDisconnectionEvent(StreamAvrConnection.this, e);
                for (AvrDisconnectionListener pioneerAvrDisconnectionListener : disconnectionListeners) {
                    pioneerAvrDisconnectionListener.onDisconnection(event);
                }
            }

            // Notify the stopReader method caller that the reader is stopped.
            this.stopLatch.countDown();
        }

        /**
         * Stop this reader. Block until the reader is really stopped.
         */
        public void stopReader() {
            this.stopReader = true;
            try {
                this.stopLatch.await(READ_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // Do nothing. The timeout is just here for safety and to be sure that the call to this method will not
                // block the caller indefinitely.
                // This exception should never happen.
            }
        }
    }
}
