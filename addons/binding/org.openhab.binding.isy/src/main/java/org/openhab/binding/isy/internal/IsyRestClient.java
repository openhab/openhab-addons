package org.openhab.binding.isy.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // TODO should support startup, shutdown lifecycle
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

    public List<Program> getPrograms(String parentId) {
        return programsTarget.path(parentId).request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.APPLICATION_XML).get(new GenericType<List<Program>>() {
                });
    }

    private void recursiveGetPrograms(String id, Set<Program> programs) {

        WebTarget endPoint = this.programsTarget;
        // add id if we have it
        if (id != null) {
            endPoint = endPoint.path(id);
        }
        logger.debug("Programs rest endpoint as string"
                + endPoint.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                        .accept(MediaType.APPLICATION_XML).get(String.class));
        List<Program> programsList = endPoint.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.APPLICATION_XML).get(new GenericType<List<Program>>() {
                });
        for (Program aProgram : programsList) {
            if ("true".equals(aProgram.folder)) {
                // recurse. first return value of rest call is same id, so skip that one
                if (!aProgram.id.equals(id)) {
                    recursiveGetPrograms(aProgram.id, programs);
                }
            } else {
                programs.add(aProgram);
            }

        }
    }

    @Override
    public Set<Program> getPrograms() {
        Set<Program> returnValue = new HashSet<Program>();
        recursiveGetPrograms(null, returnValue);
        return returnValue;
    }

    public Program getProgram(String programId) {
        return programsTarget.path(programId).request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.APPLICATION_XML).get(Program.class);
    }

    @Override
    public List<Node> getNodes() {
        return nodesTarget.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.TEXT_XML).get(new GenericType<List<Node>>() {
                });
    }

    @Override
    public List<Variable> getVariables() {
        // stateVariablesTarget
        String variables = integerVariablesTarget.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.TEXT_XML).get(String.class);
        logger.debug("variables string is: " + variables);
        List<Variable> integerVariables = integerVariablesTarget.request()
                .header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue).accept(MediaType.TEXT_XML)
                .get(new GenericType<List<Variable>>() {
                });

        List<Variable> stateVariables = stateVariablesTarget.request()
                .header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue).accept(MediaType.TEXT_XML)
                .get(new GenericType<List<Variable>>() {
                });
        List<Variable> returnValue = new ArrayList<Variable>();
        returnValue.addAll(integerVariables);
        returnValue.addAll(stateVariables);
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
        System.out.println("programs text value: " + testGetString(programsTarget));
        // List<Program> returnValue = getPrograms("0045");
        // System.out.println("text value: " + returnValue);

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

    @Override
    public boolean changeVariableState(String type, String id, int value) {
        // TODO Auto-generated method stub
        logger.warn("Unimplemented change variable state");
        return false;
    }

}
