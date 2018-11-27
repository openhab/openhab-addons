/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.source;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.binding.russound.internal.rio.AbstractRioProtocol;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.openhab.binding.russound.internal.rio.StatefulHandlerCallback;
import org.openhab.binding.russound.internal.rio.models.GsonUtilities;
import org.openhab.binding.russound.internal.rio.models.RioBank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * This is the protocol handler for the Russound Source. This handler will issue the protocol commands and will
 * process the responses from the Russound system. Please see documentation for what channels are supported by which
 * source types.
 *
 * @author Tim Roberts - Initial contribution
 */
class RioSourceProtocol extends AbstractRioProtocol {
    private final Logger logger = LoggerFactory.getLogger(RioSourceProtocol.class);

    /**
     * The source identifier (1-12)
     */
    private final int source;

    // Protocol constants
    private static final String SRC_NAME = "name";
    private static final String SRC_TYPE = "type";
    private static final String SRC_IPADDRESS = "ipaddress";
    private static final String SRC_COMPOSERNAME = "composername";
    private static final String SRC_CHANNEL = "channel";
    private static final String SRC_CHANNELNAME = "channelname";
    private static final String SRC_GENRE = "genre";
    private static final String SRC_ARTISTNAME = "artistname";
    private static final String SRC_ALBUMNAME = "albumname";
    private static final String SRC_COVERARTURL = "coverarturl";
    private static final String SRC_PLAYLISTNAME = "playlistname";
    private static final String SRC_SONGNAME = "songname";
    private static final String SRC_MODE = "mode";
    private static final String SRC_SHUFFLEMODE = "shufflemode";
    private static final String SRC_REPEATMODE = "repeatmode";
    private static final String SRC_RATING = "rating";
    private static final String SRC_PROGRAMSERVICENAME = "programservicename";
    private static final String SRC_RADIOTEXT = "radiotext";
    private static final String SRC_RADIOTEXT2 = "radiotext2";
    private static final String SRC_RADIOTEXT3 = "radiotext3";
    private static final String SRC_RADIOTEXT4 = "radiotext4";

    // Multimedia channels
    private static final String SRC_MMSCREEN = "mmscreen";
    private static final String SRC_MMTITLE = "mmtitle.text";
    private static final String SRC_MMATTR = "attr";
    private static final String SRC_MMBTNOK = "mmbtnok.text";
    private static final String SRC_MMBTNBACK = "mmbtnback.text";
    private static final String SRC_MMINFOBLOCK = "mminfoblock.text";

    private static final String SRC_MMHELP = "mmhelp.text";
    private static final String SRC_MMTEXTFIELD = "mmtextfield.text";

    // This is an undocumented volume
    private static final String SRC_VOLUME = "volume";

    private static final String BANK_NAME = "name";

    // Response patterns
    private static final Pattern RSP_MMMENUNOTIFICATION = Pattern.compile("^\\{.*\\}$");
    private static final Pattern RSP_SRCNOTIFICATION = Pattern
            .compile("(?i)^[SN] S\\[(\\d+)\\]\\.([a-zA-Z_0-9.\\[\\]]+)=\"(.*)\"$");
    private static final Pattern RSP_BANKNOTIFICATION = Pattern
            .compile("(?i)^[SN] S\\[(\\d+)\\].B\\[(\\d+)\\].(\\w+)=\"(.*)\"$");
    private static final Pattern RSP_PRESETNOTIFICATION = Pattern
            .compile("(?i)^[SN] S\\[(\\d+)\\].B\\[(\\d+)\\].P\\[(\\d+)\\].(\\w+)=\"(.*)\"$");

    /**
     * Current banks
     */
    private final RioBank[] banks = new RioBank[6];

    /**
     * {@link Gson} use to create/read json
     */
    private final Gson gson;

    /**
     * Lock used to control access to {@link #infoText}
     */
    private final Lock infoLock = new ReentrantLock();

    /**
     * The information text appeneded from media management calls
     */
    private final StringBuilder infoText = new StringBuilder(100);

