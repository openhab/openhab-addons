/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv.internal.protocol;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.jupnp.UpnpService;
import org.jupnp.controlpoint.ActionCallback;
import org.jupnp.model.action.ActionArgumentValue;
import org.jupnp.model.action.ActionException;
import org.jupnp.model.action.ActionInvocation;
import org.jupnp.model.meta.Action;
import org.jupnp.model.meta.Device;
import org.jupnp.model.meta.Service;
import org.jupnp.model.types.ServiceId;
import org.jupnp.model.types.UDAServiceId;
import org.jupnp.model.types.UDN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpRemoteController} is responsible for sending key codes to the
 * Panasonic TV.
 *
 * UpnpIOService failed to find service for Panasonic TV due to name space mismatch (Tested on E6 series tv)
 * between deviceType and serviceId
 *
 * deviceType: <deviceType>urn:panasonic-com:device:p00RemoteController:1</deviceType>
 * serviceId: <serviceId>urn:upnp-org:serviceId:p00NetworkControl</serviceId>
 *
 * findService in {@link UpnpIOServiceImpl} try to find the service using deviceType namespace that failed
 *
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
public class UpnpRemoteController {

    private final Logger logger = LoggerFactory.getLogger(UpnpRemoteController.class);
    private UpnpService upnpService;

    /**
     * Create and initialize remote controller instance.
     *
     * @param uniqueId Unique Id used to send key codes.
     */
    public UpnpRemoteController(UpnpService upnpService) {
        this.upnpService = upnpService;
    }

    /**
     * Modification of findService from UpnpIOServiceImpl that generous on namespace for serviceId
     *
     * @see "org.eclipse.smarthome.io.transport.upnp.internal.UpnpIOServiceImpl"
     *
     * @param device
     * @param serviceID
     * @return
     */
    private Service findService(Device device, String serviceID) {
        Service service = device.findService(new UDAServiceId(serviceID));
        if (service == null) {
            String namespace = device.getType().getNamespace();
            service = device.findService(new ServiceId(namespace, serviceID));
        }

        return service;
    }

    public Map<String, String> invokeAction(UpnpIOParticipant participant, String serviceID, String actionID,
            Map<String, String> inputs) {
        HashMap<String, String> resultMap = new HashMap<>();

        if (serviceID != null && actionID != null && participant != null) {
            Device device = upnpService.getRegistry().getDevice(new UDN(participant.getUDN()), true);

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
                            logger.debug("{}", anException.getMessage());
                        }

                        Map<String, ActionArgumentValue> result = invocation.getOutputMap();
                        if (result != null) {
                            for (String variable : result.keySet()) {
                                final ActionArgumentValue newArgument;
                                try {
                                    newArgument = result.get(variable);
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
}
