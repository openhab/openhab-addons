/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
/**
* Exception thrown when an unsupported command type is sent to a channel.
*
* @author Dave Badia- Initial contribution
*
*/
package org.openhab.binding.panamaxfurman.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanInformationReceivedEvent;
import org.openhab.core.types.State;

/**
 * Exception thrown when an unsupported command type is sent to a channel.
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public interface ProtocolMapper {

    /**
     * Translates the given {@link PowerConditionerChannel} into a string that the Power Conditioner will understand for
     * querying state
     *
     * @param channel the power conditioner channel to be mapped
     * @param the outlet number to be mapped
     *
     * @return the appropriate string to transmit to the Power Conditioner based on the protocol in use.
     *         Returns null if the PowerConditionerChannel is not supported by this protocol
     */
    public @Nullable String buildQueryString(PowerConditionerChannel channel, @Nullable Integer outletNumber);

    /**
     * Translates the given {@link PowerConditionerChannel} into a string that the Power Conditioner will understand for
     * requesting an update
     *
     * @param channel the power conditioner channel to be mapped
     * @param the outlet number to be mapped
     *
     * @return the appropriate string to transmit to the Power Conditioner based on the protocol in use.
     *         Returns null if the PowerConditionerChannel is not supported by this protocol
     */
    public @Nullable String buildUpdateString(PowerConditionerChannel channel, @Nullable Integer outletNumber,
            State stateToSet);

    /**
     * Translates the update message from the Power Conditioner into a {@link PanamaxFurmanStatusUpdateEvent} which can
     * be understood by this binding and the OH framework
     *
     * @param the raw data from the power conditioner to be parsed
     * @return object containing the new state as received from the Power Conditioner. Returns null if the status update
     *         from the Power Conditioner is not supported by this binding
     */
    public @Nullable PanamaxFurmanInformationReceivedEvent parseUpdateIfSupported(String data);
}
