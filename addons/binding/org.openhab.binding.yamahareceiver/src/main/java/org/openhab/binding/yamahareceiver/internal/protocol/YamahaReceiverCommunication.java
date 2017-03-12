/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openhab.binding.yamahareceiver.internal.YamahaReceiverState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Yamaha Receiver protocol class used to control a yamaha receiver with HTTP/XML.
 * No state will be saved here, but in {@link YamahaReceiverState} instead.
 *
 * @author David Gräff <david.graeff@tu-dortmund.de>
 * @author Eric Thill
 * @author Ben Jones
 * @since 1.6.0
 */
public class YamahaReceiverCommunication {
    /**
     * The names of this enum are part of the protocol!
     * Receivers have different capabilities, some have 2 zones, some up to 4.
     * Every receiver has a "Main_Zone".
     */
    public enum Zone {
        Main_Zone,
        Zone_2,
        Zone_3,
        Zone_4;
    }

    /**
     * The volume min and max is the same for all supported devices.
     */
    public static final int VOLUME_MIN = -80;
    public static final int VOLUME_MAX = 12;
    public static final int VOLUME_RANGE = -VOLUME_MIN + VOLUME_MAX;

    // We need a lot of xml parsing. Create a document builder beforehand.
    private final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    // The address of the receiver.
    private final String host;
    private final Zone zone;

    // Creates a yamaha protol connection object.
    // All commands always refer to a zone. A protocol connection object
    // therefore consists of a host address and a zone.
    public YamahaReceiverCommunication(String host, Zone zone) {
        this.host = host;
        this.zone = zone;
    }

    /**
     * Return the host address
     */
    public String getHost() {
        return host;
    }

    /**
     * Return the zone
     */
    public Zone getZone() {
        return zone;
    }

    public void updateDeviceInformation(YamahaReceiverState state) throws IOException {
        Document doc = postAndGetXmlResponse(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"GET\"><System><Config>GetParam</Config></System></YAMAHA_AV>");
        if (doc != null) {
            Node basicStatus = getNode(doc.getFirstChild(), "System/Config");

            Node node;
            String value;

            node = getNode(basicStatus, "Model_Name");
            value = node != null ? node.getTextContent() : "";
            state.name = value;

            node = getNode(basicStatus, "System_ID");
            value = node != null ? node.getTextContent() : "";
            state.id = value;

            node = getNode(basicStatus, "Version");
            value = node != null ? node.getTextContent() : "";
            state.version = value;

            state.additional_zones.clear();

            node = getNode(basicStatus, "Feature_Existence");
            if (node != null) {
                Node subnode;
                subnode = getNode(node, "Zone_2");
                value = subnode != null ? subnode.getTextContent() : null;
                if (value != null && (value.equals("1") || value.equals("Available"))) {
                    state.additional_zones.add(Zone.Zone_2);
                }
                subnode = getNode(node, "Zone_3");
                value = subnode != null ? subnode.getTextContent() : null;
                if (value != null && (value.equals("1") || value.equals("Available"))) {
                    state.additional_zones.add(Zone.Zone_3);
                }
                subnode = getNode(node, "Zone_4");
                value = subnode != null ? subnode.getTextContent() : null;
                if (value != null && (value.equals("1") || value.equals("Available"))) {
                    state.additional_zones.add(Zone.Zone_4);
                }
            }
        }
    }

    public void setPower(boolean on) throws IOException {
        if (on) {
            postAndGetResponse("<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"PUT\"><" + getZone()
                    + "><Power_Control><Power>On</Power></Power_Control></" + getZone() + "></YAMAHA_AV>");
        } else {
            postAndGetResponse("<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"PUT\"><" + getZone()
                    + "><Power_Control><Power>Standby</Power></Power_Control></" + getZone() + "></YAMAHA_AV>");
        }
    }

    public void setVolume(float volume) throws IOException {
        int vol = (int) volume * 10;
        postAndGetResponse("<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"PUT\"><" + getZone()
                + "><Volume><Lvl><Val>" + String.valueOf(vol) + "</Val><Exp>1</Exp><Unit>dB</Unit></Lvl></Volume></"
                + getZone() + "></YAMAHA_AV>");
    }

    public void setMute(boolean mute) throws IOException {
        if (mute) {
            postAndGetResponse("<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"PUT\"><" + getZone()
                    + "><Volume><Mute>On</Mute></Volume></" + getZone() + "></YAMAHA_AV>");
        } else {
            postAndGetResponse("<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"PUT\"><" + getZone()
                    + "><Volume><Mute>Off</Mute></Volume></" + getZone() + "></YAMAHA_AV>");
        }
    }

