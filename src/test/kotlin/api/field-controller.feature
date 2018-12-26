Feature: As a user I can get field configuration

  Scenario: I just started
    Given url 'http://localhost:8080/field'
    When method get
    Then status 200

  Scenario: I want for specific position on the field
    Given url 'http://localhost:8080/field'
    And request { x: "1", y: "1" }
    When method get
    Then status 200


  Scenario: I want to post some cell
    Given url 'http://localhost:8080/field'
    And request { x: "1", y: "1", kind: "X" }
    When method post
    Then status 200

  Scenario: I want to delete some cell
    Given url 'http://localhost:8080/field'
    When method delete
    Then status 200
    And match response.code == 500
    And match response == {id: '#present', code: 500, fieldConfiguration: '#present', error: {code: 500, message: '#present'}}
