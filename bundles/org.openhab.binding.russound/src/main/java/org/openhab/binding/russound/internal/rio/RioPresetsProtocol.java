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
package org.openhab.binding.russound.internal.rio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.binding.russound.internal.rio.models.GsonUtilities;
import org.openhab.binding.russound.internal.rio.models.RioPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * This {@link AbstractRioProtocol} implementation provides the implementation for managing Russound bank presets.
 * Since refreshing all 36 presets requires 72 calls to russound (for name/valid), we limit how often we can
 * refresh to {@link #UPDATE_TIME_SPAN}. Presets are tracked by source ID and will only be valid if that source type is
 * a tuner.
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioPresetsProtocol extends AbstractRioProtocol {

    // logger
    private final Logger logger = LoggerFactory.getLogger(RioPresetsProtocol.class);

    // helper names
    private static final String PRESET_NAME = "name";
    private static final String PRESET_VALID = "valid";

    /**
     * The pattern representing preset notifications
     */
    private static final Pattern RSP_PRESETNOTIFICATION = Pattern
            .compile("(?i)^[SN] S\\[(\\d+)\\].B\\[(\\d+)\\].P\\[(\\d+)\\].(\\w+)=\"(.*)\"$");

    /**
     * The pattern representing a source type notification
     */
    private static final Pattern RSP_SRCTYPENOTIFICATION = Pattern.compile("^[SN] S\\[(\\d+)\\]\\.type=\"(.*)\"$");

    /**
     * All 36 presets represented by two dimensions - 8 source by 36 presets
     */
    private final RioPreset[][] presets = new RioPreset[8][36];

    /**
     * Represents whether the source is a tuner or not
     */
    private final boolean[] isTuner = new boolean[8];

    /**
     * The {@link Gson} used for JSON operations
     */
    private final Gson gson;

    /**
     * The {@link ReentrantLock} used to control access to {@link #lastUpdateTime}
     */
    private final Lock lastUpdateLock = new ReentrantLock();

    /**
     * The last time the specified source presets were updated via {@link #refreshPresets(Integer)}
     */
    private final long[] lastUpdateTime = new long[8];

    /**
     * The minimum timespan between updates of source presets via {@link #refreshPresets(Integer)}
     */
    private static final long UPDATE_TIME_SPAN = 60000;

    /**
     * The pattern to determine if the source type is a tuner
     */
    private static final Pattern IS_TUNER = Pattern.compile("(?i)^.*AM.*FM.*TUNER.*$");

    /**
     * The list of listeners that will be called when system favorites have changed
     */
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Constructs the preset protocol from the given session and callback. Note: the passed callback is not
     * currently used
     *
     * @param session a non null {@link SocketSession} to use
     * @param callback a non-null {@link RioHandlerCallback} to use
     */
    public RioPresetsProtocol(SocketSession session, RioHandlerCallback callback) {
        super(session, callback);

        gson = GsonUtilities.createGson();
        for (int s = 1; s <= 8; s++) {
            sendCommand("GET S[" + s + "].type");

            for (int x = 1; x <= 36; x++) {
                presets[s - 1][x - 1] = new RioPreset(x);
            }
        }
    }

    /**
     * Adds the specified listener to changes to presets
     *
     * @param listener a non-null listener to add
     * @throws IllegalArgumentException if listener is null
     */
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the specified listener from change notifications
     *
     * @param listener a possibly null listener to remove (null is ignored)
     * @return true if removed, false otherwise
     */
    public boolean removeListener(Listener listener) {
        return listeners.remove(listener);
    }

    /**
     * Fires the presetsUpdate method on all listeners with the results of {@link #getJson()} for the given source
     *
     * @param sourceId a valid source identifier between 1 and 8
     * @throws IllegalArgumentException if sourceId is < 1 or > 8
     */
    private void fireUpdate(int sourceId) {
        if (sourceId < 1 || sourceId > 8) {
            throw new IllegalArgumentException("sourceId must be between 1 and 8");
        }
        final String json = getJson(sourceId);
        for (Listener l : listeners) {
            l.presetsUpdated(sourceId, json);
        }
    }

    /**
     * Helper method to request the specified presets id information (name/valid) for a given source. Please note that
     * this does NOT change the {@link #lastUpdateTime}
     *
     * @param sourceId a source identifier between 1 and 8
     * @param favIds a non-null, possibly empty list of system favorite ids to request (any id < 1 or > 32 will be
     *            ignored)
     * @throws IllegalArgumentException if favIds is null
     * @throws IllegalArgumentException if sourceId is < 1 or > 8
     */
    private void requestPresets(int sourceId, List<RioPreset> presetIds) {
        if (sourceId < 1 || sourceId > 8) {
            throw new IllegalArgumentException("sourceId must be between 1 and 8");
        }
        if (presetIds == null) {
            throw new IllegalArgumentException("presetIds must not be null");
        }
        for (RioPreset preset : presetIds) {
            sendCommand("GET S[" + sourceId + "].B[" + preset.getBank() + "].P[" + preset.getBankPreset() + "].valid");
            sendCommand("GET S[" + sourceId + "].B[" + preset.getBank() + "].P[" + preset.getBankPreset() + "].name");
        }
    }

    /**
     * Refreshes ALL presets for all sources. Simply calls {@link #refreshPresets(Integer)} with each source identifier
     */
    public void refreshPresets() {
        for (int sourceId = 1; sourceId <= 8; sourceId++) {
            refreshPresets(sourceId);
        }
    }

    /**
     * Refreshes ALL presets for the given sourceId if they have not been refreshed within the last
     * {@link #UPDATE_TIME_SPAN}. This method WILL change the {@link #lastUpdateTime}. No calls will be made if the
     * source type is not a tuner (however the {@link #lastUpdateTime} will be reset).
     *
     * @param sourceId a source identifier between 1 and 8
     * @throws IllegalArgumentException if sourceId is < 1 or > 8
     */
    public void refreshPresets(Integer sourceId) {
        if (sourceId < 1 || sourceId > 8) {
            throw new IllegalArgumentException("sourceId must be between 1 and 8");
        }
        lastUpdateLock.lock();
        try {
            final long now = System.currentTimeMillis();
            if (now > lastUpdateTime[sourceId - 1] + UPDATE_TIME_SPAN) {
                lastUpdateTime[sourceId - 1] = now;

                if (isTuner[sourceId - 1]) {
                    for (int x = 1; x <= 36; x++) {
                        final RioPreset preset = presets[sourceId - 1][x - 1];
                        sendCommand("GET S[" + sourceId + "].B[" + preset.getBank() + "].P[" + preset.getBankPreset()
                                + "].valid");
                        sendCommand("GET S[" + sourceId + "].B[" + preset.getBank() + "].P[" + preset.getBankPreset()
                                + "].name");
                    }
                }
            }
        } finally {
            lastUpdateLock.unlock();
        }
    }

    /**
     * Returns the JSON representation of the presets for the sourceId and their state. If the sourceId does not
     * represent a tuner, then an empty array JSON representation ("[]") will be returned.
     *
     * @return A non-null, non-empty JSON representation of {@link #_systemFavorites}
     */
    public String getJson(int source) {
        if (!isTuner[source - 1]) {
            return "[]";
        }

        final List<RioPreset> validPresets = new ArrayList<>();
        for (final RioPreset preset : presets[source - 1]) {
            if (preset.isValid()) {
                validPresets.add(preset);
            }
        }

        return gson.toJson(validPresets);
    }

    /**
     * Sets a zone preset. NOTE: at this time, only a single preset can be represented in the presetJson. Having more
     * than one preset saved to the same underlying channel causes the russound system to become a little unstable. This
     * method will save the preset if the status is changed from not valid to valid or if the name is simply changing on
     * a currently valid preset. The preset will be deleted if status is changed from valid to not valid. When saving a
     * preset and the name is not specified, the russound system will automatically assign a name equal to the channel
     * being played.
     *
     * @param controller a controller between 1 and 6
     * @param zone a zone between 1 and 8
     * @param source a source between 1 and 8
     * @param presetJson the possibly empty, possibly null JSON representation of the preset
     * @throws IllegalArgumentException if controller is < 1 or > 6
     * @throws IllegalArgumentException if zone is < 1 or > 8
     * @throws IllegalArgumentException if source is < 1 or > 8
     * @throws IllegalArgumentException if presetJson contains more than one preset
     */
    public void setZonePresets(int controller, int zone, int source, @Nullable String presetJson) {
        if (controller < 1 || controller > 6) {
            throw new IllegalArgumentException("Controller must be between 1 and 6");
        }

        if (zone < 1 || zone > 8) {
            throw new IllegalArgumentException("Zone must be between 1 and 8");
        }

        if (source < 1 || source > 8) {
            throw new IllegalArgumentException("Source must be between 1 and 8");
        }

        if (presetJson == null || presetJson.isEmpty()) {
            return;
        }

        final List<RioPreset> updatePresetIds = new ArrayList<>();
        try {
            final RioPreset[] newPresets = gson.fromJson(presetJson, RioPreset[].class);

            // Keeps from screwing up the system if you set a bunch of presets to the same playing
            if (newPresets.length > 1) {
                throw new IllegalArgumentException("Can only save a single preset at a time");
            }

            for (int x = newPresets.length - 1; x >= 0; x--) {
                final RioPreset preset = newPresets[x];
                if (preset == null) {
                    continue;// caused by {id,valid,name},,{id,valid,name}
                }
                final int presetId = preset.getId();
                if (presetId < 1 || presetId > 36) {
                    logger.debug("Invalid preset id (not between 1 and 36) - ignoring: {}:{}", presetId, presetJson);
                } else {
                    final RioPreset myPreset = presets[source][presetId];
                    final boolean presetValid = preset.isValid();
                    final String presetName = preset.getName();

                    // re-retrieve to see if the save/delete worked (saving on a zone that's off - valid won't be set to
                    // true)
                    if (!Objects.equals(myPreset.getName(), presetName) || myPreset.isValid() != presetValid) {
                        myPreset.setName(presetName);
                        myPreset.setValid(presetValid);
                        if (presetValid) {
                            if (presetName == null || presetName.isEmpty()) {
                                sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!savePreset " + presetId);
                            } else {
                                sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!savePreset \"" + presetName
                                        + "\" " + presetId);
                            }

                            updatePresetIds.add(preset);
                        } else {
                            sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!deletePreset " + presetId);
                        }
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Invalid JSON: {}", e.getMessage(), e);
        }

        // Invalid the presets we updated
        requestPresets(source, updatePresetIds);

        // Refresh our channel since 'presetJson' occupies it right now
        fireUpdate(source);
    }

    /**
     * Handles any system notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    void handlePresetNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }

        if (m.groupCount() == 5) {
            try {
                final int source = Integer.parseInt(m.group(1));
                if (source >= 1 && source <= 8) {
                    final int bank = Integer.parseInt(m.group(2));
                    if (bank >= 1 && bank <= 6) {
                        final int preset = Integer.parseInt(m.group(3));
                        if (preset >= 1 && preset <= 6) {
                            final String key = m.group(4).toLowerCase();
                            final String value = m.group(5);

                            final RioPreset rioPreset = presets[source - 1][(bank - 1) * 6 + preset - 1];

                            switch (key) {
                                case PRESET_NAME:
                                    rioPreset.setName(value);
                                    fireUpdate(source);
                                    break;

                                case PRESET_VALID:
                                    rioPreset.setValid(!"false".equalsIgnoreCase(value));
                                    fireUpdate(source);
                                    break;

                                default:
                                    logger.warn("Unknown preset notification: '{}'", resp);
                                    break;
                            }
                        } else {
                            logger.debug("Preset ID must be between 1 and 6: {}", resp);
                        }
                    } else {
                        logger.debug("Bank ID must be between 1 and 6: {}", resp);

                    }
                } else {
                    logger.debug("Source ID must be between 1 and 8: {}", resp);
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid Preset Notification (source/bank/preset not a parsable integer): '{}')", resp);
            }
        } else {
            logger.warn("Invalid Preset Notification: '{}')", resp);
        }
    }

    /**
     * Handles any preset notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    private void handlerSourceTypeNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }

        if (m.groupCount() == 2) {
            try {
                final int sourceId = Integer.parseInt(m.group(1));
                if (sourceId >= 1 && sourceId <= 8) {
                    final String sourceType = m.group(2);

                    final Matcher matcher = IS_TUNER.matcher(sourceType);
                    final boolean srcIsTuner = matcher.matches();

                    if (srcIsTuner != isTuner[sourceId - 1]) {
                        isTuner[sourceId - 1] = srcIsTuner;

                        if (srcIsTuner) {
                            // force a refresh on the source
                            lastUpdateTime[sourceId - 1] = 0;
                            refreshPresets(sourceId);
                        } else {
                            for (int p = 0; p < 36; p++) {
                                presets[sourceId - 1][p].setValid(false);
                                presets[sourceId - 1][p].setName(null);
                            }
                        }
                        fireUpdate(sourceId);
                    }
                } else {
                    logger.debug("Source is not between 1 and 8, Response: {}", resp);
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid Preset Notification (source/bank/preset not a parsable integer): '{}')", resp);
            }
        } else {
            logger.warn("Invalid Preset Notification: '{}')", resp);
        }
    }

    /**
     * Implements {@link SocketSessionListener#responseReceived(String)} to try to process the response from the
     * russound system. This response may be for other protocol handler - so ignore if we don't recognize the response.
     *
     * @param a possibly null, possibly empty response
     */
    @Override
    public void responseReceived(@Nullable String response) {
        if (response == null || response.isEmpty()) {
            return;
        }

        Matcher m = RSP_PRESETNOTIFICATION.matcher(response);
        if (m.matches()) {
            handlePresetNotification(m, response);
        }

        m = RSP_SRCTYPENOTIFICATION.matcher(response);
        if (m.matches()) {
            handlerSourceTypeNotification(m, response);
        }
    }

    /**
     * Defines the listener implementation to list for preset updates
     *
     * @author Tim Roberts
     *
     */
    public interface Listener {
        /**
         * Called when presets have changed for a specific sourceId. The jsonString will contain the current
         * representation of all valid presets for the source.
         *
         * @param sourceId a source identifier between 1 and 8
         * @param jsonString a non-null, non-empty json representation of {@link RioPreset}
         */
        void presetsUpdated(int sourceId, String jsonString);
    }
}
