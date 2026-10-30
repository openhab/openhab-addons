import json
import os
import sys
import time
import websocket
from config import FIXTURES_DIR, HA_TOKEN, HA_URL

# Enforce execution from inside the tools/ directory
if os.path.basename(os.getcwd()) != "tools":
    print("Error: This script must be run from inside the 'tools/' directory.")
    sys.exit(1)

OUTPUT_DIR = os.path.join(FIXTURES_DIR, "ha_fixtures")
os.makedirs(OUTPUT_DIR, exist_ok=True)

SNAPSHOT_REQUESTS = [
    {"id": 1, "type": "get_config"},
    {"id": 2, "type": "get_states"},
    {"id": 3, "type": "config/device_registry/list"},
    {"id": 4, "type": "config/entity_registry/list"},
    {"id": 5, "type": "config/area_registry/list"},
]

ws = websocket.create_connection(HA_URL)
# Set a 5-second socket timeout to prevent infinite blocking
ws.settimeout(5.0)

# Handshake
auth_req = json.loads(ws.recv())
if auth_req.get("type") == "auth_required":
    ws.send(json.dumps({"type": "auth", "access_token": HA_TOKEN}))
    auth_res = json.loads(ws.recv())
    if auth_res.get("type") != "auth_ok":
        print("Auth failed:", auth_res)
        sys.exit(1)

print("Authenticated successfully.")

# Helper class to manage incoming message routing
class WsDispatcher:
    def __init__(self, ws):
        self.ws = ws
        self.results = {}
        self.events = []

    def send_and_wait_result(self, req):
        msg_id = req["id"]
        self.ws.send(json.dumps(req))

        while msg_id not in self.results:
            try:
                raw = self.ws.recv()
                msg = json.loads(raw)

                if msg.get("type") == "event":
                    self.events.append(msg)
                elif "id" in msg:
                    self.results[msg["id"]] = msg
            except websocket.WebSocketTimeoutException:
                print(f"Timeout waiting for response to message ID {msg_id}")
                return None

        return self.results.pop(msg_id)

    def wait_for_event(self, sub_id, timeout=5.0):
        start = time.time()
        while time.time() - start < timeout:
            # Check already buffered events
            for i, evt in enumerate(self.events):
                if evt.get("id") == sub_id:
                    return self.events.pop(i)

            # Read new frame from socket
            try:
                raw = self.ws.recv()
                msg = json.loads(raw)
                if msg.get("type") == "event":
                    if msg.get("id") == sub_id:
                        return msg
                    self.events.append(msg)
                elif "id" in msg:
                    self.results[msg["id"]] = msg
            except websocket.WebSocketTimeoutException:
                pass
        return None

dispatcher = WsDispatcher(ws)

# 1. Capture static snapshots
for req in SNAPSHOT_REQUESTS:
    response = dispatcher.send_and_wait_result(req)
    if response:
        filename = req["type"].replace("/", "_") + ".json"
        filepath = os.path.join(OUTPUT_DIR, filename)
        with open(filepath, "w") as f:
            json.dump(response, f, indent=2)
        print(f"Saved snapshot fixture: {filename}")


# 2. Capture `area_registry_updated` event
print("Subscribing to area_registry_updated...")
sub_resp = dispatcher.send_and_wait_result({"id": 10, "type": "subscribe_events", "event_type": "area_registry_updated"})

# Trigger mutation: create temporary area
create_resp = dispatcher.send_and_wait_result({"id": 11, "type": "config/area_registry/create", "name": "Fixture Test Area"})

area_event = dispatcher.wait_for_event(10)
if area_event:
    with open(os.path.join(OUTPUT_DIR, "subscribe_area_registry_updated.json"), "w") as f:
        json.dump(area_event, f, indent=2)
    print("Saved subscribe_area_registry_updated.json")


# 3. Capture `device_registry_updated` event
devices_res = dispatcher.send_and_wait_result({"id": 20, "type": "config/device_registry/list"})
devices = devices_res.get("result", []) if devices_res else []

if devices:
    device_id = devices[0]["id"]
    print("Subscribing to device_registry_updated...")
    dispatcher.send_and_wait_result({"id": 21, "type": "subscribe_events", "event_type": "device_registry_updated"})

    # Trigger mutation
    dispatcher.send_and_wait_result({
        "id": 22,
        "type": "config/device_registry/update",
        "device_id": device_id,
        "name_by_user": "Fixture Test Device"
    })

    device_event = dispatcher.wait_for_event(21)
    if device_event:
        with open(os.path.join(OUTPUT_DIR, "subscribe_device_registry_updated.json"), "w") as f:
            json.dump(device_event, f, indent=2)
        print("Saved subscribe_device_registry_updated.json")


# 4. Capture `entity_registry_updated` event
entities_res = dispatcher.send_and_wait_result({"id": 30, "type": "config/entity_registry/list"})
entities = entities_res.get("result", []) if entities_res else []

if entities:
    entity_id = entities[0]["entity_id"]
    print("Subscribing to entity_registry_updated...")
    dispatcher.send_and_wait_result({"id": 31, "type": "subscribe_events", "event_type": "entity_registry_updated"})

    # Trigger mutation
    dispatcher.send_and_wait_result({
        "id": 32,
        "type": "config/entity_registry/update",
        "entity_id": entity_id,
        "name": "Fixture Test Entity"
    })

    entity_event = dispatcher.wait_for_event(31)
    if entity_event:
        with open(os.path.join(OUTPUT_DIR, "subscribe_entity_registry_updated.json"), "w") as f:
            json.dump(entity_event, f, indent=2)
        print("Saved subscribe_entity_registry_updated.json")

ws.close()
print("All registry fixtures generated successfully.")