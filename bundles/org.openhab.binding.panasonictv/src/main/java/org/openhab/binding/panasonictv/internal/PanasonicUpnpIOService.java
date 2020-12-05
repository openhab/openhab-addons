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
package org.openhab.binding.panasonictv.internal;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jupnp.UpnpService;
import org.jupnp.controlpoint.ActionCallback;
import org.jupnp.controlpoint.ControlPoint;
import org.jupnp.controlpoint.SubscriptionCallback;
import org.jupnp.model.action.ActionArgumentValue;
import org.jupnp.model.action.ActionException;
import org.jupnp.model.action.ActionInvocation;
import org.jupnp.model.gena.CancelReason;
import org.jupnp.model.gena.GENASubscription;
import org.jupnp.model.message.UpnpResponse;
import org.jupnp.model.message.header.UDNHeader;
import org.jupnp.model.meta.Action;
import org.jupnp.model.meta.Device;
import org.jupnp.model.meta.DeviceIdentity;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.Service;
import org.jupnp.model.state.StateVariableValue;
import org.jupnp.model.types.ServiceId;
import org.jupnp.model.types.UDAServiceId;
import org.jupnp.model.types.UDN;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanasonicUpnpIOService} is the implementation of the UpnpIOService
 * interface
 *
 * @author Karel Goderis - Initial contribution; added simple polling mechanism
 * @author Kai Kreuzer - added descriptor url retrieval
 * @author Markus Rathgeb - added NP checks in subscription ended callback
 * @author Andre Fuechsel - added methods to remove subscriptions
 * @author Ivan Iliev - made sure resubscribe is only done when subscription ended CancelReason was EXPIRED or
 *         RENEW_FAILED
 * @author Jan N. Klug - adapted findService to wrong namespace
 */
@SuppressWarnings({ "rawtypes" })
@Component(immediate = true, service = PanasonicUpnpIOService.class)
public class PanasonicUpnpIOService implements UpnpIOService, RegistryListener {

    private final Logger logger = LoggerFactory.getLogger(PanasonicUpnpIOService.class);

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(POOL_NAME);

    private static final int DEFAULT_POLLING_INTERVAL = 60;
    private static final String POOL_NAME = "upnp-io";

    private final UpnpService upnpService;

    final Set<UpnpIOParticipant> participants = new CopyOnWriteArraySet<>();
    final Map<UpnpIOParticipant, ScheduledFuture> pollingJobs = new ConcurrentHashMap<>();
    final Map<UpnpIOParticipant, Boolean> currentStates = new ConcurrentHashMap<>();
    final Map<Service, UpnpSubscriptionCallback> subscriptionCallbacks = new ConcurrentHashMap<>();

    public class UpnpSubscriptionCallback extends SubscriptionCallback {

        public UpnpSubscriptionCallback(Service service) {
            super(service);
        }

        public UpnpSubscriptionCallback(Service service, int requestedDurationSeconds) {
            super(service, requestedDurationSeconds);
        }

        @Override
        protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse response) {
            final Service service = subscription.getService();
            if (service != null) {
                final ServiceId serviceId = service.getServiceId();
                final Device device = service.getDevice();
                if (device != null) {
                    final Device deviceRoot = device.getRoot();
                    if (deviceRoot != null) {
                        final DeviceIdentity deviceRootIdentity = deviceRoot.getIdentity();
                        if (deviceRootIdentity != null) {
                            final UDN deviceRootUdn = deviceRootIdentity.getUdn();
                            logger.debug("A GENA subscription '{}' for device '{}' was ended", serviceId.getId(),
                                    deviceRootUdn);
                        }
                    }
                }

                if ((CancelReason.EXPIRED.equals(reason) || CancelReason.RENEWAL_FAILED.equals(reason))
                        && upnpService != null) {
                    final ControlPoint cp = upnpService.getControlPoint();
                    if (cp != null) {
                        final UpnpSubscriptionCallback callback = new UpnpSubscriptionCallback(service,
                                subscription.getActualDurationSeconds());
                        cp.execute(callback);
                    }
                }
            }
        }

