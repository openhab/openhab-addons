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
package org.openhab.binding.logreader.internal.handler;

import static org.openhab.binding.logreader.internal.LogReaderBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.logreader.internal.config.LogReaderConfiguration;
import org.openhab.binding.logreader.internal.filereader.api.FileReaderListener;
import org.openhab.binding.logreader.internal.filereader.api.LogFileReader;
import org.openhab.binding.logreader.internal.searchengine.SearchEngine;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LogHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Miika Jukka - Initial contribution
 * @author Pauli Anttila - Rewrite
 */
@NonNullByDefault
public class LogHandler extends BaseThingHandler implements FileReaderListener {
    private final Logger logger = LoggerFactory.getLogger(LogHandler.class);

    private final LogFileReader fileReader;

    private @NonNullByDefault({}) LogReaderConfiguration configuration;

    private @Nullable SearchEngine errorEngine;
    private @Nullable SearchEngine warningEngine;
    private @Nullable SearchEngine customEngine;

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
        String logDir = System.getProperty("openhab.logdir");
        if (logDir == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot determine system log directory.");
            return;
        }

        configuration = getConfigAs(LogReaderConfiguration.class);
        configuration.filePath = configuration.filePath.replaceFirst("\\$\\{OPENHAB_LOGDIR\\}", logDir);

        logger.debug("Using configuration: {}", configuration);

        clearCounters();

        try {
            warningEngine = new SearchEngine(configuration.warningPatterns, configuration.warningBlacklistingPatterns);
            errorEngine = new SearchEngine(configuration.errorPatterns, configuration.errorBlacklistingPatterns);
            String customPatterns = configuration.customPatterns;
            customEngine = new SearchEngine(customPatterns != null ? customPatterns : "",
                    configuration.customBlacklistingPatterns);
        } catch (PatternSyntaxException e) {
            logger.debug("Illegal search pattern syntax '{}'. ", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        logger.debug("Start file reader");

        try {
            fileReader.registerListener(this);
            fileReader.start(configuration.filePath, configuration.refreshRate);
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

    private void updateChannel(ChannelUID channelUID, Command command, @Nullable SearchEngine matcher) {
        if (matcher != null) {
            if (command instanceof DecimalType) {
                matcher.setMatchCount(((DecimalType) command).longValue());
            } else if (command instanceof RefreshType) {
                updateState(channelUID.getId(), new DecimalType(matcher.getMatchCount()));
            } else {
                logger.debug("Unsupported command '{}' received for channel '{}'", command, channelUID);
            }
        } else {
            logger.debug("Cannot update channel as SearchEngine is null.");
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
        final String msg = String.format("Log file '%s' does not exist", configuration.filePath);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, msg);
    }

    @Override
    public void fileRotated() {
        logger.debug("Log rotated");
        updateChannelIfLinked(CHANNEL_LOGROTATED, new DateTimeType(ZonedDateTime.now()));
    }

    @Override
    public void handle(@Nullable String line) {
        if (line == null) {
            return;
        }

        if (thing.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        if (errorEngine != null && errorEngine.isMatching(line)) {
            updateChannelIfLinked(CHANNEL_ERRORS, new DecimalType(errorEngine.getMatchCount()));
            updateChannelIfLinked(CHANNEL_LASTERROR, new StringType(line));
            triggerChannel(CHANNEL_NEWERROR, line);
        }
        if (warningEngine != null && warningEngine.isMatching(line)) {
            updateChannelIfLinked(CHANNEL_WARNINGS, new DecimalType(warningEngine.getMatchCount()));
            updateChannelIfLinked(CHANNEL_LASTWARNING, new StringType(line));
            triggerChannel(CHANNEL_NEWWARNING, line);
        }
        if (customEngine != null && customEngine.isMatching(line)) {
            updateChannelIfLinked(CHANNEL_CUSTOMEVENTS, new DecimalType(customEngine.getMatchCount()));
            updateChannelIfLinked(CHANNEL_LASTCUSTOMEVENT, new StringType(line));
            triggerChannel(CHANNEL_NEWCUSTOM, line);
        }
    }

    @Override
    public void handle(@Nullable Exception ex) {
        final String msg = ex != null ? ex.getMessage() : "";
        logger.debug("Error while trying to read log file: {}. ", msg, ex);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, msg);
    }
}
