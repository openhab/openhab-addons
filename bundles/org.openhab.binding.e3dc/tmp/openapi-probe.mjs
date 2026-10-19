// Look for OpenAPI documents on the E3DC service hosts.
const hosts = ["https://e3dc.e3dc.com", "https://installations.e3dc.com"];
const paths = ["/swagger.json", "/swagger-ui-init.js", "/api-json", "/json", "/openapi.json", "/docs", "/api-docs", "/"];

for (const h of hosts) {
  for (const p of paths) {
    try {
      const res = await fetch(h + p, { redirect: "manual" });
      const ct = res.headers.get("content-type") ?? "";
      const text = await res.text();
      console.log(`${h}${p} -> ${res.status} ${ct.split(";")[0]} len=${text.length}`);
      if (res.ok && /json|javascript/.test(ct) && text.length > 200 && !/swagger-ui\.css/.test(text.slice(0, 400))) {
        console.log("   head:", text.slice(0, 200).replace(/\s+/g, " "));
      }
    } catch (e) {
      console.log(`${h}${p} -> ERR ${e.message}`);
    }
  }
}
