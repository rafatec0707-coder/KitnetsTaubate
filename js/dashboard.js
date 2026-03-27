const API_BASE = "http://localhost:8080";

function getToken() {
  return localStorage.getItem("token");
}

function authHeaders() {
  const token = getToken();
  return {
    "Content-Type": "application/json",
    "Authorization": token ? `Bearer ${token}` : "",
  };
}

function toast(msg) {
  // Simple fallback to alert if container missing
  const container = document.getElementById("toastContainer");
  if (!container) return alert(msg);

  const el = document.createElement("div");
  el.className = "bg-black text-white px-4 py-2 rounded-lg shadow opacity-90";
  el.textContent = msg;
  container.appendChild(el);
  setTimeout(() => el.remove(), 2500);
}

function traduzStatus(status) {
  const s = String(status || "").toUpperCase();
  if (s === "APPROVED") return "APROVADO";
  if (s === "PENDING") return "PENDENTE";
  if (s === "REJECTED") return "REJEITADO";
  return status; // fallback
}

function formatVisit(v) {
  const hora = String(v.visitTime).slice(0, 5);
  const statusPt = traduzStatus(v.status);
  return `${v.visitDate} ${hora} • ${statusPt}`;
}

function visitCard(v, isAdmin) {
  const div = document.createElement("div");
  div.className = "bg-white p-4 rounded-xl shadow flex flex-col gap-2";

  const top = document.createElement("div");
  top.className = "flex items-center justify-between gap-3";

  const title = document.createElement("div");
  title.className = "font-semibold";
  title.textContent = formatVisit(v);

  top.appendChild(title);

  if (isAdmin) {
    const who = document.createElement("div");
    who.className = "text-sm text-gray-600";
    who.textContent = `${v.userName} • ${v.userEmail}`;
    div.appendChild(who);
  }

  div.appendChild(top);

  if (v.message) {
    const msg = document.createElement("div");
    msg.className = "text-sm text-gray-700";
    msg.textContent = v.message;
    div.appendChild(msg);
  }

  return div;
}

async function loadUserVisits() {
  const container = document.getElementById("userApplications");
  if (!container) return;

  const res = await fetch(`${API_BASE}/api/visits/mine`, { headers: authHeaders() });
  if (!res.ok) {
    toast("Não foi possível carregar suas solicitações.");
    return;
  }
  const data = await res.json();
  container.innerHTML = "";
  if (!data.length) {
    container.innerHTML = "<div class='text-gray-500'>Você ainda não solicitou visitas.</div>";
    return;
  }
  data.forEach(v => container.appendChild(visitCard(v, false)));
}

async function submitSchedule(e) {
  e.preventDefault();
  const date = document.getElementById("scheduleDate")?.value;
  const time = document.getElementById("scheduleTime")?.value;

  if (!date || !time) {
    toast("Preencha data e horário.");
    return;
  }

  const payload = { visitDate: date, visitTime: time, message: "" };

  const res = await fetch(`${API_BASE}/api/visits`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    const t = await res.text();
    toast(t || "Erro ao solicitar visita.");
    return;
  }

  toast("Solicitação enviada!");
  e.target.reset();
  await loadUserVisits();
}

async function loadAdminStatsAndList() {
  const statTotal = document.getElementById("statTotal");
  const statPending = document.getElementById("statPending");
  const statApproved = document.getElementById("statApproved");
  const statRejected = document.getElementById("statRejected");
  const container = document.getElementById("applicationsContainer");

  // stats
  const sres = await fetch(`${API_BASE}/api/visits/stats`, { headers: authHeaders() });
  if (sres.ok) {
    const s = await sres.json();
    statTotal.textContent = s.total;
    statPending.textContent = s.pending;
    statApproved.textContent = s.approved;
    statRejected.textContent = s.rejected;

    // chart
    if (window.Chart) {
      const ctx = document.getElementById("approvalChart");
      if (ctx) {
        if (window.__approvalChart) window.__approvalChart.destroy();
        window.__approvalChart = new Chart(ctx, {
          type: "doughnut",
          data: {
            labels: ["Pendente", "Aprovado", "Rejeitado"],
            datasets: [{
              data: [s.pending, s.approved, s.rejected],
              backgroundColor: [
                "#FACC15", // amarelo
                "#22C55E", // verde
                "#EF4444"  // vermelho
              ],
              borderWidth: 0
            }]
          },
          options: { responsive: true, maintainAspectRatio: false }
        });

      }
    }
  }

  // list
  const res = await fetch(`${API_BASE}/api/visits`, { headers: authHeaders() });
  if (!res.ok) {
    toast("Não foi possível carregar solicitações (admin).");
    return;
  }
  const data = await res.json();
  container.innerHTML = "";

  if (!data.length) {
    container.innerHTML = "<div class='text-gray-500'>Nenhuma solicitação encontrada.</div>";
    return;
  }

  data.forEach(v => {
    const card = visitCard(v, true);

    const btnRow = document.createElement("div");
    btnRow.className = "flex gap-2 pt-2";

    const mkBtn = (label, status, classes) => {
      const b = document.createElement("button");
      b.textContent = label;
      b.className = classes;
      b.addEventListener("click", async () => {
        const ures = await fetch(`${API_BASE}/api/visits/${v.id}/status`, {
          method: "PATCH",
          headers: authHeaders(),
          body: JSON.stringify({ status })
        });
        if (!ures.ok) {
          const t = await ures.text();
          toast(t || `Erro ao atualizar status (${ures.status}).`);
          return;
        }
        toast("Status atualizado!");
        await loadAdminStatsAndList();
      });
      return b;
    };

    btnRow.appendChild(mkBtn("Aprovar", "APPROVED", "bg-green-600 text-white px-3 py-1 rounded-lg text-sm hover:bg-green-700"));
    btnRow.appendChild(mkBtn("Rejeitar", "REJECTED", "bg-red-600 text-white px-3 py-1 rounded-lg text-sm hover:bg-red-700"));
    btnRow.appendChild(mkBtn("Pendente", "PENDING", "bg-yellow-500 text-white px-3 py-1 rounded-lg text-sm hover:bg-yellow-600"));

    card.appendChild(btnRow);
    container.appendChild(card);
  });
}

document.addEventListener("DOMContentLoaded", async () => {
  const userInfo = document.getElementById("userInfo");
  const logoutBtn = document.getElementById("logoutBtn");
  const userArea = document.getElementById("userArea");
  const adminArea = document.getElementById("adminArea");
  const scheduleForm = document.getElementById("scheduleForm");

  const loggedUser = JSON.parse(localStorage.getItem("user") || "null");
  const token = getToken();

  if (!loggedUser || !loggedUser.id || !token) {
    window.location.href = "login.html";
    return;
  }

  userInfo.textContent = `${loggedUser.name} (${loggedUser.role})`;

  logoutBtn.addEventListener("click", () => {
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    window.location.href = "login.html";
  });

  if (loggedUser.role === "ADMIN") {
    adminArea.classList.remove("hidden");
    await loadAdminStatsAndList();
  } else {
    userArea.classList.remove("hidden");
    scheduleForm?.addEventListener("submit", submitSchedule);
    await loadUserVisits();
  }
});
