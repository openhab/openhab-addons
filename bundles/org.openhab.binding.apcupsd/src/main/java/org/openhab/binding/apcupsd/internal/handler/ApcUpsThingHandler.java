/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.apcupsd.internal.handler;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.apcupsd.ApcUpsDBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ApcUpsThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Aitor Iturrioz - Initial contribution
 */
public class ApcUpsThingHandler extends BaseThingHandler implements NewDataListener, TcpConnectionListener {

    private final Logger logger = LoggerFactory.getLogger(ApcUpsThingHandler.class);

    private String ip;
    private Integer port;
    private Integer refresh;
    private Boolean updateOnlyOnChange;
    
    String magnitudeMappingJson = 
    		"{'STATUS': {'type': 'state', 'name': '" + ApcUpsDBindingConstants.CHANNEL_STATUS + "'}," +
    		 "'LINEV': {'type': 'state', 'name': '" + ApcUpsDBindingConstants.CHANNEL_LINE_V + "'}," +
    		 "'TIMELEFT': {'type': 'state', 'name': '" + ApcUpsDBindingConstants.CHANNEL_BATT_TIMELEFT + "'}," +
    		 "'BCHARGE': {'type': 'state', 'name': '" + ApcUpsDBindingConstants.CHANNEL_BATT_CHARGE + "'}," +
    		 "'BATTV': {'type': 'state', 'name': '" + ApcUpsDBindingConstants.CHANNEL_BATT_V + "'}," +
    		 "'BATTDATE': {'type': 'state', 'name': '" + ApcUpsDBindingConstants.CHANNEL_BATT_DATE + "'}," +
    		 "'LOADPCT': {'type': 'state', 'name': '" + ApcUpsDBindingConstants.CHANNEL_LOAD_PCT + "'}," +
    		 "'STARTTIME': {'type': 'state', 'name': '" + ApcUpsDBindingConstants.CHANNEL_START_TIME + "'}," +
    		 "'MODEL': {'type': 'property', 'name': '" + ApcUpsDBindingConstants.PROPERTY_MODEL + "'}," +
    		 "'FIRMWARE': {'type': 'property', 'name': '" + ApcUpsDBindingConstants.PROPERTY_FIRMWARE + "'}," +
    		 "'SERIALNO': {'type': 'property', 'name': '" + ApcUpsDBindingConstants.PROPERTY_SERIAL_NO + "'}," +
    		 "'UPSNAME': {'type': 'property', 'name': '" + ApcUpsDBindingConstants.PROPERTY_NAME + "'}}";
    
    private Map<String, Map<String,String>> magnitudeMapping = new HashMap<String, Map<String,String>>();
    // 0: updateOnlyOnChange, 1: updateOnEveryValue
    private Map<String, Boolean> magnitudeUpdateType = new HashMap<String, Boolean>();
    private Map<String, String> magnitudeToId = new HashMap<String, String>();
    private Map<String, String> idToMagnitude = new HashMap<String, String>();
    
    private ApcUpsDataHandler dataHandler;
    private TcpConnectionHandler tcpConnection;
    
	@SuppressWarnings("unchecked")
	public ApcUpsThingHandler(Thing thing) {
        super(thing);
        magnitudeMapping = new Gson().fromJson(magnitudeMappingJson, magnitudeMapping.getClass());
    }

    @Override
    public void initialize() {
        this.ip = (String) getConfig().get(ApcUpsDBindingConstants.PARAMETER_IP);        
        this.port = ((Number) getConfig().get(ApcUpsDBindingConstants.PARAMETER_PORT)).intValue();
        this.refresh = ((Number) getConfig().get(ApcUpsDBindingConstants.PARAMETER_REFRESH)).intValue();
        this.updateOnlyOnChange = (Boolean) getConfig().get(ApcUpsDBindingConstants.PARAMETER_UPDATE_ONLY_ON_CHANGE);

        if (this.ip == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "IP must be set!");
            return;
        }
        
        if (this.port == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set!");
            return;
        }
        
        if (this.refresh == null)
        	this.refresh = 10;
        
        if (this.updateOnlyOnChange == null)
        	this.updateOnlyOnChange = true;
        
        
        if (tcpConnection != null)
        	tcpConnection.stop();
        
