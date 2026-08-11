package com.revature.unit.controller;

import com.revature.controller.ReportController;
import com.revature.dao.IReportDao;
import com.revature.model.ApprovalStatus;
import com.revature.model.ExpenseReportView;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportControllerTest {

    private IReportDao reportDao;
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        reportDao = mock(IReportDao.class);

        reportController = new ReportController(reportDao);
    }

    @Test
    @Epic("Unit Testing: Controller")
    @Feature("ReportController")
    @Story("Happy Path: Get report by employee id when it exists")
    void getReportByEmployee_exists() {
        when(reportDao.findByEmployee(1)).thenReturn(new ArrayList<>());

        List<ExpenseReportView> result = reportController.getReportByEmployee(1);

        assertEquals(new ArrayList<>(), result);
    }

    @Test
    @Epic("Unit Testing: Controller")
    @Feature("ReportController")
    @Story("Get report employee id when no such id exists")
    void getReportByEmployee_noEmployee() {
        when(reportDao.findByEmployee(1)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> reportController.getReportByEmployee(1));
    }

    @Test
    @Epic("Unit Testing: Controller")
    @Feature("ReportController")
    @Story("Happy Path: Get report by date range with a valid date range")
    void getReportByDateRange_validDateRange() {
        LocalDate startDate = LocalDate.of(2020, 1, 15);
        LocalDate endDate = LocalDate.of(2020, 1, 30);

        when(reportDao.findByDateRange(startDate, endDate)).thenReturn(new ArrayList<>());

        List<ExpenseReportView> result = reportController.getReportByDateRange(startDate, endDate);

        assertEquals(new ArrayList<>(), result);
    }

    @Test
    @Epic("Unit Testing: Controller")
    @Feature("ReportController")
    @Story("Happy Path: Get report by date range with equal date range")
    void getReportByDateRange_validDateRange_equalsEndDate() {
        LocalDate startDate = LocalDate.of(2020, 1, 15);
        LocalDate endDate = LocalDate.of(2020, 1, 15);

        when(reportDao.findByDateRange(startDate, endDate)).thenReturn(new ArrayList<>());

        List<ExpenseReportView> result = reportController.getReportByDateRange(startDate, endDate);

        assertEquals(new ArrayList<>(), result);
    }

    @Test
    @Epic("Unit Testing: Controller")
    @Feature("ReportController")
    @Story("Get report by date range with invalid date range")
    void getReportByDateRange_invalidDateRange() {
        LocalDate startDate = LocalDate.of(2020, 1, 15);
        LocalDate endDate = LocalDate.of(2020, 1, 14);

        when(reportDao.findByDateRange(startDate, endDate)).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> reportController.getReportByDateRange(startDate, endDate));
    }

    @Test
    @Epic("Unit Testing: Controller")
    @Feature("ReportController")
    @Story("Happy Path: Get report by approved status returns approved statuses")
    void getReportByStatus_approvedStatus() {
        List<ExpenseReportView> sampleData = new ArrayList<>();
        sampleData.add(new ExpenseReportView(1, "username", 25.50,  "description", LocalDate.of(2020, 1, 1), ApprovalStatus.APPROVED, "comment"));

        when(reportDao.findByStatus(ApprovalStatus.APPROVED)).thenReturn(sampleData);

        List<ExpenseReportView> result = reportController.getReportByStatus(ApprovalStatus.APPROVED);

        assertEquals(sampleData, result);
    }
}
