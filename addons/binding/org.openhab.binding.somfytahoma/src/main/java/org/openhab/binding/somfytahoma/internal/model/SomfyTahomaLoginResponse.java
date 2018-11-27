/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.internal.model;

/**
 * The {@link SomfyTahomaLoginResponse} holds information about login
 * response to your TahomaLink account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaLoginResponse {

    private boolean success;
    private String version;

    public boolean isSuccess() {
        return success;
    }

    public String getVersion() {
        return version;
    }
}
