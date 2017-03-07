/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.preset;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.binding.russound.internal.rio.AbstractRioProtocol;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the protocol handler for the Russound Preset. This handler will issue the protocol commands and will
 * process the responses from the Russound system.
 *
 * @author Tim Roberts
 *
 */
class RioPresetProtocol extends AbstractRioProtocol {
    // logger
    private Logger logger = LoggerFactory.getLogger(RioPresetProtocol.class);

    /**
     * The preset identifier for the handler
     */
    private final int _preset;

    /**
     * The bank identifier - will be -1 if attached to a zone
     */
    private final int _bank;

    /**
     * The source identifier for the bank - will be -1 if attached to a zone
     */
    private final int _source;

    /**
     * The zone identifier - will be -1 if attached to a bank
     */
    private final int _zone;

    /**
     * The controller identifier - will be -1 if attached to a bank
     */
    private final int _controller;

    /**
     * The name of the preset (only is appled when {@link #savePreset()} is invoked)
     */
    private String _name;

    // Protocol constants
    private final static String PRESET_NAME = "name";
    private final static String PRESET_VALID = "valid";

    // Response patterns
    private final Pattern RSP_PRESETNOTIFICATION = Pattern
            .compile("^[SN] S\\[(\\d+)\\].B\\[(\\d+)\\].P\\[(\\d+)\\].(\\w+)=\"(.*)\"$");

    /**
     * Constructs the protocol handler from given parameters
     *
     * @param preset the preset identifier
     * @param bank the bank identifier or -1 if attached to a zone
     * @param source the source identifier or -1 if attached to a zone
     * @param zone the zone identifier or -1 if attached to a bank
     * @param controller the controller identifier or -1 if attached to a bank
     * @param session a non-null {@link SocketSession} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     */
    RioPresetProtocol(int preset, int bank, int source, int zone, int controller, SocketSession session,
            RioHandlerCallback callback) {
        super(session, callback);
        _preset = preset;
        _bank = bank;
        _source = source;
        _zone = zone;
        _controller = controller;
        setName("Preset " + preset);
    }

    /**
     * Helper method to determine if attached to a source/bank
     *
     * @return true if attached to a source/bank, false if attached to a controller/zone
     */
    private boolean isBank() {
        return _bank > 0;
    }

    /**
     * Refreshes the name of that preset - this can only be done from a bank level. If called on a
     * zone level, a debug warning will be issued and the call ignored.
     */
    void refreshName() {
        if (isBank()) {
            sendCommand("GET S[" + _source + "].B[" + _bank + "].P[" + _preset + "]." + PRESET_NAME);
        } else {
            logger.warn("Trying to refresh a name outside of a bank");
        }
    }

    /**
     * Refreshes the whether the preset is valid - this can only be done from a bank level. If called on a
     * zone level, a debug warning will be issued and the call ignored.
     */
    void refreshValid() {
        if (isBank()) {
            sendCommand("GET S[" + _source + "].B[" + _bank + "].P[" + _preset + "]." + PRESET_VALID);
        } else {
            logger.warn("Trying to refresh a valid outside of a bank");
        }
    }

    /**
     * Set's the name of the preset - this can only be done from a bank level. If called on a
     * zone level, a debug warning will be issued and the call ignored. Please note that the name will only be committed
     * when the preset is saved. Setting a name of null or empty is allowed (on {@link #savePreset()}, the Russound
     * system will reset the name to the current frequency).
     *
     * @param name a possibly null, possibly empty name. Please note a null will be converted to an empty string.
     */
    void setName(String name) {
        if (isBank()) {
            _name = name == null ? "" : name;
            stateChanged(RioConstants.CHANNEL_PRESETNAME, new StringType(_name));
        } else {
            logger.warn("Trying to set the name outside of a bank");
        }
    }

    /**
     * Saves the current channel as the preset - this can only be done from a zone level. If called on a
     * bank level, a debug warning will be issued and the call ignored. The name will be saved as well if it's specified
     * (if not specified [i.e. null or empty], the Russound system will create a name from the current frequency)
     */
    void savePreset() {
        if (isBank()) {
            logger.warn("Trying to save a preset outside of a zone");
        } else {
            sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!savePreset "
                    + (_name == null || _name.trim().length() == 0 ? "" : ("\"" + _name + "\"")) + " " + _preset);

            // We are not sure what source this is for - so refresh them all
            final int bank = ((_preset - 1) / 6) + 1;
            final int preset = ((_preset - 1) % 6) + 1;
            for (int source = 1; source < 13; source++) {
                sendCommand("GET S[" + source + "].B[" + bank + "].P[" + preset + "]." + PRESET_NAME);
                sendCommand("GET S[" + source + "].B[" + bank + "].P[" + preset + "]." + PRESET_VALID);
            }
        }
    }

    /**
     * Restores the saved preset to the zone - this can only be done from a zone level. If called on a
     * bank level, a debug warning will be issued and the call ignored.
     */
    void restorePreset() {
        if (isBank()) {
            logger.warn("Trying to restore a preset outside of a zone");
        } else {
            sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!restorePreset " + _preset);
        }
    }

    /**
     * Deletes the saved preset - this can only be done from a zone level. If called on a bank level, a debug warning
     * will be issued and the call ignored.
     */
    void deletePreset() {
        if (isBank()) {
            logger.warn("Trying to restore a preset outside of a zone");
        } else {
            sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!deletePreset " + _preset);
        }
        // We are not sure what source this is for - so refresh them all
        final int bank = ((_preset - 1) / 6) + 1;
        final int preset = ((_preset - 1) % 6) + 1;
        for (int source = 1; source < 13; source++) {
            sendCommand("GET S[" + source + "].B[" + bank + "].P[" + preset + "]." + PRESET_VALID);
        }
    }

    /**
     * Handles any preset notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    private void handlePresetNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }

        if (m.groupCount() == 5) {
            try {
                final int source = Integer.parseInt(m.group(1));
                if (source != _source) {
                    return;
                }

                final int bank = Integer.parseInt(m.group(2));
                if (bank != _bank) {
                    return;
                }

                final int preset = Integer.parseInt(m.group(3));
                if (preset != _preset) {
                    return;
                }

                final String key = m.group(4);
                final String value = m.group(5);

                switch (key) {
                    case PRESET_NAME:
                        setName(value);
                        break;

                    case PRESET_VALID:
                        stateChanged(RioConstants.CHANNEL_PRESETVALID,
                                "false".equalsIgnoreCase(value) ? OnOffType.OFF : OnOffType.ON);
                        break;

                    default:
                        logger.warn("Unknown preset notification: '{}'", resp);
                        break;
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
    public void responseReceived(String response) {
        if (response == null || response == "") {
            return;
        }

        final Matcher m = RSP_PRESETNOTIFICATION.matcher(response);
        if (m.matches()) {
            handlePresetNotification(m, response);
            return;
        }
    }
}
