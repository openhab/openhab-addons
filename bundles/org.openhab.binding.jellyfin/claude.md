# Claude / Copilot Workspace Notes

## openHAB Development Environment — API Tokens

**Important:** `$OPENHAB_API_TOKEN` in `~/.bashrc` is for the **productive** openHAB instance.

The **development/debug** instance (Docker container at `/home/pgfeller/Temp/openhab/`) requires
a **separate, dedicated API token**.

When using the REST API against the development server, obtain the token from that instance
directly (e.g., via the openHAB UI at `http://localhost:8080` → Settings → API Tokens) and
pass it explicitly rather than using `$OPENHAB_API_TOKEN`.

## Active Investigation

- Config page of created `jellyfin:server` thing cannot be opened
- Server IS discovered (inbox entry appears) and accepted (thing created) after the `advanced` XML
  attribute fix
- Thing state: `OFFLINE (CONFIGURATION_ERROR): No access token configured` — expected, token not
  yet set
- Next step: determine why the config page fails to load (REST API `config-descriptions` query
  needed with dev-instance token)
