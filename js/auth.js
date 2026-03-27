console.log("Auth JS loaded (backend enabled)");

const API_BASE = "http://localhost:8080";

function showError(msg) {
  alert(msg);
}

document.addEventListener("DOMContentLoaded", () => {
  const loginForm = document.getElementById("loginForm");
  const registerForm = document.getElementById("registerForm");
  const showLogin = document.getElementById("showLogin");
  const showRegister = document.getElementById("showRegister");

  if (!loginForm || !registerForm || !showLogin || !showRegister) {
    console.error("Missing elements:", { loginForm, registerForm, showLogin, showRegister });
    alert("Erro: elementos do formulário não encontrados. Verifique os IDs no HTML.");
    return;
  }

  const getVal = (form, name) => (form.elements[name]?.value ?? "").trim();
  const getNum = (form, name) => Number(form.elements[name]?.value ?? 0);

  function activateLogin() {
    loginForm.classList.remove("hidden");
    registerForm.classList.add("hidden");

    showLogin.classList.add("border-b-2", "border-blue-600", "text-blue-600");
    showLogin.classList.remove("text-gray-500");

    showRegister.classList.remove("border-b-2", "border-blue-600", "text-blue-600");
    showRegister.classList.add("text-gray-500");
  }

  function activateRegister() {
    loginForm.classList.add("hidden");
    registerForm.classList.remove("hidden");

    showRegister.classList.add("border-b-2", "border-blue-600", "text-blue-600");
    showRegister.classList.remove("text-gray-500");

    showLogin.classList.remove("border-b-2", "border-blue-600", "text-blue-600");
    showLogin.classList.add("text-gray-500");
  }

  showLogin.addEventListener("click", activateLogin);
  showRegister.addEventListener("click", activateRegister);
  activateLogin();

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const payload = {
      email: getVal(loginForm, "email"),
      password: getVal(loginForm, "password"),
    };

    if (!payload.email || !payload.password) {
      showError("Preencha email e senha.");
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const txt = await res.text();
        showError(txt || "Login inválido.");
        return;
      }

      const data = await res.json();
      localStorage.setItem("token", data.token);
      localStorage.setItem("user", JSON.stringify(data.user));
      window.location.href = "dashboard.html";
    } catch (err) {
      console.error(err);
      showError("Falha ao conectar no backend. Verifique se o Spring Boot está rodando na porta 8080.");
    }
  });

  registerForm.addEventListener("submit", async (e) => {
  e.preventDefault();

  const payload = {
    name: getVal(registerForm, "fullName"),
    email: getVal(registerForm, "email"),
    password: getVal(registerForm, "password"),
    phone: getVal(registerForm, "phone"),
    cpf: getVal(registerForm, "cpf"),
    income: getNum(registerForm, "income"),
  };

  if (payload.cpf.length !== 11) {
    alert("O CPF deve ter exatamente 11 números.");
    return;
  }

  console.log("REGISTER payload:", payload);

  try {
    const res = await fetch(`${API_BASE}/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    const { json, text } = await readBody(res);

    if (!res.ok) {
      alert(`Cadastro falhou (${res.status}):\n${text}`);
      return;
    }

    alert("Cadastro realizado! Agora faça login.");
    registerForm.reset();
    activateLogin();
  } catch (err) {
    console.error(err);
    alert("Erro ao conectar no backend (8081).");
  }
  const email = getVal(registerForm, "email");

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

if (!emailRegex.test(email)) {
  alert("Digite um e-mail válido.");
  return;
}
});

  console.log("Auth listeners attached ✅");
});

const emailInput = document.querySelector('input[name="email"]');

emailInput.addEventListener("input", () => {
  emailInput.value = emailInput.value.toLowerCase();
});