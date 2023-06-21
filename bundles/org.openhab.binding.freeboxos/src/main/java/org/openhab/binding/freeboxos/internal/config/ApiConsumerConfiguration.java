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
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiConsumerConfiguration {
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
    // public static final String PASSWORD = "password";
    // public static final String PORT = "port";
=======
>>>>>>> b6f7a14 Corrections following fwolter code review
    public static final String REFRESH_INTERVAL = "refreshInterval";
=======
    public static final String PASSWORD = "password";
    public static final String PORT = "port";
<<<<<<< Upstream, based on origin/main
>>>>>>> e4ef5cc Switching to Java 17 records
=======
=======
    // public static final String PASSWORD = "password";
    // public static final String PORT = "port";
>>>>>>> 9aef877 Rebooting Home Node part
    public static final String REFRESH_INTERVAL = "refreshInterval";
>>>>>>> cff27ca Saving work

    public int refreshInterval = 30;
    public String password = "";
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
    // public int port = 24322;
=======
>>>>>>> b6f7a14 Corrections following fwolter code review
    public boolean acceptAllMp3 = true;
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
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiConsumerConfiguration {
    public int refreshInterval = 30;
>>>>>>> 46dadb1 SAT warnings handling
=======
    public int port = 24322;
=======
    // public int port = 24322;
>>>>>>> 9aef877 Rebooting Home Node part
    public boolean acceptAllMp3 = true;
>>>>>>> e4ef5cc Switching to Java 17 records
}
