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
package org.openhab.binding.monopriceaudio.internal.communication;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.monopriceaudio.internal.configuration.MonopriceAudioThingConfiguration;
import org.openhab.binding.monopriceaudio.internal.dto.MonopriceAudioZoneDTO;
import org.openhab.core.types.StateOption;

/**
 * The {@link AmplifierModel} is responsible for mapping low level communications for each supported amplifier model.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public enum AmplifierModel {

    // Monoprice 10761/Dayton Audio DAX66
    MONOPRICE("<", "\r", "?", "", "#>", "PR", "CH", "VO", "MU", "TR", "BS", "BL", "DT", 38, -7, 7, 7, -10, 10, 10, 18,
            6, true, List.of("11", "12", "13", "14", "15", "16", "21", "22", "23", "24", "25", "26", "31", "32", "33",
                    "34", "35", "36")) {
        @Override
        public MonopriceAudioZoneDTO getZoneData(String newZoneData) {
            return getMonopriceZoneData(newZoneData);
        }

        @Override
        public List<StateOption> getSourceLabels(MonopriceAudioThingConfiguration config) {
            return List.of(new StateOption("1", config.inputLabel1), new StateOption("2", config.inputLabel2),
                    new StateOption("3", config.inputLabel3), new StateOption("4", config.inputLabel4),
                    new StateOption("5", config.inputLabel5), new StateOption("6", config.inputLabel6));
        }
    },
    // Monoprice 44519 4 zone variant
    MONOPRICE4("<", "\r", "?", "", "#>", "PR", "CH", "VO", "MU", "TR", "BS", "BL", "DT", 38, -7, 7, 7, -10, 10, 10, 12,
            6, true, List.of("11", "12", "13", "14", "21", "22", "23", "24", "31", "32", "33", "34")) {
        @Override
        public MonopriceAudioZoneDTO getZoneData(String newZoneData) {
            return getMonopriceZoneData(newZoneData);
        }

        @Override
        public List<StateOption> getSourceLabels(MonopriceAudioThingConfiguration config) {
            return List.of(new StateOption("1", config.inputLabel1), new StateOption("2", config.inputLabel2),
                    new StateOption("3", config.inputLabel3), new StateOption("4", config.inputLabel4),
                    new StateOption("5", config.inputLabel5), new StateOption("6", config.inputLabel6));
        }
    },
    // Dayton Audio DAX88
    DAX88("<", "\r", "?", "", ">", "PR", "CH", "VO", "MU", "TR", "BS", "BL", "DT", 38, -12, 12, 12, -10, 10, 10, 8, 8,
            true, List.of("01", "02", "03", "04", "05", "06", "07", "08")) {
        @Override
        public MonopriceAudioZoneDTO getZoneData(String newZoneData) {
            return getMonopriceZoneData(newZoneData);
        }

        @Override
        public List<StateOption> getSourceLabels(MonopriceAudioThingConfiguration config) {
            return List.of(new StateOption("1", config.inputLabel1), new StateOption("2", config.inputLabel2),
                    new StateOption("3", config.inputLabel3), new StateOption("4", config.inputLabel4),
                    new StateOption("5", config.inputLabel5), new StateOption("6", config.inputLabel6),
                    new StateOption("7", config.inputLabel7), new StateOption("8", config.inputLabel8));
        }
    },
    MONOPRICE70("!", "+\r", "?", "ZS", "?", "PR", "IS", "VO", "MU", "TR", "BS", "BA", "", 38, -7, 7, 7, -32, 31, 32, 6,
            2, false, List.of("1", "2", "3", "4", "5", "6")) {
        @Override
        public MonopriceAudioZoneDTO getZoneData(String newZoneData) {
            final MonopriceAudioZoneDTO zoneData = new MonopriceAudioZoneDTO();

            Matcher matcher = MONOPRICE70_PATTERN.matcher(newZoneData);
            if (matcher.find()) {
                zoneData.setZone(matcher.group(1));
                zoneData.setVolume(Integer.parseInt(matcher.group(2)));
                zoneData.setPower(matcher.group(3));
                zoneData.setMute(matcher.group(4));
                zoneData.setSource(matcher.group(5));
                return zoneData;
            }

            matcher = MONOPRICE70_TREB_PATTERN.matcher(newZoneData);
            if (matcher.find()) {
                zoneData.setZone(matcher.group(1));
                zoneData.setTreble(Integer.parseInt(matcher.group(2)));
                return zoneData;
            }

            matcher = MONOPRICE70_BASS_PATTERN.matcher(newZoneData);
            if (matcher.find()) {
                zoneData.setZone(matcher.group(1));
                zoneData.setBass(Integer.parseInt(matcher.group(2)));
                return zoneData;
            }

            matcher = MONOPRICE70_BALN_PATTERN.matcher(newZoneData);
            if (matcher.find()) {
                zoneData.setZone(matcher.group(1));
                zoneData.setBalance(Integer.parseInt(matcher.group(2)));
                return zoneData;
            }

            return zoneData;
        }

        @Override
        public List<StateOption> getSourceLabels(MonopriceAudioThingConfiguration config) {
            return List.of(new StateOption("0", config.inputLabel1), new StateOption("1", config.inputLabel2));
        }
    },
    XANTECH("!", "+\r", "?", "ZD", "#", "PR", "SS", "VO", "MU", "TR", "BS", "BL", "", 38, -7, 7, 7, -32, 31, 32, 16, 8,
            false, List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16")) {
        @Override
        public MonopriceAudioZoneDTO getZoneData(String newZoneData) {
            final MonopriceAudioZoneDTO zoneData = new MonopriceAudioZoneDTO();
            final Matcher matcher = XANTECH_PATTERN.matcher(newZoneData);

            if (matcher.find()) {
                zoneData.setZone(matcher.group(1));
                zoneData.setPower(matcher.group(2));
                zoneData.setSource(matcher.group(3));
                zoneData.setVolume(Integer.parseInt(matcher.group(4)));
                zoneData.setMute(matcher.group(5));
                zoneData.setTreble(Integer.parseInt(matcher.group(6)));
                zoneData.setBass(Integer.parseInt(matcher.group(7)));
                zoneData.setBalance(Integer.parseInt(matcher.group(8)));
                zoneData.setKeypad(matcher.group(9));
                zoneData.setPage(matcher.group(10));
            }
            return zoneData;
        }

        @Override
        public List<StateOption> getSourceLabels(MonopriceAudioThingConfiguration config) {
            return List.of(new StateOption("1", config.inputLabel1), new StateOption("2", config.inputLabel2),
                    new StateOption("3", config.inputLabel3), new StateOption("4", config.inputLabel4),
                    new StateOption("5", config.inputLabel5), new StateOption("6", config.inputLabel6),
                    new StateOption("7", config.inputLabel7), new StateOption("8", config.inputLabel8));
        }
    };

    // Used by 10761/DAX66 and DAX88
    private static MonopriceAudioZoneDTO getMonopriceZoneData(String newZoneData) {
        final MonopriceAudioZoneDTO zoneData = new MonopriceAudioZoneDTO();
        final Matcher matcher = MONOPRICE_PATTERN.matcher(newZoneData);

        if (matcher.find()) {
            zoneData.setZone(matcher.group(1));
            zoneData.setPage(matcher.group(2));
            zoneData.setPower(matcher.group(3));
            zoneData.setMute(matcher.group(4));
            zoneData.setDnd(matcher.group(5));
            zoneData.setVolume(Integer.parseInt(matcher.group(6)));
            zoneData.setTreble(Integer.parseInt(matcher.group(7)));
            zoneData.setBass(Integer.parseInt(matcher.group(8)));
            zoneData.setBalance(Integer.parseInt(matcher.group(9)));
            zoneData.setSource(matcher.group(10));
            zoneData.setKeypad(matcher.group(11));
        }
        return zoneData;
    }

    // Monoprice 10761/DAX66 status string: #>1200010000130809100601
    // DAX88 status string is the same but does not have leading '#': >xxaabbccddeeffgghhiijj
    private static final Pattern MONOPRICE_PATTERN = Pattern
            .compile("^#?>(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})");

    // Monoprice 31028 / PAM1270 status string: ?6ZS VO8 PO1 MU0 IS0+ (does not include treble, bass & balance)
    private static final Pattern MONOPRICE70_PATTERN = Pattern
            .compile("^\\?(\\d{1})ZS VO(\\d{1,2}) PO(\\d{1}) MU(\\d{1}) IS(\\d{1})+");
    private static final Pattern MONOPRICE70_TREB_PATTERN = Pattern.compile("^\\?(\\d{1})TR(\\d{1,2})+");
    private static final Pattern MONOPRICE70_BASS_PATTERN = Pattern.compile("^\\?(\\d{1})BS(\\d{1,2})+");
    private static final Pattern MONOPRICE70_BALN_PATTERN = Pattern.compile("^\\?(\\d{1})BA(\\d{1,2})+");

    // Xantech status string: #1ZS PR0 SS1 VO0 MU1 TR7 BS7 BA32 LS0 PS0+
    private static final Pattern XANTECH_PATTERN = Pattern.compile(
            "^#(\\d{1,2})ZS PR(\\d{1}) SS(\\d{1}) VO(\\d{1,2}) MU(\\d{1}) TR(\\d{1,2}) BS(\\d{1,2}) BA(\\d{1,2}) LS(\\d{1}) PS(\\d{1})+");

    private final String cmdPrefix;
    private final String cmdSuffix;
    private final String queryPrefix;
    private final String querySuffix;
    private final String respPrefix;
    private final String powerCmd;
    private final String sourceCmd;
    private final String volumeCmd;
    private final String muteCmd;
    private final String trebleCmd;
    private final String bassCmd;
    private final String balanceCmd;
    private final String dndCmd;
    private final int maxVol;
    private final int minTone;
    private final int maxTone;
    private final int toneOffset;
    private final int minBal;
    private final int maxBal;
    private final int balOffset;
    private final int maxZones;
    private final int numSources;
    private final boolean padNumbers;
    private final List<String> zoneIds;
    private final Map<String, String> zoneIdMap;

    private static final String ON_STR = "1";
    private static final String OFF_STR = "0";

    private static final String ON_STR_PAD = "01";
    private static final String OFF_STR_PAD = "00";

    /**
     * Constructor for all the enum parameters
     *
     **/
    AmplifierModel(String cmdPrefix, String cmdSuffix, String queryPrefix, String querySuffix, String respPrefix,
            String powerCmd, String sourceCmd, String volumeCmd, String muteCmd, String trebleCmd, String bassCmd,
            String balanceCmd, String dndCmd, int maxVol, int minTone, int maxTone, int toneOffset, int minBal,
            int maxBal, int balOffset, int maxZones, int numSources, boolean padNumbers, List<String> zoneIds) {
        this.cmdPrefix = cmdPrefix;
        this.cmdSuffix = cmdSuffix;
        this.queryPrefix = queryPrefix;
        this.querySuffix = querySuffix;
        this.respPrefix = respPrefix;
        this.powerCmd = powerCmd;
        this.sourceCmd = sourceCmd;
        this.volumeCmd = volumeCmd;
        this.muteCmd = muteCmd;
        this.trebleCmd = trebleCmd;
        this.bassCmd = bassCmd;
        this.balanceCmd = balanceCmd;
        this.dndCmd = dndCmd;
        this.maxVol = maxVol;
        this.minTone = minTone;
        this.maxTone = maxTone;
        this.toneOffset = toneOffset;
        this.minBal = minBal;
        this.maxBal = maxBal;
        this.balOffset = balOffset;
        this.maxZones = maxZones;
        this.numSources = numSources;
        this.padNumbers = padNumbers;
        this.zoneIds = zoneIds;

        // Build a map between the amp's physical zone IDs and the thing's logical zone names
        final Map<String, String> zoneIdMap = new HashMap<>();
        IntStream.range(0, zoneIds.size()).forEach(i -> zoneIdMap.put(zoneIds.get(i), "zone" + (i + 1)));
        this.zoneIdMap = Collections.unmodifiableMap(zoneIdMap);
    }

    public abstract MonopriceAudioZoneDTO getZoneData(String newZoneData);

    public abstract List<StateOption> getSourceLabels(MonopriceAudioThingConfiguration config);

    public String getZoneIdFromZoneName(String zoneName) {
        for (String zoneId : zoneIdMap.keySet()) {
            if (zoneName.equals(zoneIdMap.get(zoneId))) {
                return zoneId;
            }
        }
        return "";
    }

    public String getZoneName(String zoneId) {
        final String zoneName = zoneIdMap.get(zoneId);
        return zoneName != null ? zoneName : "";
    }

    public String getCmdPrefix() {
        return cmdPrefix;
    }

    public String getQueryPrefix() {
        return queryPrefix;
    }

    public String getQuerySuffix() {
        return querySuffix;
    }

    public String getRespPrefix() {
        return respPrefix;
    }

    public String getPowerCmd() {
        return powerCmd;
    }

    public String getSourceCmd() {
        return sourceCmd;
    }

    public String getVolumeCmd() {
        return volumeCmd;
    }

    public String getMuteCmd() {
        return muteCmd;
    }

    public String getTrebleCmd() {
        return trebleCmd;
    }

    public String getBassCmd() {
        return bassCmd;
    }

    public String getBalanceCmd() {
        return balanceCmd;
    }

    public String getDndCmd() {
        return dndCmd;
    }

    public int getMaxVol() {
        return maxVol;
    }

    public int getMinTone() {
        return minTone;
    }

    public int getMaxTone() {
        return maxTone;
    }

    public int getMinBal() {
        return minBal;
    }

    public int getMaxBal() {
        return maxBal;
    }

    public int getBalOffset() {
        return balOffset;
    }

    public int getToneOffset() {
        return toneOffset;
    }

    public int getMaxZones() {
        return maxZones;
    }

    public int getNumSources() {
        return numSources;
    }

    public String getCmdSuffix() {
        return cmdSuffix;
    }

    public List<String> getZoneIds() {
        return zoneIds;
    }

    public String getFormattedValue(Integer value) {
        return padNumbers ? String.format("%02d", value) : value.toString();
    }

    public String getOnStr() {
        return padNumbers ? ON_STR_PAD : ON_STR;
    }

    public String getOffStr() {
        return padNumbers ? OFF_STR_PAD : OFF_STR;
    }
}
