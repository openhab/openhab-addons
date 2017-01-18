/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants;
import org.openhab.binding.bosesoundtouch.handler.BoseSoundTouchHandler;
import org.openhab.binding.bosesoundtouch.internal.items.ContentItem;
import org.openhab.binding.bosesoundtouch.internal.items.Preset;
import org.openhab.binding.bosesoundtouch.internal.items.ZoneMember;
import org.openhab.binding.bosesoundtouch.types.OperationModeType;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */
public class XMLResponseHandler extends DefaultHandler {

    private Logger logger;
    private boolean tracing;

    private BoseSoundTouchHandler handler;

    private Map<XMLHandlerState, Map<String, XMLHandlerState>> stateSwitchingMap;

    private Stack<XMLHandlerState> states;
    private XMLHandlerState state;
    private boolean msgHeaderWasValid;

    private Preset preset;
    private ContentItem contentItem;
    private boolean volumeMuteEnabled;
    private OnOffType rateEnabled;
    private OnOffType skipEnabled;
    private OnOffType skipPreviousEnabled;
    private ZoneState zoneState;
    private BoseSoundTouchHandler zoneMaster;
    private List<ZoneMember> zoneMembers;
    private String zoneMemberIp;

    public XMLResponseHandler(BoseSoundTouchHandler boseSoundTouchHandler) {
        states = new Stack<>();
        state = XMLHandlerState.INIT;
        this.handler = boseSoundTouchHandler;
        logger = boseSoundTouchHandler.getLogger();
        tracing = logger.isTraceEnabled();
        init();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (tracing) {
            logger.trace(handler.getDeviceName() + ": startElement(\"" + localName + "\"; state: " + state + ")");
        }
        states.push(state);
        XMLHandlerState curState = state; // save for switch statement
        Map<String, XMLHandlerState> stateMap = stateSwitchingMap.get(state);
        state = XMLHandlerState.Unprocessed; // set default value; we avoid default in select to have the compiler
                                             // showing a
        // warning for unhandled states
        switch (curState) {
            case INIT:
                if ("updates".equals(localName)) {
                    // it just seems to be a ping - havn't seen any data on it..
                    if (checkDeviceId(localName, attributes, false)) {
                        state = XMLHandlerState.Updates;
                    } else {
                        state = XMLHandlerState.Unprocessed;
                    }
                } else if ("msg".equals(localName)) {
                    // message
                    state = XMLHandlerState.Msg;
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                    }
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            case Msg:
                if ("header".equals(localName)) {
                    // message
                    if (checkDeviceId(localName, attributes, false)) {
                        state = XMLHandlerState.MsgHeader;
                        msgHeaderWasValid = true;
                    } else {
                        state = XMLHandlerState.Unprocessed;
                    }
                } else if ("body".equals(localName)) {
                    if (msgHeaderWasValid) {
                        state = XMLHandlerState.MsgBody;
                    } else {
                        state = XMLHandlerState.Unprocessed;
                    }
                } else {
                    logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            case MsgHeader:
                if ("request".equals(localName)) {
                    state = XMLHandlerState.Unprocessed; // TODO implement request id / response tracking...
                } else {
                    logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            case MsgBody:
                if ("nowPlaying".equals(localName)) {
                    if (!checkDeviceId(localName, attributes, true)) {
                        state = XMLHandlerState.Unprocessed;
                        break;
                    }
                    rateEnabled = OnOffType.OFF;
                    skipEnabled = OnOffType.OFF;
                    skipPreviousEnabled = OnOffType.OFF;
                    state = XMLHandlerState.NowPlaying;
                    String source = attributes.getValue("source");
                    if (handler.getNowPlayingSource() == null
                            || !handler.getNowPlayingSource().toString().equals(source)) {
                        // source changed
                        handler.updateNowPlayingSource(new StringType(source));
                        // reset enabled states
                        handler.updateRateEnabled(OnOffType.OFF);
                        handler.updateSkipEnabled(OnOffType.OFF);
                        handler.updateSkipPreviousEnabled(OnOffType.OFF);

                        // clear all "nowPlaying" details on source change...
                        handler.updateNowPlayingAlbum(new StringType(""));
                        handler.updateNowPlayingArtwork(new StringType(""));
                        handler.updateNowPlayingArtist(new StringType(""));
                        handler.updateNowPlayingDescription(new StringType(""));
                        handler.updateNowPlayingGenre(new StringType(""));
                        handler.updateNowPlayingItemName(new StringType(""));
                        handler.updateNowPlayingPlayStatus(new StringType(""));
                        handler.updateNowPlayingStationLocation(new StringType(""));
                        handler.updateNowPlayingStationName(new StringType(""));
                        handler.updateNowPlayingTrack(new StringType(""));
                    }
                } else if ("zone".equals(localName)) {
                    zoneMembers = new ArrayList<>();
                    String master = attributes.getValue("master");
                    if (master == null || master.isEmpty()) {
                        zoneMaster = null;
                        zoneState = ZoneState.None;
                    } else {
                        if (master.equals(handler.getMacAddress())) {
                            // we are the master...
                            zoneState = ZoneState.Master;
                        } else {
                            // an other device is the master
                            zoneState = ZoneState.Member;
                            zoneMaster = handler.getFactory().getBoseSoundTouchDevice(master);
                            if (zoneMaster == null) {
                                logger.warn(handler.getDeviceName() + ": Zone update: Unable to find master with ID "
                                        + master);
                            }
                        }
                    }
                    state = XMLHandlerState.Zone;
                } else {
                    state = stateMap.get(localName);
                    if (state == null) {
                        logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                        state = XMLHandlerState.Unprocessed;
                    } else if (state != XMLHandlerState.Volume && state != XMLHandlerState.Presets
                            && state != XMLHandlerState.Unprocessed) {
                        if (!checkDeviceId(localName, attributes, false)) {
                            state = XMLHandlerState.Unprocessed;
                            break;
                        }
                    }
                }
                break;
            case Presets:
                if ("preset".equals(localName)) {
                    state = XMLHandlerState.Preset;
                    String id = attributes.getValue("id");
                    preset = new Preset(Integer.parseInt(id));
                } else {
                    logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            case Zone:
                zoneMemberIp = attributes.getValue("ipaddress");
                state = nextState(stateMap, curState, localName);
                break;
            case NowPlayingRateEnabled:
                rateEnabled = OnOffType.ON;
                state = nextState(stateMap, curState, localName);
                break;
            case NowPlayingSkipEnabled:
                skipEnabled = OnOffType.ON;
                state = nextState(stateMap, curState, localName);
                break;
            case NowPlayingSkipPreviousEnabled:
                skipPreviousEnabled = OnOffType.ON;
                state = nextState(stateMap, curState, localName);
                break;

            case ContentItem:
            case Info:
            case NowPlaying:
            case Preset:
            case Updates:
            case Volume:
                state = nextState(stateMap, curState, localName);
                break;
            // all entities without any children expected..
            case ContentItemItemName:
            case InfoName:
            case InfoType:
            case NowPlayingAlbum:
            case NowPlayingArt:
            case NowPlayingArtist:
            case NowPlayingGenre:
            case NowPlayingDescription:
            case NowPlayingPlayStatus:
            case NowPlayingStationLocation:
            case NowPlayingStationName:
            case NowPlayingTrack:
            case VolumeActual:
            case VolumeMuteEnabled:
            case ZoneMember:
            case ZoneUpdated: // currently this dosn't provide any zone details..
                logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                state = XMLHandlerState.Unprocessed;
                break;
            case Unprocessed:
                // all further things are also unprocessed
                state = XMLHandlerState.Unprocessed;
                break;
            case UnprocessedNoTextExpected:
                state = XMLHandlerState.UnprocessedNoTextExpected;
                break;
        }
        if (state == XMLHandlerState.ContentItem) {
            // // we started a content item. process data.
            // contentItem = new ContentItem();
            // String source = attributes.getValue("source");
            // if (source != null) {
            // try {
            // contentItem.source = Source.valueOf(source);
            // } catch (Throwable t) {
            // logger.error(boseSoundTouchHandler.getThing() + ": Unknown Source: "
            // + source + " - needs to be defined!");
            // contentItem.source = Source.UNKNOWN;
            // }
            // }
            // contentItem.location = attributes.getValue("location");
            // contentItem.sourceAccount = attributes.getValue("sourceAccount");
            // we started a content item. process data.
            contentItem = new ContentItem();
            String source = attributes.getValue("source");
            if (source.equals("INTERNET_RADIO")) {
                contentItem.setOperationMode(OperationModeType.INTERNET_RADIO);
            } else if (source.equals("STANDBY")) {
                contentItem.setOperationMode(OperationModeType.STANDBY);
            } else if (source.equals("AUX")) {
                contentItem.setOperationMode(OperationModeType.AUX);
            } else if (source.equals("BLUETOOTH")) {
                contentItem.setOperationMode(OperationModeType.BLUETOOTH);
            } else if (source.equals("STORED_MUSIC")) {
                contentItem.setOperationMode(OperationModeType.STORED_MUSIC);
            } else {
                contentItem.setOperationMode(OperationModeType.OTHER);
                logger.error(handler.getThing() + ": Unknown SourceType: " + source + " - needs to be defined!");
            }
            // TODO Implement other sources
            contentItem.setLocation(attributes.getValue("location"));
            contentItem.setSourceAccount(attributes.getValue("sourceAccount"));
        }
        if (state == XMLHandlerState.Presets) {
            handler.clearListOfPresets();
        }
        if (state == XMLHandlerState.Volume) {
            volumeMuteEnabled = false;
        }
    }

    private XMLHandlerState nextState(Map<String, XMLHandlerState> stateMap, XMLHandlerState curState,
            String localName) {
        XMLHandlerState state = stateMap.get(localName);
        if (state == null) {
            logger.warn("Unhandled XML entity during " + curState + ": " + localName);
            state = XMLHandlerState.Unprocessed;
        }
        return state;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (tracing) {
            logger.trace(handler.getDeviceName() + ": endElement(\"" + localName + "\")");
        }
        final XMLHandlerState prevState = state;
        state = states.pop();
        if (prevState == XMLHandlerState.Info) {
            handler.sendRequestInWebSocket("volume");
            handler.sendRequestInWebSocket("presets");
            handler.sendRequestInWebSocket("now_playing");
            handler.sendRequestInWebSocket("getZone");
        }
        if (prevState == XMLHandlerState.ContentItem && state == XMLHandlerState.NowPlaying) {
            // update now playing name...
            handler.updateNowPlayingItemName(new StringType(contentItem.getItemName()));
            handler.setCurrentContentItem(contentItem);
            handler.checkOperationMode();
        }
        if (prevState == XMLHandlerState.ContentItem && state == XMLHandlerState.Preset) {
            preset.setContentItem(contentItem);
        }
        if (prevState == XMLHandlerState.Preset && state == XMLHandlerState.Presets) {
            handler.addPresetToList(preset);
            handler.checkOperationMode();
        }
        if (prevState == XMLHandlerState.NowPlaying && state == XMLHandlerState.MsgBody) {
            handler.updateRateEnabled(rateEnabled);
            handler.updateSkipEnabled(skipEnabled);
            handler.updateSkipPreviousEnabled(skipPreviousEnabled);
        }
        if (prevState == XMLHandlerState.Volume) {
            if (handler.isMuted() != volumeMuteEnabled) {
                handler.setMuted(volumeMuteEnabled);
                handler.updateVolumeMuted(handler.isMuted() ? OnOffType.ON : OnOffType.OFF);
            }
        }
        if (prevState == XMLHandlerState.ZoneUpdated) {
            handler.sendRequestInWebSocket("getZone");
        }
        if (prevState == XMLHandlerState.Zone) {
            handler.updateZoneState(zoneState, zoneMaster, zoneMembers);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (tracing) {
            logger.trace(handler.getDeviceName() + ":Text data during " + state + ": " + new String(ch, start, length));
        }
        super.characters(ch, start, length);
        switch (state) {
            case INIT:
            case Msg:
            case MsgHeader:
            case MsgBody:
            case Updates:
            case Volume:
            case Info:
            case Preset:
            case Presets:
            case NowPlaying:
            case NowPlayingRateEnabled:
            case NowPlayingSkipEnabled:
            case NowPlayingSkipPreviousEnabled:
            case ContentItem:
            case UnprocessedNoTextExpected:
            case Zone:
            case ZoneUpdated:
                logger.warn("Unexpected text data during " + state + ": " + new String(ch, start, length));
                break;
            case Unprocessed:
                // drop quietly..
                break;
            case InfoName:
                setConfigOption(BoseSoundTouchBindingConstants.DEVICE_INFO_NAME, new String(ch, start, length));
                break;
            case InfoType:
                setConfigOption(BoseSoundTouchBindingConstants.DEVICE_INFO_TYPE, new String(ch, start, length));
                break;
            case NowPlayingAlbum:
                handler.updateNowPlayingAlbum(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingArt:
                handler.updateNowPlayingArtwork(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingArtist:
                handler.updateNowPlayingArtist(new StringType(new String(ch, start, length)));
                break;
            case ContentItemItemName:
                contentItem.setItemName(new String(ch, start, length));
                break;
            case NowPlayingDescription:
                handler.updateNowPlayingDescription(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingGenre:
                handler.updateNowPlayingGenre(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingPlayStatus:
                handler.updateNowPlayingPlayStatus(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingStationLocation:
                handler.updateNowPlayingStationLocation(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingStationName:
                handler.updateNowPlayingStationName(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingTrack:
                handler.updateNowPlayingTrack(new StringType(new String(ch, start, length)));
                break;
            case VolumeActual:
                String temp = new String(ch, start, length);
                handler.updateVolume(new PercentType(Integer.parseInt(temp)));
                break;
            case VolumeMuteEnabled:
                volumeMuteEnabled = Boolean.parseBoolean(new String(ch, start, length));
                break;
            case ZoneMember:
                String mac = new String(ch, start, length);
                BoseSoundTouchHandler memberHandler = handler.getFactory().getBoseSoundTouchDevice(mac);
                if (memberHandler == null) {
                    logger.warn("Zone update: Unable to find member with ID " + mac);
                } else {
                    ZoneMember zoneMember = new ZoneMember();
                    zoneMember.setIp(zoneMemberIp);
                    zoneMember.setMac(mac);
                    zoneMember.setHandler(memberHandler);
                    zoneMembers.add(zoneMember);
                }
                break;
        }
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        super.skippedEntity(name);
    }

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
    }

    private boolean checkDeviceId(String localName, Attributes attributes, boolean allowFromMaster) {
        String did = attributes.getValue("deviceID");
        if (did == null) {
            logger.warn("No Device-ID in Entity " + localName);
            return false;
        }
        if (did.equals(handler.getMacAddress())) {
            return true;
        }
        if (allowFromMaster && handler.getZoneMaster() != null && did.equals(handler.getZoneMaster().getMacAddress())) {
            return true;
        }
        logger.warn(
                "Wrong Device-ID in Entity " + localName + ": Got: " + did + " expected: " + handler.getMacAddress());
        return false;
    }

    private void setConfigOption(String option, String value) {
        Map<String, String> prop = handler.getThing().getProperties();
        String cur = prop.get(option);
        if (cur == null || !cur.equals(value)) {
            logger.info("Option \"" + option + "\" updated: From \"" + cur + "\" to \"" + value + "\"");
            handler.getThing().setProperty(option, value);
        }
    }
}