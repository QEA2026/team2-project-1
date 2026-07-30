const PAGE_SIZE = 5;
const storedUser = sessionStorage.getItem("expenseflow.user");
if (!storedUser) window.location.replace("/");

const user = storedUser ? JSON.parse(storedUser) : {};
const state = { expenses: [], filter: "all", page: 1, deleteId: null };
const rows = document.querySelector("#expense-rows");
const message = document.querySelector("#dashboard-message");
const expenseDialog = document.querySelector("#expense-dialog");
const expenseForm = document.querySelector("#expense-form");
const deleteDialog = document.querySelector("#delete-dialog");

const money = (value) => new Intl.NumberFormat("en-US", {
    style: "currency", currency: "USD"
}).format(Number(value) || 0);

const friendlyDate = (value) => {
    const date = new Date(`${value}T00:00:00`);
    return Number.isNaN(date.valueOf())
        ? value
        : new Intl.DateTimeFormat("en-US", { month: "short", day: "numeric", year: "numeric" }).format(date);
};

const safeText = (value) => {
    const div = document.createElement("div");
    div.textContent = value ?? "";
    return div.innerHTML;
};

function setIdentity() {
    const name = user.username ? user.username[0].toUpperCase() + user.username.slice(1) : "Employee";
    document.querySelector("#heading-name").textContent = name;
    document.querySelector("#nav-user-name").textContent = name;
    document.querySelector("#user-avatar").textContent = name[0].toUpperCase();
    const hour = new Date().getHours();
    document.querySelector("#day-period").textContent = hour < 12 ? "morning" : hour < 18 ? "afternoon" : "evening";
}

async function getStatus(expenseId) {
    const response = await fetch(`/approvals/expense/${expenseId}`);
    if (!response.ok) return { status: "pending", comment: "" };
    return response.json();
}

async function loadExpenses() {
    message.textContent = "";
    try {
        const response = await fetch(`/expenses/user/${user.id}`);
        if (!response.ok) throw new Error();
        const expenses = await response.json();
        const approvals = await Promise.all(expenses.map((expense) => getStatus(expense.id)));
        state.expenses = expenses.map((expense, index) => ({
            ...expense,
            status: String(approvals[index].status || "pending").toLowerCase(),
            comment: approvals[index].comment || ""
        })).sort((a, b) => b.id - a.id);
        render();
    } catch {
        message.textContent = "We couldn’t load your expenses. Please refresh and try again.";
    }
}

function updateSummary() {
    const total = state.expenses.reduce((sum, item) => sum + Number(item.amount), 0);
    const pending = state.expenses.filter((item) => item.status === "pending");
    const approved = state.expenses.filter((item) => item.status === "approved");
    document.querySelector("#total-amount").textContent = money(total);
    document.querySelector("#total-count").textContent = `${state.expenses.length} ${state.expenses.length === 1 ? "expense" : "expenses"}`;
    document.querySelector("#pending-amount").textContent = money(pending.reduce((sum, item) => sum + Number(item.amount), 0));
    document.querySelector("#pending-count").textContent = `${pending.length} pending`;
    document.querySelector("#approved-amount").textContent = money(approved.reduce((sum, item) => sum + Number(item.amount), 0));
    document.querySelector("#approved-count").textContent = `${approved.length} approved`;
}

function render() {
    updateSummary();
    const visible = state.filter === "history"
        ? state.expenses.filter((expense) => expense.status !== "pending")
        : state.expenses;
    const pages = Math.max(1, Math.ceil(visible.length / PAGE_SIZE));
    state.page = Math.min(state.page, pages);
    const start = (state.page - 1) * PAGE_SIZE;
    const pageItems = visible.slice(start, start + PAGE_SIZE);

    rows.innerHTML = pageItems.map((expense) => {
        const editable = expense.status === "pending";
        return `<tr>
            <td><div class="expense-cell"><span class="expense-symbol">$</span><div>
                <strong title="${safeText(expense.description)}">${safeText(expense.description) || "Untitled expense"}</strong>
                <small>Expense #${expense.id}</small>
            </div></div></td>
            <td>${friendlyDate(expense.date)}</td>
            <td class="amount">${money(expense.amount)}</td>
            <td><span class="status status--${expense.status}" title="${safeText(expense.comment)}">${safeText(expense.status)}</span></td>
            <td><div class="row-actions">${editable ? `
                <button class="row-action" data-action="edit" data-id="${expense.id}" title="Edit expense" aria-label="Edit expense ${expense.id}">
                    <svg viewBox="0 0 24 24"><path d="m14 5 5 5M4 20l3.5-.7L19 7.8a2 2 0 0 0-2.8-2.8L4.7 16.5 4 20Z"/></svg>
                </button>
                <button class="row-action row-action--delete" data-action="delete" data-id="${expense.id}" title="Delete expense" aria-label="Delete expense ${expense.id}">
                    <svg viewBox="0 0 24 24"><path d="M4 7h16M9 7V4h6v3M7 7l1 13h8l1-13M10 11v5M14 11v5"/></svg>
                </button>` : ""}</div></td>
        </tr>`;
    }).join("");

    document.querySelector("#empty-state").hidden = visible.length !== 0;
    document.querySelector(".table-scroll").hidden = visible.length === 0;
    const shownEnd = Math.min(start + PAGE_SIZE, visible.length);
    document.querySelector("#pagination-label").textContent = visible.length
        ? `Showing ${start + 1}–${shownEnd} of ${visible.length} expenses`
        : "Showing 0 expenses";
    document.querySelector("#page-number").textContent = state.page;
    document.querySelector("#previous-page").disabled = state.page === 1;
    document.querySelector("#next-page").disabled = state.page === pages;
}

