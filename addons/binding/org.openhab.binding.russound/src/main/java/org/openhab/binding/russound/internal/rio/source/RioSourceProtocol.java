/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.source;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.binding.russound.internal.rio.AbstractRioProtocol;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the protocol handler for the Russound Source. This handler will issue the protocol commands and will
 * process the responses from the Russound system. Please see documentation for what channels are supported by which
 * source types.
 *
 * @author Tim Roberts
 *
 */
class RioSourceProtocol extends AbstractRioProtocol {
    private Logger logger = LoggerFactory.getLogger(RioSourceProtocol.class);

    /**
     * The source identifier (1-12)
     */
    private final int _source;

    // Protocol constants
    private final static String SRC_NAME = "name";
    private final static String SRC_TYPE = "type";
    private final static String SRC_IPADDRESS = "ipAddress";
    private final static String SRC_IPADDRESS2 = "IPAddress"; // russound wasn't consistent on capitalization on
                                                              // notifications
    private final static String SRC_COMPOSERNAME = "composerName";
    private final static String SRC_CHANNEL = "channel";
    private final static String SRC_CHANNELNAME = "channelName";
    private final static String SRC_GENRE = "genre";
    private final static String SRC_ARTISTNAME = "artistName";
    private final static String SRC_ALBUMNAME = "albumName";
    private final static String SRC_COVERARTURL = "coverArtURL";
    private final static String SRC_PLAYLISTNAME = "playlistName";
    private final static String SRC_SONGNAME = "songName";
    private final static String SRC_MODE = "mode";
    private final static String SRC_SHUFFLEMODE = "shuffleMode";
    private final static String SRC_REPEATMODE = "repeatMode";
    private final static String SRC_RATING = "rating";
    private final static String SRC_PROGRAMSERVICENAME = "programServiceName";
    private final static String SRC_RADIOTEXT = "radioText";
    private final static String SRC_RADIOTEXT2 = "radioText2";
    private final static String SRC_RADIOTEXT3 = "radioText3";
    private final static String SRC_RADIOTEXT4 = "radioText4";

    // This is an undocumented volume
    private final static String SRC_VOLUME = "volume";

    // Response patterns
    private final Pattern RSP_SRCNOTIFICATION = Pattern.compile("^[SN] S\\[(\\d+)\\]\\.(\\w+)=\"(.*)\"$");

    /**
     * The client used for http requests
     */
    private final HttpClient _httpClient;

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
        _source = source;
        _httpClient = new HttpClient();
        _httpClient.setFollowRedirects(true);
        _httpClient.start();
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
        sendCommand("GET S[" + _source + "]." + keyName);
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
     * Turns on/off watching the source for notifications
     *
     * @param watch true to turn on, false to turn off
     */
    void watchSource(boolean watch) {
        sendCommand("WATCH S[" + _source + "] " + (watch ? "ON" : "OFF"));
    }

    private void handleCoverArt(String url) {
        stateChanged(RioConstants.CHANNEL_SOURCECOVERARTURL, new StringType(url));

        if (StringUtils.isEmpty(url)) {
            stateChanged(RioConstants.CHANNEL_SOURCECOVERART, UnDefType.UNDEF);
        } else {
            try {
                final ContentResponse content = _httpClient.GET(url);
                stateChanged(RioConstants.CHANNEL_SOURCECOVERART, new RawType(content.getContent()));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.warn("Exception retrieving cover art image from {}: {}", url, e);
                stateChanged(RioConstants.CHANNEL_SOURCECOVERART, UnDefType.UNDEF);
            }
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
                final int source = Integer.parseInt(m.group(1));
                if (source != _source) {
                    return;
                }
                final String key = m.group(2);
                final String value = m.group(3);

                switch (key) {
                    case SRC_NAME:
                        stateChanged(RioConstants.CHANNEL_SOURCENAME, new StringType(value));
                        break;

                    case SRC_TYPE:
                        setProperty(RioConstants.PROPERTY_SOURCETYPE, value);
                        break;

                    case SRC_IPADDRESS:
                    case SRC_IPADDRESS2:
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
                        handleCoverArt(value);
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

        final Matcher m = RSP_SRCNOTIFICATION.matcher(response);
        if (m.matches()) {
            handleSourceNotification(m, response);
        }
    }

    /**
     * Overrides the default implementation to turn watch off ({@link #watchSource(boolean)}) before calling the dispose
     */
    @Override
    public void dispose() {
        watchSource(false);
        if (_httpClient != null) {
            try {
                _httpClient.stop();
            } catch (Exception e) {
                logger.debug("Error stopping the httpclient: {}", e);
            }
        }
        super.dispose();
    }
}
