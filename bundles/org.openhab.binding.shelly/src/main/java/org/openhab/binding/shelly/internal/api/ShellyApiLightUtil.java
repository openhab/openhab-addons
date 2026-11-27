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
package org.openhab.binding.shelly.internal.api;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.substringAfter;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;

/**
 * The {@link ShellyApiLightUtil} provides light-specific helpers shared by Gen1 (api1) and Gen2+ (api2) code:
 * the {@code settings.lights[i].apiComponent} tag values used to identify which light API component family (RGB,
 * RGBW, CCT or plain Light) a {@code ShellySettingsRgbwLight} entry belongs to, and the light channel group/id
 * resolution shared across handlers.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyApiLightUtil {
    private ShellyApiLightUtil() {
    }

    /**
     * The light API component family a {@code settings.lights[i]} entry belongs to. {@code NONE} marks an
     * untagged entry (a Gen1 RGBW2/Bulb entry never sets apiComponent) or an out-of-range index.
     * <p>
     * Each index is an independently switchable/dimmable/metered physical component - not a sub-channel of one
     * combined light. A hybrid Pro RGBWW PM profile (e.g. {@code rgbcct}, {@code rgbx2light}) reports its color
     * component (RGB/RGBW) and its secondary CCT/Light component(s) as separate {@code settings.lights} indices,
     * each with its own RPC methods and meter - never merged into a single multi-channel model.
     */
    public enum ShellyLightApiComponent {
        RGB,
        RGBW,
        CCT,
        LIGHT,
        NONE
    }

    /**
     * True when tag identifies an RGB/RGBW color component, as opposed to a CCT/Light one.
     */
    public static boolean isColorComponent(ShellyLightApiComponent apiComponent) {
        return apiComponent == ShellyLightApiComponent.RGB || apiComponent == ShellyLightApiComponent.RGBW;
    }

    public static boolean isRgbComponent(ShellyLightApiComponent apiComponent) {
        return apiComponent == ShellyLightApiComponent.RGB;
    }

    public static boolean isRgbwComponent(ShellyLightApiComponent apiComponent) {
        return apiComponent == ShellyLightApiComponent.RGBW;
    }

    public static boolean isCctComponent(ShellyLightApiComponent apiComponent) {
        return apiComponent == ShellyLightApiComponent.CCT;
    }

    public static boolean isLightComponent(ShellyLightApiComponent apiComponent) {
        return apiComponent == ShellyLightApiComponent.LIGHT;
    }

    /**
     * The apiComponent tag of {@code lights.get(idx)}, or {@code NONE} when lights is null, idx is out of range,
     * or the entry is untagged (a Gen1 RGBW2/Bulb entry never sets apiComponent).
     */
    public static ShellyLightApiComponent tagAt(@Nullable List<ShellySettingsRgbwLight> lights, int idx) {
        return lights != null && idx >= 0 && idx < lights.size() ? lights.get(idx).apiComponent
                : ShellyLightApiComponent.NONE;
    }

    /**
     * True when any entry in lights is an RGB/RGBW color component.
     */
    public static boolean hasColorComponent(List<ShellySettingsRgbwLight> lights) {
        return lights.stream().map(l -> l.apiComponent).anyMatch(ShellyApiLightUtil::isColorComponent);
    }

    /**
     * Reverses {@link ShellyDeviceProfile#getControlGroup(int)}: converts a channel group name back into its
     * {@code settings.lights} index. On a hybrid profile (e.g. {@code rgbcct}, {@code rgbx2light}) the indexed
     * groups (light1, light2, ...) start after the leading color component slot(s), so the group number alone
     * is not the flat index - {@code profile.getColorComponentCount()} must be added back in.
     */
    public static Integer getLightIdFromGroup(String groupName, ShellyDeviceProfile profile) {
        if (groupName.startsWith(CHANNEL_GROUP_LIGHT_INDEX)) {
            return Integer.parseInt(substringAfter(groupName, CHANNEL_GROUP_LIGHT_INDEX)) - 1
                    + profile.getColorComponentCount();
        }
        if (groupName.startsWith(CHANNEL_GROUP_LIGHT_CHANNEL)) {
            return Integer.parseInt(substringAfter(groupName, CHANNEL_GROUP_LIGHT_CHANNEL)) - 1
                    + profile.getColorComponentCount();
        }
        return 0; // only 1 light, e.g. bulb or rgbw2 in color mode
    }

    // Gen2 RGBW PM ships on light1..n natively. A Gen1 RGBW2 Thing that already carries the
    // deprecated channel1..n group (from before this Thing was migrated) keeps publishing there,
    // dual-written to light1..n by ShellyBaseHandler.updateChannel(); a freshly discovered Gen1
    // RGBW2 Thing goes straight to light1..n and never gets a channel1..n group.
    public static String lightChannelGroupPrefix(ShellyDeviceProfile profile) {
        return profile.isGen2 || !profile.hasLegacyLightChannels ? CHANNEL_GROUP_LIGHT_INDEX
                : CHANNEL_GROUP_LIGHT_CHANNEL;
    }

    // Bulb/Duo report white/temp under a dedicated group, distinct from their color group; everything else
    // (including a hybrid Pro RGBWW PM profile's secondary CCT/Light component) shares the same per-component
    // group resolution as the color channels, so this just delegates to getControlGroup().
    public static String buildWhiteGroupName(ShellyDeviceProfile profile, int lightId) {
        if (profile.isBulb || profile.isDuo) {
            return CHANNEL_GROUP_WHITE_CONTROL;
        }
        return profile.getControlGroup(lightId);
    }
}
