/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ocpp.internal.transport;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chargetime.ocpp.feature.profile.ServerCoreEventHandler;
import eu.chargetime.ocpp.model.core.AuthorizationStatus;
import eu.chargetime.ocpp.model.core.AuthorizeConfirmation;
import eu.chargetime.ocpp.model.core.AuthorizeRequest;
import eu.chargetime.ocpp.model.core.BootNotificationConfirmation;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.DataTransferConfirmation;
import eu.chargetime.ocpp.model.core.DataTransferRequest;
import eu.chargetime.ocpp.model.core.DataTransferStatus;
import eu.chargetime.ocpp.model.core.HeartbeatConfirmation;
import eu.chargetime.ocpp.model.core.HeartbeatRequest;
import eu.chargetime.ocpp.model.core.IdTagInfo;
import eu.chargetime.ocpp.model.core.MeterValuesConfirmation;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.RegistrationStatus;
import eu.chargetime.ocpp.model.core.StartTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationConfirmation;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;

/**
 * Handles inbound OCPP 1.6 Core-profile requests, answering each with a spec-valid confirmation and
 * forwarding events to the {@link OcppServerListener}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class InboundCoreHandler implements ServerCoreEventHandler {

    private final Logger logger = LoggerFactory.getLogger(InboundCoreHandler.class);
    private final OcppServerListener listener;

    public InboundCoreHandler(OcppServerListener listener) {
        this.listener = listener;
    }

    @Override
    @NonNullByDefault({})
    public AuthorizeConfirmation handleAuthorizeRequest(UUID sessionIndex, AuthorizeRequest request) {
        AuthorizationStatus status = listener.isTagAuthorized(request.getIdTag()) ? AuthorizationStatus.Accepted
                : AuthorizationStatus.Invalid;
        logger.debug("Authorize from session {} idTag {} -> {}", sessionIndex, request.getIdTag(), status);
        listener.onAuthorize(sessionIndex, request.getIdTag());
        return new AuthorizeConfirmation(new IdTagInfo(status));
    }

    @Override
    @NonNullByDefault({})
    public BootNotificationConfirmation handleBootNotificationRequest(UUID sessionIndex,
            BootNotificationRequest request) {
        logger.debug("BootNotification from session {}: vendor={} model={} fw={}", sessionIndex,
                request.getChargePointVendor(), request.getChargePointModel(), request.getFirmwareVersion());
        deliver("BootNotification", sessionIndex, () -> listener.onBootNotification(sessionIndex, request));
        return new BootNotificationConfirmation(ZonedDateTime.now(ZoneOffset.UTC), listener.heartbeatFor(sessionIndex),
                RegistrationStatus.Accepted);
    }

    /** Deliver an inbound message to the listener without letting a throw there starve the OCPP confirmation. */
    private void deliver(String what, UUID session, Runnable delivery) {
        try {
            delivery.run();
        } catch (RuntimeException e) {
            logger.warn("Failed to process {} from session {}: {}", what, session, e.getMessage());
        }
    }

    @Override
    @NonNullByDefault({})
    public DataTransferConfirmation handleDataTransferRequest(UUID sessionIndex, DataTransferRequest request) {
        logger.debug("DataTransfer from session {} vendorId {}", sessionIndex, request.getVendorId());
        return new DataTransferConfirmation(DataTransferStatus.UnknownVendorId);
    }

    @Override
    @NonNullByDefault({})
    public HeartbeatConfirmation handleHeartbeatRequest(UUID sessionIndex, HeartbeatRequest request) {
        logger.trace("Heartbeat from session {}", sessionIndex);
        deliver("Heartbeat", sessionIndex, () -> listener.onHeartbeat(sessionIndex));
        return new HeartbeatConfirmation(ZonedDateTime.now(ZoneOffset.UTC));
    }

    @Override
    @NonNullByDefault({})
    public MeterValuesConfirmation handleMeterValuesRequest(UUID sessionIndex, MeterValuesRequest request) {
        logger.debug("MeterValues from session {} connector {}", sessionIndex, request.getConnectorId());
        deliver("MeterValues", sessionIndex, () -> listener.onMeterValues(sessionIndex, request));
        return new MeterValuesConfirmation();
    }

    @Override
    @NonNullByDefault({})
    public StartTransactionConfirmation handleStartTransactionRequest(UUID sessionIndex,
            StartTransactionRequest request) {
        boolean authorized = listener.isTagAuthorized(request.getIdTag());
        int transactionId = listener.nextTransactionId();
        logger.debug("StartTransaction from session {} connector {} idTag {} -> txId {} ({})", sessionIndex,
                request.getConnectorId(), request.getIdTag(), transactionId, authorized ? "accepted" : "invalid");
        if (authorized) {
            deliver("StartTransaction", sessionIndex,
                    () -> listener.onStartTransaction(sessionIndex, request, transactionId));
        }
        AuthorizationStatus status = authorized ? AuthorizationStatus.Accepted : AuthorizationStatus.Invalid;
        return new StartTransactionConfirmation(new IdTagInfo(status), transactionId);
    }

    @Override
    @NonNullByDefault({})
    public StatusNotificationConfirmation handleStatusNotificationRequest(UUID sessionIndex,
            StatusNotificationRequest request) {
        logger.debug("StatusNotification from session {} connector {}: {} ({})", sessionIndex, request.getConnectorId(),
                request.getStatus(), request.getErrorCode());
        deliver("StatusNotification", sessionIndex, () -> listener.onStatusNotification(sessionIndex, request));
        return new StatusNotificationConfirmation();
    }

    @Override
    @NonNullByDefault({})
    public StopTransactionConfirmation handleStopTransactionRequest(UUID sessionIndex, StopTransactionRequest request) {
        logger.debug("StopTransaction from session {} txId {}", sessionIndex, request.getTransactionId());
        deliver("StopTransaction", sessionIndex, () -> listener.onStopTransaction(sessionIndex, request));
        return new StopTransactionConfirmation();
    }
}
