/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * This class represents a Thread dataset.
 * It supports both JSON and hex serialization according to the Thread specification.
 * The JSON format is compatible with the OTBR JSON format.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ThreadDataset {

    private static final long DEFAULT_CHANNEL_MASK = 0x07FFF800L; // Decimal: 134152192, 2.4 GHz channels 11-26

    private static final int TLV_CHANNEL = 0;
    private static final int TLV_PAN_ID = 1;
    private static final int TLV_EXT_PAN_ID = 2;
    private static final int TLV_NETWORK_NAME = 3;
    private static final int TLV_PSKC = 4;
    private static final int TLV_NETWORK_KEY = 5;
    private static final int TLV_MESH_LOCAL_PREFIX = 7;
    private static final int TLV_SECURITY_POLICY = 12;
    private static final int TLV_ACTIVE_TIMESTAMP = 14;
    private static final int TLV_PENDING_TIMESTAMP = 51;
    private static final int TLV_DELAY_TIMER = 52;
    private static final int TLV_CHANNEL_MASK = 53;

    private static final String TYPE_ACTIVE_TIMESTAMP = "ActiveTimestamp";
    private static final String TYPE_PENDING_TIMESTAMP = "PendingTimestamp";
    private static final String TYPE_DELAY_TIMER = "DelayTimer";
    private static final String TYPE_CHANNEL = "Channel";
    private static final String TYPE_CHANNEL_MASK = "ChannelMask";
    private static final String TYPE_PAN_ID = "PanId";
    private static final String TYPE_NETWORK_NAME = "NetworkName";
    private static final String TYPE_NETWORK_KEY = "NetworkKey";
    private static final String TYPE_EXT_PAN_ID = "ExtPanId";
    private static final String TYPE_MESH_LOCAL_PREFIX = "MeshLocalPrefix";
    private static final String TYPE_PSKC = "PSKc";
    private static final String TYPE_SECURITY_POLICY = "SecurityPolicy";

    private static final String PROP_SECONDS = "Seconds";
    private static final String PROP_TICKS = "Ticks";
    private static final String PROP_AUTHORITATIVE = "Authoritative";
    private static final String PROP_ROTATION_TIME = "RotationTime";
    private static final String PROP_OBTAIN_NETWORK_KEY = "ObtainNetworkKey";
    private static final String PROP_NATIVE_COMMISSIONING = "NativeCommissioning";
    private static final String PROP_ROUTERS = "Routers";
    private static final String PROP_EXTERNAL_COMMISSIONING = "ExternalCommissioning";
    private static final String PROP_COMMERCIAL_COMMISSIONING = "CommercialCommissioning";
    private static final String PROP_AUTONOMOUS_ENROLLMENT = "AutonomousEnrollment";
    private static final String PROP_NETWORK_KEY_PROVISIONING = "NetworkKeyProvisioning";
    private static final String PROP_TOBLE_LINK = "TobleLink";
    private static final String PROP_NON_CCM_ROUTERS = "NonCcmRouters";

    /* Security Policy Flags */
    private static final int SP_OBTAIN_NETWORK_KEY = 1 << 15;
    private static final int SP_NATIVE_COMMISSIONING = 1 << 14;
    private static final int SP_ROUTERS = 1 << 13;
    private static final int SP_EXTERNAL_COMM = 1 << 12;
    private static final int SP_COMMERCIAL_COMM = 1 << 10;
    private static final int SP_AUTO_ENROLL = 1 << 9;
    private static final int SP_NET_KEY_PROV = 1 << 8;
    private static final int SP_TO_BLE_LINK = 1 << 7;
    private static final int SP_NON_CCM_ROUTERS = 1 << 6;
    private static final int SP_RSV = 0x0038;

    private static final int MK_KEY_LEN = 16;

    private static final int PBKDF_ITERATIONS = 16_384;
    private static final int PBKDF_KEY_LENGTH = 128;

    private static final long MIN_DELAY_MS = 30_000L;
    private static final long MAX_DELAY_MS = 259_200_000L;

    private static final String PBKDF_ALG = "PBKDF2WithHmacSHA256";

    private static final String DEFAULT_SECURITY_POLICY = "02A0F3B8";

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ThreadDataset.class, new ThreadDatasetAdapter()).setPrettyPrinting().create();

    private final TlvCodec codec = new TlvCodec();

    public ThreadDataset() {
        setSecurityPolicy(DEFAULT_SECURITY_POLICY);
        setChannelMask(DEFAULT_CHANNEL_MASK);
    }

    /**
     * Create a new Thread dataset with the given parameters.
     * TLV values are optional and may be null.
     * 
     * @param activeTs
     * @param pendingTs
     * @param delayMs
     * @param channel
     * @param channelMask
     * @param panId
     * @param networkName
     * @param networkKeyHex
     * @param extPanIdHex
     * @param pskcHex
     * @param meshPrefix
     * @param secPolicyHex
     */
    public ThreadDataset(@Nullable ThreadTimestamp activeTs, @Nullable ThreadTimestamp pendingTs,
            @Nullable Long delayMs, @Nullable Integer channel, @Nullable Long channelMask, @Nullable Integer panId,
            @Nullable String networkName, @Nullable String networkKeyHex, @Nullable String extPanIdHex,
            @Nullable String pskcHex, @Nullable String meshPrefix, @Nullable String secPolicyHex) {
        if (activeTs != null) {
            setActiveTimestamp(activeTs);
        }
        if (pendingTs != null) {
            setPendingTimestamp(pendingTs);
        }
        if (delayMs != null) {
            setDelayTimer(delayMs.longValue());
        }
        if (channel != null) {
            setChannel(channel.intValue());
        }
        if (channelMask != null) {
            setChannelMask(channelMask.longValue());
        } else {
            setChannelMask(DEFAULT_CHANNEL_MASK);
        }
        if (panId != null) {
            setPanId(panId.intValue());
        }
        if (networkName != null) {
            setNetworkName(networkName);
        }
        if (present(networkKeyHex)) {
            setNetworkKey(Objects.requireNonNull(networkKeyHex));
        }
        if (present(extPanIdHex)) {
            setExtPanId(Objects.requireNonNull(extPanIdHex));
        }
        if (present(pskcHex)) {
            setPskc(Objects.requireNonNull(pskcHex));
        }
        if (present(meshPrefix)) {
            try {
                setMeshLocalPrefix(parsePrefix(Objects.requireNonNull(meshPrefix)));
            } catch (IOException e) {
                throw new IllegalArgumentException("Bad mesh-local prefix: " + meshPrefix, e);
            }
        }

        if (present(secPolicyHex)) {
            setSecurityPolicy(Objects.requireNonNull(secPolicyHex));
        } else {
            setSecurityPolicy(DEFAULT_SECURITY_POLICY);
        }
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public Optional<Long> getActiveTimestamp() {
        return codec.getUint64(TLV_ACTIVE_TIMESTAMP);
    }

    public Optional<String> getActiveTimestampHex() {
        return toHex(getActiveTimestamp(), 16);
    }

    public Optional<ThreadTimestamp> getActiveTimestampObject() {
        return getActiveTimestamp().map(ThreadTimestamp::fromLong);
    }

    public Optional<Long> getPendingTimestamp() {
        return codec.getUint64(TLV_PENDING_TIMESTAMP);
    }

    public Optional<String> getPendingTimestampHex() {
        return toHex(getPendingTimestamp(), 16);
    }

    public Optional<Long> getDelayTimer() {
        return codec.getUint32(TLV_DELAY_TIMER);
    }

    public Optional<String> getDelayTimerHex() {
        return toHex(getDelayTimer(), 8);
    }

    public Optional<Integer> getChannel() {
        return codec.getBytes(TLV_CHANNEL).flatMap(b -> {
            if (b.length != 3) { // Thread 1.4 requires a 3-byte Channel TLV
                return Optional.empty();
            }
            int msb = Byte.toUnsignedInt(b[1]);
            int lsb = Byte.toUnsignedInt(b[2]);
            return Optional.of((msb << 8) | lsb);
        });
    }

    public Optional<String> getChannelHex() {
        return toHex(getChannel().map(Long::valueOf), 2);
    }

    /**
     * Get the channel mask as a long.
     * Value the raw bits into their IEEE channel positions
     * 
     * @return
     */
    public Optional<Long> getChannelMask() {
        return getChannelSet().map(set -> {
            long mask = 0;
            for (int ch : set) {
                if (ch >= 0 && ch < 64) {
                    mask |= 1L << ch; // set bit at the channel number
                }
            }
            return mask;
        });
    }

    public Optional<String> getChannelMaskHex() {
        return toHex(getChannelMask(), 8);
    }

    public Optional<Integer> getPanId() {
        return codec.getUint16(TLV_PAN_ID);
    }

    public Optional<byte[]> getExtPanId() {
        return codec.getBytes(TLV_EXT_PAN_ID);
    }

    public Optional<String> getExtPanIdHex() {
        return getExtPanId().map(TlvCodec::bytesToHex);
    }

    public Optional<String> getNetworkName() {
        return codec.getBytes(TLV_NETWORK_NAME).map(String::new);
    }

    public Optional<byte[]> getNetworkKey() {
        return codec.getBytes(TLV_NETWORK_KEY);
    }

    public Optional<String> getNetworkKeyHex() {
        return getNetworkKey().map(TlvCodec::bytesToHex);
    }

    public Optional<byte[]> getPskc() {
        return codec.getBytes(TLV_PSKC);
    }

    public Optional<String> getPskcHex() {
        return getPskc().map(TlvCodec::bytesToHex);
    }

    public Optional<byte[]> getMeshLocalPrefix() {
        return codec.getBytes(TLV_MESH_LOCAL_PREFIX);
    }

    public Optional<String> getMeshLocalPrefixHex() {
        return getMeshLocalPrefix().map(TlvCodec::bytesToHex);
    }

    public Optional<String> getMeshLocalPrefixFormatted() {
        return getMeshLocalPrefix().map(pfx -> {
            String hex = TlvCodec.bytesToHex(pfx).toLowerCase(Locale.ROOT);
            return hex.substring(0, 4) + ":" + hex.substring(4, 8) + ":" + hex.substring(8, 12) + ":"
                    + hex.substring(12, 16) + "::/64";
        });
    }

    public Optional<Integer> getSecurityPolicyRotation() {
        return codec.getBytes(TLV_SECURITY_POLICY).map(b -> ByteBuffer.wrap(b).getShort(0) & 0xFFFF);
    }

    public Optional<Integer> getSecurityPolicyFlags() {
        return codec.getBytes(TLV_SECURITY_POLICY).map(b -> {
            if (b.length >= 4) {
                return (Byte.toUnsignedInt(b[2]) << 8) | Byte.toUnsignedInt(b[3]);
            } else {
                return Byte.toUnsignedInt(b[2]) << 8;
            }
        });
    }

    public Optional<String> getSecurityPolicyHex() {
        return codec.getBytes(TLV_SECURITY_POLICY).map(TlvCodec::bytesToHex);
    }

    public void setActiveTimestamp(long ts) {
        codec.putUint64(TLV_ACTIVE_TIMESTAMP, ts);
    }

    public void setActiveTimestamp(ThreadTimestamp ts) {
        setActiveTimestamp(ts.toLong());
    }

    public void setActiveTimestampAuthoritative(boolean authoritative) {
        long ts = Objects.requireNonNull(getActiveTimestamp().orElse(0L));
        if (authoritative) {
            ts |= 0x1L; // set bit 0
        } else {
            ts &= ~0x1L; // unset bit 0
        }
        setActiveTimestamp(ts);
    }

    public void setActiveTimestamp(String h) {
        setActiveTimestamp(Long.parseLong(TlvCodec.strip0x(h), 16));
    }

    public void setPendingTimestamp(long ts) {
        codec.putUint64(TLV_PENDING_TIMESTAMP, ts);
    }

    public void setPendingTimestamp(ThreadTimestamp ts) {
        setPendingTimestamp(ts.toLong());
    }

    public void setPendingTimestamp(String h) {
        setPendingTimestamp(Long.parseLong(TlvCodec.strip0x(h), 16));
    }

    public void setDelayTimer(long ms) {
        if (ms < MIN_DELAY_MS || ms > MAX_DELAY_MS) {
            throw new IllegalArgumentException(
                    "DelayTimer must be between 30000ms (30 seconds) and 259200000ms (72 hours)");
        }
        codec.putUint32(TLV_DELAY_TIMER, ms);
    }

    public void setDelayTimer(String h) {
        setDelayTimer(Long.parseLong(TlvCodec.strip0x(h), 16));
    }

    public void setChannel(int ch) {
        ByteBuffer bb = ByteBuffer.allocate(3);
        bb.put((byte) 0); // page 0 (2.4 GHz)
        bb.putShort((short) ch);
        codec.putBytes(TLV_CHANNEL, bb.array());
    }

    public void setChannelMask(long rawMask) {
        Set<Integer> channels = new TreeSet<>();
        for (int i = 0; i < 64; i++) {
            if (((rawMask >> i) & 1) != 0) {
                channels.add(i);
            }
        }
        setChannelSet(channels);
    }

    public void setPanId(int pan) {
        codec.putUint16(TLV_PAN_ID, pan);
    }

    public void setExtPanId(byte[] id) {
        codec.putBytes(TLV_EXT_PAN_ID, id, 8);
    }

    public void setExtPanId(String hex) {
        setExtPanId(TlvCodec.hexStringToBytes(fixLength(TlvCodec.strip0x(hex), 16)));
    }

    public void setNetworkName(String name) {
        codec.putBytes(TLV_NETWORK_NAME, name.getBytes());
    }

    public void setNetworkKey(byte[] key) {
        codec.putBytes(TLV_NETWORK_KEY, key, 16);
    }

    public void setNetworkKey(String hex) {
        setNetworkKey(TlvCodec.hexStringToBytes(fixLength(TlvCodec.strip0x(hex), 32)));
    }

    public void setPskc(byte[] key) {
        codec.putBytes(TLV_PSKC, key, 16);
    }

    public void setPskc(String hex) {
        setPskc(TlvCodec.hexStringToBytes(fixLength(TlvCodec.strip0x(hex), 32)));
    }

    public void setMeshLocalPrefix(byte[] pfx) {
        codec.putBytes(TLV_MESH_LOCAL_PREFIX, pfx, 8);
    }

    public void setMeshLocalPrefix(String hex) {
        setMeshLocalPrefix(TlvCodec.hexStringToBytes(fixLength(TlvCodec.strip0x(hex), 16)));
    }

    public void setMeshLocalPrefixFormatted(String formatted) {
        try {
            setMeshLocalPrefix(parsePrefix(formatted));
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid mesh local prefix: " + formatted, e);
        }
    }

    public void setSecurityPolicy(int rotationHours, int flags) {
        if (rotationHours < 2) {
            throw new IllegalArgumentException("RotationTime must be greater than 2 hours");
        }
        // Reserved bits 5‑3 MUST be 1
        int f = flags | SP_RSV;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putShort((short) rotationHours);
        bb.put((byte) (f >> 8)); // high‑order flag byte
        bb.put((byte) f); // low‑order byte (Rsv+VR)
        codec.putBytes(TLV_SECURITY_POLICY, bb.array());
    }

    public void setSecurityPolicy(String hex) {
        codec.putBytes(TLV_SECURITY_POLICY, TlvCodec.hexStringToBytes(TlvCodec.strip0x(hex)));
    }

    public boolean isObtainNetworkKey() {
        return hasFlag(SP_OBTAIN_NETWORK_KEY);
    }

    public void setObtainNetworkKey(boolean on) {
        setFlag(SP_OBTAIN_NETWORK_KEY, on);
    }

    public boolean isNativeCommissioning() {
        return hasFlag(SP_NATIVE_COMMISSIONING);
    }

    public void setNativeCommissioning(boolean on) {
        setFlag(SP_NATIVE_COMMISSIONING, on);
    }

    public boolean isRoutersEnabled() {
        return hasFlag(SP_ROUTERS);
    }

    public void setRoutersEnabled(boolean on) {
        setFlag(SP_ROUTERS, on);
    }

    public boolean isCommercialCommissioning() {
        return hasFlag(SP_COMMERCIAL_COMM);
    }

    public void setCommercialCommissioning(boolean on) {
        setFlag(SP_COMMERCIAL_COMM, on);
    }

    public boolean isExternalCommissioning() {
        return hasFlag(SP_EXTERNAL_COMM);
    }

    public void setExternalCommissioning(boolean on) {
        setFlag(SP_EXTERNAL_COMM, on);
    }

    public boolean isAutonomousEnrollment() {
        return hasFlag(SP_AUTO_ENROLL);
    }

    public void setAutonomousEnrollment(boolean on) {
        setFlag(SP_AUTO_ENROLL, on);
    }

    public boolean isNetworkKeyProvisioning() {
        return hasFlag(SP_NET_KEY_PROV);
    }

    public void setNetworkKeyProvisioning(boolean on) {
        setFlag(SP_NET_KEY_PROV, on);
    }

    public boolean isToBleLink() {
        return hasFlag(SP_TO_BLE_LINK);
    }

    public void setToBleLink(boolean on) {
        setFlag(SP_TO_BLE_LINK, on);
    }

    public boolean isNonCcmRouters() {
        return hasFlag(SP_NON_CCM_ROUTERS);
    }

    public void setNonCcmRouters(boolean on) {
        setFlag(SP_NON_CCM_ROUTERS, on);
    }

    public Optional<Set<Integer>> getChannelSet() {
        return codec.getBytes(TLV_CHANNEL_MASK).map(b -> {
            // TLV must be at least 3 bytes: page, maskLen, at least 1 mask byte
            if (b.length < 3) {
                return Set.of();
            }
            int page = Byte.toUnsignedInt(b[0]);
            int maskLen = Byte.toUnsignedInt(b[1]);
            // Must be exactly 2 + maskLen bytes, and maskLen 1..4
            if (maskLen < 1 || maskLen > 4 || b.length != 2 + maskLen) {
                return Set.of();
            }
            int baseChannel = pageToBaseChannel(page);
            int mask = 0;
            for (int i = 0; i < maskLen; i++) {
                mask |= (Byte.toUnsignedInt(b[2 + i]) << (8 * (maskLen - 1 - i)));
            }
            Set<Integer> channels = new TreeSet<>();
            for (int i = 0; i < 32; i++) {
                if (((mask >> i) & 1) != 0) {
                    channels.add(baseChannel + i);
                }
            }
            return channels;
        });
    }

    /**
     * Set the channel mask using a set of channel numbers.
     * The mask will be 4 bytes long, with the appropriate page and base channel.
     * Channels outside the 32-channel window for the page are ignored.
     */
    public void setChannelSet(Set<Integer> channels) {
        if (channels.isEmpty()) {
            return;
        }
        int min = channels.stream().mapToInt(Integer::intValue).min().getAsInt();
        int page = 0;
        int baseChannel = 11;
        if (min >= 33) {
            page = 2;
            baseChannel = 33;
        }
        if (min >= 45) {
            page = 4;
            baseChannel = 45;
        }
        if (min >= 51) {
            page = 5;
            baseChannel = 51;
        }
        if (min >= 63) {
            page = 6;
            baseChannel = 63;
        }

        int mask = 0;
        for (int ch : channels) {
            int bit = ch - baseChannel;
            if (bit >= 0 && bit < 32) {
                mask |= (1 << bit);
            }
        }

        ByteBuffer bb = ByteBuffer.allocate(6);
        bb.put((byte) page);
        bb.put((byte) 0x04); // mask length
        bb.putInt(mask);
        codec.putBytes(TLV_CHANNEL_MASK, bb.array());
    }

    /**
     * Convert the dataset to a hex string.
     * The hex string preserves the canonical order for the primary TLVs.
     * 
     * @return the hex string
     */
    public String toHex() {
        // canonical order for the 10 TLVs that appear in every Active dataset, matches the order a OTBR uses
        int[] canonical = { TLV_ACTIVE_TIMESTAMP, TLV_CHANNEL, TLV_CHANNEL_MASK, TLV_EXT_PAN_ID, TLV_MESH_LOCAL_PREFIX,
                TLV_NETWORK_KEY, TLV_NETWORK_NAME, TLV_PAN_ID, TLV_PSKC, TLV_SECURITY_POLICY };
        Set<Integer> canonicalSet = new HashSet<>();
        for (int t : canonical) {
            canonicalSet.add(t);
        }

        ByteBuffer out = ByteBuffer.allocate(1024);

        // loop through canonical TLVs first
        for (int type : canonical) {
            byte[] val = codec.getBytes(type).orElse(null);
            if (val != null) {
                if (val.length > 255) {
                    throw new IllegalArgumentException("TLV >255 B");
                }
                out.put((byte) type).put((byte) val.length).put(val);
            }
        }

        // loop through all others once, in ascending order
        codec.asMap().entrySet().stream().filter(e -> !canonicalSet.contains(e.getKey()))
                .sorted(Map.Entry.comparingByKey()).forEach(e -> {
                    byte[] val = e.getValue();
                    if (val.length > 255) {
                        throw new IllegalArgumentException("TLV >255 B");
                    }
                    out.put(e.getKey().byteValue()).put((byte) val.length).put(val);
                });

        return TlvCodec.bytesToHex(Arrays.copyOf(out.array(), out.position())).toUpperCase();
    }

    public static ThreadDataset fromHex(String hex) {
        Objects.requireNonNull(hex, "hex string");
        hex = hex.replaceAll("\\s+", "");
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Odd-length hex");
        }
        byte[] buf = TlvCodec.hexStringToBytes(hex);
        ThreadDataset ds = new ThreadDataset();
        for (int i = 0; i < buf.length;) {
            int type = Byte.toUnsignedInt(buf[i++]);
            int len = Byte.toUnsignedInt(buf[i++]);
            if (i + len > buf.length) {
                throw new IllegalArgumentException("TLV length exceeds buffer at type 0x" + Integer.toHexString(type));
            }
            ds.codec.putBytes(type, Arrays.copyOfRange(buf, i, i + len));
            i += len;
        }
        return ds;
    }

    public static @Nullable ThreadDataset fromJson(String json) {
        return GSON.fromJson(json, ThreadDataset.class);
    }

    /**
     * Generate a new secure master key.
     * 
     * @return the master key
     */
    public static byte[] generateMasterKey() throws NoSuchAlgorithmException {
        byte[] key = new byte[MK_KEY_LEN];
        SecureRandom.getInstanceStrong().nextBytes(key);
        return key;
    }

    public static byte[] generatePskc(String passPhrase, String networkName, String extPanIdHex)
            throws GeneralSecurityException {
        if (passPhrase.length() < 6 || passPhrase.length() > 255) {
            throw new IllegalArgumentException(
                    "Pass-phrase must be 6–255 characters: current length is " + passPhrase.length());
        }
        if (networkName.isEmpty() || networkName.length() > 16) {
            throw new IllegalArgumentException(
                    "Network name must be 1–16 characters: current length is " + networkName.length());
        }
        if (!extPanIdHex.matches("[0-9a-fA-F]{16}")) {
            throw new IllegalArgumentException(
                    "ExtPANID must be 16 hex chars: current length is " + extPanIdHex.length());
        }
        byte[] saltPrefix = "Thread".getBytes(StandardCharsets.US_ASCII);
        byte[] nameUpper = networkName.toUpperCase().getBytes(StandardCharsets.US_ASCII);
        byte[] extPanId = HexFormat.of().parseHex(extPanIdHex);
        byte[] salt = new byte[saltPrefix.length + nameUpper.length + extPanId.length];
        System.arraycopy(saltPrefix, 0, salt, 0, saltPrefix.length);
        System.arraycopy(nameUpper, 0, salt, saltPrefix.length, nameUpper.length);
        System.arraycopy(extPanId, 0, salt, saltPrefix.length + nameUpper.length, extPanId.length);
        PBEKeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), salt, PBKDF_ITERATIONS, PBKDF_KEY_LENGTH);
        SecretKeyFactory kf = SecretKeyFactory.getInstance(PBKDF_ALG);
        return kf.generateSecret(spec).getEncoded();
    }

    private int currentFlags() {
        return Objects.requireNonNull(getSecurityPolicyFlags().orElse(0));
    }

    private int currentRotation() {
        return Objects.requireNonNull(getSecurityPolicyRotation().orElse(672));
    }

    public void setSecurityPolicyRotation(int hours) {
        if (hours < 2) {
            throw new IllegalArgumentException("RotationTime must be greater than 2 hours");
        }
        setSecurityPolicy(hours, currentFlags());
    }

    private void setFlag(int bit, boolean on) {
        int flags = currentFlags();
        flags = on ? (flags | bit) : (flags & ~bit);
        setSecurityPolicy(currentRotation(), flags);
    }

    private boolean hasFlag(int bit) {
        return (currentFlags() & bit) != 0;
    }

    private static boolean present(@Nullable String s) {
        return s != null && !s.isBlank();
    }

    private static Optional<String> toHex(Optional<? extends Number> v, int width) {
        return v.map(x -> String.format("%0" + width + "X", x.longValue()));
    }

    private static String fixLength(String h, int chars) {
        if (h.length() != chars) {
            throw new IllegalArgumentException("Expected " + chars + " hex chars");
        }
        return h;
    }

    private static int pageToBaseChannel(int page) {
        return switch (page) {
            case 0 -> 11;
            case 2 -> 33;
            case 4 -> 45;
            case 5 -> 51;
            case 6 -> 63;
            default -> 0;
        };
    }

    private static byte[] parsePrefix(String text) throws IOException {
        String ip = text.split("/")[0];
        try {
            byte[] full = InetAddress.getByName(ip).getAddress();
            return Arrays.copyOf(full, 8);
        } catch (UnknownHostException e) {
            throw new IOException("Bad mesh-local prefix: " + text, e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        codec.asMap().forEach((type, value) -> {
            String hex = TlvCodec.bytesToHex(value);
            String typeName = switch (type) {
                case TLV_ACTIVE_TIMESTAMP -> TYPE_ACTIVE_TIMESTAMP;
                case TLV_PENDING_TIMESTAMP -> TYPE_PENDING_TIMESTAMP;
                case TLV_DELAY_TIMER -> TYPE_DELAY_TIMER;
                case TLV_CHANNEL -> TYPE_CHANNEL;
                case TLV_CHANNEL_MASK -> TYPE_CHANNEL_MASK;
                case TLV_PAN_ID -> TYPE_PAN_ID;
                case TLV_NETWORK_NAME -> TYPE_NETWORK_NAME;
                case TLV_NETWORK_KEY -> TYPE_NETWORK_KEY;
                case TLV_EXT_PAN_ID -> TYPE_EXT_PAN_ID;
                case TLV_MESH_LOCAL_PREFIX -> TYPE_MESH_LOCAL_PREFIX;
                case TLV_PSKC -> TYPE_PSKC;
                case TLV_SECURITY_POLICY -> TYPE_SECURITY_POLICY;
                default -> String.format("UnknownType(0x%02X)", type);
            };
            sb.append(String.format("  %-18s = %s\n", typeName, hex));
            if (type == TLV_SECURITY_POLICY) {
                sb.append(String.format("    %s = %d\n", PROP_ROTATION_TIME, getSecurityPolicyRotation().orElse(672)));
                sb.append(String.format("    %s = %s\n", PROP_OBTAIN_NETWORK_KEY, isObtainNetworkKey()));
                sb.append(String.format("    %s = %s\n", PROP_NATIVE_COMMISSIONING, isNativeCommissioning()));
                sb.append(String.format("    %s = %s\n", PROP_ROUTERS, isRoutersEnabled()));
                sb.append(String.format("    %s = %s\n", PROP_EXTERNAL_COMMISSIONING, isExternalCommissioning()));
                sb.append(String.format("    %s = %s\n", PROP_COMMERCIAL_COMMISSIONING, isCommercialCommissioning()));
                sb.append(String.format("    %s = %s\n", PROP_AUTONOMOUS_ENROLLMENT, isAutonomousEnrollment()));
                sb.append(String.format("    %s = %s\n", PROP_NETWORK_KEY_PROVISIONING, isNetworkKeyProvisioning()));
                sb.append(String.format("    %s = %s\n", PROP_TOBLE_LINK, isToBleLink()));
                sb.append(String.format("    %s = %s\n", PROP_NON_CCM_ROUTERS, isNonCcmRouters()));
            }
        });
        return sb.toString();
    }

    /**
     * Representation of a Thread timestamp.
     * <p>
     * The 64‑bit packed format is laid out as:
     *
     * <pre>
     * 63‥16 : seconds (48 bits)
     * 15‥1  : ticks   (15 bits, 1/32768 s)
     * 0     : authoritative flag
     * </pre>
     * 
     * @author Dan Cunningham - Initial contribution
     */
    public static class ThreadTimestamp {

        private long seconds;
        private int ticks;
        private boolean authoritative;

        /**
         * Build a timestamp from the individual components.
         * 
         * @param seconds
         * @param ticks
         * @param authoritative
         */
        public ThreadTimestamp(long seconds, int ticks, boolean authoritative) {
            if (seconds < 0 || seconds > 0x0000_FFFF_FFFF_FFFFL) {
                throw new IllegalArgumentException("Invalid seconds value: " + seconds);
            }
            if (ticks < 0 || ticks > 0x7FFF) {
                throw new IllegalArgumentException("Invalid ticks value: " + ticks);
            }
            setSeconds(seconds);
            setTicks(ticks);
            this.authoritative = authoritative;
        }

        /**
         * Factory for new timestamps based on the current time.
         * 
         * @param authoritative
         * @return
         */
        public static ThreadTimestamp now(boolean authoritative) {
            long millis = System.currentTimeMillis();
            long secs = (millis / 1000L) & 0x0000_FFFF_FFFF_FFFFL;
            int t = (int) (((millis % 1000L) * 32_768L) / 1000L) & 0x7FFF;
            return new ThreadTimestamp(secs, t, authoritative);
        }

        /**
         * Decode the timestamp from a long.
         * 
         * @param value
         * @return
         */
        public static ThreadTimestamp fromLong(long value) {
            long secs = (value >>> 16) & 0x0000_FFFF_FFFF_FFFFL;
            int t = (int) ((value >>> 1) & 0x7FFF);
            boolean auth = (value & 0x1L) != 0;
            return new ThreadTimestamp(secs, t, auth);
        }

        /**
         * Encode a timestamp to a long.
         * 
         * @return
         */
        public long toLong() {
            long v = (seconds & 0x0000_FFFF_FFFF_FFFFL) << 16;
            v |= ((long) (ticks & 0x7FFF)) << 1;
            if (authoritative) {
                v |= 0x1L;
            }
            return v;
        }

        /**
         * Get the seconds component of the timestamp.
         * 
         * @return
         */
        public long getSeconds() {
            return seconds;
        }

        /**
         * Set the seconds component of the timestamp.
         * 
         * @param seconds
         */
        public void setSeconds(long seconds) {
            this.seconds = seconds & 0x0000_FFFF_FFFF_FFFFL;
        }

        /**
         * Get the ticks component of the timestamp.
         * 
         * @return
         */
        public int getTicks() {
            return ticks;
        }

        /**
         * Set the ticks component of the timestamp.
         * 
         * @param ticks
         */
        public void setTicks(int ticks) {
            this.ticks = ticks & 0x7FFF;
        }

        /**
         * Get the authoritative flag of the timestamp.
         * An Authoritative timestamp is one based on a GPS, NTP, Cellular, or other precision time source.
         * 
         * @return
         */
        public boolean isAuthoritative() {
            return authoritative;
        }

        /**
         * Set the authoritative flag of the timestamp.
         * An Authoritative timestamp is one based on a GPS, NTP, Cellular, or other precision time source.
         * 
         * @param authoritative
         */
        public void setAuthoritative(boolean authoritative) {
            this.authoritative = authoritative;
        }

        @Override
        public String toString() {
            return String.format("ThreadTimestamp{seconds=%d, ticks=%d, authoritative=%s}", seconds, ticks,
                    authoritative);
        }
    }

    /**
     * GSON Type adapter for {@link ThreadDataset}.
     * 
     * @author Dan Cunningham - Initial contribution
     */
    static class ThreadDatasetAdapter extends TypeAdapter<ThreadDataset> {

        @Override
        public void write(@Nullable JsonWriter out, @Nullable ThreadDataset ds) throws IOException {
            if (out == null) {
                throw new IOException("JsonWriter is null");
            }
            if (ds == null) {
                out.nullValue();
                return;
            }
            out.beginObject();

            if (ds.getActiveTimestamp().isPresent()) {
                writeTimestamp(out, TYPE_ACTIVE_TIMESTAMP, ds.getActiveTimestamp().get());
            }

            if (ds.getPendingTimestamp().isPresent()) {
                writeTimestamp(out, TYPE_PENDING_TIMESTAMP, ds.getPendingTimestamp().get());
            }

            intOrLong(out, TYPE_DELAY_TIMER, ds.getDelayTimer());
            string(out, TYPE_NETWORK_KEY, ds.getNetworkKeyHex());
            string(out, TYPE_NETWORK_NAME, ds.getNetworkName());
            string(out, TYPE_EXT_PAN_ID, ds.getExtPanIdHex());
            intOrLong(out, TYPE_PAN_ID, ds.getPanId());
            intOrLong(out, TYPE_CHANNEL, ds.getChannel());
            string(out, TYPE_PSKC, ds.getPskcHex());
            intOrLong(out, TYPE_CHANNEL_MASK, ds.getChannelMask());

            if (ds.getMeshLocalPrefix().isPresent()) {
                string(out, TYPE_MESH_LOCAL_PREFIX, ds.getMeshLocalPrefixFormatted());
            }

            if (ds.getSecurityPolicyRotation().isPresent() || ds.getSecurityPolicyFlags().isPresent()) {
                out.name(TYPE_SECURITY_POLICY).beginObject();
                out.name(PROP_ROTATION_TIME).value(ds.getSecurityPolicyRotation().orElse(672));
                out.name(PROP_OBTAIN_NETWORK_KEY).value(ds.isObtainNetworkKey());
                out.name(PROP_NATIVE_COMMISSIONING).value(ds.isNativeCommissioning());
                out.name(PROP_ROUTERS).value(ds.isRoutersEnabled());
                out.name(PROP_EXTERNAL_COMMISSIONING).value(ds.isExternalCommissioning());
                out.name(PROP_COMMERCIAL_COMMISSIONING).value(ds.isCommercialCommissioning());
                out.name(PROP_AUTONOMOUS_ENROLLMENT).value(ds.isAutonomousEnrollment());
                out.name(PROP_NETWORK_KEY_PROVISIONING).value(ds.isNetworkKeyProvisioning());
                out.name(PROP_TOBLE_LINK).value(ds.isToBleLink());
                out.name(PROP_NON_CCM_ROUTERS).value(ds.isNonCcmRouters());
                out.endObject();
            }
            out.endObject();
        }

        @Override
        public @Nullable ThreadDataset read(@Nullable JsonReader in) throws IOException {
            if (in == null) {
                throw new IOException("JsonReader is null");
            }
            ThreadDataset ds = new ThreadDataset();

            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case TYPE_ACTIVE_TIMESTAMP -> readTimestamp(in, ds::setActiveTimestamp);
                    case TYPE_PENDING_TIMESTAMP -> readTimestamp(in, ds::setPendingTimestamp);
                    case TYPE_DELAY_TIMER -> ds.setDelayTimer(in.nextLong());
                    case TYPE_NETWORK_KEY -> ds.setNetworkKey(in.nextString());
                    case TYPE_NETWORK_NAME -> ds.setNetworkName(in.nextString());
                    case TYPE_EXT_PAN_ID -> ds.setExtPanId(in.nextString());
                    case TYPE_MESH_LOCAL_PREFIX -> ds.setMeshLocalPrefix(parsePrefix(in.nextString()));
                    case TYPE_PAN_ID -> ds.setPanId(in.nextInt());
                    case TYPE_CHANNEL -> ds.setChannel(in.nextInt());
                    case TYPE_PSKC -> ds.setPskc(in.nextString());
                    case TYPE_CHANNEL_MASK -> ds.setChannelMask(in.nextLong());
                    case TYPE_SECURITY_POLICY -> readSecurityPolicy(in, ds);
                    default -> in.skipValue();
                }
            }
            in.endObject();
            return ds;
        }

        private static void writeTimestamp(JsonWriter out, String name, long ts) throws IOException {
            out.name(name).beginObject();
            out.name(PROP_SECONDS).value((ts >>> 16) & 0x0000_FFFF_FFFF_FFFFL);
            out.name(PROP_TICKS).value((ts >>> 1) & 0x7FFF);
            out.name(PROP_AUTHORITATIVE).value((ts & 0x1L) != 0);
            out.endObject();
        }

        private static void readTimestamp(JsonReader in, java.util.function.LongConsumer setter) throws IOException {
            long seconds = 0;
            int ticks = 0;
            boolean auth = false;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case PROP_SECONDS -> seconds = in.nextLong();
                    case PROP_TICKS -> ticks = in.nextInt();
                    case PROP_AUTHORITATIVE -> auth = in.nextBoolean();
                    default -> in.skipValue();
                }
            }
            in.endObject();
            long ts = (seconds & 0x0000_FFFF_FFFF_FFFFL) << 16;
            ts |= ((long) ticks & 0x7FFFL) << 1;
            if (auth) {
                ts |= 0x1L;
            }
            setter.accept(ts);
        }

        private static void readSecurityPolicy(JsonReader in, ThreadDataset ds) throws IOException {
            int rotation = 672;
            int flags = 0;

            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case PROP_ROTATION_TIME -> rotation = in.nextInt();

                    case PROP_OBTAIN_NETWORK_KEY -> {
                        if (in.nextBoolean()) {
                            flags |= SP_OBTAIN_NETWORK_KEY;
                        }
                    }
                    case PROP_NATIVE_COMMISSIONING -> {
                        if (in.nextBoolean()) {
                            flags |= SP_NATIVE_COMMISSIONING;
                        }
                    }
                    case PROP_ROUTERS -> {
                        if (in.nextBoolean()) {
                            flags |= SP_ROUTERS;
                        }
                    }
                    case PROP_EXTERNAL_COMMISSIONING -> {
                        if (in.nextBoolean()) {
                            flags |= SP_EXTERNAL_COMM;
                        }
                    }
                    case PROP_COMMERCIAL_COMMISSIONING -> {
                        if (in.nextBoolean()) {
                            flags |= SP_COMMERCIAL_COMM;
                        }
                    }
                    case PROP_AUTONOMOUS_ENROLLMENT -> {
                        if (in.nextBoolean()) {
                            flags |= SP_AUTO_ENROLL;
                        }
                    }
                    case PROP_NETWORK_KEY_PROVISIONING -> {
                        if (in.nextBoolean()) {
                            flags |= SP_NET_KEY_PROV;
                        }
                    }
                    case PROP_TOBLE_LINK -> {
                        if (in.nextBoolean()) {
                            flags |= SP_TO_BLE_LINK;
                        }
                    }
                    case PROP_NON_CCM_ROUTERS -> {
                        if (in.nextBoolean()) {
                            flags |= SP_NON_CCM_ROUTERS;
                        }
                    }
                    case "RawFlags" -> flags = Integer.parseInt(in.nextString(), 16);

                    default -> in.skipValue();
                }
            }
            in.endObject();

            // ensure reserved bits are set
            flags |= SP_RSV;
            ds.setSecurityPolicy(rotation, flags);
        }

        private static void intOrLong(JsonWriter out, String name, Optional<? extends Number> n) throws IOException {
            if (n.isPresent()) {
                out.name(name);
                out.value(n.get());
            }
        }

        private static void string(JsonWriter out, String name, Optional<String> s) throws IOException {
            if (s.isPresent()) {
                out.name(name);
                out.value(s.get());
            }
        }
    }
}