        @Override
        protected void established(GENASubscription subscription) {
            Device deviceRoot = subscription.getService().getDevice().getRoot();
            String serviceId = subscription.getService().getServiceId().getId();

            logger.trace("A GENA subscription '{}' for device '{}' is established", serviceId,
                    deviceRoot.getIdentity().getUdn());

            for (UpnpIOParticipant participant : participants) {
                if (Objects.equals(getDevice(participant), deviceRoot)) {
                    try {
                        participant.onServiceSubscribed(serviceId, true);
                    } catch (Exception e) {
                        logger.error("Participant threw an exception onServiceSubscribed", e);
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void eventReceived(GENASubscription sub) {
            Map<String, StateVariableValue> values = sub.getCurrentValues();
            Device deviceRoot = sub.getService().getDevice().getRoot();
            String serviceId = sub.getService().getServiceId().getId();

            logger.trace("Receiving a GENA subscription '{}' response for device '{}'", serviceId,
                    deviceRoot.getIdentity().getUdn());
            for (UpnpIOParticipant participant : participants) {
                if (Objects.equals(getDevice(participant), deviceRoot)) {
                    for (String stateVariable : values.keySet()) {
                        StateVariableValue value = values.get(stateVariable);
                        if (value != null && value.getValue() != null) {
                            try {
                                participant.onValueReceived(stateVariable, value.getValue().toString(), serviceId);
                            } catch (Exception e) {
                                logger.error("Participant threw an exception onValueReceived", e);
                            }
                        }
                    }
                    break;
                }
            }
        }

        @Override
        protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
            logger.debug("A GENA subscription '{}' for device '{}' missed events",
                    subscription.getService().getServiceId(),
                    subscription.getService().getDevice().getRoot().getIdentity().getUdn());
        }

        @Override
        protected void failed(GENASubscription subscription, UpnpResponse response, Exception e, String defaultMsg) {
            Device deviceRoot = subscription.getService().getDevice().getRoot();
            String serviceId = subscription.getService().getServiceId().getId();

            logger.debug("A GENA subscription '{}' for device '{}' failed: {}", serviceId,
                    deviceRoot.getIdentity().getUdn(), response.getResponseDetails(), e);

            for (UpnpIOParticipant participant : participants) {
                if (Objects.equals(getDevice(participant), deviceRoot)) {
                    try {
                        participant.onServiceSubscribed(serviceId, false);
                    } catch (Exception e2) {
                        logger.error("Participant threw an exception onServiceSubscribed", e2);
                    }
                }
            }
        }
    }

    @Activate
    public PanasonicUpnpIOService(final @Reference UpnpService upnpService) {
        this.upnpService = upnpService;
    }

    @Activate
    public void activate() {
        logger.debug("Starting Panasonic UPnP IO service...");
        upnpService.getRegistry().getRemoteDevices().forEach(device -> informParticipants(device, true));
        upnpService.getRegistry().addListener(this);
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Stopping Panasonic UPnP IO service...");
        upnpService.getRegistry().removeListener(this);
    }

    private Device getDevice(UpnpIOParticipant participant) {
        return upnpService.getRegistry().getDevice(new UDN(participant.getUDN()), true);
    }

    @Override
    public void addSubscription(UpnpIOParticipant participant, String serviceID, int duration) {
        if (participant != null && serviceID != null) {
            registerParticipant(participant);
            Device device = getDevice(participant);
            if (device != null) {
                Service subService = searchSubService(serviceID, device);
                if (subService != null) {
                    logger.trace("Setting up an UPNP service subscription '{}' for participant '{}'", serviceID,
                            participant.getUDN());

                    UpnpSubscriptionCallback callback = new UpnpSubscriptionCallback(subService, duration);
                    subscriptionCallbacks.put(subService, callback);
                    upnpService.getControlPoint().execute(callback);
                } else {
                    logger.trace("Could not find service '{}' for device '{}'", serviceID,
                            device.getIdentity().getUdn());
                }
            } else {
                logger.trace("Could not find an upnp device for participant '{}'", participant.getUDN());
            }
        }
    }

    private Service searchSubService(String serviceID, Device device) {
        Service subService = findService(device, serviceID);
        if (subService == null) {
            // service not on the root device, we search the embedded devices as well
            Device[] embedded = device.getEmbeddedDevices();
            if (embedded != null) {
                for (Device aDevice : embedded) {
                    subService = findService(aDevice, serviceID);
                    if (subService != null) {
                        break;
                    }
                }
            }
        }
        return subService;
    }

    @Override
    public void removeSubscription(UpnpIOParticipant participant, String serviceID) {
        if (participant != null && serviceID != null) {
            Device device = getDevice(participant);
            if (device != null) {
                Service subService = searchSubService(serviceID, device);
                if (subService != null) {
                    logger.trace("Removing an UPNP service subscription '{}' for particpant '{}'", serviceID,
                            participant.getUDN());

                    UpnpSubscriptionCallback callback = subscriptionCallbacks.get(subService);
                    if (callback != null) {
                        callback.end();
                    }
                    subscriptionCallbacks.remove(subService);
                } else {
                    logger.trace("Could not find service '{}' for device '{}'", serviceID,
                            device.getIdentity().getUdn());
                }
            } else {
                logger.trace("Could not find an upnp device for participant '{}'", participant.getUDN());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> invokeAction(UpnpIOParticipant participant, String serviceID, String actionID,
            Map<String, String> inputs) {
        Map<String, String> resultMap = new HashMap<>();

        if (serviceID != null && actionID != null && participant != null) {
            registerParticipant(participant);
            Device device = getDevice(participant);

            if (device != null) {
                Service service = findService(device, serviceID);
                if (service != null) {
                    Action action = service.getAction(actionID);
                    if (action != null) {
                        ActionInvocation invocation = new ActionInvocation(action);
                        if (inputs != null) {
                            for (String variable : inputs.keySet()) {
                                invocation.setInput(variable, inputs.get(variable));
                            }
                        }

                        logger.trace("Invoking Action '{}' of service '{}' for participant '{}'", actionID, serviceID,
                                participant.getUDN());
                        new ActionCallback.Default(invocation, upnpService.getControlPoint()).run();

                        ActionException anException = invocation.getFailure();
                        if (anException != null && anException.getMessage() != null) {
                            logger.debug("Invoking Action failed: {}", anException.getMessage());
                        }

                        Map<String, ActionArgumentValue> result = invocation.getOutputMap();
                        if (result != null) {
                            for (String variable : result.keySet()) {
                                final ActionArgumentValue newArgument;
                                try {
                                    newArgument = Objects.requireNonNull(result.get(variable));
                                } catch (final Exception ex) {
                                    logger.debug("An exception '{}' occurred, cannot get argument for variable '{}'",
                                            ex.getMessage(), variable);
                                    continue;
                                }
                                try {
                                    if (newArgument.getValue() != null) {
                                        resultMap.put(variable, newArgument.getValue().toString());
                                    }
                                } catch (final Exception ex) {
                                    logger.debug(
                                            "An exception '{}' occurred processing ActionArgumentValue '{}' with value '{}'",
                                            ex.getMessage(), newArgument.getArgument().getName(),
                                            newArgument.getValue());
                                }
                            }
                        }
                    } else {
                        logger.debug("Could not find action '{}' for participant '{}'", actionID, participant.getUDN());
                    }
                } else {
                    logger.debug("Could not find service '{}' for participant '{}'", serviceID, participant.getUDN());
                }
            } else {
                logger.debug("Could not find an upnp device for participant '{}'", participant.getUDN());
            }
        }
        return resultMap;
    }

    @Override
    public boolean isRegistered(UpnpIOParticipant participant) {
        UDN udn = new UDN(participant.getUDN());
        if (upnpService.getRegistry().getDevice(udn, true) != null) {
            return true;
        } else {
            upnpService.getControlPoint().search(new UDNHeader(udn));
            return false;
        }
    }

    @Override
    public void registerParticipant(UpnpIOParticipant participant) {
        if (participant != null) {
            participants.add(participant);
        }
    }

    @Override
    public void unregisterParticipant(UpnpIOParticipant participant) {
        if (participant != null) {
            stopPollingForParticipant(participant);
            pollingJobs.remove(participant);
            currentStates.remove(participant);
            participants.remove(participant);
        }
    }

    @Override
    public URL getDescriptorURL(UpnpIOParticipant participant) {
        RemoteDevice device = upnpService.getRegistry().getRemoteDevice(new UDN(participant.getUDN()), true);
        if (device != null) {
            return device.getIdentity().getDescriptorURL();
        } else {
            return null;
        }
    }

    private Service<?, ?> findService(Device<?, ?, ?> device, String serviceID) {
        Service<?, ?> service = device.findService(new UDAServiceId(serviceID));
        if (service == null) {
            String namespace = device.getType().getNamespace();
            service = device.findService(new ServiceId(namespace, serviceID));
        }

        return service;
    }

    /**
     * Propagates a device status change to all participants
     *
     * @param device the device that has changed its status
     * @param status true, if device is reachable, false otherwise
     */
    private void informParticipants(RemoteDevice device, boolean status) {
        for (UpnpIOParticipant participant : participants) {
            if (participant.getUDN().equals(device.getIdentity().getUdn().getIdentifierString())) {
                setDeviceStatus(participant, status);
            }
        }
    }

    private void setDeviceStatus(UpnpIOParticipant participant, boolean newStatus) {
        if (!Objects.equals(currentStates.get(participant), newStatus)) {
            currentStates.put(participant, newStatus);
            logger.debug("Device '{}' reachability status changed to '{}'", participant.getUDN(), newStatus);
            participant.onStatusChanged(newStatus);
        }
    }

    private class UPNPPollingRunnable implements Runnable {

        private final UpnpIOParticipant participant;
        private final String serviceID;
        private final String actionID;

        public UPNPPollingRunnable(UpnpIOParticipant participant, String serviceID, String actionID) {
            this.participant = participant;
            this.serviceID = serviceID;
            this.actionID = actionID;
        }

        @Override
        public void run() {
            // It is assumed that during addStatusListener() a check is made whether the participant is correctly
            // registered
            try {
                Device device = getDevice(participant);
                if (device != null) {
                    Service service = findService(device, serviceID);
                    if (service != null) {
                        Action action = service.getAction(actionID);
                        if (action != null) {
                            @SuppressWarnings("unchecked")
                            ActionInvocation invocation = new ActionInvocation(action);
                            logger.debug("Polling participant '{}' through Action '{}' of Service '{}' ",
                                    participant.getUDN(), actionID, serviceID);
                            new ActionCallback.Default(invocation, upnpService.getControlPoint()).run();

                            // The UDN is reachable if no connection exception occurs
                            boolean status = true;
                            ActionException anException = invocation.getFailure();
                            if (anException != null) {
                                String message = anException.getMessage();
                                if (message != null && message.contains("Connection error or no response received")) {
                                    // The UDN is not reachable anymore
                                    status = false;
                                }
                            }
                            // Set status
                            setDeviceStatus(participant, status);
                        } else {
                            logger.debug("Could not find action '{}' for participant '{}'", actionID,
                                    participant.getUDN());
                        }
                    } else {
                        logger.debug("Could not find service '{}' for participant '{}'", serviceID,
                                participant.getUDN());
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred while polling an UPNP device: '{}'", e.getMessage(), e);
            }
        }
    }

    @Override
    public void addStatusListener(UpnpIOParticipant participant, String serviceID, String actionID, int interval) {
        if (participant != null) {
            registerParticipant(participant);

            int pollingInterval = interval == 0 ? DEFAULT_POLLING_INTERVAL : interval;

            // remove the previous polling job, if any
            stopPollingForParticipant(participant);

            currentStates.put(participant, true);

            Runnable pollingRunnable = new UPNPPollingRunnable(participant, serviceID, actionID);
            pollingJobs.put(participant,
                    scheduler.scheduleWithFixedDelay(pollingRunnable, 0, pollingInterval, TimeUnit.SECONDS));
        }
    }

    private void stopPollingForParticipant(UpnpIOParticipant participant) {
        if (pollingJobs.containsKey(participant)) {
            ScheduledFuture<?> pollingJob = pollingJobs.get(participant);
            if (pollingJob != null) {
                pollingJob.cancel(true);
            }
        }
    }

    @Override
    public void removeStatusListener(UpnpIOParticipant participant) {
        if (participant != null) {
            unregisterParticipant(participant);
        }
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        informParticipants(device, true);
    }

    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        informParticipants(device, false);
    }

    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
    }

    @Override
    public void beforeShutdown(Registry registry) {
    }

    @Override
    public void afterShutdown() {
    }
}
