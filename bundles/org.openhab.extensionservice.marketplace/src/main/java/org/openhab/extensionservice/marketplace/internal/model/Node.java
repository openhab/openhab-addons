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
package org.openhab.extensionservice.marketplace.internal.model;

import java.util.Set;

/**
 * A node represents an entry on the marketplace.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class Node {

    public String id;

    public String name;

    public Integer favorited;

    public Integer installsTotal;

    public Integer installsRecent;

    public Set<String> tags;

    public String shortdescription;

    public String body;

    public Long created;

    public Long changed;

    public String image;

    public String license;

    public String companyname;

    public String status;

    public String version;

    public String supporturl;

    public String packagetypes;

    public String packageformat;

    public String updateurl;
}
