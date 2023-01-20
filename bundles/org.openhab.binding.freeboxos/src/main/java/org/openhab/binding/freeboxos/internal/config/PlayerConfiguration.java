/**
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
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
package org.openhab.binding.freeboxos.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PlayerConfiguration} is responsible for holding configuration informations needed to access/poll the
 * freebox player
<<<<<<< Upstream, based on origin/main
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerConfiguration extends ClientConfiguration {
    public static final String REMOTE_CODE = "remoteCode";
    public String remoteCode = "";
=======
 * Copyright (c) 2010-2022 Contributors to the openHAB project
=======
 * Copyright (c) 2010-2023 Contributors to the openHAB project
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
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
package org.openhab.binding.freeboxos.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PlayerConfiguration} is responsible for holding
 * configuration informations needed to access/poll the freebox player
=======
>>>>>>> e4ef5cc Switching to Java 17 records
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerConfiguration extends ClientConfiguration {
    public static final String REMOTE_CODE = "remoteCode";
    public String remoteCode = "";
<<<<<<< Upstream, based on origin/main
    public String callBackUrl = "";
>>>>>>> 46dadb1 SAT warnings handling
=======
>>>>>>> e4ef5cc Switching to Java 17 records
}
