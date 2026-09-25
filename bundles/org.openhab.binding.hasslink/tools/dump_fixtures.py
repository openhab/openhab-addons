import json
import os
import sys
import websocket
from config import FIXTURES_DIR, HA_TOKEN, HA_URL

# Ensure the script is executed from within the tools/ directory
if os.path.basename(os.getcwd()) != "tools":
    print("Error: This script must be run from inside the 'tools/' directory.")
    sys.exit(1)

# Output target directory inside Java Maven test resources
OUTPUT_DIR = os.path.join(FIXTURES_DIR, "fixtures")
os.makedirs(OUTPUT_DIR, exist_ok=True)


def on_message(ws, message):
    data = json.loads(message)

    # Handshake Phase
    if data.get("type") == "auth_required":
        ws.send(json.dumps({"type": "auth", "access_token": HA_TOKEN}))
        return

    if data.get("type") == "auth_ok":
        print("Authenticated successfully. Fetching entity states...")
        # Request all current entity states
        ws.send(json.dumps({"id": 1, "type": "get_states"}))
        return

    # Process response to `get_states`
    if data.get("id") == 1 and data.get("type") == "result":
        entities = data.get("result", [])
        saved_domains = set()

        for entity in entities:
            entity_id = entity["entity_id"]
            domain = entity_id.split(".")[0]

            # Save the first distinct entity sample per domain as a representative fixture
            filename = os.path.join(OUTPUT_DIR, f"{domain}_state_event.json")

            # Wrap sample state into the standard `state_changed` event payload structure
            ws_payload = {
                "id": 18,
                "type": "event",
                "event": {
                    "event_type": "state_changed",
                    "data": {
                        "entity_id": entity_id,
                        "old_state": None,
                        "new_state": entity,
                    },
                    "origin": "LOCAL",
                    "time_fired": "2026-09-19T00:00:00.000000+00:00",
                    "context": {
                        "id": "01HXXXXXXX",
                        "parent_id": None,
                        "user_id": None,
                    },
                },
            }

            # Save fixture file
            with open(
                os.path.join(OUTPUT_DIR, f"{entity_id}.json"), "w"
            ) as f:
                json.dump(ws_payload, f, indent=2)

            if domain not in saved_domains:
                with open(filename, "w") as f:
                    json.dump(ws_payload, f, indent=2)
                saved_domains.add(domain)
                print(f"Captured fixture for domain '{domain}' ({entity_id})")

        ws.close()


def on_error(ws, error):
    print("Error:", error)


ws = websocket.WebSocketApp(
    HA_URL, on_message=on_message, on_error=on_error
)
ws.run_forever()