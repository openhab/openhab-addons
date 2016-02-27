/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tivo.handler;

import static org.openhab.binding.tivo.TiVoBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TiVoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jayson Kubilis - Initial contribution
 */
public class TiVoHandler extends BaseThingHandler implements DiscoveryListener {

    private Logger logger = LoggerFactory.getLogger(TiVoHandler.class);

    // Class variables to hold our connection out to our tivo managed by this instance of a Thing
    private Socket tivoSocket = null;
    private PrintStream tivoIOSendCommand = null;
    private BufferedReader tiviIOReadStatus = null;

    private String cfgHost = null;
    private int cfgTcpPort = -1;
    private int cfgNumConnRetry = 0;
    private int cfgPollInterval = 30; // a default
    private boolean cfgPollChanges = false;
    private boolean cfgKeepConnOpen = false;
    private int cfgCmdWait = 0;

    private TivoStatusData tivoStatusData = null;

    private ScheduledFuture<?> refreshJob;

    SortedSet<Integer> cfgIgnoreChannels = null;

    private DiscoveryServiceRegistry discoveryServiceRegistry;

    public TiVoHandler(Thing thing, DiscoveryServiceRegistry discoveryServiceRegistry) {

        super(thing);
        logger.debug("Creating a TiVo handler for thing '{}'", getThing().getUID());
        if (discoveryServiceRegistry != null) {
            this.discoveryServiceRegistry = discoveryServiceRegistry;
        }
    }

    // This will query the tivo and store the status.

    public TivoStatusData doRefreshDeviceStatus() {

        // what the heck is going on with the state of all of all of the variables i should be able to see?
        // turned out the beta version creates 2 instances of the scheduled polling job and that was what
        // was causing the problems I was seeing.

        if (tivoStatusData != null) {
            logger.debug("While refreshing thing '{}' - status data FOUND in class - '{}'", getThing().getUID(),
                    tivoStatusData.toString());
        } else {
            logger.debug("While refreshing thing '{}' - status data NOT FOUND in class!", getThing().getUID());
        }

        TivoStatusData myTivo = getTivoStatus();

        if (myTivo != null) {
            logger.debug("While refreshing thing '{}' - status data FOUND in CURRENT CHECK - '{}'", getThing().getUID(),
                    myTivo.toString());
        } else {
            logger.debug("While refreshing thing '{}' - status data NOT FOUND in CURRENT CHECK!", getThing().getUID());
        }

        if (myTivo != null && myTivo.isCmdOk()) {
            // pTivoData = myTivo;
            tivoStatusData = myTivo;
        } else {
            logger.debug("While refreshing thing '{}' TiVo returned no status, I will return a stored status",
                    getThing().getUID());
        }
        // myTivo = null;

        updateStatus();

        return tivoStatusData;
    }

