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
package org.openhab.binding.omatic.internal.api.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omatic.internal.OMaticBindingConstants;
import org.openhab.binding.omatic.internal.OMaticMachineThingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * The {@link OMaticMachine} is the base data model for any OMatic State Machine
 *
 * @author Joseph (Seaside) Hagberg - Initial contribution
 */
@NonNullByDefault
public class OMaticMachine {

    private static final String LOG_GOING_FROM_STATE_TO_STATE = "Going from state: {} to state: {} inputPower: {}";
    private static final String LOG_IDLE_TIME_HAS_EXPIRED_CONFIG_IDLE_TIME_NOW_TIME_IDLE_TIME = "Idle time has expired: {} configIdleTime: {}, nowTime: {}, idleTime: {}";
    private static final String PREFIX_DEBUG_LOG = "[{}] [{}] {}";
    private static final String PREFIX_INFO_LOG = "[{}] {}";
    private final DateTimeFormatter dateTimeFormatter;
    private static final String LOG_DISABLED_STATE_MACHINE = "Disabled state machine, power input ignored";
    private volatile boolean disable = false;
    private PropertyChangeSupport propertyChangeSupport;
    private final Logger logger = LoggerFactory.getLogger(OMaticMachine.class);

    private OMaticMachineThingConfig config;

    private @Nullable Double powerInput;
    private @Nullable Double energyInput;
    private Double power = 0D;
    private Double energy = 0D;
    private Double estimatedEnergy = 0D;
    private Double maxPower = 0D;
    private Double totalEnergy = 0D;
    private Double totalEnergyEstimated = 0D;

    private OMaticMachineState state = OMaticMachineState.NOT_STARTED;

    private int powerSampleCounter;
    private long totalPower;
    private Instant startedTimeStamp = Instant.EPOCH;
    private Instant completedTimeStamp = Instant.EPOCH;
    private @Nullable Instant lastPowerTimeStamp;
    private long totalTime;
    private double lastKnownEnergyValue = 0.0;
    private @Nullable ScheduledFuture<?> updateStateJob;
    protected final @Nullable ScheduledExecutorService scheduler;
    private Instant idleTimeStamp = Instant.EPOCH;
    private Instant nowTimeStamp = Instant.now();
    private @Nullable OMaticMachineState oldState;
    private double startEnergyValue;

