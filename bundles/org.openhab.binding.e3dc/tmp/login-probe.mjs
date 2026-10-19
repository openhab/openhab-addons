// Probe the login flow: is there a programmatic username/password endpoint?
const BASE = "https://my.e3dc.com";
const html = await (await fetch(BASE + "/")).text();
const bundles = [...html.matchAll(/(?:src|href)="([^"]+\.js)"/g)].map((m) => m[1]);
const out = [];
for (const b of bundles) {
  const res = await fetch(BASE + b);
  if (!res.ok) continue;
  const text = await res.text();
  for (const n of ["auth-saml/login", "auth-saml", "/login?", "saml"]) {
    let i = text.indexOf(n);
    let k = 0;
    while (i !== -1 && k < 2 && out.length < 26) {
      out.push(`\n### ${b.split("/").pop()} :: ${n} @${i}\n${text.slice(Math.max(0, i - 400), i + 520).replace(/\s+/g, " ")}`);
      i = text.indexOf(n, i + 1);
      k++;
    }
  }
}
console.log(out.join("\n"));
