/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import static org.openhab.binding.wink.WinkBindingConstants.*;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.wink.client.IWinkDevice;
import org.openhab.binding.wink.client.WinkSupportedDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Joel Shuman - Initial contribution
 */
public class ThermostatHandler extends WinkBaseThingHandler {
	private final Logger logger = LoggerFactory.getLogger(ThermostatHandler.class);

	public ThermostatHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void handleWinkCommand(ChannelUID channelUID, Command command) {
		if (command instanceof RefreshType) {
			logger.debug("Refreshing state");
			updateDeviceState(getDevice());
		}
		if (channelUID.getId().equals(CHANNEL_THERMOSTAT_CURRENTSETPOINT)) {
			if (command instanceof Number) {
				logger.debug("Setting desired temperature {}", command);
				float temperature = ((Number) command).floatValue();
				setDesiredTemperature(temperature);

				// Try running the update after a few seconds.
				delayedUpdateState();
			}
		} else if (channelUID.getId().equals(CHANNEL_THERMOSTAT_CURRENTMODE)) {
			if (command instanceof StringType) {
				logger.debug("Setting desired mode {}", command);
				String mode = ((StringType) command).toString();
				setDesiredMode(mode);

				// Try running the update after a few seconds.
				delayedUpdateState();
			}
		}
	}
	
	@Override
	protected boolean connectionStatus(IWinkDevice device){
		Map<String, String> jsonData = getDevice().getCurrentStateComplexJson();
		return jsonData.get("connection").equals("true");
	}

	private void setDesiredTemperature(float temperature) {
		bridgeHandler.setThermostatTemperature(getDevice(), temperature);
	}

	private void setDesiredMode(String mode) {
		bridgeHandler.setThermostatMode(getDevice(), mode);
	}

	private void delayedUpdateState() {
		final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.schedule(new Runnable() {
			@Override
			public void run() {
				updateDeviceState(getDevice());
			}
		}, 20, TimeUnit.SECONDS);
	}

	@Override
	protected WinkSupportedDevice getDeviceType() {
		logger.error("Its a thermostat!");
		return WinkSupportedDevice.THERMOSTAT;
	}