    public void setInput(String name) throws IOException {
        postAndGetResponse("<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"PUT\"><" + getZone()
                + "><Input><Input_Sel>" + name + "</Input_Sel></Input></" + getZone() + "></YAMAHA_AV>");
    }

    public void setSurroundProgram(String name) throws IOException {
        postAndGetResponse("<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"PUT\"><" + getZone()
                + "><Surround><Program_Sel><Current><Sound_Program>" + name
                + "</Sound_Program></Current></Program_Sel></Surround></" + getZone() + "></YAMAHA_AV>");
    }

    public void setNetRadio(int lineNo) throws IOException {
        /* Jump to specified line in preset list */
        postAndGetResponse(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"PUT\"><NET_RADIO><Play_Control><Preset><Preset_Sel>"
                        + lineNo + "</Preset_Sel></Preset></Play_Control></NET_RADIO></YAMAHA_AV>");
    }

    public void updateState(YamahaReceiverState state) throws IOException {
        Document doc = postAndGetXmlResponse("<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"GET\"><" + zone
                + "><Basic_Status>GetParam</Basic_Status></" + zone + "></YAMAHA_AV>");
        Node basicStatus = getNode(doc.getFirstChild(), "" + zone + "/Basic_Status");

        Node node;
        String value;

        node = getNode(basicStatus, "Power_Control/Power");
        value = node != null ? node.getTextContent() : "";
        state.power = "On".equalsIgnoreCase(value);

        node = getNode(basicStatus, "Input/Input_Sel");
        value = node != null ? node.getTextContent() : "";
        state.input = value;

        node = getNode(basicStatus, "Surround/Program_Sel/Current/Sound_Program");
        value = node != null ? node.getTextContent() : "";
        state.surroundProgram = value;

        node = getNode(basicStatus, "Volume/Lvl/Val");
        value = node != null ? node.getTextContent() : String.valueOf(VOLUME_MIN);
        state.volume = Float.parseFloat(value) * .1f; // in DB
        state.volume = (state.volume + -YamahaReceiverCommunication.VOLUME_MIN) * 100.0f
                / YamahaReceiverCommunication.VOLUME_RANGE; // in percent

        node = getNode(basicStatus, "Volume/Mute");
        value = node != null ? node.getTextContent() : "";
        state.mute = "On".equalsIgnoreCase(value);

        node = getNode(basicStatus, "Input/Input_Sel_Item_Info/Src_Number");
        value = node != null ? node.getTextContent() : "0";
        state.netRadioChannel = Integer.parseInt(value);
    }

    public void updateInputsList(YamahaReceiverState state) throws IOException {
        Document doc = postAndGetXmlResponse("<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"GET\"><" + zone
                + "><Input><Input_Sel_Item>GetParam</Input_Sel_Item></Input></" + zone + "></YAMAHA_AV>");
        Node inputSelItem = getNode(doc.getFirstChild(), zone + "/Input/Input_Sel_Item");
        NodeList items = inputSelItem.getChildNodes();
        state.inputNames.clear();
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String name = item.getElementsByTagName("Param").item(0).getTextContent();
            boolean writable = item.getElementsByTagName("RW").item(0).getTextContent().contains("W");
            if (writable) {
                state.inputNames.add(name);
            }
        }
    }

    private static Node getNode(Node root, String nodePath) {
        String[] nodePathArr = nodePath.split("/");
        return getNode(root, nodePathArr, 0);
    }

    private static Node getNode(Node parent, String[] nodePath, int offset) {
        if (parent == null) {
            return null;
        }
        if (offset < nodePath.length - 1) {
            return getNode(((Element) parent).getElementsByTagName(nodePath[offset]).item(0), nodePath, offset + 1);
        } else {
            return ((Element) parent).getElementsByTagName(nodePath[offset]).item(0);
        }
    }

    private Document postAndGetXmlResponse(String message) throws IOException {
        String response = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + postAndGetResponse(message);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(response)));
            if (doc.getFirstChild().hasChildNodes() == false) {
                throw new IOException("Could not handle response");
            }
            return doc;
        } catch (Exception e) {
            throw new IOException("Could not handle response", e);
        }
    }

    private String postAndGetResponse(String message) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://" + host + "/YamahaRemoteControl/ctrl");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(message.length()));

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(message);
            wr.flush();
            wr.close();

            // Read response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            throw new IOException("Could not handle http post", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
