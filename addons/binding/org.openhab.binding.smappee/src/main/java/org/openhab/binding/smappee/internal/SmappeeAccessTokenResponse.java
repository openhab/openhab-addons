/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.internal;

/**
 * Interface for readings updates
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeAccessTokenResponse {
    public String access_token;

    public int expires_in;

    public String refresh_token;
}

// example JSON of a token received from the smappee API :
// {"access_token":"92dXXX49-0645-39ae-b7be-fXXXX65397ed","refresh_token":"8bXXXX54-4a8e-362d-a59a-d7bXXXXX4fd0","expires_in":86400}
