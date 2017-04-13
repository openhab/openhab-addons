/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants;
import org.openhab.binding.bosesoundtouch.handler.BoseSoundTouchHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The {@link XMLResponseProcessor} class handles all nowPlaying Channels
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */
public class XMLResponseProcessor {

    private final Logger logger = LoggerFactory.getLogger(XMLResponseProcessor.class);

    private BoseSoundTouchHandler handler;

    private Map<XMLHandlerState, Map<String, XMLHandlerState>> stateSwitchingMap;

    // channels exclusively updated by messages received from soundtouch handler...
    private ChannelUID channelNowPlayingAlbumUID;
    private ChannelUID channelNowPlayingArtistUID;
    private ChannelUID channelNowPlayingArtworkUID;
    private ChannelUID channelNowPlayingDescriptionUID;
    private ChannelUID channelNowPlayingGenreUID;
    private ChannelUID channelNowPlayingItemNameUID;
    private ChannelUID channelNowPlayingPlayStatusUID;
    private ChannelUID channelNowPlayingStationLocationUID;
    private ChannelUID channelNowPlayingStationNameUID;
    private ChannelUID channelNowPlayingTrackUID;
    private ChannelUID channelRateEnabled;
    private ChannelUID channelSkipEnabled;
    private ChannelUID channelSkipPreviousEnabled;

