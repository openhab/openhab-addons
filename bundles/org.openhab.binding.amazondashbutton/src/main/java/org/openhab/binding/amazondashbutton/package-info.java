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
@Requirement(namespace = ExtenderNamespace.EXTENDER_NAMESPACE, filter = "(osgi.extender=osgi.serviceloader.registrar)")
@Requirement(namespace = ExtenderNamespace.EXTENDER_NAMESPACE, filter = "(&(osgi.extender=osgi.serviceloader.processor)(version>=1.0)(!(version>=2.0)))")
@Requirement(namespace = "osgi.serviceloader", filter = "(osgi.serviceloader=org.pcap4j.packet.factory.PacketFactoryBinderProvider)", cardinality = Cardinality.MULTIPLE)
@Capability(namespace = "osgi.serviceloader", name = "org.pcap4j.packet.factory.PacketFactoryBinderProvider")
package org.openhab.binding.amazondashbutton;

import org.osgi.annotation.bundle.Capability;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.annotation.bundle.Requirement.Cardinality;
import org.osgi.namespace.extender.ExtenderNamespace;
/**
 * Additional information for AmazonDashButton package
 *
 * @author Jan N. Klug - Initial contribution
 *
 */