    /**
     * The table of channels to unique identifiers for media management functions
     */
    @SuppressWarnings("serial")
    private final Map<String, AtomicInteger> mmSeqNbrs = Collections
            .unmodifiableMap(new HashMap<String, AtomicInteger>() {
                {
                    put(RioConstants.CHANNEL_SOURCEMMMENU, new AtomicInteger(0));
                    put(RioConstants.CHANNEL_SOURCEMMSCREEN, new AtomicInteger(0));
                    put(RioConstants.CHANNEL_SOURCEMMTITLE, new AtomicInteger(0));
                    put(RioConstants.CHANNEL_SOURCEMMATTR, new AtomicInteger(0));
                    put(RioConstants.CHANNEL_SOURCEMMBUTTONOKTEXT, new AtomicInteger(0));
                    put(RioConstants.CHANNEL_SOURCEMMBUTTONBACKTEXT, new AtomicInteger(0));
                    put(RioConstants.CHANNEL_SOURCEMMINFOTEXT, new AtomicInteger(0));
                    put(RioConstants.CHANNEL_SOURCEMMHELPTEXT, new AtomicInteger(0));
                    put(RioConstants.CHANNEL_SOURCEMMTEXTFIELD, new AtomicInteger(0));
                }
            });

    /**
     * The client used for http requests
     */
    private final HttpClient httpClient;

    /**
     * Constructs the protocol handler from given parameters
     *
     * @param source the source identifier
     * @param session a non-null {@link SocketSession} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     * @throws Exception exception when starting the {@link HttpClient}
     */
    RioSourceProtocol(int source, SocketSession session, RioHandlerCallback callback) throws Exception {
        super(session, callback);
        if (source < 1 || source > 12) {
            throw new IllegalArgumentException("Source must be between 1-12: " + source);
        }
        this.source = source;
        httpClient = new HttpClient();
        httpClient.setFollowRedirects(true);
        httpClient.start();

        gson = GsonUtilities.createGson();

        for (int x = 1; x <= 6; x++) {
            banks[x - 1] = new RioBank(x);
        }
    }

    /**
     * Helper method to issue post online commands
     */
    void postOnline() {
        watchSource(true);
        refreshSourceIpAddress();
        refreshSourceName();

        updateBanksChannel();
    }

    /**
     * Helper method to refresh a source key
     *
     * @param keyName a non-null, non-empty source key to refresh
     * @throws IllegalArgumentException if keyName is null or empty
     */
    private void refreshSourceKey(String keyName) {
        if (keyName == null || keyName.trim().length() == 0) {
            throw new IllegalArgumentException("keyName cannot be null or empty");
        }
        sendCommand("GET S[" + source + "]." + keyName);
    }

    /**
     * Refreshes the source name
     */
    void refreshSourceName() {
        refreshSourceKey(SRC_NAME);
    }

    /**
     * Refresh the source model type
     */
    void refreshSourceType() {
        refreshSourceKey(SRC_TYPE);
    }

    /**
     * Refresh the source ip address
     */
    void refreshSourceIpAddress() {
        refreshSourceKey(SRC_IPADDRESS);
    }

    /**
     * Refresh composer name
     */
    void refreshSourceComposerName() {
        refreshSourceKey(SRC_COMPOSERNAME);
    }

    /**
     * Refresh the channel frequency (for tuners)
     */
    void refreshSourceChannel() {
        refreshSourceKey(SRC_CHANNEL);
    }

    /**
     * Refresh the channel's name
     */
    void refreshSourceChannelName() {
        refreshSourceKey(SRC_CHANNELNAME);
    }

    /**
     * Refresh the song's genre
     */
    void refreshSourceGenre() {
        refreshSourceKey(SRC_GENRE);
    }

    /**
     * Refresh the artist name
     */
    void refreshSourceArtistName() {
        refreshSourceKey(SRC_ARTISTNAME);
    }

    /**
     * Refresh the album name
     */
    void refreshSourceAlbumName() {
        refreshSourceKey(SRC_ALBUMNAME);
    }

    /**
     * Refresh the cover art URL
     */
    void refreshSourceCoverArtUrl() {
        refreshSourceKey(SRC_COVERARTURL);
    }

    /**
     * Refresh the playlist name
     */
    void refreshSourcePlaylistName() {
        refreshSourceKey(SRC_PLAYLISTNAME);
    }

    /**
     * Refresh the song name
     */
    void refreshSourceSongName() {
        refreshSourceKey(SRC_SONGNAME);
    }

    /**
     * Refresh the provider mode/streaming service
     */
    void refreshSourceMode() {
        refreshSourceKey(SRC_MODE);
    }

    /**
     * Refresh the shuffle mode
     */
    void refreshSourceShuffleMode() {
        refreshSourceKey(SRC_SHUFFLEMODE);
    }

    /**
     * Refresh the repeat mode
     */
    void refreshSourceRepeatMode() {
        refreshSourceKey(SRC_REPEATMODE);
    }

