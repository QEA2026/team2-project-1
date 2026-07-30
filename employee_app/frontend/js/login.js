const form = document.querySelector("#login-form");
const errorMessage = document.querySelector("#login-error");
const passwordInput = document.querySelector("#password");
const toggle = document.querySelector(".password-toggle");

toggle.addEventListener("click", () => {
    const showing = passwordInput.type === "text";
    passwordInput.type = showing ? "password" : "text";
    toggle.textContent = showing ? "Show" : "Hide";
    toggle.setAttribute("aria-label", showing ? "Show password" : "Hide password");
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorMessage.textContent = "";

    if (!form.checkValidity()) {
        errorMessage.textContent = "Enter both your username and password.";
        return;
    }

    const button = form.querySelector("button[type='submit']");
    button.disabled = true;
    try {
        const response = await fetch("/users/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                username: form.username.value.trim(),
                password: form.password.value
            })
        });
        const contentType = response.headers.get("content-type") || "";
        if (!contentType.includes("application/json")) {
            throw new Error(
                response.ok
                    ? "The login server returned an unexpected response."
                    : `The login server returned an error (${response.status}).`
            );
        }
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "That username or password doesn’t look right.");
        }
        if (String(data.role).toLowerCase() === "manager") {
            throw new Error("This portal is for employee accounts only.");
        }

        sessionStorage.setItem("expenseflow.user", JSON.stringify({
            id: data.id,
            username: data.username,
            role: data.role
        }));
        window.location.href = "dashboard.html";
    } catch (error) {
        errorMessage.textContent = error.message === "Failed to fetch"
            ? "We couldn’t reach the server. Please try again."
            : error.message;
    } finally {
        button.disabled = false;
    }
});
