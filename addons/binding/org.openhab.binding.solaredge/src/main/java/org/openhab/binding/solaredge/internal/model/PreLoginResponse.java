/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * this class is used to map the login response
 *
 * @author afriese
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreLoginResponse implements DataResponse {
    private String isLicensed;
    private String success;
    private String failure;

    @Override
    public Map<String, String> getValues() {
        return null;
    }

    public final String getIsLicensed() {
        return isLicensed;
    }

    public final void setIsLicensed(String isLicensed) {
        this.isLicensed = isLicensed;
    }

    public final String getSuccess() {
        return success;
    }

    public final void setSuccess(String success) {
        this.success = success;
    }

    public final String getFailure() {
        return failure;
    }

    public final void setFailure(String failure) {
        this.failure = failure;
    }

}
