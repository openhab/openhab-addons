/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
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

    private String ip = "127.0.0.1";
    private int port = 5555;
    private @Nullable Socket socket;
    private @Nullable AdbConnection connection;

    public void configure(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void sendKeyEvent(String eventCode)
            throws IOException, InterruptedException, AndroidDebugBridgeDeviceException {
        runAdbShell("input", "keyevent", eventCode);
    }

    public void sendText(String text) throws IOException, AndroidDebugBridgeDeviceException, InterruptedException {
        runAdbShell("input", "text", URLEncoder.encode(text, StandardCharsets.UTF_8));
    }

    public void startPackage(String packageName) throws IOException, AndroidDebugBridgeDeviceException,
            InterruptedException, AndroidDebugBridgeDeviceException {
        var out = runAdbShell("monkey", "--pct-syskeys", "0", "-p", packageName, "-v", "1");
        if (out.contains("monkey aborted"))
            throw new AndroidDebugBridgeDeviceException("Unable to open package");
    }

    public void stopPackage(String packageName)
            throws IOException, AndroidDebugBridgeDeviceException, InterruptedException {
        runAdbShell("am", "force-stop", packageName);
    }

    public String getCurrentPackage() throws IOException, AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException {
        var out = runAdbShell("dumpsys", "window", "windows", "|", "grep", "mFocusedApp");
        var targetLine = Arrays.stream(out.split("\n")).findFirst().orElse("");
        var lineParts = targetLine.split(" ");
        if (lineParts.length >= 2) {
            var packageActivityName = lineParts[lineParts.length - 2];
            if (packageActivityName.contains("/"))
                return packageActivityName.split("/")[0];
        }
        throw new AndroidDebugBridgeDeviceReadException("can read package name");
    }

    public boolean isPlayingMedia() throws IOException, AndroidDebugBridgeDeviceException, InterruptedException {
        // Try to get players info from audio dump
        String audioDump = runAdbShell("dumpsys", "audio", "|", "grep", "ID:");
        if (audioDump.length() == 0) {
            // Fallback to media session dump
            String devicesResp = runAdbShell("dumpsys", "media_session", "|", "grep", "PlaybackState");
            return devicesResp.contains("PlaybackState {state=3");
        }
        return audioDump.contains("state:started");
    }

    public VolumeInfo getMediaVolume() throws IOException, AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException {
        return getVolume(ANDROID_MEDIA_STREAM);
    }

    public void setMediaVolume(int volume) throws IOException, AndroidDebugBridgeDeviceException, InterruptedException {
        setVolume(ANDROID_MEDIA_STREAM, volume);
    }

    private void setVolume(int stream, int volume)
            throws IOException, AndroidDebugBridgeDeviceException, InterruptedException {
        runAdbShell("media", "volume", "--show", "--stream", String.valueOf(stream), "--set", String.valueOf(volume));
    }

    public String getModel() throws IOException, AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException {
        return getDeviceProp("ro.product.model");
    }

    public String getAndroidVersion() throws IOException, AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException {
        return getDeviceProp("ro.build.version.release");
    }

    public String getBrand() throws IOException, AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException {
        return getDeviceProp("ro.product.brand");
    }

    public String getSerialNo() throws IOException, AndroidDebugBridgeDeviceException, InterruptedException,
            AndroidDebugBridgeDeviceReadException {
        return getDeviceProp("ro.serialno");
    }

    private String getDeviceProp(String name) throws IOException, AndroidDebugBridgeDeviceException,
            InterruptedException, AndroidDebugBridgeDeviceReadException {
        var propValue = runAdbShell("getprop", name, "&&", "sleep", "0.3").replace("\n", "").replace("\r", "");
        if (propValue.length() == 0) {
            throw new AndroidDebugBridgeDeviceReadException("Unable to get device property");
        }
        return propValue;
    }

    private VolumeInfo getVolume(int stream) throws IOException, AndroidDebugBridgeDeviceException,
            InterruptedException, AndroidDebugBridgeDeviceReadException {
        String volumeResp = runAdbShell("media", "volume", "--show", "--stream", String.valueOf(stream), "--get", "|",
                "grep", "volume");
        Matcher matcher = VOLUME_PATTERN.matcher(volumeResp);
        if (!matcher.find())
            throw new AndroidDebugBridgeDeviceReadException("Unable to get volume info");
        var volumeInfo = new VolumeInfo(Integer.parseInt(matcher.group("current")),
                Integer.parseInt(matcher.group("min")), Integer.parseInt(matcher.group("max")));
        logger.debug("Device {}:{} VolumeInfo: current {}, min {}, max {}", this.ip, this.port, volumeInfo.current,
                volumeInfo.min, volumeInfo.max);
        return volumeInfo;
    }

    public boolean isConnected() {
        var currentSocket = socket;
        return currentSocket != null && currentSocket.isConnected();
    }

    public void connect() throws AndroidDebugBridgeDeviceException {
        this.disconnect();
        AdbConnection adbConnection;
        Socket sock;
        AdbCrypto crypto = adbCrypto;
        if (crypto == null) {
            throw new AndroidDebugBridgeDeviceException("Device not connected");
        }
        try {
            sock = new Socket(ip, port);
            sock.setTcpNoDelay(true);
        } catch (IOException e) {
            throw new AndroidDebugBridgeDeviceException("Can not open socket " + ip + ":" + port);
        }
        try {
            adbConnection = AdbConnection.create(sock, crypto);
            adbConnection.connect();
        } catch (IOException | InterruptedException e) {
            throw new AndroidDebugBridgeDeviceException("Can not open adb connection " + ip + ":" + port);
        }
        connection = adbConnection;
        socket = sock;
    }

    private String runAdbShell(String... args)
            throws IOException, InterruptedException, AndroidDebugBridgeDeviceException {
        var adb = connection;
        if (adb == null) {
            throw new AndroidDebugBridgeDeviceException("Device not connected");
        }
        synchronized (adb) {
            var byteArrayOutputStream = new ByteArrayOutputStream();
            var cmd = String.join(" ", args);
            logger.debug("{} - shell:{}", ip, cmd);
            try {
                var stream = adb.open("shell:" + cmd);
                do {
                    byteArrayOutputStream.writeBytes(stream.read());
                } while (!stream.isClosed());
            } catch (IOException e) {
                var message = e.getMessage();
                if (message != null && !message.equals("Stream closed"))
                    throw e;
            }
            return byteArrayOutputStream.toString(StandardCharsets.US_ASCII);
        }
    }

    private static AdbBase64 getBase64Impl() {
        return bytes -> new String(Base64.getEncoder().encode(bytes));
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
