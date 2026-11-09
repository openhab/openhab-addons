import json
import os
import sys
import websocket
from config import FIXTURES_DIR, HA_TOKEN, HA_URL

# Enforce execution from inside the tools/ directory
if os.path.basename(os.getcwd()) != "tools":
    print("Error: This script must be run from inside the 'tools/' directory.")
    sys.exit(1)

# Target output directory inside Java Maven test resources
OUTPUT_DIR = os.path.join(FIXTURES_DIR, "compressed_fixtures")
os.makedirs(OUTPUT_DIR, exist_ok=True)


def on_message(ws, message):
    data = json.loads(message)

    # Handshake
    if data.get("type") == "auth_required":
        ws.send(json.dumps({"type": "auth", "access_token": HA_TOKEN}))
        return

    if data.get("type") == "auth_ok":
        print("Authenticated. Subscribing to entities (compressed)...")
        # Issue subscribe_entities command (id 10)
        ws.send(json.dumps({"id": 10, "type": "subscribe_entities"}))
        return

    # Process subscribe_entities initial dump response
    if data.get("id") == 10 and data.get("type") == "event":
        event_data = data.get("event", {})
        additions = event_data.get("a", {})

        if additions:
            saved_domains = set()

            for entity_id, compressed_state in additions.items():
                domain = entity_id.split(".")[0]

                # Reconstruct a discrete WebSocket message payload representing this single entity
                single_entity_payload = {
                    "id": 10,
                    "type": "event",
                    "event": {
                        "a": {
                            entity_id: compressed_state
                        }
                    }
                }

                # Save individual entity fixture
                with open(os.path.join(OUTPUT_DIR, f"{entity_id}.json"), "w") as f:
                    json.dump(single_entity_payload, f, indent=2)

                # Save one domain representative fixture (e.g. light_compressed.json)
                if domain not in saved_domains:
                    domain_filename = os.path.join(OUTPUT_DIR, f"{domain}_compressed.json")
                    with open(domain_filename, "w") as f:
                        json.dump(single_entity_payload, f, indent=2)
                    saved_domains.add(domain)
                    print(f"Saved compressed fixture for '{domain}' -> {entity_id}")

            print("Fixtures written successfully.")
            ws.close()


def on_error(ws, error):
    print("Error:", error)


ws = websocket.WebSocketApp(
    HA_URL, on_message=on_message, on_error=on_error
)
ws.run_forever()