        this.createMappings();
        
        tcpConnection = new TcpConnectionHandler(this.ip, this.port);
        tcpConnection.addConnectionListener(this);
        tcpConnection.start();
        
        if (dataHandler != null)
        	dataHandler.stop();
        
        dataHandler = new ApcUpsDataHandler(tcpConnection, this.refresh, scheduler);
        dataHandler.addDatalistener(this, this.magnitudeUpdateType);
        dataHandler.start();     
        
        updateStatus(ThingStatus.UNKNOWN);
    }
    
    @Override
    public void dispose() {
        this.dataHandler.stop();
        this.tcpConnection.stop();
        logger.info("Stopped APC UPS Thing Handler");
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
    }
    
    @Override
    public void channelLinked(ChannelUID channelUID) {
    	logger.trace("Channel {} is linked", channelUID.getId());
    	String channelId = channelUID.getId();
    	String magnitude = this.idToMagnitude.get(channelId);
    	String value = this.dataHandler.getData(magnitude);
    	if (value != null)
    		this.updateChannelState(channelId, value);
    }
    
	@Override
	public void onConnectionStatusChange(boolean status) {
		logger.debug("Connection status has changed to: {}", status);
		if (status)
			updateStatus(ThingStatus.ONLINE);
		else
			updateStatus(ThingStatus.OFFLINE);
		
	}
        
    private void updateChannelState(String channelID, String value) {
    	String itemType = getThing().getChannel(channelID).getAcceptedItemType();
    	State state = createState(itemType, value);
    	
    	if (state != null) {
    		logger.trace("Update channel '{}' to: {}", channelID, value);
    		updateState(channelID, state);
    	}
    }
    
    private State createState(String itemType, String value) {
    	State state = null;
    	switch(itemType) {
			case("String"):
				state = new StringType(value);
				break;
			case("Number:ElectricPotential"):
				state = new QuantityType<ElectricPotential>(new BigDecimal(value), SmartHomeUnits.VOLT);
				break;
			case("Number:Dimensionless"):
				state = new QuantityType<Dimensionless>(new BigDecimal(value), SmartHomeUnits.PERCENT);
				break;
			case("Number:Time"):
				state = new QuantityType<Time>(new BigDecimal(value), SmartHomeUnits.MINUTE);
				break;
			case("DateTime"):
				String[] split_value = value.split(" ");
				if (split_value.length == 1)
					state = new DateTimeType(split_value[0]);
				else if (split_value.length == 2)
					state = new DateTimeType(String.format("%sT%s", split_value[0], split_value[1]));
				else if (split_value.length == 3)
					state = new DateTimeType(String.format("%sT%s%s", split_value[0], split_value[1], split_value[2]));
				else
					logger.debug("Unknown datetype format");
				break;
			default:
				logger.debug("Unknown itemtype: '{}'", itemType);
				break;
		}
		return state;
    }

	@Override
	public void onNewData(String magnitude, String value) {
		logger.debug("New data for '{}': {}", magnitude, value);
		if (!magnitudeMapping.containsKey(magnitude))
			return;
		
		// State channel
		if (magnitudeMapping.get(magnitude).get("type").equals("state")) {
			String channelId = magnitudeMapping.get(magnitude).get("name");
			this.updateChannelState(channelId, value);
		}
		
		// Trigger channel
		if (magnitudeMapping.get(magnitude).get("type").equals("trigger")) {
			String channelId = magnitudeMapping.get(magnitude).get("name");
			triggerChannel(channelId, value);
		}
		
		// Thing property
		if (magnitudeMapping.get(magnitude).get("type").equals("property")) {
			String property = magnitudeMapping.get(magnitude).get("name");
			updateProperty(property, value);
		}
	}
	
	private void createMappings() {
		for (Map.Entry<String, Map<String,String>> entry : magnitudeMapping.entrySet()) {
			String magnitude = entry.getKey();
			String id = entry.getValue().get("name");
			Boolean type = !entry.getValue().get("type").equals("state") || this.updateOnlyOnChange;
			this.magnitudeUpdateType.put(magnitude, type);
			this.magnitudeToId.put(magnitude, id);
			this.idToMagnitude.put(id, magnitude);
		}
	}
}
