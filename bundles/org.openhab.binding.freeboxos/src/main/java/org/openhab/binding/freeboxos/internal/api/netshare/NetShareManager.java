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
package org.openhab.binding.freeboxos.internal.api.netshare;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.NETSHARE_PATH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.netshare.afp.AfpManager;
import org.openhab.binding.freeboxos.internal.api.netshare.samba.SambaManager;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.rest.RestManager;

/**
 * The {@link NetShareManager} is the Java class used to handle api requests related to network shares
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetShareManager extends RestManager {

    public NetShareManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.NONE, NETSHARE_PATH);
        session.addManager(SambaManager.class, new SambaManager(session, getUriBuilder()));
        session.addManager(AfpManager.class, new AfpManager(session, getUriBuilder()));
    }

}
