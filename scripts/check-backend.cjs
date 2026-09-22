// Read-only probe. Never print keys, tokens, profiles, or raw server responses.
const fs = require('fs');
const src = fs.readFileSync('app/src/main/java/com/mrdiy/careers/data/auth/AuthManager.kt', 'utf8');
const url = src.match(/SUPABASE_URL\s*=\s*"([^"]+)"/)[1];
const block = src.match(/SUPABASE_ANON_KEY\s*=([\s\S]*?)\n\s*val supabaseJson/)[1];
const key = [...block.matchAll(/"([^"]+)"/g)].map(m => m[1]).join('');
(async () => {
  for (const path of ['/auth/v1/settings', '/rest/v1/jobs?status=eq.published&select=id,location,status&limit=1000']) {
    try {
      const res = await fetch(url + path, {headers:{apikey:key, Authorization:'Bearer '+key}, signal:AbortSignal.timeout(20000)});
      const data = await res.json();
      console.log(JSON.stringify(path.includes('settings') ? {probe:'auth',status:res.status,signupDisabled:data.disable_signup,confirmationRequired:!data.mailer_autoconfirm,emailEnabled:data.external?.email} :
        {probe:'published-jobs-anon',status:res.status,count:Array.isArray(data)?data.length:null,locations:Array.isArray(data)?[...new Set(data.map(r=>r.location))]:undefined}));
    } catch(e) { console.log(JSON.stringify({probe:path.split('?')[0],error:e.name})); process.exitCode=1; }
  }
})();
