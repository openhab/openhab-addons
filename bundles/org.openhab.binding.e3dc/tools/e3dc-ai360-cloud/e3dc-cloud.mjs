#!/usr/bin/env node
// E3/DC Cloud control for AI 360 and price-based battery charging.
//
// Talks to the same REST services the "My E3/DC" web portal uses:
//   auth:  https://e3dc.e3dc.com      (Bearer JWT + reAuthToken)
//   data:  https://installations.e3dc.com
//
// The endpoints and payload shapes were read from the public portal frontend
// (my.e3dc.com static bundles). They are undocumented and can change without
// notice; this tool is not affiliated with E3/DC.
//
// Usage:
//   node e3dc-cloud.mjs login                 # validate stored tokens
//   node e3dc-cloud.mjs discover              # list installations
//   node e3dc-cloud.mjs raw <path>            # GET any API path
//   node e3dc-cloud.mjs status  --installation <id> [--storage <id>]
//   node e3dc-cloud.mjs ai360   on|off --storage <id>
//   node e3dc-cloud.mjs battery on|off --installation <id> [--storage <id>]

import { readFileSync, writeFileSync, existsSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const AUTH_BASE = "https://e3dc.e3dc.com";
const API_BASE = "https://installations.e3dc.com";
const HERE = dirname(fileURLToPath(import.meta.url));
const TOKEN_FILE = join(HERE, ".tokens.json");

/**
 * Tokens live in the portal's localStorage under "authToken" and "reAuthToken".
 * The re-auth endpoint returns a fresh pair, so storing both keeps the chain alive.
 */
function loadTokens() {
  const fromFile = existsSync(TOKEN_FILE) ? JSON.parse(readFileSync(TOKEN_FILE, "utf8")) : {};
  const authToken = process.env.E3DC_AUTH_TOKEN ?? fromFile.authToken;
  const reAuthToken = process.env.E3DC_REAUTH_TOKEN ?? fromFile.reAuthToken;
  if (!authToken && !reAuthToken) {
    fail("No tokens. Export E3DC_REAUTH_TOKEN (and E3DC_AUTH_TOKEN) or create .tokens.json.",
      "Open my.e3dc.com, log in, then in DevTools console run:",
      "  copy(JSON.stringify({authToken: localStorage.authToken, reAuthToken: localStorage.reAuthToken}))",
      "and paste the result into .tokens.json next to this script.");
  }
  return { authToken, reAuthToken };
}

function saveTokens(tokens) {
  writeFileSync(TOKEN_FILE, JSON.stringify(tokens, null, 2), { mode: 0o600 });
}

function jwtPayload(token) {
  try {
    const part = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(Buffer.from(part, "base64").toString("utf8"));
  } catch {
    return {};
  }
}

function fail(...lines) {
  for (const line of lines) console.error(line);
  process.exit(1);
}

/** Exchanges the reAuthToken for a fresh {token, reAuthToken} pair. */
async function reAuth(tokens) {
  if (!tokens.reAuthToken) fail("reAuthToken expired or missing – grab a fresh pair from the portal.");
  const form = new FormData();
  form.append("reAuthToken", tokens.reAuthToken);
  const res = await fetch(`${AUTH_BASE}/auth-saml/re-auth`, { method: "POST", body: form });
  if (!res.ok) fail(`re-auth failed: HTTP ${res.status}`, "The reAuthToken is invalid or expired – log in to my.e3dc.com again and copy a fresh pair.");
  const data = await res.json();
  const next = { authToken: data.token, reAuthToken: data.reAuthToken };
  saveTokens(next);
  return next;
}

/** Authenticated API call; retries once after a re-auth when the token is stale. */
async function api(path, { method = "GET", payload, tokens } = {}) {
  let current = tokens ?? loadTokens();
  const exp = jwtPayload(current.authToken ?? "").exp;
  if (!current.authToken || (exp && exp * 1000 < Date.now() + 60_000)) {
    current = await reAuth(current);
    tokens = current;
  }

  const call = async (bearer) => {
    const url = `${API_BASE}/${path.replace(/^\//, "")}`;
    const headers = { authorization: `Bearer ${bearer}` };
    let body;
    if (method === "GET") {
      if (payload) {
        const query = new URLSearchParams();
        for (const [k, v] of Object.entries(payload)) query.set(k, String(v));
        return fetch(`${url}?${query}`, { method, headers });
      }
      return fetch(url, { method, headers });
    }
    headers["content-type"] = "application/json";
    body = JSON.stringify(payload ?? {});
    return fetch(url, { method, headers, body });
  };

  let res = await call(current.authToken);
  if (res.status === 401) {
    tokens = await reAuth(tokens ?? current);
    res = await call(tokens.authToken);
  }
  const text = await res.text();
  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }
  if (!res.ok) {
    fail(`HTTP ${res.status} ${method} ${path}`, typeof data === "string" ? data.slice(0, 400) : JSON.stringify(data).slice(0, 400));
  }
  return data;
}

