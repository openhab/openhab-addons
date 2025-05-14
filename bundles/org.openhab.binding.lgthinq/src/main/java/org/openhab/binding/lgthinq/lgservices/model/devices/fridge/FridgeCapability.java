/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.devices.fridge;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;

/**
 * Represents the capabilities of a fridge device, defining various mappings for temperature control,
 * filter status, and additional features.
 * This interface extends {@link CapabilityDefinition}.
 *
 * <p>
 * It provides access to key mappings for different functionalities and allows checking the presence
 * of specific modes.
 * </p>
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface FridgeCapability extends CapabilityDefinition {

    /**
     * Retrieves a mapping of fridge temperature values in Celsius.
     *
     * @return A {@link Map} where keys are feature names and values are corresponding temperature settings in Celsius.
     */
    Map<String, String> getFridgeTempCMap();

    /**
     * Retrieves a mapping of fridge temperature values in Fahrenheit.
     *
     * @return A {@link Map} where keys are feature names and values are corresponding temperature settings in
     *         Fahrenheit.
     */
    Map<String, String> getFridgeTempFMap();

    /**
     * Retrieves a mapping of freezer temperature values in Celsius.
     *
     * @return A {@link Map} where keys are feature names and values are corresponding temperature settings in Celsius.
     */
    Map<String, String> getFreezerTempCMap();

    /**
     * Retrieves a mapping of freezer temperature values in Fahrenheit.
     *
     * @return A {@link Map} where keys are feature names and values are corresponding temperature settings in
     *         Fahrenheit.
     */
    Map<String, String> getFreezerTempFMap();

    /**
     * Retrieves a mapping of temperature unit settings.
     *
     * @return A {@link Map} where keys are feature names and values indicate the temperature unit used (Celsius or
     *         Fahrenheit).
     */
    Map<String, String> getTempUnitMap();

    /**
     * Retrieves a mapping related to the Ice Plus feature.
     *
     * @return A {@link Map} representing Ice Plus settings.
     */
    Map<String, String> getIcePlusMap();

    /**
     * Retrieves a mapping related to the Fresh Air Filter status.
     *
     * @return A {@link Map} representing Fresh Air Filter status.
     */
    Map<String, String> getFreshAirFilterMap();

    /**
     * Retrieves a mapping related to the Water Filter status.
     *
     * @return A {@link Map} representing Water Filter status.
     */
    Map<String, String> getWaterFilterMap();

    /**
     * Retrieves a mapping related to the Express Freeze mode.
     *
     * @return A {@link Map} representing Express Freeze mode settings.
     */
    Map<String, String> getExpressFreezeModeMap();

    /**
     * Retrieves a mapping related to the Smart Saving feature.
     *
     * @return A {@link Map} representing Smart Saving settings.
     */
    Map<String, String> getSmartSavingMap();

    /**
     * Retrieves a mapping related to the Active Saving feature.
     *
     * @return A {@link Map} representing Active Saving settings.
     */
    Map<String, String> getActiveSavingMap();

    /**
     * Retrieves a mapping that indicates whether at least one door is open.
     *
     * @return A {@link Map} representing the door open status.
     */
    Map<String, String> getAtLeastOneDoorOpenMap();

    /**
     * Retrieves a mapping of command definitions available for the fridge.
     *
     * @return A {@link Map} where keys are command names and values are {@link CommandDefinition} instances.
     */
    Map<String, CommandDefinition> getCommandsDefinition();

    /**
     * Checks whether the Express Cool mode is present.
     *
     * @return {@code true} if Express Cool mode is available, otherwise {@code false}.
     */
    boolean isExpressCoolModePresent();

    /**
     * Sets the presence status of Express Cool mode.
     *
     * @param isPresent {@code true} if Express Cool mode is available, otherwise {@code false}.
     */
    void setExpressCoolModePresent(boolean isPresent);

    /**
     * Checks whether the Eco-Friendly mode is present.
     *
     * @return {@code true} if Eco-Friendly mode is available, otherwise {@code false}.
     */
    boolean isEcoFriendlyModePresent();

    /**
     * Sets the presence status of Eco-Friendly mode.
     *
     * @param isPresent {@code true} if Eco-Friendly mode is available, otherwise {@code false}.
     */
    void setEcoFriendlyModePresent(boolean isPresent);
}
