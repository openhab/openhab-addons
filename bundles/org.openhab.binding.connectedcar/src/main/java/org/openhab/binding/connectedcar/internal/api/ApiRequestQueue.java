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
package org.openhab.binding.connectedcar.internal.api;

import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.ApiActionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiRequestQueue} implements queueing of pending requests
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class ApiRequestQueue {
    private final Logger logger = LoggerFactory.getLogger(ApiRequestQueue.class);

    private String thingId = "";
    private @Nullable ApiEventListener eventListener;
    private Map<String, ApiActionRequest> pendingRequests = new ConcurrentHashMap<>();

    public void setupRequestQueue(String vin, @Nullable ApiEventListener eventListener) {
        this.thingId = vin;
        this.eventListener = eventListener;
        pendingRequests.clear();
    }

    public boolean isRequestPending(String serviceId) {
        // return true if a request for a specific service is pending
        for (Map.Entry<String, ApiActionRequest> r : pendingRequests.entrySet()) {
            if (r.getValue().service.equals(serviceId)) {
                return true;
            }
        }
        return false;
    }

    public boolean areRequestsPending() {
        // return true if any request is pending
        boolean pending = false;
        for (Map.Entry<String, ApiActionRequest> e : pendingRequests.entrySet()) {
            ApiActionRequest request = e.getValue();
            if (request.isInProgress()) {
                pending = true;
            }
        }
        return pending;
    }

    public String queuePendingAction(ApiActionRequest req) throws ApiException {
        logger.debug("{}: Request {} queued for status updates", thingId, req.requestId);
        if (req.requestId.isEmpty()) {
            throw new IllegalArgumentException("queuePendingAction(): requestId must not be empty!");
        }
        pendingRequests.put(req.requestId, req);
        if (eventListener != null) {
            eventListener.onActionSent(req.service, req.action, req.requestId);
        }

        // Check if action was accepted
        return getRequestStatus(req.requestId, req.status);
    }

    /**
     * Get status update for pending requests
     */
    public void checkPendingRequests() {
        if (!pendingRequests.isEmpty()) {
            logger.debug("{}: Checking status for {} pending requets", thingId, pendingRequests.size());
            for (Map.Entry<String, ApiActionRequest> e : pendingRequests.entrySet()) {
                ApiActionRequest request = e.getValue();
                try {
                    request.status = getRequestStatus(request.requestId, "");
                } catch (ApiException ex) {
                    ApiErrorDTO error = ex.getApiResult().getApiError();
                    if (error.isTechValidationError()) {
                        // Id is no longer valid
                        request.status = API_REQUEST_ERROR;
                    }
                }
            }
        }
    }

    // Will be overwritten by beand implementation to support different formats
    public String getApiRequestStatus(ApiActionRequest req) throws ApiException {
        return API_REQUEST_SUCCESSFUL;
    }

    /**
     * Get request status, handle different formats, return unified request status (even CarNet has different codes for
     * the same logical status)
     *
     * @param requestId The request id return from the API call
     * @param rstatus Raw status returned from status call
     * @return Unified request code (API_REQUEST_xxx)
     * @throws ApiException
     */

    public String getRequestStatus(String requestId, String rstatus) throws ApiException {
        if (!pendingRequests.containsKey(requestId)) {
            throw new IllegalArgumentException("Invalid requestId");
        }

        boolean remove = false;
        String status = rstatus;
        ApiActionRequest request = pendingRequests.get(requestId);
        if (request == null) {
            return "";
        }
        if (request.isExpired()) {
            status = API_REQUEST_TIMEOUT;
            remove = true;
            if (eventListener != null) {
                eventListener.onActionTimeout(request.service, request.action, request.requestId);
            }
        } else {
            try {
                int error = -1;
                if (status.isEmpty()) {
                    if (request.checkUrl.isEmpty()) {
                        // this should not happen
                        logger.warn("{}: Unable to check request {} status for action {}.{}; checkUrl is missing!",
                                thingId, request.requestId, request.service, request.action);
                        status = API_REQUEST_ERROR;
                        remove = true;
                    } else {
                        logger.debug("{}: Check request {} status for action {}.{}; checkUrl={}", thingId,
                                request.requestId, request.service, request.action, request.checkUrl);
                        status = getApiRequestStatus(request);
                    }
                }

                status = status.toLowerCase(); // Hon&Flash returns in upper case
                String actionStatus = status;
                switch (status) {
                    case API_REQUEST_SUCCESSFUL:
                    case API_REQUEST_SUCCEEDED:
                        actionStatus = API_REQUEST_SUCCESSFUL; // normalize status
                        remove = true;
                        break;
                    case API_REQUEST_IN_PROGRESS:
                    case API_REQUEST_QUEUED:
                    case API_REQUEST_FETCHED:
                    case API_REQUEST_STARTED:
                        actionStatus = API_REQUEST_IN_PROGRESS; // normalize status
                        break;
                    case API_REQUEST_NOT_FOUND:
                    case API_REQUEST_FAIL:
                    case API_REQUEST_FAILED:
                        logger.warn("{}: Action {}.{} failed with status {}, error={} (requestId={})", thingId,
                                request.service, request.action, status, error, request.requestId);
                        remove = true;
                        actionStatus = API_REQUEST_FAILED; // normalize status
                        break;
                    default:
                        logger.debug("{}: Request {} has unknown status: {}", thingId, requestId, status);
                }

                if (eventListener != null) {
                    eventListener.onActionResult(request.service, request.action, request.requestId,
                            actionStatus.toUpperCase(), status);
                }
            } catch (ApiException e) {
                logger.debug("{}: Unable to validate request {}, {}", thingId, requestId, e.toString());
            } catch (RuntimeException e) {
                logger.debug("{}: Unable to validate request {}", thingId, requestId, e);
            }
        }

        if (remove) {
            logger.debug("{}: Remove request {} for action {}.{} from queue, status is {}", thingId, request.requestId,
                    request.service, request.action, status);
            pendingRequests.remove(request.requestId);
        }
        return status;
    }
}
