/* Service Worker - FDT Calculator
   Kazi: Ku-"cache" faili zote muhimu za app mara ya kwanza zinapopakiwa
   (zikiwa na intaneti), ili baadaye app iweze kufanya kazi KAMILI bila
   intaneti (offline) - jambo muhimu kwa matumizi ya field.

   MUHIMU UKIBADILISHA index.html AU lib/*.js BAADAYE:
   Lazima ubadilishe CACHE_NAME hapa chini (mf. "fdt-v2", "fdt-v3"...)
   ili simu zipakue toleo jipya. Kama hutabadilisha CACHE_NAME, Service
   Worker itaendelea kutumia faili za ZAMANI zilizohifadhiwa kwenye
   cache, hata ukishasasisha faili kwenye Netlify. */
const CACHE_NAME = "fdt-v21";

const ASSETS_TO_CACHE = [
  "./",
  "./index.html",
  "./lib/jspdf.umd.min.js",
  "./lib/jspdf.plugin.autotable.min.js"
];

self.addEventListener("install", (event) => {
  self.skipWaiting();
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(ASSETS_TO_CACHE))
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
    )
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET") return;

  event.respondWith(
    caches.match(event.request).then((cached) => {
      // Tumia toleo lililohifadhiwa kwenye cache MARA MOJA (haraka zaidi, na
      // linafanya kazi hata bila intaneti kabisa).
      if (cached) return cached;

      // Kama halijawahi kuhifadhiwa, jaribu kulichukua mtandaoni na
      // kulihifadhi kwa matumizi ya baadaye.
      return fetch(event.request)
        .then((response) => {
          if (response && response.status === 200 && response.type === "basic") {
            const responseClone = response.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(event.request, responseClone));
          }
          return response;
        })
        .catch(() => {
          if (event.request.mode === "navigate") {
            return caches.match("./index.html");
          }
        });
    })
  );
});
