/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ftpupload.internal.ftp;

import java.util.HashMap;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple FTP user manager implementation.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class FTPUserManager implements UserManager {
    private final Logger logger = LoggerFactory.getLogger(FTPUserManager.class);

    private int idleTimeout;
    private HashMap<String, UsernamePassword> authenticationData = new HashMap<String, UsernamePassword>();

    @Override
    public User authenticate(final Authentication inAuth) throws AuthenticationFailedException {
        logger.trace("authenticate: {}", inAuth);

        UsernamePasswordAuthentication upa = (UsernamePasswordAuthentication) inAuth;
        String login = upa.getUsername();
        String password = upa.getPassword();

        if (!autheticate(login, password)) {
            throw new AuthenticationFailedException();
        }
        return new FTPUser(login, idleTimeout);
    }

    private boolean autheticate(String login, String password) {
        boolean result = false;

        if (login != null && password != null) {
            UsernamePassword credential = authenticationData.get(login);

            if (credential != null) {
                if (login.equals(credential.getUsername()) && password.equals(credential.getPassword())) {
                    return true;
                }
            }
        }
        return result;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public synchronized void addAuthenticationCredentials(String username, String password)
            throws IllegalArgumentException {

        if (authenticationData.containsKey(username)) {
            throw new IllegalArgumentException("Credentials for user '" + username + "' already exists!");
        }
        authenticationData.put(username, new UsernamePassword(username, password));
    }

    public synchronized void removeAuthenticationCredentials(String username) {
        authenticationData.remove(username);
    }

    @Override
    public User getUserByName(final String login) throws FtpException {
        logger.trace("getUserByName: {}", login);
        return new FTPUser(login, idleTimeout);
    }

    @Override
    public void delete(String arg0) throws FtpException {
        logger.trace("delete: {}", arg0);
    }

    @Override
    public boolean doesExist(String arg0) throws FtpException {
        logger.trace("doesExist: {}", arg0);
        return false;
    }

    @Override
    public String getAdminName() throws FtpException {
        logger.trace("getAdminName");
        return null;
    }

    @Override
    public String[] getAllUserNames() throws FtpException {
        logger.trace("getAllUserNames");
        return null;
    }

    @Override
    public boolean isAdmin(String arg0) throws FtpException {
        logger.trace("isAdmin: {}", arg0);
        return false;
    }

    @Override
    public void save(User arg0) throws FtpException {
        logger.trace("save: {}", arg0);
    }
}
