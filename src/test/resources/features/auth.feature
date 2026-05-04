Feature: Authentication REST API

  Scenario: Register, login, and read the current profile
    Given a unique user with password "password123"
    When the user registers
    Then the response status is 201
    And the response includes an auth token
    And the response includes the username
    When the user logs in
    Then the response status is 200
    And the response includes an auth token
    And the response includes the username
    When the user requests their profile
    Then the response status is 200
    And the response includes the username