	@Override
	protected void updateDeviceState(IWinkDevice device) {
		logger.error("Updating Thermostat-=-=-=-=-=-=-");
		Map<String, String> jsonData = device.getCurrentStateComplexJson();
		Map<String, String> jsonDataDesired = device.getDesiredState();

		String units = "f";
		if (jsonData.get("units") != null && !jsonData.get("units").equals("null")) {
			units = jsonData.get("units");
		}

		if (jsonData.get("temperature") != null && !jsonData.get("temperature").equals("null")) {
			Float temperature = Float.valueOf(jsonData.get("temperature"));
			if ("f".equals(units)) {
				temperature = temperature * 1.8f + 32;
			}
			updateState(CHANNEL_THERMOSTAT_CURRENTTEMPERATURE, new DecimalType(temperature));
			logger.error("Updated CHANNEL_THERMOSTAT_CURRENTTEMPERATURE-=-=-=-=-=-=-");
		}

		// For the channels the user changes, I need to take into account the desired data as well.
		if (jsonData.get("mode") != null && !jsonData.get("mode").equals("null")) {
			String desiredOperationMode = jsonDataDesired.get("mode");
			String currentOperationMode = jsonData.get("mode");
			String currentOperationModePretty = "";
			float currentSetPoint = 0.0f;
			float minSet = Float.valueOf(jsonData.get("min_set_point"));
			float maxSet = Float.valueOf(jsonData.get("max_set_point"));
			float minSetDesired = Float.valueOf(jsonDataDesired.get("min_set_point"));
			float maxSetDesired = Float.valueOf(jsonDataDesired.get("max_set_point"));
			logger.debug(
					"Updating State for set point and set mode. current mode '{}', desired mode '{}',"
							+ " current min '{}', desired min '{}', current max '{}', desired max '{}'",
					currentOperationMode, desiredOperationMode, minSet, minSetDesired, maxSet, maxSetDesired);
			if (currentOperationMode.equals("cool_only")) {
				currentSetPoint = maxSet;
				currentOperationModePretty = "Cool";
			} else if (currentOperationMode.equals("heat_only")) {
				currentSetPoint = minSet;
				currentOperationModePretty = "Heat";
			} else { // auto
				// return the lower of the two.
				currentSetPoint = maxSet < minSet ? maxSet : minSet;
				currentOperationModePretty = "Auto";
			}

			if ("f".equals(units)) {
				currentSetPoint = currentSetPoint * 1.8f + 32;
			}

			updateState(CHANNEL_THERMOSTAT_CURRENTSETPOINT, new DecimalType(currentSetPoint));
			updateState(CHANNEL_THERMOSTAT_CURRENTMODE, new StringType(currentOperationModePretty));
			logger.error("Updated CHANNEL_THERMOSTAT_CURRENTSETPOINT-=-=-=-=-=-=-");
			logger.error("Updated CHANNEL_THERMOSTAT_CURRENTMODE-=-=-=-=-=-=-");
		}

		if (jsonData.get("smart_temperature") != null && !jsonData.get("smart_temperature").equals("null")) {
			Float smartTemperature = Float.valueOf(jsonData.get("smart_temperature"));
			if ("f".equals(units)) {
				smartTemperature = smartTemperature * 1.8f + 32;
			}
			updateState(CHANNEL_THERMOSTAT_SMARTTEMPERATURE, new DecimalType(smartTemperature));
			logger.error("Updated CHANNEL_THERMOSTAT_SMARTTEMPERATURE-=-=-=-=-=-=-");
		}

		if (jsonData.get("external_temperature") != null && !jsonData.get("external_temperature").equals("null")) {
			Float externalTemperature = Float.valueOf(jsonData.get("external_temperature"));
			if ("f".equals(units)) {
				externalTemperature = externalTemperature * 1.8f + 32;
			}
			updateState(CHANNEL_THERMOSTAT_EXTERNALTEMPERATURE, new DecimalType(externalTemperature));
		}

		if (jsonData.get("humidity") != null && !jsonData.get("humidity").equals("null")) {
			final Float humidity = Float.valueOf(jsonData.get("humidity"));
			updateState(CHANNEL_THERMOSTAT_HUMIDITY, new DecimalType(humidity));
			logger.error("Updated CHANNEL_THERMOSTAT_HUMIDITY-=-=-=-=-=-=-");
		}

		if (jsonData.get("connection") != null && !jsonData.get("connection").equals("null")) {
			final boolean connectionState = Boolean.valueOf(jsonData.get("connection"));
			updateState(CHANNEL_THERMOSTAT_ONLINE, connectionState ? OnOffType.ON : OnOffType.OFF);
		}

		if (jsonData.get("has_fan") != null && !jsonData.get("has_fan").equals("null")) {
			final boolean hasFan = Boolean.valueOf(jsonData.get("has_fan"));
			updateState(CHANNEL_THERMOSTAT_HASFAN, hasFan ? OnOffType.ON : OnOffType.OFF);
		}

		if (jsonData.get("eco_target") != null && !jsonData.get("eco_target").equals("null")) {
			final boolean ecoMode = Boolean.valueOf(jsonData.get("eco_target"));
			updateState(CHANNEL_THERMOSTAT_ECOMODE, ecoMode ? OnOffType.ON : OnOffType.OFF);
		}

		if (jsonData.get("technician_name") != null && !jsonData.get("technician_name").equals("null")) {
			updateState(CHANNEL_THERMOSTAT_TECHNAME, new StringType(jsonData.get("technician_name")));
		}

		if (jsonData.get("technician_phone") != null && !jsonData.get("technician_phone").equals("null")) {
			updateState(CHANNEL_THERMOSTAT_TECHPHONE, new StringType(jsonData.get("technician_phone")));
		}

		if (jsonData.get("fan_active") != null && !jsonData.get("fan_active").equals("null")) {
			final boolean fanState = Boolean.valueOf(jsonData.get("fan_active"));
			updateState(CHANNEL_THERMOSTAT_FANACTIVE, fanState ? OnOffType.ON : OnOffType.OFF);
			logger.error("Updated CHANNEL_THERMOSTAT_FANACTIVE-=-=-=-=-=-=-");
		}

		if (jsonData.get("last_error") != null && !jsonData.get("last_error").equals("null")) {
			updateState(CHANNEL_THERMOSTAT_LASTERROR, new StringType(jsonData.get("last_error")));
		}

		if (jsonData.get("occupied") != null && !jsonData.get("occupied").equals("null")) {
			final boolean occupied = Boolean.valueOf(jsonData.get("occupied"));
			updateState(CHANNEL_THERMOSTAT_OCCUPIED, occupied ? OnOffType.ON : OnOffType.OFF);
		}

		final boolean auxActive = Boolean.valueOf(jsonData.get("aux_active"));
		final boolean coolActive = Boolean.valueOf(jsonData.get("cool_active"));
		final boolean heatActive = Boolean.valueOf(jsonData.get("heat_active"));

		String runningMode = null;
		if (auxActive && !coolActive && !heatActive) {
			runningMode = "Aux";
		} else if (!auxActive && coolActive && !heatActive) {
			runningMode = "Cool";
		} else if (!auxActive && !coolActive && heatActive) {
			runningMode = "Heat";
		} else if (!auxActive && !coolActive && !heatActive) {
			runningMode = "Idle";
		} else {
			runningMode = "Unknown";
		}

		updateState(CHANNEL_THERMOSTAT_RUNNINGMODE, new StringType(runningMode));
		logger.error("All Done Updating Thermostat-=-=-=-=-=-=-");
	}
}