    public XMLResponseProcessor(BoseSoundTouchHandler handler) {
        this.handler = handler;
        init();
        channelNowPlayingAlbumUID = handler.getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_ALBUM);
        channelNowPlayingArtworkUID = handler.getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_ARTWORK);
        channelNowPlayingArtistUID = handler.getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_ARTIST);
        channelNowPlayingDescriptionUID = handler
                .getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_DESCRIPTION);
        channelNowPlayingItemNameUID = handler
                .getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_ITEMNAME);
        channelNowPlayingGenreUID = handler.getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_GENRE);
        channelNowPlayingPlayStatusUID = handler
                .getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_PLAYSTATUS);
        channelNowPlayingStationLocationUID = handler
                .getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_STATIONLOCATION);
        channelNowPlayingStationNameUID = handler
                .getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_STATIONNAME);
        channelNowPlayingTrackUID = handler.getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_TRACK);
        channelRateEnabled = handler.getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_RATEENABLED);
        channelSkipEnabled = handler.getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_SKIPENABLED);
        channelSkipPreviousEnabled = handler.getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_SKIPPREVIOUSENABLED);
    }

    // initializes our XML parsing state machine
    private void init() {
        stateSwitchingMap = new HashMap<>();
        Map<String, XMLHandlerState> msgBodyMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.MsgBody, msgBodyMap);
        msgBodyMap.put("info", XMLHandlerState.Info);
        msgBodyMap.put("volume", XMLHandlerState.Volume);
        msgBodyMap.put("presets", XMLHandlerState.Presets);
        msgBodyMap.put("key", XMLHandlerState.Unprocessed); // only confirmation of our key presses...
        msgBodyMap.put("status", XMLHandlerState.Unprocessed); // only confirmation of commands sent to device...
        msgBodyMap.put("zone", XMLHandlerState.Zone); // only confirmation of our key presses...
        msgBodyMap.put("bass", XMLHandlerState.Bass);

        // info message states
        Map<String, XMLHandlerState> infoMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Info, infoMap);
        infoMap.put("components", XMLHandlerState.Unprocessed); // TODO read software version and serial number
        infoMap.put("name", XMLHandlerState.InfoName);
        infoMap.put("type", XMLHandlerState.InfoType);
        infoMap.put("networkInfo", XMLHandlerState.Unprocessed);
        infoMap.put("margeAccountUUID", XMLHandlerState.Unprocessed);
        infoMap.put("margeURL", XMLHandlerState.Unprocessed);
        infoMap.put("moduleType", XMLHandlerState.Unprocessed);
        infoMap.put("variant", XMLHandlerState.Unprocessed);
        infoMap.put("variantMode", XMLHandlerState.Unprocessed);
        infoMap.put("countryCode", XMLHandlerState.Unprocessed);
        infoMap.put("regionCode", XMLHandlerState.Unprocessed);

        Map<String, XMLHandlerState> updatesMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Updates, updatesMap);
        updatesMap.put("clockDisplayUpdated", XMLHandlerState.Unprocessed); // can we get anything useful of that?
        updatesMap.put("connectionStateUpdated", XMLHandlerState.UnprocessedNoTextExpected);
        updatesMap.put("infoUpdated", XMLHandlerState.Unprocessed);
        updatesMap.put("bassUpdated", XMLHandlerState.BassUpdated);
        updatesMap.put("nowPlayingUpdated", XMLHandlerState.MsgBody);
        updatesMap.put("nowSelectionUpdated", XMLHandlerState.Unprocessed); // TODO this seems to be quite a useful info
                                                                            // what is currently played..
        updatesMap.put("recentsUpdated", XMLHandlerState.Unprocessed);
        updatesMap.put("volumeUpdated", XMLHandlerState.MsgBody);
        updatesMap.put("zoneUpdated", XMLHandlerState.ZoneUpdated); // just notifies but dosn't provide details

        Map<String, XMLHandlerState> volume = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Volume, volume);
        volume.put("targetvolume", XMLHandlerState.Unprocessed);
        volume.put("actualvolume", XMLHandlerState.VolumeActual);
        volume.put("muteenabled", XMLHandlerState.VolumeMuteEnabled);

        Map<String, XMLHandlerState> nowPlayingMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.NowPlaying, nowPlayingMap);
        nowPlayingMap.put("album", XMLHandlerState.NowPlayingAlbum);
        nowPlayingMap.put("art", XMLHandlerState.NowPlayingArt);
        nowPlayingMap.put("artist", XMLHandlerState.NowPlayingArtist);
        nowPlayingMap.put("ContentItem", XMLHandlerState.ContentItem);
        nowPlayingMap.put("description", XMLHandlerState.NowPlayingDescription);
        nowPlayingMap.put("playStatus", XMLHandlerState.NowPlayingPlayStatus);
        nowPlayingMap.put("rateEnabled", XMLHandlerState.NowPlayingRateEnabled);
        nowPlayingMap.put("skipEnabled", XMLHandlerState.NowPlayingSkipEnabled);
        nowPlayingMap.put("skipPreviousEnabled", XMLHandlerState.NowPlayingSkipPreviousEnabled);
        nowPlayingMap.put("stationLocation", XMLHandlerState.NowPlayingStationLocation);
        nowPlayingMap.put("stationName", XMLHandlerState.NowPlayingStationName);
        nowPlayingMap.put("track", XMLHandlerState.NowPlayingTrack);
        nowPlayingMap.put("connectionStatusInfo", XMLHandlerState.Unprocessed); // TODO active when Source==Bluetooth
        // TODO active when Source==Pandora and maybe also other sources - seems to be rating related
        nowPlayingMap.put("time", XMLHandlerState.Unprocessed);
        nowPlayingMap.put("rating", XMLHandlerState.Unprocessed);
        nowPlayingMap.put("rateEnabled", XMLHandlerState.Unprocessed);

        // ContentItem specifies a resource (that also could be bookmarked in a preset)
        Map<String, XMLHandlerState> contentItemMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.ContentItem, contentItemMap);
        contentItemMap.put("itemName", XMLHandlerState.ContentItemItemName);
        contentItemMap.put("containerArt", XMLHandlerState.Unprocessed);

        Map<String, XMLHandlerState> presetMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Preset, presetMap);
        presetMap.put("ContentItem", XMLHandlerState.ContentItem);

        Map<String, XMLHandlerState> zoneMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Zone, zoneMap);
        zoneMap.put("member", XMLHandlerState.ZoneMember);

        Map<String, XMLHandlerState> bassMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Bass, bassMap);
        bassMap.put("targetbass", XMLHandlerState.BassTargetValue);
        bassMap.put("actualbass", XMLHandlerState.BassActualValue);

    }

    public void handleMessage(String msg) {
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(new XMLResponseHandler(this, handler, stateSwitchingMap));
            reader.parse(new InputSource(new StringReader(msg)));
        } catch (IOException e) {
            // This should never happen - we're not performing I/O!
            logger.error("{}: Could not parse XML from string '{}'; exception is: ", handler.getDeviceName(), msg, e);
        } catch (Throwable s) {
            logger.error("{}: Could not parse XML from string '{}'; exception is: ", handler.getDeviceName(), msg, s);
        }
    }

    protected void updateNowPlayingAlbum(State state) {
        handler.updateState(channelNowPlayingAlbumUID, state);
    }

    protected void updateNowPlayingArtwork(State state) {
        handler.updateState(channelNowPlayingArtworkUID, state);
    }

    protected void updateNowPlayingArtist(State state) {
        handler.updateState(channelNowPlayingArtistUID, state);
    }

    protected void updateNowPlayingDescription(State state) {
        handler.updateState(channelNowPlayingDescriptionUID, state);
    }

    protected void updateNowPlayingGenre(State state) {
        handler.updateState(channelNowPlayingGenreUID, state);
    }

    protected void updateNowPlayingItemName(State state) {
        handler.updateState(channelNowPlayingItemNameUID, state);
    }

    protected void updateNowPlayingPlayStatus(State state) {
        handler.updateState(channelNowPlayingPlayStatusUID, state);
    }

    protected void updateNowPlayingStationLocation(State state) {
        handler.updateState(channelNowPlayingStationLocationUID, state);
    }

    protected void updateNowPlayingStationName(State state) {
        handler.updateState(channelNowPlayingStationNameUID, state);
    }

    protected void updateNowPlayingTrack(State state) {
        handler.updateState(channelNowPlayingTrackUID, state);
    }

    protected void updateRateEnabled(State state) {
        handler.updateState(channelRateEnabled, state);
    }

    protected void updateSkipEnabled(State state) {
        handler.updateState(channelSkipEnabled, state);
    }

    protected void updateSkipPreviousEnabled(State state) {
        handler.updateState(channelSkipPreviousEnabled, state);
    }

}