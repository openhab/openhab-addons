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
package org.openhab.binding.satel.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.event.IntegraVersionEvent;
import org.openhab.binding.satel.internal.protocol.SatelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command class for command that returns Integra version and type.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class IntegraVersionCommand extends SatelCommandBase {

    private final Logger logger = LoggerFactory.getLogger(IntegraVersionCommand.class);

    public static final byte COMMAND_CODE = 0x7e;

    /**
     * Creates new command class instance.
     */
    public IntegraVersionCommand() {
        super(COMMAND_CODE, false);
    }

    /**
     * @return Integra firmware version and release date
     */
    public String getVersion() {
        return getVersion(1);
    }

    /**
     * @return Integra type
     */
    public byte getType() {
        return getResponse().getPayload()[0];
    }

    /**
     * @return firmware language
     */
    public byte getLanguage() {
        return getResponse().getPayload()[12];
    }

    /**
     * @return <code>true</code> if alarm settings are stored in flash memory
     */
    public boolean areSettingsInFlash() {
        return getResponse().getPayload()[13] == (byte) 0xFF;
    }

    @Override
    protected boolean isResponseValid(SatelMessage response) {
        // validate response
        if (response.getPayload().length != 14) {
            logger.debug("Invalid payload length: {}", response.getPayload().length);
            return false;
        }
        return true;
    }

    @Override
    protected void handleResponseInternal(final EventDispatcher eventDispatcher) {
        // dispatch version event
        eventDispatcher
                .dispatchEvent(new IntegraVersionEvent(getType(), getVersion(), getLanguage(), areSettingsInFlash()));
    }
}
