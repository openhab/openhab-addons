package org.openhab.binding.wink.client;

/**
 * Exception that is thrown when unable to authenticate to wink api
 *
 * @author Shawn Crosby
 *
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String string) {
        super(string);
    }

    /**
     * serialization version ID
     */
    private static final long serialVersionUID = 1L;

}
