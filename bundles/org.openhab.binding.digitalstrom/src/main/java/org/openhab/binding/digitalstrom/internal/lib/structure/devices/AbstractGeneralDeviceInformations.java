/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices;

import org.openhab.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ChangeableDeviceConfigEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;

import com.google.gson.JsonObject;

/**
 * The {@link AbstractGeneralDeviceInformations} is an abstract implementation of {@link GeneralDeviceInformations} and
 * can be implement by subclasses which contains the same device informations like dSID and/or mechanismen like the
 * {@link DeviceStatusListener}.
 *
 * @author Michael Ochel - initial contributer
 * @author Matthias Siegele - initial contributer
 */
public abstract class AbstractGeneralDeviceInformations implements GeneralDeviceInformation {

    protected DSID dsid;
    protected String dSUID;
    protected Boolean isPresent;
    protected Boolean isValide;
    protected String name;
    protected String displayID;
    protected DeviceStatusListener listener;

    /**
     * Creates a new {@link AbstractGeneralDeviceInformations} through the digitalSTROM json response as
     * {@link JsonObject}.
     *
     * @param jsonDeviceObject json response of the digitalSTROM-Server, must not be null
     */
    public AbstractGeneralDeviceInformations(JsonObject jsonDeviceObject) {
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.NAME.getKey()) != null) {
            name = jsonDeviceObject.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
        }
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.ID.getKey()) != null) {
            dsid = new DSID(jsonDeviceObject.get(JSONApiResponseKeysEnum.ID.getKey()).getAsString());
        } else if (jsonDeviceObject.get(JSONApiResponseKeysEnum.DSID.getKey()) != null) {
            dsid = new DSID(jsonDeviceObject.get(JSONApiResponseKeysEnum.DSID.getKey()).getAsString());
        } else if (jsonDeviceObject.get(JSONApiResponseKeysEnum.DSID_LOWER_CASE.getKey()) != null) {
            dsid = new DSID(jsonDeviceObject.get(JSONApiResponseKeysEnum.DSID_LOWER_CASE.getKey()).getAsString());
        }
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.DSUID.getKey()) != null) {
            dSUID = jsonDeviceObject.get(JSONApiResponseKeysEnum.DSUID.getKey()).getAsString();
        }
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.DISPLAY_ID.getKey()) != null) {
            displayID = jsonDeviceObject.get(JSONApiResponseKeysEnum.DISPLAY_ID.getKey()).getAsString();
        }
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.IS_PRESENT.getKey()) != null) {
            isPresent = jsonDeviceObject.get(JSONApiResponseKeysEnum.IS_PRESENT.getKey()).getAsBoolean();
        } else if (jsonDeviceObject.get(JSONApiResponseKeysEnum.PRESENT.getKey()) != null) {
            isPresent = jsonDeviceObject.get(JSONApiResponseKeysEnum.PRESENT.getKey()).getAsBoolean();
        }
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.IS_VALID.getKey()) != null) {
            isValide = jsonDeviceObject.get(JSONApiResponseKeysEnum.IS_VALID.getKey()).getAsBoolean();
        }
    }

    @Override
    public synchronized String getName() {
        return this.name;
    }

    @Override
    public synchronized void setName(String name) {
        this.name = name;
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.DEVICE_NAME);
        }
    }

    @Override
    public DSID getDSID() {
        return dsid;
    }

    @Override
    public String getDSUID() {
        return this.dSUID;
    }

    @Override
    public Boolean isPresent() {
        return isPresent;
    }

    @Override
    public void setIsPresent(boolean isPresent) {
        this.isPresent = isPresent;
        if (listener != null) {
            if (!isPresent) {
                listener.onDeviceRemoved(this);
            } else {
                listener.onDeviceAdded(this);
            }
        }
    }

    @Override
    public Boolean isValid() {
        return isValide;
    }

    @Override
    public void setIsValid(boolean isValide) {
        this.isValide = isValide;
    }

    @Override
    public void registerDeviceStatusListener(DeviceStatusListener listener) {
        if (listener != null) {
            this.listener = listener;
            listener.onDeviceAdded(this);
        }
    }

    @Override
    public DeviceStatusListener unregisterDeviceStatusListener() {
        DeviceStatusListener listener = this.listener;
        this.listener = null;
        return listener;
    }

    @Override
    public boolean isListenerRegisterd() {
        return listener != null;
    }

    @Override
    public DeviceStatusListener getDeviceStatusListener() {
        return listener;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dSUID == null) ? 0 : dSUID.hashCode());
        result = prime * result + ((displayID == null) ? 0 : displayID.hashCode());
        result = prime * result + ((dsid == null) ? 0 : dsid.hashCode());
        result = prime * result + ((isPresent == null) ? 0 : isPresent.hashCode());
        result = prime * result + ((isValide == null) ? 0 : isValide.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractGeneralDeviceInformations)) {
            return false;
        }
        AbstractGeneralDeviceInformations other = (AbstractGeneralDeviceInformations) obj;
        if (dSUID == null) {
            if (other.dSUID != null) {
                return false;
            }
        } else if (!dSUID.equals(other.dSUID)) {
            return false;
        }
        if (displayID == null) {
            if (other.displayID != null) {
                return false;
            }
        } else if (!displayID.equals(other.displayID)) {
            return false;
        }
        if (dsid == null) {
            if (other.dsid != null) {
                return false;
            }
        } else if (!dsid.equals(other.dsid)) {
            return false;
        }
        if (isPresent == null) {
            if (other.isPresent != null) {
                return false;
            }
        } else if (!isPresent.equals(other.isPresent)) {
            return false;
        }
        if (isValide == null) {
            if (other.isValide != null) {
                return false;
            }
        } else if (!isValide.equals(other.isValide)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String getDisplayID() {
        return displayID;
    }
}
