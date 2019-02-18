/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.ui.cometvisu.php;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Tobias Bräutigam - Initial contribution
 */
public interface PHProvider {
    public void createQuercusEngine();

    public void setIni(String key, String value);

    public void init(String absolutePath, String defaultUserDir, ServletContext _servletContext);

    public void phpService(File file, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;
}
