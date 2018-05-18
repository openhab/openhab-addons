/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.handler;

import static org.openhab.binding.freebox.FreeboxBindingConstants.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.matmaul.freeboxos.FreeboxException;
import org.matmaul.freeboxos.airmedia.AirMediaReceiver;
import org.matmaul.freeboxos.airmedia.AirMediaReceiverRequest;
import org.matmaul.freeboxos.call.CallEntry;
import org.matmaul.freeboxos.lan.LanHostConfig;
import org.matmaul.freeboxos.lan.LanHostL3Connectivity;
import org.matmaul.freeboxos.lan.LanHostsConfig;
import org.matmaul.freeboxos.phone.PhoneStatus;
import org.openhab.binding.freebox.FreeboxBindingConstants;
import org.openhab.binding.freebox.internal.config.FreeboxAirPlayDeviceConfiguration;
import org.openhab.binding.freebox.internal.config.FreeboxNetDeviceConfiguration;
import org.openhab.binding.freebox.internal.config.FreeboxNetInterfaceConfiguration;
import org.openhab.binding.freebox.internal.config.FreeboxPhoneConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxThingHandler} is responsible for handling everything associated to
 * any Freebox thing types except the bridge thing type.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxThingHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(FreeboxThingHandler.class);

    private ScheduledFuture<?> phoneJob;
    private ScheduledFuture<?> callsJob;
    private FreeboxHandler bridgeHandler;
    private Calendar lastPhoneCheck;
    private String netAddress;
    private String airPlayName;
    private String airPlayPassword;

    public FreeboxThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((getThing().getStatus() == ThingStatus.OFFLINE)
                && ((getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE)
                        || (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR))) {
            return;
        }
        try {
            if (command instanceof RefreshType) {
                return;
            } else if (command instanceof StringType && PLAYURL.equals(channelUID.getId())) {
                playMedia(command.toString());
            } else if (command instanceof OnOffType && STOP.equals(channelUID.getId())) {
                stopMedia();
            } else {
                logger.debug("Thing {}: unexpected command {} from channel {}", getThing().getUID(), command,
                        channelUID.getId());
            }
        } catch (FreeboxException e) {
            logger.debug("Thing {}: error while handling command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
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
        initializeThing((getBridge() == null) ? null : getBridge().getHandler(), bridgeStatusInfo.getStatus());
    }

    private void initializeThing(ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeThing {}", bridgeStatus);
        if (thingHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);

                bridgeHandler = (FreeboxHandler) thingHandler;

                if (getThing().getThingTypeUID().equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_PHONE)) {
                    lastPhoneCheck = Calendar.getInstance();

                    if (phoneJob == null || phoneJob.isCancelled()) {
                        long pollingInterval = getConfigAs(FreeboxPhoneConfiguration.class).refreshPhoneInterval;
                        if (pollingInterval > 0) {
                            logger.debug("Scheduling phone state job every {} seconds...", pollingInterval);
                            phoneJob = scheduler.scheduleWithFixedDelay(phoneRunnable, 1, pollingInterval,
                                    TimeUnit.SECONDS);
                        }
                    }

                    if (callsJob == null || callsJob.isCancelled()) {
                        long pollingInterval = getConfigAs(FreeboxPhoneConfiguration.class).refreshPhoneCallsInterval;
                        if (pollingInterval > 0) {
                            logger.debug("Scheduling phone calls job every {} seconds...", pollingInterval);
                            callsJob = scheduler.scheduleWithFixedDelay(callsRunnable, 1, pollingInterval,
                                    TimeUnit.SECONDS);
                        }
                    }

                } else if (getThing().getThingTypeUID().equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_DEVICE)) {
                    netAddress = getConfigAs(FreeboxNetDeviceConfiguration.class).macAddress;
                } else if (getThing().getThingTypeUID()
                        .equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_INTERFACE)) {
                    netAddress = getConfigAs(FreeboxNetInterfaceConfiguration.class).ipAddress;
                } else if (getThing().getThingTypeUID().equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_AIRPLAY)) {
                    airPlayName = getConfigAs(FreeboxAirPlayDeviceConfiguration.class).name;
                    airPlayPassword = getConfigAs(FreeboxAirPlayDeviceConfiguration.class).password;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private Runnable phoneRunnable = () -> {
        logger.debug("Polling phone state...");

        try {
            fetchPhone();

            if (getThing().getStatus() == ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

        } catch (Throwable t) {
            if (t instanceof FreeboxException) {
                logger.error("Phone state job - FreeboxException: {}", ((FreeboxException) t).getMessage());
            } else if (t instanceof Exception) {
                logger.error("Phone state job - Exception: {}", ((Exception) t).getMessage());
            } else if (t instanceof Error) {
                logger.error("Phone state job - Error: {}", ((Error) t).getMessage());
            } else {
                logger.error("Phone state job - Unexpected error");
            }
            StringWriter sw = new StringWriter();
            if ((t instanceof RuntimeException) && (t.getCause() != null)) {
                t.getCause().printStackTrace(new PrintWriter(sw));
            } else {
                t.printStackTrace(new PrintWriter(sw));
            }
            logger.error("{}", sw);
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
    };

    private Runnable callsRunnable = () -> {
        logger.debug("Polling phone calls...");

        try {
            fetchNewCalls();

            if (getThing().getStatus() == ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

        } catch (Throwable t) {
            if (t instanceof FreeboxException) {
                logger.error("Phone calls job - FreeboxException: {}", ((FreeboxException) t).getMessage());
            } else if (t instanceof Exception) {
                logger.error("Phone calls job - Exception: {}", ((Exception) t).getMessage());
            } else if (t instanceof Error) {
                logger.error("Phone calls job - Error: {}", ((Error) t).getMessage());
            } else {
                logger.error("Phone calls job - Unexpected error");
            }
            StringWriter sw = new StringWriter();
            if ((t instanceof RuntimeException) && (t.getCause() != null)) {
                t.getCause().printStackTrace(new PrintWriter(sw));
            } else {
                t.printStackTrace(new PrintWriter(sw));
            }
            logger.error("{}", sw);
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
    };

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

    private void fetchPhone() throws FreeboxException {
        List<PhoneStatus> phoneStatus;
        phoneStatus = bridgeHandler.getFbClient().getPhoneManager().getPhoneStatus();
        updateState(new ChannelUID(getThing().getUID(), STATE, ONHOOK),
                phoneStatus.get(0).getOn_hook() ? OnOffType.ON : OnOffType.OFF);
        updateState(new ChannelUID(getThing().getUID(), STATE, RINGING),
                phoneStatus.get(0).getIs_ringing() ? OnOffType.ON : OnOffType.OFF);
    }

    private void fetchNewCalls() throws FreeboxException {
        List<CallEntry> callEntries = bridgeHandler.getFbClient().getCallManager().getCallEntries();
        PhoneCallComparator comparator = new PhoneCallComparator();
        Collections.sort(callEntries, comparator);

        for (CallEntry call : callEntries) {
            Calendar callEndTime = call.getTimeStamp();
            callEndTime.add(Calendar.SECOND, (int) (call.getDuration()));
            if ((call.getDuration() > 0) && callEndTime.after(lastPhoneCheck)) {
                updateCall(call, ANY);

                if (call.getType().equalsIgnoreCase("accepted")) {
                    updateCall(call, ACCEPTED);
                } else if (call.getType().equalsIgnoreCase("missed")) {
                    updateCall(call, MISSED);
                } else if (call.getType().equalsIgnoreCase("outgoing")) {
                    updateCall(call, OUTGOING);
                }

                lastPhoneCheck = callEndTime;
            }
        }
    }

    private void updateCall(CallEntry call, String channelGroup) {
        if (channelGroup != null) {
            updateState(new ChannelUID(getThing().getUID(), channelGroup, CALLNUMBER),
                    new StringType(call.getNumber()));
            updateState(new ChannelUID(getThing().getUID(), channelGroup, CALLDURATION),
                    new DecimalType(call.getDuration()));
            updateState(new ChannelUID(getThing().getUID(), channelGroup, CALLTIMESTAMP),
                    new DateTimeType(call.getTimeStamp()));
            updateState(new ChannelUID(getThing().getUID(), channelGroup, CALLNAME), new StringType(call.getName()));
            if (channelGroup.equals(ANY)) {
                updateState(new ChannelUID(getThing().getUID(), channelGroup, CALLSTATUS),
                        new StringType(call.getType()));
            }
        }
    }

    public void updateNetInfo(LanHostsConfig config) {
        if ((config != null) && (getThing().getStatus() == ThingStatus.ONLINE)
                && (getThing().getThingTypeUID().equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_DEVICE)
                        || getThing().getThingTypeUID()
                                .equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_INTERFACE))) {
            boolean found = false;
            boolean reachable = false;
            String vendor = null;
            for (LanHostConfig hostConfig : config.getConfig()) {
                if ((getThing().getThingTypeUID().equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_DEVICE))
                        && (hostConfig.getMAC() != null) && hostConfig.getMAC().equals(netAddress)) {
                    found = true;
                    reachable = hostConfig.getReachable();
                    vendor = hostConfig.getVendorName();
                    break;
                }
                if (hostConfig.getL3connectivities() != null) {
                    for (LanHostL3Connectivity l3 : hostConfig.getL3connectivities()) {
                        if ((getThing().getThingTypeUID()
                                .equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_INTERFACE))
                                && (l3.getAddr() != null) && l3.getAddr().equals(netAddress)) {
                            found = true;
                            if (l3.getReachable()) {
                                reachable = true;
                                vendor = hostConfig.getVendorName();
                                break;
                            }
                        }
                    }
                }
            }
            if (found) {
                updateState(new ChannelUID(getThing().getUID(), FreeboxBindingConstants.REACHABLE),
                        reachable ? OnOffType.ON : OnOffType.OFF);
            }
            if ((vendor != null) && !vendor.isEmpty()) {
                updateProperty(Thing.PROPERTY_VENDOR, vendor);
            }
        }
    }

    public void updateAirPlayDevice(List<AirMediaReceiver> receivers) {
        if (getThing().getThingTypeUID().equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_AIRPLAY)) {
            // The Freebox API allows pushing media only to receivers with photo or video capabilities
            // but not to receivers with only audio capability
            boolean found = false;
            boolean usable = false;
            if (receivers != null) {
                for (AirMediaReceiver receiver : receivers) {
                    if (airPlayName.equals(receiver.getName())) {
                        found = true;
                        usable = Boolean.TRUE.equals(receiver.isVideoCapable());
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
    }

    public void playMedia(String url) throws FreeboxException {
        if (bridgeHandler != null && url != null) {
            stopMedia();

            AirMediaReceiverRequest request = new AirMediaReceiverRequest();
            request.setAction("start");
            request.setMediaType("video");
            if (airPlayPassword != null && !airPlayPassword.isEmpty()) {
                request.setPassword(airPlayPassword);
            }
            request.setMedia(url);
            bridgeHandler.getFbClient().getAirMediaManager().sendRequestToReceiver(airPlayName, request);
        }
    }

    private void stopMedia() throws FreeboxException {
        if (bridgeHandler != null) {
            AirMediaReceiverRequest request = new AirMediaReceiverRequest();
            request.setAction("stop");
            request.setMediaType("video");
            if (airPlayPassword != null && !airPlayPassword.isEmpty()) {
                request.setPassword(airPlayPassword);
            }
            bridgeHandler.getFbClient().getAirMediaManager().sendRequestToReceiver(airPlayName, request);
        }
    }

    /**
     * A comparator of phone calls by ascending end date and time
     */
    private class PhoneCallComparator implements Comparator<CallEntry> {

        @Override
        public int compare(CallEntry call1, CallEntry call2) {
            int result = 0;
            Calendar callEndTime1 = call1.getTimeStamp();
            callEndTime1.add(Calendar.SECOND, (int) (call1.getDuration()));
            Calendar callEndTime2 = call2.getTimeStamp();
            callEndTime2.add(Calendar.SECOND, (int) (call2.getDuration()));
            if (callEndTime1.before(callEndTime2)) {
                result = -1;
            } else if (callEndTime1.after(callEndTime2)) {
                result = 1;
            }
            return result;
        }

    }
}
