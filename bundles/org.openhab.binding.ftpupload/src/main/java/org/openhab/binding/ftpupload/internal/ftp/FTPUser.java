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
package org.openhab.binding.ftpupload.internal.ftp;

import java.util.List;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple FTP user implementation.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class FTPUser implements User {
    private static Logger logger = LoggerFactory.getLogger(FTPUser.class);

    private final String login;
    private int idleTimeout;

    public FTPUser(String login, int idleTimeout) {
        this.login = login;
        this.idleTimeout = idleTimeout;
    }

    @Override
    public AuthorizationRequest authorize(final AuthorizationRequest authRequest) {
        logger.trace("authorize: {}", authRequest);
        return authRequest;
    }

    @Override
    public boolean getEnabled() {
        logger.trace("getEnabled");
        return true;
    }

    @Override
    public String getHomeDirectory() {
        logger.trace("getHomeDirectory");
        return "/";
    }

    @Override
    public int getMaxIdleTime() {
        logger.trace("getMaxIdleTime");
        return idleTimeout;
    }

    @Override
    public String getName() {
        logger.trace("getName");
        return this.login;
    }

    @Override
    public List<Authority> getAuthorities() {
        logger.trace("getAuthorities");
        return null;
    }

    @Override
    public List<Authority> getAuthorities(Class<? extends Authority> arg0) {
        logger.trace("getAuthorities: {}", arg0);
        return null;
    }

    @Override
    public String getPassword() {
        logger.trace("getPassword");
        return null;
    }
}
