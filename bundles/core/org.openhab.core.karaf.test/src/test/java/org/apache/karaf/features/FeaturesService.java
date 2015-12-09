/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.apache.karaf.features;

public interface FeaturesService {

    void installFeature(String name) throws Exception;

    void installFeature(String name, String version) throws Exception;

    void uninstallFeature(String name) throws Exception;

    void uninstallFeature(String name, String version) throws Exception;

    Feature[] listFeatures() throws Exception;

    Feature[] listInstalledFeatures() throws Exception;

    boolean isInstalled(Feature f);

    Feature getFeature(String name, String version) throws Exception;

    Feature getFeature(String name) throws Exception;

}