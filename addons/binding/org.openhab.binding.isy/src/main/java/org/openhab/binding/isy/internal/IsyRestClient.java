package org.openhab.binding.isy.internal;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openhab.binding.isy.rest.NodeResponseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public IsyRestClient(String url, String userName, String password, ISYModelChangeListener listener) {
        String usernameAndPassword = userName + ":" + password;
        this.authorizationHeaderValue = "Basic "
                + java.util.Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());

        this.isyClient = ClientBuilder.newClient();
        this.isyTarget = isyClient.target("http://" + url + "/rest");
        this.nodesTarget = isyTarget.path(NODES).register(NodeResponseInterceptor.class);
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

    public List<Program> getPrograms(String programId) {
        return programsTarget.path(programId).request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.APPLICATION_XML).get(new GenericType<List<Program>>() {
                });
    }

    @Override
    public List<Node> getNodes() {
        return nodesTarget.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.APPLICATION_XML).get(new GenericType<List<Node>>() {
                });
    }

    private Variable testGetIntVar(String variableId) {
        return integerVariablesTarget.path(variableId).request()
                .header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue).accept(MediaType.APPLICATION_XML)
                .get(Variable.class);

    }

    private void dumpNodes() {
        String nodes = testGetString(nodesTarget);
        System.out.println(nodes);
    }

    private void dumpStatus() {
        String nodes = testGetString(statusTarget);
        System.out.println(nodes);
    }

    public void doTests() {
        System.out.println("programs text value: " + testGetString(programsTarget));
        List<Program> returnValue = getPrograms("0045");
        System.out.println("text value: " + returnValue);

        System.out.println("vars text value: " + testGetString(integerVariablesTarget.path("1")));
        Variable vars = testGetIntVar("1");
        System.out.println("text value: " + vars);

        System.out.println("Dumping status");
        dumpStatus();
        System.out.println("Dumping nodes");
        dumpNodes();

        List<Node> theNodes = getNodes();
        System.out.println("Nodes count: " + theNodes.size());

        for (Node node : theNodes) {
            System.out.println(node);
        }
    }

    public static void main(String[] args) {
        // IsyRestClient test = new IsyRestClient();
        // test.doTests();

    }

}
