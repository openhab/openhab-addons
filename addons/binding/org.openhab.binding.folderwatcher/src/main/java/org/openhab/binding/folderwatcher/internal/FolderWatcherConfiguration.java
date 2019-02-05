/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.folderwatcher.internal;

/**
 * The {@link FolderWatcherConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
public class FolderWatcherConfiguration {

    /**
     * Sample configuration parameter. Replace with your own.
     */
    public String ftpAddress;
    public String ftpUsername;
    public String ftpPassword;
    public String ftpDir;
    public Integer pollInterval;
    public Integer connectionTimeout;
    public boolean listHidden;
    public Integer diffHours;
    // config1;
}
