export interface Request {
    id: string;
    namespace: string;
    function: string;
    args?: any[];
}

export interface Response {
    type: string;
    id: string;
    result?: any;
    error?: string;
}

export interface Event {
    type: string;
    data?: any;
}

export enum EventType {
    AttributeChanged = "attributeChanged",
    EventTriggered = "eventTriggered",
    NodeStateInformation = "nodeStateInformation",
    NodeData = "nodeData",
    BridgeEvent = "bridgeEvent",
}

export interface Message {
    type: string;
    message: any;
}

export enum MessageType {
    ResultError = "resultError",
    ResultSuccess = "resultSuccess",
}

export enum BridgeEventType {
    AttributeChanged = "attributeChanged",
    EventTriggered = "eventTriggered",
}
export interface BridgeEvent {
    type: string;
    data: any;
}

export interface BridgeAttributeChangedEvent {
    endpointId: string;
    clusterName: string;
    attributeName: string;
    data: any;
}

export interface BridgeEventTrigger {
    eventName: string;
    data: any;
}

export enum NodeState {
    /** Node is connected, but not fully initialized. */
    CONNECTED = "Connected",

    /**
     * Node is disconnected. Data are stale and interactions will most likely return an error. If controller
     * instance is still active then the device will be reconnected once it is available again.
     */
    DISCONNECTED = "Disconnected",

    /** Node is reconnecting. Data are stale. It is yet unknown if the reconnection is successful. */
    RECONNECTING = "Reconnecting",

    /**
     * The node could not be connected and the controller is now waiting for a MDNS announcement and tries every 10
     * minutes to reconnect.
     */
    WAITING_FOR_DEVICE_DISCOVERY = "WaitingForDeviceDiscovery",

    /**
     * Node structure has changed (Endpoints got added or also removed). Data are up-to-date.
     * This State information will only be fired when the subscribeAllAttributesAndEvents option is set to true.
     */
    STRUCTURE_CHANGED = "StructureChanged",

    /**
     * The node was just Decommissioned.
     */
    DECOMMISSIONED = "Decommissioned",
}
