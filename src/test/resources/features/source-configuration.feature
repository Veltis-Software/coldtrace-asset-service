@assets
Feature: Measurement sources associated with refrigerated assets
  As a configuration manager
  I want sources to stay bound to the right device and asset
  So that readings keep their origin and the asset history is preserved

  @US30
  Scenario: A reader consults the source configuration history but cannot change it
    Given a user of organization 7 with permissions "assets:view"
    When the user consults the source configuration history of device 10
    Then the response status is 200
    When the user replaces the source of device 10 with "probe-a"
    Then the response status is 403

  @US03 @US29
  Scenario: Requests from other organizations never reach the asset owner
    Given a user of organization 8 with permissions "assets:manage"
    When the user consults the source configuration history of device 10
    Then the response status is 403
    And the source configuration service was not called

  @US03 @US29
  Scenario: A source replacement with an invalid version is rejected
    Given a user of organization 7 with permissions "assets:manage"
    When the user replaces the source of device 10 with "probe-a" using expected version "1.5"
    Then the response status is 400
    And the source configuration service was not called

  @US26
  Scenario: Device metadata can be edited while keeping its asset, gateway and identity
    Given device 10 of organization 7 managed by a source configuration on gateway 3 and asset 20
    When its metadata is updated keeping gateway 3 and asset 20
    Then the update succeeds

  @US29
  Scenario Outline: A managed device cannot be reassigned through a metadata update
    Given device 10 of organization 7 managed by a source configuration on gateway 3 and asset 20
    When its metadata is updated with gateway <gateway>, asset <asset> and <identity> identity
    Then the update is rejected because the source configuration manages the device

    Examples:
      | gateway | asset | identity |
      | 3       | 21    | the same |
      | 4       | 20    | the same |
      | 3       | 20    | a new    |

  @US27
  Scenario: A managed device cannot be deleted and lose its history
    Given device 10 of organization 7 managed by a source configuration on gateway 3 and asset 20
    When the device is deleted
    Then the update is rejected because the source configuration manages the device
