// Interactive logic for SearchPro web landing page & custom domain generator

const DNS_CONFIGS = {
  cloudflare: (domain) => [
    { type: "CNAME", name: "@", value: "searchpro.pages.dev", ttl: "Auto / Proxied" },
    { type: "CNAME", name: "www", value: "searchpro.pages.dev", ttl: "Auto / Proxied" }
  ],
  vercel: (domain) => [
    { type: "A", name: "@", value: "76.76.21.21", ttl: "Auto" },
    { type: "CNAME", name: "www", value: "cname.vercel-dns.com", ttl: "Auto" }
  ],
  github: (domain) => [
    { type: "A", name: "@", value: "185.199.108.153", ttl: "3600" },
    { type: "A", name: "@", value: "185.199.109.153", ttl: "3600" },
    { type: "CNAME", name: "www", value: `${domain}.`, ttl: "3600" }
  ],
  firebase: (domain) => [
    { type: "A", name: "@", value: "199.36.158.100", ttl: "3600" },
    { type: "TXT", name: "@", value: "hosted-domain-verify=searchpro", ttl: "3600" }
  ],
  netlify: (domain) => [
    { type: "A", name: "@", value: "75.2.60.5", ttl: "Auto" },
    { type: "CNAME", name: "www", value: "searchpro.netlify.app", ttl: "Auto" }
  ]
};

let currentProvider = "cloudflare";

function updateDnsTable() {
  const input = document.getElementById("domainInput");
  const rawDomain = (input.value || "tubuscador.com").trim().toLowerCase().replace(/^https?:\/\//, "").replace(/\/$/, "");
  
  const records = DNS_CONFIGS[currentProvider](rawDomain);
  const tbody = document.getElementById("dnsTableBody");
  tbody.innerHTML = "";

  records.forEach((record) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><span class="dns-code">${record.type}</span></td>
      <td><strong>${record.name}</strong></td>
      <td>
        <span class="dns-code">${record.value}</span>
        <button class="dns-copy-btn" onclick="copyText('${record.value}')">Copiar</button>
      </td>
      <td>${record.ttl}</td>
    `;
    tbody.appendChild(tr);
  });

  const cnameFileContent = document.getElementById("cnameValue");
  if (cnameFileContent) {
    cnameFileContent.textContent = rawDomain;
  }
}

function selectProvider(provider) {
  currentProvider = provider;
  document.querySelectorAll(".provider-tab").forEach((tab) => {
    tab.classList.toggle("active", tab.dataset.provider === provider);
  });
  updateDnsTable();
}

function copyText(text) {
  navigator.clipboard.writeText(text).then(() => {
    alert("¡Copiado al portapapeles: " + text);
  }).catch(() => {
    prompt("Copia este valor:", text);
  });
}

function downloadCnameFile() {
  const input = document.getElementById("domainInput");
  const domain = (input.value || "tubuscador.com").trim().toLowerCase();
  const blob = new Blob([domain], { type: "text/plain" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "CNAME";
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

// Generate simple visual QR matrix for scanning on canvas
function drawQrPlaceholder(canvasId) {
  const canvas = document.getElementById(canvasId);
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  const size = 150;
  canvas.width = size;
  canvas.height = size;
  
  ctx.fillStyle = "#ffffff";
  ctx.fillRect(0, 0, size, size);

  // Draw stylish QR locator boxes
  const drawCorner = (x, y) => {
    ctx.fillStyle = "#0f172a";
    ctx.fillRect(x, y, 35, 35);
    ctx.fillStyle = "#ffffff";
    ctx.fillRect(x + 5, y + 5, 25, 25);
    ctx.fillStyle = "#4f46e5";
    ctx.fillRect(x + 10, y + 10, 15, 15);
  };

  drawCorner(5, 5);
  drawCorner(size - 40, 5);
  drawCorner(5, size - 40);

  // Pseudo-random deterministic dots
  ctx.fillStyle = "#1e293b";
  const seed = 1337;
  for (let r = 0; r < 20; r++) {
    for (let c = 0; c < 20; c++) {
      if ((r < 6 && c < 6) || (r < 6 && c > 13) || (r > 13 && c < 6)) continue;
      const pseudo = Math.sin(r * 19 + c * 37 + seed);
      if (pseudo > 0.05) {
        ctx.fillRect(10 + c * 6.5, 10 + r * 6.5, 4.5, 4.5);
      }
    }
  }

  // Center brand icon or dot
  ctx.fillStyle = "#8b5cf6";
  ctx.beginPath();
  ctx.arc(size / 2, size / 2, 9, 0, 2 * Math.PI);
  ctx.fill();
}

window.addEventListener("DOMContentLoaded", () => {
  updateDnsTable();
  drawQrPlaceholder("qrCanvas");

  const input = document.getElementById("domainInput");
  if (input) {
    input.addEventListener("input", updateDnsTable);
  }
});
