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
package org.openhab.binding.sony.internal.scalarweb.models;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.SonyUtil;

/**
 * This class represents a web scalar method definition that can be called
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebMethod {

    // The following are various method names that can be used

    // Supported by all services
    public static final String GETVERSIONS = "getVersions";
    public static final String GETMETHODTYPES = "getMethodTypes";

    // Only AccessControl service
    public static final String ACTREGISTER = "actRegister";

    // Only AppControl service
    public static final String GETAPPLICATIONLIST = "getApplicationList";
    public static final String GETAPPLICATIONSTATUSLIST = "getApplicationStatusList";
    public static final String GETTEXTFORM = "getTextForm";
    public static final String GETWEBAPPSTATUS = "getWebAppStatus";
    public static final String SETACTIVEAPP = "setActiveApp";
    public static final String SETTEXTFORM = "setTextForm";
    public static final String TERMINATEAPPS = "terminateApps";

    // Only Audio service
    public static final String GETCUSTOMEQUALIZERSETTINGS = "getCustomEqualizerSettings";
    public static final String GETSOUNDSETTINGS = "getSoundSettings";
    public static final String GETSPEAKERSETTINGS = "getSpeakerSettings";
    public static final String GETVOLUMEINFORMATION = "getVolumeInformation";
    public static final String SETAUDIOMUTE = "setAudioMute";
    public static final String SETAUDIOVOLUME = "setAudioVolume";
    public static final String SETCUSTOMEQUALIZERSETTINGS = "setCustomEqualizerSettings";
    public static final String SETSOUNDSETTINGS = "setSoundSettings";
    public static final String SETSPEAKERSETTINGS = "setSpeakerSettings";

    // Only AvContent service
    public static final String DELETECOUNT = "deleteContent";
    public static final String GETBLUETOOTHSETTINGS = "getBluetoothSettings";
    public static final String GETCONTENTCOUNT = "getContentCount";
    public static final String GETCONTENTLIST = "getContentList";
    public static final String GETCURRENTEXTERNALINPUTSSTATUS = "getCurrentExternalInputsStatus";
    public static final String GETCURRENTEXTERNALTERMINALSSTATUS = "getCurrentExternalTerminalsStatus";
    public static final String GETFAVORITELIST = "getFavoriteList"; // TODO - figure out
    public static final String GETPARENTALRATINGSETTINGS = "getParentalRatingSettings";
    public static final String GETPLAYBACKMODESETTINGS = "getPlaybackModeSettings";
    public static final String GETPLAYINGCONTENTINFO = "getPlayingContentInfo";
    public static final String GETSCHEMELIST = "getSchemeList";
    public static final String GETSOURCELIST = "getSourceList";
    public static final String PAUSEPLAYINGCONTENT = "pausePlayingContent";
    public static final String PRESETBROADCASTSTATION = "presetBroadcastStation";
    public static final String SCANPLAYINGCONTENT = "scanPlayingContent";
    public static final String SEEKBROADCASTSTATION = "seekBroadcastStation";
    public static final String SETACTIVETERMINAL = "setActiveTerminal";
    public static final String SETBLUETOOTHSETTINGS = "setBluetoothSettings";
    public static final String SETDELETEPROTECTION = "setDeleteProtection";
    public static final String SETFAVORITECONTENTLIST = "setFavoriteContentList";// TODO - figure out
    public static final String SETPLAYBACKMODESETTINGS = "setPlaybackModeSettings";
    public static final String SETPLAYCONTENT = "setPlayContent";
    public static final String SETPLAYNEXTCONTENT = "setPlayNextContent";
    public static final String SETPLAYPREVIOUSCONTENT = "setPlayPreviousContent";
    public static final String SETTVCONTENTVISIBILITY = "setTvContentVisibility";
    public static final String STOPPLAYINGCONTENT = "stopPlayingContent";

    // Only CEC service
    public static final String SETCECCONTROLMODE = "setCecControlMode";
    public static final String SETMHLAUTOINPUTCHANGEMODE = "setMhlAutoInputChangeMode";
    public static final String SETMHLPOWERFEEDMODE = "setMhlPowerFeedMode";
    public static final String SETPOWERSYNCMODE = "setPowerSyncMode";

    // Only Encryption service
    public static final String GETPUBLICKEY = "getPublicKey";

    // Only Browser service
    public static final String ACTIVATEBROWSERCONTROL = "actBrowserControl";
    public static final String GETBROWSERBOOKMARKLIST = "getBrowserBookmarkList";
    public static final String GETTEXTURL = "getTextUrl";
    public static final String SETTEXTURL = "setTextUrl";

    // Only Illumination service
    public static final String GETILLUMNATIONSETTING = "getIlluminationSettings";
    public static final String SETILLUMNATIONSETTING = "setIlluminationSettings";

    // Only Guide service
    public static final String GETSERVICEPROTOCOLS = "getServiceProtocols";
    public static final String GETSUPPORTEDAPIINFO = "getSupportedApiInfo";

    // Only System service
    public static final String GETCURRENTTIME = "getCurrentTime";
    public static final String GETDEVICEMISCSETTINGS = "getDeviceMiscSettings";
    public static final String GETDEVICEMODE = "getDeviceMode";
    public static final String GETINTERFACEINFORMATION = "getInterfaceInformation";
    public static final String GETLEDINDICATORSTATUS = "getLEDIndicatorStatus";
    public static final String GETNETWORKSETTINGS = "getNetworkSettings";
    public static final String GETPOSTALCODE = "getPostalCode";
    public static final String GETPOWERSAVINGMODE = "getPowerSavingMode";
    public static final String GETPOWERSETTINGS = "getPowerSettings";
    public static final String GETPOWERSTATUS = "getPowerStatus";
    public static final String GETREMOTECONTROLLERINFO = "getRemoteControllerInfo";
    public static final String GETREMOTEDEVICESETTINGS = "getRemoteDeviceSettings";
    public static final String GETSLEEPTIMERSETTINGS = "getSleepTimerSettings";
    public static final String GETSTORAGELIST = "getStorageList"; // TODO- figure out
    public static final String GETSYSTEMINFORMATION = "getSystemInformation";
    public static final String GETSYSTEMSUPPORTEDFUNCTION = "getSystemSupportedFunction"; // TODO- figure out
    public static final String GETWOLMODE = "getWolMode";
    public static final String GETWUTANGINFO = "getWuTangInfo";
    public static final String MOUNTSTORAGE = "mountStorage"; // TODO- figure out
    public static final String REQUESTREBOOT = "requestReboot";
    public static final String SETDEVICEMISSETTINGS = "setDeviceMiscSettings";
    public static final String SETDEVICEMODE = "setDeviceMode";
    public static final String SETLANGUAGE = "setLanguage";
    public static final String SETLEDINDICATORSTATUS = "setLEDIndicatorStatus";
    public static final String SETPOSTALCODE = "setPostalCode";
    public static final String SETPOWERSAVINGMODE = "setPowerSavingMode";
    public static final String SETPOWERSETTINGS = "setPowerSettings";
    public static final String SETPOWERSTATUS = "setPowerStatus";
    public static final String SETSLEEPTIMERSETTINGS = "setSleepTimerSettings";
    public static final String SETWOLMODE = "setWolMode";
    public static final String SETWUTANGINFO = "setWuTangInfo";
    public static final String SWITCHNOTIFICATIONS = "switchNotifications";

    // Only Video Screen service
    public static final String GETAUDIOSOURCESCREEN = "getAudioSourceScreen";
    public static final String GETBANNERMODE = "getBannerMode";
    public static final String GETMULTISCREENMODE = "getMultiScreenMode";
    public static final String GETPICTUREQUALITYSETTINGS = "getPictureQualitySettings";
    public static final String GETPIPSUBSCREENPOSITION = "getPipSubScreenPosition";
    public static final String GETSCENESETTING = "getSceneSetting";
    public static final String SETAUDIOSOURCESCREEN = "setAudioSourceScreen";
    public static final String SETBANNERMODE = "setBannerMode";
    public static final String SETMULTISCREENMODE = "setMultiScreenMode";
    public static final String SETPICTUREQUALITYSETTINGS = "setPictureQualitySettings";
    public static final String SETPIPSUBSCREENPOSITION = "setPipSubScreenPosition";
    public static final String SETSCENESETTING = "setSceneSetting";

    // Various versions
    public static final String V1_0 = "1.0";
    public static final String V1_1 = "1.1";
    public static final String V1_2 = "1.2";
    public static final String V1_3 = "1.3";
    public static final String V1_4 = "1.4";
    public static final String V1_5 = "1.5";

    public static final int UNKNOWN_VARIATION = -1;

    /** The method name */
    private final String methodName;

    /** The parameters for the method (unmodifiable) */
    private final List<String> parms;

    /** The return values for the method (unmodifiable) */
    private final List<String> retVals;

    /** The method version */
    private final String version;

    /** The method version */
    private final int variation;

    // The comparator to compare scalar web methods
    public static final Comparator<ScalarWebMethod> COMPARATOR = Comparator
            .comparing((final ScalarWebMethod e) -> e.getMethodName()).thenComparing(e -> e.getVersion());

    /**
     * Instantiates a new scalar web method base on the parameters
     *
     * @param methodName the non-null, non-empty method name
     * @param parms the non-null, possibly empty parameter names
     * @param retVals the non-null, possibly empty return values
     * @param version the non-null, non-empty method version
     */
    public ScalarWebMethod(final String methodName, final List<String> parms, final List<String> retVals,
            final String version) {
        this(methodName, parms, retVals, version, 0);
    }

    /**
     * Instantiates a new scalar web method base on the parameters
     *
     * @param methodName the non-null, non-empty method name
     * @param parms the non-null, possibly empty parameter names
     * @param retVals the non-null, possibly empty return values
     * @param version the non-null, non-empty method version
     * @param variation the variation of the method
     */
    public ScalarWebMethod(final String methodName, final List<String> parms, final List<String> retVals,
            final String version, final int variation) {
        Validate.notEmpty(methodName, "methodName cannot be empty");
        Objects.requireNonNull(parms, "parms cannot be null");
        Objects.requireNonNull(retVals, "retVals cannot be null");
        Validate.notEmpty(version, "getVolumeInformationersion cannot be empty");

        this.methodName = methodName;
        this.parms = Collections
                .unmodifiableList(parms.stream().map(s -> StringUtils.trim(s)).collect(Collectors.toList()));
        this.retVals = Collections
                .unmodifiableList(retVals.stream().map(s -> StringUtils.trim(s)).collect(Collectors.toList()));
        this.version = version;
        this.variation = variation;
    }

    /**
     * Gets the method name
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Gets the method parameters
     *
     * @return the non-null, possibly empty (unmodifiable) list of parameters
     */
    public List<String> getParms() {
        return parms;
    }

    /**
     * Gets the return values
     *
     * @return the non-null, possibly empty (unmodifiable) list of return values
     */
    public List<String> getRetVals() {
        return retVals;
    }

    /**
     * Gets the method version
     *
     * @return the method version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the method version variation
     *
     * @return the method version variation
     */
    public int getVariation() {
        return variation;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(100);

        sb.append(getMethodName());
        sb.append("[");
        sb.append(getVersion());
        if (variation > 0) {
            sb.append("-");
            sb.append(getVariation());
        }
        sb.append("](");
        sb.append(StringUtils.join(parms, ','));
        sb.append("): ");
        sb.append(StringUtils.join(retVals, ','));

        return sb.toString();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof ScalarWebMethod)) {
            return false;
        }

        final ScalarWebMethod other = (ScalarWebMethod) obj;
        return StringUtils.equalsIgnoreCase(methodName, other.methodName)
                && StringUtils.equalsIgnoreCase(version, other.version)
                && SonyUtil.equalsIgnoreCase(new HashSet<>(parms), new HashSet<>(other.parms))
                && SonyUtil.equalsIgnoreCase(new HashSet<>(retVals), new HashSet<>(other.retVals))
                && variation == ((ScalarWebMethod) obj).variation;
    }
}