    private void updateStatus() {

        if (tivoStatusData != null) {

            updateState(new ChannelUID(getThing().getUID(), CHANNEL_TIVO_STATUS),
                    new StringType(tivoStatusData.getMsg()));
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_TIVO_CHANNEL_FORCE),
                    new DecimalType(tivoStatusData.getChannelNum()));
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_TIVO_CHANNEL_SET),
                    new DecimalType(tivoStatusData.getChannelNum()));

        } else {
            logger.error("While refreshing thing '{}' no tivo status found to report!", getThing().getUID());
        }
    }

    private void addIgnoredChannel(Integer pChannel) {
        logger.info("TiVo handler for thing '{}' Adding new ignored channel '{}'", getThing().getUID(), pChannel);

        // if we already see this channel as being ignored there is no reason to ignore it again.
        if (getChkIgnoredChannel(pChannel)) {
            return;
        }

        Configuration conf = editConfiguration();
        cfgIgnoreChannels.add(pChannel);
        conf.put(CONFIG_IGNORE_CHANNELS, doCfgParseIgnoreChannel(cfgIgnoreChannels));
        updateConfiguration(conf);

    }

    private boolean getChkIgnoredChannel(int pChannel) {

        if (cfgIgnoreChannels != null && cfgIgnoreChannels.contains(pChannel)) {
            return true;
        } else {
            return false;
        }
    }

    private int getNextChannel(int pChannel) {

        // if (getChkIgnoredChannel(pChannel)) {
        if (tivoStatusData != null && tivoStatusData.isCmdOk()) {
            // retry logic is allowed otherwise we only do the logic below once.

            if (tivoStatusData.getChannelNum() > pChannel && pChannel > 1) {
                // we appear to be changing the channel DOWNWARD so we try to go down a bit further
                // addIgnoredChannel(chnl);
                pChannel--;
                logger.info("TiVo handler for thing '{}' next proposed channel - '{}'", getThing().getUID(), pChannel);
            } else if (tivoStatusData.getChannelNum() < pChannel) {
                // addIgnoredChannel(chnl);
                pChannel++;
                logger.info("TiVo handler for thing '{}' next proposed channel + '{}'", getThing().getUID(), pChannel);
            } else {
                // either we are going to attempt to change a channel to less then 1 or
                // its already on the same channel. we shouldn't retry this here.
                pChannel = -1;
            }

        }
        // }

        return pChannel;

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command on channel: {}, command: {}", channelUID, command);
        // TODO: handle commands

        TivoStatusData myTivo = null;
        String tivoCommand = null;

        switch (channelUID.getId()) {
            case CHANNEL_TIVO_CHANNEL_FORCE:
                tivoCommand = "FORCECH";
            case CHANNEL_TIVO_CHANNEL_SET:
                logger.debug("  CHANNEL_TIVO_CHANNEL found!");

                if (tivoCommand == null) {
                    tivoCommand = "SETCH";
                }

                int chnl = -1;

                try {
                    // if we can compare this to the current channel we will try and determine the "direction" (up or
                    // down) we are going

                    int numTries = 5;
                    chnl = Double.valueOf(command.toString()).intValue();

                    // check for ignored channels

                    // execute, check and learn new ignored channels

                    while (numTries > 0 && chnl > 0) {
                        numTries--;

                        while (getChkIgnoredChannel(chnl) && getNextChannel(chnl) > 0) {
                            logger.debug("TiVo handler for thing '{}' skipping channel: '{}'", getThing().getUID(),
                                    chnl);
                            chnl = getNextChannel(chnl);
                        }

                        String tmpCommand = tivoCommand + " " + chnl;

                        logger.debug("TiVo handler for thing '{}' sending command to tivo: '{}'", getThing().getUID(),
                                tmpCommand);

                        // Attempt to execute the command on the tivo
                        myTivo = setTivoStatus(tmpCommand);

                        // Check to see if the command was successful
                        if (myTivo != null && myTivo.isCmdOk()) {
                            numTries = 0; // were done trying.
                            tivoStatusData = myTivo;
                            logger.debug(" Returned Tivo Data Object: '{}'", myTivo.toString());
                        } else {
                            logger.error("TiVo handler for thing '{}' command failed '{}'", getThing().getUID(),
                                    tmpCommand);
                            // Here's a little magic we can play if the command was not executed successfully
                            // we can "channel down/up to the next available channel
                            addIgnoredChannel(chnl);
                            chnl = getNextChannel(chnl);
                            if (chnl > 0) {
                                logger.info("TiVo handler for thing '{}' retrying next channel '{}'",
                                        getThing().getUID(), chnl);
                            } else {
                                numTries = 0;
                            }

                        }

                    }

                    // myTivo = null;

                } catch (NumberFormatException e) {
                    logger.error(
                            "TiVo handler for thing '{}' unable to parse channel integer from CHANNEL_TIVO_CHANNEL: '{}'",
                            getThing().getUID(), command.toString());
                }

                // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TIVO_STATUS),
                // new StringType(tivoStatusData.getMsg()));
                // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TIVO_CHANNEL),
                // new DecimalType(tivoStatusData.getChannelNum()));

                updateStatus();

                break;

            case CHANNEL_TIVO_COMMAND:
                logger.debug("  CHANNEL_TIVO_COMMAND found!");
                // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TIVO_STATUS), new
                // StringType(command.toString()));
                // this.setTivoConnected(true);

                logger.debug("TiVo handler for thing '{}' sending CUSTOM command to tivo: '{}'", getThing().getUID(),
                        command.toString());

                // Attempt to execute the command on the tivo
                myTivo = setTivoStatus(command.toString());

                // Check to see if the command was successful
                if (myTivo != null && myTivo.isCmdOk()) {
                    tivoStatusData = myTivo;
                    logger.debug(" Returned Tivo Data Object: '{}'", myTivo.toString());
                } else {
                    logger.error("TiVo handler for thing '{}' command failed '{}'", getThing().getUID(),
                            command.toString());
                }

                myTivo = null;

                updateStatus();

                break;
            case CHANNEL_TIVO_TELEPORT:
                logger.debug("  CHANNEL_TIVO_TELEPORT found!");
                // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TIVO_STATUS), new
                // StringType(command.toString()));
                // this.setTivoConnected(true);

                tivoCommand = "TELEPORT " + command.toString();

                logger.debug("TiVo handler for thing '{}' TELEPORT command to tivo: '{}'", getThing().getUID(),
                        tivoCommand);

                // Attempt to execute the command on the tivo
                myTivo = setTivoStatus(tivoCommand);

                // Check to see if the command was successful
                if (myTivo != null && myTivo.isCmdOk()) {
                    tivoStatusData = myTivo;
                    logger.debug(" Returned Tivo Data Object: '{}'", myTivo.toString());
                }

                // Per Tivo documentation this command will never fail. We also don't really want to store this status
                // anyway.
                // else {
                // logger.error("TiVo handler for thing '{}' command failed '{}'", getThing().getUID(), tivoCommand);
                // }

                myTivo = null;

                updateStatus();

                break;

            case CHANNEL_TIVO_IRCMD:
                tivoCommand = "IRCODE";
            case CHANNEL_TIVO_KBDCMD:
                logger.debug("  CHANNEL_TIVO_IR/KBDCMD found!");

                if (tivoCommand == null) {
                    tivoCommand = "KEYBOARD";
                }

                String tmpCommand = tivoCommand + " " + command.toString();

                logger.debug("TiVo handler for thing '{}' IR/KBD command to tivo: '{}'", getThing().getUID(),
                        tmpCommand);

                // Attempt to execute the command on the tivo
                myTivo = setTivoStatus(tmpCommand);

                // Check to see if the command was successful
                if (myTivo != null && myTivo.isCmdOk()) {
                    tivoStatusData = myTivo;
                    logger.debug(" Returned Tivo Data Object: '{}'", myTivo.toString());
                }
                // Tivo doesnt provide feedback on these commands either.
                // else {
                // logger.error("TiVo handler for thing '{}' command failed '{}'", getThing().getUID(), tmpCommand);
                // }

                myTivo = null;

                updateStatus();

                break;
        }
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.INITIALIZING);

        logger.debug("Initializing a TiVo handler for thing '{}' with config options", getThing().getUID());

        Configuration conf = this.getConfig();

        cfgHost = String.valueOf(conf.get(CONFIG_ADDRESS));
        cfgTcpPort = Integer.parseInt(String.valueOf(conf.get(CONFIG_PORT)));
        cfgNumConnRetry = Integer.parseInt(String.valueOf(conf.get(CONFIG_CONNECTION_RETRY)));
        cfgPollInterval = Integer.parseInt(String.valueOf(conf.get(CONFIG_POLL_INTERVAL)));
        cfgPollChanges = Boolean.parseBoolean(String.valueOf(conf.get(CONFIG_POLL_FOR_CHANGES)));
        cfgKeepConnOpen = Boolean.parseBoolean(String.valueOf(conf.get(CONFIG_KEEP_CONNECTION_OPEN)));
        cfgCmdWait = Integer.parseInt(String.valueOf(conf.get(CONFIG_CMD_WAIT_INTERVAL)));
        cfgIgnoreChannels = doCfgParseIgnoreChannel(String.valueOf(conf.get(CONFIG_IGNORE_CHANNELS)));

        logger.debug(
                "  '{}' host: '{}' port: '{}' retries: '{}' poll_interval: '{}'(s) poll_changes: '{}' keep_conn_open: '{}'",
                getThing().getUID(), cfgHost, cfgTcpPort, cfgNumConnRetry, cfgPollInterval, cfgPollChanges,
                cfgKeepConnOpen);

        addIgnoredChannel(1);
        addIgnoredChannel(9);
        addIgnoredChannel(7);
        addIgnoredChannel(5);

        // Initialize our connection to the tivo box
        if (this.setTivoConnectedRetry(true)) {
            updateStatus(ThingStatus.ONLINE); // No reason why we shouldn't be ready to roll now

            if (!cfgKeepConnOpen) {
                this.setTivoConnectedRetry(false);
            }
            if (cfgPollChanges) {
                startPollStatus();
            }

            tivoStatusData = doRefreshDeviceStatus();

            // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TIVO_STATUS), new
            // StringType(getTivoStatus().toString()));

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR); // COMMUNICATION_ERROR); //
                                                                                      // Something bad happened. Check
                                                                                      // the LOGS!
        }

        if (this.discoveryServiceRegistry != null) {
            this.discoveryServiceRegistry.addDiscoveryListener(this);
        }
        // Should we do our first data update here?

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");

        logger.debug("Initializing a TiVo handler for thing '{}' - finished!", getThing().getUID());
    }

    @Override
    public void dispose() {

        logger.debug("Disposing of a TiVo handler for thing '{}'", getThing().getUID());

        if (refreshJob != null) {
            refreshJob.cancel(true);
        }

        // Disconnect from the TiVo - It's all over now
        setTivoConnected(false);

    }

    private TivoStatusData setTivoStatus(String pCmd) {

        boolean oC = false; // Tracks if this step caused a connection to be opened
        TivoStatusData tmp = null;

        // Will open a connection prior to operation if we are not keeping one open.
        if (!cfgKeepConnOpen && !getTivoConnected()) {
            setTivoConnected(true);
            oC = true;
        }

        tmp = setTivoStatus(pCmd, cfgNumConnRetry);

        // Will close the connection after operation if we are not keeping one open.
        if (!cfgKeepConnOpen && getTivoConnected() && oC) {
            setTivoConnected(false);
        }

        return tmp;

    }

    private TivoStatusData setTivoStatus(String pCmd, int retryCount) {
        logger.debug("TiVo handler for thing '{}' - sending message: '{}' retry @ '{}'", getThing().getUID(), pCmd,
                retryCount);

        if (!getTivoConnected()) {
            logger.error("TiVo handler for thing '{}' - called setTivoStatus but not connected!", getThing().getUID());
            return new TivoStatusData(false, -1, "NOT_CONNECTED");
        }

        try {

            tivoIOSendCommand.println(pCmd + "\r");
            tivoIOSendCommand.flush();
            TimeUnit.MILLISECONDS.sleep(cfgCmdWait);
            logger.debug("TiVo handler for thing '{}' - I felt like napping for '{}' milliseconds", getThing().getUID(),
                    cfgCmdWait);

            if (tivoIOSendCommand.checkError()) {
                logger.error("TiVo handler for thing '{}' called setTivoStatus and encountered an IO error!",
                        getThing().getUID());

                setTivoConnected(false); // close connection
                setTivoConnected(true); // open a new connection

                if (retryCount <= 0) {
                    // We have failed at life... Give up.
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "CONNECTION_RETRIES_EXHAUSTED");
                    return new TivoStatusData(false, -1, "CONNECTION_RETRIES_EXHAUSTED");

                } else {
                    return setTivoStatus(pCmd, retryCount - 1);
                }

            }

        } catch (Exception e) {
            logger.error("TiVo handler for thing '{}' - message send failed '{}'", e.getMessage());

        }

        // now read the status since we have sent a command
        return getTivoStatus();

    }

    private TivoStatusData getTivoStatus() {

        boolean oC = false;
        TivoStatusData rtnVal = null;

        // Will open a connection prior to operation if we are not keeping one open.
        if (!cfgKeepConnOpen && !getTivoConnected()) {
            setTivoConnected(true);
            oC = true;
        }

        rtnVal = getTivoStatus(cfgNumConnRetry);

        // Will close the connection after operation if we are not keeping one open.
        if (!cfgKeepConnOpen && getTivoConnected() && oC) {
            setTivoConnected(false);
        }

        // Do the needful.
        return rtnVal;
    }

    private TivoStatusData getTivoStatus(int retryCount) {
        logger.debug("TiVo handler for thing '{}' - reading from TiVo - retry @ '{}'", getThing().getUID(), retryCount);

        String line = null;
        String tivoStatus = null;

        // If were not connected we can't provide a status
        if (!getTivoConnected()) {
            logger.error("TiVo handler for thing '{}' - called getTivoStatus but not connected!", getThing().getUID());
            return new TivoStatusData(false, -1, "NOT_CONNECTED");
        }

        try { // tiviIOReadStatus.ready() &&
            while ((line = tiviIOReadStatus.readLine()) != null) {
                logger.debug(" TiVo handeler read: '{}'", line);
                if (!line.isEmpty() && !line.trim().equalsIgnoreCase("COMMAND_TIMEOUT")) {
                    // use this line
                    tivoStatus = line;
                } else {
                    logger.debug(" TiVo handeler ignored line: '{}'", line);
                }

            }
        } catch (IOException e) {

            if (e.getMessage().equalsIgnoreCase("READ TIMED OUT")) {
                logger.debug("TiVo handler for thing '{}' - nothing to read from the stream - this is normal",
                        getThing().getUID());
            } else if (e.getMessage().equalsIgnoreCase("Connection reset")) {
                logger.error("TiVo handler for thing '{}' - CONNECTION_RESET! '{}'", getThing().getUID(),
                        e.getMessage());

                setTivoConnected(false); // close connection
                setTivoConnected(true); // open a new connection

                if (retryCount <= 0) {
                    // We have failed at life... Give up.
                    tivoStatus = "";
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "CONNECTION_RETRIES_EXHAUSTED");
                    return new TivoStatusData(false, -1, "CONNECTION_RETRIES_EXHAUSTED");

                } else {
                    return getTivoStatus(retryCount - 1);
                }

            } else {
                logger.error("TiVo handler for thing '{}' - couldn't read from the tivo - '{}'", getThing().getUID(),
                        e.getMessage());
            }
        }

        return getParsedStatus(tivoStatus);

    }

    private TivoStatusData getParsedStatus(String strTivoStatus) {

        if (strTivoStatus == null || strTivoStatus.equals("")) {
            return new TivoStatusData(false, -1, "NO_STATUS_DATA_RETURNED");
        }

        Integer tivoC = getParsedChannel(strTivoStatus);

        if (tivoC != null) {
            // tivoChannelInt = tivoC.intValue();
            return new TivoStatusData(true, tivoC.intValue(), strTivoStatus);
        } else {
            // right now we are just assuming if we can't get a channel number back then it wasnt successful.
            return new TivoStatusData(false, -1, strTivoStatus);
        }

    }

    private Integer getParsedChannel(String strTivoStatus) {

        logger.debug("TiVo handler for thing '{}' - getParsedChannel running on string '{}'", getThing().getUID(),
                strTivoStatus);

        if (strTivoStatus == null) {
            return -1;
        }

        Integer retVal = null;

        // Pattern tivoStatusPattern = Pattern.compile("[0]+(\\d+\\s*\\d*)");
        Pattern tivoStatusPattern = Pattern.compile("[0]+(\\d+)\\s+");
        Matcher matcher = tivoStatusPattern.matcher(strTivoStatus);
        if (matcher.find()) {
            logger.debug("TiVo handler for thing '{}' - getParsedChannel groups '{}' with group count of '{}'",
                    getThing().getUID(), matcher.group(), matcher.groupCount());
            if (matcher.groupCount() == 1) {
                // retVal = matcher.group(1).trim().replace(' ', '-');
                retVal = new Integer(Integer.parseInt(matcher.group(1).trim()));
            }
            logger.debug("TiVo handler for thing '{}' - getParsedChannel parsed channel '{}'", getThing().getUID(),
                    retVal);
        } else {
            logger.debug("TiVo handler for thing '{}' - getParsedChannel NO MATCH on string '{}'", getThing().getUID(),
                    strTivoStatus);
        }

        return retVal;
    }

    private boolean getTivoConnected() {
        if (tivoSocket != null && tivoSocket.isConnected()) {
            return true;
        } else {
            return false;
        }

    }

    private boolean setTivoConnectedRetry(boolean pConnect) {
        // only makes sense to retry connection attempts, not disconnection attempts.
        if (pConnect) {
            for (int i = 0; i < cfgNumConnRetry; i++) {
                logger.warn("TiVo handler for thing '{}' - connection attempt '{} of '{}'", getThing().getUID(), i + 1,
                        cfgNumConnRetry);
                if (setTivoConnected(pConnect)) {
                    return true;
                }

            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return false;
        }

        return setTivoConnected(pConnect);

    }

    private boolean setTivoConnected(boolean pConnect) {

        // Handles case where we are connected and wish to disconnect
        if (!pConnect && getTivoConnected()) {
            logger.warn("TiVo handeler for thing '{}' - disconnecting", getThing().getUID());
            try {
                tivoSocket.close();
            } catch (IOException e) {
                logger.error("TiVo handler for thing '{}' - I/O exception: '{}' so setting connection as closed",
                        getThing().getUID(), e.getMessage());

                return false;
            }
            tivoSocket = null;
            return true;

        } else if (!pConnect) {// handles case where we wish to disconnect but we were not actually connected
            logger.warn("TiVo handeler for thing '{}' - disconnect requested but not connected, ignoring",
                    getThing().getUID());
            return true;
        } else { // The only other case is we want to connect

            // check to see if we are already connected
            if (getTivoConnected()) {
                logger.warn("TiVo handeler for thing '{}' - connection attempt while aready connected made, ignoring",
                        getThing().getUID());
                return true;
            }

            logger.debug(
                    "TiVo handeler for thing '{}' - setTivoConnected attempting connection to host '{}', port '{}'",
                    getThing().getUID(), cfgHost, cfgTcpPort);

            try {

                tivoSocket = new Socket(cfgHost, cfgTcpPort);
                tivoSocket.setKeepAlive(true); // let the system keep the connection alive to prevent a firewall or
                                               // something else from closing it
                tivoSocket.setSoTimeout(CONFIG_SOCKET_TIMEOUT); // set timeout for the connection to 1 seconds before we
                                                                // give up

                if (tivoSocket.isConnected()) {

                    tivoIOSendCommand = new PrintStream(tivoSocket.getOutputStream(), true);
                    tiviIOReadStatus = new BufferedReader(new InputStreamReader(tivoSocket.getInputStream()));

                } else {
                    logger.error("TiVo handler for thing '{}' - couldn't connect!", getThing().getUID());
                    return false;
                }

            } catch (UnknownHostException e) {
                logger.error("TiVo handler for thing '{}' - unknown host error: '{}'", getThing().getUID(),
                        e.getMessage());
                return false;
            } catch (IOException e) {
                logger.error("TiVo handler for thing '{}' - I/O exception: '{}'", getThing().getUID(), e.getMessage());
                return false;
            }

            logger.debug("TiVo handler for thing '{}' - setTivoConnected CONNECTED to host '{}', on port '{}'",
                    getThing().getUID(), cfgHost, cfgTcpPort);

            return true;
        }
    }

    private void startPollStatus() {

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    logger.info("Refreshing thing '{}' @ rate of '{}' seconds", getThing().getUID(), cfgPollInterval);

                    doRefreshDeviceStatus();

                } catch (Exception e) {
                    logger.debug("Exception occurred during Refresh: {}", e);
                }
            }
        };

        refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, cfgPollInterval, TimeUnit.SECONDS);

    }

    private SortedSet<Integer> doCfgParseIgnoreChannel(String pChannels) {

        logger.debug("TiVo handler for thing '{}' called doCfgParseIgnoreChannel w/ list: '{}'", getThing().getUID(),
                pChannels);

        SortedSet<Integer> result = new TreeSet<Integer>();

        // List<Integer> result = new ArrayList();

        if (pChannels.equals("")) {
            return result;
        }

        List<String> tmp = Arrays.asList(pChannels.split("\\s*,\\s*"));

        try {
            for (int i = 0; i < tmp.size(); i++) {
                logger.debug("  parser parsing '{}'", tmp.get(i));
                // Determine if we have a string with a '-' in it.
                if (tmp.get(i).matches(".+-.+")) {
                    logger.debug("  parser matched dash (-) on string '{}'", tmp.get(i));
                    List<String> sTmp = Arrays.asList(tmp.get(i).split("-"));
                    if (sTmp != null && sTmp.size() == 2) {
                        int s = Integer.parseInt(sTmp.get(0));
                        int e = Integer.parseInt(sTmp.get(1));

                        logger.debug("  found start '{}' and end '{}'", s, e);

                        if (e < s) { // some funny guy eh?
                            s = Integer.parseInt(sTmp.get(1));
                            e = Integer.parseInt(sTmp.get(0));
                        }
                        while (s <= e) {
                            if (result.contains(s)) {
                                logger.debug("  element already in list - '{}'", s);
                            } else {
                                logger.debug("  adding element to list - '{}'", s);
                                result.add(s);
                            }
                            s++;
                        }
                    } else {
                        logger.debug("  parser matched - on string but didn't have an expected size");
                    }
                } else {
                    logger.debug("  parser didn't match - on string '{}', must be singleton", tmp.get(i));
                    int se = Integer.parseInt(tmp.get(i));

                    if (result.contains(se)) {
                        logger.debug("  element already in list - '{}'", se);
                    } else {
                        logger.debug("  adding element to list - '{}'", se);
                        result.add(se);
                    }
                }
            }
        } catch (NumberFormatException e) {
            logger.error(
                    "TiVo handler for thing '{}' was unable to parse list of channels to ignore from thing settings: {}",
                    getThing().getUID(), pChannels);
        }

        tmp = null;
        return result;
    }

    private String doCfgParseIgnoreChannel(SortedSet<Integer> pSet) {

        // SortedSet<Integer> numbers = new TreeSet<Integer>();

        String rtnString = "";

        Integer start = null;
        Integer end = null;

        for (Integer num : pSet) {
            // initialize
            if (start == null || end == null) {
                start = num;
                end = num;
            }
            // next number in range
            else if (end.equals(num - 1)) {
                end = num;
            }
            // there's a gap
            else {
                // range length 1
                if (start.equals(end)) {
                    rtnString += start + ",";
                }
                // range length 2
                else if (start.equals(end - 1)) {
                    rtnString += start + "," + end + ",";
                }
                // range lenth 2+
                else {
                    rtnString += start + "-" + end + ",";
                }

                start = num;
                end = num;
            }
        }

        if (start != null && start.equals(end)) {
            rtnString += start;
        } else if (start != null && end != null && start.equals(end - 1)) {
            rtnString += start + "," + end;
        } else if (start != null && end != null) {
            rtnString += start + "-" + end;
        }

        return rtnString;

    }

    @Override
    public void thingUpdated(Thing thing) {
        // TODO: test for changes in config and send update
        logger.debug("TiVo handler for thing '{}' - thingUpdated", getThing().getUID());
        super.thingUpdated(thing);
    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        logger.debug("thingDiscovered thing '{}'", getThing().getUID());
        if (result.getThingUID().equals(this.getThing().getUID())) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        logger.debug("thingRemoved thing '{}'", getThing().getUID());
        if (thingUID.equals(this.getThing().getUID())) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs) {
        logger.debug("removeOlderResults thing '{}' (ignored)", getThing().getUID());
        return null;
    }

}

class TivoStatusData {

    private boolean cmdOk;
    private Date time;
    private int channelNum;
    private String msg;

    public TivoStatusData(boolean cmdOk, int channelNum, String msg) {
        this.cmdOk = cmdOk;
        this.time = new Date();
        this.channelNum = channelNum;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "TivoStatusData [cmdOk=" + cmdOk + ", time=" + time + ", channelNum=" + channelNum + ", msg=" + msg
                + "]";
    }

    public boolean isCmdOk() {
        return cmdOk;
    }

    public void setCmdOk(boolean cmdOk) {
        this.cmdOk = cmdOk;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getChannelNum() {
        return channelNum;
    }

    public void setChannelNum(int channelNum) {
        this.channelNum = channelNum;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
