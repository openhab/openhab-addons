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
package org.openhab.binding.boschspexor.internal.api.service.auth;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService.SpexorAuthGrantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Value holder of the Auth2.0 Device Code flow process.
 * Contains - dependent on the state - the specific values.
 *
 * @author Marc Fischer - Initial contribution
 */
public class AuthProcessingStatus {

    private final Logger logger = LoggerFactory.getLogger(AuthProcessingStatus.class);

    private SpexorAuthGrantState state = SpexorAuthGrantState.UNINITIALIZED;
    private String errorMessage;
    private String userCode;
    private String deviceCode;

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
        return errorMessage;
    }

    private void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * will return a user code if the state {@link SpexorAuthGrantState.AWAITING_USER_ACCEPTANCE}
     *
     * @return an OAuth2.0 User Code
     */
    public String getUserCode() {
        return userCode;
    }

    private void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    private void clear() {
        this.userCode = null;
        this.deviceCode = null;
        this.errorMessage = null;
    }

    /**
     * returns a flag if the current state is an error state
     *
     * @return
     */
    public boolean isError() {
        return errorMessage != null;
    }

    public void valid(@NonNull SpexorAuthorizationProcessListener authListener) {
        SpexorAuthGrantState oldState = state;
        clear();
        setState(SpexorAuthGrantState.AUTHORIZED);
        authListener.changedState(oldState, state);
    }

    public void uninitialized(@NonNull SpexorAuthorizationProcessListener authListener) {
        SpexorAuthGrantState oldState = state;
        clear();
        setState(SpexorAuthGrantState.UNINITIALIZED);
        authListener.changedState(oldState, state);
    }

    public void error(String error, @NonNull SpexorAuthorizationProcessListener authListener) {
        SpexorAuthGrantState oldState = state;
        clear();
        setState(SpexorAuthGrantState.UNINITIALIZED);
        setErrorMessage(error);
        authListener.changedState(oldState, state);
    }

    public void codeRequested(@NonNull SpexorAuthorizationProcessListener authListener) {
        SpexorAuthGrantState oldState = state;
        clear();
        setState(SpexorAuthGrantState.CODE_REQUESTED);
        authListener.changedState(oldState, state);
    }

    public void awaitingUserAcceptance(String deviceCode, String userCode,
            @NonNull SpexorAuthorizationProcessListener authListener) {
        SpexorAuthGrantState oldState = state;
        clear();
        setState(SpexorAuthGrantState.AWAITING_USER_ACCEPTANCE);
        setDeviceCode(deviceCode);
        setUserCode(userCode);
        authListener.changedState(oldState, state);
    }

    public void expiredDeviceToken(@NonNull SpexorAuthorizationProcessListener authListener) {
        SpexorAuthGrantState oldState = state;
        clear();
        setState(SpexorAuthGrantState.CODE_REQUEST_FAILED);
        setErrorMessage("User refused authorization");
        authListener.changedState(oldState, state);
    }
}
