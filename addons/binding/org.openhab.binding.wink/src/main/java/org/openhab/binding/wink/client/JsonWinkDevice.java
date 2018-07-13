/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class parses json from the wink api and from pubnub and produces an IWinkDevice
 *
 * @author Shawn Crosby
 *
 */
public class JsonWinkDevice implements IWinkDevice {
	private JsonObject json;
	
    private final Logger logger = LoggerFactory.getLogger(JsonWinkDevice.class);

	public JsonWinkDevice(JsonObject element) {
		this.json = element;
	}

	@Override
	public String getId() {
		return json.get("uuid").getAsString();
	}

	@Override
	public String getName() {
		return json.get("name").getAsString();
	}

	@Override
	public WinkSupportedDevice getDeviceType() {
		if (json.get("lock_id") != null) {
			return WinkSupportedDevice.LOCK;
		} else if (json.get("light_bulb_id") != null) {
			return WinkSupportedDevice.DIMMABLE_LIGHT;
		} else if (json.get("binary_switch_id") != null) {
			return WinkSupportedDevice.BINARY_SWITCH;
		} else if (json.get("remote_id") != null) {
			return WinkSupportedDevice.REMOTE;
		} else if (json.get("door_bell_id") != null) {
			return WinkSupportedDevice.DOORBELL;
		} else if (json.get("thermostat_id") != null) {
			return WinkSupportedDevice.THERMOSTAT;
		} else {
			return WinkSupportedDevice.HUB;
		}
	}

	@Override
	public String getPubNubSubscriberKey() {
		return json.get("subscription").getAsJsonObject().get("pubnub").getAsJsonObject().get("subscribe_key")
				.getAsString();
	}

	@Override
	public String getPubNubChannel() {
		return json.get("subscription").getAsJsonObject().get("pubnub").getAsJsonObject().get("channel").getAsString();
	}

	@Override
	public String getProperty(String property) {
		return json.get(property).getAsString();
	}

	@Override
	public Map<String, String> getCurrentState() {
		JsonObject data = json.get("last_reading").getAsJsonObject();
		return toMap(data);
	}

	@Override
	public Map<String, String> getDesiredState() {
		JsonObject data = json.get("desired_state").getAsJsonObject();
		return toMap(data);
	}

	private Map<String, String> toMap(JsonObject json) {
		return new Gson().fromJson(json, new TypeToken<HashMap<String, String>>() {
		}.getType());
	}

	@Override
	public Map<String, String> getCurrentStateComplexJson() {
		try
		{
			JsonObject data = json.get("last_reading").getAsJsonObject();
			Map<String, String> theMap = new HashMap<String, String>();
			Set<Entry<String, JsonElement>> entrySet = data.entrySet();
			for (Map.Entry<String, JsonElement> entry : entrySet) {
				if (entry.getValue().isJsonArray()) {
					// If it is a json array, iterate thru the items and add them as comma separated strings.
					// This is the item that does not parse correctly in the toMap() call above.
					String theValues = "";
					for (JsonElement element : entry.getValue().getAsJsonArray()) {
						if (element.isJsonNull()) {
							theValues += "null,";
							continue;
						}
						theValues += element.getAsString() + ",";
					}
					theMap.put(entry.getKey(), theValues.replaceFirst(",$", "")); // Remove trailing ','
					logger.debug("json data: {}:{}", entry.getKey(), theValues.replaceFirst(",$", ""));
					continue;
				}

				if (entry.getValue().isJsonNull()) {
					theMap.put(entry.getKey(), "null");
					continue;
				}

				theMap.put(entry.getKey(), entry.getValue().getAsString());
				logger.debug("json data2: {}:{}", entry.getKey(), entry.getValue().getAsString());
			}
			return theMap;
		} catch (RuntimeException e) {
			logger.error("getCurrentStateComplexJson threw: {}", e.getMessage());
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append(this.getDeviceType() + " ");
		ret.append("Device: (" + this.getId() + ") ");
		ret.append(this.getName());

		return ret.toString();
	}

}