    /**
     * Refresh the rating of the song
     */
    void refreshSourceRating() {
        refreshSourceKey(SRC_RATING);
    }

    /**
     * Refresh the program service name
     */
    void refreshSourceProgramServiceName() {
        refreshSourceKey(SRC_PROGRAMSERVICENAME);
    }

    /**
     * Refresh the radio text
     */
    void refreshSourceRadioText() {
        refreshSourceKey(SRC_RADIOTEXT);
    }

    /**
     * Refresh the radio text (line #2)
     */
    void refreshSourceRadioText2() {
        refreshSourceKey(SRC_RADIOTEXT2);
    }

    /**
     * Refresh the radio text (line #3)
     */
    void refreshSourceRadioText3() {
        refreshSourceKey(SRC_RADIOTEXT3);
    }

    /**
     * Refresh the radio text (line #4)
     */
    void refreshSourceRadioText4() {
        refreshSourceKey(SRC_RADIOTEXT4);
    }

    /**
     * Refresh the source volume
     */
    void refreshSourceVolume() {
        refreshSourceKey(SRC_VOLUME);
    }

    /**
     * Refreshes the names of the banks
     */
    void refreshBanks() {
        for (int b = 1; b <= 6; b++) {
            sendCommand("GET S[" + source + "].B[" + b + "]." + BANK_NAME);
        }
    }

