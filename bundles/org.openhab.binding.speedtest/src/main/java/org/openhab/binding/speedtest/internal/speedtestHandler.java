/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.speedtest.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.speedtest.internal.dto.ResultContainer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link speedtestHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Brian Homeyer - Initial contribution
 */
@NonNullByDefault
public class speedtestHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(speedtestHandler.class);
    private @Nullable speedtestConfiguration config;
    private Gson GSON = new Gson();
    private static Runtime rt = Runtime.getRuntime();
    private long pollingInterval = 1440;

    private @Nullable ScheduledFuture<?> pollingJob;
    public volatile boolean isRunning = false;

    public static final String[] SHELL_WINDOWS = new String[] { "cmd" };
    public static final String[] SHELL_NIX = new String[] { "sh", "bash", "zsh", "csh" };

    private String speedTestCommand = "";
    private static OS os = OS.NOT_SET;

    /**
     * Contains information about which operating system openHAB is running on.
     */
    public enum OS {
        WINDOWS,
        LINUX,
        MAC,
        SOLARIS,
        UNKNOWN,
        NOT_SET
    }

    public speedtestHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read only
    }

    @Override
    public void initialize() {
        config = getConfigAs(speedtestConfiguration.class);
        pollingInterval = config.refreshInterval;
        if (!config.execPath.isEmpty()) {
            speedTestCommand = config.execPath;
        } else {
            switch (getOperatingSystemType()) {
                case WINDOWS:
                    speedTestCommand = "";
                    break;
                case LINUX:
                case MAC:
                case SOLARIS:
                    speedTestCommand = "/usr/bin/speedtest";
                    break;
                default:
                    speedTestCommand = "";
            }
        }

        updateStatus(ThingStatus.UNKNOWN);

        if (!checkConfig(speedTestCommand)) { // check the config
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Speedtest Executable not found.   Please check configuration.");
            return;
        }
        if (!getSpeedTestVersion()) {
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        isRunning = true;
        onUpdate(); // Setup the scheduler
    }

    /**
     * This is called to start the refresh job and also to reset that refresh job when a config change is done.
     */
    private void onUpdate() {
        logger.debug("Polling Interval Set : {} ", pollingInterval);
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, pollingInterval, TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Speedtest Handler Thing");
        isRunning = false;
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * Called when this thing gets it's configuration changed.
     */
    @Override
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
    }

    /**
     * Polling event used to get speed data from speedtest
     */
    private Runnable pollingRunnable = () -> {
        try {
            getSpeed();
        } catch (Exception e) {
            logger.warn("An exception occurred while running Speedtest: '{}'", e.getMessage());
            updateStatus(ThingStatus.OFFLINE);
        }
    };

    /*
     * Gets the version information from speedtest, this is really for debug in the event they change things
     */
    private boolean getSpeedTestVersion() {
        String stOutput[] = executeCmd(speedTestCommand + " -V").split("\n");
        if (stOutput.length > 0) {
            if (stOutput[0].indexOf("Speedtest") > -1) {
                logger.debug("Speedtest Version : {}", stOutput[0]);
                return true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Speedtest version not recognized, Ookla version REQUIRED.  Please check configuration."
                                + stOutput[0]);
                return false;
            }
        }
        return false;
    }

    /*
     * Get the speedtest data and convert it from JSON and send it to update the channels.
     */
    private void getSpeed() {
        logger.debug("Getting Speed Measurement");
        String speedOutput = executeCmd(speedTestCommand + " -f json --accept-license");
        ResultContainer tmpCont = GSON.fromJson(speedOutput, ResultContainer.class);
        if (tmpCont != null) {
            updateChannels(tmpCont);
        }
    }

    /*
     * Update the channels
     */
    private void updateChannels(ResultContainer results) {
        logger.debug("Updating channels");
        String serverTxt = "";
        serverTxt += results.getServer().getName();
        serverTxt += " (" + results.getServer().getId().toString() + ") ";
        serverTxt += results.getServer().getLocation();
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.PING_JITTER),
                new DecimalType(results.getPing().getJitter().doubleValue()));
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.PING_LATENCY),
                new DecimalType(results.getPing().getLatency().doubleValue()));
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.DOWNLOAD_BANDWIDTH),
                new DecimalType(results.getDownload().getBandwidth().doubleValue() / 125000));
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.DOWNLOAD_BYTES),
                new DecimalType(results.getDownload().getBytes().intValue()));
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.DOWNLOAD_ELAPSED),
                new DecimalType(results.getDownload().getElapsed().doubleValue()));
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.UPLOAD_BANDWIDTH),
                new DecimalType(results.getUpload().getBandwidth().doubleValue() / 125000));
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.UPLOAD_BYTES),
                new DecimalType(results.getUpload().getBytes().intValue()));
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.UPLOAD_ELAPSED),
                new DecimalType(results.getUpload().getElapsed().intValue()));
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.INTERFACE_EXTERNALIP),
                new StringType(String.valueOf(results.getInterface().getExternalIp())));
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.INTERFACE_INTERNALIP),
                new StringType(String.valueOf(results.getInterface().getInternalIp())));
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.ISP),
                new StringType(String.valueOf(results.getIsp())));
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.RESULT_URL),
                new StringType(String.valueOf(results.getResult().getUrl())));
        updateState(new ChannelUID(getThing().getUID(), speedtestBindingConstants.SERVER),
                new StringType(String.valueOf(serverTxt)));

    }

    /*
     * Checks to make sure the executable for speedtest is valid
     */
    public boolean checkConfig(String execPath) {
        if (checkExecPath(execPath)) { // Check if entered path exists
            return true;
        }
        return false;
    }

    /*
     * Executes a given command and returns back the String data of stdout.
     */
    private String executeCmd(String commandLine) {
        int timeOut = 60000;
        String[] cmdArray;
        String[] shell;
        logger.debug("Passing to shell for parsing command.");
        switch (getOperatingSystemType()) {
            case WINDOWS:
                shell = SHELL_WINDOWS;
                logger.debug("OS: WINDOWS ({})", getOperatingSystemName());
                cmdArray = createCmdArray(shell, "/c", commandLine);
                break;
            case LINUX:
            case MAC:
            case SOLARIS:
                // assume sh is present, should all be POSIX-compliant
                shell = SHELL_NIX;
                logger.debug("OS: *NIX ({})", getOperatingSystemName());
                cmdArray = createCmdArray(shell, "-c", commandLine);
                break;
            default:
                logger.debug("OS: Unknown ({})", getOperatingSystemName());
                return "";
        }

        if (cmdArray.length == 0) {
            logger.debug("Empty command received, not executing");
            return "";
        }

        logger.debug("The command to be executed will be '{}'", Arrays.asList(cmdArray));

        Process proc;
        try {
            proc = rt.exec(cmdArray);
        } catch (Exception e) {
            logger.debug("An exception occurred while executing '{}' : '{}'", Arrays.asList(cmdArray), e.getMessage());
            return "";
        }

        StringBuilder outputBuilder = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();

        try (InputStreamReader isr = new InputStreamReader(proc.getInputStream());
                BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                outputBuilder.append(line).append("\n");
                logger.debug("Exec [{}]: '{}'", "OUTPUT", line);
            }
        } catch (IOException e) {
            logger.warn("An exception occurred while reading the stdout when executing '{}' : '{}'", commandLine,
                    e.getMessage());
        }

        try (InputStreamReader isr = new InputStreamReader(proc.getErrorStream());
                BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                // errorBuilder.append(line).append("\n");
                logger.debug("Exec [{}]: '{}'", "ERROR", line);
            }
        } catch (IOException e) {
            logger.warn("An exception occurred while reading the stderr when executing '{}' : '{}'", commandLine,
                    e.getMessage());
        }

        boolean exitVal = false;
        try {
            exitVal = proc.waitFor(timeOut, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.debug("An exception occurred while waiting for the process ('{}') to finish : '{}'", commandLine,
                    e.getMessage());
        }

        if (!exitVal) {
            logger.debug("Forcibly termininating the process ('{}') after a timeout of {} ms", commandLine, timeOut);
            proc.destroyForcibly();
        }

        outputBuilder.append(errorBuilder.toString());

        outputBuilder.append(errorBuilder.toString());
        return outputBuilder.toString();

    }

    /**
     * Transforms the command string into an array.
     * Either invokes the shell and passes using the "c" option
     * or (if command already starts with one of the shells) splits by space.
     *
     * @param shell (path), picks to first one to execute the command
     * @param cOption "c"-option string
     * @param commandLine to execute
     * @return command array
     */
    protected String[] createCmdArray(String[] shell, String cOption, String commandLine) {
        boolean startsWithShell = false;
        for (String sh : shell) {
            if (commandLine.startsWith(sh + " ")) {
                startsWithShell = true;
                break;
            }
        }

        if (!startsWithShell) {
            return new String[] { shell[0], cOption, commandLine };
        } else {
            logger.debug("Splitting by spaces");
            try {
                return commandLine.split(" ");
            } catch (PatternSyntaxException e) {
                logger.warn("An exception occurred while splitting '{}' : '{}'", commandLine, e.getMessage());
                return new String[] {};
            }
        }
    }

    public static OS getOperatingSystemType() {
        if (os == OS.NOT_SET) {
            String operSys = System.getProperty("os.name").toLowerCase();
            if (operSys.contains("win")) {
                os = OS.WINDOWS;
            } else if (operSys.contains("nix") || operSys.contains("nux") || operSys.contains("aix")) {
                os = OS.LINUX;
            } else if (operSys.contains("mac")) {
                os = OS.MAC;
            } else if (operSys.contains("sunos")) {
                os = OS.SOLARIS;
            } else {
                os = OS.UNKNOWN;
            }
        }
        return os;
    }

    public static String getOperatingSystemName() {
        String osname = System.getProperty("os.name");
        return osname != null ? osname : "unknown";
    }

    public boolean checkExecPath(String path) {
        File file = new File(path);
        if (isFileExists(file)) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isFileExists(File file) {
        return file.exists() && !file.isDirectory();
    }
}
