/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lightwaverf.internal.handler;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.google.gson.JsonObject;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.joda.time.DateTime;
import org.openhab.binding.lightwaverf.internal.Http;
import org.openhab.binding.lightwaverf.internal.LWDiscoveryService;
import org.openhab.binding.lightwaverf.internal.api.FeatureStatus;
import org.openhab.binding.lightwaverf.internal.api.discovery.Devices;
import org.openhab.binding.lightwaverf.internal.api.discovery.FeatureSets;
import org.openhab.binding.lightwaverf.internal.api.discovery.Features;
import org.eclipse.smarthome.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * The {@link lightwaverfBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */

public class DeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);
    String value;
    private LWAccountHandler account;
    private ScheduledFuture<?> refreshTask;
    private ScheduledFuture<?> listTask;
    private String sdId = this.thing.getConfiguration().get("sdId").toString();
    private FeatureSets featureSet;
    private Features feature;
    private String featureSetId;
    private int i;
    private String groupType;
    private int noChannels;
    public int pollingInterval;
    private String channelId;
    private String channelTypeId;
    private int channelValue;
    private String list;
    private String ch;
    private int group;
    private String state;
    private String featureId;
    private FeatureStatus status;
    private String featureString;
    List<Devices> devices = new ArrayList<Devices>();
    List<FeatureSets> featureSets = new ArrayList<FeatureSets>();
    List<Features> features = new ArrayList<Features>();
    List<FeatureStatus> featureStatus = new ArrayList<FeatureStatus>();
    Devices device;

    public DeviceHandler(Thing thing) {
        super(thing);
    }

    public List<String> channelList() {
        return account.channelList();
    }

    public List<String> cLinked() {
        return account.cLinked();
    }

    public int featureStatusIndex(String Id) {
        for (int i = 0; i < featureStatus.size(); i++) {
            if (featureStatus.get(i).getFeatureId().contains(Id)) {
                return i;
            }
        }
        ;
        return -1;
    }

    public int featureSetIndex(String Id) {
        for (int i = 0; i < featureSets.size(); i++) {
            if (featureSets.get(i).getFeatureSetId().contains(Id)) {
                return i;
            }
        }
        return -1;
    }

    public int deviceIndex(String sdId) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getDeviceId().contains("-" + sdId + "-")) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing {} handler.", getThing().getThingTypeUID());
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge Not set");
        }
        initializeBridge(bridge.getHandler(), bridge.getStatus());
        listTask = scheduler.schedule(this::connect, 2, TimeUnit.SECONDS);
    }

    private void connect() {
        devices = account.devices();
        device = devices.get(deviceIndex(sdId));
        featureStatus = account.featureStatus();
        featureSets = account.featureSets();
        pollingInterval = Integer.valueOf(this.getBridge().getConfiguration().get("pollingInterval").toString());
        logger.debug("Polling Interval: {} ", pollingInterval);
        refreshTask = scheduler.scheduleWithFixedDelay(this::updateChannels, 10, pollingInterval, TimeUnit.SECONDS);
        logger.debug("Refresh task added");
        properties();
        if (!account.cLinked().isEmpty()) {
            logger.debug("temp link size: {} ", account.cLinked().size());
            for (int i = 0; i < account.cLinked().size(); i++) {
                group = Integer.parseInt(account.cLinked().get(i).split(",")[0]);
                ch = account.cLinked().get(i).split(",")[1];
                state = account.cLinked().get(i).split(",")[2];
                feature = getFeature(group, ch);
                featureId = feature.getFeatureId();
                if (state == "linked" && (!account.channelList().contains(featureId))) {
                    account.addChannelList(featureId);
                    account.removecLinked(account.cLinked().get(i));
                } else if (state == "unlinked" && (account.channelList().contains(featureId))) {
                    account.removeChannelList(featureId);
                    account.removecLinked(account.cLinked().get(i));
                }
            }
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
        if (listTask != null) {
            listTask.cancel(true);
        }
        listTask = null;
        refreshTask = null;
        account = null;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(LWDiscoveryService.class);
    }

    public ThingUID getID() {
        return getThing().getUID();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        Bridge bridge = getBridge();
        if (bridge != null) {
            initializeBridge(bridge.getHandler(), bridgeStatusInfo.getStatus());
        }
    }

    private void initializeBridge(ThingHandler thingHandler,ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());

        if (thingHandler != null && bridgeStatus != null) {
            account = (LWAccountHandler) thingHandler;
            if (bridgeStatus != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void channelUnlinked(final ChannelUID channelUID) {
        ch = channelUID.getIdWithoutGroup().toString();
        group = (Integer.parseInt(channelUID.getGroupId()) - 1);
        if (device == null) {
            account.addcLinked(group + "," + ch + ",unlinked");
            logger.debug("channel {} group {} added to temporary list for removal", ch, group);
        } else if (device != null) {
            Objects.requireNonNull(channelUID, "channelUID cannot be null");
            feature = getFeature(group, ch);
            if (account.channelList().contains(feature.getFeatureId())) {
                account.removeChannelList(feature.getFeatureId());
            }
        }
        super.channelUnlinked(channelUID);
    }

    @Override
    public void channelLinked(final ChannelUID channelUID) {
        ch = channelUID.getIdWithoutGroup().toString();
        group = (Integer.parseInt(channelUID.getGroupId()) - 1);
        if (device == null) {
            account.addcLinked(group + "," + ch + ",linked");
            logger.debug("channel {} group {} added to temporary list for addition", ch, group);
        } else if (device != null) {
            Objects.requireNonNull(channelUID, "channelUID cannot be null");
            feature = getFeature(group, ch);
            if (!account.channelList().contains(feature.getFeatureId())) {
                account.addChannelList(feature.getFeatureId());
            }
        }
        super.channelLinked(channelUID);
    }

    private void properties() {
        featureString = "";
        Map<String, String> dProperties = editProperties();
        dProperties.clear();
        for (int i = 0; i < device.getFeatureSets().size(); i++) {
            String name = device.getFeatureSets().get(i).getName();
            logger.debug("Item Property Added, Device channel: {} - {}", i, name);
            list = "Channel: " + (i + 1) + ", Name: " + name + "\r\n";
            dProperties.put("Channel" + (i + 1), list);
        }
        for (int i = 0; i < device.getFeatureSets().size(); i++) {
            featureString = featureString + "Channel " + (i + 1) + ": ";
            for (int j = 0; j < device.getFeatureSets().get(i).getFeatures().size(); j++) {
                featureString = featureString + device.getFeatureSets().get(i).getFeatures().get(j).getType() + ",";
            }
        }
        dProperties.put("Available Channels", featureString.substring(0, featureString.length() - 1));
        dProperties.put("Device ID", device.getDeviceId());
        dProperties.put("sdId", sdId);
        dProperties.put("Name", device.getName());
        dProperties.put("Device", device.getDevice());
        dProperties.put("Type", device.getType());
        dProperties.put("Description", device.getDesc());
        dProperties.put("Product", device.getProduct());
        dProperties.put("Product Code", device.getProductCode());
        dProperties.put("Category", device.getCat());
        dProperties.put("Generation", device.getGen().toString());
        dProperties.put("Channels", new Integer(device.getFeatureSets().size()).toString());
        updateProperties(dProperties);
    }

    private Features getFeature(int featureSetNo,String channelId) {
        featureSetId = device.getFeatureSets().get(featureSetNo).getFeatureSetId();
        featureSet = featureSets.stream().filter(i -> featureSetId.equals(i.getFeatureSetId())).findFirst()
                .orElse(featureSets.get(0));
        return featureSet.getFeatures().stream().filter(i -> channelId.equals(i.getType())).findFirst()
                .orElse(featureSet.getFeatures().get(0));
    }

    private synchronized FeatureStatus getFeatureStatus(String featureId) {
        return account.featureStatus().stream().filter(i -> featureId.equals(i.getFeatureId())).findFirst()
                .orElse(featureStatus.get(0));
    }

    private void updateChannels() {
        noChannels = device.getFeatureSets().size();
        i = noChannels;
        for (int j = 0; j < i; j++) {
            groupType = (j + 1) + "";
            for (Channel group : getThing().getChannelsOfGroup(groupType)) {
                if (isLinked(group.getUID())) {
                    channelTypeId = group.getChannelTypeUID().getId();
                    logger.debug("Update Channels: j = {} , channeltype = {} ", j, channelTypeId);
                    feature = getFeature(j, channelTypeId);
                    if (!account.channelList().contains(feature.getFeatureId())) {
                        account.addChannelList(feature.getFeatureId());
                        logger.debug("channel added to update list {} for featureId {}",
                                group.getUID().getId().toString(), feature.getFeatureId());
                    }
                    status = getFeatureStatus(feature.getFeatureId());
                    channelValue = status.getValue();
                    channelId = group.getUID().getId().toString();
                    updateChannels(channelId, channelValue, group.getUID().getIdWithoutGroup());
                    logger.debug("value {} sent to {} with channel name {}", channelValue,
                            group.getUID().getId().toString(), group.getUID().getIdWithoutGroup());
                }
            }
        }
    }

    private void updateChannels(String channelId, Integer value,String channelName) {
        switch (channelName) {
        case "switch":
        case "diagnostics":
        case "outletInUse":
        case "protection":
        case "identify":
        case "reset":
        case "upgrade":
        case "heatState":
        case "callForHeat":
        case "bulbSetup":
        case "dimSetup":
            if (value == 1) {
                updateState(channelId, OnOffType.ON);
            } else {
                updateState(channelId, OnOffType.OFF);
            }
            break;
        case "power": case "rssi":
            updateState(channelId, new DecimalType(Float.parseFloat(value.toString())));
            break;
        case "energy":
            updateState(channelId, new DecimalType(Float.parseFloat(value.toString()) / 1000));
            break;
        case "temperature":
        case "targetTemperature":
        case "voltage":
            updateState(channelId, new DecimalType(Float.parseFloat(value.toString()) / 10));
            break;
        case "dimLevel":
        case "valveLevel":
            updateState(channelId, new PercentType(value));
            break;
            case "batteryLevel":
            updateState(channelId, new DecimalType(value));
            break;
        case "rgbColor":
            Color color = new Color(value);
            int red = (color.getRed());
            int green = (color.getGreen());
            int blue = (color.getBlue());
            float[] hsb = Color.RGBtoHSB(red, green, blue, null);
            double hue = (hsb[0] * 100 / 0.255);
            float saturation = (hsb[1] * 100);
            float brightness = (hsb[2] * 100);
            if(hue > 360) {hue = 360;}
            String hsb1 = hue + "," + saturation + "," + brightness;
            updateState(channelId, new HSBType(hsb1));
            break;
        case "periodOfBroadcast":
        case "monthArray":
        case "weekdayArray":
            updateState(channelId, new StringType(value.toString()));
            break;
        case "timeZone":
        case "day":
        case "month":
        case "year":
            updateState(channelId, new DecimalType(value.toString()));
            break;
        case "date":
            Number abc = ((value).longValue()*100000);
            DateTimeType date = new DateTimeType(new DateTime(abc).toString());
            updateState(channelId,date);
        break;
        case "currentTime":
            Number def = ((value).longValue()*1000);
            DateTimeType time = new DateTimeType(new DateTime(def).toString());
            updateState(channelId, time);
            break;
        case "duskTime":
        case "dawnTime":
        case "time":
        String hoursPad = "";
        String minsPad = "";
        String secsPad = "";
            int minutes = ((Integer.parseInt(value.toString()) / 60)%60);
            int hours = (value / 3600);
            int seconds = (Integer.parseInt(value.toString()) % 60);
            if (hours < 10) {
                hoursPad = "0";
            }
            if (minutes < 10) {
                minsPad = "0";
            }
            if (seconds < 10) {
                secsPad = "0";
            } 
            String timeValue = hoursPad + hours + ":" + minsPad + minutes + ":" + secsPad + seconds ;
            updateState(channelId, new DateTimeType(timeValue));
            break;
        case "weekday":
            if (value != 0) {
                updateState(channelId, new StringType(DayOfWeek.of(value).toString()));
                break;
            } else {
                break;
            }
        case "locationLongitude":
        case "locationLatitude":
            updateState(channelId, new StringType(new DecimalType(Float.parseFloat(value.toString()) / 1000000).toString()));
            break;
        }
        logger.debug("channel {} received update", channelId);
    }
    

    @Override
    public  void handleCommand(ChannelUID channelUID,Command command) {
        String channelName = channelUID.getIdWithoutGroup();
        String value = "";
        String group = channelUID.getGroupId();
        i = ((int) Double.parseDouble(group) - 1);
        if (command instanceof RefreshType) {
            logger.debug("Refresh command not supported");
            return;
        }
        else if (account == null) {
            logger.warn("No connection to Lightwave available, ignoring command");
            return;
        }
        else {
        logger.debug("handleCommand(list): channel = {} group = {}", channelName,i);
        feature = getFeature(i,channelName);
        featureId = feature.getFeatureId();
        switch (channelName) {
        case "switch":
        case "diagnostics":
        case "outletInUse":
        case "protection":
        case "identify":
        case "reset":
        case "upgrade":
        case "heatState":
        case "callForHeat":
        case "bulbSetup":
        case "dimSetup":
            if (command.toString() == "ON") {
                value = "1";
            } else {
                value = "0";
            }
            break;
        case "rgbColor": 
            if(command.toString().contains(",")) {        
            PercentType redp =  new HSBType(command.toString()).getRed();
            PercentType greenp =  new HSBType(command.toString()).getGreen();
            PercentType bluep =  new HSBType(command.toString()).getBlue();
            int red = (int) (redp.doubleValue() * 255 / 100);
            int green = (int) (greenp.doubleValue() * 255 / 100);
            int blue = (int) (bluep.doubleValue() * 255 / 100);
            Integer c = (red << 16) + (green << 8) + (blue);
            value = c.toString();
            break;
        }
        else {
        logger.warn("Brightness Is Not Supported For the RGB Colour Channel");
            break; 
        }  
        case "timeZone":
        case "locationLongitude":
        case "locationLatitude":
        case "dimLevel":
        case "valveLevel":
            value = (new DecimalType(Float.parseFloat(command.toString()))).toString();
            break;
            case "temperature":
            case "targetTemperature":
            value = (new DecimalType((Float.parseFloat(command.toString())) * 10)).toString();
            break;
        default:
            value = "-1";
        }
        logger.debug("channel: {}", channelUID.getId());
        logger.debug("value: {}", value);
        long now = System.currentTimeMillis();
        account.addLocked(featureId, now);
        logger.debug("lock added: {} : {}", featureId, now);
        Integer pollingSize = (account.pollingSize() * 1000);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                removeLocked(featureId,now);
            }
        }, pollingSize);
        try {
            setStatus(featureId, value);
            int valueint = Integer.parseInt(value);
                account.featureStatus().stream().filter(i -> featureId.equals(i.getFeatureId())).forEach(u -> u.setValue(valueint));
        } catch (Exception e) {
        }
    }
    }

    public void removeLocked(String featureId, Long time) {
        account.removeLocked(featureId,time);
        logger.debug("lock removed: {} : {}", featureId, time); 
    }
    
    public void setStatus(String featureId,String value) throws Exception {
        JsonObject jsonReq = new JsonObject();
        jsonReq.addProperty("value", value);
        InputStream data = new ByteArrayInputStream(jsonReq.toString().getBytes(StandardCharsets.UTF_8));
        Http.httpClient("feature", data, "application/json",featureId);     
    } 
}
