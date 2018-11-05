/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal.dto;

/**
 * The {@link BridgeApiLockActionDto} class defines the Data Transfer Object (POJO) for the Nuki Bridge API /lockAction
 * endpoint.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeApiLockActionDto {

    private boolean success;
    private boolean batteryCritical;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isBatteryCritical() {
        return batteryCritical;
    }

    public void setBatteryCritical(boolean batteryCritical) {
        this.batteryCritical = batteryCritical;
    }

}
