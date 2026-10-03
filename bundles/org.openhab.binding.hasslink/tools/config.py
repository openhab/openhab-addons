# tools/config.py
import os

HA_URL = "ws://localhost:9123/api/websocket"

# Determine path to tools/ directory and target Java test resources directory
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
FIXTURES_DIR = os.path.abspath(
    os.path.join(
        BASE_DIR,
        "../src/test/resources/org/openhab/binding/hasslink/internal/fixtures",
    )
)


def load_token(token_file=".token") -> str:
    """Reads the token string from a .token file in tools/."""
    token_path = os.path.join(BASE_DIR, token_file)
    if not os.path.exists(token_path):
        raise FileNotFoundError(
            f"Token file '{token_path}' not found. Please run 'bash bootstrap_ha.sh' first."
        )

    with open(token_path, "r") as f:
        token = f.read().strip()

    if not token:
        raise ValueError(f"Token file '{token_path}' is empty.")

    return token


HA_TOKEN = load_token()