function parseArgs(argv) {
  const flags = {};
  const positional = [];
  const args = argv.slice(2);
  for (let i = 0; i < args.length; i++) {
    if (args[i].startsWith("--")) {
      flags[args[i].slice(2)] = args[i + 1] && !args[i + 1].startsWith("--") ? args[++i] : true;
    } else {
      positional.push(args[i]);
    }
  }
  return { command: positional[0], args: positional.slice(1), flags };
}

async function main() {
  const { command, args, flags } = parseArgs(process.argv);
  const installationId = flags.installation;
  const storageId = flags.storage;

  switch (command) {
    case "login": {
      const tokens = await reAuth(loadTokens());
      const exp = jwtPayload(tokens.authToken).exp;
      console.log("OK – new token pair stored in .tokens.json");
      console.log("authToken valid until:", exp ? new Date(exp * 1000).toISOString() : "unknown");
      return;
    }

    case "discover": {
      const list = await api("installations");
      console.log(JSON.stringify(list, null, 2));
      return;
    }

    case "raw": {
      if (!args[0]) fail("Usage: raw <path> [--method PUT] [--body '{\"a\":1}']");
      const body = flags.body ? JSON.parse(flags.body) : undefined;
      const data = await api(args[0], { method: flags.method ?? "GET", payload: body });
      console.log(JSON.stringify(data, null, 2));
      return;
    }

    case "status": {
      if (!installationId) fail("Usage: status --installation <id> [--storage <id>]");
      console.log("dynamic-ems (installation):");
      console.log(JSON.stringify(await api(`installations/${installationId}/dynamic-ems`), null, 2));
      if (storageId) {
        console.log("\ndynamic-power-price (storage):");
        console.log(JSON.stringify(await api(`storages/${storageId}/dynamic-power-price`), null, 2));
      }
      return;
    }

    case "ai360": {
      // AI 360 on/off for one storage: the "AI 360 aktiv" master switch.
      const value = args[0] === "on" ? true : args[0] === "off" ? false : undefined;
      if (value === undefined || !storageId) fail("Usage: ai360 on|off --storage <id>");
      await api(`storages/${storageId}/dynamic-power-price`, { method: "PUT", payload: { enableDynamicEms: value } });
      console.log(`AI 360 ${value ? "enabled" : "disabled"} for storage ${storageId}`);
      return;
    }

    case "battery": {
      // "Hauskraftwerksbatterie preisbasiert laden?" – read, modify, write so the
      // other fields of the settings object survive.
      const value = args[0] === "on" ? true : args[0] === "off" ? false : undefined;
      if (value === undefined || !installationId) fail("Usage: battery on|off --installation <id> [--storage <id>]");

      const ems = (await api(`installations/${installationId}/dynamic-ems`)) ?? {};
      const emsBody = {
        battery: value,
        sgReady: ems.sgReady ?? false,
        feedInTariffType: ems.feedInTariffType ?? null,
      };
      await api(`installations/${installationId}/dynamic-ems`, { method: "PUT", payload: emsBody });
      console.log(`price-based battery charging ${value ? "enabled" : "disabled"} (installation ${installationId})`);

      if (storageId) {
        const dpp = (await api(`storages/${storageId}/dynamic-power-price`)) ?? {};
        await api(`storages/${storageId}/dynamic-power-price`, {
          method: "PUT",
          payload: {
            priceLimitWb: dpp.priceLimitWb,
            gridfee: dpp.gridfee,
            surcharge: dpp.surcharge,
            furthertaxes: dpp.furthertaxes,
            dppEnabledWb: dpp.dppEnabledWb,
            dppEnabledBattery: value,
            enableDynamicEms: value ? true : dpp.enableDynamicEms,
            priceLimitBattery: dpp.priceLimitBattery,
            activeMonthsBattery: dpp.activeMonthsBattery,
            maxSocBattery: dpp.maxSocBattery,
          },
        });
        console.log(`dynamic battery prices ${value ? "enabled" : "disabled"} (storage ${storageId})`);
      }
      return;
    }

    default:
      fail("Commands: login | discover | raw <path> | status | ai360 on|off | battery on|off", "See README.md in this folder.");
  }
}

await main();
