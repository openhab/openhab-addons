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
package org.openhab.binding.folderwatcher.internal.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * The {@link WatcherCommon} class contains commonly used methods.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
public class WatcherCommon {

    public ArrayList<String> InitStorage(File file) throws IOException {

        ArrayList<String> rerurnList = new ArrayList<String>();

        try {
            if (!file.exists()) {

                Files.createDirectories(file.toPath().getParent());

                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write("INIT");

                fileWriter.close();
            } else {

                rerurnList = (ArrayList<String>) Files.readAllLines(file.toPath().toAbsolutePath());

            }
        } catch (IOException e) {
            throw e;
        }

        return rerurnList;
    }

    public ArrayList<String> listNewFiles(ArrayList<String> oldList, ArrayList<String> newList, File listingFile)
            throws IOException {

        ArrayList<String> diffFtpListing = new ArrayList<String>();

        if (!newList.isEmpty()) {
            diffFtpListing = (ArrayList<String>) newList.clone();
            diffFtpListing.removeAll(oldList);
        }

        FileWriter fileWriter = new FileWriter(listingFile);

        for (String newFtpFile : newList) {
            fileWriter.write(newFtpFile + "\n");
        }
        fileWriter.close();

        return diffFtpListing;
    }

}
