Feature: Employee expense workflows
  Employees should be able to submit expenses and follow their review status,
  while invalid and unauthenticated interactions are rejected.

  Background:
    Given I am on the employee login page

  Scenario: Submit an expense and see a manager approval
    When I log in as employee "alice" with password "password123"
    Then I should see the employee expense dashboard
    When I submit an expense for "E2E client train fare" costing "42.75"
    Then I should see "E2E client train fare" with status "pending"
    When a manager approves "E2E client train fare"
    And I refresh the expense dashboard
    Then I should see "E2E client train fare" with status "approved"

  Scenario: Reject invalid employee credentials
    When I log in as employee "alice" with password "not-the-password"
    Then I should remain on the employee login page
    And I should see a login error

  Scenario: Reject an incomplete expense form
    When I log in as employee "alice" with password "password123"
    And I open the new expense form
    And I submit the expense form without an amount
    Then I should see the expense validation message
    And the expense form should remain open

  Scenario: Redirect an unauthenticated visitor
    When I clear my employee session
    And I open the expense dashboard directly
    Then I should be redirected to the employee login page
