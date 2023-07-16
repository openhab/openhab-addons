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
package org.openhab.binding.connectedcar.internal.api.carnet;

import static org.openhab.binding.connectedcar.internal.BindingConstants.API_REQUEST_TIMEOUT_SEC;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.ApiActionRequest;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetActionResponse.CNActionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetPendingRequest} handles queueing for service requests and status updates
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class CarNetPendingRequest extends ApiActionRequest {
    private final Logger logger = LoggerFactory.getLogger(CarNetPendingRequest.class);

    public CarNetPendingRequest(String service, String action, CNActionResponse rsp) {
        // normalize the resonse type
        this.service = service;
        this.action = action;
        switch (service) {
            case CNAPI_SERVICE_VEHICLE_STATUS_REPORT:
                this.requestId = rsp.currentVehicleDataResponse.requestId;
                this.vin = rsp.currentVehicleDataResponse.vin;
                checkUrl = "bs/vsr/v1/{0}/{1}/vehicles/{2}/requests/" + requestId + "/jobstatus";
                timeout = 2 * API_REQUEST_TIMEOUT_SEC;
                break;
            case CNAPI_SERVICE_REMOTE_LOCK_UNLOCK:
                if (rsp.rluActionResponse != null) {
                    this.vin = rsp.rluActionResponse.vin;
                    this.requestId = rsp.rluActionResponse.requestId;
                }
                checkUrl = "bs/rlu/v1/{0}/{1}/vehicles/{2}/requests/" + requestId + "/status";
                break;
            case CNAPI_SERVICE_REMOTE_HEATING:
                if (rsp.performActionResponse != null) {
                    this.requestId = rsp.performActionResponse.requestId;
                    checkUrl = "bs/rs/v1/{0}/{1}/vehicles/{2}/requests/" + requestId + "/status";
                } else {
                    checkUrl = "bs/rs/v1/{0}/{1}/vehicles/{2}/climater/actions/" + requestId;
                }
                break;
            case CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION:
                if (rsp.action != null) {
                    this.requestId = rsp.action.actionId;
                    this.status = rsp.action.actionState;
                }
                checkUrl = "bs/climatisation/v1/{0}/{1}/vehicles/{2}/climater/actions/" + requestId;
                break;
            case CNAPI_SERVICE_REMOTE_BATTERY_CHARGE:
                // {"action":{"settings":{"maxChargeCurrent":5},"actionState":"queued","actionId":64876731,"type":"setSettings"}}
                if (rsp.action != null) {
                    this.requestId = rsp.action.actionId;
                    this.status = rsp.action.actionState;
                }
                checkUrl = "bs/batterycharge/v1/{0}/{1}/vehicles/{2}/charger/actions/" + requestId;
                break;
            case CNAPI_SERVICE_REMOTE_HONK_AND_FLASH:
                if (rsp.honkAndFlashRequest != null) {
                    this.requestId = rsp.honkAndFlashRequest.id;
                    this.status = rsp.honkAndFlashRequest.status.statusCode;
                    checkUrl = "bs/rhf/v1/{0}/{1}/vehicles/{2}/honkAndFlash/'" + requestId + "'/status";
                }
                break;
            default:
                logger.debug("Unable to queue request, unknown service type {}}.{}", service, action);
        }
    }
}
