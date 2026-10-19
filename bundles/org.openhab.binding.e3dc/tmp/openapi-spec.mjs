// Fetch the official OpenAPI spec and list auth + AI 360 relevant operations.
for (const host of ["https://e3dc.e3dc.com", "https://installations.e3dc.com"]) {
  const res = await fetch(host + "/docs/swagger.json");
  console.log(`\n=== ${host}/docs/swagger.json -> ${res.status}`);
  if (!res.ok) continue;
  const spec = await res.json();
  console.log("title:", spec.info?.title, "version:", spec.info?.version, "paths:", Object.keys(spec.paths ?? {}).length);
  for (const [p, ops] of Object.entries(spec.paths ?? {})) {
    if (/auth|login|token|saml|dynamic|price|ai360|ai-360/i.test(p)) {
      console.log("  ", p, "->", Object.keys(ops).join(","));
    }
  }
  const tryPath = process.argv[2];
  if (tryPath && spec.paths?.[tryPath]) {
    console.log("\nDETAIL", tryPath);
    console.log(JSON.stringify(spec.paths[tryPath], null, 2).slice(0, 4000));
  }
}
