document.getElementById("login-form").addEventListener("submit", async function (event) {
    event.preventDefault(); // stop the browser's default full-page-reload form submission

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const errorMessage = document.getElementById("error-message");

    errorMessage.textContent = ""; // clear any previous error

    try {
        const response = await fetch(`/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include", // send/receive the session cookie
            body: JSON.stringify({ username: username, password: password })
        });

        const data = await response.json();

        if (response.ok) {
            window.location.href = "pending.html";
        } else {
            errorMessage.textContent = data.error;
        }

    } catch (err) {
        errorMessage.textContent = "Unable to reach the server.";
    }
});