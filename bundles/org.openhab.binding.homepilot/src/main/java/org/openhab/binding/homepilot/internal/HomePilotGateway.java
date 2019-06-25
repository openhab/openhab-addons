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
package org.openhab.binding.homepilot.internal;

import static org.openhab.binding.homepilot.HomePilotBindingConstants.THING_TYPE_ROLLERSHUTTER;
import static org.openhab.binding.homepilot.HomePilotBindingConstants.THING_TYPE_SWITCH;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.Fields;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Steffen Stundzig - Initial contribution
 */
public class HomePilotGateway {

	private final String id;
	private final HomePilotConfig config;
	private final HttpClient httpClient;

	public HomePilotGateway(String id, HomePilotConfig config) {
		this.id = id;
		this.config = config;
		httpClient = new HttpClient();
		try {
			httpClient.start();
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public String getId() {
		return id;
	}

	public List<HomePilotDevice> loadAllDevices() {
		String url = String.format("http://%s/deviceajax.do?alldevices", config.getAddress());		
		try {
			String contentAsString = httpClient.POST(url)
					.header(HttpHeader.CONTENT_TYPE, "application/json;charset=utf-8").send().getContentAsString();
			return transform2Devices(contentAsString);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<HomePilotDevice> transform2Devices(String json) {
		final List<HomePilotDevice> devices = new ArrayList<HomePilotDevice>();
		try {
			final JsonObject responseJSON = new JsonParser().parse(json).getAsJsonObject();
			final JsonArray devicesJSON = responseJSON.get("devices").getAsJsonArray();
			for (int i = 0; i < devicesJSON.size(); i++) {
				final JsonObject deviceJSON = devicesJSON.get(i).getAsJsonObject();
				int deviceGroup = deviceJSON.get("deviceGroup").getAsInt();
				final ThingTypeUID thingTypeUID;
				if (deviceGroup == 1) {
					thingTypeUID = THING_TYPE_SWITCH;
				} else if (deviceGroup == 2) {
					thingTypeUID = THING_TYPE_ROLLERSHUTTER;
				} else {
					throw new RuntimeException(String.format("unknown serial %s for device %s", deviceGroup,
							deviceJSON.get("did").getAsString()));
				}
				final HomePilotDevice device = new HomePilotDevice(thingTypeUID, deviceJSON.get("did").getAsInt(),
						deviceJSON.get("name").getAsString(), deviceJSON.get("description").getAsString(),
						deviceJSON.get("position").getAsInt());
				devices.add(device);
			}
		} catch (JsonIOException e) {
			throw new RuntimeException(e);
		}
		return devices;
	}

	public boolean handleSetPosition(final String deviceId, final int position) {
		final Fields fields = new Fields();
		fields.add("cid", "9");
		fields.add("goto", String.valueOf(position));
		return sendFields(deviceId, fields);
	}

	public boolean handleSetOnOff(final String deviceId, final boolean on) {
		final Fields fields = new Fields();
		fields.add("cid", on ? "10" : "11");
		return sendFields(deviceId, fields);
	}

	public boolean handleStop(final String deviceId) {
		final Fields fields = new Fields();
		fields.add("cid", "2");
		return sendFields(deviceId, fields);
	}

	private boolean sendFields(final String deviceId, final Fields fields) {
		final String url = String.format("http://%s/deviceajax.do?", config.getAddress());
		try {
			fields.add("did", deviceId);
			fields.add("command", "1");
			final String response = httpClient.POST(url)
					.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8")
					.header(HttpHeader.ACCEPT, "application/json;charset=utf-8")
					.content(new FormContentProvider(fields)).send().getContentAsString();
			return "uisuccess".equals(new JsonParser().parse(response).getAsJsonObject().get("status").getAsString());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public HomePilotDevice loadDevice(final String deviceId) {
		for (HomePilotDevice device : loadAllDevices()) {
			if (deviceId.equals(device.getDeviceId())) {
				return device;
			}
		}
		throw new IllegalStateException("no device for id " + deviceId + " found");
	}
}
