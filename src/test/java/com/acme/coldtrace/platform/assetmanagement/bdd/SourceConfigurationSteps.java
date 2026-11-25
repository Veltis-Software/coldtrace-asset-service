package com.acme.coldtrace.platform.assetmanagement.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.acme.coldtrace.platform.assetmanagement.application.commandservices.IoTDeviceCommandFailure;
import com.acme.coldtrace.platform.assetmanagement.application.internal.commandservices.IoTDeviceCommandServiceImpl;
import com.acme.coldtrace.platform.assetmanagement.application.internal.commandservices.SourceConfigurationService;
import com.acme.coldtrace.platform.assetmanagement.application.ports.OrganizationDirectory;
import com.acme.coldtrace.platform.assetmanagement.domain.model.aggregates.Asset;
import com.acme.coldtrace.platform.assetmanagement.domain.model.aggregates.Gateway;
import com.acme.coldtrace.platform.assetmanagement.domain.model.aggregates.IoTDevice;
import com.acme.coldtrace.platform.assetmanagement.domain.model.valueobjects.SourceConfiguration;
import com.acme.coldtrace.platform.assetmanagement.domain.model.commands.DeleteIoTDeviceCommand;
import com.acme.coldtrace.platform.assetmanagement.domain.model.commands.UpdateIoTDeviceCommand;
import com.acme.coldtrace.platform.assetmanagement.domain.model.valueobjects.IoTDeviceUuid;
import com.acme.coldtrace.platform.assetmanagement.domain.repositories.AssetRepository;
import com.acme.coldtrace.platform.assetmanagement.domain.repositories.GatewayRepository;
import com.acme.coldtrace.platform.assetmanagement.domain.repositories.IoTDeviceRepository;
import com.acme.coldtrace.platform.assetmanagement.domain.repositories.SourceConfigurationRepository;
import com.acme.coldtrace.platform.assetmanagement.interfaces.rest.SourceConfigurationsController;
import com.acme.coldtrace.platform.billing.interfaces.acl.SubscriptionBillingContextFacade;
import com.acme.coldtrace.platform.iam.interfaces.acl.SessionContextFacade;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class SourceConfigurationSteps {
  private static final String DEVICE_UUID = "e846df25-9981-4911-9552-4143bf79f596";
  private static final String REQUEST =
      """
      {"assetId":20,"expectedVersion":0,"sourceId":"SOURCE","transport":"http",
       "profileVersion":1,"schemaVersion":1,"temperatureUnit":"C","humidityUnit":"%RH",
       "fields":{"temperature":"temp","humidity":"rh","recordedAt":"timestamp"},"reason":"Sensor replacement"}
      """;

  private final UsernamePasswordAuthenticationToken principal =
      new UsernamePasswordAuthenticationToken("operator@example.test", "unused", List.of());
  private SourceConfigurationService service;
  private SessionContextFacade sessions;
  private MockMvc mvc;
  private int status;

  private IoTDeviceRepository devices;
  private IoTDeviceCommandServiceImpl deviceService;
  private Object outcome;

  @Before
  public void prepare() {
    service = mock(SourceConfigurationService.class);
    sessions = mock(SessionContextFacade.class);
    mvc = MockMvcBuilders.standaloneSetup(new SourceConfigurationsController(service, sessions)).build();
  }

  @Given("a user of organization {long} with permissions {string}")
  public void user(long organizationId, String permissions) {
    when(sessions.resolve(principal.getName()))
        .thenReturn(
            Optional.of(
                new SessionContextFacade.SessionContext(
                    12L, organizationId, "Operator", Arrays.asList(permissions.split(",")))));
  }

  @When("the user consults the source configuration history of device {long}")
  public void history(long deviceId) throws Exception {
    status = mvc.perform(get(path(deviceId) + "/history").principal(principal)).andReturn().getResponse().getStatus();
  }

  @When("the user replaces the source of device {long} with {string}")
  public void replace(long deviceId, String sourceId) throws Exception {
    replaceWithVersion(deviceId, sourceId, "0");
  }

  @When("the user replaces the source of device {long} with {string} using expected version {string}")
  public void replaceWithVersion(long deviceId, String sourceId, String version) throws Exception {
    String body = REQUEST.replace("SOURCE", sourceId).replace("\"expectedVersion\":0", "\"expectedVersion\":" + version);
    status =
        mvc.perform(put(path(deviceId)).principal(principal).contentType(MediaType.APPLICATION_JSON).content(body))
            .andReturn()
            .getResponse()
            .getStatus();
  }

  @Then("the response status is {int}")
  public void responseStatus(int expected) {
    assertThat(status).isEqualTo(expected);
  }

  @And("the source configuration service was not called")
  public void notCalled() {
    verifyNoInteractions(service);
  }

  @Given("device {long} of organization {long} managed by a source configuration on gateway {long} and asset {long}")
  public void managedDevice(long deviceId, long organizationId, long gatewayId, long assetId) {
    devices = mock(IoTDeviceRepository.class);
    var gateways = mock(GatewayRepository.class);
    var assets = mock(AssetRepository.class);
    var organizations = mock(OrganizationDirectory.class);
    var sources = mock(SourceConfigurationRepository.class);
    when(organizations.existsById(organizationId)).thenReturn(true);
    var gateway = mock(Gateway.class);
    when(gateway.getLocationId()).thenReturn(1L);
    var asset = mock(Asset.class);
    when(asset.getLocationId()).thenReturn(1L);
    when(gateways.findByIdAndOrganizationId(anyLong(), eq(organizationId))).thenReturn(Optional.of(gateway));
    when(assets.findByIdAndOrganizationId(anyLong(), eq(organizationId))).thenReturn(Optional.of(asset));
    var device =
        new IoTDevice(
            deviceId, organizationId, gatewayId, new IoTDeviceUuid(DEVICE_UUID), "sensor", "TH-120",
            "temperature", List.of("temperature"), 30, assetId, "online", "calibrated",
            LocalDate.now(), LocalDate.now().plusYears(1));
    when(devices.lockByIdAndOrganizationId(deviceId, organizationId)).thenReturn(Optional.of(device));
    when(devices.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(sources.find(organizationId, deviceId)).thenReturn(Optional.of(mock(SourceConfiguration.class)));
    deviceService =
        new IoTDeviceCommandServiceImpl(
            devices, gateways, assets, organizations, mock(SubscriptionBillingContextFacade.class), sources);
  }

  @When("its metadata is updated keeping gateway {long} and asset {long}")
  public void updateKeeping(long gatewayId, long assetId) {
    outcome = deviceService.handle(update(gatewayId, DEVICE_UUID, assetId));
  }

  @When("its metadata is updated with gateway {long}, asset {long} and {word} {word} identity")
  public void updateWith(long gatewayId, long assetId, String article, String kind) {
    String uuid = "new".equals(kind) ? UUID.randomUUID().toString() : DEVICE_UUID;
    outcome = deviceService.handle(update(gatewayId, uuid, assetId));
  }

  @When("the device is deleted")
  public void delete() {
    outcome = deviceService.handle(new DeleteIoTDeviceCommand(7L, 10L));
  }

  @Then("the update succeeds")
  public void succeeds() throws Exception {
    assertThat((Boolean) outcome.getClass().getMethod("isSuccess").invoke(outcome)).isTrue();
    verify(devices).save(any());
  }

  @Then("the update is rejected because the source configuration manages the device")
  public void rejected() throws Exception {
    Optional<?> failure = (Optional<?>) outcome.getClass().getMethod("failure").invoke(outcome);
    assertThat(failure.orElseThrow()).isInstanceOf(IoTDeviceCommandFailure.ManagedSourceConfiguration.class);
    verify(devices, never()).save(any());
    verify(devices, never()).deleteById(anyLong());
  }

  private static String path(long deviceId) {
    return "/api/v1/organizations/7/iot-devices/" + deviceId + "/source-configuration";
  }

  private static UpdateIoTDeviceCommand update(long gatewayId, String uuid, long assetId) {
    return new UpdateIoTDeviceCommand(
        7L, 10L, gatewayId, uuid, "sensor", "TH-120", "temperature", List.of("temperature"), 30, assetId,
        "online", "calibrated", LocalDate.now(), LocalDate.now().plusYears(1));
  }
}
