/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.antiferencematrix.handler;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.antiferencematrix.AntiferenceMatrixBindingConstants;
import org.openhab.binding.antiferencematrix.internal.AntiferenceMatrixApi;
import org.openhab.binding.antiferencematrix.internal.discovery.AntiferenceMatrixDiscoveryListener;
import org.openhab.binding.antiferencematrix.internal.model.InputPort;
import org.openhab.binding.antiferencematrix.internal.model.InputPortDetails;
import org.openhab.binding.antiferencematrix.internal.model.OutputPort;
import org.openhab.binding.antiferencematrix.internal.model.OutputPortDetails;
import org.openhab.binding.antiferencematrix.internal.model.Port;
import org.openhab.binding.antiferencematrix.internal.model.PortList;
import org.openhab.binding.antiferencematrix.internal.model.SystemDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AntiferenceMatrixBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(AntiferenceMatrixBridgeHandler.class);
    private AntiferenceMatrixApi api = null;
    private int refreshIntervalInSeconds = 60;

    private AntiferenceMatrixDiscoveryListener listener = null;
    protected ScheduledFuture<?> refreshTask;

    public AntiferenceMatrixBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        // TODO Auto-generated method stub
        super.updateConfiguration(configuration);
    }

    @Override
    public void initialize() {
        logger.info("Initalizing matrix: {}", getThing().getLabel());
        try {
            Configuration configuration = getThing().getConfiguration();
            String hostname = (String) configuration.get("address");
            BigDecimal refreshInterval = (BigDecimal) configuration.get("pollingInterval");
            this.refreshIntervalInSeconds = refreshInterval.intValue();
            logger.debug("Antiference Matrix Hostname {}", hostname);
            logger.debug("Antiference Matrix Refresh Interval {}", refreshIntervalInSeconds);
            api = new AntiferenceMatrixApi(hostname);
            api.start();
            matrixStatusUpdate();
            startRefreshTask();
        } catch (Exception e) {
            logger.error("Error starting Matrix API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error connecting to API: " + e.getMessage());
        }
        logger.info("Finished initalizing matrix: {}", getThing().getLabel());
    }

    private void startRefreshTask() {
        disposeRefreshTask();

        refreshTask = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    fullUpdate();
                } catch (Exception e) {
                    logger.error("Error whilst refreshing", e);
                }
            }
        }, 0, refreshIntervalInSeconds, TimeUnit.SECONDS);
    }

    private void disposeRefreshTask() {
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
    }

    @Override
    public void dispose() {
        disposeRefreshTask();
        try {
            api.stop();
        } catch (Exception e) {
            logger.error("Error stopping http client", e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            handleRefreshCommand(channelUID, (RefreshType) command);
        }
    }

    protected void handleRefreshCommand(ChannelUID channelUID, RefreshType command) {
        matrixStatusUpdate();
    }

    private void fullUpdate() {
        matrixStatusUpdate();
        portStatusUpdate();
    }

    public void matrixStatusUpdate() {
        SystemDetails systemDetails = api.getSystemDetails();
        if (!systemDetails.getResult()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            updateState(
                    new ChannelUID(getThing().getUID(), AntiferenceMatrixBindingConstants.PORT_STATUS_MESSAGE_CHANNEL),
                    new StringType(systemDetails.getStatusMessage()));
            if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    public void portStatusUpdate() {
        Map<Integer, AntiferenceMatrixOutputHandler> outputPortHandlers = new HashMap<Integer, AntiferenceMatrixOutputHandler>();
        Map<Integer, AntiferenceMatrixInputHandler> inputPortHandlers = new HashMap<Integer, AntiferenceMatrixInputHandler>();
        for (Thing handler : getThing().getThings()) {
            ThingHandler thingHandler = handler.getHandler();
            if (thingHandler instanceof AntiferenceMatrixOutputHandler) {
                AntiferenceMatrixOutputHandler outputHandler = (AntiferenceMatrixOutputHandler) thingHandler;
                outputPortHandlers.put(outputHandler.getOutputId(), outputHandler);
            } else if (thingHandler instanceof AntiferenceMatrixInputHandler) {
                AntiferenceMatrixInputHandler inputHandler = (AntiferenceMatrixInputHandler) thingHandler;
                inputPortHandlers.put(inputHandler.getInputId(), inputHandler);
            }
        }

        PortList portList = api.getPortList();
        for (Port port : portList.getPorts()) {
            if (port instanceof InputPort) {
                InputPortDetails portDetails = api.getInputPortDetails(port.getBay());
                AntiferenceMatrixInputHandler inputPortHandler = inputPortHandlers.get(portDetails.getBay());
                if (inputPortHandler != null) {
                    inputPortHandler.refresh(portDetails);
                }
                for (AntiferenceMatrixOutputHandler outputHandler : outputPortHandlers.values()) {
                    outputHandler.refresh(portDetails);
                }
            } else if (port instanceof OutputPort) {
                OutputPortDetails portDetails = api.getOutputPortDetails(port.getBay());
                AntiferenceMatrixOutputHandler outputPortHandler = outputPortHandlers.get(portDetails.getBay());
                if (outputPortHandler != null) {
                    outputPortHandler.refresh(portDetails);
                }
            }
        }
    }

    public void smallUpdate() {
        PortList portList = api.getPortList();
        notifyDiscoveryListener(portList);
    }

    private void notifyDiscoveryListener(PortList portList) {
        if (listener != null) {
            listener.update(portList);
        }
    }

    public void registerDiscoveryListener(AntiferenceMatrixDiscoveryListener listener) {
        this.listener = listener;
    }

    public void unregisterDiscoveryListener() {
        this.listener = null;
    }

    public void changePower(int outputId, OnOffType command) {
        boolean on;
        switch (command) {
            case ON:
                on = true;
                break;
            case OFF:
            default:
                on = false;
        }
        api.changePower(outputId, on);
    }

    public void changeSource(int outputId, DecimalType command) {
        api.changeSource(outputId, command.intValue());
    }

    public OutputPortDetails getOutputPortDetails(int outputId) {
        return api.getOutputPortDetails(outputId);
    }

    public InputPortDetails getInputPortDetails(int inputId) {
        return api.getInputPortDetails(inputId);
    }
}
