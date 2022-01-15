/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Factory creating {@link DefaultMieleWebservice} instances.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class DefaultMieleWebserviceFactory implements MieleWebserviceFactory {
    @Override
    public MieleWebservice create(MieleWebserviceConfiguration configuration) {
        return new DefaultMieleWebservice(configuration);
    }
}
