package org.openhab.binding.boschspexor.internal.api.service.auth;

import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService.SpexorAuthGrantState;

/**
 * Value holder of the Auth2.0 Device Code flow process.
 * Contains - dependent on the state - the specific values.
 *
 * @author Marc
 *
 */
public class AuthProcessingStatus {

    private SpexorAuthGrantState state;
    private String errorMessage;
    private String userCode;

    /**
     * returns the current state of the authorization status
     *
     * @return
     */
    public SpexorAuthGrantState getState() {
        return state;
    }

    private void setState(SpexorAuthGrantState state) {
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

    private void clear() {
        userCode = null;
        errorMessage = null;
    }

    /**
     * returns a flag if the current state is an error state
     *
     * @return
     */
    public boolean isError() {
        return errorMessage != null;
    }

    public void valid() {
        clear();
        setState(SpexorAuthGrantState.AUTHORIZED);
    }

    public void uninitialized() {
        clear();
        setState(SpexorAuthGrantState.UNINITIALIZED);
    }

    public void error(String error) {
        clear();
        setState(SpexorAuthGrantState.UNINITIALIZED);
        setErrorMessage(error);
    }

    public void codeRequested() {
        clear();
        setState(SpexorAuthGrantState.CODE_REQUESTED);
    }

    public void awaitingUserAcceptance(String userCode) {
        clear();
        setState(SpexorAuthGrantState.AWAITING_USER_ACCEPTANCE);
        setUserCode(userCode);
    }

    public void expiredDeviceToken() {
        clear();
        setState(SpexorAuthGrantState.CODE_REQUEST_FAILED);
        setErrorMessage("User refused authorization");
    }
}
