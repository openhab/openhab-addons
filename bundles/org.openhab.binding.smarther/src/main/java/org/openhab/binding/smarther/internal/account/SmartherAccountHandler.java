/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.account;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.smarther.internal.api.model.Location;
import org.openhab.binding.smarther.internal.api.model.Module;
import org.openhab.binding.smarther.internal.api.model.ModuleSettings;
import org.openhab.binding.smarther.internal.api.model.ModuleStatus;
import org.openhab.binding.smarther.internal.api.model.Plant;
import org.openhab.binding.smarther.internal.api.model.Program;
import org.openhab.binding.smarther.internal.api.model.Subscription;

/**
 * Interface to decouple Smarther Account Handler implementation from other code.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public interface SmartherAccountHandler extends ThingHandler {

    /**
     * @return The {@link ThingUID} associated with this Smarther Account Handler
     */
    ThingUID getUID();

    /**
     * @return The label of the Smarther Bridge associated with this Smarther Account Handler
     */
    String getLabel();

    /**
     * @return List of available locations associated with this Smarther Account Handler
     */
    List<Location> listLocations();

    /**
     * @param plantId Id of the location to search for
     * @return true if the given location is managed by the handler
     */
    boolean hasLocation(String plantId);

    /**
     * @return List of available plants or an empty list if nothing was returned
     */
    List<Plant> listPlants();

    /**
     * @return The plant current subscription list or an empty list if nothing was returned
     */
    List<Subscription> getSubscriptionList();

    /**
     * @param plantId Identifier of the location plant the module is contained in
     * @param notificationUrl Notification Url the new subscription will send notifications to
     * @return The identifier of the newly added subscription
     */
    String addSubscription(String plantId, String notificationUrl);

    /**
     * @param plantId Identifier of the location plant the module is contained in
     * @param subscriptionId Identifier of the subscription to be removed
     */
    void removeSubscription(String plantId, String subscriptionId);

    /**
     * @param location Location plant to get the topology map of
     * @return List of modules contained in the given location or an empty list if nothing was returned
     */
    List<Module> listModules(Location location);

    /**
     * @param plantId Identifier of the location plant the module is contained in
     * @param moduleId Identifier of the module to query the status for
     * @return The module current status or an empty list if nothing was returned
     */
    ModuleStatus getModuleStatus(String plantId, String moduleId);

    /**
     * @param settings The new settings to be remotely applied to the module
     * @return true if the operation succeeded, false otherwise
     */
    boolean setModuleStatus(ModuleSettings moduleSettings);

    /**
     * @param plantId Identifier of the location plant the module is contained in
     * @param moduleId Identifier of the module to get the program list for
     * @return The module current program list or an empty list if nothing was returned
     */
    List<Program> getModuleProgramList(String plantId, String moduleId);

    /**
     * @return true if the Smarther Bridge is authorized.
     */
    boolean isAuthorized();

    /**
     * @return true if the device is online
     */
    boolean isOnline();

    /**
     * Calls BTicino/Legrand API gateway to obtain refresh and access tokens and persist data with Thing.
     *
     * @param redirectUrl The redirect url BTicino/Legrand portal calls back to
     * @param reqCode The unique code passed by BTicino/Legrand portal to obtain the refresh and access tokens
     * @param notificationUrl The endpoint BTicino/Legrand C2C notification service will send status notifications to
     *            once authorized
     * @return The name of the BTicino/Legrand portal user that is authorized
     */
    String authorize(String redirectUrl, String reqCode, String notificationUrl);

    /**
     * Returns true if the given Thing UID relates to this {@link SmartherAccountHandler} instance.
     *
     * @param thingUID The Thing UID to check
     * @return true if it relates to the given Thing UID
     */
    boolean equalsThingUID(String thingUID);

    /**
     * Formats the Url to use to call BTicino/Legrand API gateway to authorize the application.
     *
     * @param redirectUri The uri BTicino/Legrand portal will redirect back to
     * @return The formatted url that should be used to call BTicino/Legrand API gateway with
     */
    String formatAuthorizationUrl(String redirectUri);

}
