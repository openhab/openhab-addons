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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lightwaverf.internal.UpdateListener;
import org.openhab.binding.lightwaverf.internal.api.FeatureStatus;
import org.openhab.binding.lightwaverf.internal.api.discovery.Devices;
import org.openhab.binding.lightwaverf.internal.api.discovery.FeatureSets;
import org.openhab.binding.lightwaverf.internal.api.discovery.Features;
import org.openhab.binding.lightwaverf.internal.api.discovery.Root;
import org.openhab.binding.lightwaverf.internal.api.discovery.StructureList;
import org.openhab.binding.lightwaverf.internal.config.AccountConfig;
import org.openhab.binding.lightwaverf.internal.LWDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link lightwaverfBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */
public class LWAccountHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(LWAccountHandler.class);
    List<Root> structures = new ArrayList<Root>();
    List<Devices> devices = new ArrayList<Devices>();
    List<FeatureSets> featureSets = new ArrayList<FeatureSets>();
    List<Features> features = new ArrayList<Features>();
    int partitionSize = Integer.valueOf(this.thing.getConfiguration().get("pollingGroupSize").toString());
    private ScheduledFuture<?> connectionCheckTask;
    private ScheduledFuture<?> refreshTask;
    private AccountConfig config;
    public UpdateListener listener;
    private ScheduledFuture<?> listTask;
    private String list;
    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    public LWAccountHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Lightwave account handler.");
        config = getConfigAs(AccountConfig.class);
        listener = new UpdateListener();
        try {
            listener.login(config.username, config.password);
        } catch (IOException e) {

        }
        try {
            createLists();
        } catch (IOException e) {

        }
        properties();
        startRefresh();
        //if (listTask == null || listTask.isCancelled()) {
        //    listTask = scheduler.schedule(this::startRefresh, 10, TimeUnit.SECONDS);
        //}
        connectionCheckTask = scheduler.schedule(this::startConnectionCheck,60, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
        
    }

    private void createLists() throws IOException {
        logger.debug("Started List Generation");
        StructureList structureList = listener.getStructureList();
        for (int a = 0; a < structureList.getStructures().size(); a++) {
            String structureId = structureList.getStructures().get(a).toString();
            Root structure = listener.getStructure(structureId);
            structures.add(structure);
            devices.addAll(structure.getDevices());
            for (int b = 0; b < devices.size(); b++) {
                featureSets.addAll(devices.get(b).getFeatureSets());
            }
            for (int c = 0; c < featureSets.size(); c++) {
                features.addAll(featureSets.get(c).getFeatures());
            }
            logger.debug("createLists Features size {}", features.size());
        }
        for (int d = 0; d < features.size(); d++) {
            String json = "{\"featureId\": " + features.get(d).getFeatureId() + ",\"value\": 0}";
            FeatureStatus featureStatusItem = gson.fromJson(json, FeatureStatus.class);
            listener.addFeatureStatus(featureStatusItem);
        }
        logger.debug("createLists Feature Status size {}", listener.featureStatus().size());
    }

    private void startConnectionCheck() {
        if (connectionCheckTask == null || connectionCheckTask.isCancelled()) {
            logger.debug("Start periodic connection check");
            Runnable runnable = () -> {
                logger.debug("Checking Lightwave connection");
                if (isConnected()) {
                    logger.debug("Connection to Lightwave in tact");
                } else {
                        try {
                        connect();
                    } catch (Exception e) {
                    }
                    
                }
            };
            connectionCheckTask = scheduler.scheduleWithFixedDelay(runnable, 0, 60, TimeUnit.SECONDS);
        } else {
             logger.debug("Connection check task already running");
        }
    }

    private void connect() throws IOException {
        logger.debug("Initializing connection to Lightwave");
        updateStatus(ThingStatus.OFFLINE);
            listener.login(config.username, config.password);
            updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        // logger.debug("Running dispose()");
        stopConnectionCheck();
        config = null;
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
        if (listTask != null) {
            listTask.cancel(true);
        }
        if (connectionCheckTask != null) {
            connectionCheckTask.cancel(true);
        }
            connectionCheckTask = null;
            listTask = null;
            refreshTask = null;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(LWDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public ThingUID getID() {
        return getThing().getUID();
    }

    public boolean isConnected() {
        return listener.isConnected();
    }

    public List<String> channelList() {
        return listener.channelList();
    }

    public boolean addChannelList( String newLink ) {
        listener.addChannelList(newLink);
        return true;
    }

    public Integer pollingSize() {
        Integer noPartitions = 0;
        Integer channelSizeTemp;
        List<String> channelListTemp = channelList();
        if(channelList().isEmpty()) {
            channelSizeTemp = 20;
        }
        else{
        channelSizeTemp = channelListTemp.size();
        }
        List<List<String>> partitionsTemp = new ArrayList<>();
        for (int i = 0; i < channelSizeTemp; i += partitionSize) {
            partitionsTemp.add(channelListTemp.subList(i, Math.min(i + partitionSize, channelListTemp.size())));
        }
        noPartitions = partitionsTemp.size();
        Integer pollingIntervalTemp = Integer.valueOf(this.thing.getConfiguration().get("pollingInterval").toString());
        return (noPartitions * pollingIntervalTemp);
    }

    public boolean removeChannelList( String newLink ) {
        listener.removeChannelList(newLink );
        return true;
     }

     public Map<String, Long> getLocked() {
        return listener.getLocked();
    }

    public boolean addLocked( String featureId, Long time ) {
        listener.addLocked(featureId,time);
        return true;
    }

    public boolean removeLocked( String featureId, Long time ) {
        listener.removeLocked(featureId,time);
        return true;
    }

    public List<String> cLinked() {
        return listener.cLinked();
    }

    public boolean addcLinked( String newLink ) {
        listener.addcLinked(newLink);
        return true;
    }

    public boolean removecLinked( String newLink ) {
        listener.removecLinked(newLink );
        return true;
     }

    public List<Devices> devices() {
        return devices;
    }

    public List<FeatureSets> featureSets() {
        return featureSets;
    }

    public List<FeatureStatus> featureStatus() {
        return listener.featureStatus();
    }

    public boolean addFeatureStatus( FeatureStatus newFeatureStatus ) {
        listener.addFeatureStatus(newFeatureStatus);
        return true;
    }

    public boolean removeFeatureStatus( FeatureStatus newFeatureStatus ) {
        listener.removeFeatureStatus(newFeatureStatus );
        return true;
     }

    private void properties() {
        Map<String, String> properties = editProperties();
        properties.clear();
        for (int i=0; i < devices.size(); i++) { 
            String deviceArray[] = devices.get(i).getDeviceId().split("-");
            list = "Simple DeviceId (sdId): " + deviceArray[1] + ", Product: " + devices.get(i).getDesc() +
            ", Gen: " + devices.get(i).getGen() + ", Channels: " + devices.get(i).getFeatureSets().size() + "\r\n";
            properties.put("Connected Device: " + i + "", list);
        }
        updateProperties(properties);
    }
    private void startRefresh() {
        //int refresh = Integer.valueOf(this.thing.getConfiguration().get("pollingInterval").toString());
        int refresh = pollingSize();
        if (channelList().size() == 0) {
            logger.warn("Channel List For Updating Is Empty");
        }
        if (refreshTask == null || refreshTask.isCancelled()) {
            refreshTask = scheduler.scheduleWithFixedDelay(this::updateStateAndChannels, 0, refresh, TimeUnit.SECONDS);
        }
    }

    private void updateStateAndChannels() {
        if (channelList().size() > 0) {
        //new Thread(new Runnable() {
            //@Override
            //public void run() {
                try {
                        listener.updateListener(partitionSize);
                } catch (Exception e) {
                }
    }
//}).start();
        }
    //}

    

    private void stopConnectionCheck() {
        if (connectionCheckTask != null) {
            logger.debug("Stop periodic connection check");
            connectionCheckTask.cancel(true);
            connectionCheckTask = null;
        }
    }

    

}
