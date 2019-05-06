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
package org.openhab.binding.lutron.internal.xml;

import java.io.File;
import java.io.Reader;
import java.net.URL;

import org.openhab.binding.lutron.internal.discovery.project.Area;
import org.openhab.binding.lutron.internal.discovery.project.Component;
import org.openhab.binding.lutron.internal.discovery.project.Device;
import org.openhab.binding.lutron.internal.discovery.project.DeviceGroup;
import org.openhab.binding.lutron.internal.discovery.project.GreenMode;
import org.openhab.binding.lutron.internal.discovery.project.Output;
import org.openhab.binding.lutron.internal.discovery.project.Project;
import org.openhab.binding.lutron.internal.discovery.project.Timeclock;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * The {@link DbXmlInfoReader} reads Lutron XML project files and converts them to {@link Project}
 * objects describing the device things contained within the Lutron system.
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Added support for reading XML from a file. Requires using XStream directly
 *         instead of XmlDocumentReader.
 */
public class DbXmlInfoReader {

    private XStream xstream;
    
    public DbXmlInfoReader() {
        StaxDriver driver = new StaxDriver();

        xstream = new XStream(driver);
        
        setClassLoader(Project.class.getClassLoader());
        registerAliases(this.xstream);
    }

    public void setClassLoader(ClassLoader classLoader) {
        xstream.setClassLoader(classLoader);
    }
    
    public void registerAliases(XStream xstream) {
        xstream.alias("Project", Project.class);
        xstream.aliasField("AppVer", Project.class, "appVersion");
        xstream.aliasField("XMLVer", Project.class, "xmlVersion");
        xstream.aliasField("Areas", Project.class, "areas");
        xstream.aliasField("Timeclocks", Project.class, "timeclocks");
        xstream.aliasField("GreenModes", Project.class, "greenmodes");

        xstream.alias("Area", Area.class);
        xstream.aliasField("Name", Area.class, "name");
        xstream.useAttributeFor(Area.class, "name");
        xstream.aliasField("DeviceGroups", Area.class, "deviceNodes");
        xstream.aliasField("Outputs", Area.class, "outputs");
        xstream.aliasField("Areas", Area.class, "areas");

        xstream.alias("DeviceGroup", DeviceGroup.class);
        xstream.aliasField("Name", DeviceGroup.class, "name");
        xstream.useAttributeFor(DeviceGroup.class, "name");
        xstream.aliasField("Devices", DeviceGroup.class, "devices");

        xstream.alias("Device", Device.class);
        xstream.aliasField("Name", Device.class, "name");
        xstream.useAttributeFor(Device.class, "name");
        xstream.aliasField("IntegrationID", Device.class, "integrationId");
        xstream.useAttributeFor(Device.class, "integrationId");
        xstream.aliasField("DeviceType", Device.class, "type");
        xstream.useAttributeFor(Device.class, "type");
        xstream.aliasField("Components", Device.class, "components");

        xstream.alias("Component", Component.class);
        xstream.aliasField("ComponentNumber", Component.class, "componentNumber");
        xstream.useAttributeFor(Component.class, "componentNumber");
        xstream.aliasField("ComponentType", Component.class, "type");
        xstream.useAttributeFor(Component.class, "type");

        xstream.alias("Output", Output.class);
        xstream.aliasField("Name", Output.class, "name");
        xstream.useAttributeFor(Output.class, "name");
        xstream.aliasField("IntegrationID", Output.class, "integrationId");
        xstream.useAttributeFor(Output.class, "integrationId");
        xstream.aliasField("OutputType", Output.class, "type");
        xstream.useAttributeFor(Output.class, "type");

        xstream.alias("Timeclock", Timeclock.class);
        xstream.aliasField("Name", Timeclock.class, "name");
        xstream.useAttributeFor(Timeclock.class, "name");
        xstream.aliasField("IntegrationID", Timeclock.class, "integrationId");
        xstream.useAttributeFor(Timeclock.class, "integrationId");

        xstream.alias("GreenMode", GreenMode.class);
        xstream.aliasField("Name", GreenMode.class, "name");
        xstream.useAttributeFor(GreenMode.class, "name");
        xstream.aliasField("IntegrationID", GreenMode.class, "integrationId");
        xstream.useAttributeFor(GreenMode.class, "integrationId");

        // This reader is only interested in device thing information and does not read
        // everything contained in DbXmlInfo. Ignoring unknown elements also makes the
        // binding more tolerant of potential future changes to the XML schema.
        xstream.ignoreUnknownElements();
    }
    
    public Project readFromXML(URL xmlURL) throws ConversionException {
        if (xmlURL != null) {
            return (Project) xstream.fromXML(xmlURL);
        }

        return null;
    }
    
    public Project readFromXML(File xmlFile) throws ConversionException {
        if (xmlFile != null) {
            return (Project) xstream.fromXML(xmlFile);
        }

        return null;
    }
    
    public Project readFromXML(Reader xmlReader) throws ConversionException {
        if (xmlReader != null) {
            return (Project) xstream.fromXML(xmlReader);
        }

        return null;
    }
    
    public Project readFromXML(String xmlString) throws ConversionException {
        if (xmlString != null) {
            return (Project) xstream.fromXML(xmlString);
        }

        return null;
    }
}
