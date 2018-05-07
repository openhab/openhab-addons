/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.logreader.handler;

import static org.openhab.binding.logreader.LogReaderBindingConstants.*;

import java.util.Calendar;
import java.util.regex.PatternSyntaxException;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.logreader.internal.config.LogReaderConfiguration;
import org.openhab.binding.logreader.internal.filereader.api.FileReaderListener;
import org.openhab.binding.logreader.internal.filereader.api.LogFileReader;
import org.openhab.binding.logreader.internal.searchengine.SearchEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LogReaderHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Miika Jukka - Initial contribution
 * @author Pauli Anttila - Rewrite
 */
public class LogHandler extends BaseThingHandler implements FileReaderListener {
    private final Logger logger = LoggerFactory.getLogger(LogHandler.class);

    private LogReaderConfiguration configuration;

    private LogFileReader fileReader;

    private SearchEngine errorEngine;
    private SearchEngine warningEngine;
    private SearchEngine customEngine;

    public LogHandler(Thing thing, LogFileReader fileReader) {
        super(thing);
        this.fileReader = fileReader;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_ERRORS:
                updateChannel(channelUID, command, errorEngine);
                break;

            case CHANNEL_WARNINGS:
                updateChannel(channelUID, command, warningEngine);
                break;

            case CHANNEL_CUSTOMEVENTS:
                updateChannel(channelUID, command, customEngine);
                break;

            default:
                logger.debug("Unsupported command '{}' received for channel '{}'", command, channelUID);
        }
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(LogReaderConfiguration.class);

        configuration.filePath = configuration.filePath.replaceFirst("\\$\\{OPENHAB_LOGDIR\\}",
                System.getProperty("openhab.logdir"));

        logger.debug("Using configuration: {}", configuration);

        clearCounters();

        try {
            warningEngine = new SearchEngine(configuration.warningPatterns, configuration.warningBlacklistingPatterns);
            errorEngine = new SearchEngine(configuration.errorPatterns, configuration.errorBlacklistingPatterns);
            customEngine = new SearchEngine(configuration.customPatterns, configuration.customBlacklistingPatterns);

        } catch (PatternSyntaxException e) {
            logger.debug("Illegal search pattern syntax '{}'. ", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        logger.debug("Start file reader");

        try {
            fileReader.registerListener(this);
            fileReader.start(configuration.filePath, configuration.refreshRate, scheduler);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.debug("Exception occurred during initalization: {}. ", e.getMessage(), e);
            shutdown();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Stopping thing");
        shutdown();
    }

    private void updateChannel(ChannelUID channelUID, Command command, SearchEngine matcher) {
        if (command instanceof DecimalType) {
            matcher.setMatchCount(((DecimalType) command).longValue());
        } else if (command instanceof RefreshType) {
            updateState(channelUID.getId(), new DecimalType(matcher.getMatchCount()));
        } else {
            logger.debug("Unsupported command '{}' received for channel '{}'", command, channelUID);
        }
    }

    private void clearCounters() {
        if (errorEngine != null) {
            errorEngine.clearMatchCount();
        }
        if (warningEngine != null) {
            warningEngine.clearMatchCount();
        }
        if (customEngine != null) {
            customEngine.clearMatchCount();
        }
    }

    private void updateChannelIfLinked(String channelID, State state) {
        if (isLinked(channelID)) {
            updateState(channelID, state);
        }
    }

    private void shutdown() {
        logger.debug("Stop file reader");
        fileReader.unregisterListener(this);
        fileReader.stop();
    }

    @Override
    public void fileNotFound() {
        final String msg = String.format("Log file '{}' does not exist", configuration.filePath);
        logger.debug(msg);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, msg);
    }

    @Override
    public void fileRotated() {
        logger.debug("Log rotated");
        updateChannelIfLinked(CHANNEL_LOGROTATED, new DateTimeType(Calendar.getInstance()));
    }

    @Override
    public void handle(String line) {
        if (line == null) {
            return;
        }

        if (!(thing.getStatus() == ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE);
        }

        if (errorEngine.isMatching(line)) {
            updateChannelIfLinked(CHANNEL_ERRORS, new DecimalType(errorEngine.getMatchCount()));
            updateChannelIfLinked(CHANNEL_LASTERROR, new StringType(line));
            triggerChannel(CHANNEL_NEWERROR, line);
        }
        if (warningEngine.isMatching(line)) {
            updateChannelIfLinked(CHANNEL_WARNINGS, new DecimalType(warningEngine.getMatchCount()));
            updateChannelIfLinked(CHANNEL_LASTWARNING, new StringType(line));
            triggerChannel(CHANNEL_NEWWARNING, line);
        }
        if (customEngine.isMatching(line)) {
            updateChannelIfLinked(CHANNEL_CUSTOMEVENTS, new DecimalType(customEngine.getMatchCount()));
            updateChannelIfLinked(CHANNEL_LASTCUSTOMEVENT, new StringType(line));
            triggerChannel(CHANNEL_NEWCUSTOM, line);
        }
    }

    @Override
    public void handle(Exception ex) {
        final String msg = ex != null ? ex.getMessage() : "";
        logger.debug("Error while trying to read log file: {}. ", msg, ex);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, msg);
    }
}
