/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tivo.handler;

import static org.openhab.binding.tivo.TiVoBindingConstants.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.tivo.internal.service.TivoConfigData;
import org.openhab.binding.tivo.internal.service.TivoStatusData;
import org.openhab.binding.tivo.internal.service.TivoStatusData.ConnectionStatus;
import org.openhab.binding.tivo.internal.service.TivoStatusProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TiVoHandler} is the BaseThingHandler responsible for handling commands that are
 * sent to one of the Tivo's channels.
 *
 * @author Jayson Kubilis (DigitalBytes) - Initial contribution
 * @author Andrew Black (AndyXMB) - Updates / compilation corrections. Addition of channel scanning functionality.
 */

public class TiVoHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(TiVoHandler.class);
    private TivoConfigData tivoConfigData = null;
    private ConnectionStatus lastConnectionStatus = ConnectionStatus.UNKNOWN;
    private TivoStatusProvider tivoConnection = null;
    private ScheduledFuture<?> refreshJob = null;
    private ScheduledFuture<?> chScanJob = null;

    /**
     * Instantiates a new TiVo handler.
     *
     * @param thing the thing
     */
    public TiVoHandler(Thing thing) {
        super(thing);
        logger.debug("TiVoHandler '{}' - creating", getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        // Handles the commands from the various TiVo channel objects
        logger.debug("handleCommand '{}', parameter: {}", channelUID, command);

        if (!isInitialized()) {
            logger.debug("handleCommand '{}' device is not intialised yet, command '{}' will be ignored.",
                    getThing().getUID(), channelUID + " " + command);
            return;
        }

        if (command == null || tivoConnection == null) {
            return;
        }
        TivoStatusData currentStatus = tivoConnection.getServiceStatus();

        String commandKeyword = null;
        // Check to see if we are running a channel scan, if so 'disable' UI commands, else chaos ensues...
        if (currentStatus != null && currentStatus.isChannelScanInProgress()) {
            logger.warn("TiVo '{}' channel scan is in progress, command '{}' will be ignored.", getThing().getUID(),
                    channelUID + " " + command);
            return;
        }

        String commandParameters = command.toString().toUpperCase();
        if (command instanceof RefreshType) {
            // Future enhancement, if we can come up with a sensible set of actions when a REFRESH is issued
            logger.info("TiVo '{}' skipping REFRESH command for channel: '{}'.", getThing().getUID(),
                    channelUID.getId());
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_TIVO_CHANNEL_FORCE:
                commandKeyword = "FORCECH";
                break;
            case CHANNEL_TIVO_CHANNEL_SET:
                commandKeyword = "SETCH";
                break;
            case CHANNEL_TIVO_COMMAND:
                // Special case, user sends KEYWORD and PARAMETERS to Item
                commandKeyword = "";
                break;
            case CHANNEL_TIVO_TELEPORT:
                commandKeyword = "TELEPORT";
                break;
            case CHANNEL_TIVO_IRCMD:
                commandKeyword = "IRCODE";
                break;
            case CHANNEL_TIVO_KBDCMD:
                commandKeyword = "KEYBOARD";
                break;
        }
        sendCommand(commandKeyword, commandParameters, currentStatus);

    }

    private void sendCommand(String commandKeyword, String commandParameters, TivoStatusData currentStatus) {
        TivoStatusData commandResult = null;
        logger.debug("handleCommand '{}' - {} found!", getThing().getUID(), commandKeyword);
        // Re-write command keyword if we are in STANDBY, as only IRCODE TIVO will wake the unit from
        // standby mode
        if (tivoConnection.getServiceStatus().getConnectionStatus() == ConnectionStatus.STANDBY
                && commandKeyword.contentEquals("TELEPORT") && commandParameters.contentEquals("TIVO")) {
            commandKeyword = "IRCODE " + commandParameters;
            logger.debug("TiVo '{}' TELEPORT re-mapped to IRCODE as we are in standby: '{}'", getThing().getUID(),
                    commandKeyword);
        }
        // Execute command
        if (commandKeyword.contentEquals("FORCECH") || commandKeyword.contentEquals("SETCH")) {
            commandResult = chChannelChange(commandKeyword, commandParameters);
        } else {
            commandResult = tivoConnection.cmdTivoSend(commandKeyword + " " + commandParameters);
        }

        // Post processing
        if (commandParameters.contentEquals("STANDBY")) {
            // Force thing state into STANDBY as this command does not return a status when executed
            commandResult.setConnectionStatus(ConnectionStatus.STANDBY);
        }

        // Push status updates
        if (commandResult != null && commandResult.isCmdOk()) {
            updateTivoStatus(currentStatus, commandResult);
        }

        // return commandResult;
    }

    int convertValueToInt(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof String) {
            return Integer.valueOf((String) value);
        }
        if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        return (Integer) value;
    }

    boolean convertValueToBoolean(Object value) {
        return value instanceof Boolean ? ((Boolean) value) : Boolean.valueOf((String) value);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing a TiVo '{}' with config options", getThing().getUID());

        // tivoConfigData = getConfigAs(TivoConfigData.class);
        // logger.info("Using configuration: {}", tivoConfigData);

        Configuration conf = this.getConfig();
        TivoConfigData tivoConfig = new TivoConfigData();

        Object value;
        value = conf.get(CONFIG_ADDRESS);
        if (value != null) {
            tivoConfig.setCfgHost(String.valueOf(value));
        }

        value = conf.get(CONFIG_PORT);
        if (value != null) {
            tivoConfig.setCfgTcpPort(convertValueToInt(value));
        }

        value = conf.get(CONFIG_CONNECTION_RETRY);
        if (value != null) {
            tivoConfig.setCfgNumConnRetry(convertValueToInt(value));
        }

        value = conf.get(CONFIG_POLL_INTERVAL);
        if (value != null) {
            tivoConfig.setCfgPollInterval(convertValueToInt(value));
        }

        value = conf.get(CONFIG_POLL_FOR_CHANGES);
        if (value != null) {
            tivoConfig.setCfgPollChanges(convertValueToBoolean(value));
        }

        value = conf.get(CONFIG_KEEP_CONNECTION_OPEN);
        if (value != null) {
            tivoConfig.setCfgKeepConnOpen(convertValueToBoolean(value));
        }

        value = conf.get(CONFIG_CMD_WAIT_INTERVAL);
        if (value != null) {
            tivoConfig.setCfgCmdWait(convertValueToInt(value));
        }

        value = conf.get(CONFIG_CH_START);
        if (value != null) {
            tivoConfig.setCfgMinChannel(convertValueToInt(value));
        }

        value = conf.get(CONFIG_CH_END);
        if (value != null) {
            tivoConfig.setCfgMaxChannel(convertValueToInt(value));
        }

        value = conf.get(CONFIG_IGNORE_SCAN);
        if (value != null) {
            tivoConfig.setCfgIgnoreChannelScan(convertValueToBoolean(value));
        }

        value = getThing().getUID();
        if (value != null) {
            tivoConfig.setCfgIdentifier(String.valueOf(value));
        }

        value = conf.get(CONFIG_IGNORE_CHANNELS);
        if (value != null) {
            tivoConfig.setCfgIgnoreChannels(chParseIgnored(String.valueOf(conf.get(CONFIG_IGNORE_CHANNELS)),
                    tivoConfig.getCfgMinChannel(), tivoConfig.getCfgMaxChannel()));
        }

        tivoConfigData = tivoConfig;
        logger.debug("TivoConfigData Obj: '{}'", tivoConfigData);

        if (tivoConnection == null) {
            tivoConnection = new TivoStatusProvider(tivoConfigData, this);
        }

        // scheduler.execute(new Runnable() {
        //
        // @Override
        // public void run() {
        // logger.debug("Open connection to Onkyo Receiver @{}", getThing().getUID());

        if (tivoConfig.doChannelScan()) {
            startChannelScan();
        } else {
            startPollStatus();
            // }
            // };
        }

        updateStatus(ThingStatus.UNKNOWN);
        lastConnectionStatus = ConnectionStatus.UNKNOWN;
        logger.debug("Initializing a TiVo handler for thing '{}' - finished!", getThing().getUID());

    }

    @Override
    public void dispose() {
        logger.debug("Disposing of a TiVo handler for thing '{}'", getThing().getUID());

        if (tivoConnection != null) {
            tivoConnection.connTivoDisconnect(true);
        }

        if (refreshJob != null) {
            logger.warn("'{}' - Polling cancelled by dispose()", getThing().getUID());
            refreshJob.cancel(false);
        }
        if (chScanJob != null) {
            logger.warn("'{}' - Channel Scan cancelled by dispose()", getThing().getUID());
            chScanJob.cancel(false);
        }

        while (chScanJob != null && !chScanJob.isDone()) {
            try {
                TimeUnit.MILLISECONDS.sleep(tivoConfigData.getCfgCmdWait());
            } catch (InterruptedException e) {
                logger.debug("Disposing '{}' while waiting for 'channelScanJob' to end error: '{}' ",
                        getThing().getUID(), e.getMessage());
            }
        }

        tivoConnection = null;
    }

    /**
     * {@link startPollStatus} scheduled job to poll for changes in state.
     */
    private void startPollStatus() {
        int firstStartDelay = tivoConfigData.getCfgPollInterval();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                logger.debug("startPollStatus '{}' @ rate of '{}' seconds", getThing().getUID(),
                        tivoConfigData.getCfgPollInterval());
                tivoConnection.statusRefresh();
            }
        };

        if (tivoConfigData.isCfgKeepConnOpen()) {
            // Run once
            refreshJob = scheduler.schedule(runnable, firstStartDelay, TimeUnit.SECONDS);
            logger.info("Status collection '{}' will start in '{}' seconds.", getThing().getUID(), firstStartDelay);
        } else if (tivoConfigData.doPollChanges()) {
            // Run at intervals
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, firstStartDelay,
                    tivoConfigData.getCfgPollInterval(), TimeUnit.SECONDS);
            logger.info("Status polling '{}' will start in '{}' seconds.", getThing().getUID(), firstStartDelay);
        } else {
            // Just update the status now
            tivoConnection.statusRefresh();
        }
    }

    /**
     * {@link startChannelScan} starts a channel scan between the minimum and maximum channel numbers. Populates the
     * {@code cfgIgnoreChannels} list which improves the performance of channel changing operations.
     */
    private void startChannelScan() {
        int firstStartDelay = tivoConfigData.getCfgPollInterval();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int minCh = tivoConfigData.getCfgMinChannel();
                int maxCh = tivoConfigData.getCfgMaxChannel();
                TivoStatusData commandResult = tivoConnection.getServiceStatus();

                updateState(CHANNEL_TIVO_STATUS, new StringType("CHANNEL SCAN IN PROGRESS"));
                if (tivoConnection.connTivoConnect()) {
                    tivoConnection.setChScan(true);
                    // change to first channel number, this forces the channel scan to run from Min# to Max#
                    tivoConnection.cmdTivoSend("TELEPORT LIVETV");
                    tivoConnection.cmdTivoSend("SETCH " + minCh);

                    for (int i = minCh + 1; i <= maxCh;) {
                        if (chScanJob.isCancelled()) {
                            // job has been cancelled, so we need to exit
                            logger.warn("Channel Scan for '{}' has been cancelled by configuraition parameter change",
                                    getThing().getUID());
                            updateState(CHANNEL_TIVO_STATUS, new StringType("CHANNEL SCAN CANCELLED"));
                            break;
                        }
                        logger.info("Channel Scan for '{}' testing channel num: '{}'", getThing().getUID(), i);
                        commandResult = chChannelChange("SETCH", String.valueOf(i));
                        if (commandResult.getChannelNum() != -1) {
                            i = commandResult.getChannelNum() + 1;
                        } else {
                            i++;
                        }

                        if (i >= maxCh) {
                            logger.info(
                                    "Perform Channel Scan for thing '{}' has been completed successfully.  Normal operation will now commence.",
                                    getThing().getUID());
                            updateState(CHANNEL_TIVO_STATUS, new StringType("CHANNEL SCAN COMPLETE"));
                        }

                    }

                    tivoConnection.cmdTivoSend("SETCH " + minCh);

                } else {
                    logger.warn("Channel Scan for '{}' failed - unable to connect (offline)", getThing().getUID());
                    updateState(CHANNEL_TIVO_STATUS, new StringType("CHANNEL SCAN CANCELLED (OFFLINE)"));
                }
                Configuration conf = editConfiguration();
                conf.put(CONFIG_IGNORE_SCAN, false);
                updateConfiguration(conf);
                tivoConnection.setChScan(false);
                thingUpdated(getThing());
            }
        };
        chScanJob = scheduler.schedule(runnable, firstStartDelay, TimeUnit.SECONDS);
        logger.info("Channel Scanning job for thing '{}' will start in '{}' seconds.  TiVo will scan all channels!",
                getThing().getUID(), firstStartDelay);
    }

    /**
     * {@link chChannelChange} performs channel changing operations. Checks {@link chCheckIgnored} channel numbers to
     * improve performance, reads the response and adds any new invalid channels {@link chAddIgnored}. Calls
     * {@link chGetNext} to determine the direction of channel change.
     *
     * @param commandKeyword the TiVo command object.
     * @param command the command parameter.
     * @return int channel number.
     */
    private TivoStatusData chChannelChange(String commandKeyword, String command) {
        int chnl = tivoConfigData.getCfgMinChannel();
        TivoStatusData tmpStatus = tivoConnection.getServiceStatus();

        // compare this to the current channel and determine the "direction" (up or down)
        int numTries = 10;
        chnl = Integer.valueOf(command.toString()).intValue();
        try {
            // check for ignored channels execute, check and learn new ignored channels
            while (numTries > 0 && chnl > 0) {
                numTries--;

                while (chCheckIgnored(chnl) && chGetNext(chnl, tmpStatus) > 0) {
                    logger.info("chChannelChange '{}' skipping channel: '{}'", getThing().getUID(), chnl);
                    chnl = chGetNext(chnl, tmpStatus);
                }

                String tmpCommand = commandKeyword + " " + chnl;
                logger.debug("chChannelChange '{}' sending command to tivo: '{}'", getThing().getUID(), tmpCommand);

                // Attempt to execute the command on the tivo
                tivoConnection.cmdTivoSend(tmpCommand);
                try {
                    TimeUnit.MILLISECONDS.sleep(tivoConfigData.getCfgCmdWait() * 2);
                } catch (Exception e) {
                }
                tmpStatus = tivoConnection.getServiceStatus();

                // Check to see if the command was successful
                if (tmpStatus != null && tmpStatus.isCmdOk()) {
                    numTries = 0;
                    if (tmpStatus.getMsg().contains("CH_STATUS")) {
                        return tmpStatus;
                    }

                } else if (tmpStatus != null) {
                    logger.warn("TiVo'{}' set channel command failed '{}' with msg '{}'", getThing().getUID(),
                            tmpCommand, tmpStatus.getMsg());
                    switch (tmpStatus.getMsg()) {
                        case "CH_FAILED INVALID_CHANNEL":
                            chAddIgnored(chnl);
                            chnl = chGetNext(chnl, tmpStatus);
                            if (chnl > 0) {
                                logger.debug("chChannelChange '{}' retrying next channel '{}'", getThing().getUID(),
                                        chnl);
                            } else {
                                numTries = 0;
                            }
                        case "CH_FAILED NO_LIVE":
                            tmpStatus.setChannelNum(chnl);
                            return tmpStatus;
                        case "CH_FAILED REORDING":
                        case "NO_STATUS_DATA_RETURNED":
                            tmpStatus.setChannelNum(-1);
                            return tmpStatus;
                    }

                    logger.info("TiVo'{}' retrying next channel '{}'", getThing().getUID(), chnl);
                }

            }

        } catch (NumberFormatException e) {
            logger.error("TiVo'{}' unable to parse channel integer from CHANNEL_TIVO_CHANNEL: '{}'",
                    getThing().getUID(), command.toString());
        }
        return tmpStatus;
    }

    /**
     * {@link chParseIgnored} parses the channels to ignore and populates {@link TivoConfigData}
     * {@code cfgIgnoreChannels} object with a sorted list of the channel numbers to ignore.
     *
     * @param pChannels source channel list.
     * @param chMin minimum channel number.
     * @param chMax maximum channel number.
     * @return the sorted set
     */
    private SortedSet<Integer> chParseIgnored(String pChannels, Integer chMin, Integer chMax) {
        logger.debug("chParseIgnored '{}' called doCfgParseIgnoreChannel with list: '{}'", getThing().getUID(),
                pChannels);

        SortedSet<Integer> result = new TreeSet<Integer>();

        if (pChannels.equals("null") || pChannels.isEmpty()) {
            return result;
        }

        if (pChannels.contains("[") | pChannels.contains("]")) {
            pChannels = pChannels.replace("[", "");
            pChannels = pChannels.replace("]", "");
        }

        List<String> tmp = Arrays.asList(pChannels.split("\\s*,\\s*"));

        try {
            for (int i = 0; i < tmp.size(); i++) {
                // Determine if we have a range with a '-' in it.
                if (tmp.get(i).matches(".+-.+")) {
                    List<String> sTmp = Arrays.asList(tmp.get(i).split("-"));
                    if (sTmp != null && sTmp.size() == 2) {

                        Double ds = Double.valueOf(sTmp.get(0));
                        Integer is = Integer.valueOf(ds.intValue());

                        Double de = Double.valueOf(sTmp.get(1));
                        Integer ie = Integer.valueOf(de.intValue());

                        if (ie < is) {
                            ds = Double.valueOf(sTmp.get(1));
                            is = Integer.valueOf(ds.intValue());

                            de = Double.valueOf(sTmp.get(0));
                            ie = Integer.valueOf(de.intValue());
                        }
                        while (is <= ie) {
                            if (!result.contains(is)) {
                                result.add(is);
                            }
                            is++;
                        }
                    } else {
                        logger.warn(
                                " chParseIgnored '{}' - parser matched - on string but didn't have an expected size",
                                getThing().getUID());
                    }
                } else {

                    Double de = Double.valueOf(tmp.get(i));
                    Integer se = Integer.valueOf(de.intValue());

                    if (result.contains(se)) {
                        logger.debug(" chParseIgnored '{}' - element already in list - '{}'", se);
                    } else {
                        if (se > chMin && se < chMax) {
                            result.add(se);
                        } else {
                            result.remove(se);
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            logger.warn(
                    " chParseIgnored '{}' was unable to parse list of 'Channels to Ignore' from thing settings: {}, error '{}'",
                    getThing().getUID(), pChannels, e);
            return result;

        }

        logger.debug(" chParseIgnored '{}' result will be used as the channel ignore channel list: {}",
                getThing().getUID(), result);

        // Re-parse the list (if populated) to make this more manageable in the UI
        if (result.size() > 0) {
            Integer[] uiArr = result.toArray(new Integer[result.size()]);
            String uiResult;
            if (result.size() > 1) {
                uiResult = chParseRange(uiArr);
            } else {
                uiResult = uiArr[0].toString();
            }

            logger.debug(" chParseIgnored '{}' uiResult will be posted back to the consoles: {}", getThing().getUID(),
                    uiResult);

            Configuration conf = editConfiguration();
            conf.put(CONFIG_IGNORE_CHANNELS, uiResult);
            updateConfiguration(conf);
        }
        return result;
    }

    /**
     * {@link chParseRange} re-parses the channels to ignore in {@code cfgIgnoreChannels}. Replaces consecutive numbers
     * with a range to reduce size of list in UIs.
     *
     * @param Integer array of channel numbers
     * @return string list of channel numbers with consecutive numbers returned as ranges.
     */

    private String chParseRange(Integer[] nums) {
        StringBuilder sb = new StringBuilder();
        int rangeStart = nums[0];
        int previous = nums[0];
        int current;
        int expected = previous + 1;

        for (int i = 1; i < nums.length; i++) {
            current = nums[i];
            expected = previous + 1;
            if (current != expected || i == (nums.length - 1)) {
                if (current == rangeStart) {
                    sb.append(previous + ",");
                } else {
                    if (rangeStart != previous) {
                        if (i == nums.length - 1) {
                            sb.append(rangeStart + "-" + current);
                        } else {
                            sb.append(rangeStart + "-" + previous + ",");
                        }
                    } else {
                        if (i == nums.length - 1) {
                            sb.append(rangeStart + "," + current);
                        } else {
                            sb.append(rangeStart + ",");
                        }
                    }
                }
                rangeStart = current;
            }
            previous = current;
        }
        if (sb.length() > 1) {
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        return sb.toString();
    }

    /**
     * {@link updateTivoStatus} populates the items with the status / channel information.
     *
     * @param tivoStatusData the {@link TivoStatusData}
     */
    public void updateTivoStatus(TivoStatusData oldStatusData, TivoStatusData newStatusData) {
        if (newStatusData != null && !tivoConfigData.doChannelScan()) {
            // Update Item Status
            if (newStatusData.getPubToUI()) {
                if (oldStatusData == null || !(oldStatusData.getMsg().contentEquals(newStatusData.getMsg()))) {
                    updateState(CHANNEL_TIVO_STATUS, new StringType(newStatusData.getMsg()));
                }
                // If the cmd was successful, publish the channel channel numbers
                if (newStatusData.isCmdOk() && newStatusData.getChannelNum() != -1) {
                    if (oldStatusData == null || oldStatusData.getChannelNum() != newStatusData.getChannelNum()) {
                        updateState(CHANNEL_TIVO_CHANNEL_FORCE, new DecimalType(newStatusData.getChannelNum()));
                        updateState(CHANNEL_TIVO_CHANNEL_SET, new DecimalType(newStatusData.getChannelNum()));
                    }
                }

                // Now set the pubToUI flag to false, as we have already published this status
                if (isLinked(CHANNEL_TIVO_STATUS) || isLinked(CHANNEL_TIVO_CHANNEL_FORCE)
                        || isLinked(CHANNEL_TIVO_CHANNEL_SET)) {
                    newStatusData.setPubToUI(false);
                    tivoConnection.setServiceStatus(newStatusData);
                }
            } else {
                if (newStatusData.getMsg().contentEquals("COMMAND_TIMEOUT")) {
                    tivoConnection.connTivoDisconnect(false);
                }
            }
            // Update Thing status
            if (newStatusData.getConnectionStatus() != lastConnectionStatus) {
                switch (newStatusData.getConnectionStatus()) {
                    case OFFLINE:
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Power on device or check network configuration/connection.");
                        break;
                    case ONLINE:
                        updateStatus(ThingStatus.ONLINE);
                        break;
                    case STANDBY:
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                                "STANDBY MODE: Send command TIVO to Remote Control Button (IRCODE) item to wakeup.");
                        break;
                    case UNKNOWN:
                        updateStatus(ThingStatus.INITIALIZING);
                        break;
                }
                lastConnectionStatus = newStatusData.getConnectionStatus();
            }
        }
    }

    /**
     * {@link chAddIgnored} adds a channel number to the list of Ignored Channels.
     *
     * @param pChannel the channel number.
     */
    private void chAddIgnored(Integer pChannel) {

        // if we already see this channel as being ignored there is no reason to ignore it again.
        if (chCheckIgnored(pChannel)) {
            return;
        }

        logger.info("chAddIgnored '{}' Adding new ignored channel '{}'", getThing().getUID(), pChannel);
        tivoConfigData.addCfgIgnoreChannels(pChannel);

        // Re-parse the sorted set and publish to UI
        SortedSet<Integer> myConfig = tivoConfigData.getCfgIgnoreChannels();
        Integer[] uiArr = myConfig.toArray(new Integer[myConfig.size()]);
        String uiResult = chParseRange(uiArr);

        Configuration conf = editConfiguration();
        conf.put(CONFIG_IGNORE_CHANNELS, uiResult);
        updateConfiguration(conf);

    }

    /**
     * {@link chGetNext} gets the next channel number, depending on the direction of navigation (based on the current
     * channel vs new proposed number).
     *
     * @param pChannel the channel number.
     * @return the next channel number.
     */
    private int chGetNext(int pChannel, TivoStatusData tivoStatusData) {
        if (chCheckIgnored(pChannel)) {
            if (tivoStatusData != null && tivoStatusData.isCmdOk()) {
                // retry logic is allowed otherwise we only do the logic below once.

                if (tivoStatusData.getChannelNum() > pChannel) {
                    // we appear to be changing the channel DOWNWARD so we try to go down -1
                    if (pChannel < tivoConfigData.getCfgMinChannel()) {
                        pChannel = tivoConfigData.getCfgMinChannel();
                    } else {
                        pChannel--;
                    }
                } else if (tivoStatusData.getChannelNum() <= pChannel) {
                    // we appear to be changing the channel UPWARD so we try to go up +1
                    if (pChannel > tivoConfigData.getCfgMaxChannel()) {
                        pChannel = tivoConfigData.getCfgMaxChannel();
                    } else {
                        pChannel++;
                    }
                } else {
                    // either we are going to attempt to change a channel to less then 1 or
                    // its already on the same channel. we shouldn't retry this here.
                    return -1;
                }

            } else if (tivoStatusData != null) {
                pChannel++;
            }
        }
        logger.debug("chGetNext '{}' next proposed channel '{}'", getThing().getUID(), pChannel);
        return pChannel;

    }

    /**
     * {@link chCheckIgnored} checks if the passed TV channel number is contained within the list of stored
     * channels contained within {@link getCfgIgnoreChannels}.
     *
     * @param pChannel the TV channel number to test.
     * @return true= channel is contained within the list, false= channel number is not contained within the list.
     */
    private boolean chCheckIgnored(int pChannel) {
        if (tivoConfigData.getCfgIgnoreChannels() != null && tivoConfigData.getCfgIgnoreChannels().contains(pChannel)) {
            return true;
        } else {
            return false;
        }
    }
}
