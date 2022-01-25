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
package org.openhab.binding.androiddebugbridge.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

/**
 * The {@link AndroidDebugBridgeConfiguration} class encapsulates adb device connection logic.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class AndroidDebugBridgeDevice {
    public static final int ANDROID_MEDIA_STREAM = 3;
    private static final String ADB_FOLDER = OpenHAB.getUserDataFolder() + File.separator + ".adb";
    private final Logger logger = LoggerFactory.getLogger(AndroidDebugBridgeDevice.class);
    private static final Pattern VOLUME_PATTERN = Pattern
            .compile("volume is (?<current>\\d.*) in range \\[(?<min>\\d.*)\\.\\.(?<max>\\d.*)]");
    private static final Pattern TAP_EVENT_PATTERN = Pattern.compile("(?<x>\\d+),(?<y>\\d+)");
    private static final Pattern PACKAGE_NAME_PATTERN = Pattern
            .compile("^([A-Za-z]{1}[A-Za-z\\d_]*\\.)+[A-Za-z][A-Za-z\\d_]*$");
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,4}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)$");
    private static final Pattern INPUT_EVENT_PATTERN = Pattern
            .compile("/(?<input>\\S+): (?<n1>\\S+) (?<n2>\\S+) (?<n3>\\S+)$", Pattern.MULTILINE);

    private static @Nullable AdbCrypto adbCrypto;

    static {
        var logger = LoggerFactory.getLogger(AndroidDebugBridgeDevice.class);
        try {
            File directory = new File(ADB_FOLDER);
            if (!directory.exists()) {
                directory.mkdir();
            }
            adbCrypto = loadKeyPair(ADB_FOLDER + File.separator + "adb_pub.key",
                    ADB_FOLDER + File.separator + "adb.key");
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            logger.warn("Unable to setup adb keys: {}", e.getMessage());
        }
    }

    private final ScheduledExecutorService scheduler;
    private final ReentrantLock commandLock = new ReentrantLock();

    private String ip = "127.0.0.1";
    private int port = 5555;
    private int timeoutSec = 5;
    private int recordDuration;
    private @Nullable Socket socket;
    private @Nullable AdbConnection connection;
    private @Nullable Future<String> commandFuture;

    public AndroidDebugBridgeDevice(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    public void configure(String ip, int port, int timeout, int recordDuration) {
        this.ip = ip;
        this.port = port;
        this.timeoutSec = timeout;
        this.recordDuration = recordDuration;
    }

    public void sendKeyEvent(String eventCode)
            throws InterruptedException, AndroidDebugBridgeDeviceException, TimeoutException, ExecutionException {
        runAdbShell("input", "keyevent", eventCode);
    }

    public void sendText(String text)
            throws AndroidDebugBridgeDeviceException, InterruptedException, TimeoutException, ExecutionException {
        runAdbShell("input", "text", URLEncoder.encode(text, StandardCharsets.UTF_8));
    }

    public void sendTap(String point)
            throws AndroidDebugBridgeDeviceException, InterruptedException, TimeoutException, ExecutionException {
        var match = TAP_EVENT_PATTERN.matcher(point);
        if (!match.matches()) {
            throw new AndroidDebugBridgeDeviceException("Unable to parse tap event");
        }
        runAdbShell("input", "mouse", "tap", match.group("x"), match.group("y"));
    }

    public void openUrl(String url)
            throws InterruptedException, AndroidDebugBridgeDeviceException, TimeoutException, ExecutionException {
        var match = URL_PATTERN.matcher(url);
        if (!match.matches()) {
            throw new AndroidDebugBridgeDeviceException("Unable to parse url");
        }
        runAdbShell("am", "start", "-a", url);
    }

    public void startPackage(String packageName)
            throws InterruptedException, AndroidDebugBridgeDeviceException, TimeoutException, ExecutionException {
        if (packageName.contains("/")) {
            startPackageWithActivity(packageName);
            return;
        }
        if (!PACKAGE_NAME_PATTERN.matcher(packageName).matches()) {
            logger.warn("{} is not a valid package name", packageName);
            return;
        }
        var out = runAdbShell("monkey", "--pct-syskeys", "0", "-p", packageName, "-v", "1");
        if (out.contains("monkey aborted")) {
            startTVPackage(packageName);
        }
    }

    private void startTVPackage(String packageName)
            throws InterruptedException, AndroidDebugBridgeDeviceException, TimeoutException, ExecutionException {
        // https://developer.android.com/training/tv/start/start
        String result = runAdbShell("monkey", "--pct-syskeys", "0", "-c", "android.intent.category.LEANBACK_LAUNCHER",
                "-p", packageName, "1");
        if (result.contains("monkey aborted")) {
            throw new AndroidDebugBridgeDeviceException("Unable to open package");
        }
    }

    public void startPackageWithActivity(String packageWithActivity)
            throws InterruptedException, AndroidDebugBridgeDeviceException, TimeoutException, ExecutionException {
        var parts = packageWithActivity.split("/");
        if (parts.length != 2) {
            logger.warn("{} is not a valid package", packageWithActivity);
            return;
        }
        var packageName = parts[0];
        var activityName = parts[1];
        if (!PACKAGE_NAME_PATTERN.matcher(packageName).matches()) {
            logger.warn("{} is not a valid package name", packageName);
            return;
        }
        if (!PACKAGE_NAME_PATTERN.matcher(activityName).matches()) {
            logger.warn("{} is not a valid activity name", activityName);
            return;
        }
        var out = runAdbShell("am", "start", "-n", packageWithActivity);
        if (out.contains("usage: am")) {
            out = runAdbShell("am", "start", packageWithActivity);
        }
        if (out.contains("usage: am") || out.contains("Exception")) {
            logger.warn("open {} fail; retrying to open without activity info", packageWithActivity);
            startPackage(packageName);
        }
    }

    public void stopPackage(String packageName)
            throws AndroidDebugBridgeDeviceException, InterruptedException, TimeoutException, ExecutionException {
        if (!PACKAGE_NAME_PATTERN.matcher(packageName).matches()) {
            logger.warn("{} is not a valid package name", packageName);
            return;
        }
        runAdbShell("am", "force-stop", packageName);
    }

    public String getCurrentPackage() throws AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException, TimeoutException, ExecutionException {
        var out = runAdbShell("dumpsys", "window", "windows", "|", "grep", "mFocusedApp");
        var targetLine = Arrays.stream(out.split("\n")).findFirst().orElse("");
        var lineParts = targetLine.split(" ");
        if (lineParts.length >= 2) {
            var packageActivityName = lineParts[lineParts.length - 2];
            if (packageActivityName.contains("/")) {
                return packageActivityName.split("/")[0];
            }
        }
        throw new AndroidDebugBridgeDeviceReadException("Unable to read package name");
    }

    public boolean isAwake()
            throws InterruptedException, AndroidDebugBridgeDeviceException, TimeoutException, ExecutionException {
        String devicesResp = runAdbShell("dumpsys", "activity", "|", "grep", "mWakefulness");
        return devicesResp.contains("mWakefulness=Awake");
    }

    public boolean isScreenOn() throws InterruptedException, AndroidDebugBridgeDeviceException,
            AndroidDebugBridgeDeviceReadException, TimeoutException, ExecutionException {
        String devicesResp = runAdbShell("dumpsys", "power", "|", "grep", "'Display Power'");
        if (devicesResp.contains("=")) {
            try {
                var state = devicesResp.split("=")[1].trim();
                return state.equals("ON");
            } catch (NumberFormatException e) {
                logger.debug("Unable to parse device screen state: {}", e.getMessage());
            }
        }
        throw new AndroidDebugBridgeDeviceReadException("Unable to read screen state");
    }

    public boolean isPlayingMedia(String currentApp)
            throws AndroidDebugBridgeDeviceException, InterruptedException, TimeoutException, ExecutionException {
        String devicesResp = runAdbShell("dumpsys", "media_session", "|", "grep", "-A", "100", "'Sessions Stack'", "|",
                "grep", "-A", "50", currentApp);
        String[] mediaSessions = devicesResp.split("\n\n");
        if (mediaSessions.length == 0) {
            // no media session found for current app
            return false;
        }
        boolean isPlaying = mediaSessions[0].contains("PlaybackState {state=3");
        logger.debug("device media state playing {}", isPlaying);
        return isPlaying;
    }

    public boolean isPlayingAudio()
            throws AndroidDebugBridgeDeviceException, InterruptedException, TimeoutException, ExecutionException {
        String audioDump = runAdbShell("dumpsys", "audio", "|", "grep", "ID:");
        return audioDump.contains("state:started");
    }

    public VolumeInfo getMediaVolume() throws AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException, TimeoutException, ExecutionException {
        return getVolume(ANDROID_MEDIA_STREAM);
    }

    public void setMediaVolume(int volume)
            throws AndroidDebugBridgeDeviceException, InterruptedException, TimeoutException, ExecutionException {
        setVolume(ANDROID_MEDIA_STREAM, volume);
    }

    public int getPowerWakeLock() throws InterruptedException, AndroidDebugBridgeDeviceException,
            AndroidDebugBridgeDeviceReadException, TimeoutException, ExecutionException {
        String lockResp = runAdbShell("dumpsys", "power", "|", "grep", "Locks", "|", "grep", "'size='");
        if (lockResp.contains("=")) {
            try {
                return Integer.parseInt(lockResp.replace("\n", "").split("=")[1].trim());
            } catch (NumberFormatException e) {
                String message = String.format("Unable to parse device wake-lock '%s'", lockResp);
                logger.debug("{}: {}", message, e.getMessage());
                throw new AndroidDebugBridgeDeviceReadException(message);
            }
        }
        throw new AndroidDebugBridgeDeviceReadException(String.format("Unable to read wake-lock '%s'", lockResp));
    }

    private void setVolume(int stream, int volume)
            throws AndroidDebugBridgeDeviceException, InterruptedException, TimeoutException, ExecutionException {
        runAdbShell("media", "volume", "--show", "--stream", String.valueOf(stream), "--set", String.valueOf(volume));
    }

    public String getModel() throws AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException, TimeoutException, ExecutionException {
        return getDeviceProp("ro.product.model");
    }

    public String getAndroidVersion() throws AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException, TimeoutException, ExecutionException {
        return getDeviceProp("ro.build.version.release");
    }

    public String getBrand() throws AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException, TimeoutException, ExecutionException {
        return getDeviceProp("ro.product.brand");
    }

    public String getSerialNo() throws AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException, TimeoutException, ExecutionException {
        return getDeviceProp("ro.serialno");
    }

    public String getMacAddress() throws AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException, TimeoutException, ExecutionException {
        return getDeviceProp("ro.boot.wifimacaddr").toLowerCase();
    }

    private String getDeviceProp(String name) throws AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException, TimeoutException, ExecutionException {
        var propValue = runAdbShell("getprop", name, "&&", "sleep", "0.3").replace("\n", "").replace("\r", "");
        if (propValue.length() == 0) {
            throw new AndroidDebugBridgeDeviceReadException(String.format("Unable to get device property '%s'", name));
        }
        return propValue;
    }

    private VolumeInfo getVolume(int stream) throws AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException, TimeoutException, ExecutionException {
        String volumeResp = runAdbShell("media", "volume", "--show", "--stream", String.valueOf(stream), "--get", "|",
                "grep", "volume");
        Matcher matcher = VOLUME_PATTERN.matcher(volumeResp);
        if (!matcher.find()) {
            throw new AndroidDebugBridgeDeviceReadException("Unable to get volume info");
        }
        var volumeInfo = new VolumeInfo(Integer.parseInt(matcher.group("current")),
                Integer.parseInt(matcher.group("min")), Integer.parseInt(matcher.group("max")));
        logger.debug("Device {}:{} VolumeInfo: current {}, min {}, max {}", this.ip, this.port, volumeInfo.current,
                volumeInfo.min, volumeInfo.max);
        return volumeInfo;
    }

    public String recordInputEvents()
            throws AndroidDebugBridgeDeviceException, InterruptedException, TimeoutException, ExecutionException {
        String out = runAdbShell(recordDuration * 2, "getevent", "&", "sleep", Integer.toString(recordDuration), "&&",
                "exit");
        var matcher = INPUT_EVENT_PATTERN.matcher(out);
        var commandList = new ArrayList<String>();
        try {
            while (matcher.find()) {
                String inputPath = matcher.group("input");
                int n1 = Integer.parseInt(matcher.group("n1"), 16);
                int n2 = Integer.parseInt(matcher.group("n2"), 16);
                int n3 = Integer.parseInt(matcher.group("n3"), 16);
                commandList.add(String.format("sendevent /%s %d %d %d", inputPath, n1, n2, n3));
            }
        } catch (NumberFormatException e) {
            logger.warn("NumberFormatException while parsing events, aborting");
            return "";
        }
        return String.join(" && ", commandList);
    }

    public void sendInputEvents(String command)
            throws AndroidDebugBridgeDeviceException, InterruptedException, TimeoutException, ExecutionException {
        String out = runAdbShell(command.split(" "));
        if (out.length() != 0) {
            logger.warn("Device event unexpected output: {}", out);
            throw new AndroidDebugBridgeDeviceException("Device event execution fail");
        }
    }

    public void rebootDevice()
            throws AndroidDebugBridgeDeviceException, InterruptedException, TimeoutException, ExecutionException {
        try {
            runAdbShell("reboot", "&", "sleep", "0.1", "&&", "exit");
        } finally {
            disconnect();
        }
    }

    public void powerOffDevice()
            throws AndroidDebugBridgeDeviceException, InterruptedException, TimeoutException, ExecutionException {
        try {
            runAdbShell("reboot", "-p", "&", "sleep", "0.1", "&&", "exit");
        } finally {
            disconnect();
        }
    }

    public boolean isConnected() {
        var currentSocket = socket;
        return currentSocket != null && currentSocket.isConnected();
    }

    public void connect() throws AndroidDebugBridgeDeviceException, InterruptedException {
        this.disconnect();
        AdbConnection adbConnection;
        Socket sock;
        AdbCrypto crypto = adbCrypto;
        if (crypto == null) {
            throw new AndroidDebugBridgeDeviceException("Device not connected");
        }
        try {
            sock = new Socket();
            socket = sock;
            sock.connect(new InetSocketAddress(ip, port), (int) TimeUnit.SECONDS.toMillis(15));
        } catch (IOException e) {
            logger.debug("Error connecting to {}: [{}] {}", ip, e.getClass().getName(), e.getMessage());
            if ("Socket closed".equals(e.getMessage())) {
                // Connection aborted by us
                throw new InterruptedException();
            }
            throw new AndroidDebugBridgeDeviceException("Can not open socket " + ip + ":" + port);
        }
        try {
            adbConnection = AdbConnection.create(sock, crypto);
            connection = adbConnection;
            adbConnection.connect(15, TimeUnit.SECONDS, false);
        } catch (IOException e) {
            logger.debug("Error connecting to {}: {}", ip, e.getMessage());
            throw new AndroidDebugBridgeDeviceException("Can not open adb connection " + ip + ":" + port);
        }
    }

    private String runAdbShell(String... args)
            throws InterruptedException, AndroidDebugBridgeDeviceException, TimeoutException, ExecutionException {
        return runAdbShell(timeoutSec, args);
    }

    private String runAdbShell(int commandTimeout, String... args)
            throws InterruptedException, AndroidDebugBridgeDeviceException, TimeoutException, ExecutionException {
        var adb = connection;
        if (adb == null) {
            throw new AndroidDebugBridgeDeviceException("Device not connected");
        }
        try {
            commandLock.lock();
            var commandFuture = scheduler.submit(() -> {
                var byteArrayOutputStream = new ByteArrayOutputStream();
                String cmd = String.join(" ", args);
                logger.debug("{} - shell:{}", ip, cmd);
                try (AdbStream stream = adb.open("shell:" + cmd)) {
                    do {
                        byteArrayOutputStream.writeBytes(stream.read());
                    } while (!stream.isClosed());
                } catch (IOException e) {
                    if (!"Stream closed".equals(e.getMessage())) {
                        throw e;
                    }
                }
                return byteArrayOutputStream.toString(StandardCharsets.US_ASCII);
            });
            this.commandFuture = commandFuture;
            return commandFuture.get(commandTimeout, TimeUnit.SECONDS);
        } finally {
            var commandFuture = this.commandFuture;
            if (commandFuture != null) {
                commandFuture.cancel(true);
                this.commandFuture = null;
            }
            commandLock.unlock();
        }
    }

    private static AdbBase64 getBase64Impl() {
        Charset asciiCharset = Charset.forName("ASCII");
        return bytes -> new String(Base64.getEncoder().encode(bytes), asciiCharset);
    }

    private static AdbCrypto loadKeyPair(String pubKeyFile, String privKeyFile)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        File pub = new File(pubKeyFile);
        File priv = new File(privKeyFile);
        AdbCrypto c = null;
        // load key pair
        if (pub.exists() && priv.exists()) {
            try {
                c = AdbCrypto.loadAdbKeyPair(getBase64Impl(), priv, pub);
            } catch (IOException ignored) {
                // Keys don't exits
            }
        }
        if (c == null) {
            // generate key pair
            c = AdbCrypto.generateAdbKeyPair(getBase64Impl());
            c.saveAdbKeyPair(priv, pub);
        }
        return c;
    }

    public void disconnect() {
        var commandFuture = this.commandFuture;
        if (commandFuture != null && !commandFuture.isDone()) {
            commandFuture.cancel(true);
        }
        var adb = connection;
        var sock = socket;
        if (adb != null) {
            try {
                adb.close();
            } catch (IOException ignored) {
            }
            connection = null;
        }
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException ignored) {
            }
            socket = null;
        }
    }

    public static class VolumeInfo {
        public int current;
        public int min;
        public int max;

        VolumeInfo(int current, int min, int max) {
            this.current = current;
            this.min = min;
            this.max = max;
        }
    }
}
