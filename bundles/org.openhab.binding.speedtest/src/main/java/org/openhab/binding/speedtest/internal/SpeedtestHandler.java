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
package org.openhab.binding.speedtest.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.speedtest.internal.dto.ResultContainer;
import org.openhab.binding.speedtest.internal.dto.ResultsContainerServerList;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
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
import org.openhab.core.types.UnDefType;
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
    private Gson gson = new Gson();
    private static Runtime rt = Runtime.getRuntime();
    private long pollingInterval = 60;
    private String serverID = "";
    private final TimeZoneProvider timeZoneProvider;

    private @Nullable ScheduledFuture<?> pollingJob;
    public volatile boolean isRunning = false;

    public static final String[] SHELL_WINDOWS = new String[] { "cmd" };
    public static final String[] SHELL_NIX = new String[] { "sh", "bash", "zsh", "csh" };

    private String speedTestCommand = "";
    private static volatile OS os = OS.NOT_SET;
    private static final Object LOCK = new Object();

    private State pingJitter = UnDefType.NULL;
    private State pingLatency = UnDefType.NULL;
    private State downloadBandwidth = UnDefType.NULL;
    private State downloadBytes = UnDefType.NULL;
    private State downloadElapsed = UnDefType.NULL;
    private State uploadBandwidth = UnDefType.NULL;
    private State uploadBytes = UnDefType.NULL;
    private State uploadElapsed = UnDefType.NULL;
    private String isp = "";
    private String interfaceInternalIp = "";
    private String interfaceExternalIp = "";
    private String resultUrl = "";
    private State resultImage = UnDefType.NULL;
    private String server = "";
    private State timestamp = UnDefType.NULL;

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

    public SpeedtestHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand channel: {} command: {}", channelUID, command);
        String ch = channelUID.getId();
        if (command instanceof RefreshType) {
            if (!server.isBlank()) {
                updateChannels();
            }
            return;
        }
        if (ch.equals(SpeedtestBindingConstants.TRIGGER_TEST)) {
            if (command instanceof OnOffType) {
                if (command == OnOffType.ON) {
                    getSpeed();
                    updateState(channelUID, OnOffType.OFF);
                }
            }
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
        logger.debug("Polling Interval Set: {}", pollingInterval);
        if (pollingInterval > 0) {
            ScheduledFuture<?> pollingJob = this.pollingJob;
            if (pollingJob == null || pollingJob.isCancelled()) {
                this.pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, pollingInterval,
                        TimeUnit.MINUTES);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Speedtest Handler Thing");
        isRunning = false;
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
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

    /**
     * Gets the version information from speedtest, this is really for debug in the event they change things
     */
    private boolean getSpeedTestVersion() {
        String versionString = doExecuteRequest(" -V", String.class);
        if ((versionString != null) && !versionString.isEmpty()) {
            int newLI = versionString.indexOf(System.lineSeparator());
            String versionLine = versionString.substring(0, newLI);
            if (versionString.contains("Speedtest by Ookla")) {
                logger.debug("Speedtest Version: {}", versionLine);
                return true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.configuration-error.type");
                return false;
            }
        }
        return false;
    }

    /**
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

    /**
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
            if ("result".equals(tmpCont.getType())) {
                try {
                    // timestamp format: "2023-07-20T19:34:54Z"
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(tmpCont.getTimestamp())
                            .withZoneSameInstant(timeZoneProvider.getTimeZone());
                    timestamp = new DateTimeType(zonedDateTime);
                } catch (DateTimeParseException e) {
                    timestamp = UnDefType.NULL;
                    logger.debug("Exception: {}", e.getMessage());
                }
                try {
                    pingJitter = new QuantityType<>(Double.parseDouble(tmpCont.getPing().getJitter()) / 1000.0,
                            Units.SECOND);
                } catch (NumberFormatException e) {
                    pingJitter = UnDefType.NULL;
                    logger.debug("Exception: {}", e.getMessage());
                }
                try {
                    pingLatency = new QuantityType<>(Double.parseDouble(tmpCont.getPing().getLatency()) / 1000.0,
                            Units.SECOND);
                } catch (NumberFormatException e) {
                    pingLatency = UnDefType.NULL;
                    logger.debug("Exception: {}", e.getMessage());
                }
                try {
                    downloadBandwidth = new QuantityType<>(
                            Double.parseDouble(tmpCont.getDownload().getBandwidth()) / 125000.0,
                            Units.MEGABIT_PER_SECOND);
                } catch (NumberFormatException e) {
                    downloadBandwidth = UnDefType.NULL;
                    logger.debug("Exception: {}", e.getMessage());
                }
                try {
                    downloadBytes = new QuantityType<>(Double.parseDouble(tmpCont.getDownload().getBytes()),
                            Units.BYTE);
                } catch (NumberFormatException e) {
                    downloadBytes = UnDefType.NULL;
                    logger.debug("Exception: {}", e.getMessage());
                }
                try {
                    downloadElapsed = new QuantityType<>(
                            Double.parseDouble(tmpCont.getDownload().getElapsed()) / 1000.0, Units.SECOND);
                } catch (NumberFormatException e) {
                    downloadElapsed = UnDefType.NULL;
                    logger.debug("Exception: {}", e.getMessage());
                }
                try {
                    uploadBandwidth = new QuantityType<>(
                            Double.parseDouble(tmpCont.getUpload().getBandwidth()) / 125000.0,
                            Units.MEGABIT_PER_SECOND);
                } catch (NumberFormatException e) {
                    uploadBandwidth = UnDefType.NULL;
                    logger.debug("Exception: {}", e.getMessage());
                }
                try {
                    uploadBytes = new QuantityType<>(Double.parseDouble(tmpCont.getUpload().getBytes()), Units.BYTE);
                } catch (NumberFormatException e) {
                    uploadBytes = UnDefType.NULL;
                    logger.debug("Exception: {}", e.getMessage());
                }
                try {
                    uploadElapsed = new QuantityType<>(Double.parseDouble(tmpCont.getUpload().getElapsed()) / 1000.0,
                            Units.SECOND);
                } catch (NumberFormatException e) {
                    uploadElapsed = UnDefType.NULL;
                    logger.debug("Exception: {}", e.getMessage());
                }
                isp = tmpCont.getIsp();
                interfaceInternalIp = tmpCont.getInterface().getInternalIp();
                interfaceExternalIp = tmpCont.getInterface().getExternalIp();
                resultUrl = tmpCont.getResult().getUrl();
                String url = String.valueOf(resultUrl) + ".png";
                logger.debug("Downloading result image from: {}", url);
                RawType image = HttpUtil.downloadImage(url);
                if (image != null) {
                    resultImage = image;
                } else {
                    resultImage = UnDefType.NULL;
                }

                server = tmpCont.getServer().getName() + " (" + tmpCont.getServer().getId().toString() + ") "
                        + tmpCont.getServer().getLocation();
                updateChannels();
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.results");
        }
    }

    private @Nullable <T> T doExecuteRequest(String arguments, Class<T> type) {
        try {
            String dataOut = executeCmd(speedTestCommand + arguments);
            if (type != String.class) {
                @Nullable
                T obj = gson.fromJson(dataOut, type);
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

    /**
     * Update the channels
     */
    private void updateChannels() {
        logger.debug("Updating channels");

        logger.debug("timestamp: {}", timestamp);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.TIMESTAMP), timestamp);

        logger.debug("pingJitter: {}", pingJitter);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.PING_JITTER), pingJitter);

        logger.debug("pingLatency: {}", pingLatency);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.PING_LATENCY), pingLatency);

        logger.debug("downloadBandwidth: {}", downloadBandwidth);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.DOWNLOAD_BANDWIDTH),
                downloadBandwidth);

        logger.debug("downloadBytes: {}", downloadBytes);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.DOWNLOAD_BYTES), downloadBytes);

        logger.debug("downloadElapsed: {}", downloadElapsed);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.DOWNLOAD_ELAPSED), downloadElapsed);

        logger.debug("uploadBandwidth: {}", uploadBandwidth);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.UPLOAD_BANDWIDTH), uploadBandwidth);

        logger.debug("uploadBytes: {}", uploadBytes);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.UPLOAD_BYTES), uploadBytes);

        logger.debug("uploadElapsed: {}", uploadElapsed);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.UPLOAD_ELAPSED), uploadElapsed);

        logger.debug("interfaceExternalIp: {}", interfaceExternalIp);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.INTERFACE_EXTERNALIP),
                new StringType(interfaceExternalIp));

        logger.debug("interfaceInternalIp: {}", interfaceInternalIp);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.INTERFACE_INTERNALIP),
                new StringType(interfaceInternalIp));

        logger.debug("isp: {}", isp);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.ISP), new StringType(isp));

        logger.debug("resultUrl: {}", resultUrl);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.RESULT_URL),
                new StringType(resultUrl));

        logger.debug("resultImage: <RawType>");
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.RESULT_IMAGE), resultImage);

        logger.debug("server: {}", server);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.SERVER), new StringType(server));
    }

    /**
     * Checks to make sure the executable for speedtest is valid
     */
    public boolean checkConfig(String execPath) {
        File file = new File(execPath);
        if (!checkFileExists(file)) { // Check if entered path exists
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.file");
            return false;
        }

        if (!checkFileExecutable(file)) { // Check if speedtest is executable
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.exec");
            return false;
        }
        return true;
    }

    /**
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
                outputBuilder.append(line).append(System.lineSeparator());
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
                return commandLine.split(" ");
            } catch (PatternSyntaxException e) {
                logger.warn("An exception occurred while splitting '{}': '{}'", commandLine, e.getMessage());
                return new String[] {};
            }
        }
    }

    public static OS getOperatingSystemType() {
        synchronized (LOCK) {
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
        }
        return os;
    }

    public static String getOperatingSystemName() {
        String osname = System.getProperty("os.name");
        return osname != null ? osname : "unknown";
    }

    public boolean checkFileExists(File file) {
        return file.exists() && !file.isDirectory();
    }

    public boolean checkFileExecutable(File file) {
        return file.canExecute();
    }
}
