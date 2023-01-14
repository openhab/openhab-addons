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
package org.openhab.binding.upnpcontrol.internal.config;

import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.DEFAULT_PATH;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.upnpcontrol.internal.util.UpnpControlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing the binding configuration parameters. Some helper methods take care of updating the relevant classes
 * with parameter changes.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UpnpControlBindingConfiguration {
    private final Logger logger = LoggerFactory.getLogger(UpnpControlBindingConfiguration.class);

    public String path = DEFAULT_PATH;

    public void update(UpnpControlBindingConfiguration newConfig) {
        String newPath = newConfig.path;

        if (newPath.isEmpty()) {
            path = DEFAULT_PATH;
        } else {
            File file = new File(newPath);
            if (!file.isDirectory()) {
                file = file.getParentFile();
            }
            if (file.exists()) {
                if (!(newPath.endsWith(File.separator) || newPath.endsWith("/"))) {
                    newPath = newPath + File.separator;
                }
                path = newPath;
            } else {
                path = DEFAULT_PATH;
            }
        }

        logger.debug("Storage path updated to {}", path);

        UpnpControlUtil.bindingConfigurationChanged(path);
    }
}
