/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx;

import java.io.File;

/**
 * The {@link KNXProjectProvider} is an interface that needs to be
 * implemented by classes that want to provide knxproject files
 *
 * @author Karel Goderis - Initial contribution
 */
public interface KNXProjectProvider {

    /**
     *
     * Returns the Collection of knxproject Files
     *
     */
    public Iterable<File> getAllProjects();

    /**
     *
     * Removes a knxproject file
     *
     * @param file - the file to remove
     */
    public void removeProject(File file);

    /**
     *
     * Adds or refreshed a knxproject file
     *
     * @param file - the file to add or refresh
     */
    public void addOrRefreshProject(File file);

}
