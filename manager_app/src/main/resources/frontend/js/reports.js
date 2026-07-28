async function fetchReport(url) {
    const reportError = document.getElementById("report-error");
    reportError.textContent = "";

    try {
        const response = await fetch(url, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = "index.html";
                return;
            }
            const data = await response.json();
            reportError.textContent = data.error || "Failed to load report.";
            return;
        }

        const results = await response.json();
        renderReport(results);

    } catch (err) {
        reportError.textContent = "Unable to reach the server.";
    }
}

function renderReport(results) {
    const tableBody = document.getElementById("report-table-body");
    tableBody.innerHTML = "";

    if (results.length === 0) {
        const row = document.createElement("tr");
        row.innerHTML = `<td colspan="7">No matching expenses.</td>`;
        tableBody.appendChild(row);
        return;
    }

    results.forEach(function (expense) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${expense.expenseId}</td>
            <td>${expense.employeeUsername}</td>
            <td>$${expense.amount.toFixed(2)}</td>
            <td>${expense.description}</td>
            <td>${expense.date}</td>
            <td>${expense.status}</td>
            <td>${expense.comment ?? ""}</td>
        `;
        tableBody.appendChild(row);
    });
}

document.getElementById("employee-report-button").addEventListener("click", function () {
    const employeeId = document.getElementById("employee-id").value;

    if (employeeId === "") {
        document.getElementById("report-error").textContent = "Please enter an employee ID.";
        return;
    }

    fetchReport(`/reports/employee/${employeeId}`);
});

document.getElementById("date-report-button").addEventListener("click", function () {
    const start = document.getElementById("start-date").value;
    const end = document.getElementById("end-date").value;

    if (start === "" || end === "") {
        document.getElementById("report-error").textContent = "Please select both a start and end date.";
        return;
    }

    fetchReport(`/reports/date?start=${start}&end=${end}`);
});

document.getElementById("status-report-button").addEventListener("click", function () {
    const status = document.getElementById("status-select").value;
    fetchReport(`/reports/status/${status}`);
});

document.getElementById("logout-button").addEventListener("click", async function () {
    await fetch("/logout", { method: "POST", credentials: "include" });
    window.location.href = "index.html";
});