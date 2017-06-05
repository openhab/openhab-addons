/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.api.manager;

import org.openhab.binding.energenie.internal.api.EnergenieDeviceTypes;
import org.openhab.binding.energenie.internal.api.JsonGateway;
import org.openhab.binding.energenie.internal.api.JsonSubdevice;

/**
 * A helper class for executing requests to the Mi|Home REST API.
 * For more information see {@link https://mihome4u.co.uk/docs/api-documentation}
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Dimitar Ivanov - Initial contribution and API
 *
 */
public interface EnergenieApiManager {

    /**
     * This method returns the {@link EnergenieApiConfiguration} (consisting of user and password) which is
     * needed for making the HTTP requests
     *
     * @return the configuration associated with the EnergenieApiManager
     */
    public EnergenieApiConfiguration getConfiguration();

    /**
     * This method will register a new network attached device (gateway) to an account.
     *
     * @param label - a friendly name for the device, please use the Eclipse SmartHome Thing short name
     * @param authCode - the device auth code (10-sign code)
     *
     * @return details about the registered device
     */
    public JsonGateway registerGateway(String label, String authCode);

    /**
     * This method lists all gateway devices in your account.
     *
     * @return details about all gateway devices in the account, along with information about available firmware
     *         upgrades
     */
    public JsonGateway[] listGateways();

    /**
     * Unregister a gateway from the account.
     *
     * @param id - the ID of the device to unregister
     * @return details of the removed device
     */
    public JsonGateway unregisterGateway(int id);

    /**
     * Initiate an upgrade to the latest available firmware for a gateway.
     *
     * @param id - the ID of the gateway to upgrade
     * @return if an upgrade can be commenced, information about the new firmware; if no update is
     *         available, a validation error
     */
    public JsonGateway upgradeGatewayFirmware(int id);

    /**
     * This method lists all subdevices in your account, along with information, basic status and usage information.
     *
     * @return array of subdevices, properties may vary between objects depending on their type
     */
    public JsonSubdevice[] listSubdevices();

    /**
     * This method will initiate the registration (pairing) of a new subdevice to a radio master. Please follow the
     * pairing instructions for the specific device to finish the pairing process
     *
     * @param gatewayID - the ID of the relevant gateway device
     * @param deviceType - the type of subdevice we want to register. One of the available {@link EnergenieDeviceTypes}
     * @return details of the master device that will perform the pairing
     */
    public JsonSubdevice registerSubdevice(int gatewayID, EnergenieDeviceTypes deviceType);

    /**
     * This method will show information about a particular subdevice.
     *
     * @param id - the ID of the subdevice
     * @return details of the subdevice
     */
    public JsonSubdevice showSubdeviceInfo(int id);

    /**
     * This method allows you to update the properties of a subdevice.
     *
     * @param id - the ID of the subdevice to modify
     * @param label - a new friendly name for the device
     * @return details of the updated subdevice
     */
    public JsonSubdevice updateSubdevice(int id, String label);

    /**
     * This method allows you to unregister a subdevice from the system.
     *
     * @param id - the ID of the subdevice to unregister
     * @return details of the removed subdevice
     */
    public JsonSubdevice unregisterSubdevice(int id);
}
