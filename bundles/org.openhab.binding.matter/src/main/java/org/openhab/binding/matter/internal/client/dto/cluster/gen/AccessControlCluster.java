/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * AccessControl
 *
 * @author Dan Cunningham - Initial contribution
 */
public class AccessControlCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x001F;
    public static final String CLUSTER_NAME = "AccessControl";
    public static final String CLUSTER_PREFIX = "accessControl";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_ACL = "acl";
    public static final String ATTRIBUTE_EXTENSION = "extension";
    public static final String ATTRIBUTE_SUBJECTS_PER_ACCESS_CONTROL_ENTRY = "subjectsPerAccessControlEntry";
    public static final String ATTRIBUTE_TARGETS_PER_ACCESS_CONTROL_ENTRY = "targetsPerAccessControlEntry";
    public static final String ATTRIBUTE_ACCESS_CONTROL_ENTRIES_PER_FABRIC = "accessControlEntriesPerFabric";
    public static final String ATTRIBUTE_COMMISSIONING_ARL = "commissioningArl";
    public static final String ATTRIBUTE_ARL = "arl";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * An attempt to add an Access Control Entry when no more entries are available shall result in a RESOURCE_EXHAUSTED
     * error being reported and the ACL attribute shall NOT have the entry added to it. See access control limits.
     * See the AccessControlEntriesPerFabric attribute for the actual value of the number of entries per fabric
     * supported by the server.
     * Each Access Control Entry codifies a single grant of privilege on this Node, and is used by the Access Control
     * Privilege Granting algorithm to determine if a subject has privilege to interact with targets on the Node.
     */
    public List<AccessControlEntryStruct> acl; // 0 list RW F A
    /**
     * If present, the Access Control Extensions may be used by Administrators to store arbitrary data related to
     * fabric’s Access Control Entries.
     * The Access Control Extension list shall support a single extension entry per supported fabric.
     */
    public List<AccessControlExtensionStruct> extension; // 1 list RW F A
    /**
     * This attribute shall provide the minimum number of Subjects per entry that are supported by this server.
     * Since reducing this value over time may invalidate ACL entries already written, this value shall NOT decrease
     * across time as software updates occur that could impact this value. If this is a concern for a given
     * implementation, it is recommended to only use the minimum value required and avoid reporting a higher value than
     * the required minimum.
     */
    public Integer subjectsPerAccessControlEntry; // 2 uint16 R V
    /**
     * This attribute shall provide the minimum number of Targets per entry that are supported by this server.
     * Since reducing this value over time may invalidate ACL entries already written, this value shall NOT decrease
     * across time as software updates occur that could impact this value. If this is a concern for a given
     * implementation, it is recommended to only use the minimum value required and avoid reporting a higher value than
     * the required minimum.
     */
    public Integer targetsPerAccessControlEntry; // 3 uint16 R V
    /**
     * This attribute shall provide the minimum number of ACL Entries per fabric that are supported by this server.
     * Since reducing this value over time may invalidate ACL entries already written, this value shall NOT decrease
     * across time as software updates occur that could impact this value. If this is a concern for a given
     * implementation, it is recommended to only use the minimum value required and avoid reporting a higher value than
     * the required minimum.
     */
    public Integer accessControlEntriesPerFabric; // 4 uint16 R V
    /**
     * This attribute shall provide the set of CommissioningAccessRestrictionEntryStruct applied during commissioning on
     * a managed device.
     * When present, the CommissioningARL attribute shall indicate the access restrictions applying during
     * commissioning.
     * Attempts to access data model elements described by an entry in the CommissioningARL attribute during
     * commissioning shall result in an error of ACCESS_RESTRICTED. See Access Control Model for more information about
     * the features related to controlling access to a Node’s Endpoint Clusters (&quot;Targets&quot; hereafter) from
     * other Nodes.
     * See Section 9.10.4.2.1, “Managed Device Feature Usage Restrictions” for limitations on the use of access
     * restrictions.
     */
    public List<CommissioningAccessRestrictionEntryStruct> commissioningArl; // 5 list R V
    /**
     * This attribute shall provide the set of AccessRestrictionEntryStruct applied to the associated fabric on a
     * managed device.
     * When present, the ARL attribute shall indicate the access restrictions applying to the accessing fabric. In
     * contrast, the CommissioningARL attribute indicates the accessing restrictions that apply when there is no
     * accessing fabric, such as during commissioning.
     * The access restrictions are externally added/removed based on the particular relationship the device hosting this
     * server has with external entities such as its owner, external service provider, or end-user.
     * Attempts to access data model elements described by an entry in the ARL attribute for the accessing fabric shall
     * result in an error of ACCESS_RESTRICTED. See Access Control Model for more information about the features related
     * to controlling access to a Node’s Endpoint Clusters (&quot;Targets&quot; hereafter) from other Nodes.
     * See Section 9.10.4.2.1, “Managed Device Feature Usage Restrictions” for limitations on the use of access
     * restrictions.
     */
    public List<AccessRestrictionEntryStruct> arl; // 6 list R F V

    // Structs
    /**
     * The cluster shall generate AccessControlEntryChanged events whenever its ACL attribute data is changed by an
     * Administrator.
     * • Each added entry shall generate an event with ChangeType Added.
     * • Each changed entry shall generate an event with ChangeType Changed.
     * • Each removed entry shall generate an event with ChangeType Removed.
     */
    public static class AccessControlEntryChanged {
        /**
         * The Node ID of the Administrator that made the change, if the change occurred via a CASE session.
         * Exactly one of AdminNodeID and AdminPasscodeID shall be set, depending on whether the change occurred via a
         * CASE or PASE session; the other shall be null.
         */
        public BigInteger adminNodeId; // node-id
        /**
         * The Passcode ID of the Administrator that made the change, if the change occurred via a PASE session.
         * Non-zero values are reserved for future use (see PasscodeId generation in PBKDFParamRequest).
         * Exactly one of AdminNodeID and AdminPasscodeID shall be set, depending on whether the change occurred via a
         * CASE or PASE session; the other shall be null.
         */
        public Integer adminPasscodeId; // uint16
        /**
         * The type of change as appropriate.
         */
        public ChangeTypeEnum changeType; // ChangeTypeEnum
        /**
         * The latest value of the changed entry.
         * This field SHOULD be set if resources are adequate for it; otherwise it shall be set to NULL if resources are
         * scarce.
         */
        public AccessControlEntryStruct latestValue; // AccessControlEntryStruct
        public Integer fabricIndex; // FabricIndex

        public AccessControlEntryChanged(BigInteger adminNodeId, Integer adminPasscodeId, ChangeTypeEnum changeType,
                AccessControlEntryStruct latestValue, Integer fabricIndex) {
            this.adminNodeId = adminNodeId;
            this.adminPasscodeId = adminPasscodeId;
            this.changeType = changeType;
            this.latestValue = latestValue;
            this.fabricIndex = fabricIndex;
        }
    }

    /**
     * The cluster shall generate AccessControlExtensionChanged events whenever its extension attribute data is changed
     * by an Administrator.
     * • Each added extension shall generate an event with ChangeType Added.
     * • Each changed extension shall generate an event with ChangeType Changed.
     * • Each removed extension shall generate an event with ChangeType Removed.
     */
    public static class AccessControlExtensionChanged {
        /**
         * The Node ID of the Administrator that made the change, if the change occurred via a CASE session.
         * Exactly one of AdminNodeID and AdminPasscodeID shall be set, depending on whether the change occurred via a
         * CASE or PASE session; the other shall be null.
         */
        public BigInteger adminNodeId; // node-id
        /**
         * The Passcode ID of the Administrator that made the change, if the change occurred via a PASE session.
         * Non-zero values are reserved for future use (see PasscodeId generation in PBKDFParamRequest).
         * Exactly one of AdminNodeID and AdminPasscodeID shall be set, depending on whether the change occurred via a
         * CASE or PASE session; the other shall be null.
         */
        public Integer adminPasscodeId; // uint16
        /**
         * The type of change as appropriate.
         */
        public ChangeTypeEnum changeType; // ChangeTypeEnum
        /**
         * The latest value of the changed extension.
         * This field SHOULD be set if resources are adequate for it; otherwise it shall be set to NULL if resources are
         * scarce.
         */
        public AccessControlExtensionStruct latestValue; // AccessControlExtensionStruct
        public Integer fabricIndex; // FabricIndex

        public AccessControlExtensionChanged(BigInteger adminNodeId, Integer adminPasscodeId, ChangeTypeEnum changeType,
                AccessControlExtensionStruct latestValue, Integer fabricIndex) {
            this.adminNodeId = adminNodeId;
            this.adminPasscodeId = adminPasscodeId;
            this.changeType = changeType;
            this.latestValue = latestValue;
            this.fabricIndex = fabricIndex;
        }
    }

    /**
     * The cluster shall generate a FabricRestrictionReviewUpdate event to indicate completion of a fabric restriction
     * review. Due to the requirement to generate this event within a bound time frame of successful receipt of the
     * ReviewFabricRestrictions command, this event may include additional steps that the client may present to the user
     * in order to help the user locate the user interface for the Managed Device feature.
     */
    public static class FabricRestrictionReviewUpdate {
        /**
         * This field shall indicate the Token that can be used to correlate a ReviewFabricRestrictionsResponse with a
         * FabricRestrictionReviewUpdate event.
         */
        public BigInteger token; // uint64
        /**
         * This field shall provide human readable text that may be displayed to the user to help them locate the user
         * interface for managing access restrictions for each fabric.
         * A device SHOULD implement the Localization Configuration Cluster when it has no other means to determine the
         * locale to use for this text.
         * Examples include &quot;Please try again and immediately access device display for further instructions.&quot;
         * or &quot;Please check email associated with your Acme account.&quot;
         */
        public String instruction; // string
        /**
         * This field shall indicate the URL for the service associated with the device maker which the user can visit
         * to manage fabric limitations. The syntax of this field shall follow the syntax as specified in RFC 1738 and
         * shall use the https scheme for internet-hosted URLs.
         * • The URL may embed the token, fabric index, fabric vendor, or other information transparently in order to
         * pass context about the originating ReviewFabricRestrictions command to the service associated with the URL.
         * The service associated with the device vendor may perform vendor ID verification on the fabric from which the
         * ReviewFabricRestrictions command originated.
         * • If the device grants the request, the ARL attribute in the Access Control Cluster shall be updated to
         * reflect the new access rights and a successful response shall be returned to the device making the request
         * using the MTaer field of the callbackUrl. If the request is denied, the ARL attribute shall remain unchanged
         * and a failure response shall be returned to the device making the request using the MTaer field of the
         * callbackUrl.
         * • The device using this mechanism shall provide a service at the URL that can accept requests for additional
         * access and return responses indicating whether the requests were granted or denied.
         * • This URL will typically lead to a server which (e.g. by looking at the User-Agent) redirects the user to
         * allow viewing, downloading, installing or using a manufacturer-provided means for guiding the user through
         * the process to review and approve or deny the request. The device manufacturer may choose to use a
         * constructed URL which is valid in a HTTP GET request (i.e. dedicated for the product) such as, for example,
         * https://domain.example/arl-app?vid&#x3D;FFF1&amp; pid&#x3D;1234. If a client follows or launches the
         * ARLRequestFlowUrl, it shall expand it as described in Section 9.10.9.3.4, “ARLRequestFlowUrl format”.
         * • A manufacturer contemplating using this flow should realize that
         * ◦ This flow typically requires internet access to access the URL, and access extension may fail when internet
         * connectivity is not available.
         * ◦ If the flow prefers to redirect the user to an app which is available on popular platforms, it SHOULD also
         * provide a fallback option such as a web browser interface to ensure users can complete access extension.
         * ### ARLRequestFlowUrl format
         * The ARLRequestFlowUrl shall contain a query component (see RFC 3986 section 3.4) composed of one or more
         * key-value pairs:
         * • The query shall use the &amp; delimiter between key/value pairs.
         * • The key-value pairs shall in the format name&#x3D;&lt;value&gt; where name is the key name, and
         * &lt;value&gt; is the contents of the value encoded with proper URL-encoded escaping.
         * • If key MTcu is present, it shall have a value of &quot;_&quot; (i.e. MTcu&#x3D;_). This is the
         * &quot;callback URL (CallbackUrl) placeholder&quot;.
         * • Any key whose name begins with MT not mentioned in the previous bullets shall be reserved for future use by
         * this specification. Manufacturers shall NOT include query keys starting with MT in the ARLRequestFlowUrl
         * unless they are referenced by a version of this specification.
         * Any other element in the ARLRequestFlowUrl query field not covered by the above rules, as well as the
         * fragment field (if present), shall remain including the order of query key/value pairs present.
         * Once the URL is obtained, it shall be expanded to form a final URL (ExpandedARLRequestFlowUrl) by proceeding
         * with the following substitution algorithm on the original ARLRequestFlowUrl:
         * 1. If key MTcu is present, compute the CallbackUrl desired (see Section 9.10.9.3.5, “CallbackUrl format for
         * ARL Request Flow response”), and substitute the placeholder value &quot;_&quot; (i.e. in MTcu&#x3D;_) in the
         * ARLRequestFlowUrl with the desired contents, encoded with proper URL-encoded escaping (see RFC 3986 section
         * 2).
         * The final URL after expansion (ExpandedARLRequestFlowUrl) shall be the one to follow, rather than the
         * original value obtained from the FabricRestrictionReviewUpdate event.
         * ### CallbackUrl format for ARL Request Flow response
         * If a CallbackUrl field (i.e. MTcu&#x3D;) query field placeholder is present in the ARLRequestFlowUrl, the
         * client may replace the placeholder value &quot;_&quot; in the ExpandedARLRequestFlowUrl with a URL that the
         * manufacturer flow can use to make a smooth return to the client when the ARL flow has terminated.
         * This URL field may contain a query component (see RFC 3986 section 3.4). If a query is present, it shall be
         * composed of one or more key-value pairs:
         * • The query shall use the &amp; delimiter between key/value pairs.
         * • The key-value pairs shall follow the format name&#x3D;&lt;value&gt; where name is the key name, and
         * &lt;value&gt; is the contents of the value encoded with proper URL-encoded escaping.
         * • If key MTaer is present, it shall have a value of &quot;_&quot; (i.e. MTaer&#x3D;_). This is the
         * placeholder for a &quot;access extension response&quot; provided by the manufacturer flow to the client. The
         * manufacturer flow shall replace this placeholder with the final status of the access extension request, which
         * shall be formatted following Expansion of CallbackUrl by the manufacturer custom flow and encoded with proper
         * URL-encoded escaping.
         * • Any key whose name begins with MT not mentioned in the previous bullets shall be reserved for future use by
         * this specification.
         * Any other element in the CallbackUrl query field not covered by the above rules, as well as the fragment
         * field (if present), shall remain as provided by the client through embedding within the
         * ExpandedARLRequestFlowUrl, including the order of query key/value pairs present.
         * Once the CallbackUrl is obtained by the manufacturer flow, it may be expanded to form a final
         * ExpandedARLRequestCallbackUrl URL to be used by proceeding with the following substitution algorithm on the
         * provided CallbackUrl:
         * • If key MTaer is present, the manufacturer custom flow having received the initial query containing the
         * CallbackUrl shall substitute the placeholder value &quot;_&quot; (i.e. in MTaer&#x3D;_) in the CallbackUrl
         * with the final status of the access extension request flow which shall be one of the following. Any value
         * returned in the MTaer field not listed above shall be considered an error and shall be treated as
         * GeneralFailure.
         * ◦ Success - The flow completed successfully and the ARL attribute was updated. The client may now read the
         * ARL attribute to determine the new access restrictions.
         * ◦ NoChange - The ARL attribute was already listing minimum restrictions for the requesting fabric.
         * ◦ GeneralFailure - The flow failed for an unspecified reason.
         * ◦ FlowAuthFailure - The user failed to authenticate to the flow.
         * ◦ NotFound - Access extension failed because the target fabric was not found.
         * A manufacturer custom flow having received an ExpandedARLRequestFlowUrl SHOULD attempt to open the
         * ExpandedARLRequestCallbackUrl, on completion of the request, if an ExpandedARLRequestCallbackUrl was computed
         * from the CallbackUrl and opening such a URL is supported.
         * ### Examples of ARLRequestFlowUrl URLs
         * Below are some examples of valid ExpandedARLRequestFlowUrl for several valid values of ARLRequestFlowUrl, as
         * well as some examples of invalid values of ARLRequestFlowUrl:
         * • Invalid URL with no query string: http scheme is not allowed:
         * ◦ http://company.domain.example/matter/arl/vFFF1p1234
         * • Valid URL :
         * ◦ https://company.domain.example/matter/arl/vFFF1p1234
         * • Valid URL, CallbackUrl requested:
         * ◦ Before expansion:
         * https://company.domain.example/matter/arl?vid&#x3D;FFF1&amp;pid&#x3D;1234&amp;MTcu&#x3D;_
         * ◦ After expansion:
         * https://company.domain.example/matter/arl?vid&#x3D;FFF1&amp;pid&#x3D;1234&amp;MTcu&#x3D;https%3A%2F%2Fc
         * lient.domain.example%2Fcb%3Ftoken%3DmAsJ6_vqbr-vjDiG_w%253D%253D%26MTaer%3D_
         * ◦ The ExpandedARLRequestFlowUrl URL contains:
         * ▪ A CallbackUrl with a client-provided arbitrary token&#x3D; key/value pair and the MTaer&#x3D; key/value
         * pair place-holder to indicate support for a return access extension completion status:
         * https://client.domain.example/cb?token&#x3D;mAsJ6_vqbr-vjDiG_w%3D%3D&amp;MTaer&#x3D;_
         * ▪ After expansion of the CallbackUrl (MTcu key) into an ExpandedCallbackUrl, with an example return access
         * extension completion status of Success, the ExpandedARLRequestCallbackUrl would be:
         * https://client.domain.example/cb?token&#x3D;mAsJ6_vqbr-vjDiG_w%3D%3D&amp;MTaer&#x3D;Success
         * Note that the MTcu key/value pair was initially provided URL-encoded within the ExpandedARLRequestFlowUrl URL
         * and the MTaer&#x3D;_ key/value pair placeholder now contains a substituted returned completion status.
         * • Invalid URL, due to MTza&#x3D;79 key/value pair in reserved MT-prefixed keys reserved for future use:
         * ◦ https://company.domain.example/matter/arl?vid&#x3D;FFF1&amp;pid&#x3D;1234&amp;MTop&#x3D;_&amp;MTza&#x3D;79
         */
        public String arlRequestFlowUrl; // string
        public Integer fabricIndex; // FabricIndex

        public FabricRestrictionReviewUpdate(BigInteger token, String instruction, String arlRequestFlowUrl,
                Integer fabricIndex) {
            this.token = token;
            this.instruction = instruction;
            this.arlRequestFlowUrl = arlRequestFlowUrl;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class AccessControlTargetStruct {
        public Integer cluster; // cluster-id
        public Integer endpoint; // endpoint-no
        public Integer deviceType; // devtype-id

        public AccessControlTargetStruct(Integer cluster, Integer endpoint, Integer deviceType) {
            this.cluster = cluster;
            this.endpoint = endpoint;
            this.deviceType = deviceType;
        }
    }

    public static class AccessControlEntryStruct {
        /**
         * The privilege field shall specify the level of privilege granted by this Access Control Entry.
         * Each privilege builds upon its predecessor, expanding the set of actions that can be performed upon a Node.
         * Administer is the highest privilege, and is special as it pertains to the administration of privileges
         * itself, via the Access Control Cluster.
         * When a Node is granted a particular privilege, it is also implicitly granted all logically lower privilege
         * levels as well. The following diagram illustrates how the higher privilege levels subsume the lower privilege
         * levels:
         * Individual clusters shall define whether attributes are readable, writable, or both readable and writable.
         * Clusters also shall define which privilege is minimally required to be able to perform a particular read or
         * write action on those attributes, or invoke particular commands. Device type specifications may further
         * restrict the privilege required.
         * The Access Control Cluster shall require the Administer privilege to observe and modify the Access Control
         * Cluster itself. The Administer privilege shall NOT be used on Access Control Entries which use the Group auth
         * mode.
         */
        public AccessControlEntryPrivilegeEnum privilege; // AccessControlEntryPrivilegeEnum
        /**
         * The AuthMode field shall specify the authentication mode required by this Access Control Entry.
         */
        public AccessControlEntryAuthModeEnum authMode; // AccessControlEntryAuthModeEnum
        /**
         * The subjects field shall specify a list of Subject IDs, to which this Access Control Entry grants access.
         * Device types may impose additional constraints on the minimum number of subjects per Access Control Entry.
         * An attempt to create an entry with more subjects than the node can support shall result in a
         * RESOURCE_EXHAUSTED error and the entry shall NOT be created.
         * ### Subject ID shall be of type uint64 with semantics depending on the entry’s AuthMode as follows:
         * An empty subjects list indicates a wildcard; that is, this entry shall grant access to any Node that
         * successfully authenticates via AuthMode. The subjects list shall NOT be empty if the entry’s AuthMode is
         * PASE.
         * The PASE AuthMode is reserved for future use (see Section 6.6.2.9, “Bootstrapping of the Access Control
         * Cluster”). An attempt to write an entry with AuthMode set to PASE shall fail with a status code of
         * CONSTRAINT_ERROR.
         * For PASE authentication, the Passcode ID identifies the required passcode verifier, and shall be 0 for the
         * default commissioning passcode.
         * For CASE authentication, the Subject ID is a distinguished name within the Operational Certificate shared
         * during CASE session establishment, the type of which is determined by its range to be one of:
         * • a Node ID, which identifies the required source node directly (by ID)
         * • a CASE Authenticated Tag, which identifies the required source node indirectly (by tag)
         * For Group authentication, the Group ID identifies the required group, as defined in the Group Key Management
         * Cluster.
         */
        public List<BigInteger> subjects; // list
        /**
         * The targets field shall specify a list of AccessControlTargetStruct, which define the clusters on this Node
         * to which this Access Control Entry grants access.
         * Device types may impose additional constraints on the minimum number of targets per Access Control Entry.
         * An attempt to create an entry with more targets than the node can support shall result in a
         * RESOURCE_EXHAUSTED error and the entry shall NOT be created.
         * A single target shall contain at least one field (Cluster, Endpoint, or DeviceType), and shall NOT contain
         * both an Endpoint field and a DeviceType field.
         * A target grants access based on the presence of fields as follows:
         * An empty targets list indicates a wildcard: that is, this entry shall grant access to all cluster instances
         * on all endpoints on this Node.
         */
        public List<AccessControlTargetStruct> targets; // list
        public Integer fabricIndex; // FabricIndex

        public AccessControlEntryStruct(AccessControlEntryPrivilegeEnum privilege,
                AccessControlEntryAuthModeEnum authMode, List<BigInteger> subjects,
                List<AccessControlTargetStruct> targets, Integer fabricIndex) {
            this.privilege = privilege;
            this.authMode = authMode;
            this.subjects = subjects;
            this.targets = targets;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class AccessControlExtensionStruct {
        /**
         * This field may be used by manufacturers to store arbitrary TLV-encoded data related to a fabric’s Access
         * Control Entries.
         * The contents shall consist of a top-level anonymous list; each list element shall include a profile-specific
         * tag encoded in fully-qualified form.
         * Administrators may iterate over this list of elements, and interpret selected elements at their discretion.
         * The content of each element is not specified, but may be coordinated among manufacturers at their discretion.
         */
        public OctetString data; // octstr
        public Integer fabricIndex; // FabricIndex

        public AccessControlExtensionStruct(OctetString data, Integer fabricIndex) {
            this.data = data;
            this.fabricIndex = fabricIndex;
        }
    }

    /**
     * This structure describes an access restriction that would be applied to a specific data model element on a given
     * endpoint/cluster pair (see AccessRestrictionEntryStruct).
     */
    public static class AccessRestrictionStruct {
        /**
         * This field shall indicate the type of restriction, for example, AttributeAccessForbidden.
         */
        public AccessRestrictionTypeEnum type; // AccessRestrictionTypeEnum
        /**
         * This field shall indicate the element Manufacturer Extensible Identifier (MEI) associated with the element
         * type subject to the access restriction, based upon the AccessRestrictionTypeEnum. When the Type is
         * AttributeAccessForbidden or AttributeWriteForbidden, this value shall be considered of type attrib-id (i.e.
         * an attribute identifier). When the Type is CommandForbidden, this value shall be considered of type
         * command-id (i.e. an attribute identifier). When the Type is EventForbidden, this value shall be considered of
         * type event-id (i.e. an event identifier).
         * A null value shall indicate the wildcard value for the given value of Type (i.e. all elements associated with
         * the Type under the associated endpoint and cluster for the containing AccessRestrictionEntryStruct).
         */
        public Integer id; // uint32

        public AccessRestrictionStruct(AccessRestrictionTypeEnum type, Integer id) {
            this.type = type;
            this.id = id;
        }
    }

    /**
     * This structure describes a current access restriction on the fabric.
     */
    public static class AccessRestrictionEntryStruct {
        /**
         * This field shall indicate the endpoint having associated access restrictions scoped to the associated fabric
         * of the list containing the entry.
         */
        public Integer endpoint; // endpoint-no
        /**
         * This field shall indicate the cluster having associated access restrictions under the entry’s Endpoint,
         * scoped to the associated fabric of the list containing the entry.
         */
        public Integer cluster; // cluster-id
        /**
         * This field shall indicate the set of restrictions applying to the Cluster under the given Endpoint, scoped to
         * the associated fabric of the list containing the entry.
         * This list shall NOT be empty.
         */
        public List<AccessRestrictionStruct> restrictions; // list
        public Integer fabricIndex; // FabricIndex

        public AccessRestrictionEntryStruct(Integer endpoint, Integer cluster,
                List<AccessRestrictionStruct> restrictions, Integer fabricIndex) {
            this.endpoint = endpoint;
            this.cluster = cluster;
            this.restrictions = restrictions;
            this.fabricIndex = fabricIndex;
        }
    }

    /**
     * This structure describes a current access restriction when there is no accessing fabric.
     */
    public static class CommissioningAccessRestrictionEntryStruct {
        /**
         * This field shall indicate the endpoint having associated access restrictions scoped to the associated fabric
         * of the list containing the entry.
         */
        public Integer endpoint; // endpoint-no
        /**
         * This field shall indicate the cluster having associated access restrictions under the entry’s Endpoint,
         * scoped to the associated fabric of the list containing the entry.
         */
        public Integer cluster; // cluster-id
        /**
         * This field shall indicate the set of restrictions applying to the Cluster under the given Endpoint, scoped to
         * the associated fabric of the list containing the entry.
         * This list shall NOT be empty.
         */
        public List<AccessRestrictionStruct> restrictions; // list

        public CommissioningAccessRestrictionEntryStruct(Integer endpoint, Integer cluster,
                List<AccessRestrictionStruct> restrictions) {
            this.endpoint = endpoint;
            this.cluster = cluster;
            this.restrictions = restrictions;
        }
    }

    // Enums
    public enum ChangeTypeEnum implements MatterEnum {
        CHANGED(0, "Changed"),
        ADDED(1, "Added"),
        REMOVED(2, "Removed");

        public final Integer value;
        public final String label;

        private ChangeTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * ### Proxy View Value
     * ### This value implicitly grants View privileges
     */
    public enum AccessControlEntryPrivilegeEnum implements MatterEnum {
        VIEW(1, "View"),
        PROXY_VIEW(2, "Proxy View"),
        OPERATE(3, "Operate"),
        MANAGE(4, "Manage"),
        ADMINISTER(5, "Administer");

        public final Integer value;
        public final String label;

        private AccessControlEntryPrivilegeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum AccessRestrictionTypeEnum implements MatterEnum {
        ATTRIBUTE_ACCESS_FORBIDDEN(0, "Attribute Access Forbidden"),
        ATTRIBUTE_WRITE_FORBIDDEN(1, "Attribute Write Forbidden"),
        COMMAND_FORBIDDEN(2, "Command Forbidden"),
        EVENT_FORBIDDEN(3, "Event Forbidden");

        public final Integer value;
        public final String label;

        private AccessRestrictionTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum AccessControlEntryAuthModeEnum implements MatterEnum {
        PASE(1, "Pase"),
        CASE(2, "Case"),
        GROUP(3, "Group");

        public final Integer value;
        public final String label;

        private AccessControlEntryAuthModeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * This feature indicates the device supports ACL Extension attribute.
         */
        public boolean extension;
        /**
         * 
         * This feature is for a device that is managed by a service associated with the device vendor and which imposes
         * default access restrictions upon each new fabric added to it. This could arise, for example, if the device is
         * managed by a service provider under contract to an end-user, in such a way that the manager of the device
         * does not unconditionally grant universal access to all of a device’s functionality, even for fabric
         * administrators. For example, many Home Routers are managed by an Internet Service Provider (a service), and
         * these services often have a policy that requires them to obtain user consent before certain administrative
         * functions can be delegated to a third party (e.g., a fabric Administrator). These restrictions are expressed
         * using an Access Restriction List (ARL).
         * The purpose of this feature on the Access Control cluster is to indicate to a fabric Administrator that
         * access by it to specific attributes, commands and/or events for specific clusters is currently prohibited.
         * Attempts to access these restricted data model elements shall result in an error of ACCESS_RESTRICTED.
         * A device that implements this feature shall have a mechanism to honor the ReviewFabricRestrictions command,
         * such as user interfaces or service interactions associated with a service provider or the device
         * manufacturer, which allows the owner (or subscriber) to manage access restrictions for each fabric. The user
         * interface design, which includes the way restrictions are organized and presented to the user, is not
         * specified, but SHOULD be usable by non-expert end-users from common mobile devices, personal computers, or an
         * on-device user interface.
         * Controllers and clients SHOULD incorporate generic handling of the ACCESS_RESTRICTED error code, when it
         * appears in allowed contexts, in order to gracefully handle situations where this feature is encountered.
         * Device vendors that adopt this feature SHOULD be judicious in its use given the risk of unexpected behavior
         * in controllers and clients.
         * For certification testing, a device that implements this feature shall provide a way for all restrictions to
         * be removed.
         * The ARL attribute provides the set of restrictions currently applied to this fabric.
         * The ReviewFabricRestrictions command provides a way for the fabric Administrator to request that the server
         * triggers a review of the current fabric restrictions, by involving external entities such as end-users, or
         * other services associated with the manager of the device hosting the server. This review process may involve
         * communication between external services and the user, and may take an unpredictable amount of time to
         * complete since an end-user may need to visit some resources, such as a mobile application or web site. A
         * FabricRestrictionReviewUpdate event will be generated by the device within a predictable time period of the
         * ReviewFabricRestrictionsResponse (see ReviewFabricRestrictions for specification of this time period), and
         * this event can be correlated with the ReviewFabricRestrictionsResponse using a token provided in both. The
         * device may provide instructions or a Redirect URL in the FabricRestrictionReviewUpdate event in order to help
         * the user access the features required for managing per-fabric restrictions.
         * See Section 6.6.2, “Model” for a description of how access control is impacted by the ARL attribute.
         * ### Managed Device Feature Usage Restrictions
         * Use of this feature shall be limited to the mandatory clusters of endpoints having a device type that
         * explicitly permits its use in the Device Library Specification. As a reminder, the device types associated
         * with an endpoint are listed in the Descriptor cluster of the endpoint.
         * In addition, use of this feature shall NOT restrict the following clusters on any endpoint:
         * 1. the Descriptor Cluster (0x001D)
         * 2. the Binding Cluster (0x001E)
         * 3. the Network Commissioning Cluster (0x0031)
         * 4. the Identify Cluster (0x0003)
         * 5. the Groups Cluster (0x0004)
         * In addition, use of this feature shall NOT restrict the global attributes of any cluster.
         * Because ARLs cannot be used to restrict root node access or access to any clusters required for
         * commissioning, administrators may determine the current restrictions of the ARL at any point, including
         * during commissioning after joining the fabric.
         */
        public boolean managedDevice;

        public FeatureMap(boolean extension, boolean managedDevice) {
            this.extension = extension;
            this.managedDevice = managedDevice;
        }
    }

    public AccessControlCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 31, "AccessControl");
    }

    protected AccessControlCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command signals to the service associated with the device vendor that the fabric administrator would like a
     * review of the current restrictions on the accessing fabric. This command includes an optional list of ARL entries
     * that the fabric administrator would like removed.
     * In response, a ReviewFabricRestrictionsResponse is sent which contains a token that can be used to correlate a
     * review request with a FabricRestrictionReviewUpdate event.
     * Within 1 hour of the ReviewFabricRestrictionsResponse, the FabricRestrictionReviewUpdate event shall be
     * generated, in order to indicate completion of the review and any additional steps required by the user for the
     * review.
     * A review may include obtaining consent from the user, which can take time. For example, the user may need to
     * respond to an email or a push notification.
     * The ARL attribute may change at any time due to actions taken by the user, or the service associated with the
     * device vendor.
     */
    public static ClusterCommand reviewFabricRestrictions(List<CommissioningAccessRestrictionEntryStruct> arl) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (arl != null) {
            map.put("arl", arl);
        }
        return new ClusterCommand("reviewFabricRestrictions", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "acl : " + acl + "\n";
        str += "extension : " + extension + "\n";
        str += "subjectsPerAccessControlEntry : " + subjectsPerAccessControlEntry + "\n";
        str += "targetsPerAccessControlEntry : " + targetsPerAccessControlEntry + "\n";
        str += "accessControlEntriesPerFabric : " + accessControlEntriesPerFabric + "\n";
        str += "commissioningArl : " + commissioningArl + "\n";
        str += "arl : " + arl + "\n";
        return str;
    }
}
