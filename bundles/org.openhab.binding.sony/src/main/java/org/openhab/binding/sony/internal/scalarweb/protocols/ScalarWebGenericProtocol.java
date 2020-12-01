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
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebContext;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;

/**
 * The implementation of the protocol is a generic placeholder protocol that provides no active channels
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type for the callback
 */
@NonNullByDefault
class ScalarWebGenericProtocol<T extends ThingCallback<String>> extends AbstractScalarWebProtocol<T> {
    /**
     * Instantiates a new scalar web video protocol.
     *
     * @param context the non-null context
     * @param service the non-null service
     * @param callback the non-null callback
     */
    ScalarWebGenericProtocol(final ScalarWebProtocolFactory<T> factory, final ScalarWebContext context,
            final ScalarWebService service, final T callback) {
        super(factory, context, service, callback);
    }

    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors(final boolean dynamicOnly) {
        return Collections.emptyList();
    }

    @Override
    public void refreshState(boolean initial) {
    }

    @Override
    public void refreshChannel(final ScalarWebChannel channel) {
    }

    @Override
    public void setChannel(final ScalarWebChannel channel, final Command command) {
    }
}
