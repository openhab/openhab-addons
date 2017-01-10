/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

import java.util.HashMap;
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

    private BoseSoundTouchHandler boseSoundTouchHandler;

    private Map<XMLHandlerState, Map<String, XMLHandlerState>> stateSwitchingMap;

    private ContentItem contentItem;
    private Stack<XMLHandlerState> states;
    private XMLHandlerState state;
    private boolean msgHeaderWasValid;
    private Preset preset;
    private boolean volumeMuteEnabled;
    private ZoneMember zoneMember;
    private Logger logger;

    public XMLResponseHandler(BoseSoundTouchHandler boseSoundTouchHandler) {
        states = new Stack<>();
        state = XMLHandlerState.INIT;
        this.boseSoundTouchHandler = boseSoundTouchHandler;
        logger = boseSoundTouchHandler.getLogger();
        init();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        logger.debug("startElement(\"" + localName + "\"; state: " + state + ")");
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
                    if (checkDeviceId(localName, attributes)) {
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
                    if (checkDeviceId(localName, attributes)) {
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
                    if (!checkDeviceId(localName, attributes)) {
                        state = XMLHandlerState.Unprocessed;
                        break;
                    }
                    state = XMLHandlerState.NowPlaying;
                    String source = attributes.getValue("source");
                    if (boseSoundTouchHandler.getCurrentSourceString() == null
                            || !boseSoundTouchHandler.getCurrentSourceString().equals(source)) {
                        // source changed
                        boseSoundTouchHandler.updateNowPlayingSource(new StringType(source));
                        // clear all "nowPlaying" details on source change...
                        boseSoundTouchHandler.updateNowPlayingAlbum(new StringType(""));
                        boseSoundTouchHandler.updateNowPlayingArtwork(new StringType(""));
                        boseSoundTouchHandler.updateNowPlayingArtist(new StringType(""));
                        boseSoundTouchHandler.updateNowPlayingDescription(new StringType(""));
                        boseSoundTouchHandler.updateNowPlayingItemName(new StringType(""));
                        boseSoundTouchHandler.updateNowPlayingPlayStatus(new StringType(""));
                        boseSoundTouchHandler.updateNowPlayingStationLocation(new StringType(""));
                        boseSoundTouchHandler.updateNowPlayingStationName(new StringType(""));
                        boseSoundTouchHandler.updateNowPlayingTrack(new StringType(""));
                    }
                } else if ("zone".equals(localName)) {
                    String master = attributes.getValue("master");
                    if (master == null || master.isEmpty()) {
                        boseSoundTouchHandler.setMasterZoneSoundTouchHandler(null);
                        boseSoundTouchHandler.setZoneState(ZoneState.None);
                    } else {
                        if (master.equals(boseSoundTouchHandler.getMacAddress())) {
                            // we are the master...
                            boseSoundTouchHandler.setZoneState(ZoneState.Master);
                        } else {
                            // an other device is the master
                            boseSoundTouchHandler.setZoneState(ZoneState.Master);
                            boseSoundTouchHandler.setMasterZoneSoundTouchHandler(
                                    boseSoundTouchHandler.getBoseSoundTouchHandler(master));
                            if (boseSoundTouchHandler.getMasterZoneSoundTouchHandler() == null) {
                                logger.warn("Zone update: Unable to find master with ID " + master);
                            }
                        }
                    }
                    state = XMLHandlerState.Zone;
                } else {
                    state = stateMap.get(localName);
                    if (state == null) {
                        logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                        state = XMLHandlerState.Unprocessed;
                    } else if (state != XMLHandlerState.Volume && state != XMLHandlerState.Presets) {
                        if (!checkDeviceId(localName, attributes)) {
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
                    this.preset = new Preset();
                    this.preset.setPos(Integer.parseInt(id));
                } else {
                    logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            case Zone:
                zoneMember = new ZoneMember();
                zoneMember.setIp(attributes.getValue("ipaddress"));
                boseSoundTouchHandler.addZoneMember(zoneMember);
                state = stateMap.get(localName);
                if (state == null) {
                    logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            case ContentItem:
            case Info:
            case NowPlaying:
            case Preset:
            case Updates:
            case Volume:
                state = stateMap.get(localName);
                if (state == null) {
                    logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            // all entities without any children expected..
            case ContentItemItemName:
            case InfoName:
            case InfoType:
            case NowPlayingAlbum:
            case NowPlayingArt:
            case NowPlayingArtist:
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
                logger.error(boseSoundTouchHandler.getThing() + ": Unknown SourceType: " + source
                        + " - needs to be defined!");
            }
            // TODO Implement other sources
            contentItem.setLocation(attributes.getValue("location"));
            contentItem.setSourceAccount(attributes.getValue("sourceAccount"));
        }
        if (state == XMLHandlerState.Presets) {
            boseSoundTouchHandler.clearListOfPresets();
        }
        if (state == XMLHandlerState.Volume) {
            volumeMuteEnabled = false;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        logger.debug("endElement(\"" + localName + "\")");
        final XMLHandlerState prevState = state;
        state = states.pop();
        if (prevState == XMLHandlerState.Info) {
            boseSoundTouchHandler.sendRequestInWebSocket("volume");
            boseSoundTouchHandler.sendRequestInWebSocket("presets");
            boseSoundTouchHandler.sendRequestInWebSocket("now_playing");
            boseSoundTouchHandler.sendRequestInWebSocket("getZone");
        }
        if (prevState == XMLHandlerState.ContentItem && state == XMLHandlerState.NowPlaying) {
            // update now playing name...
            if (contentItem.getItemName() == null) {
                contentItem.setItemName(""); // null values cause exceptions in openhab...
            }
            boseSoundTouchHandler.updateNowPlayingItemName(new StringType(contentItem.getItemName()));
            boseSoundTouchHandler.setCurrentContentItem(contentItem);
            boseSoundTouchHandler.checkOperationMode();
        }
        if (prevState == XMLHandlerState.ContentItem && state == XMLHandlerState.Preset) {
            preset.setContentItem(contentItem);
        }
        if (prevState == XMLHandlerState.Preset && state == XMLHandlerState.Presets) {
            boseSoundTouchHandler.addPresetToList(preset);
            boseSoundTouchHandler.checkOperationMode();
        }
        if (prevState == XMLHandlerState.Volume) {
            if (boseSoundTouchHandler.isMuted() != volumeMuteEnabled) {
                boseSoundTouchHandler.setMuted(volumeMuteEnabled);
                boseSoundTouchHandler.updateVolumeMuted(boseSoundTouchHandler.isMuted() ? OnOffType.ON : OnOffType.OFF);
            }
        }
        if (prevState == XMLHandlerState.ZoneUpdated) {
            boseSoundTouchHandler.sendRequestInWebSocket("getZone");
        }
        if (prevState == XMLHandlerState.Zone) {
            boseSoundTouchHandler.zonesChanged();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        logger.debug("Text data during " + state + ": " + new String(ch, start, length));
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
                boseSoundTouchHandler.updateNowPlayingAlbum(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingArt:
                boseSoundTouchHandler.updateNowPlayingArtwork(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingArtist:
                boseSoundTouchHandler.updateNowPlayingArtist(new StringType(new String(ch, start, length)));
                break;
            case ContentItemItemName:
                contentItem.setItemName(new String(ch, start, length));
                break;
            case NowPlayingDescription:
                boseSoundTouchHandler.updateNowPlayingDescription(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingPlayStatus:
                boseSoundTouchHandler.updateNowPlayingPlayStatus(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingStationLocation:
                boseSoundTouchHandler.updateNowPlayingStationLocation(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingStationName:
                boseSoundTouchHandler.updateNowPlayingStationName(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingTrack:
                boseSoundTouchHandler.updateNowPlayingTrack(new StringType(new String(ch, start, length)));
                break;
            case VolumeActual:
                String temp = new String(ch, start, length);
                boseSoundTouchHandler.updateVolume(new PercentType(Integer.parseInt(temp)));
                break;
            case VolumeMuteEnabled:
                volumeMuteEnabled = Boolean.parseBoolean(new String(ch, start, length));
                break;
            case ZoneMember:
                String mac = new String(ch, start, length);
                zoneMember.setMac(mac);
                zoneMember.setHandler(boseSoundTouchHandler.getBoseSoundTouchHandler(mac));
                if (zoneMember.getHandler() == null) {
                    logger.warn("Zone update: Unable to find member with ID " + mac);
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
        nowPlayingMap.put("stationLocation", XMLHandlerState.NowPlayingStationLocation);
        nowPlayingMap.put("stationName", XMLHandlerState.NowPlayingStationName);
        nowPlayingMap.put("track", XMLHandlerState.NowPlayingTrack);
        nowPlayingMap.put("connectionStatusInfo", XMLHandlerState.Unprocessed); // TODO active when Source==Bluetooth
        // TODO active when Source==Pandora and maybe also other sources - seems to be rating related
        nowPlayingMap.put("time", XMLHandlerState.Unprocessed);
        nowPlayingMap.put("rating", XMLHandlerState.Unprocessed);
        nowPlayingMap.put("skipEnabled", XMLHandlerState.Unprocessed);
        nowPlayingMap.put("rateEnabled", XMLHandlerState.Unprocessed);

        Map<String, XMLHandlerState> contentItemMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.ContentItem, contentItemMap);
        contentItemMap.put("itemName", XMLHandlerState.ContentItemItemName);

        Map<String, XMLHandlerState> presetMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Preset, presetMap);
        presetMap.put("ContentItem", XMLHandlerState.ContentItem);

        Map<String, XMLHandlerState> zoneMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Zone, zoneMap);
        zoneMap.put("member", XMLHandlerState.ZoneMember);
    }

    private boolean checkDeviceId(String localName, Attributes attributes) {
        String did = attributes.getValue("deviceID");
        if (did == null) {
            logger.warn("No Device-ID in Entity " + localName);
            return false;
        }
        if (!did.equals(boseSoundTouchHandler.getMacAddress())) {
            logger.warn("Wrong Device-ID in Entity " + localName + ": Got: " + did + " expected: "
                    + boseSoundTouchHandler.getMacAddress());
            return false;
        }
        return true;
    }

    private void setConfigOption(String option, String value) {
        Map<String, String> prop = boseSoundTouchHandler.getThing().getProperties();
        String cur = prop.get(option);
        if (cur == null || !cur.equals(value)) {
            logger.info("Option \"" + option + "\" updated: From \"" + cur + "\" to \"" + value + "\"");
            boseSoundTouchHandler.getThing().setProperty(option, value);
        }
    }
}