    public OMaticMachine(@Nullable ScheduledExecutorService scheduler, OMaticMachineThingConfig config) {
        this.scheduler = scheduler;
        this.config = config;
        dateTimeFormatter = DateTimeFormatter.ofPattern(config.getDateFormat()).withZone(ZoneId.systemDefault());
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    private void logDebug(String message, Object... parameters) {
        final FormattingTuple logMessage = MessageFormatter.arrayFormat(message, parameters);
        logger.debug(PREFIX_DEBUG_LOG, config.getName(), hashCode(), logMessage.getMessage());
    }

    private void logInfo(String message, Object... parameters) {
        final FormattingTuple logMessage = MessageFormatter.arrayFormat(message, parameters);
        logger.info(PREFIX_INFO_LOG, config.getName(), logMessage.getMessage());
    }

    public synchronized void powerInput(double inputPower) {
        if (disable) {
            logDebug(LOG_DISABLED_STATE_MACHINE);
            return;
        }
        cancelTimer();
        nowTimeStamp = Instant.now();
        power = getLastKnownPowerValue(inputPower);
        oldState = state;

        // If state Machine is to be started
        if (machineShouldBeStarted(power)) {
            startStateMachine();
        } else if (isRunning() && isIdle(power)) { // Statemachine is runnning but idle
            setStateIdle();
            if (idleTimeHasExpired()) {
                stateMachineCompleted();
            }
        } else if (isRunning() && isActive(power)) {
            setStateActive();
            if (runningTimeHasExpired(nowTimeStamp)) {
                stateMachineCompleted();
            }
        }
        if (logger.isDebugEnabled() && oldState != state) {
            logDebug(LOG_GOING_FROM_STATE_TO_STATE, oldState, state, inputPower);
        }
        if (isRunning()) {
            updatePowerValues();
            startTimer();
        }
        propertyChangeSupport.firePropertyChange(OMaticBindingConstants.PROPERTY_POWER_INPUT, oldState, state);
    }

    public void setLatKnownEnergyValue(double lastKnownEnergyValue) {
        this.lastKnownEnergyValue = lastKnownEnergyValue;
    }

    public void energyInput(double inputEnergy) {
        if (disable) {
            logDebug("Disabled state machine");
            return;
        }
        final double oldValue = lastKnownEnergyValue;
        lastKnownEnergyValue = inputEnergy;
        propertyChangeSupport.firePropertyChange(OMaticBindingConstants.PROPERTY_LAST_KNOWN_ENERGY_VALUE, oldValue,
                lastKnownEnergyValue);
    }

    private void updatePowerValues() {
        powerSampleCounter++;
        totalPower += getPower();
        setMaxPower(getPower());
    }

    /**
     * If the power value is not set, i.e triggered via timer, then use last known value
     *
     * @param inputPower
     * @return
     */
    private double getLastKnownPowerValue(double inputPower) {
        power = inputPower == -1 ? getPower() : inputPower;
        return power;
    }

    public void setMaxPower(double power) {
        if (power > maxPower.doubleValue()) {
            logDebug("Set maxPower: {}", power);
            double oldMaxPower = maxPower;
            maxPower = power;
            propertyChangeSupport.firePropertyChange(OMaticBindingConstants.PROPERTY_MAX_POWER, oldMaxPower, maxPower);
        }
    }

    private synchronized void setStateActive() {
        state = OMaticMachineState.ACTIVE;
        completedTimeStamp = Instant.EPOCH;
        idleTimeStamp = Instant.EPOCH;
    }

    private synchronized void setStateIdle() {
        state = OMaticMachineState.IDLE;
        if (idleTimeStamp == Instant.EPOCH) { // Idle for first time
            idleTimeStamp = nowTimeStamp;
        }
    }

    private boolean runningTimeHasExpired(Instant nowTimeStamp) {
        final Duration duration = Duration.between(startedTimeStamp, nowTimeStamp);
        return duration.getSeconds() >= config.getMaxRunningTime();
    }

    private synchronized boolean machineShouldBeStarted(double power) {
        return !isRunning() && isActive(power);
    }

    @SuppressWarnings("null")
    private void cancelTimer() {
        if (updateStateJob != null) {
            updateStateJob.cancel(true);
            updateStateJob = null;
        }
    }

    private void stateMachineCompleted() {
        cancelTimer();
        completedTimeStamp = nowTimeStamp;
        OMaticMachineState oldState = state;
        double runningTime = getRunningTime();
        energy = (lastKnownEnergyValue - startEnergyValue);
        estimatedEnergy = ((double) totalPower) / powerSampleCounter;
        estimatedEnergy = (estimatedEnergy / 1000.0) * (runningTime / (60 * 60));
        totalEnergyEstimated += estimatedEnergy;
        totalEnergy += energy;
        totalTime += runningTime;

        if (logger.isDebugEnabled()) {
            logDebug("Completed machine: {}", toString());
        }
        state = OMaticMachineState.COMPLETE;
        propertyChangeSupport.firePropertyChange(OMaticBindingConstants.PROPERTY_COMPLETED, oldState, state);
    }

    @SuppressWarnings("null")
    private boolean idleTimeHasExpired() {
        Duration duration = Duration.between(idleTimeStamp, nowTimeStamp);
        logDebug(LOG_IDLE_TIME_HAS_EXPIRED_CONFIG_IDLE_TIME_NOW_TIME_IDLE_TIME,
                config.getIdleTime() <= duration.getSeconds(), config.getIdleTime(), nowTimeStamp.getEpochSecond(),
                idleTimeStamp.getEpochSecond());
        return config.getIdleTime() <= duration.getSeconds();
    }

    @SuppressWarnings("null")
    private void startTimer() {
        if (scheduler == null) {
            logger.error("Can't start timer, no scheduler");
            return;
        }
        if (updateStateJob == null) {
            logDebug("Starting timer: {}", config.getTimerDelay());
            updateStateJob = scheduler.scheduleWithFixedDelay(this::run, config.getTimerDelay(), config.getTimerDelay(),
                    TimeUnit.SECONDS);
        } else {
            logDebug("Not starting timer");
        }
    }

    private boolean isIdle(double power) {
        logDebug("isIdle: {} configThreshold: {}", power <= config.getActiveThreshold(), config.getActiveThreshold());
        return power < config.getActiveThreshold().doubleValue();
    }

    private boolean isActive(double power) {
        return power >= config.getActiveThreshold().doubleValue();
    }

    private void startStateMachine() {
        setStateActive();
        totalPower = 0;
        powerSampleCounter = 0;
        startEnergyValue = lastKnownEnergyValue;
        startedTimeStamp = nowTimeStamp;
        logInfo("Starting state Machine: {}", getStartedTimeStr());
    }

    public synchronized boolean isRunning() {
        return state == OMaticMachineState.ACTIVE || state == OMaticMachineState.IDLE;
    }

    public synchronized void run() {
        powerInput(-1.0);
    }

    public long getRunningTime() {
        if (startedTimeStamp == Instant.EPOCH) {
            return -1;
        }
        return isRunning() ? Duration.between(startedTimeStamp, nowTimeStamp).getSeconds()
                : Duration.between(startedTimeStamp, completedTimeStamp).getSeconds();
    }

    public @Nullable String getCompletedTimeStr() {
        return completedTimeStamp == Instant.EPOCH ? null : dateTimeFormatter.format(completedTimeStamp);
    }

    public @Nullable String getStartedTimeStr() {
        return startedTimeStamp == Instant.EPOCH ? null : dateTimeFormatter.format(startedTimeStamp);
    }

    public String getRunningTimeString() {
        return OMaticMachineUtil.convertSecondsToTimeString(getRunningTime());
    }

    public String getTotalTimeString() {
        return OMaticMachineUtil.convertSecondsToTimeString(getTotalTime());
    }

    public synchronized void terminate() {
        cancelTimer();
    }

    public synchronized void reset() {
        lastKnownEnergyValue = 0;
        estimatedEnergy = 0D;
        totalEnergy = 0D;
        totalEnergyEstimated = 0D;
        totalPower = 0;
        totalTime = 0;
        power = 0D;
        energy = 0D;
        maxPower = 0D;
        powerSampleCounter = 0;
        totalPower = 0;
        startEnergyValue = 0;
    }

    public synchronized void disable(boolean disable) {
        cancelTimer();
        this.disable = disable;
    }

    public double getCost() {
        return energy * config.getCost();
    }

    public double getTotalCost() {
        return totalEnergy * config.getCost();
    }

    public double getEstimatedCost() {
        return estimatedEnergy * config.getCost();
    }

    public double getEstimatedTotalCost() {
        return totalEnergyEstimated * config.getCost();
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.removePropertyChangeListener(pcl);
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public Double getEstimatedEnergy() {
        return estimatedEnergy;
    }

    public Double getTotalEnergyEstimated() {
        return totalEnergyEstimated;
    }

    public void setTotalEnergyEstimated(Double totalEnergyEstimated) {
        this.totalEnergyEstimated = totalEnergyEstimated;
    }

    public double getLastKnownEnergyValue() {
        return lastKnownEnergyValue;
    }

    public OMaticMachineThingConfig getConfig() {
        return config;
    }

    public OMaticMachineState getState() {
        return state;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public Double getMaxPower() {
        return maxPower;
    }

    public Double getEnergy() {
        return energy;
    }

    public double getPower() {
        return power.doubleValue();
    }

    public Double getTotalEnergy() {
        return totalEnergy;
    }

    public void setTotalEnergy(Double totalEnergy) {
        this.totalEnergy = totalEnergy;
    }

    @Override
    public String toString() {
        return "OMaticMachine [dateTimeFormatter=" + dateTimeFormatter + ", disable=" + disable
                + ", propertyChangeSupport=" + propertyChangeSupport + ", logger=" + logger + ", config=" + config
                + ", powerInput=" + powerInput + ", energyInput=" + energyInput + ", power=" + power + ", energy="
                + energy + ", estimatedEnergy=" + estimatedEnergy + ", maxPower=" + maxPower + ", totalEnergy="
                + totalEnergy + ", totalEnergyEstimated=" + totalEnergyEstimated + ", state=" + state
                + ", powerSampleCounter=" + powerSampleCounter + ", totalPower=" + totalPower + ", startedTimeStamp="
                + startedTimeStamp + ", completedTimeStamp=" + completedTimeStamp + ", lastPowerTimeStamp="
                + lastPowerTimeStamp + ", totalTime=" + totalTime + ", lastKnownEnergyValue=" + lastKnownEnergyValue
                + ", updateStateJob=" + updateStateJob + ", scheduler=" + scheduler + ", idleTimeStamp=" + idleTimeStamp
                + ", nowTimeStamp=" + nowTimeStamp + ", oldState=" + oldState + ", startEnergyValue=" + startEnergyValue
                + "]";
    }
}
