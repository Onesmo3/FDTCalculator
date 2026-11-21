/* Service Worker - FDT Calculator
   Kazi: Ku-"cache" faili zote muhimu za app ili iweze kufanya kazi KAMILI
   bila intaneti (offline) - jambo muhimu kwa matumizi ya field.

   MKAKATI (v26 - umebadilika):
   - App shell (index.html, "./") = NETWORK-FIRST: jaribu mtandao kwanza ili
     mtumiaji apate toleo jipya mara moja akiwa na intaneti; kama hakuna
     intaneti, anguka kwenye cache (offline bado inafanya kazi 100%).
   - Maktaba za jsPDF (lib/*.js) = CACHE-FIRST: haya hayabadiliki mara kwa
     mara, hivyo cache ni haraka zaidi na hakuna sababu ya kusubiri mtandao.

   MUHIMU UKIBADILISHA index.html AU lib/*.js BAADAYE:
   Lazima ubadilishe CACHE_NAME hapa chini (mf. "fdt-v27") ili mfumo wa
   update-notification hapa chini uweze kumjulisha mtumiaji kuna toleo jipya. */
const CACHE_NAME = "fdt-v26";

const STATIC_LIBS = [
  "./lib/jspdf.umd.min.js",
  "./lib/jspdf.plugin.autotable.min.js"
];

const APP_SHELL = [
  "./",
  "./index.html"
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll([...APP_SHELL, ...STATIC_LIBS]))
  );
  /* HATUTUMII self.skipWaiting() moja kwa moja hapa tena kama awali.
     Tunasubiri ruhusa kutoka kwa mtumiaji (kupitia ujumbe "SKIP_WAITING"
     kutoka index.html) - hii inazuia app "kubadilika chini ya miguu" ya
     mtumiaji wakati anajaza fomu ya field bila kujua kuna toleo jipya. */
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

/* Ukurasa (index.html) utatuma ujumbe huu baada ya mtumiaji kubonyeza
   "Sasisha Sasa" kwenye banner ya update-notification. */
self.addEventListener("message", (event) => {
  if (event.data && event.data.type === "SKIP_WAITING") {
    self.skipWaiting();
  }
});

self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET") return;

  const url = new URL(event.request.url);
  const isStaticLib = STATIC_LIBS.some((path) => url.pathname.endsWith(path.replace("./", "/")));

  if (isStaticLib) {
    /* CACHE-FIRST kwa maktaba za jsPDF */
    event.respondWith(
      caches.match(event.request).then((cached) => cached || fetch(event.request))
    );
    return;
  }

  /* NETWORK-FIRST kwa app shell (index.html na "./") */
  event.respondWith(
    fetch(event.request)
      .then((response) => {
        if (response && response.status === 200 && response.type === "basic") {
          const responseClone = response.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(event.request, responseClone));
        }
        return response;
      })
      .catch(() =>
        caches.match(event.request).then((cached) => {
          if (cached) return cached;
          if (event.request.mode === "navigate") return caches.match("./index.html");
        })
      )
  );
});
