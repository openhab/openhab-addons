/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miele.handler;

import static org.openhab.binding.miele.MieleBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.miele.handler.MieleBridgeHandler.DeviceClassObject;
import org.openhab.binding.miele.handler.MieleBridgeHandler.DeviceMetaData;
import org.openhab.binding.miele.handler.MieleBridgeHandler.DeviceOperation;
import org.openhab.binding.miele.handler.MieleBridgeHandler.DeviceProperty;
import org.openhab.binding.miele.handler.MieleBridgeHandler.HomeDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link MieleApplianceHandler} is an abstract class
 * responsible for handling commands, which are sent to one
 * of the channels of the appliance that understands/"talks"
 * the {@link ApplianceChannelSelector} datapoints
 *
 * @author Karel Goderis - Initial contribution
 */
public abstract class MieleApplianceHandler<E extends Enum<E> & ApplianceChannelSelector> extends BaseThingHandler
        implements ApplianceStatusListener {

    private final Logger logger = LoggerFactory.getLogger(MieleApplianceHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_DISHWASHER,
            THING_TYPE_OVEN, THING_TYPE_FRIDGE, THING_TYPE_DRYER, THING_TYPE_HOB, THING_TYPE_FRIDGEFREEZER,
            THING_TYPE_HOOD, THING_TYPE_WASHINGMACHINE);

    protected Gson gson = new Gson();

    protected String UID;
    protected MieleBridgeHandler bridgeHandler;
    private Class<E> selectorType;
    protected String modelID;

    protected Map<String, String> metaDataCache = new HashMap<String, String>();

    public MieleApplianceHandler(Thing thing, Class<E> selectorType, String modelID) {
        super(thing);
        this.selectorType = selectorType;
        this.modelID = modelID;
    }

    public ApplianceChannelSelector getValueSelectorFromChannelID(String valueSelectorText)
            throws IllegalArgumentException {

        for (ApplianceChannelSelector c : selectorType.getEnumConstants()) {
            if (c.getChannelID() != null && c.getChannelID().equals(valueSelectorText)) {
                return c;
            }
        }

        throw new IllegalArgumentException("Not valid value selector");
    }

    public ApplianceChannelSelector getValueSelectorFromMieleID(String valueSelectorText)
            throws IllegalArgumentException {

        for (ApplianceChannelSelector c : selectorType.getEnumConstants()) {
            if (c.getMieleID() != null && c.getMieleID().equals(valueSelectorText)) {
                return c;
            }
        }

        throw new IllegalArgumentException("Not valid value selector");
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Miele appliance handler.");
        final String UID = (String) getThing().getConfiguration().getProperties().get(APPLIANCE_ID);
        if (UID != null) {
            this.UID = UID;
            if (getMieleBridgeHandler() != null) {
                ThingStatusInfo statusInfo = getBridge().getStatusInfo();
                updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
            }
        }
    }

    public void onBridgeConnectionResumed() {
        if (getMieleBridgeHandler() != null) {
            ThingStatusInfo statusInfo = getBridge().getStatusInfo();
            updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposes. Unregistering listener.");
        if (UID != null) {
            MieleBridgeHandler bridgeHandler = getMieleBridgeHandler();
            if (bridgeHandler != null) {
                getMieleBridgeHandler().unregisterApplianceStatusListener(this);
            }
            UID = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Here we could handle commands that are common to all Miele Appliances, but so far I don't know of any
        if (command instanceof RefreshType) {
            // Placeholder for future refinement
            return;
        }
    }

    @Override
    public void onApplianceStateChanged(String UID, DeviceClassObject dco) {

        String myUID = "hdm:ZigBee:" + (String) getThing().getConfiguration().getProperties().get(APPLIANCE_ID);
        String modelID = StringUtils.right(dco.DeviceClass,
                dco.DeviceClass.length() - new String("com.miele.xgw3000.gateway.hdm.deviceclasses.Miele").length());

        if (myUID.equals(UID)) {

            if (modelID.equals(this.modelID)) {
                for (JsonElement prop : dco.Properties.getAsJsonArray()) {
                    try {
                        DeviceProperty dp = gson.fromJson(prop, DeviceProperty.class);
                        dp.Value = StringUtils.trim(dp.Value);
                        dp.Value = StringUtils.strip(dp.Value);

                        onAppliancePropertyChanged(UID, dp);
                    } catch (Exception p) {
                        // Ignore - this is due to an unrecognized and not yet reverse-engineered array property
                    }
                }

                for (JsonElement operation : dco.Operations.getAsJsonArray()) {
                    try {
                        DeviceOperation devop = gson.fromJson(operation, DeviceOperation.class);
                        DeviceMetaData pmd = gson.fromJson(devop.Metadata, DeviceMetaData.class);
                    } catch (Exception p) {
                        // Ignore - this is due to an unrecognized and not yet reverse-engineered array property
                    }
                }
            }
        }
    }

    @Override
    public void onAppliancePropertyChanged(String UID, DeviceProperty dp) {
        String myUID = "hdm:ZigBee:" + (String) getThing().getConfiguration().getProperties().get(APPLIANCE_ID);

        String dpValue = StringUtils.strip(dp.Value);
        dpValue = StringUtils.trim(dpValue);

        if (myUID.equals(UID)) {
            try {
                DeviceMetaData dmd = null;
                if (dp.Metadata == null) {
                    String metadata = metaDataCache.get(new StringBuilder().append(dp.Name).toString().trim());
                    if (metadata != null) {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonMetaData = (JsonObject) parser.parse(metadata);
                        dmd = gson.fromJson(jsonMetaData, DeviceMetaData.class);
                        // only keep the enum, if any - that's all we care for events we receive via multicast
                        // all other fields are nulled
                        dmd.LocalizedID = null;
                        dmd.LocalizedValue = null;
                        dmd.Filter = null;
                        dmd.description = null;
                    }
                }
                if (dp.Metadata != null) {
                    String metadata = StringUtils.replace(dp.Metadata.toString(), "enum", "MieleEnum");
                    JsonParser parser = new JsonParser();
                    JsonObject jsonMetaData = (JsonObject) parser.parse(metadata);
                    dmd = gson.fromJson(jsonMetaData, DeviceMetaData.class);
                    metaDataCache.put(new StringBuilder().append(dp.Name).toString().trim(), metadata);
                }

                ApplianceChannelSelector selector = null;
                try {
                    selector = getValueSelectorFromMieleID(dp.Name);
                } catch (Exception h) {
                    logger.trace("{} is not a valid channel for a {}", dp.Name, modelID);
                }

                if (selector != null && !selector.isProperty()) {
                    ChannelUID theChannelUID = new ChannelUID(getThing().getUID(), selector.getChannelID());
                    logger.trace("Update state of {} with '{}'",
                            new Object[] { theChannelUID.toString(), dpValue });

                    if (dp.Value != null) {
                        logger.trace("Update state of {} with getState '{}'",
                                new Object[] { theChannelUID.toString(), selector.getState(dpValue, dmd) });
                        updateState(theChannelUID, selector.getState(dpValue, dmd));
                    } else {
                        updateState(theChannelUID, UnDefType.UNDEF);
                    }
                } else {
                    if (selector != null && dpValue != null) {
                        logger.debug("Updating the property '{}' of '{}' to '{}'",
                                new Object[] { selector.getChannelID(), getThing().getUID(),
                                        selector.getState(dpValue, dmd).toString() });
                        Map<String, String> properties = editProperties();
                        properties.put(selector.getChannelID(), selector.getState(dpValue, dmd).toString());
                        updateProperties(properties);
                    }
                }
            } catch (IllegalArgumentException e) {
                logger.error("An exception occurred while processing a changed device property :'{}'", e.getMessage());
            }
        }
    }

    @Override
    public void onApplianceRemoved(HomeDevice appliance) {
        if (UID != null) {
            if (UID.equals(appliance.UID)) {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    @Override
    public void onApplianceAdded(HomeDevice appliance) {
        if (UID != null) {
            if (UID.equals(appliance.UID)) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    private synchronized MieleBridgeHandler getMieleBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof MieleBridgeHandler) {
                this.bridgeHandler = (MieleBridgeHandler) handler;
                this.bridgeHandler.registerApplianceStatusListener(this);
            } else {
                return null;
            }
        }
        return this.bridgeHandler;
    }
}