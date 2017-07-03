/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebMethod.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ScalarWebMethod {

    /** The Constant GetVersions. */
    // Common to all service
    public final static String GetVersions = "getVersions";

    /** The Constant GetMethodTypes. */
    public final static String GetMethodTypes = "getMethodTypes";

    /** The Constant ActRegister. */
    // Only AccessControl
    public final static String ActRegister = "actRegister";

    /** The Constant GetApplicationList. */
    // Only AppControl
    public final static String GetApplicationList = "getApplicationList";

    /** The Constant GetApplicationStatusList. */
    public final static String GetApplicationStatusList = "getApplicationStatusList";

    /** The Constant GetTextForm. */
    public final static String GetTextForm = "getTextForm";

    /** The Constant SetTextForm. */
    public final static String SetTextForm = "setTextForm";

    /** The Constant GetWebAppStatus. */
    public final static String GetWebAppStatus = "getWebAppStatus";

    /** The Constant SetActiveApp. */
    public final static String SetActiveApp = "setActiveApp";

    /** The Constant TerminateApps. */
    public final static String TerminateApps = "terminateApps";

    /** The Constant GetVolumeInformation. */
    // Only Audio
    public final static String GetVolumeInformation = "getVolumeInformation";

    /** The Constant SetAudioMute. */
    public final static String SetAudioMute = "setAudioMute";

    /** The Constant SetAudioVolume. */
    public final static String SetAudioVolume = "setAudioVolume";

    /** The Constant GetSchemeList. */
    // Only AvContent
    public final static String GetSchemeList = "getSchemeList";

    /** The Constant GetSourceList. */
    public final static String GetSourceList = "getSourceList";

    /** The Constant GetContentCount. */
    public final static String GetContentCount = "getContentCount";

    /** The Constant GetContentList. */
    public final static String GetContentList = "getContentList";

    /** The Constant GetCurrentExternalInputsStatus. */
    public final static String GetCurrentExternalInputsStatus = "getCurrentExternalInputsStatus";

    /** The Constant GetParentalRatingSettings. */
    public final static String GetParentalRatingSettings = "getParentalRatingSettings";

    /** The Constant GetPlayingContentInfo. */
    public final static String GetPlayingContentInfo = "getPlayingContentInfo";

    /** The Constant DeleteContent. */
    public final static String DeleteContent = "deleteContent";

    /** The Constant SetPlayContent. */
    public final static String SetPlayContent = "setPlayContent";

    /** The Constant SetDeleteProtection. */
    public final static String SetDeleteProtection = "setDeleteProtection";

    /** The Constant SetTvContentVisibility. */
    public final static String SetTvContentVisibility = "setTvContentVisibility";

    /** The Constant SetCecControlMode. */
    // Only CEC
    public final static String SetCecControlMode = "setCecControlMode";

    /** The Constant SetMhlAutoInputChangeMode. */
    public final static String SetMhlAutoInputChangeMode = "setMhlAutoInputChangeMode";

    /** The Constant SetMhlPowerFeedMode. */
    public final static String SetMhlPowerFeedMode = "setMhlPowerFeedMode";

    /** The Constant SetPowerSyncMode. */
    public final static String SetPowerSyncMode = "setPowerSyncMode";

    /** The Constant GetPublicKey. */
    // Only Encryption
    public final static String GetPublicKey = "getPublicKey";

    /** The Constant ActivateBrowserControl. */
    // Only Browser
    public final static String ActivateBrowserControl = "actBrowserControl";

    /** The Constant GetBrowserBookmarkList. */
    public final static String GetBrowserBookmarkList = "getBrowserBookmarkList";

    /** The Constant GetTextUrl. */
    public final static String GetTextUrl = "getTextUrl";

    /** The Constant SetTextUrl. */
    public final static String SetTextUrl = "setTextUrl";

    /** The Constant GetServiceProtocols. */
    // Only Guide
    public final static String GetServiceProtocols = "getServiceProtocols";

    /** The Constant GetCurrentTime. */
    // Only System
    public final static String GetCurrentTime = "getCurrentTime";

    /** The Constant GetDeviceMode. */
    public final static String GetDeviceMode = "getDeviceMode";

    /** The Constant GetInterfaceInformation. */
    public final static String GetInterfaceInformation = "getInterfaceInformation";

    /** The Constant GetLedIndicatorStatus. */
    public final static String GetLedIndicatorStatus = "getLEDIndicatorStatus";

    /** The Constant GetNetworkSettings. */
    public final static String GetNetworkSettings = "getNetworkSettings";

    /** The Constant GetPowerSavingMode. */
    public final static String GetPowerSavingMode = "getPowerSavingMode";

    /** The Constant GetPowerStatus. */
    public final static String GetPowerStatus = "getPowerStatus";

    /** The Constant GetRemoteControllerInfo. */
    public final static String GetRemoteControllerInfo = "getRemoteControllerInfo";

    /** The Constant GetRemoteDeviceSettings. */
    public final static String GetRemoteDeviceSettings = "getRemoteDeviceSettings";

    /** The Constant GetSystemInformation. */
    public final static String GetSystemInformation = "getSystemInformation";

    /** The Constant GetWolMode. */
    public final static String GetWolMode = "getWolMode";

    /** The Constant GetPostalCode. */
    public final static String GetPostalCode = "getPostalCode";

    /** The Constant RequestReboot. */
    public final static String RequestReboot = "requestReboot";

    /** The Constant SetDeviceMode. */
    public final static String SetDeviceMode = "setDeviceMode";

    /** The Constant SetLedIndicatorStatus. */
    public final static String SetLedIndicatorStatus = "setLEDIndicatorStatus";

    /** The Constant SetLanguage. */
    public final static String SetLanguage = "setLanguage";

    /** The Constant SetPowerSavingsMode. */
    public final static String SetPowerSavingsMode = "setPowerSavingMode";

    /** The Constant SetPowerStatus. */
    public final static String SetPowerStatus = "setPowerStatus";

    /** The Constant SetWolMode. */
    public final static String SetWolMode = "setWolMode";

    /** The Constant SetPostalCode. */
    public final static String SetPostalCode = "setPostalCode";

    /** The Constant GetAudioSourceScreen. */
    // Only Video Screen
    public final static String GetAudioSourceScreen = "getAudioSourceScreen";

    /** The Constant GetBannerMode. */
    public final static String GetBannerMode = "getBannerMode";

    /** The Constant GetMultiScreenMode. */
    public final static String GetMultiScreenMode = "getMultiScreenMode";

    /** The Constant GetPipSubScreenPosition. */
    public final static String GetPipSubScreenPosition = "getPipSubScreenPosition";

    /** The Constant GetSceneSetting. */
    public final static String GetSceneSetting = "getSceneSetting";

    /** The Constant setAudioSourceScreen. */
    public final static String setAudioSourceScreen = "setAudioSourceScreen";

    /** The Constant setBannerMode. */
    public final static String setBannerMode = "setBannerMode";

    /** The Constant setMultiScreenMode. */
    public final static String setMultiScreenMode = "setMultiScreenMode";

    /** The Constant setPipSubScreenPosition. */
    public final static String setPipSubScreenPosition = "setPipSubScreenPosition";

    /** The Constant setSceneSetting. */
    public final static String setSceneSetting = "setSceneSetting";

    // ?? doubt I'll support ??

    /** The method name. */
    private final String methodName;

    /** The parms. */
    private final List<String> parms;

    /** The ret vals. */
    private final List<String> retVals;

    /** The method version. */
    private final String methodVersion;

    /**
     * Instantiates a new scalar web method.
     *
     * @param methodName the method name
     * @param parms the parms
     * @param retVals the ret vals
     * @param methodVersion the method version
     */
    public ScalarWebMethod(String methodName, List<String> parms, List<String> retVals, String methodVersion) {
        this.methodName = methodName;
        this.parms = Collections.unmodifiableList(parms);
        this.retVals = Collections.unmodifiableList(retVals);
        this.methodVersion = methodVersion;
    }

    /**
     * Gets the method name.
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Gets the parms.
     *
     * @return the parms
     */
    public List<String> getParms() {
        return parms;
    }

    /**
     * Gets the ret vals.
     *
     * @return the ret vals
     */
    public List<String> getRetVals() {
        return retVals;
    }

    /**
     * Gets the method version.
     *
     * @return the method version
     */
    public String getMethodVersion() {
        return methodVersion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(100);

        sb.append(getMethodName());
        sb.append("(");
        sb.append(StringUtils.join(parms, ','));
        sb.append("): ");
        sb.append(StringUtils.join(retVals, ','));

        return sb.toString();
    }
}
