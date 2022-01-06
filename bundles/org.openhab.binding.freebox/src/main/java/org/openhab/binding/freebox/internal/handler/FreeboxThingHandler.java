/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.handler;

import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.FreeboxAirMediaReceiver;
import org.openhab.binding.freebox.internal.api.model.FreeboxCallEntry;
import org.openhab.binding.freebox.internal.api.model.FreeboxLanHost;
import org.openhab.binding.freebox.internal.api.model.FreeboxLanHostL3Connectivity;
import org.openhab.binding.freebox.internal.api.model.FreeboxPhoneStatus;
import org.openhab.binding.freebox.internal.config.FreeboxAirPlayDeviceConfiguration;
import org.openhab.binding.freebox.internal.config.FreeboxNetDeviceConfiguration;
import org.openhab.binding.freebox.internal.config.FreeboxNetInterfaceConfiguration;
import org.openhab.binding.freebox.internal.config.FreeboxPhoneConfiguration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxThingHandler} is responsible for handling everything associated to
 * any Freebox thing types except the bridge thing type.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Laurent Garnier - use new internal API manager
 */
public class FreeboxThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FreeboxThingHandler.class);

    private final TimeZoneProvider timeZoneProvider;

    private ScheduledFuture<?> phoneJob;
    private ScheduledFuture<?> callsJob;
    private FreeboxHandler bridgeHandler;
    private Calendar lastPhoneCheck;
    private String netAddress;
    private String airPlayName;
    private String airPlayPassword;

    public FreeboxThingHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        if (getThing().getStatus() == ThingStatus.UNKNOWN || (getThing().getStatus() == ThingStatus.OFFLINE
                && (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE
                        || getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_UNINITIALIZED
                        || getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR))) {
            return;
        }
        if (bridgeHandler == null) {
            return;
        }
        switch (channelUID.getId()) {
            case PLAYURL:
                playMedia(channelUID, command);
                break;
            case STOP:
                stopMedia(channelUID, command);
                break;
            default:
                logger.debug("Thing {}: unexpected command {} from channel {}", getThing().getUID(), command,
                        channelUID.getId());
                break;
        }
    }

    @Override
    public void initialize() {
        logger.debug("initializing handler for thing {}", getThing().getUID());
        Bridge bridge = getBridge();
        if (bridge == null) {
            initializeThing(null, null);
        } else {
            initializeThing(bridge.getHandler(), bridge.getStatus());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        Bridge bridge = getBridge();
        if (bridge == null) {
            initializeThing(null, bridgeStatusInfo.getStatus());
        } else {
            initializeThing(bridge.getHandler(), bridgeStatusInfo.getStatus());
        }
    }

    private void initializeThing(ThingHandler bridgeHandler, ThingStatus bridgeStatus) {
        if (bridgeHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                this.bridgeHandler = (FreeboxHandler) bridgeHandler;

                if (getThing().getThingTypeUID().equals(FREEBOX_THING_TYPE_PHONE)) {
                    updateStatus(ThingStatus.ONLINE);
                    lastPhoneCheck = Calendar.getInstance();
                    if (phoneJob == null || phoneJob.isCancelled()) {
                        long pollingInterval = getConfigAs(FreeboxPhoneConfiguration.class).refreshPhoneInterval;
                        if (pollingInterval > 0) {
                            logger.debug("Scheduling phone state job every {} seconds...", pollingInterval);
                            phoneJob = scheduler.scheduleWithFixedDelay(() -> {
                                try {
                                    pollPhoneState();
                                } catch (Exception e) {
                                    logger.debug("Phone state job failed: {}", e.getMessage(), e);
                                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                            e.getMessage());
                                }
                            }, 1, pollingInterval, TimeUnit.SECONDS);
                        }
                    }
                    if (callsJob == null || callsJob.isCancelled()) {
                        long pollingInterval = getConfigAs(FreeboxPhoneConfiguration.class).refreshPhoneCallsInterval;
                        if (pollingInterval > 0) {
                            logger.debug("Scheduling phone calls job every {} seconds...", pollingInterval);
                            callsJob = scheduler.scheduleWithFixedDelay(() -> {
                                try {
                                    pollPhoneCalls();
                                } catch (Exception e) {
                                    logger.debug("Phone calls job failed: {}", e.getMessage(), e);
                                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                            e.getMessage());
                                }
                            }, 1, pollingInterval, TimeUnit.SECONDS);
                        }
                    }
                } else if (getThing().getThingTypeUID().equals(FREEBOX_THING_TYPE_NET_DEVICE)) {
                    updateStatus(ThingStatus.ONLINE);
                    netAddress = getConfigAs(FreeboxNetDeviceConfiguration.class).macAddress;
                    netAddress = (netAddress == null) ? "" : netAddress;
                } else if (getThing().getThingTypeUID().equals(FREEBOX_THING_TYPE_NET_INTERFACE)) {
                    updateStatus(ThingStatus.ONLINE);
                    netAddress = getConfigAs(FreeboxNetInterfaceConfiguration.class).ipAddress;
                    netAddress = (netAddress == null) ? "" : netAddress;
                } else if (getThing().getThingTypeUID().equals(FREEBOX_THING_TYPE_AIRPLAY)) {
                    updateStatus(ThingStatus.UNKNOWN);
                    airPlayName = getConfigAs(FreeboxAirPlayDeviceConfiguration.class).name;
                    airPlayName = (airPlayName == null) ? "" : airPlayName;
                    airPlayPassword = getConfigAs(FreeboxAirPlayDeviceConfiguration.class).password;
                    airPlayPassword = (airPlayPassword == null) ? "" : airPlayPassword;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    private void pollPhoneState() {
        logger.debug("Polling phone state...");
        try {
            FreeboxPhoneStatus phoneStatus = bridgeHandler.getApiManager().getPhoneStatus();
            updateGroupChannelSwitchState(STATE, ONHOOK, phoneStatus.isOnHook());
            updateGroupChannelSwitchState(STATE, RINGING, phoneStatus.isRinging());
            updateStatus(ThingStatus.ONLINE);
        } catch (FreeboxException e) {
            if (e.isMissingRights()) {
                logger.debug("Phone state job: missing right {}", e.getResponse().getMissingRight());
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Phone state job failed: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private void pollPhoneCalls() {
        logger.debug("Polling phone calls...");
        try {
            List<FreeboxCallEntry> callEntries = bridgeHandler.getApiManager().getCallEntries();
            if (callEntries != null) {
                PhoneCallComparator comparator = new PhoneCallComparator();
                Collections.sort(callEntries, comparator);

                for (FreeboxCallEntry call : callEntries) {
                    Calendar callEndTime = call.getTimeStamp();
                    callEndTime.add(Calendar.SECOND, call.getDuration());
                    if ((call.getDuration() > 0) && callEndTime.after(lastPhoneCheck)) {
                        updateCall(call, ANY);

                        if (call.isAccepted()) {
                            updateCall(call, ACCEPTED);
                        } else if (call.isMissed()) {
                            updateCall(call, MISSED);
                        } else if (call.isOutGoing()) {
                            updateCall(call, OUTGOING);
                        }

                        lastPhoneCheck = callEndTime;
                    }
                }
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (FreeboxException e) {
            if (e.isMissingRights()) {
                logger.debug("Phone calls job: missing right {}", e.getResponse().getMissingRight());
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Phone calls job failed: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing handler for thing {}", getThing().getUID());
        if (phoneJob != null && !phoneJob.isCancelled()) {
            phoneJob.cancel(true);
            phoneJob = null;
        }
        if (callsJob != null && !callsJob.isCancelled()) {
            callsJob.cancel(true);
            callsJob = null;
        }
        super.dispose();
    }

    private void updateCall(FreeboxCallEntry call, String channelGroup) {
        if (channelGroup != null) {
            updateGroupChannelStringState(channelGroup, CALLNUMBER, call.getNumber());
            updateGroupChannelDecimalState(channelGroup, CALLDURATION, call.getDuration());
            ZonedDateTime zoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(call.getTimeStamp().getTimeInMillis()),
                    timeZoneProvider.getTimeZone());
            updateGroupChannelDateTimeState(channelGroup, CALLTIMESTAMP, zoned);
            updateGroupChannelStringState(channelGroup, CALLNAME, call.getName());
            if (channelGroup.equals(ANY)) {
                updateGroupChannelStringState(channelGroup, CALLSTATUS, call.getType());
            }
        }
    }

    public void updateNetInfo(List<FreeboxLanHost> hosts) {
        if (!getThing().getThingTypeUID().equals(FREEBOX_THING_TYPE_NET_DEVICE)
                && !getThing().getThingTypeUID().equals(FREEBOX_THING_TYPE_NET_INTERFACE)) {
            return;
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        boolean found = false;
        boolean reachable = false;
        String vendor = null;
        if (hosts != null) {
            for (FreeboxLanHost host : hosts) {
                if (getThing().getThingTypeUID().equals(FREEBOX_THING_TYPE_NET_DEVICE)
                        && netAddress.equals(host.getMAC())) {
                    found = true;
                    reachable = host.isReachable();
                    vendor = host.getVendorName();
                    break;
                }
                if (host.getL3Connectivities() != null) {
                    for (FreeboxLanHostL3Connectivity l3 : host.getL3Connectivities()) {
                        if (getThing().getThingTypeUID().equals(FREEBOX_THING_TYPE_NET_INTERFACE)
                                && netAddress.equals(l3.getAddr())) {
                            found = true;
                            if (l3.isReachable()) {
                                reachable = true;
                                vendor = host.getVendorName();
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (found) {
            updateState(new ChannelUID(getThing().getUID(), REACHABLE), reachable ? OnOffType.ON : OnOffType.OFF);
        }
        if (vendor != null && !vendor.isEmpty()) {
            updateProperty(Thing.PROPERTY_VENDOR, vendor);
        }
    }

    public void updateAirPlayDevice(List<FreeboxAirMediaReceiver> receivers) {
        if (!getThing().getThingTypeUID().equals(FREEBOX_THING_TYPE_AIRPLAY)) {
            return;
        }
        if (airPlayName == null) {
            return;
        }

        // The Freebox API allows pushing media only to receivers with photo or video capabilities
        // but not to receivers with only audio capability
        boolean found = false;
        boolean usable = false;
        if (receivers != null) {
            for (FreeboxAirMediaReceiver receiver : receivers) {
                if (airPlayName.equals(receiver.getName())) {
                    found = true;
                    usable = receiver.isVideoCapable();
                    break;
                }
            }
        }
        if (!found) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "AirPlay device not found");
        } else if (!usable) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "AirPlay device without video capability");
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public void playMedia(String url) throws FreeboxException {
        if (bridgeHandler != null && url != null) {
            stopMedia();
            bridgeHandler.getApiManager().playMedia(url, airPlayName, airPlayPassword);
        }
    }

    private void playMedia(ChannelUID channelUID, Command command) {
        if (command instanceof StringType) {
            try {
                playMedia(command.toString());
            } catch (FreeboxException e) {
                bridgeHandler.logCommandException(e, channelUID, command);
            }
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId());
        }
    }

    public void stopMedia() throws FreeboxException {
        if (bridgeHandler != null) {
            bridgeHandler.getApiManager().stopMedia(airPlayName, airPlayPassword);
        }
    }

    private void stopMedia(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            try {
                stopMedia();
            } catch (FreeboxException e) {
                bridgeHandler.logCommandException(e, channelUID, command);
            }
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId());
        }
    }

    private void updateGroupChannelSwitchState(String group, String channel, boolean state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), state ? OnOffType.ON : OnOffType.OFF);
    }

    private void updateGroupChannelStringState(String group, String channel, String state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), new StringType(state));
    }

    private void updateGroupChannelDecimalState(String group, String channel, int state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), new DecimalType(state));
    }

    private void updateGroupChannelDateTimeState(String group, String channel, ZonedDateTime zonedDateTime) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), new DateTimeType(zonedDateTime));
    }

    /**
     * A comparator of phone calls by ascending end date and time
     */
    private class PhoneCallComparator implements Comparator<FreeboxCallEntry> {

        @Override
        public int compare(FreeboxCallEntry call1, FreeboxCallEntry call2) {
            int result = 0;
            Calendar callEndTime1 = call1.getTimeStamp();
            callEndTime1.add(Calendar.SECOND, call1.getDuration());
            Calendar callEndTime2 = call2.getTimeStamp();
            callEndTime2.add(Calendar.SECOND, call2.getDuration());
            if (callEndTime1.before(callEndTime2)) {
                result = -1;
            } else if (callEndTime1.after(callEndTime2)) {
                result = 1;
            }
            return result;
        }
    }
}
