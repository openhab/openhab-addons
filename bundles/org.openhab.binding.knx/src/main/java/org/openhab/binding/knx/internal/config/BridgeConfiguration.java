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
package org.openhab.binding.knx.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.tpm.TpmInterface;
import org.openhab.core.auth.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler} configuration
 *
 * @author Simon Kaufmann - Initial contribution
 *
 */
@NonNullByDefault
public class BridgeConfiguration {
    private final Logger logger = LoggerFactory.getLogger(BridgeConfiguration.class);
    @Nullable
    TpmInterface tpmIf = null;
    private int autoReconnectPeriod = 0;
    private int readingPause = 0;
    private int readRetriesLimit = 0;
    private int responseTimeout = 0;
    private String keyringFile = "";
    private String keyringPassword = "";

    public int getAutoReconnectPeriod() {
        return autoReconnectPeriod;
    }

    public int getReadingPause() {
        return readingPause;
    }

    public int getReadRetriesLimit() {
        return readRetriesLimit;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public void setAutoReconnectPeriod(int period) {
        autoReconnectPeriod = period;
    }

    public String getKeyringFile() {
        return keyringFile;
    }

    public String getKeyringPassword() {
        return decrypt(keyringPassword);
    }

    protected String decrypt(String secret) {
        if (secret.startsWith(KNXBindingConstants.ENCRYPTED_PASSWORD_SERIALIZATION_PREFIX)) {
            try {
                logger.info("trying to access TPM module");
                return TpmInterface.TPM.deserializeAndDecryptSecret(
                        secret.substring(KNXBindingConstants.ENCRYPTED_PASSWORD_SERIALIZATION_PREFIX.length()));
            } catch (SecurityException e) {
                logger.error("Unable to decode stored password using TPM: {}", e.getMessage());
                // fall through
            }
        }
        return secret;
    }
}
