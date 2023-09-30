/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bticinosmarther.internal.account;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bticinosmarther.internal.api.dto.Location;
import org.openhab.binding.bticinosmarther.internal.api.dto.Module;
import org.openhab.binding.bticinosmarther.internal.api.dto.ModuleStatus;
import org.openhab.binding.bticinosmarther.internal.api.dto.Plant;
import org.openhab.binding.bticinosmarther.internal.api.dto.Program;
import org.openhab.binding.bticinosmarther.internal.api.dto.Subscription;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherGatewayException;
import org.openhab.binding.bticinosmarther.internal.model.ModuleSettings;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * The {@code SmartherAccountHandler} interface is used to decouple the Smarther account handler implementation from
 * other Bridge code.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public interface SmartherAccountHandler extends ThingHandler {

    /**
     * Returns the {@link ThingUID} associated with this Smarther account handler.
     *
     * @return the thing UID associated with this Smarther account handler
     */
    ThingUID getUID();

    /**
     * Returns the label of the Smarther Bridge associated with this Smarther account handler.
     *
     * @return a string containing the bridge label associated with the account handler
     */
    String getLabel();

    /**
     * Returns the available locations associated with this Smarther account handler.
     *
     * @return the list of available locations, or an empty {@link List} in case of no locations found
     */
    List<Location> getLocations();

    /**
     * Checks whether the given location is managed by this Smarther account handler
     *
     * @param plantId
     *            the identifier of the location to search for
     *
     * @return {@code true} if the given location is found, {@code false} otherwise
     */
    boolean hasLocation(String plantId);

    /**
     * Returns the plants registered under the Smarther account the bridge has been configured with.
     *
     * @return the list of registered plants, or an empty {@link List} in case of no plants found
     *
     * @throws {@link SmartherGatewayException}
     *             in case of communication issues with the Smarther API
     */
    List<Plant> getPlants() throws SmartherGatewayException;

    /**
     * Returns the subscriptions registered to the C2C Webhook, where modules status notifications are currently sent
     * for all the plants.
     *
     * @return the list of registered subscriptions, or an empty {@link List} in case of no subscriptions found
     *
     * @throws SmartherGatewayException in case of communication issues with the Smarther API
     */
    List<Subscription> getSubscriptions() throws SmartherGatewayException;

    /**
     * Subscribes a plant to the C2C Webhook to start receiving modules status notifications.
     *
     * @param plantId
     *            the identifier of the plant to be subscribed
     * @param notificationUrl
     *            the url notifications will have to be sent to for the given plant
     *
     * @return the identifier this subscription has been registered under
     *
     * @throws SmartherGatewayException in case of communication issues with the Smarther API
     */
    String subscribePlant(String plantId, String notificationUrl) throws SmartherGatewayException;

    /**
     * Unsubscribes a plant from the C2C Webhook to stop receiving modules status notifications.
     *
     * @param plantId
     *            the identifier of the plant to be unsubscribed
     * @param subscriptionId
     *            the identifier of the subscription to be removed for the given plant
     *
     * @throws SmartherGatewayException in case of communication issues with the Smarther API
     */
    void unsubscribePlant(String plantId, String subscriptionId) throws SmartherGatewayException;

    /**
     * Returns the chronothermostat modules registered at the given location.
     *
     * @param location
     *            the identifier of the location
     *
     * @return the list of registered modules, or an empty {@link List} if the location contains no module or in case of
     *         communication issues with the Smarther API
     */
    List<Module> getLocationModules(Location location);

    /**
     * Returns the current status of a given chronothermostat module.
     *
     * @param plantId
     *            the identifier of the plant
     * @param moduleId
     *            the identifier of the chronothermostat module inside the plant
     *
     * @return the current status of the chronothermostat module
     *
     * @throws SmartherGatewayException in case of communication issues with the Smarther API
     */
    ModuleStatus getModuleStatus(String plantId, String moduleId) throws SmartherGatewayException;

    /**
     * Sends new settings to be applied to a given chronothermostat module.
     *
     * @param moduleSettings
     *            the module settings to be applied
     *
     * @return {@code true} if the settings have been successfully applied, {@code false} otherwise
     *
     * @throws SmartherGatewayException in case of communication issues with the Smarther API
     */
    boolean setModuleStatus(ModuleSettings moduleSettings) throws SmartherGatewayException;

    /**
     * Returns the automatic mode programs registered for the given chronothermostat module.
     *
     * @param plantId
     *            the identifier of the plant
     * @param moduleId
     *            the identifier of the chronothermostat module inside the plant
     *
     * @return the list of registered programs, or an empty {@link List} in case of no programs found
     *
     * @throws SmartherGatewayException in case of communication issues with the Smarther API
     */
    List<Program> getModulePrograms(String plantId, String moduleId) throws SmartherGatewayException;

    /**
     * Checks whether the Smarther Bridge associated with this Smarther account handler is authorized by Smarther API.
     *
     * @return {@code true} if the Bridge is authorized, {@code false} otherwise
     */
    boolean isAuthorized();

    /**
     * Checks whether the Smarther Bridge thing is online.
     *
     * @return {@code true} if the Bridge is online, {@code false} otherwise
     */
    boolean isOnline();

    /**
     * Performs the authorization procedure with Legrand/Bticino portal.
     * In case of success, the returned refresh/access tokens and the notification url are stored in the Bridge.
     *
     * @param redirectUrl
     *            the redirect url BTicino/Legrand portal calls back to
     * @param reqCode
     *            the unique code passed by BTicino/Legrand portal to obtain the refresh and access tokens
     * @param notificationUrl
     *            the endpoint C2C Webhook service will send module status notifications to, once authorized
     *
     * @return a string containing the name of the BTicino/Legrand portal user that is authorized
     */
    String authorize(String redirectUrl, String reqCode, String notificationUrl) throws SmartherGatewayException;

    /**
     * Compares this Smarther account handler instance to a given Thing UID.
     *
     * @param thingUID
     *            the Thing UID the account handler is compared to
     *
     * @return {@code true} if the two instances match, {@code false} otherwise
     */
    boolean equalsThingUID(String thingUID);

    /**
     * Formats the url used to call the Smarther API in order to authorize the Smarther Bridge associated with this
     * Smarther account handler.
     *
     * @param redirectUri
     *            the uri BTicino/Legrand portal redirects back to
     *
     * @return a string containing the formatted url, or the empty string ("") in case of issue
     */
    String formatAuthorizationUrl(String redirectUri);
}
