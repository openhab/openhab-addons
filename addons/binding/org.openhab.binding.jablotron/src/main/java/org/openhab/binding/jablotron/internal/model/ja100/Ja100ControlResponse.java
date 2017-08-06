/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.internal.model.ja100;

/**
 * The {@link Ja100ControlResponse} class defines the control command
 * response for ja100.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class Ja100ControlResponse {
    private Integer result;
    private int responseCode;
    private int authorization;

    public Integer getResult() {
        return result;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public int getAuthorization() {
        return authorization;
    }
}
