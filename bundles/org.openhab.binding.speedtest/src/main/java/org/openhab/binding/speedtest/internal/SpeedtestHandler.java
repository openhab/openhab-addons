/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.speedtest.internal.dto.ResultContainer;
import org.openhab.binding.speedtest.internal.dto.ResultsContainerServerList;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
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

import com.google.gson.Gson;

/**
 * The {@link SpeedtestHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Brian Homeyer - Initial contribution
 */
@NonNullByDefault
public class SpeedtestHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SpeedtestHandler.class);
    private SpeedtestConfiguration config = new SpeedtestConfiguration();
    private Gson GSON = new Gson();
    private static Runtime rt = Runtime.getRuntime();
    private long pollingInterval = 1440;
    private String serverID = "";

    private @Nullable ScheduledFuture<?> pollingJob;
    public volatile boolean isRunning = false;

    public static final String[] SHELL_WINDOWS = new String[] { "cmd" };
    public static final String[] SHELL_NIX = new String[] { "sh", "bash", "zsh", "csh" };

    private String speedTestCommand = "";
    private static OS os = OS.NOT_SET;

    private String ping_jitter = "";
    private String ping_latency = "";
    private String download_bandwidth = "";
    private String download_bytes = "";
    private String download_elapsed = "";
    private String upload_bandwidth = "";
    private String upload_bytes = "";
    private String upload_elapsed = "";
    private String isp = "";
    private String interface_internalIp = "";
    private String interface_externalIp = "";
    private String result_url = "";
    private String server_name = "";

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

    public SpeedtestHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand channel: {} command: {}", channelUID, command);
        String ch = channelUID.getId();
        if (command instanceof RefreshType) {
            if (!server_name.isBlank()) {
                updateChannels();
            }
            return;
        }
        if (ch.equals(SpeedtestBindingConstants.TRIGGER_TEST)) {
            getSpeed();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SpeedtestConfiguration.class);
        pollingInterval = config.refreshInterval;
        serverID = config.serverID;
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
                    "Speedtest executable not found or not executable. Please check configuration.");
            return;
        }
        if (!getSpeedTestVersion()) {
            return;
        }
        getServerList();
        updateStatus(ThingStatus.ONLINE);
        isRunning = true;
        onUpdate(); // Setup the scheduler
    }

    /**
     * This is called to start the refresh job and also to reset that refresh job when a config change is done.
     */
    private void onUpdate() {
        logger.debug("Polling Interval Set: {} ", pollingInterval);
        if (pollingInterval > 0) {
            if (pollingJob == null || pollingJob.isCancelled()) {
                pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, pollingInterval, TimeUnit.MINUTES);
            }
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
        String versionString = doExecuteRequest(" -V", String.class);
        if ((versionString != null) && !versionString.isEmpty()) {
            int newLI = versionString.indexOf("\n");
            String versionLine = versionString.substring(0, newLI);
            if (versionString.indexOf("Speedtest by Ookla") > -1) {
                logger.debug("Speedtest Version: {}", versionLine);
                return true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Unsupported type of Speedtest recognized, Speedtest CLI tool from Ookla is REQUIRED (https://www.speedtest.net/). Please check installation/configuration.");
                return false;
            }
        }
        return false;
    }

    /*
     * Get the server list from the speedtest command. Update the properties of the thing so the user
     * can see the list of servers closest to them.
     */
    private boolean getServerList() {
        String serverListTxt = "";
        ResultsContainerServerList tmpCont = doExecuteRequest(" -f json -L", ResultsContainerServerList.class);
        if (tmpCont != null) {
            int id = 1;
            Map<String, String> properties = editProperties();
            for (ResultsContainerServerList.Server server : tmpCont.servers) {
                serverListTxt = "ID: " + server.id.toString() + ", " + server.host + " (" + server.location + ")";
                switch (id) {
                    case 1:
                        properties.replace(SpeedtestBindingConstants.PROPERTY_SERVER_LIST1, serverListTxt);
                        break;
                    case 2:
                        properties.replace(SpeedtestBindingConstants.PROPERTY_SERVER_LIST2, serverListTxt);
                        break;
                    case 3:
                        properties.replace(SpeedtestBindingConstants.PROPERTY_SERVER_LIST3, serverListTxt);
                        break;
                    case 4:
                        properties.replace(SpeedtestBindingConstants.PROPERTY_SERVER_LIST4, serverListTxt);
                        break;
                    case 5:
                        properties.replace(SpeedtestBindingConstants.PROPERTY_SERVER_LIST5, serverListTxt);
                        break;
                    case 6:
                        properties.replace(SpeedtestBindingConstants.PROPERTY_SERVER_LIST6, serverListTxt);
                        break;
                    case 7:
                        properties.replace(SpeedtestBindingConstants.PROPERTY_SERVER_LIST7, serverListTxt);
                        break;
                    case 8:
                        properties.replace(SpeedtestBindingConstants.PROPERTY_SERVER_LIST8, serverListTxt);
                        break;
                    case 9:
                        properties.replace(SpeedtestBindingConstants.PROPERTY_SERVER_LIST9, serverListTxt);
                        break;
                    case 10:
                        properties.replace(SpeedtestBindingConstants.PROPERTY_SERVER_LIST10, serverListTxt);
                        break;
                }
                id++;
            }
            updateProperties(properties);
        }
        return false;
    }

    /*
     * Get the speedtest data and convert it from JSON and send it to update the channels.
     */
    private void getSpeed() {
        logger.debug("Getting Speed Measurement");
        String postCommand = "";
        if (!serverID.isBlank()) {
            postCommand = " -s " + serverID;
        }
        ResultContainer tmpCont = doExecuteRequest(" -f json --accept-license --accept-gdpr" + postCommand,
                ResultContainer.class);
        if (tmpCont != null) {
            if (tmpCont.getType().equals("result")) {
                ping_jitter = tmpCont.getPing().getJitter();
                ping_latency = tmpCont.getPing().getLatency();
                download_bandwidth = tmpCont.getDownload().getBandwidth();
                download_bytes = tmpCont.getDownload().getBytes();
                download_elapsed = tmpCont.getDownload().getElapsed();
                upload_bandwidth = tmpCont.getUpload().getBandwidth();
                upload_bytes = tmpCont.getUpload().getBytes();
                upload_elapsed = tmpCont.getUpload().getElapsed();
                isp = tmpCont.getIsp();
                interface_internalIp = tmpCont.getInterface().getInternalIp();
                interface_externalIp = tmpCont.getInterface().getExternalIp();
                result_url = tmpCont.getResult().getUrl();
                server_name = tmpCont.getServer().getName() + " (" + tmpCont.getServer().getId().toString() + ") "
                        + tmpCont.getServer().getLocation();
                updateChannels();
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Speedtest is not returning valid results.");
        }
    }

    private @Nullable <T> T doExecuteRequest(String arguments, Class<T> type) {
        try {
            String dataOut = executeCmd(speedTestCommand + arguments);
            if (type != String.class) {
                @Nullable
                T obj = GSON.fromJson(dataOut, type);
                return obj;
            } else {
                @SuppressWarnings("unchecked")
                T obj = (T) dataOut;
                return obj;
            }
        } catch (Exception e) {
            logger.debug("Exception: {}", e.getMessage());
        }
        return null;
    }

    /*
     * Update the channels
     */
    private void updateChannels() {
        logger.debug("Updating channels");

        State newState = new QuantityType<>(Double.parseDouble(ping_jitter) / 1000.0, Units.SECOND);
        logger.debug("ping_jitter: {}", newState);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.PING_JITTER), newState);

        newState = new QuantityType<>(Double.parseDouble(ping_latency) / 1000.0, Units.SECOND);
        logger.debug("ping_latency: {}", newState);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.PING_LATENCY), newState);

        newState = new QuantityType<>(Double.parseDouble(download_bandwidth) / 125000.0, Units.MEGABIT_PER_SECOND);
        logger.debug("download_bandwidth: {}", newState);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.DOWNLOAD_BANDWIDTH), newState);

        newState = new QuantityType<>(Double.parseDouble(download_bytes), Units.BYTE);
        logger.debug("download_bytes: {}", newState);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.DOWNLOAD_BYTES), newState);

        newState = new QuantityType<>(Double.parseDouble(download_elapsed) / 1000.0, Units.SECOND);
        logger.debug("download_elapsed: {}", newState);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.DOWNLOAD_ELAPSED), newState);

        newState = new QuantityType<>(Double.parseDouble(upload_bandwidth) / 125000.0, Units.MEGABIT_PER_SECOND);
        logger.debug("upload_bandwidth: {}", newState);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.UPLOAD_BANDWIDTH), newState);

        newState = new QuantityType<>(Double.parseDouble(upload_bytes), Units.BYTE);
        logger.debug("upload_bytes: {}", newState);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.UPLOAD_BYTES), newState);

        newState = new QuantityType<>(Double.parseDouble(upload_elapsed) / 1000.0, Units.SECOND);
        logger.debug("upload_elapsed: {}", newState);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.UPLOAD_ELAPSED), newState);

        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.INTERFACE_EXTERNALIP),
                new StringType(String.valueOf(interface_externalIp)));
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.INTERFACE_INTERNALIP),
                new StringType(String.valueOf(interface_internalIp)));
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.ISP),
                new StringType(String.valueOf(isp)));
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.RESULT_URL),
                new StringType(String.valueOf(result_url)));
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.SERVER),
                new StringType(String.valueOf(server_name)));
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
            logger.debug("An exception occurred while executing '{}': '{}'", Arrays.asList(cmdArray), e.getMessage());
            return "";
        }

        StringBuilder outputBuilder = new StringBuilder();
        try (InputStreamReader isr = new InputStreamReader(proc.getInputStream());
                BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                outputBuilder.append(line).append("\n");
                logger.debug("Exec [{}]: '{}'", "OUTPUT", line);
            }
        } catch (IOException e) {
            logger.warn("An exception occurred while reading the stdout when executing '{}': '{}'", commandLine,
                    e.getMessage());
        }

        boolean exitVal = false;
        try {
            exitVal = proc.waitFor(timeOut, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.debug("An exception occurred while waiting for the process ('{}') to finish: '{}'", commandLine,
                    e.getMessage());
        }

        if (!exitVal) {
            logger.debug("Forcibly termininating the process ('{}') after a timeout of {} ms", commandLine, timeOut);
            proc.destroyForcibly();
        }
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
                String[] splitCmd = commandLine.split(" ");
                return splitCmd;
            } catch (PatternSyntaxException e) {
                logger.warn("An exception occurred while splitting '{}': '{}'", commandLine, e.getMessage());
                return new String[] {};
            }
        }
    }

    public static OS getOperatingSystemType() {
        if (os == OS.NOT_SET) {
            String operSys = System.getProperty("os.name");
            if (operSys == null) {
                os = OS.UNKNOWN;
            } else {
                operSys = operSys.toLowerCase();

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
        }
        return os;
    }

    public static String getOperatingSystemName() {
        String osname = System.getProperty("os.name");
        return osname != null ? osname : "unknown";
    }

    public boolean checkExecPath(String path) {
        File file = new File(path);
        if (fileExistsAndIsExecutable(file)) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean fileExistsAndIsExecutable(File file) {
        return file.exists() && !file.isDirectory() && file.canExecute();
    }
}
