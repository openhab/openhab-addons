package org.openhab.binding.isy.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class IsyRestClient implements OHIsyClient {
    private Logger logger = LoggerFactory.getLogger(IsyRestClient.class);
    public static final String NODES = "nodes";
    public static final String PROGRAMS = "programs";
    public static final String VARIABLES = "vars/get";
    public static final String SCENES = "scenes";
    public static final String STATUS = "status";
    public static final String VAR_INTEGER_TYPE = "1";
    public static final String VAR_STATE_TYPE = "2";

    private static String AUTHORIZATIONHEADERNAME = "Authorization";
    String authorizationHeaderValue;

    // REST Client API variables
    protected Client isyClient;
    protected WebTarget isyTarget;
    protected WebTarget nodesTarget;
    protected WebTarget programsTarget;
    protected WebTarget scenesTarget;
    protected WebTarget statusTarget;
    protected WebTarget integerVariablesTarget;
    protected WebTarget stateVariablesTarget;
    private IsyWebSocketSubscription isySubscription;

    // TODO should support startup, shutdown lifecycle
    public IsyRestClient(String url, String userName, String password, ISYModelChangeListener listener) {
        String usernameAndPassword = userName + ":" + password;
        this.authorizationHeaderValue = "Basic "
                + java.util.Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());

        this.isyClient = ClientBuilder.newClient();
        this.isyClient.register(Variable.class);
        this.isyTarget = isyClient.target("http://" + url + "/rest");
        this.nodesTarget = isyTarget.path(NODES);// .register(NodeResponseInterceptor.class);
        this.programsTarget = isyTarget.path(PROGRAMS);
        this.scenesTarget = nodesTarget.path(SCENES);
        this.statusTarget = isyTarget.path(STATUS);
        this.integerVariablesTarget = isyTarget.path(VARIABLES).path(VAR_INTEGER_TYPE);
        this.stateVariablesTarget = isyTarget.path(VARIABLES).path(VAR_STATE_TYPE);
        isySubscription = new IsyWebSocketSubscription(url, authorizationHeaderValue, listener);
    }

    @Override
    public boolean changeNodeState(String command, String value, String address) {
        Builder changeNodeTarget = nodesTarget.path(address).path("cmd").path(command).path(value).request()
                .header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue);
        Response result = changeNodeTarget.get();
        return result.getStatus() == 200;
    }

    private String testGetString(WebTarget endpoint) {
        return endpoint.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue).get(String.class);
    }

    @Override
    public Collection<Program> getPrograms() {
        List<Program> returnValue = new ArrayList<Program>();
        String variables = programsTarget.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.TEXT_XML).get(String.class);
        System.out.println("nodes xml: " + variables);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = domFactory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(variables)));

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            XPathExpression expr = xpath.compile("//program");
            NodeList list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                org.w3c.dom.Node node = list.item(i);

                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element firstElement = (Element) node;
                    String id = firstElement.getAttribute("id");
                    String folder = firstElement.getAttribute("folder");
                    String name = getValue(firstElement, "name");
                    if (!"true".equals(folder)) {
                        returnValue.add(new Program(id, name));
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return returnValue;
    }

    public Program getProgram(String programId) {
        return programsTarget.path(programId).request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.APPLICATION_XML).get(Program.class);
    }

    @Override
    public List<Node> getNodes() {
        List<Node> returnValue = new ArrayList<Node>();
        String variables = nodesTarget.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.TEXT_XML).get(String.class);
        System.out.println("nodes xml: " + variables);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = domFactory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(variables)));

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            XPathExpression expr = xpath.compile("//node");
            NodeList list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                org.w3c.dom.Node node = list.item(i);

                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element firstElement = (Element) node;
                    String name = getValue(firstElement, "name");
                    String address = getValue(firstElement, "address");
                    String type = getValue(firstElement, "type");
                    returnValue.add(new Node(name, address, type));
                }
            }
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return returnValue;
    }

    private String getValue(Element firstElement, String valueName) {
        NodeList firstNameList = firstElement.getElementsByTagName(valueName);
        Element firstNameElement = (Element) firstNameList.item(0);
        NodeList textFNList = firstNameElement.getChildNodes();
        String name = textFNList.item(0).getNodeValue().trim();
        return name;
    }

    @Override
    public List<Variable> getVariables() {
        // stateVariablesTarget

        List<Variable> integerVariables = getVariables(integerVariablesTarget);
        List<Variable> stateVariables = getVariables(stateVariablesTarget);
        String variables = integerVariablesTarget.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.TEXT_XML).get(String.class);
        logger.debug("variables string is: " + variables);

        List<Variable> returnValue = new ArrayList<Variable>();
        returnValue.addAll(integerVariables);
        returnValue.addAll(stateVariables);
        return returnValue;
    }

    private List<Variable> getVariables(WebTarget target) {
        List<Variable> returnValue = new ArrayList<Variable>();
        String variables = target.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.TEXT_XML).get(String.class);
        System.out.println("variables xml: " + variables);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = domFactory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(variables)));

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            XPathExpression expr = xpath.compile("//var");
            NodeList list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                org.w3c.dom.Node node = list.item(i);

                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element firstElement = (Element) node;
                    String id = firstElement.getAttribute("id");
                    String type = firstElement.getAttribute("type");
                    System.out.println("id (el) :" + id);
                    System.out.println("type (el) :" + type);
                    NodeList firstNameList = firstElement.getElementsByTagName("val");
                    Element firstNameElement = (Element) firstNameList.item(0);
                    NodeList textFNList = firstNameElement.getChildNodes();
                    int value = Integer.parseInt(textFNList.item(0).getNodeValue().trim());
                    System.out.println("value : " + value);
                    returnValue.add(new Variable(id, type, value));
                }

                System.out.println(node.getTextContent());
            }
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return returnValue;
    }

    @Override
    public List<Scene> getScenes() {
        List<Scene> returnValue = new ArrayList<Scene>();
        String variables = scenesTarget.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.TEXT_XML).get(String.class);
        System.out.println("scenes xml: " + variables);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = domFactory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(variables)));

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            XPathExpression expr = xpath.compile("//group[@flag=132]");
            NodeList list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                org.w3c.dom.Node node = list.item(i);

                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element firstElement = (Element) node;

                    String name = getValue(firstElement, "name");
                    String address = getValue(firstElement, "address");

                    returnValue.add(new Scene(name, address));
                }

                System.out.println(node.getTextContent());
            }
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return returnValue;
    }

    private Variable testGetIntVar(String variableId) {
        return integerVariablesTarget.path(variableId).request()
                .header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue).accept(MediaType.APPLICATION_XML)
                .get(Variable.class);

    }

    private void dumpNodes() {
        String nodes = testGetString(nodesTarget);
    }

    private void dumpStatus() {
        String nodes = testGetString(statusTarget);
        System.out.println(nodes);
    }

    public void doTests() {
        getScenes();
        getPrograms();
        System.out.println("Dumping nodes");
        dumpNodes();

        List<Node> theNodes = getNodes();
        System.out.println("Nodes count: " + theNodes.size());

        for (Node node : theNodes) {
            System.out.println(node);
        }

        System.out.println("programs text value: " + testGetString(programsTarget));
        // List<Program> returnValue = getPrograms("0045");
        // System.out.println("text value: " + returnValue);

        System.out.println("vars text value: " + testGetString(integerVariablesTarget.path("1")));
        Variable vars = testGetIntVar("1");
        System.out.println("text value: " + vars);

        System.out.println("Dumping status");
        dumpStatus();
    }

    public static void main(String[] args) {
        IsyRestClient test = new IsyRestClient("192.168.0.211", args[0], args[1], null);
        test.doTests();

    }

    @Override
    public boolean changeVariableState(String type, String id, int value) {
        // TODO Auto-generated method stub
        logger.warn("Unimplemented change variable state");
        return false;
    }

    @Override
    public boolean changeSceneState(String address, int value) {
        String cmd = null;
        if (value == 255) {
            cmd = "DON";
        } else if (value == 0) {
            cmd = "DOF";
        }

        if (cmd != null) {
            Builder changeNodeTarget = nodesTarget.path(address).path("cmd").path(cmd).request()
                    .header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue);
            Response result = changeNodeTarget.get();
            return result.getStatus() == 200;
        }
        return false;
    }

}
