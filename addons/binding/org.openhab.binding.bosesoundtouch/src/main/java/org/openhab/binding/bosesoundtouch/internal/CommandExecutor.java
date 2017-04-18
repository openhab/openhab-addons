/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants;
import org.openhab.binding.bosesoundtouch.handler.BoseSoundTouchHandler;
import org.openhab.binding.bosesoundtouch.internal.exceptions.BoseSoundTouchNotFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.ContentItemNotPresetableException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.NoInternetRadioPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.NoPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.NoStoredMusicPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.OperationModeNotAvailableException;
import org.openhab.binding.bosesoundtouch.internal.items.ContentItem;
import org.openhab.binding.bosesoundtouch.internal.items.ContentItemMaker;
import org.openhab.binding.bosesoundtouch.internal.items.PresetContainer;
import org.openhab.binding.bosesoundtouch.internal.items.ZoneMember;
import org.openhab.binding.bosesoundtouch.types.OperationModeType;
import org.openhab.binding.bosesoundtouch.types.RemoteKeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Enigma2CommandExecutor} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class CommandExecutor {
    private Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private boolean muted;

    private Session session;
    private BoseSoundTouchHandler handler;

    private ZoneState zoneState;
    private BoseSoundTouchHandler zoneMaster;
    private List<ZoneMember> zoneMembers;
    private File presetFile;

    private ContentItem currentContentItem;

    private OperationModeType currentOperationMode;

    private PresetContainer presetContainer;

    public CommandExecutor(Session session, BoseSoundTouchHandler handler) {
        this.session = session;
        this.handler = handler;
        this.zoneMembers = new ArrayList<ZoneMember>();
        this.zoneState = ZoneState.None;
        this.zoneMaster = null;
        this.currentOperationMode = OperationModeType.OFFLINE;
        this.presetContainer = new PresetContainer();
        this.currentContentItem = null;

        File folder = new File(ConfigConstants.getUserDataFolder() + "/" + BoseSoundTouchBindingConstants.BINDING_ID);
        if (!folder.exists()) {
            logger.debug("Creating directory {}", folder.getPath());
            folder.mkdirs();
        }
        presetFile = new File(folder, "presets.txt");
        try {
            presetContainer.readFromFile(presetFile);
        } catch (IOException e) {
            logger.warn("{}: Could not load presets from file", handler.getDeviceName());
        }
    }

    private void removeFromZone(BoseSoundTouchHandler handler) {
        if (handler != null) {
            if (!removeZoneMember(handler)) {
                logger.warn("{}: Zone remove: ID {} is not a member in zone!", handler.getDeviceName(),
                        handler.getMacAddress());
            } else {
                updateZones();
            }
        }
    }

    private void addToZone(BoseSoundTouchHandler handler) {
        if (handler != null) {
            boolean found = false;
            for (ZoneMember m : zoneMembers) {
                if (handler.getMacAddress().equals(m.getMac())) {
                    logger.warn("{}: Zone add: ID {} is already member in zone!", handler.getDeviceName(),
                            handler.getMacAddress());
                    found = true;
                    break;
                }
            }
            if (!found) {
                ZoneMember nm = new ZoneMember();
                nm.setHandler(handler);
                nm.setMac(handler.getMacAddress());
                Map<String, Object> props = handler.getThing().getConfiguration().getProperties();
                String host = (String) props.get(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_HOST);
                nm.setIp(host);
                // zoneMembers.add(nm);
                addZoneMember(nm);
                updateZones();
            }
        }
    }

    public void setPower(OnOffType command) {
        if (command.equals(OnOffType.ON)) {
            if (currentOperationMode == OperationModeType.STANDBY) {
                simulateRemoteKey(RemoteKeyType.POWER);
            }
        } else if (command.equals(OnOffType.OFF)) {
            if (currentOperationMode != OperationModeType.STANDBY) {
                simulateRemoteKey(RemoteKeyType.POWER);
            }
        }
    }

    public void setOperationMode(OperationModeType operationModeType) {
        if (operationModeType == OperationModeType.OFFLINE) {
            currentOperationMode = OperationModeType.OFFLINE;
        } else {
            if (operationModeType == OperationModeType.STANDBY) {
                if (currentOperationMode != OperationModeType.STANDBY) {
                    simulateRemoteKey(RemoteKeyType.POWER);
                }
            } else {
                try {
                    ContentItemMaker contentItemMaker = new ContentItemMaker(handler, presetContainer);
                    ContentItem contentItem = contentItemMaker.getContentItem(operationModeType);
                    setContentItem(contentItem);
                } catch (OperationModeNotAvailableException e) {
                    logger.warn("{}: OperationMode \"{}\" is not supported yet", handler.getDeviceName(),
                            operationModeType.name());
                } catch (NoInternetRadioPresetFoundException e) {
                    logger.warn("{}: Unable to switch to mode \"INTERNET_RADIO\". No PRESET defined",
                            handler.getDeviceName());
                } catch (NoStoredMusicPresetFoundException e) {
                    logger.warn("{}: Unable to switch to mode: \"STORED_MUSIC\". No PRESET defined",
                            handler.getDeviceName());

                } finally {
                    checkOperationMode();
                }
            }
        }
    }

    public void setVolume(PercentType command) {
        sendPostRequestInWebSocket("volume",
                "<volume deviceID=\"" + handler.getMacAddress() + "\"" + ">" + command.intValue() + "</volume>");
    }

    public void setBass(DecimalType command) {
        sendPostRequestInWebSocket("bass",
                "<bass deviceID=\"" + handler.getMacAddress() + "\"" + ">" + command.intValue() + "</bass>");
    }

    public void setMuted(OnOffType command) {
        handler.updateMuteState(command);
        if (command.equals(OnOffType.ON)) {
            if (!muted) {
                muted = true;
                simulateRemoteKey(RemoteKeyType.MUTE);
            }
        } else if (command.equals(OnOffType.OFF)) {
            if (muted) {
                muted = false;
                simulateRemoteKey(RemoteKeyType.MUTE);
            }
        }
    }

    public void setCurrentContentItem(ContentItem currentContentItem) {
        this.currentContentItem = currentContentItem;
    }

    public void setContentItem(ContentItem contentItem) {
        if (contentItem != null) {
            setCurrentContentItem(contentItem);
            sendPostRequestInWebSocket("select", "", contentItem.generateXML());
        }
    }

    public void setContentItemAsPreset(DecimalType command) {
        if (command.intValue() > 6) {
            try {
                presetContainer.put(command.intValue(), currentContentItem);
                presetContainer.writeToFile(presetFile);
            } catch (ContentItemNotPresetableException e) {
                logger.warn("{}: Selected item is not presetable", handler.getDeviceName());
            } catch (IOException e) {
                logger.warn("{}: Could not save presets to file", handler.getDeviceName());
            }
        } else {
            logger.warn("{}: Only PresetID >6 is allowed", handler.getDeviceName());
        }
    }

    public void setPreset(DecimalType command) {
        ContentItem item = null;
        try {
            item = presetContainer.get(command.intValue());
        } catch (NoPresetFoundException e) {
            logger.warn("{}: No preset found at id: {}", handler.getDeviceName(), currentContentItem.getPresetID());
        }
        setContentItem(item);
    }

    public void setPreset(NextPreviousType command) {
        ContentItem item = null;
        if (command.equals(NextPreviousType.NEXT)) {
            try {
                item = presetContainer.getNext(currentContentItem);
            } catch (NoPresetFoundException e) {
                logger.warn("{}: No preset found at id: {}", handler.getDeviceName(),
                        currentContentItem.getPresetID() + 1);
            }
        } else if (command.equals(NextPreviousType.PREVIOUS)) {
            try {
                item = presetContainer.getPrev(currentContentItem);
            } catch (NoPresetFoundException e) {
                logger.warn("{}: No preset found at id: {}", handler.getDeviceName(),
                        currentContentItem.getPresetID() - 1);
            }
        }
        setContentItem(item);
    }

    public void setPlayerControl(Command command) {
        if (command.equals(PlayPauseType.PLAY)) {
            simulateRemoteKey(RemoteKeyType.PLAY);
        } else if (command.equals(PlayPauseType.PAUSE)) {
            simulateRemoteKey(RemoteKeyType.PAUSE);
        } else if (command.equals(NextPreviousType.NEXT)) {
            simulateRemoteKey(RemoteKeyType.NEXT_TRACK);
        } else if (command.equals(NextPreviousType.PREVIOUS)) {
            simulateRemoteKey(RemoteKeyType.PREV_TRACK);
        }
    }

    public void setZone(StringType command) {
        // try to parse string command...
        String cmd = command.toString();
        String cmdlc = cmd.toLowerCase();
        if (cmdlc.split(" ").length == 2) {
            String action = cmdlc.split(" ")[0];
            String identifier = cmdlc.split(" ")[1];

            BoseSoundTouchHandler handlerFound = null;
            try {
                handlerFound = findHandlerByNameOrMAC(identifier);
            } catch (BoseSoundTouchNotFoundException e) {
                logger.warn("{}: Could not find Soundtouchd: {}", handler.getDeviceName(), identifier);
            }

            if ("add".equals(action)) {
                addToZone(handlerFound);
            } else if ("remove".equals(action)) {
                removeFromZone(handlerFound);
            } else {
                logger.warn("{}: Invalid zone command: {}", handler.getDeviceName(), cmd);
            }
        } else {
            logger.warn("{}: Invalid zone command: {}", handler.getDeviceName(), cmd);
        }
    }

    public void simulateRemoteKey(RemoteKeyType key) {
        sendPostRequestInWebSocket("key", "mainNode=\"keyPress\"",
                "<key state=\"press\" sender=\"Gabbo\">" + key.name() + "</key>");
        sendPostRequestInWebSocket("key", "mainNode=\"keyRelease\"",
                "<key state=\"release\" sender=\"Gabbo\">" + key.name() + "</key>");
    }

    private void sendGetRequestInWebSocket(String url) {
        String msg = "<msg><header " + "deviceID=\"" + handler.getMacAddress() + "\"" + " url=\"" + url
                + "\" method=\"GET\"><request requestID=\"0\"><info type=\"new\"/></request></header></msg>";
        try {
            session.getRemote().sendString(msg);
        } catch (IOException e) {
            handler.onWebSocketError(e);
        }
    }

    public void sendAPIRequest(APIRequest apiRequest) {
        String msg = "<msg><header " + "deviceID=\"" + handler.getMacAddress() + "\"" + " url=\"" + apiRequest.getName()
                + "\" method=\"GET\"><request requestID=\"0\"><info type=\"new\"/></request></header></msg>";
        try {
            session.getRemote().sendString(msg);
        } catch (IOException e) {
            handler.onWebSocketError(e);
        }
    }

    private void sendPostRequestInWebSocket(String url, String postData) {
        sendPostRequestInWebSocket(url, "", postData);
    }

    private void sendPostRequestInWebSocket(String url, String infoAddon, String postData) {
        int id = 0;
        String msg = "<msg><header " + "deviceID=\"" + handler.getMacAddress() + "\"" + " url=\"" + url
                + "\" method=\"POST\"><request requestID=\"" + id + "\"><info " + infoAddon
                + " type=\"new\"/></request></header><body>" + postData + "</body></msg>";
        try {
            session.getRemote().sendString(msg);
        } catch (IOException e) {
            handler.onWebSocketError(e);
        }
    }

    public void updateZoneState(ZoneState zoneState, BoseSoundTouchHandler zoneMaster, List<ZoneMember> zoneMembers) {
        this.zoneState = zoneState;
        this.zoneMaster = zoneMaster;
        this.zoneMembers = zoneMembers;
        zonesChanged();
    }

    private void addZoneMember(ZoneMember zoneMember) {
        boolean found = false;
        for (ZoneMember m : zoneMembers) {
            if (zoneMember.getHandler().getMacAddress().equals(m.getMac())) {
                logger.warn("{}: Zone add: ID '{}' is already member in zone!", handler.getDeviceName(),
                        zoneMember.getHandler().getMacAddress());
                found = true;
                break;
            }
        }
        if (!found) {
            zoneMembers.add(zoneMember);
        }
    }

    private boolean removeZoneMember(BoseSoundTouchHandler oh) {
        boolean found = false;
        for (Iterator<ZoneMember> mi = zoneMembers.iterator(); mi.hasNext();) {
            ZoneMember m = mi.next();
            if (oh == m.getHandler()) {
                mi.remove();
                found = true;
                break;
            }
        }
        return found;
    }

    private void updateZones() {
        StringBuilder sb = new StringBuilder();
        sb.append("<zone master=\"").append(handler.getMacAddress()).append("\">");
        for (ZoneMember mbr : zoneMembers) {
            sb.append("<member ipaddress=\"").append(mbr.getIp()).append("\">").append(mbr.getMac())
                    .append("</member>");
        }
        sb.append("</zone>");
        sendPostRequestInWebSocket("setZone", "mainNode=\"newZone\"", sb.toString());
    }

    public void zonesChanged() {
        StringBuilder sb = new StringBuilder();
        switch (zoneState) {
            case Master:
                sb.append("Master; Members: ");
                break;
            case Member:
                sb.append("Member; Master is: ");
                if (zoneMaster == null) {
                    sb.append("<null>");
                } else {
                    sb.append(zoneMaster.getDeviceName());
                }
                sb.append("; Members: ");
                break;
            case None:
                sb.append("Standalone");
                break;
        }
        for (int i = 0; i < zoneMembers.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(zoneMembers.get(i).getHandler().getDeviceName());
        }
        String zoneData = sb.toString();
        logger.debug("{}: zoneInfo updated: {}", handler.getDeviceName(), zoneData);
        handler.updateZoneInfo(new StringType(zoneData));
    }

    public void getInfo() {
        sendGetRequestInWebSocket("info");
    }

    public void checkOperationMode() {
        OperationModeType operationMode = OperationModeType.OTHER;
        if (currentContentItem != null) {
            ContentItem psFound = null;
            Collection<ContentItem> listOfPresets = presetContainer.values();
            for (ContentItem ps : listOfPresets) {
                if (ps.isPresetable()) {
                    if (ps.getLocation().equals(currentContentItem.getLocation())) {
                        psFound = ps;
                    }
                }
            }
            int presetID = 0;
            if (psFound != null) {
                presetID = psFound.getPresetID();
            }
            handler.updatePreset(new DecimalType(presetID));

            operationMode = currentContentItem.getOperationMode();
        } else {
            operationMode = OperationModeType.STANDBY;
        }

        handler.updateOperationMode(new StringType(operationMode.getName()));
        currentOperationMode = operationMode;
        if (operationMode == OperationModeType.STANDBY) {
            // zone is leaved / destroyed if turned off
            zoneMembers.clear();
            if (zoneMaster != null) {
                zoneMaster.getCommandExecutor().removeZoneMember(handler);
                zoneMaster = null;
            }
            handler.updatePowerState(OnOffType.OFF);
            handler.updatePlayerControl(PlayPauseType.PAUSE);
        } else {
            handler.updatePowerState(OnOffType.ON);
        }
    }

    public BoseSoundTouchHandler getZoneMaster() {
        return zoneMaster;
    }

    public void addContentItemToPresetList(ContentItem preset) {
        try {
            presetContainer.put(preset);
        } catch (ContentItemNotPresetableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private BoseSoundTouchHandler findHandlerByNameOrMAC(String identifier) throws BoseSoundTouchNotFoundException {
        BoseSoundTouchHandler handlerFound = null;
        BoseSoundTouchHandlerFactory factory = handler.getFactory();
        for (Entry<String, BoseSoundTouchHandler> entry : factory.getAllSoundTouchDevices().entrySet()) {
            BoseSoundTouchHandler curHandler = entry.getValue();
            // try by mac
            String mac = entry.getKey();
            if (identifier.equalsIgnoreCase(mac)) {
                handlerFound = curHandler;
                break;
            }
            // try by name
            String devName = curHandler.getDeviceName();
            if (identifier.equalsIgnoreCase(devName)) {
                handlerFound = curHandler;
                break;
            }
        }

        if (handlerFound != null) {
            return handlerFound;
        } else {
            throw new BoseSoundTouchNotFoundException();
        }
    }
}