    /**
     * Sets the bank names from the supplied bank JSON and returns a runnable to call {@link #updateBanksChannel()}
     *
     * @param bankJson a possibly null, possibly empty json containing the {@link RioBank} to update
     * @return a non-null {@link Runnable} to execute after this call
     */
    Runnable setBanks(String bankJson) {
        // If null or empty - simply return a do nothing runnable
        if (StringUtils.isEmpty(bankJson)) {
            return () -> {
            };
        }

        try {
            final RioBank[] newBanks;
            newBanks = gson.fromJson(bankJson, RioBank[].class);
            for (int x = 0; x < newBanks.length; x++) {
                final RioBank bank = newBanks[x];
                if (bank == null) {
                    continue; // caused by {id,valid,name},,{id,valid,name}
                }

                final int bankId = bank.getId();
                if (bankId < 1 || bankId > 6) {
                    logger.debug("Invalid bank id (not between 1 and 6) - ignoring: {}:{}", bankId, bankJson);
                } else {
                    final RioBank myBank = banks[bankId - 1];

                    if (!StringUtils.equals(myBank.getName(), bank.getName())) {
                        myBank.setName(bank.getName());
                        sendCommand(
                                "SET S[" + source + "].B[" + bankId + "]." + BANK_NAME + "=\"" + bank.getName() + "\"");
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Invalid JSON: {}", e.getMessage(), e);
        }

        // regardless of what happens above - reupdate the channel
        // (to remove anything bad from it)
        return this::updateBanksChannel;
    }

    /**
     * Helper method to simply update the banks channel. Will create a JSON representation from {@link #banks} and send
     * it via the channel
     */
    private void updateBanksChannel() {
        final String bankJson = gson.toJson(banks);
        stateChanged(RioConstants.CHANNEL_SOURCEBANKS, new StringType(bankJson));
    }

    /**
     * Turns on/off watching the source for notifications
     *
     * @param watch true to turn on, false to turn off
     */
    void watchSource(boolean watch) {
        sendCommand("WATCH S[" + source + "] " + (watch ? "ON" : "OFF"));
    }

    /**
     * Helper method to handle any media management change. If the channel is the INFO text channel, we delegate to
     * {@link #handleMMInfoText(String)} instead. This helper method will simply get the next MM identifier and send the
     * json representation out for the channel change (this ensures unique messages for each MM notification)
     *
     * @param channelId a non-null, non-empty channelId
     * @param value the value for the channel
     * @throws IllegalArgumentException if channelID is null or empty
     */
    private void handleMMChange(String channelId, String value) {
        if (StringUtils.isEmpty(channelId)) {
            throw new NullArgumentException("channelId cannot be null or empty");
        }

        final AtomicInteger ai = mmSeqNbrs.get(channelId);
        if (ai == null) {
            logger.error("Channel {} does not have an ID configuration - programmer error!", channelId);
        } else {
            if (channelId.equals(RioConstants.CHANNEL_SOURCEMMINFOTEXT)) {
                value = handleMMInfoText(value);
                if (value == null) {
                    return;
                }
            }

            final int id = ai.getAndIncrement();

            final String json = gson.toJson(new IdValue(id, value));
            stateChanged(channelId, new StringType(json));
        }
    }

    /**
     * Helper method to handle MMInfoText notifications. There may be multiple infotext messages that represent a single
     * message. We know when we get the last info text when the MMATTR contains an 'E' (last item). Once we have the
     * last item, we update the channel with the complete message.
     *
     * @param infoTextValue the last info text value
     * @return a non-null containing the complete or null if the message isn't complete yet
     */
    private String handleMMInfoText(String infoTextValue) {
        final StatefulHandlerCallback callback = ((StatefulHandlerCallback) getCallback());

        final State attr = callback.getProperty(RioConstants.CHANNEL_SOURCEMMATTR);

        infoLock.lock();
        try {
            infoText.append(infoTextValue.toString());
            if (attr != null && attr.toString().indexOf("E") >= 0) {
                final String text = infoText.toString();

                infoText.setLength(0);
                callback.removeState(RioConstants.CHANNEL_SOURCEMMATTR);

                return text;
            }
            return null;
        } finally {
            infoLock.unlock();
        }
    }

    /**
     * Handles any source notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    private void handleSourceNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 3) {
            try {
                final int notifySource = Integer.parseInt(m.group(1));
                if (notifySource != source) {
                    return;
                }
                final String key = m.group(2).toLowerCase();
                final String value = m.group(3);

                switch (key) {
                    case SRC_NAME:
                        stateChanged(RioConstants.CHANNEL_SOURCENAME, new StringType(value));
                        break;

                    case SRC_TYPE:
                        stateChanged(RioConstants.CHANNEL_SOURCETYPE, new StringType(value));
                        break;

                    case SRC_IPADDRESS:
                        setProperty(RioConstants.PROPERTY_SOURCEIPADDRESS, value);
                        break;

                    case SRC_COMPOSERNAME:
                        stateChanged(RioConstants.CHANNEL_SOURCECOMPOSERNAME, new StringType(value));
                        break;

                    case SRC_CHANNEL:
                        stateChanged(RioConstants.CHANNEL_SOURCECHANNEL, new StringType(value));
                        break;

                    case SRC_CHANNELNAME:
                        stateChanged(RioConstants.CHANNEL_SOURCECHANNELNAME, new StringType(value));
                        break;

                    case SRC_GENRE:
                        stateChanged(RioConstants.CHANNEL_SOURCEGENRE, new StringType(value));
                        break;

                    case SRC_ARTISTNAME:
                        stateChanged(RioConstants.CHANNEL_SOURCEARTISTNAME, new StringType(value));
                        break;

                    case SRC_ALBUMNAME:
                        stateChanged(RioConstants.CHANNEL_SOURCEALBUMNAME, new StringType(value));
                        break;

                    case SRC_COVERARTURL:
                        stateChanged(RioConstants.CHANNEL_SOURCECOVERARTURL, new StringType(value));
                        break;

                    case SRC_PLAYLISTNAME:
                        stateChanged(RioConstants.CHANNEL_SOURCEPLAYLISTNAME, new StringType(value));
                        break;

                    case SRC_SONGNAME:
                        stateChanged(RioConstants.CHANNEL_SOURCESONGNAME, new StringType(value));
                        break;

                    case SRC_MODE:
                        stateChanged(RioConstants.CHANNEL_SOURCEMODE, new StringType(value));
                        break;

                    case SRC_SHUFFLEMODE:
                        stateChanged(RioConstants.CHANNEL_SOURCESHUFFLEMODE, new StringType(value));
                        break;

                    case SRC_REPEATMODE:
                        stateChanged(RioConstants.CHANNEL_SOURCEREPEATMODE, new StringType(value));
                        break;

                    case SRC_RATING:
                        stateChanged(RioConstants.CHANNEL_SOURCERATING, new StringType(value));
                        break;

                    case SRC_PROGRAMSERVICENAME:
                        stateChanged(RioConstants.CHANNEL_SOURCEPROGRAMSERVICENAME, new StringType(value));
                        break;

                    case SRC_RADIOTEXT:
                        stateChanged(RioConstants.CHANNEL_SOURCERADIOTEXT, new StringType(value));
                        break;

                    case SRC_RADIOTEXT2:
                        stateChanged(RioConstants.CHANNEL_SOURCERADIOTEXT2, new StringType(value));
                        break;

                    case SRC_RADIOTEXT3:
                        stateChanged(RioConstants.CHANNEL_SOURCERADIOTEXT3, new StringType(value));
                        break;

                    case SRC_RADIOTEXT4:
                        stateChanged(RioConstants.CHANNEL_SOURCERADIOTEXT4, new StringType(value));
                        break;

                    case SRC_VOLUME:
                        stateChanged(RioConstants.CHANNEL_SOURCEVOLUME, new StringType(value));
                        break;

                    case SRC_MMSCREEN:
                        handleMMChange(RioConstants.CHANNEL_SOURCEMMSCREEN, value);
                        break;

                    case SRC_MMTITLE:
                        handleMMChange(RioConstants.CHANNEL_SOURCEMMTITLE, value);
                        break;

                    case SRC_MMATTR:
                        handleMMChange(RioConstants.CHANNEL_SOURCEMMATTR, value);
                        break;

                    case SRC_MMBTNOK:
                        handleMMChange(RioConstants.CHANNEL_SOURCEMMBUTTONOKTEXT, value);
                        break;

                    case SRC_MMBTNBACK:
                        handleMMChange(RioConstants.CHANNEL_SOURCEMMBUTTONBACKTEXT, value);
                        break;

                    case SRC_MMHELP:
                        handleMMChange(RioConstants.CHANNEL_SOURCEMMHELPTEXT, value);
                        break;

                    case SRC_MMTEXTFIELD:
                        handleMMChange(RioConstants.CHANNEL_SOURCEMMTEXTFIELD, value);
                        break;

                    case SRC_MMINFOBLOCK:
                        handleMMChange(RioConstants.CHANNEL_SOURCEMMINFOTEXT, value);
                        break;
                    default:
                        logger.warn("Unknown source notification: '{}'", resp);
                        break;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid Source Notification (source not a parsable integer): '{}')", resp);
            }
        } else {
            logger.warn("Invalid Source Notification response: '{}'", resp);
        }

    }

    /**
     * Handles any bank notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    private void handleBankNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }

        // System notification
        if (m.groupCount() == 4) {
            try {
                final int bank = Integer.parseInt(m.group(2));
                if (bank >= 1 && bank <= 6) {
                    final int notifySource = Integer.parseInt(m.group(1));
                    if (notifySource != source) {
                        return;
                    }

                    final String key = m.group(3).toLowerCase();
                    final String value = m.group(4);

                    switch (key) {
                        case BANK_NAME:
                            banks[bank - 1].setName(value);
                            updateBanksChannel();
                            break;

                        default:
                            logger.warn("Unknown bank name notification: '{}'", resp);
                            break;
                    }
                } else {
                    logger.debug("Bank ID must be between 1 and 6: {}", resp);
                }

            } catch (NumberFormatException e) {
                logger.warn("Invalid Bank Name Notification (bank/source not a parsable integer): '{}')", resp);
            }

        } else {
            logger.warn("Invalid Bank Notification: '{}')", resp);
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
        if (StringUtils.isEmpty(response)) {
            return;
        }

        Matcher m = RSP_BANKNOTIFICATION.matcher(response);
        if (m.matches()) {
            handleBankNotification(m, response);
            return;
        }

        m = RSP_PRESETNOTIFICATION.matcher(response);
        if (m.matches()) {
            // does nothing
            return;
        }

        m = RSP_SRCNOTIFICATION.matcher(response);
        if (m.matches()) {
            handleSourceNotification(m, response);
        }

        m = RSP_MMMENUNOTIFICATION.matcher(response);
        if (m.matches()) {
            try {
                handleMMChange(RioConstants.CHANNEL_SOURCEMMMENU, response);
            } catch (NumberFormatException e) {
                logger.debug("Could not parse the menu text (1) from {}", response);
            }
        }
    }

    /**
     * Overrides the default implementation to turn watch off ({@link #watchSource(boolean)}) before calling the dispose
     */
    @Override
    public void dispose() {
        watchSource(false);
        if (httpClient != null) {
            try {
                httpClient.stop();
            } catch (Exception e) {
                logger.debug("Error stopping the httpclient: {}", e);
            }
        }
        super.dispose();
    }

    /**
     * The following class is simply used as a model for an id/value combination that will be serialized to JSON.
     * Nothing needs to be public because the serialization walks the properties.
     *
     * @author Tim Roberts
     *
     */
    @SuppressWarnings("unused")
    private class IdValue {
        /** The id of the value */
        private final int id;

        /** The value for the id */
        private final String value;

        /**
         * Constructions ID/Value from the given parms (no validations are done)
         *
         * @param id the identifier
         * @param value the associated value
         */
        public IdValue(int id, String value) {
            this.id = id;
            this.value = value;
        }
    }

}
