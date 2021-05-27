/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.api;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.API_REQUEST_TIMEOUT_SEC;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.*;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetActionResponse.CNActionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetPendingRequest} holds the information for queued requests
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetPendingRequest {
    private final Logger logger = LoggerFactory.getLogger(CarNetPendingRequest.class);

    public String vin = "";
    public String service = "";
    public String action = "";
    public String checkUrl = "";
    public String requestId = "";
    public String status = "";
    public Date creationTime = new Date();
    public long timeout = API_REQUEST_TIMEOUT_SEC;

    public CarNetPendingRequest(String service, String action, CNActionResponse rsp) {
        // normalize the resonse type
        this.service = service;
        this.action = action;

        switch (service) {
            case CNAPI_SERVICE_VEHICLE_STATUS_REPORT:
                this.requestId = rsp.currentVehicleDataResponse.requestId;
                this.vin = rsp.currentVehicleDataResponse.vin;
                checkUrl = "bs/vsr/v1/{0}/{1}/vehicles/{2}/requests/" + requestId + "/jobstatus";
                timeout = 5 * API_REQUEST_TIMEOUT_SEC;
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

    public static boolean isInProgress(String status) {
        String st = status.toLowerCase();
        return CNAPI_REQUEST_IN_PROGRESS.equals(st) || CNAPI_REQUEST_QUEUED.equals(st)
                || CNAPI_REQUEST_FETCHED.equals(st) || CNAPI_REQUEST_STARTED.equals(st);
    }

    public boolean isExpired() {
        Date currentTime = new Date();
        long diff = currentTime.getTime() - creationTime.getTime();
        return (diff / 1000) > timeout;
    }
}
