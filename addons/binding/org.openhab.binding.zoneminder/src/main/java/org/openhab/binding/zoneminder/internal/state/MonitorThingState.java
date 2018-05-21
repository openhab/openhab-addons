/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.state;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.activation.UnsupportedDataTypeException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.eskildsen.zoneminder.api.telnet.ZoneMinderTriggerEvent;
import name.eskildsen.zoneminder.common.ZoneMinderMonitorFunctionEnum;
import name.eskildsen.zoneminder.common.ZoneMinderMonitorStatusEnum;
import name.eskildsen.zoneminder.data.IMonitorDataGeneral;
import name.eskildsen.zoneminder.data.IZoneMinderEventData;

/**
 * The {@link MonitorThingState} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class MonitorThingState extends GenericThingState {

    private Logger logger = LoggerFactory.getLogger(MonitorThingState.class);

    private boolean isDirty;
    private String logIdentifier = "";

    private IZoneMinderEventData activeEvent = null;
    private ZoneMinderTriggerEvent curTriggerEvent = null;

    /*
     * Used in recalculate
     */
    AtomicBoolean recordingFunction = new AtomicBoolean(false);
    AtomicBoolean recordingDetailedState = new AtomicBoolean(false);
    AtomicBoolean alarmedFunction = new AtomicBoolean(false);
    AtomicBoolean alarmedDetailedState = new AtomicBoolean(false);

    // Monitor properties

    // Video Url
    private State channelVideoUrl;

    boolean bRecalculating = false;

    /*
     * public MonitorThingState(String id, ChannelStateChangeSubscriber subscriber) {
     * super(subscriber);
     * logIdentifier = id;
     * initialize();
     * }
     */
    protected void initialize() {
        isDirty = true;
    }

    public MonitorThingState(ChannelStateChangeSubscriber subscriber) {
        super(subscriber);

    }

    @Override
    public GenericChannelState createChannelSubscription(ChannelUID channelUID) {
        GenericChannelState channelState = null;
        try {
            switch (channelUID.getId()) {
                case ZoneMinderConstants.CHANNEL_MONITOR_DETAILED_STATUS:
                case ZoneMinderConstants.CHANNEL_MONITOR_EVENT_CAUSE:
                case ZoneMinderConstants.CHANNEL_MONITOR_FUNCTION:
                    channelState = createSubscriptionStringType(channelUID);
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_STILL_IMAGE:
                    channelState = createSubscriptionRawType(channelUID);
                    break;
                default:
                    channelState = createSubscriptionOnOffType(channelUID);
                    break;
            }

        } catch (Exception ex) {
            logger.error("{}: context='subscribe' - Exception occurred when subscribing to channel '{}'", "<UNKNOWN>",
                    channelUID.getId());
        }
        return channelState;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean isAlarmed() {
        State stateEnabled = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_EVENT_STATE).getState();
        return ((alarmedDetailedState.get() || (stateEnabled == OnOffType.ON)) ? true : false);

    }

    private boolean isEnabled() {
        State stateEnabled = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_ENABLED).getState();
        if (stateEnabled == OnOffType.ON) {
            return true;
        }
        return false;
    }

    public ZoneMinderMonitorFunctionEnum getMonitorFunction() {
        State stateFunction = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_FUNCTION).getState();
        return ZoneMinderMonitorFunctionEnum.getEnum(stateFunction.toString());
    }

    public String getMonitorEventCause() {
        State stateEventCause = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_EVENT_CAUSE).getState();
        return stateEventCause.toString();
    }

    public boolean getMonitorEventMotion() {
        State stateEventMotion = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_MOTION_EVENT).getState();
        return false;

    }

    public ZoneMinderMonitorStatusEnum getMonitorDetailedStatus() {
        State stateStatus = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_DETAILED_STATUS).getState();
        ZoneMinderMonitorStatusEnum result = ZoneMinderMonitorStatusEnum.UNKNOWN;

        if (stateStatus == UnDefType.NULL || stateStatus == UnDefType.UNDEF) {
            return ZoneMinderMonitorStatusEnum.UNKNOWN;
        }

        try {
            result = ZoneMinderMonitorStatusEnum.getEnumFromName(stateStatus.toString());
        } catch (Exception ex) {
            logger.error(
                    "{}: context='getMonitorDetailedStatus' Exception occurred when calling getMonitorDetailedStatus",
                    logIdentifier, ex);

        }
        return result;
    }

    public void setMonitorTriggerEvent(ZoneMinderTriggerEvent event) {
        isDirty = true;
        curTriggerEvent = event;
    }

    public void setMonitorFunction(ZoneMinderMonitorFunctionEnum monitorFunction) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_FUNCTION);
        if (gcs != null) {
            try {
                gcs.setState(monitorFunction.toString());
            } catch (UnsupportedDataTypeException e) {
                logger.error("{}: context='setMonitorFunction' Exception occurred when updating capture-daemon channel",
                        logIdentifier, e);
            }
        }
    }

    public void setMonitorEventCause(State state) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_EVENT_CAUSE);
        if (gcs != null) {
            try {
                gcs.setState(state);
            } catch (UnsupportedDataTypeException e) {
                logger.error(
                        "{}: context='setMonitorEventCause' Exception occurred when updating capture-daemon channel",
                        logIdentifier, e);

            }
        }

    }

    public void setMonitorEventMotion(State state) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_MOTION_EVENT);

        if (gcs != null) {
            try {
                gcs.setState(state);
            } catch (UnsupportedDataTypeException e) {
                logger.error("{}: context='setMonitorEnabled' Exception occurred when updating capture-daemon channel",
                        logIdentifier, e);
            }
        }

    }

    public void setMonitorGeneralData(IMonitorDataGeneral monitorData) {
        if (monitorData != null) {
            setMonitorFunction(monitorData.getFunction());
            setMonitorEnabled(monitorData.getEnabled());
        }
    }

    public void setMonitorEnabled(boolean state) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_ENABLED);

        try {
            if (gcs != null) {
                gcs.setState(state);
            }
        } catch (UnsupportedDataTypeException e) {
            logger.error("{}: context='setMonitorEnabled' Exception occurred when calling setMonitorEnabled",
                    logIdentifier, e);

        } catch (Exception ex) {
            logger.error(
                    "{}: context='setMonitorEnabled' tag='exception' Exception occurredwhen calling setMonitorEnabled",
                    logIdentifier, ex);
        }

    }

    public void setMonitorRecording(boolean state) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_RECORD_STATE);
        if (gcs != null) {
            try {
                gcs.setState(state);
            } catch (UnsupportedDataTypeException e) {
                logger.error("{}: context='setMonitorRecortding' Exception occurred", logIdentifier, e);
            }
        }

    }

    public void setMonitorEventState(boolean state) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_EVENT_STATE);
        if (gcs != null) {
            try {
                gcs.setState(state);
            } catch (UnsupportedDataTypeException e) {
                logger.error("{}: context='setMonitorEventState' Exception occurred", logIdentifier, e);
            }
        }
    }

    public void setMonitorForceAlarmInternal(boolean state) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_FORCE_ALARM);
        if (gcs != null) {
            try {
                gcs.setState(state, true);
            } catch (UnsupportedDataTypeException e) {
                logger.error("{}: context='setMonitorForceAlarmInternal' Exception occurred", logIdentifier, e);

            }
        }

    }

    public void setMonitorForceAlarmExternal(boolean state) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_FORCE_ALARM);
        if (gcs != null) {
            try {
                gcs.setState(state);
            } catch (UnsupportedDataTypeException e) {
                logger.error("{}: context='setMonitorForceAlarmExternal' Exception occurred", logIdentifier, e);

            }
        }

    }

    public void setMonitorEventData(IZoneMinderEventData eventData) {
        // If it is set to null ignore since set back to idle from alarm is handled internally
        if (eventData == null) {
            return;
        }

        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_EVENT_CAUSE);
        if (gcs != null) {
            try {
                if (!activeEvent.equals(eventData)) {
                    activeEvent = eventData;
                    gcs.setState(eventData.getCause());
                }
            } catch (UnsupportedDataTypeException e) {
                logger.error("{}: context='setMonitorEventData' Exception occurred", logIdentifier, e);
            }
        }
    }

    public void setMonitorDetailedStatus(ZoneMinderMonitorStatusEnum status) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_DETAILED_STATUS);
        if (gcs != null) {
            try {
                gcs.setState(status.toString());

            } catch (UnsupportedDataTypeException e) {
                logger.error("{}: context='setMonitorDetailedStatus' Exception occurred", logIdentifier, e);

            }
        }
    }

    public void setMonitorCaptureDaemonStatus(State state) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_CAPTURE_DAEMON_STATE);
        if (gcs != null) {
            try {
                gcs.setState(state);
            } catch (UnsupportedDataTypeException e) {
                logger.error("{}: context='setMonitorStillImage' Exception occurred", logIdentifier, e);

            }
        }
    }

    public void setMonitorAnalysisDaemonStatus(State state) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_ANALYSIS_DAEMON_STATE);
        if (gcs != null) {
            try {
                gcs.setState(state);
            } catch (UnsupportedDataTypeException e) {
                logger.error("{}: context='setMonitorAnalysisDaemonStatus' Exception occurred", logIdentifier, e);

            }
        }
    }

    public void setMonitorFrameDaemonStatus(State state) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_FRAME_DAEMON_STATE);
        if (gcs != null) {
            try {
                gcs.setState(state);
            } catch (UnsupportedDataTypeException e) {
                logger.error("{}: context='setMonitorFrameDaemonStatus' Exception occurred", logIdentifier, e);

            }
        }
    }

    public void setMonitorFrameDaemonStatus(String status) {
        getStringAsStringState(status);
    }

    public void setMonitorStillImage(ByteArrayOutputStream baos) {
        GenericChannelState gcs = getChannelStateHandler(ZoneMinderConstants.CHANNEL_MONITOR_STILL_IMAGE);
        if (gcs != null) {
            try {
                if (baos != null) {
                    gcs.setState(baos);
                } else {
                    gcs.setState(UnDefType.UNDEF);
                }
            } catch (UnsupportedDataTypeException e) {
                logger.error("{}: context='setMonitorStillImage' Exception occurred", logIdentifier, e);

            }
        }

    }

    public void setMonitorVideoUrl(String url) {
        channelVideoUrl = new StringType(url);
    }

    public State getVideoUrl() {
        return channelVideoUrl;
    }

    @Override
    public void onChannelChanged(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case ZoneMinderConstants.CHANNEL_MONITOR_ENABLED:
            case ZoneMinderConstants.CHANNEL_MONITOR_FUNCTION:
            case ZoneMinderConstants.CHANNEL_MONITOR_DETAILED_STATUS:
            case ZoneMinderConstants.CHANNEL_MONITOR_FORCE_ALARM:
                recalculate();
                break;
        }

    }

    @Override
    protected void recalculate() {
        try {
            ZoneMinderMonitorFunctionEnum monitorFunction = getMonitorFunction();
            ZoneMinderMonitorStatusEnum monitorStatus = getMonitorDetailedStatus();

            // Calculate based on state of Function
            switch (monitorFunction) {
                case NONE:
                case MONITOR:
                    alarmedFunction.set(false);
                    recordingFunction.set(false);
                    break;

                case MODECT:
                    alarmedFunction.set(true);
                    recordingFunction.set(true);
                    break;
                case RECORD:
                    alarmedFunction.set(false);
                    recordingFunction.set(true);
                    break;
                case MOCORD:
                    alarmedFunction.set(true);
                    recordingFunction.set(true);
                    break;
                case NODECT:
                    alarmedFunction.set(false);
                    recordingFunction.set(true);
                    break;
                default:
                    recordingFunction.set((activeEvent != null) ? true : false);
            }
            logger.debug(
                    "{}: Recalculate channel states based on Function: Function='{}' -> alarmState='{}', recordingState='{}'",
                    logIdentifier, monitorFunction, alarmedFunction, recordingFunction);

            // Calculated based on detailed Monitor Status
            switch (monitorStatus) {
                case IDLE:
                    alarmedDetailedState.set(false);
                    recordingDetailedState.set(false);
                    break;

                case PRE_ALARM:
                case ALARM:
                case ALERT:
                    alarmedDetailedState.set(true);
                    recordingDetailedState.set(true);
                    break;

                case RECORDING:
                    alarmedDetailedState.set(false);
                    recordingDetailedState.set(true);
                    break;
                case UNKNOWN:
                default:
                    alarmedDetailedState.set(false);
                    recordingDetailedState.set(false);
                    break;

            }
            logger.debug(
                    "{}: Recalculate channel states based on Detailed State: DetailedState='{}' -> alarmState='{}', recordingState='{}'",
                    logIdentifier, monitorStatus.name(), alarmedDetailedState, recordingDetailedState);

            if (monitorStatus == ZoneMinderMonitorStatusEnum.IDLE && !alarmedDetailedState.get()
                    && activeEvent != null) {
                activeEvent = null;
            }

            updateEventChannels();

            // Now we can conclude on the Alarmed and Recording channel state
            setMonitorRecording((recordingFunction.get() && recordingDetailedState.get() && isEnabled()));
            setMonitorEventState((alarmedFunction.get() && alarmedDetailedState.get() && isEnabled()));

            switch (getMonitorDetailedStatus()) {
                case UNKNOWN:
                    setMonitorEventCause(UnDefType.UNDEF);
                    setMonitorEventMotion(UnDefType.UNDEF);
                    break;

                case PRE_ALARM:
                case ALARM:
                case ALERT:
                    if (activeEvent != null) {
                        setMonitorEventCause(new StringType(activeEvent.getCause()));
                        setMonitorEventMotion(
                                activeEvent.getCause().equalsIgnoreCase("motion") ? OnOffType.ON : OnOffType.OFF);
                    } else {
                        setMonitorEventMotion(OnOffType.OFF);
                    }
                    break;

                case IDLE:
                case RECORDING:
                default:
                    setMonitorEventCause(new StringType(""));
                    setMonitorEventMotion(OnOffType.OFF);
                    break;
            }
        } catch (Exception ex) {
            logger.error("{}: context='recalculate' Exception occurred", logIdentifier, ex);
        } finally {
            isDirty = false;
        }
    }

    protected State getChannelByteArrayAsRawType(ByteArrayOutputStream image) {
        State state = UnDefType.UNDEF;
        try {
            if (image != null) {
                state = new RawType(image.toByteArray(), "image/jpeg");
            }

        } catch (Exception ex) {
            logger.error("{}: Exception occurred in 'getChannelByteArrayAsRawType()'", logIdentifier, ex);
        }

        return state;
    }

    private void updateEventChannels() {
        switch (getMonitorDetailedStatus()) {
            case UNKNOWN:
                setMonitorEventCause(UnDefType.UNDEF);
                setMonitorEventMotion(UnDefType.UNDEF);
                break;

            case PRE_ALARM:
            case ALARM:
            case ALERT:
                if (activeEvent != null) {
                    setMonitorEventCause(new StringType(activeEvent.getCause()));
                    setMonitorEventMotion(
                            activeEvent.getCause().toLowerCase().contains("motion") ? OnOffType.ON : OnOffType.OFF);
                }
                break;

            case IDLE:
            case RECORDING:
            default:
                setMonitorEventCause(new StringType(""));
                setMonitorEventMotion(OnOffType.OFF);
                break;
        }
    }
}