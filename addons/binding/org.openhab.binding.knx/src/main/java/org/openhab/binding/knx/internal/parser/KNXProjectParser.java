/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.knx.internal.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;

/**
 * The {@link KNXProjectParser} is an interface that needs to be
 * implemented by classes that want to parse knxproject files
 *
 * @author Karel Goderis - Initial contribution
 */
public interface KNXProjectParser {

    /**
     *
     * Add a Collection of named XML strings to the parser
     *
     * @param xmlRepository - a Map containing {name,content} tuples w
     *
     */
    public void addXML(HashMap<String, String> xmlRepository);

    /**
     *
     * Add a names XML string to the parser
     *
     * @param name - a name to identify the XML string added
     * @param content - the XML content
     *
     */
    public void addXML(String name, String content);

    /**
     *
     * A method to post process the parsed content at the end of the parsing process. This method should be implemented
     * if there is a need to create links between data structures
     *
     */
    public void postProcess();

    /**
     *
     * Returns a list of all the Individual Addresses (in x.y.z format) that are contained in all the XML strings
     * provided to the parser
     *
     */
    public Set<String> getIndividualAddresses();

    /**
     *
     * Returns a list of all the Group Addresses (in x/y/z format) that are used by the given Individual Address. These
     * are in fact the Group Addresses used by all the Communication Objects of the KNX Actor identified by the
     * Individual Address
     *
     * @param individualAddress - the Individual Address to get the Group Addresses for
     */
    public Set<String> getGroupAddresses(String individualAddress);

    /**
     *
     * Returns the Datapoint (in x.yyyy format) of the given Group Address
     *
     * @param groupAddress - the Group Address to get the DPT for
     */
    public String getDPT(String groupAddress);

    /**
     *
     * Returns a 'filled' Configuration for the provided Group Address and Individual Address. This Configuration is
     * used to configure the Channel that will be created for the Group Address
     *
     * @param groupAddress - the Group Address to get a Configuration for
     * @param individualAddress - the Individual Address
     */
    public Configuration getGroupAddressConfiguration(String groupAddress, String individualAddress);

    /**
     *
     * Returns a 'filled' Configuration for KNX actor identified by the provided Individual Address. This Configuration
     * is used to configure the Thing that will be created for this Individual Address/KNX device
     *
     * @param individualAddress - the Individual Address to get a Configuration for
     */
    public Configuration getDeviceConfiguration(String individualAddress);

    /**
     *
     * Returns a 'filled' properties map for KNX actor identified by the provided Individual Address. This map is used
     * to enrich the Thing that will be created for this Individual Address/KNX device
     *
     * @param individualAddress - the Individual Address to get a properties map for
     */
    public Map<String, String> getDeviceProperties(String individualAddress);

}
