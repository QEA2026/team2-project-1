package com.revature;

import com.revature.controller.AuthController;
import com.revature.controller.ReportController;
import com.revature.dao.*;
import com.revature.model.*;
import com.revature.service.LoginService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import com.revature.controller.ExpenseController;
import io.javalin.json.JavalinJackson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    private static final Set<String> PUBLIC_PATHS = Set.of("/login");

    public static void main(String[] args) {

        IUserDao userDao = new UserDao();
        IExpenseDao expenseDao = new ExpenseDao();
        IApprovalDao approvalDao = new ApprovalDao();
        IReportDao reportDao = new ReportDao();
        ReportController reportController = new ReportController(reportDao);

        LoginService loginService = new LoginService(userDao);
        AuthController authController = new AuthController(loginService);
        ExpenseController expenseController = new ExpenseController(expenseDao, approvalDao);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Javalin app = Javalin.create(config -> {
            config.useVirtualThreads = true;
            config.jsonMapper(new JavalinJackson(objectMapper, false));
        }).start(7000);

        app.before(Main::requireManager);

        app.post("/login", ctx -> handleLogin(ctx, authController));
        app.get("/whoami", Main::handleWhoAmI);
        app.post("/logout", Main::handleLogout);

        app.get("/expenses/pending", ctx -> handleGetPending(ctx, expenseController));

        app.post("/expenses/{id}/decision", ctx -> handleDecideExpense(ctx, expenseController));

        app.get("/reports/employee/{userId}", ctx -> handleReportByEmployee(ctx, reportController));
        app.get("/reports/date", ctx -> handleReportByDateRange(ctx, reportController));

        app.get("/reports/status/{status}", ctx -> handleReportByStatus(ctx, reportController));
    }

    private static void requireManager(Context ctx) {
        if (PUBLIC_PATHS.contains(ctx.path())) {
            return;
        }

        String role = ctx.sessionAttribute("role");

        if (role == null) {
            ctx.status(401);
            ctx.json(Map.of("error", "Not logged in"));
            ctx.skipRemainingHandlers();
            return;
        }

        if (!Role.MANAGER.name().equals(role)) {
            ctx.status(403);
            ctx.json(Map.of("error", "Access is restricted to managers"));
            ctx.skipRemainingHandlers();
        }
    }

    private static void handleLogin(Context ctx, AuthController authController) {
        LoginRequest body = ctx.bodyAsClass(LoginRequest.class);

        try {
            User user = authController.login(body.username(), body.password());

            ctx.sessionAttribute("userId", user.getId());
            ctx.sessionAttribute("username", user.getUsername());
            ctx.sessionAttribute("role", user.getRole().name());

            ctx.status(200);
            ctx.json(Map.of(
                    "message", "Login successful",
                    "username", user.getUsername(),
                    "role", user.getRole()
            ));

        } catch (SecurityException e) {
            ctx.status(401);
            ctx.json(Map.of("error", e.getMessage()));
        }
    }

    record LoginRequest(String username, String password) {}

    private static void handleWhoAmI(Context ctx) {
        ctx.json(Map.of(
                "username", (String) ctx.sessionAttribute("username"),
                "role", (String) ctx.sessionAttribute("role")
        ));
    }

    private static void handleLogout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.status(200);
        ctx.json(Map.of("message", "Logged out"));
    }

    private static void handleGetPending(Context ctx, ExpenseController expenseController) {
        List<PendingExpenseView> pending = expenseController.getPendingExpenses();
        ctx.status(200);
        ctx.json(pending);
    }



    private static void handleDecideExpense(Context ctx, ExpenseController expenseController) {
        int expenseId;
        try {
            expenseId = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json(Map.of("error", "Invalid expense id"));
            return;
        }

        DecisionRequest body = ctx.bodyAsClass(DecisionRequest.class);
        Integer reviewerId = ctx.sessionAttribute("userId");

        try {
            expenseController.decideExpense(expenseId, reviewerId, body.approve(), body.comment());

            ctx.status(200);
            ctx.json(Map.of(
                    "message", "Decision recorded",
                    "expenseId", expenseId,
                    "status", body.approve() ? "APPROVED" : "DENIED"
            ));

        } catch (IllegalArgumentException e) {
            // Bad input: expense doesn't exist, or missing required comment on denial
            ctx.status(400);
            ctx.json(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            // Valid input, but the expense is already in a state that disallows this action
            ctx.status(409);
            ctx.json(Map.of("error", e.getMessage()));
        }
    }

    record DecisionRequest(boolean approve, String comment) {}

    private static void handleReportByEmployee(Context ctx, ReportController reportController) {
        int userId;
        try {
            userId = Integer.parseInt(ctx.pathParam("userId"));
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json(Map.of("error", "Invalid user id"));
            return;
        }

        List<ExpenseReportView> report = reportController.getReportByEmployee(userId);
        ctx.status(200);
        ctx.json(report);
    }

    private static void handleReportByDateRange(Context ctx, ReportController reportController) {
        String startParam = ctx.queryParam("start");
        String endParam = ctx.queryParam("end");

        if (startParam == null || endParam == null) {
            ctx.status(400);
            ctx.json(Map.of("error", "Both 'start' and 'end' query parameters are required"));
            return;
        }

        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(startParam);
            end = LocalDate.parse(endParam);
        } catch (DateTimeParseException e) {
            ctx.status(400);
            ctx.json(Map.of("error", "Dates must be in YYYY-MM-DD format"));
            return;
        }

        try {
            List<ExpenseReportView> report = reportController.getReportByDateRange(start, end);
            ctx.status(200);
            ctx.json(report);
        } catch (IllegalArgumentException e) {
            ctx.status(400);
            ctx.json(Map.of("error", e.getMessage()));
        }
    }

    private static void handleReportByStatus(Context ctx, ReportController reportController) {
        String statusParam = ctx.pathParam("status").toUpperCase();

        ApprovalStatus status;
        try {
            status = ApprovalStatus.valueOf(statusParam);
        } catch (IllegalArgumentException e) {
            ctx.status(400);
            ctx.json(Map.of(
                    "error", "Invalid status. Must be one of: PENDING, APPROVED, DENIED"
            ));
            return;
        }

        List<ExpenseReportView> report = reportController.getReportByStatus(status);
        ctx.status(200);
        ctx.json(report);
    }
}