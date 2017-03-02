package org.openhab.binding.blueiris.internal.data;

/**
 * Login to the blue iris system and get back a login reply with session details.
 *
 * @author David Bennett - Initial Contribution
 */
public class LoginRequest extends BlueIrisCommandRequest<LoginReply> {
    public LoginRequest() {
        super(LoginReply.class, "login");
    }
}
