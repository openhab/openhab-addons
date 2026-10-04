/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import com.google.gson.JsonParseException;

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
    private long pollingInterval = 60;
    private String serverID = "";
    private final TimeZoneProvider timeZoneProvider;

    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> initializationJob;
    public volatile boolean isRunning = false;
    private volatile int initId = 0;
    private @Nullable Integer speedTestRunningInitId;
    private final Object lifecycleLock = new Object();

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
            int currentInitId = initId;
            synchronized (lifecycleLock) {
                if (currentInitId != initId) {
                    return;
                }
                ResultSnapshot snapshot = snapshotCurrentResult();
                if (!snapshot.server.isBlank()) {
                    updateChannels(snapshot);
                }
            }
            return;
        }
        if (ch.equals(SpeedtestBindingConstants.TRIGGER_TEST)) {
            if (command instanceof OnOffType) {
                if (command == OnOffType.ON) {
                    int currentInitId = initId;
                    getSpeed(currentInitId);
                    synchronized (lifecycleLock) {
                        if (currentInitId == initId) {
                            updateState(channelUID, OnOffType.OFF);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {
        int currentInitId = initId;
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
        initializationJob = scheduler.schedule(() -> initializeAsync(currentInitId), 0, TimeUnit.MILLISECONDS);
    }

    private void initializeAsync(int currentInitId) {
        try {
            if (!getSpeedTestVersion(currentInitId) || !isCurrentInitId(currentInitId)) {
                return;
            }
            getServerList(currentInitId);
            synchronized (lifecycleLock) {
                if (currentInitId != initId) {
                    return;
                }
                updateStatus(ThingStatus.ONLINE);
                if (currentInitId != initId) {
                    return;
                }
                isRunning = true;
                onUpdate();
            }
        } catch (RuntimeException e) {
            logger.warn("An exception occurred while initializing Speedtest: '{}'", e.getMessage());
            updateStatusIfCurrent(currentInitId, ThingStatus.OFFLINE);
        }
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
        @Nullable
        ScheduledFuture<?> initializationJob;
        @Nullable
        ScheduledFuture<?> pollingJob;
        synchronized (lifecycleLock) {
            ++initId;
            isRunning = false;
            initializationJob = this.initializationJob;
            this.initializationJob = null;
            pollingJob = this.pollingJob;
            this.pollingJob = null;
        }
        if (initializationJob != null) {
            initializationJob.cancel(true);
        }
        if (pollingJob != null) {
            pollingJob.cancel(true);
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
        runSpeedTest();
    };

    void runSpeedTest() {
        int currentInitId = initId;
        try {
            getSpeed(currentInitId);
        } catch (RuntimeException e) {
            logger.warn("An exception occurred while running Speedtest: '{}'", e.getMessage());
            updateStatusIfCurrent(currentInitId, ThingStatus.OFFLINE);
        }
    };

    /**
     * Gets the version information from speedtest, this is really for debug in the event they change things
     */
    private boolean getSpeedTestVersion(int currentInitId) {
        String versionString = doExecuteRequest(" -V", String.class);
        if ((versionString != null) && !versionString.isEmpty()) {
            int newLI = versionString.indexOf(System.lineSeparator());
            String versionLine = versionString.substring(0, newLI);
            if (versionString.contains("Speedtest by Ookla")) {
                logger.debug("Speedtest Version: {}", versionLine);
                return true;
            } else {
                updateStatusIfCurrent(currentInitId, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
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
    private boolean getServerList(int currentInitId) {
        ResultsContainerServerList tmpCont = doExecuteRequest(" -f json -L", ResultsContainerServerList.class);
        if (tmpCont != null) {
            int id = 1;
            Map<String, String> properties = editProperties();
            for (ResultsContainerServerList.Server server : tmpCont.servers) {
                if (id > SpeedtestBindingConstants.SERVER_LIST_PROPERTY_COUNT) {
                    break;
                }
                String serverListTxt = "ID: " + server.id.toString() + ", " + server.host + " (" + server.location
                        + ")";
                properties.replace(SpeedtestBindingConstants.PROPERTY_SERVER_LIST_PREFIX + id, serverListTxt);
                id++;
            }
            synchronized (lifecycleLock) {
                if (currentInitId != initId) {
                    return false;
                }
                updateProperties(properties);
            }
        }
        return false;
    }

    /**
     * Get the speedtest data and convert it from JSON and send it to update the channels.
     */
    void getSpeed(int currentInitId) {
        synchronized (lifecycleLock) {
            if (currentInitId != initId) {
                return;
            }
            if (speedTestRunningInitId != null && speedTestRunningInitId == currentInitId) {
                logger.debug("Speed measurement already running");
                return;
            }
            speedTestRunningInitId = currentInitId;
        }

        try {
            getSpeedResult(currentInitId);
        } finally {
            synchronized (lifecycleLock) {
                if (speedTestRunningInitId != null && speedTestRunningInitId == currentInitId) {
                    speedTestRunningInitId = null;
                }
            }
        }
    }

    private void getSpeedResult(int currentInitId) {
        logger.debug("Getting Speed Measurement");
        String postCommand = "";
        if (!serverID.isBlank()) {
            postCommand = " -s " + serverID;
        }
        ResultContainer tmpCont = doExecuteRequest(" -f json --accept-license --accept-gdpr" + postCommand,
                ResultContainer.class);
        if (tmpCont == null) {
            updateStatusIfCurrent(currentInitId, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.results");
            return;
        }

        if ("result".equals(tmpCont.getType())) {
            ResultSnapshot snapshot = parseResult(tmpCont);
            synchronized (lifecycleLock) {
                if (currentInitId != initId) {
                    return;
                }
                applyResult(snapshot);
                updateChannels(snapshotCurrentResult());
            }
            if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatusIfCurrent(currentInitId, ThingStatus.ONLINE);
            }
        }
    }

    private void updateStatusIfCurrent(int currentInitId, ThingStatus status) {
        synchronized (lifecycleLock) {
            if (currentInitId == initId) {
                updateStatus(status);
            }
        }
    }

    private void updateStatusIfCurrent(int currentInitId, ThingStatus status, ThingStatusDetail statusDetail,
            String description) {
        synchronized (lifecycleLock) {
            if (currentInitId == initId) {
                updateStatus(status, statusDetail, description);
            }
        }
    }

    private boolean isCurrentInitId(int currentInitId) {
        synchronized (lifecycleLock) {
            return currentInitId == initId;
        }
    }

    private ResultSnapshot parseResult(ResultContainer tmpCont) {
        State newTimestamp;
        State newPingJitter;
        State newPingLatency;
        State newDownloadBandwidth;
        State newDownloadBytes;
        State newDownloadElapsed;
        State newUploadBandwidth;
        State newUploadBytes;
        State newUploadElapsed;

        try {
            // timestamp format: "2023-07-20T19:34:54Z"
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(tmpCont.getTimestamp())
                    .withZoneSameInstant(timeZoneProvider.getTimeZone());
            newTimestamp = new DateTimeType(zonedDateTime);
        } catch (DateTimeParseException e) {
            newTimestamp = UnDefType.NULL;
            logger.debug("Exception: {}", e.getMessage());
        }
        try {
            newPingJitter = new QuantityType<>(Double.parseDouble(tmpCont.getPing().getJitter()) / 1000.0,
                    Units.SECOND);
        } catch (NumberFormatException e) {
            newPingJitter = UnDefType.NULL;
            logger.debug("Exception: {}", e.getMessage());
        }
        try {
            newPingLatency = new QuantityType<>(Double.parseDouble(tmpCont.getPing().getLatency()) / 1000.0,
                    Units.SECOND);
        } catch (NumberFormatException e) {
            newPingLatency = UnDefType.NULL;
            logger.debug("Exception: {}", e.getMessage());
        }
        try {
            newDownloadBandwidth = new QuantityType<>(
                    Double.parseDouble(tmpCont.getDownload().getBandwidth()) / 125000.0, Units.MEGABIT_PER_SECOND);
        } catch (NumberFormatException e) {
            newDownloadBandwidth = UnDefType.NULL;
            logger.debug("Exception: {}", e.getMessage());
        }
        try {
            newDownloadBytes = new QuantityType<>(Double.parseDouble(tmpCont.getDownload().getBytes()), Units.BYTE);
        } catch (NumberFormatException e) {
            newDownloadBytes = UnDefType.NULL;
            logger.debug("Exception: {}", e.getMessage());
        }
        try {
            newDownloadElapsed = new QuantityType<>(Double.parseDouble(tmpCont.getDownload().getElapsed()) / 1000.0,
                    Units.SECOND);
        } catch (NumberFormatException e) {
            newDownloadElapsed = UnDefType.NULL;
            logger.debug("Exception: {}", e.getMessage());
        }
        try {
            newUploadBandwidth = new QuantityType<>(Double.parseDouble(tmpCont.getUpload().getBandwidth()) / 125000.0,
                    Units.MEGABIT_PER_SECOND);
        } catch (NumberFormatException e) {
            newUploadBandwidth = UnDefType.NULL;
            logger.debug("Exception: {}", e.getMessage());
        }
        try {
            newUploadBytes = new QuantityType<>(Double.parseDouble(tmpCont.getUpload().getBytes()), Units.BYTE);
        } catch (NumberFormatException e) {
            newUploadBytes = UnDefType.NULL;
            logger.debug("Exception: {}", e.getMessage());
        }
        try {
            newUploadElapsed = new QuantityType<>(Double.parseDouble(tmpCont.getUpload().getElapsed()) / 1000.0,
                    Units.SECOND);
        } catch (NumberFormatException e) {
            newUploadElapsed = UnDefType.NULL;
            logger.debug("Exception: {}", e.getMessage());
        }
        String newResultUrl;
        State newResultImage;
        if (tmpCont.getResult().isPersisted()) {
            newResultUrl = tmpCont.getResult().getUrl();
            String url = newResultUrl + ".png";
            logger.debug("Downloading result image from: {}", url);
            RawType image = HttpUtil.downloadImage(url);
            if (image != null) {
                newResultImage = image;
            } else {
                newResultImage = UnDefType.NULL;
            }
        } else {
            logger.debug("Result image not persisted");
            newResultUrl = "";
            newResultImage = UnDefType.NULL;
        }

        String newServer = tmpCont.getServer().getName() + " (" + tmpCont.getServer().getId().toString() + ") "
                + tmpCont.getServer().getLocation();
        return new ResultSnapshot(newPingJitter, newPingLatency, newDownloadBandwidth, newDownloadBytes,
                newDownloadElapsed, newUploadBandwidth, newUploadBytes, newUploadElapsed, tmpCont.getIsp(),
                tmpCont.getInterface().getInternalIp(), tmpCont.getInterface().getExternalIp(), newResultUrl,
                newResultImage, newServer, newTimestamp);
    }

    private void applyResult(ResultSnapshot snapshot) {
        pingJitter = snapshot.pingJitter;
        pingLatency = snapshot.pingLatency;
        downloadBandwidth = snapshot.downloadBandwidth;
        downloadBytes = snapshot.downloadBytes;
        downloadElapsed = snapshot.downloadElapsed;
        uploadBandwidth = snapshot.uploadBandwidth;
        uploadBytes = snapshot.uploadBytes;
        uploadElapsed = snapshot.uploadElapsed;
        isp = snapshot.isp;
        interfaceInternalIp = snapshot.interfaceInternalIp;
        interfaceExternalIp = snapshot.interfaceExternalIp;
        resultUrl = snapshot.resultUrl;
        resultImage = snapshot.resultImage;
        server = snapshot.server;
        timestamp = snapshot.timestamp;
    }

    private record ResultSnapshot(State pingJitter, State pingLatency, State downloadBandwidth, State downloadBytes,
            State downloadElapsed, State uploadBandwidth, State uploadBytes, State uploadElapsed, String isp,
            String interfaceInternalIp, String interfaceExternalIp, String resultUrl, State resultImage, String server,
            State timestamp) {
    }

    protected @Nullable <T> T doExecuteRequest(String arguments, Class<T> type) {
        String dataOut = executeCmd(speedTestCommand + arguments);
        if (type == String.class) {
            @SuppressWarnings("unchecked")
            T obj = (T) dataOut;
            return obj;
        }

        try {
            @Nullable
            T obj = gson.fromJson(dataOut, type);
            return obj;
        } catch (JsonParseException e) {
            logger.debug("Exception: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Update the channels
     */
    private ResultSnapshot snapshotCurrentResult() {
        return new ResultSnapshot(pingJitter, pingLatency, downloadBandwidth, downloadBytes, downloadElapsed,
                uploadBandwidth, uploadBytes, uploadElapsed, isp, interfaceInternalIp, interfaceExternalIp, resultUrl,
                resultImage, server, timestamp);
    }

    private void updateChannels(ResultSnapshot snapshot) {
        logger.debug("Updating channels");

        logger.debug("timestamp: {}", snapshot.timestamp);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.TIMESTAMP), snapshot.timestamp);

        logger.debug("pingJitter: {}", snapshot.pingJitter);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.PING_JITTER), snapshot.pingJitter);

        logger.debug("pingLatency: {}", snapshot.pingLatency);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.PING_LATENCY), snapshot.pingLatency);

        logger.debug("downloadBandwidth: {}", snapshot.downloadBandwidth);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.DOWNLOAD_BANDWIDTH),
                snapshot.downloadBandwidth);

        logger.debug("downloadBytes: {}", snapshot.downloadBytes);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.DOWNLOAD_BYTES),
                snapshot.downloadBytes);

        logger.debug("downloadElapsed: {}", snapshot.downloadElapsed);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.DOWNLOAD_ELAPSED),
                snapshot.downloadElapsed);

        logger.debug("uploadBandwidth: {}", snapshot.uploadBandwidth);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.UPLOAD_BANDWIDTH),
                snapshot.uploadBandwidth);

        logger.debug("uploadBytes: {}", snapshot.uploadBytes);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.UPLOAD_BYTES), snapshot.uploadBytes);

        logger.debug("uploadElapsed: {}", snapshot.uploadElapsed);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.UPLOAD_ELAPSED),
                snapshot.uploadElapsed);

        logger.debug("interfaceExternalIp: {}", snapshot.interfaceExternalIp);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.INTERFACE_EXTERNALIP),
                new StringType(snapshot.interfaceExternalIp));

        logger.debug("interfaceInternalIp: {}", snapshot.interfaceInternalIp);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.INTERFACE_INTERNALIP),
                new StringType(snapshot.interfaceInternalIp));

        logger.debug("isp: {}", snapshot.isp);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.ISP), new StringType(snapshot.isp));

        logger.debug("resultUrl: {}", snapshot.resultUrl);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.RESULT_URL),
                new StringType(snapshot.resultUrl));

        logger.debug("resultImage: <RawType>");
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.RESULT_IMAGE), snapshot.resultImage);

        logger.debug("server: {}", snapshot.server);
        updateState(new ChannelUID(getThing().getUID(), SpeedtestBindingConstants.SERVER),
                new StringType(snapshot.server));
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
        return executeCmd(commandLine, 60000);
    }

    String executeCmd(String commandLine, long timeoutMillis) {
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
                if (cmdArray.length >= 3 && "-c".equals(cmdArray[1])) {
                    cmdArray[2] = "trap 'wait' 0\n" + cmdArray[2];
                }
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

        Process process;
        try {
            process = new ProcessBuilder(cmdArray).redirectErrorStream(true).start();
        } catch (IOException | SecurityException e) {
            logger.debug("An exception occurred while executing '{}': '{}'", Arrays.asList(cmdArray), e.getMessage());
            return "";
        }

        CompletableFuture<String> output = CompletableFuture.supplyAsync(() -> readOutput(process, commandLine));
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        try {
            if (!process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
                logger.debug("Forcibly terminating the process ('{}') after a timeout of {} ms", commandLine,
                        timeoutMillis);
                terminateProcess(process);
                output.cancel(true);
                return "";
            }
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0) {
                terminateProcess(process);
                output.cancel(true);
                return "";
            }
            return output.get(remainingNanos, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            terminateProcess(process);
            output.cancel(true);
            Thread.currentThread().interrupt();
            logger.debug("Interrupted while waiting for the process ('{}') to finish", commandLine);
            return "";
        } catch (TimeoutException e) {
            logger.debug("Timed out while collecting process output for '{}'", commandLine);
            terminateProcess(process);
            output.cancel(true);
            return "";
        } catch (ExecutionException e) {
            logger.debug("Exception while collecting process output for '{}': {}", commandLine,
                    e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
            terminateProcess(process);
            return "";
        }
    }

    private void terminateProcess(Process process) {
        process.toHandle().descendants().forEach(ProcessHandle::destroyForcibly);
        try {
            if (!process.waitFor(100, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
        }
        try {
            process.getInputStream().close();
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted while closing process output stream");
        } catch (IOException e) {
            logger.debug("Exception while closing process output stream: {}", e.getMessage());
        }
    }

    String readOutput(Process process, String commandLine) {
        StringBuilder outputBuilder = new StringBuilder();
        try (InputStreamReader isr = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                outputBuilder.append(line).append(System.lineSeparator());
                logger.debug("Exec [{}]: '{}'", "OUTPUT", line);
            }
        } catch (InterruptedIOException e) {
            terminateProcess(process);
            Thread.currentThread().interrupt();
            logger.debug("Interrupted while reading process output for '{}': {}", commandLine, e.getMessage());
            return "";
        } catch (IOException e) {
            logger.warn("An exception occurred while reading the stdout when executing '{}': '{}'", commandLine,
                    e.getMessage());
            return "";
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
                    operSys = operSys.toLowerCase(Locale.ROOT);

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
