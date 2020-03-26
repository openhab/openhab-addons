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
package org.openhab.binding.fox.internal;

import static org.openhab.binding.fox.internal.FoxBindingConstants.*;

import java.time.Duration;
import java.time.Instant;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.fox.internal.core.Fox;
import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.core.FoxMessenger;
import org.openhab.binding.fox.internal.core.FoxMessengerTcpIp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FoxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
public class FoxHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FoxHandler.class);
    private Fox fox = new Fox(new FoxMessengerTcpIp());
    private Instant pingTime = Instant.now();
    private boolean pingError = false;
    private boolean finishConnectionTask = false;

    private FoxTaskHandler taskHandler = new FoxTaskHandler();
    private FoxResultHandler resultHandler = new FoxResultHandler();

    private @Nullable FoxDynamicStateDescriptionProvider stateDescriptionProvider;
    private @Nullable FoxDynamicCommandDescriptionProvider commandDescriptionProvider;

    public FoxHandler(Thing thing, @Nullable FoxDynamicStateDescriptionProvider stateDescriptionProvider,
            @Nullable FoxDynamicCommandDescriptionProvider commandDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.commandDescriptionProvider = commandDescriptionProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_TASK_COMMAND.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                String data = ((StringType) command).toString();
                write(data);
            }
        }
    }

    private void loadTaskCommands() {
        Channel taskCommandCh = getThing().getChannel(CHANNEL_TASK_COMMAND);
        if (taskCommandCh != null && commandDescriptionProvider != null) {
            commandDescriptionProvider.setCommandOptions(taskCommandCh.getUID(), taskHandler.listCommands());
        }
    }

    private void loadTaskStates() {
        Channel taskStateCh = getThing().getChannel(CHANNEL_TASK_STATE);
        if (taskStateCh != null && stateDescriptionProvider != null) {
            stateDescriptionProvider.setStateOptions(taskStateCh.getUID(), taskHandler.listStates());
        }
    }

    private void loadResultTriggers() {
        Channel resultTriggerCh = getThing().getChannel(CHANNEL_RESULT_TRIGGER);
        if (resultTriggerCh != null && stateDescriptionProvider != null) {
            stateDescriptionProvider.setStateOptions(resultTriggerCh.getUID(), resultHandler.listStates());
        }
    }

    private void loadResultStates() {
        Channel resultStateCh = getThing().getChannel(CHANNEL_RESULT_STATE);
        if (resultStateCh != null && stateDescriptionProvider != null) {
            stateDescriptionProvider.setStateOptions(resultStateCh.getUID(), resultHandler.listStates());
        }
    }

    private void loadTasksAndResults(@Nullable String functionsJson) {
        if (functionsJson == null) {
            return;
        }
        FoxFunctionsConfig config = new FoxFunctionsConfig(functionsJson);
        taskHandler.setCommands(config.getTasks());
        resultHandler.setStates(config.getResults());
        loadTaskCommands();
        loadTaskStates();
        loadResultTriggers();
        loadResultStates();
    }

    private void configMessengerTcpIp(FoxMessengerTcpIp m, String host, String password) {
        m.setHost(host);
        m.setTimeout(1000);
        m.setPassword(password);
    }

    private void connect(String host, String password) throws FoxException {
        FoxMessenger messenger = fox.getMessenger();
        messenger.close();
        if (messenger instanceof FoxMessengerTcpIp) {
            configMessengerTcpIp((FoxMessengerTcpIp) messenger, host, password);
        }
        messenger.open();
    }

    private boolean requestTask(String task) {
        try {
            if (taskHandler.request(fox, task) || taskHandler.requestByLabel(fox, task)) {
                logger.debug("Tasks request: {}", task);
                return true;
            }
        } catch (FoxException e) {
            logger.debug("Write exception: {}", e.getMessage());
        }
        return false;
    }

    private void executedTask(String task) {
        updateState(CHANNEL_TASK_STATE, new StringType(task));
        logger.debug("Task executed: {}", task);
    }

    private void noticeResult(String result) {
        triggerChannel(CHANNEL_RESULT_TRIGGER, result);
        String resultLabel = resultHandler.findResultLabel(result);
        triggerChannel(CHANNEL_RESULT_TRIGGER, resultLabel);
        logger.debug("Result notice: {}", result);
    }

    private void acquireResult(String result) {
        updateState(CHANNEL_RESULT_STATE, new StringType(result));
        logger.debug("Result acquired == {}", result);
    }

    private void write(String data) {
        scheduler.execute(() -> {
            if (requestTask(data)) {
                pingTime = Instant.now();
            }
        });
    }

    private String read() throws FoxException {
        return resultHandler.acquire(fox);
    }

    private boolean checkIfResultReceived(String data) {
        String checked = resultHandler.findResult(data);
        if (!checked.isEmpty()) {
            noticeResult(checked);
            acquireResult(checked);
            return true;
        }
        return false;
    }

    private boolean checkIfTaskReceived(String data) {
        String task = taskHandler.findTask(data);
        if (!task.isEmpty()) {
            executedTask(task);
            return true;
        }
        return false;
    }

    private void ping() {
        try {
            fox.getMessenger().ping();
        } catch (FoxException e) {
            pingError = true;
        }
    }

    private void closeConnection() {
        try {
            fox.getMessenger().close();
        } catch (FoxException e) {
        }
    }

    private boolean receivedDataTask(String data) {
        return data.isEmpty() || checkIfResultReceived(data) || checkIfTaskReceived(data);
    }

    private void pingCheckTask(int maxIntervalBetweenPingsSec) {
        Instant nowTime = Instant.now();
        if (Duration.between(pingTime, nowTime).toMillis() < maxIntervalBetweenPingsSec * 1000) {
            return;
        }
        pingTime = nowTime;
        ping();
    }

    private void requestConnectionTaskFinish() {
        finishConnectionTask = true;
    }

    private boolean checkIfFinishConnectionTaskRequest() {
        return finishConnectionTask;
    }

    private void markConnectionTaskFinished() {
        finishConnectionTask = false;
    }

    private void waitForConnectionTaskFinish(int maxTimeSec) {
        for (int i = 0; i < maxTimeSec; i++) {
            if (checkIfFinishConnectionTaskRequest()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            } else {
                return;
            }
        }
        markConnectionTaskFinished();
    }

    private @Nullable Boolean openConnection(String host, String password, StringBuilder status) {
        try {
            connect(host, password);
            if (checkIfFinishConnectionTaskRequest()) {
                return null;
            }
            pingTime = Instant.now();
            pingError = false;
            return true;
        } catch (FoxException e) {
            logger.debug("Open exception: {}", e.getMessage());
            status.append(e.getMessage());
            if (checkIfFinishConnectionTaskRequest()) {
                return null;
            }
        }
        return false;
    }

    private @Nullable Boolean readConnection() {
        try {
            String data = read();
            if (checkIfFinishConnectionTaskRequest()) {
                return null;
            }
            scheduler.execute(() -> {
                receivedDataTask(data);
            });
            if (pingError) {
                return false;
            }
            scheduler.execute(() -> {
                pingCheckTask(30);
            });
            return true;
        } catch (FoxException e) {
            logger.debug("Read exception: {}", e.getMessage());
            if (checkIfFinishConnectionTaskRequest()) {
                return null;
            }
        }
        return false;
    }

    private void connectionTask(String host, String password) {
        waitForConnectionTaskFinish(30);
        Boolean connected = false;
        for (;;) {
            if (connected) {
                connected = readConnection();
                if (connected == null) {
                    break;
                }
                if (!connected) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection broken");
                }
            } else {
                StringBuilder status = new StringBuilder();
                connected = openConnection(host, password, status);
                if (connected == null) {
                    break;
                }
                if (connected) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            String.format("Cannot open connection: %s", status.toString()));
                }
            }
        }
        closeConnection();
        markConnectionTaskFinished();
    }

    @Override
    public void initialize() {
        FoxConfiguration config = getConfigAs(FoxConfiguration.class);

        if (StringUtils.trimToNull(config.gateHost) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Net address is not set");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING, "Loading configuration...");
        loadTasksAndResults(StringUtils.trimToNull(config.functions));

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING, "Connecting to system...");
        scheduler.execute(() -> {
            connectionTask(StringUtils.trimToNull(config.gateHost), StringUtils.trimToNull(config.gatePassword));
        });
    }

    @Override
    public void dispose() {
        requestConnectionTaskFinish();
    }
}
