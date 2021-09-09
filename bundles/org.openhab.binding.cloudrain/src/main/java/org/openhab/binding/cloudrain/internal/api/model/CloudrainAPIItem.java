/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.cloudrain.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * An abstract base class for Cloudrain API items containing common attributes
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public abstract class CloudrainAPIItem {

    private static final String CONTROLLER_NAME_DEFAULT = "Unknwon Controller";
    private static final String CONTROLLER_ID_DEFAULT = "controller";

    @SerializedName("controllerId")
    protected @Nullable String controllerId;

    @SerializedName("controllerName")
    protected @Nullable String controllerName;

    /**
     * Create a new CloudrainAPIItem with the required attributes. Useful for test implementations. Typically objects of
     * this type will be created through reflection by the GSON library when parsing the JSON response of the API
     *
     * @param controllerId the ID of the controller managing this item
     * @param controllerName the name of the controller managing this item
     */
    public CloudrainAPIItem(String controllerId, String controllerName) {
        this.controllerId = controllerId;
        this.controllerName = controllerName;
    }

    public @Nullable String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public @Nullable String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    /**
     * This class is being de-serialized from an API JSON result and thus fields are nullable.
     * In case the Controller attributes are null this is not a problem as those fields are for information only.
     * This methods offers a convenient way to substitute null fields with defaults.
     *
     * @return a non-null String representing the ClontrollerName or a default value
     */
    public String getControllerNameWithDefault() {
        String localName = getControllerName();
        if (localName == null || localName.isBlank()) {
            return CONTROLLER_NAME_DEFAULT;
        }
        return localName;
    }

    /**
     * This class is being de-serialized from an API JSON result and thus fields are nullable.
     * In case the Controller attributes are null this is not a problem as those fields are for information only.
     * This methods offers a convenient way to substitute null fields with defaults.
     *
     * @return a non-null String representing the ClontrollerId or a default value
     */
    public String getControlleIdWithDefault() {
        String localId = getControllerId();
        if (localId == null || localId.isBlank()) {
            return CONTROLLER_ID_DEFAULT;
        }
        return localId;
    }
}
