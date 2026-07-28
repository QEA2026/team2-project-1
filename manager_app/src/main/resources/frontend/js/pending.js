async function loadPendingExpenses() {
    const loadError = document.getElementById("load-error");

    try {
        const response = await fetch("/expenses/pending", {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = "index.html";
                return;
            }
            const data = await response.json();
            loadError.textContent = data.error || "Failed to load expenses.";
            return;
        }

        const expenses = await response.json();
        loadError.textContent = "";
        renderExpenses(expenses);

    } catch (err) {
        loadError.textContent = "Unable to reach the server.";
    }
}

function renderExpenses(expenses) {
    const tableBody = document.getElementById("pending-table-body");
    tableBody.innerHTML = "";

    if (expenses.length === 0) {
        const row = document.createElement("tr");
        row.innerHTML = `<td colspan="6">No pending expenses.</td>`;
        tableBody.appendChild(row);
        return;
    }

    expenses.forEach(function (expense) {
        const row = document.createElement("tr");
        row.setAttribute("data-expense-id", expense.expenseId);

        row.innerHTML = `
            <td>${expense.expenseId}</td>
            <td>${expense.employeeUsername}</td>
            <td>$${expense.amount.toFixed(2)}</td>
            <td>${expense.description}</td>
            <td>${expense.date}</td>
            <td>
                <button class="approve-btn" data-id="${expense.expenseId}">Approve</button>
                <button class="deny-btn" data-id="${expense.expenseId}">Deny</button>
            </td>
        `;

        tableBody.appendChild(row);
    });
}

async function decideExpense(expenseId, approve, comment) {
    const decisionError = document.getElementById("decision-error");
    decisionError.textContent = "";

    try {
        const response = await fetch(`/expenses/${expenseId}/decision`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ approve: approve, comment: comment })
        });

        const data = await response.json();

        if (!response.ok) {
            decisionError.textContent = data.error;
            return;
        }

        // Success: refresh the list so the decided expense disappears from "pending"
        loadPendingExpenses();

    } catch (err) {
        decisionError.textContent = "Unable to reach the server.";
    }
}

document.getElementById("pending-table-body").addEventListener("click", function (event) {
    const target = event.target;
    const expenseId = target.getAttribute("data-id");

    if (target.classList.contains("approve-btn")) {
        const comment = window.prompt("Enter a comment for this approval:");
        handleDecisionPrompt(expenseId, true, comment);
    }

    if (target.classList.contains("deny-btn")) {
        const comment = window.prompt("Enter a reason for denying this expense:");
        handleDecisionPrompt(expenseId, false, comment);
    }
});

function handleDecisionPrompt(expenseId, approve, comment) {
    if (comment === null) {
        return; // user clicked Cancel — do nothing
    }
    if (comment.trim() === "") {
        document.getElementById("decision-error").textContent =
            "A comment is required for approval or denial decisions.";
        return;
    }

    decideExpense(expenseId, approve, comment);
}

loadPendingExpenses();

document.getElementById("logout-button").addEventListener("click", async function () {
    await fetch("/logout", { method: "POST", credentials: "include" });
    window.location.href = "index.html";
});