function openExpenseDialog(expense = null) {
    expenseForm.reset();
    document.querySelector("#expense-form-message").textContent = "";
    document.querySelector("#expense-id").value = expense?.id || "";
    document.querySelector("#dialog-title").textContent = expense ? "Edit pending expense" : "Submit a new expense";
    document.querySelector("#dialog-description").textContent = expense
        ? "Update the details before your expense is reviewed."
        : "Add the details below for manager review.";
    document.querySelector("#save-expense-button").textContent = expense ? "Save changes" : "Submit expense";
    document.querySelector("#expense-description").value = expense?.description || "";
    document.querySelector("#expense-amount").value = expense?.amount || "";
    document.querySelector("#expense-date").value = expense?.date || new Date().toISOString().slice(0, 10);
    document.querySelector("#expense-date").max = new Date().toISOString().slice(0, 10);
    expenseDialog.showModal();
}

expenseForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formMessage = document.querySelector("#expense-form-message");
    if (!expenseForm.checkValidity()) {
        formMessage.textContent = "Complete every field with a valid value.";
        return;
    }
    const id = document.querySelector("#expense-id").value;
    const payload = {
        user_id: user.id,
        amount: Number(document.querySelector("#expense-amount").value),
        description: document.querySelector("#expense-description").value.trim(),
        date: document.querySelector("#expense-date").value
    };
    const saveButton = document.querySelector("#save-expense-button");
    saveButton.disabled = true;
    try {
        const response = await fetch(id ? `/expenses/${id}` : "/expenses", {
            method: id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const saved = await response.json();
        if (!response.ok) throw new Error(saved.error || "The expense could not be saved.");
        if (!id) {
            const approvalResponse = await fetch("/approvals", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ expense_id: saved.id, status: "PENDING", reviewer: null, comment: "", review_date: "" })
            });
            if (!approvalResponse.ok) throw new Error("The expense was saved, but its review status could not be created.");
        }
        expenseDialog.close();
        showToast(id ? "Expense updated." : "Expense submitted for review.");
        await loadExpenses();
    } catch (error) {
        formMessage.textContent = error.message;
    } finally {
        saveButton.disabled = false;
    }
});

rows.addEventListener("click", (event) => {
    const button = event.target.closest("[data-action]");
    if (!button) return;
    const expense = state.expenses.find((item) => item.id === Number(button.dataset.id));
    if (button.dataset.action === "edit") openExpenseDialog(expense);
    if (button.dataset.action === "delete") {
        state.deleteId = expense.id;
        deleteDialog.showModal();
    }
});

document.querySelector("#confirm-delete").addEventListener("click", async () => {
    const button = document.querySelector("#confirm-delete");
    button.disabled = true;
    try {
        const response = await fetch(`/expenses/${state.deleteId}`, { method: "DELETE" });
        if (!response.ok) throw new Error();
        deleteDialog.close();
        showToast("Expense deleted.");
        await loadExpenses();
    } catch {
        deleteDialog.close();
        message.textContent = "We couldn’t delete that expense. Please try again.";
    } finally {
        button.disabled = false;
    }
});

document.querySelectorAll(".filter-button").forEach((button) => button.addEventListener("click", () => {
    document.querySelector(".filter-button.active").classList.remove("active");
    button.classList.add("active");
    state.filter = button.dataset.filter;
    state.page = 1;
    render();
}));
document.querySelector("#new-expense-button").addEventListener("click", () => openExpenseDialog());
document.querySelectorAll(".dialog-close").forEach((button) => button.addEventListener("click", () => expenseDialog.close()));
document.querySelector(".delete-cancel").addEventListener("click", () => deleteDialog.close());
document.querySelector("#previous-page").addEventListener("click", () => { state.page -= 1; render(); });
document.querySelector("#next-page").addEventListener("click", () => { state.page += 1; render(); });
document.querySelector("#logout-button").addEventListener("click", () => {
    sessionStorage.removeItem("expenseflow.user");
    window.location.replace("/");
});

function showToast(text) {
    const toast = document.querySelector("#toast");
    toast.textContent = text;
    toast.classList.add("show");
    window.setTimeout(() => toast.classList.remove("show"), 2800);
}

setIdentity();
loadExpenses();
