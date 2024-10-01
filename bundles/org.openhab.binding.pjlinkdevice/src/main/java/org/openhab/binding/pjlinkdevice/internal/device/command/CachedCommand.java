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
package org.openhab.binding.pjlinkdevice.internal.device.command;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.cache.ExpiringCache;

/**
 * CachedCommand wraps any command and caches its response for a configurable period of time.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class CachedCommand<ResponseType extends Response<?>> implements Command<ResponseType> {

    private Command<ResponseType> cachedCommand;
    private ExpiringCache<ResponseType> cache;

    public CachedCommand(Command<ResponseType> cachedCommand) {
        this(cachedCommand, 1000);
    }

    public CachedCommand(Command<ResponseType> cachedCommand, int expiry) {
        this.cachedCommand = cachedCommand;
        this.cache = new ExpiringCache<>(expiry, () -> {
            try {
                return this.cachedCommand.execute();
            } catch (ResponseException | IOException | AuthenticationException e) {
                // wrap exception into RuntimeException to unwrap again later in CachedCommand.execute()
                throw new CacheException(e);
            }
        });
    }

    @Override
    public ResponseType execute() throws ResponseException, IOException, AuthenticationException {
        ExpiringCache<ResponseType> cache = this.cache;
        try {
            @Nullable
            ResponseType result = cache.getValue();
            if (result == null) {
                // this can not happen in reality, limitation of ExpiringCache
                throw new ResponseException("Cached value is null");
            }
            return result;
        } catch (CacheException e) {
            // try to unwrap RuntimeException thrown in ExpiringCache
            Throwable cause = e.getCause();
            if (cause instanceof ResponseException responseException) {
                throw responseException;
            }
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            if (cause instanceof AuthenticationException authenticationException) {
                throw authenticationException;
            }
            throw e;
        }
    }
}
