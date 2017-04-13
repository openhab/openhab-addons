/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants;
import org.openhab.binding.bosesoundtouch.handler.BoseSoundTouchHandler;
import org.openhab.binding.bosesoundtouch.internal.items.ContentItem;
import org.openhab.binding.bosesoundtouch.internal.items.ZoneMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The {@link XMLResponseHandler} class handles the XML communication with the Soundtouch
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */
public class XMLResponseHandler extends DefaultHandler {

    private final Logger logger = LoggerFactory.getLogger(XMLResponseHandler.class);

    private boolean tracing;

    private XMLResponseProcessor processor;
    private BoseSoundTouchHandler handler;

    private Map<XMLHandlerState, Map<String, XMLHandlerState>> stateSwitchingMap;

    private Stack<XMLHandlerState> states;
    private XMLHandlerState state;
    private boolean msgHeaderWasValid;

    private ContentItem contentItem;
    private boolean volumeMuteEnabled;
    private OnOffType rateEnabled;
    private OnOffType skipEnabled;
    private OnOffType skipPreviousEnabled;
    private ZoneState zoneState;
    private BoseSoundTouchHandler zoneMaster;
    private List<ZoneMember> zoneMembers;
    private String zoneMemberIp;

    public XMLResponseHandler(XMLResponseProcessor processor, BoseSoundTouchHandler handler,
            Map<XMLHandlerState, Map<String, XMLHandlerState>> stateSwitchingMap) {
        this.processor = processor;
        this.handler = handler;
        this.stateSwitchingMap = stateSwitchingMap;
        states = new Stack<>();
        state = XMLHandlerState.INIT;
        tracing = logger.isTraceEnabled();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (tracing) {
            logger.trace("{}: startElement('{}'; state: {})", handler.getDeviceName(), localName, state);
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
                } else if ("SoundTouchSdkInfo".equals(localName)) {
                    // TODO
                    state = XMLHandlerState.Unprocessed;
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState,
                                localName);
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
                    logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState,
                            localName);
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            case MsgHeader:
                if ("request".equals(localName)) {
                    state = XMLHandlerState.Unprocessed; // TODO implement request id / response tracking...
                } else {
                    logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState,
                            localName);
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            case MsgBody:
                if ("nowPlaying".equals(localName)) {
                    /*
                     * if (!checkDeviceId(localName, attributes, true)) {
                     * state = XMLHandlerState.Unprocessed;
                     * break;
                     * }
                     */
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
                        processor.updateRateEnabled(OnOffType.OFF);
                        processor.updateSkipEnabled(OnOffType.OFF);
                        processor.updateSkipPreviousEnabled(OnOffType.OFF);

                        // clear all "nowPlaying" details on source change...
                        processor.updateNowPlayingAlbum(new StringType(""));
                        processor.updateNowPlayingArtwork(new StringType(""));
                        processor.updateNowPlayingArtist(new StringType(""));
                        processor.updateNowPlayingDescription(new StringType(""));
                        processor.updateNowPlayingGenre(new StringType(""));
                        processor.updateNowPlayingItemName(new StringType(""));
                        processor.updateNowPlayingPlayStatus(new StringType(""));
                        processor.updateNowPlayingStationLocation(new StringType(""));
                        processor.updateNowPlayingStationName(new StringType(""));
                        processor.updateNowPlayingTrack(new StringType(""));
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
                                logger.warn("{}: Zone update: Unable to find master with ID {}",
                                        handler.getDeviceName(), master);
                            }
                        }
                    }
                    state = XMLHandlerState.Zone;
                } else {
                    state = stateMap.get(localName);
                    if (state == null) {
                        logger.warn("{}: Unhandled XML entity during {}: '{}", handler.getDeviceName(), curState,
                                localName);
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
                    if (contentItem == null) {
                        contentItem = new ContentItem();
                    }
                    contentItem.setPresetID(Integer.parseInt(id));
                } else {
                    logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState,
                            localName);
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            case Zone:
                zoneMemberIp = attributes.getValue("ipaddress");
                state = nextState(stateMap, curState, localName);
                break;
            case Bass:
            case ContentItem:
            case Info:
            case NowPlaying:
            case Preset:
            case Updates:
            case Volume:
                state = nextState(stateMap, curState, localName);
                break;
            // all entities without any children expected..
            case BassTargetValue:
            case BassActualValue:
            case BassUpdated:
            case ContentItemItemName:
            case InfoName:
            case InfoType:
            case NowPlayingAlbum:
            case NowPlayingArt:
            case NowPlayingArtist:
            case NowPlayingGenre:
            case NowPlayingDescription:
            case NowPlayingPlayStatus:
            case NowPlayingRateEnabled:
            case NowPlayingSkipEnabled:
            case NowPlayingSkipPreviousEnabled:
            case NowPlayingStationLocation:
            case NowPlayingStationName:
            case NowPlayingTrack:
            case VolumeActual:
            case VolumeMuteEnabled:
            case ZoneMember:
            case ZoneUpdated: // currently this dosn't provide any zone details..
                logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState, localName);
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
            if (contentItem == null) {
                contentItem = new ContentItem();
            }
            contentItem.setSource(attributes.getValue("source"));
            contentItem.setSourceAccount(attributes.getValue("sourceAccount"));
            contentItem.setLocation(attributes.getValue("location"));
            contentItem.setUnusedField(Integer.parseInt(attributes.getValue("unusedField")));
            contentItem.setPresetable(Boolean.parseBoolean(attributes.getValue("isPresetable")));
        }
        if (state == XMLHandlerState.Volume) {
            volumeMuteEnabled = false;
        }
    }

    private XMLHandlerState nextState(Map<String, XMLHandlerState> stateMap, XMLHandlerState curState,
            String localName) {
        XMLHandlerState state = stateMap.get(localName);
        if (state == null) {
            logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState, localName);
            state = XMLHandlerState.Unprocessed;
        }
        return state;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (tracing) {
            logger.trace("{}: endElement('{}')", handler.getDeviceName(), localName);
        }
        final XMLHandlerState prevState = state;
        state = states.pop();
        switch (prevState) {
            case Info:
                handler.sendRequestInWebSocket("volume");
                handler.sendRequestInWebSocket("presets");
                handler.sendRequestInWebSocket("now_playing");
                handler.sendRequestInWebSocket("getZone");
                handler.sendRequestInWebSocket("bass");
                break;
            case ContentItem:
                if (state == XMLHandlerState.NowPlaying) {
                    // update now playing name...
                    processor.updateNowPlayingItemName(new StringType(contentItem.getItemName()));
                    handler.setCurrentContentItem(contentItem);
                    handler.checkOperationMode();
                }
                break;
            case Preset:
                if (state == XMLHandlerState.Presets) {
                    handler.addContentItemToPresetList(contentItem);
                    contentItem = null;
                    handler.checkOperationMode();
                }
                break;
            case NowPlaying:
                if (state == XMLHandlerState.MsgBody) {
                    processor.updateRateEnabled(rateEnabled);
                    processor.updateSkipEnabled(skipEnabled);
                    processor.updateSkipPreviousEnabled(skipPreviousEnabled);
                }
                break;
            // handle special tags..
            case BassUpdated:
                // request current bass level
                handler.sendRequestInWebSocket("bass");
                break;
            case NowPlayingRateEnabled:
                rateEnabled = OnOffType.ON;
                break;
            case NowPlayingSkipEnabled:
                skipEnabled = OnOffType.ON;
                break;
            case NowPlayingSkipPreviousEnabled:
                skipPreviousEnabled = OnOffType.ON;
                break;
            case Volume:
                if (handler.isMuted() != volumeMuteEnabled) {
                    handler.setMuted(volumeMuteEnabled);
                    handler.updateVolumeMuted(handler.isMuted() ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case ZoneUpdated:
                handler.sendRequestInWebSocket("getZone");
                break;
            case Zone:
                handler.updateZoneState(zoneState, zoneMaster, zoneMembers);
                break;
            default:
                // no actions...
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (tracing) {
            logger.trace("{}: Text data during {}: '{}'", handler.getDeviceName(), state,
                    new String(ch, start, length));
        }
        String temp;
        super.characters(ch, start, length);
        switch (state) {
            case INIT:
            case Msg:
            case MsgHeader:
            case MsgBody:
            case Bass:
            case BassUpdated:
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
                logger.debug("{}: Unexpected text data during {}: '{}'", handler.getDeviceName(), state,
                        new String(ch, start, length));
                break;
            case Unprocessed:
                // drop quietly..
                break;
            case BassActualValue:
                handler.updateBassLevel(new DecimalType(new String(ch, start, length)));
                break;
            case BassTargetValue:
                break;
            case InfoName:
                setConfigOption(BoseSoundTouchBindingConstants.DEVICE_INFO_NAME, new String(ch, start, length));
                break;
            case InfoType:
                setConfigOption(BoseSoundTouchBindingConstants.DEVICE_INFO_TYPE, new String(ch, start, length));
                break;
            case NowPlayingAlbum:
                processor.updateNowPlayingAlbum(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingArt:
                processor.updateNowPlayingArtwork(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingArtist:
                processor.updateNowPlayingArtist(new StringType(new String(ch, start, length)));
                break;
            case ContentItemItemName:
                temp = new String(ch, start, length);
                contentItem.setItemName(temp);
                break;
            case NowPlayingDescription:
                processor.updateNowPlayingDescription(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingGenre:
                temp = new String(ch, start, length);
                processor.updateNowPlayingGenre(new StringType(temp));
                break;
            case NowPlayingPlayStatus:
                String playPauseState = new String(ch, start, length);
                processor.updateNowPlayingPlayStatus(new StringType(playPauseState));
                if ("PLAY_STATE".equals(playPauseState) || "BUFFERING_STATE".equals(playPauseState)) {
                    handler.updatePlayerControl(PlayPauseType.PLAY);
                } else if ("STOP_STATE".equals(playPauseState) || "PAUSE_STATE".equals(playPauseState)) {
                    handler.updatePlayerControl(PlayPauseType.PAUSE);
                }
                break;
            case NowPlayingStationLocation:
                processor.updateNowPlayingStationLocation(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingStationName:
                processor.updateNowPlayingStationName(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingTrack:
                processor.updateNowPlayingTrack(new StringType(new String(ch, start, length)));
                break;
            case VolumeActual:
                temp = new String(ch, start, length);
                handler.updateVolume(new PercentType(Integer.parseInt(temp)));
                break;
            case VolumeMuteEnabled:
                volumeMuteEnabled = Boolean.parseBoolean(new String(ch, start, length));
                break;
            case ZoneMember:
                String mac = new String(ch, start, length);
                BoseSoundTouchHandler memberHandler = handler.getFactory().getBoseSoundTouchDevice(mac);
                if (memberHandler == null) {
                    logger.warn("{}: Zone update: Unable to find member with ID {}", handler.getDeviceName(), mac);
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

    private boolean checkDeviceId(String localName, Attributes attributes, boolean allowFromMaster) {
        String did = attributes.getValue("deviceID");
        if (did == null) {
            logger.warn("{}: No Device-ID in Entity {}", handler.getDeviceName(), localName);
            return false;
        }
        if (did.equals(handler.getMacAddress())) {
            return true;
        }
        if (allowFromMaster && handler.getZoneMaster() != null && did.equals(handler.getZoneMaster().getMacAddress())) {
            return true;
        }
        logger.warn("{}: Wrong Device-ID in Entity '{}': Got: '{}', expected: '{}'", handler.getDeviceName(), localName,
                did, handler.getMacAddress());
        return false;
    }

    private void setConfigOption(String option, String value) {
        Map<String, String> prop = handler.getThing().getProperties();
        String cur = prop.get(option);
        if (cur == null || !cur.equals(value)) {
            logger.info("{}: Option '{}' updated: From '{}' to '{}'", handler.getDeviceName(), option, cur, value);
            handler.getThing().setProperty(option, value);
        }
    }
}