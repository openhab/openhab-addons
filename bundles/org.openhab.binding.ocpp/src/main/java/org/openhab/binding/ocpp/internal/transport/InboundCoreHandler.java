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
 * Handles inbound OCPP 1.6 Core-profile requests from chargers, answering each with a spec-valid
 * confirmation and forwarding the load-bearing events (boot, status, metering, transactions) to the
 * {@link OcppServerListener}. Authorize / StartTransaction honour the optional idTag whitelist, and
 * the BootNotification response carries the per-charger heartbeat.
 *
 * <p>
 * A {@code null} return here makes the library reply CallError {@code NotSupported}; a thrown
 * exception makes it reply {@code InternalError}. Both are deliberately avoided for the Core profile.
 *
 * <p>
 * The {@code handle*} overrides carry {@code @NonNullByDefault({})} because the library's
 * {@link ServerCoreEventHandler} leaves its parameters null-unconstrained; a {@code @NonNull}
 * override of an unconstrained parameter is rejected by the null checker. The library never actually
 * passes null here.
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
        listener.onBootNotification(sessionIndex, request);
        // UTC: this timestamp (like the heartbeat's) is what chargers synchronize their clock to.
        return new BootNotificationConfirmation(ZonedDateTime.now(ZoneOffset.UTC), listener.heartbeatFor(sessionIndex),
                RegistrationStatus.Accepted);
    }

    @Override
    @NonNullByDefault({})
    public DataTransferConfirmation handleDataTransferRequest(UUID sessionIndex, DataTransferRequest request) {
        logger.debug("DataTransfer from session {} vendorId {}", sessionIndex, request.getVendorId());
        // Spec-correct default for an unrecognised vendor id (rather than a CallError).
        return new DataTransferConfirmation(DataTransferStatus.UnknownVendorId);
    }

    @Override
    @NonNullByDefault({})
    public HeartbeatConfirmation handleHeartbeatRequest(UUID sessionIndex, HeartbeatRequest request) {
        logger.trace("Heartbeat from session {}", sessionIndex);
        listener.onHeartbeat(sessionIndex);
        return new HeartbeatConfirmation(ZonedDateTime.now(ZoneOffset.UTC));
    }

    @Override
    @NonNullByDefault({})
    public MeterValuesConfirmation handleMeterValuesRequest(UUID sessionIndex, MeterValuesRequest request) {
        logger.debug("MeterValues from session {} connector {}", sessionIndex, request.getConnectorId());
        try {
            listener.onMeterValues(sessionIndex, request);
        } catch (RuntimeException e) {
            // Always acknowledge received metering — a processing failure is ours to log, not a
            // protocol error (InternalError) to hand back to the charger.
            logger.warn("Failed to process MeterValues from session {}: {}", sessionIndex, e.getMessage());
        }
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
            listener.onStartTransaction(sessionIndex, request, transactionId);
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
        listener.onStatusNotification(sessionIndex, request);
        return new StatusNotificationConfirmation();
    }

    @Override
    @NonNullByDefault({})
    public StopTransactionConfirmation handleStopTransactionRequest(UUID sessionIndex, StopTransactionRequest request) {
        logger.debug("StopTransaction from session {} txId {}", sessionIndex, request.getTransactionId());
        listener.onStopTransaction(sessionIndex, request);
        return new StopTransactionConfirmation();
    }
}
