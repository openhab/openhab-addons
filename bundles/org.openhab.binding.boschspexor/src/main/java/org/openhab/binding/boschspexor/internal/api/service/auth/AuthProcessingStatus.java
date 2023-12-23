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
package org.openhab.binding.boschspexor.internal.api.service.auth;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService.SpexorAuthGrantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Value holder of the Auth2.0 Device Code flow process.
 * Contains - dependent on the state - the specific values.
 *
 * @author Marc Fischer - Initial contribution
 */
@NonNullByDefault
public class AuthProcessingStatus {

    private final Logger logger = LoggerFactory.getLogger(AuthProcessingStatus.class);

    private SpexorAuthGrantState state = SpexorAuthGrantState.UNINITIALIZED;
    private Optional<String> errorMessage = Optional.empty();
    private Optional<String> userCode = Optional.empty();
    private Optional<String> deviceCode = Optional.empty();

    /**
     * returns the current state of the authorization status
     *
     * @return
     */
    public SpexorAuthGrantState getState() {
        return state;
    }

    private void setState(SpexorAuthGrantState state) {
        logger.debug("setting state from {} to {}", this.state, state);
        this.state = state;
    }

    /**
     * return an error message if the current status is faulty
     *
     * @return
     */
    public String getErrorMessage() {
        return errorMessage.orElse("");
    }

    private void setErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = Optional.ofNullable(errorMessage);
    }

    /**
     * will return a user code if the state {@link SpexorAuthGrantState.AWAITING_USER_ACCEPTANCE}
     *
     * @return an OAuth2.0 User Code
     */
    public String getUserCode() {
        return userCode.orElse("");
    }

    private void setUserCode(@Nullable String userCode) {
        this.userCode = Optional.ofNullable(userCode);
    }

    /**
     * will return a device code that needs to be accepted by the user
     *
     * @return an OAuth2.0 Device Code
     */
    public String getDeviceCode() {
        return deviceCode.orElse("");
    }

    public void setDeviceCode(@Nullable String deviceCode) {
        this.deviceCode = Optional.ofNullable(deviceCode);
    }

    private void clear() {
        this.userCode = Optional.empty();
        this.deviceCode = Optional.empty();
        this.errorMessage = Optional.empty();
    }

    /**
     * returns a flag if the current state is an error state
     *
     * @return
     */
    public boolean isError() {
        return !errorMessage.isEmpty();
    }

    public void valid(SpexorAuthorizationProcessListener authListener) {
        SpexorAuthGrantState oldState = state;
        clear();
        setState(SpexorAuthGrantState.AUTHORIZED);
        authListener.changedState(oldState, state);
    }

    public void uninitialized(SpexorAuthorizationProcessListener authListener) {
        SpexorAuthGrantState oldState = state;
        clear();
        setState(SpexorAuthGrantState.UNINITIALIZED);
        authListener.changedState(oldState, state);
    }

    public void error(String error, SpexorAuthorizationProcessListener authListener) {
        SpexorAuthGrantState oldState = state;
        clear();
        setState(SpexorAuthGrantState.UNINITIALIZED);
        setErrorMessage(error);
        authListener.changedState(oldState, state);
    }

    public void codeRequested(SpexorAuthorizationProcessListener authListener) {
        SpexorAuthGrantState oldState = state;
        clear();
        setState(SpexorAuthGrantState.CODE_REQUESTED);
        authListener.changedState(oldState, state);
    }

    public void awaitingUserAcceptance(String deviceCode, String userCode,
            SpexorAuthorizationProcessListener authListener) {
        SpexorAuthGrantState oldState = state;
        clear();
        setState(SpexorAuthGrantState.AWAITING_USER_ACCEPTANCE);
        setDeviceCode(deviceCode);
        setUserCode(userCode);
        authListener.changedState(oldState, state);
    }

    public void bridgeNotConfigured() {
        clear();
        setState(SpexorAuthGrantState.BRIDGE_NOT_CONFIGURED);
    }

    public void expiredDeviceToken(SpexorAuthorizationProcessListener authListener) {
        SpexorAuthGrantState oldState = state;
        clear();
        setState(SpexorAuthGrantState.CODE_REQUEST_FAILED);
        setErrorMessage("User refused authorization");
        authListener.changedState(oldState, state);
    }
}
