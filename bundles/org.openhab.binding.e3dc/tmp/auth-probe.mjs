// What does the auth server expose? Login form or SAML-only?
const urls = [
  "https://e3dc.e3dc.com/",
  "https://e3dc.e3dc.com/auth-saml/login",
  "https://my.e3dc.com/login",
  "https://e3dc.e3dc.com/auth-saml/re-auth",
];

for (const u of urls) {
  try {
    const res = await fetch(u, { redirect: "manual" });
    const text = await res.text();
    console.log(`\n=== ${u}`);
    console.log("status:", res.status, "location:", res.headers.get("location"), "type:", res.headers.get("content-type"));
    const forms = [...text.matchAll(/<form[^>]*>/gi)].map((m) => m[0]);
    const inputs = [...text.matchAll(/<input[^>]*name="([^"]+)"[^>]*>/gi)].map((m) => m[1]);
    if (forms.length) console.log("forms:", forms.join(" | "));
    if (inputs.length) console.log("inputs:", inputs.join(", "));
    console.log("body:", text.slice(0, 400).replace(/\s+/g, " "));
  } catch (e) {
    console.log(`\n=== ${u}\nERR ${e.message}`);
  }
}
