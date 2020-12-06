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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cgutman.adblib.AdbBase64;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;

/**
 * The {@link AndroidDebugBridgeConfiguration} class encapsulates adb device connection logic.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class AndroidDebugBridgeDevice {
    public static final int ANDROID_MEDIA_STREAM = 3;
    private final Logger logger = LoggerFactory.getLogger(AndroidDebugBridgeDevice.class);

    private static final Pattern VOLUME_PATTERN = Pattern
            .compile("volume is (?<current>\\d.*) in range \\[(?<min>\\d.*)\\.\\.(?<max>\\d.*)]");

    @Nullable
    static AdbCrypto adbCrypto;

    static {
        var logger = LoggerFactory.getLogger(AndroidDebugBridgeDevice.class);
        try {
            File directory = new File(".adb");
            if (!directory.exists()) {
                directory.mkdir();
            }
            adbCrypto = loadKeyPair(".adb/adb_pub.key", ".adb/adb.key");
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            logger.warn("Unable to setup adb keys: {}", e.getMessage());
        }
    }

    private String ip = "127.0.0.1";
    private int port = 5555;
    @Nullable
    private Socket socket;
    @Nullable
    private AdbConnection connection;

    public AndroidDebugBridgeDevice() {
    }

    public void configure(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void sendKeyEvent(String eventCode)
            throws IOException, AndroidDebugBridgeDeviceNotConnectedException, InterruptedException {
        runAdbShell("input", "keyevent", eventCode);
    }

    public void sendText(String text)
            throws IOException, AndroidDebugBridgeDeviceNotConnectedException, InterruptedException {
        runAdbShell("input", "text", URLEncoder.encode(text, StandardCharsets.UTF_8));
    }

    public void startPackage(String packageName) throws IOException, AndroidDebugBridgeDeviceNotConnectedException,
            InterruptedException, AndroidDebugBridgeDeviceException {
        var out = runAdbShell("monkey", "-p", packageName, "-v", "1", "&&", "sleep", "0.3");
        if (out.contains("monkey aborted"))
            throw new AndroidDebugBridgeDeviceException("Unable to open package");
    }

    public void stopPackage(String packageName)
            throws IOException, AndroidDebugBridgeDeviceNotConnectedException, InterruptedException {
        runAdbShell("am", "force-stop", packageName);
    }

    public String getCurrentPackage() throws IOException, AndroidDebugBridgeDeviceNotConnectedException,
            InterruptedException, AndroidDebugBridgeDeviceException, AndroidDebugBridgeDeviceReadException {
        var out = runAdbShell("dumpsys", "window", "windows", "|", "grep", "mFocusedApp", "&&", "sleep", "0.3");
        var targetLine = Arrays.stream(out.split("\n")).findFirst().orElse("");
        var lineParts = targetLine.split(" ");
        if (lineParts.length >= 2) {
            var packageActivityName = lineParts[lineParts.length - 2];
            if (packageActivityName.contains("/"))
                return packageActivityName.split("/")[0];
        }
        throw new AndroidDebugBridgeDeviceReadException("can read package name");
    }

    // public String[] getPackages(String packageName)
    // throws IOException, AndroidDebugBridgeDeviceNotConnectedException, InterruptedException {
    // var out = runAdbShell("cmd", "package", "list", "packages");
    // return Arrays.asList(out.split("\n")).stream().filter(line -> line.contains("package:"))
    // .map(line -> line.substring("package:".length())).toArray(String[]::new);
    // }

    public boolean isPlayingMedia()
            throws IOException, AndroidDebugBridgeDeviceNotConnectedException, InterruptedException {
        // Try to get players info from audio dump
        String audioDump = runAdbShell("dumpsys", "audio", "|", "grep", "ID:", "&&", "sleep", "0.3");
        if (audioDump.length() == 0) {
            // Fallback to media session dump
            String devicesResp = runAdbShell("dumpsys", "media_session", "|", "grep", "PlaybackState", "&&", "sleep",
                    "0.3");
            return devicesResp.contains("PlaybackState {state=3");
        }
        return audioDump.contains("state:started");
    }

    public VolumeInfo getMediaVolume() throws IOException, AndroidDebugBridgeDeviceNotConnectedException,
            InterruptedException, AndroidDebugBridgeDeviceReadException {
        return getVolume(ANDROID_MEDIA_STREAM);
    }

    public void setMediaVolume(int volume)
            throws IOException, AndroidDebugBridgeDeviceNotConnectedException, InterruptedException {
        setVolume(ANDROID_MEDIA_STREAM, volume);
    }

    private void setVolume(int stream, int volume)
            throws IOException, AndroidDebugBridgeDeviceNotConnectedException, InterruptedException {
        runAdbShell("media", "volume", "--show", "--stream", String.valueOf(stream), "--set", String.valueOf(volume));
    }

    public String getModel() throws IOException, AndroidDebugBridgeDeviceNotConnectedException, InterruptedException,
            AndroidDebugBridgeDeviceReadException {
        return getDeviceProp("ro.product.model");
    }

    public String getAndroidVersion() throws IOException, AndroidDebugBridgeDeviceNotConnectedException,
            InterruptedException, AndroidDebugBridgeDeviceReadException {
        return getDeviceProp("ro.build.version.release");
    }

    public String getBrand() throws IOException, AndroidDebugBridgeDeviceNotConnectedException, InterruptedException,
            AndroidDebugBridgeDeviceReadException {
        return getDeviceProp("ro.product.brand");
    }

    public String getSerialNo() throws IOException, AndroidDebugBridgeDeviceNotConnectedException, InterruptedException,
            AndroidDebugBridgeDeviceReadException {
        return getDeviceProp("ro.serialno");
    }

    private String getDeviceProp(String name) throws IOException, AndroidDebugBridgeDeviceNotConnectedException,
            InterruptedException, AndroidDebugBridgeDeviceReadException {
        var propValue = runAdbShell("getprop", name, "&&", "sleep", "0.3").replace("\n", "").replace("\r", "");
        if (propValue.length() == 0) {
            throw new AndroidDebugBridgeDeviceReadException("Unable to get device property");
        }
        return propValue;
    }

    private VolumeInfo getVolume(int stream) throws IOException, AndroidDebugBridgeDeviceNotConnectedException,
            InterruptedException, AndroidDebugBridgeDeviceReadException {
        String volumeResp = runAdbShell("media", "volume", "--show", "--stream", String.valueOf(stream), "--get", "|",
                "grep", "volume", "&&", "sleep", "0.3");
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

    public void connect() throws AndroidDebugBridgeDeviceException, AndroidDebugBridgeDeviceCryptographyException {
        this.disconnect();
        AdbConnection adbConnection;
        Socket sock;
        AdbCrypto crypto = adbCrypto;
        if (crypto == null) {
            throw new AndroidDebugBridgeDeviceCryptographyException();
        }
        try {
            sock = new Socket(ip, port);
            sock.setTcpNoDelay(true);
            // sock.setSoTimeout(5000);
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
            throws IOException, InterruptedException, AndroidDebugBridgeDeviceNotConnectedException {
        var adb = connection;
        if (adb == null) {
            throw new AndroidDebugBridgeDeviceNotConnectedException();
        }
        synchronized (adb) {
            var byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                var stream = adb.open("shell:" + String.join(" ", args));
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

    static class AndroidDebugBridgeDeviceException extends Exception {

        public AndroidDebugBridgeDeviceException(String message) {
            super(message);
        }
    }

    static class AndroidDebugBridgeDeviceNotConnectedException extends Exception {

        public AndroidDebugBridgeDeviceNotConnectedException() {
            super("Device not connected");
        }
    }

    static class AndroidDebugBridgeDeviceCryptographyException extends Exception {

        public AndroidDebugBridgeDeviceCryptographyException() {
            super("Device not connected");
        }
    }

    static class AndroidDebugBridgeDeviceReadException extends Exception {

        public AndroidDebugBridgeDeviceReadException(String message) {
            super(message);
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
