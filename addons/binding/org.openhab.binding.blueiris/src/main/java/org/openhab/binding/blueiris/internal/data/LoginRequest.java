/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
