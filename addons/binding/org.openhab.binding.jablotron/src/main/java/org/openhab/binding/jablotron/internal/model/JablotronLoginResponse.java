/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.internal.model;

/**
 * The {@link JablotronLoginResponse} class defines the login call
 * response.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class JablotronLoginResponse {
    private int status;

    public int getStatus() {
        return status;
    }

    public boolean isOKStatus() {
        return status == 200;
    }
}
