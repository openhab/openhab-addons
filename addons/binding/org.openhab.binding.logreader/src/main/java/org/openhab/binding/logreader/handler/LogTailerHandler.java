/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.logreader.handler;

import static org.openhab.binding.logreader.LogReaderBindingConstants.*;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.binding.logreader.internal.tailer.Tailer;
import org.openhab.binding.logreader.internal.tailer.TailerListener;
import org.openhab.binding.logreader.internal.tailer.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LogReaderHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Miika Jukka - Initial contribution
 * @author Pauli Anttila - Completed implementation
 */
public class LogTailerHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(LogTailerHandler.class);

    private LogReaderConfiguration configuration;

    private ExecutorService listenerExecutor;

    private Tailer tailer;

    private Matcher warningMatcher;
    private Matcher errorMatcher;

    private long warningCount;
    private long errorCount;

    public LogTailerHandler(Thing thing) {
        super(thing);
    }

    TailerListener logListener = new TailerListenerAdapter() {

        @Override
        public void handle(@Nullable String line) {
            if (line == null) {
                return;
            }

            if (!thing.getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }

            warningMatcher.reset(line);

            if (warningMatcher.find()) {
                if (!isBlacklisted(configuration.warningBlacklistingPatterns, line)) {
                    warningCount++;
                    updateChannelIfLinked(CHANNEL_TAILER_WARNINGS, new DecimalType(warningCount));
                    updateChannelIfLinked(CHANNEL_TAILER_LASTWARNING, new StringType(line));
                    triggerChannel(CHANNEL_TAILER_NEWWARNING, line);
                }
            } else {
                errorMatcher.reset(line);
                if (errorMatcher.find()) {
                    if (!isBlacklisted(configuration.errorBlacklistingPatterns, line)) {
                        errorCount++;
                        updateChannelIfLinked(CHANNEL_TAILER_ERRORS, new DecimalType(errorCount));
                        updateChannelIfLinked(CHANNEL_TAILER_LASTERROR, new StringType(line));
                        triggerChannel(CHANNEL_TAILER_NEWERROR, line);
                    }
                }
            }
        }

        @Override
        public void fileNotFound() {
            final String msg = String.format("Log file '%s' does not exist", configuration.filePath);
            logger.debug(msg);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, msg);
        }

        @Override
        public void handle(@Nullable Exception e) {
            final String msg = e != null ? e.getMessage() : "";
            logger.debug("Error while trying to read log file: {}. ", msg, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, msg);
        }

        @Override
        public void fileRotated() {
            logger.debug("Log rotated");
            updateChannelIfLinked(CHANNEL_TAILER_LOGROTATED, new DateTimeType(Calendar.getInstance()));
        }
    };

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_TAILER_ERRORS:
                if (command instanceof DecimalType) {
                    errorCount = ((DecimalType) command).longValue();
                } else if (command instanceof RefreshType) {
                    updateState(CHANNEL_TAILER_ERRORS, new DecimalType(errorCount));
                } else {
                    logger.debug("Unsupported command '{}' received for channel '{}'", command, channelUID);
                }
                break;

            case CHANNEL_TAILER_WARNINGS:
                if (command instanceof DecimalType) {
                    warningCount = ((DecimalType) command).longValue();
                } else if (command instanceof RefreshType) {
                    updateState(CHANNEL_TAILER_WARNINGS, new DecimalType(warningCount));
                } else {
                    logger.debug("Unsupported command '{}' received for channel '{}'", command, channelUID);
                }
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

        logger.info("Using configuration: {}", configuration.toString());

        warningCount = 0;
        errorCount = 0;

        try {
            Pattern warningPattern = Pattern.compile(configuration.warningPattern);
            Pattern errorPattern = Pattern.compile(configuration.errorPattern);
            warningMatcher = warningPattern.matcher("");
            errorMatcher = errorPattern.matcher("");
        } catch (PatternSyntaxException e) {
            logger.debug("Illegal search pattern syntax '{}'. ", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        tailer = new Tailer(new File(configuration.filePath), logListener, configuration.refreshRate, true, false,
                true);

        try {
            logger.debug("Start executor");
            listenerExecutor = Executors.newCachedThreadPool();
            listenerExecutor.execute(tailer);
            updateStatus(ThingStatus.ONLINE);
        } catch (Throwable e) {
            logger.debug("Exception occurred during initalization: {}. ", e.getMessage(), e);
            shutdownExecutor();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Stopping thing");
        shutdownExecutor();
    }

    private void updateChannelIfLinked(String channelID, State state) {
        if (isLinked(channelID)) {
            updateState(channelID, state);
        }
    }

    private void shutdownExecutor() {
        logger.debug("Shutdown listenerExecutor");
        tailer.stop();
        listenerExecutor.shutdown();
        try {
            if (!listenerExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                logger.debug("Forcing listenerExecutor shutdown");
            }
        } catch (InterruptedException ex) {
            // nothing to do
        } finally {
            if (!listenerExecutor.isShutdown()) {
                listenerExecutor.shutdownNow();
            }
        }
        logger.debug("listenerExecutor shutdown");
    }

    private boolean isBlacklisted(@Nullable String patterns, String line) {
        if (patterns != null) {
            String list[] = patterns.split("\\|");
            if (list.length > 0) {
                for (String patternStr : list) {
                    try {
                        Pattern pattern = Pattern.compile(patternStr);
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            // logger.debug("Log line blacklisted by '{}'", patternStr);
                            return true;
                        }
                    } catch (PatternSyntaxException e) {
                        logger.debug("Illegal search pattern syntax '{}'. ", e.getMessage(), e);
                    }
                }
            }
        }
        return false;
    }
}
