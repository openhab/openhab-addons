# openHAB MCP Server

The openHAB MCP add-on lets AI assistants — Claude Desktop, Claude Code, ChatGPT, and other [Model Context Protocol](https://modelcontextprotocol.io) (MCP) clients — read and control your openHAB smart home.

Once you've connected a client, you can say things like:

- _"What's the status of my home?"_
- _"Dim the office to 30%."_
- _"Turn on the porch light every day at sunset."_
- _"Let me know if the garage door opens while we're talking."_
- _"Turn off the kitchen light at 10pm tonight."_

The assistant uses your home's semantic model (rooms, equipment, devices) along with fuzzy matching logic to understand which items you mean to monitor or control without having to use the exact item name.

## Installation

Install the **Model Context Protocol (MCP) Server** add-on in openHAB under **Settings → Add-on Store → System Integrations**.

The server starts automatically on `http://<your-openhab>:8080/mcp`.
Every connection must present an openHAB bearer token (see [Authentication](#authentication) below).

## Settings

| Setting                | Default | Advanced | Description                                                                                                                                                                                                           |
| ---------------------- | ------- | -------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `enabled`              | `true`  | NO       | Turn the server on or off without uninstalling.                                                                                                                                                                       |
| `enableFullApiAccess`  | `false` | NO       | Give the assistant access to the **full openHAB REST API** — including destructive endpoints. Only turn this on if you trust the assistant with that scope. See [Full REST API access](#full-rest-api-access-opt-in). |
| `registerCloudWebhook` | `false` | NO       | Let remote MCP clients reach this server through openHAB Cloud (`myopenhab.org`) with no port forwarding. Requires the openHAB Cloud add-on. See [openHAB Cloud](#2-openhab-cloud) under Connecting a client.         |
| `exposeUntaggedItems`  | `false` | YES      | Also include items that aren't assigned to a Location/Equipment/Point. Most people leave this off.                                                                                                                    |
| `maxItemsPerPage`      | `100`   | YES      | Maximum items returned in one paginated response.                                                                                                                                                                     |
| `resourceCoalesceMs`   | `500`   | YES      | Rate-limit update notifications for chatty items like power meters.                                                                                                                                                   |

## Authentication

Every connection needs an **openHAB API token**.

Tokens can be obtained in 2 different ways:

1. Clients that support oAuth will automatically redirect users to login to openHAB and obtain a user token. This will likely require the [openHAB Cloud](#2-openhab-cloud) webhook option to allow outside ingress to the binding.
1. Clients can be [manually configured](#manual-token-generation) with a token and either used locally, directly to openHAB, or with the [openHAB Cloud](#2-openhab-cloud) webhook.

### Manual Token Generation

Generate a token in the openHAB UI:

1. Click your user menu (bottom-left) → **Profile**.
1. Open the **API Tokens** section → **+ Add API Token**.
1. Give it a name (e.g. _"Claude"_) and a scope (leave blank for full access).
1. Copy the token — it looks like `oh.abcDEF123…`. You won't see it again.

Paste the token into your MCP client as an `Authorization: Bearer oh.…` header — the [examples](#client-setup) below show how for each client.

Tokens are tied to the user that created them, so the assistant's permissions match whatever that user can do through the openHAB UI.
If you want to restrict what the assistant can touch, create a dedicated openHAB user with limited permissions and generate the token from that account.

> Modern MCP clients (recent Claude, ChatGPT, `mcp-remote`) can drive a browser-based OAuth login — no token pasting needed.
> The server advertises the necessary OAuth metadata automatically; just give the client your server URL and it'll walk you through sign-in the first time.

## Connecting a client

There are three ways to connect an MCP client to your openHAB server.
Which one you use depends on whether the client can reach your openHAB instance directly and whether it speaks Streamable HTTP natively.

### Connection methods

#### 1. Direct connection (LAN or VPN)

Point the client at `http://<your-openhab>:8080/mcp` with an `Authorization: Bearer oh.…` header.
The client must be able to reach your openHAB instance over the network.
Use this when the client is on the same LAN, connected via VPN, or you have a reverse proxy set up.

#### 2. openHAB Cloud

> **Warning:** Exposing the MCP service via a webhook will make the openHAB user login UI available to anyone with the unique generated cloud URL.
> This URL should be considered private and not shared outside of using it with trusted AI providers.
> To remove the webhook, disable the option from the settings menu. Enabling will generate a new unique URL.

Enable **`registerCloudWebhook`** in the add-on settings.
The server registers with your openHAB Cloud service and you get a public HTTPS URL:

```text
https://myopenhab.org/api/hooks/<uuid>
```

The generated URL will be visible in the configuration menu under **Settings → Add-on Settings → Model Context Protocol (MCP) Server**.
Use this URL in any MCP client — no port forwarding or reverse proxy needed.
You can either include a static `oh.*` token in the client config, or let the client handle sign-in automatically via OAuth (Claude Desktop, ChatGPT, and `mcp-remote` all support this).

> **First-time OAuth login:** you'll sign in twice — once to the openHAB Cloud service, then again to openHAB itself to authorize the MCP client.
> Subsequent connections reuse stored tokens.

#### 3. mcp-remote bridge (stdio)

Some clients (like Claude Desktop) can also connect using a local Node.js bridge that translates between stdio and HTTP.
This requires [Node.js 18+](https://nodejs.org) installed on the machine running the client.
The bridge can point at either a direct LAN URL or a Cloud URL.

### Client setup

#### Claude Desktop

Edit `claude_desktop_config.json` (macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`, Windows: `%APPDATA%\Claude\claude_desktop_config.json`).
Completely quit and reopen Claude Desktop after editing — you should see "openhab" in the MCP indicator.

**Option A — Local network with token:**

```json
{
  "mcpServers": {
    "openhab": {
      "command": "npx",
      "args": [
        "mcp-remote@latest",
        "http://openhab.local:8080/mcp",
        "--allow-http",
        "--header",
        "Authorization: Bearer oh.YOUR_TOKEN"
      ]
    }
  }
}
```

**Option B — openHAB Cloud with token:**

```json
{
  "mcpServers": {
    "openhab": {
      "command": "npx",
      "args": [
        "mcp-remote@latest",
        "https://myopenhab.org/api/hooks/<uuid>",
        "--header",
        "Authorization: Bearer oh.YOUR_TOKEN"
      ]
    }
  }
}
```

**Option C — openHAB Cloud with OAuth (no token needed):**

1. In the Claude app, go to **Settings → Connectors → Add Connector**.
1. Enter the Cloud hook URL (e.g. `https://myopenhab.org/api/hooks/<uuid>`).
1. Claude will open a browser to sign in the first time you connect.

No config file editing or Node.js required.

Options A and B require [Node.js 18+](https://nodejs.org).

#### Claude Code (CLI)

```bash
claude mcp add --transport http openhab http://openhab.local:8080/mcp \
    --header "Authorization: Bearer oh.YOUR_TOKEN"
```

Works with both direct LAN URLs and Cloud URLs.

#### ChatGPT (chatgpt.com, desktop, mobile)

ChatGPT requires a **public HTTPS URL**, so use the **openHAB Cloud** method.
ChatGPT handles sign-in through a browser OAuth prompt — no token pasting needed.
Requires a paid ChatGPT plan (Plus, Pro, Business, Enterprise, Education).

1. Turn on **`registerCloudWebhook`** in the add-on settings and copy the hook URL from the openHAB log.
1. In ChatGPT: **Settings → Apps & Connectors → Advanced settings** → toggle **Developer mode** on.
1. **Settings → Connectors → Create**.
1. Name: `openHAB`. Description: _"Smart-home control. Use this to query device state, turn things on/off, check temperatures, and create automations."_
1. Connector URL: the hook URL from step 1 (e.g. `https://myopenhab.org/api/hooks/<uuid>`).
1. Save — ChatGPT will walk you through sign-in the first time, then list the tools.

## Example prompts

### Query & control

- _"What's the status of my home right now?"_
- _"What rooms are in my house?"_
- _"Turn the kitchen lights off."_
- _"Dim the office to 30%."_

### Automations

- _"Turn on the office light at 7pm tonight."_ — one-shot rule, auto-deletes after it fires.
- _"Every day at sunset, turn on the porch light."_ — recurring schedule.
- _"When the garage door opens, turn on the garage light."_ — event-driven rule that runs even when you're not chatting.
- _"Delete the rules you created earlier."_ — the assistant can list and remove rules it set up.

### Watching for changes

- _"Keep an eye on the garage door — tell me if it opens while we're talking."_
- _"Is the dishwasher done yet?"_ — start watching once, ask again later for a summary.

> Current MCP clients don't surface server-pushed notifications to the LLM yet, so the assistant can only report on changes **when you ask** — it won't interrupt you proactively.
> That will improve as clients add support; the server is already ready for it.

## What the assistant can do

### Items

| Tool                 | What it does                                                                                                      |
| -------------------- | ----------------------------------------------------------------------------------------------------------------- |
| `get_semantic_model` | Returns the home layout: Locations → Equipment → Points. The assistant usually calls this first to orient itself. |
| `search_items`       | Fuzzy search across names, labels, and synonyms. Tolerates typos and word reordering.                             |
| `get_item`           | Current state and details for one or more items by exact name.                                                    |
| `create_item`        | Create a new item (Switch, Dimmer, Number, Group, etc.) with label, tags, and group memberships.                  |
| `update_item`        | Modify an item's label, category, tags, or group memberships.                                                     |
| `delete_item`        | Permanently remove an item.                                                                                       |
| `send_command`       | Turn things on/off, set dimmer/colour values, etc.                                                                |
| `update_state`       | Directly set the state of a virtual item (bypasses the device handler).                                           |
| `get_home_status`    | A single snapshot: security (open doors/windows), active lights, climate, energy, device health.                  |
| `get_system_info`    | openHAB version, item/thing/rule counts, installed bindings.                                                      |

### Things & links

| Tool                | What it does                                                   |
| ------------------- | -------------------------------------------------------------- |
| `get_things`        | Lists Things with online/offline status.                       |
| `get_thing_details` | Channels, configuration, and linked items for one Thing.       |
| `get_links`         | List item-channel links, filtered by item name or channel UID. |
| `create_link`       | Wire an item to a thing's channel.                             |
| `delete_link`       | Remove an item-channel link.                                   |

### Rules

| Tool          | What it does                                                                                                                                          |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| `get_rules`   | Lists automation rules. `tag='MCP'` scopes the list to rules the assistant created.                                                                   |
| `create_rule` | Creates a rule. Triggers supported: `time_of_day`, `cron`, `item_state_change`, `item_command`, and `datetime` (one-shot; auto-deletes after firing). |
| `update_rule` | Modify a rule's name, description, tags, or actions without recreating it.                                                                            |
| `manage_rule` | Enable, disable, manually trigger, or delete a rule.                                                                                                  |

### Watching for changes

| Tool            | What it does                                                                |
| --------------- | --------------------------------------------------------------------------- |
| `watch_items`   | Start tracking the given items for state changes in this session.           |
| `get_events`    | Return any state changes buffered since the last call and drain the buffer. |
| `unwatch_items` | Stop watching (omit the item list to unwatch everything).                   |

### Improving how the assistant resolves names

Items have a built-in `synonyms` metadata namespace.
Add a comma-separated list of nicknames (e.g. `den, study, office nook` on your family room light) and the assistant will match them when you use those words.
Items can also be associated with a Location via the `hasLocation` metadata, not just Group membership.

## Full REST API access (opt-in)

> ⚠️ With this flag on, the assistant can call **any** openHAB REST endpoint, including destructive ones (delete items, modify Things, change service configs). It uses your bearer token, so it can only do things your user could do from the UI. Only turn this on if you trust the assistant with that scope.

Setting `enableFullApiAccess=true` exposes three extra tools:

| Tool                    | What it does                                                                                                   |
| ----------------------- | -------------------------------------------------------------------------------------------------------------- |
| `list_api_endpoints`    | Enumerate every REST endpoint from openHAB's live OpenAPI spec. Filter by tag (`items`, `rules`, …) or method. |
| `describe_api_endpoint` | Return the schema fragment for one endpoint so the assistant knows what parameters to pass.                    |
| `call_api`              | Invoke any endpoint. Supports every HTTP method.                                                               |

Useful when the built-in tools don't cover the task, for example:

- _"Rename the metadata namespace 'alexa' to 'openhabcloud' on all items."_
- _"Query the last 24 hours of temperature persistence for the hallway sensor."_
- _"Approve the pending Zigbee discovery result for the bulb I just paired."_

## Real-time subscriptions (advanced)

> Most clients (including Claude Desktop) don't expose this to the LLM yet. Use `watch_items` / `get_events` instead for now — they work with every client.

MCP clients that honour `notifications/resources/updated` can subscribe to these URIs and receive push notifications:

| URI template                    | Fires on                               |
| ------------------------------- | -------------------------------------- |
| `openhab://item/{itemName}`     | Item state changes                     |
| `openhab://thing/{thingUID}`    | Thing online/offline transitions       |
| `openhab://rule/{ruleUID}`      | Rule status transitions                |
| `openhab://home/semantic-model` | The full Location/Equipment/Point tree |

High-frequency items (power meters, fast sensors) are coalesced to at most one notification per `resourceCoalesceMs` window.

## Troubleshooting

Enable debug logging from the openHAB console to see every request and response:

```text
log:set DEBUG org.openhab.io.mcp
```

For raw wire-level dumps (headers and full body), turn on `TRACE`:

```text
log:set TRACE org.openhab.io.mcp
```
