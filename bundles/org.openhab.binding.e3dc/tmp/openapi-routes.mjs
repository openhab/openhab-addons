// Which auth endpoints exist? 404 errno=105 means "no such route".
const candidates = [
  "/auth/login",
  "/auth/token",
  "/auth/authenticate",
  "/auth-saml/auth",
  "/auth-saml/login",
  "/auth-saml/token",
  "/auth-saml/re-auth",
  "/saml/login",
  "/login",
  "/auth/refresh",
  "/docs/swagger.json",
];

for (const method of ["POST", "GET"]) {
  console.log(`\n--- ${method}`);
  for (const p of candidates) {
    try {
      const res = await fetch("https://e3dc.e3dc.com" + p, {
        method,
        headers: method === "POST" ? { "content-type": "application/json" } : undefined,
        body: method === "POST" ? "{}" : undefined,
      });
      const text = (await res.text()).slice(0, 160).replace(/\s+/g, " ");
      console.log(`  ${res.status} ${p} ${text}`);
    } catch (e) {
      console.log(`  ERR ${p} ${e.message}`);
    }
  }
}
