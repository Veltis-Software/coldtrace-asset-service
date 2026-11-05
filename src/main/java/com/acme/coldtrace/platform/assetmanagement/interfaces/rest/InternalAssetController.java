package com.acme.coldtrace.platform.assetmanagement.interfaces.rest;

import com.acme.coldtrace.platform.assetmanagement.interfaces.acl.AssetManagementContextFacade;
import com.acme.coldtrace.platform.assetmanagement.interfaces.acl.AssetManagementContextFacade.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Owner snapshots for service-to-service reads; the security filter requires a workload key. */
@RestController
@RequestMapping("/internal/assets/organizations/{organizationId}")
public class InternalAssetController {
  private final AssetManagementContextFacade assets;
  public InternalAssetController(AssetManagementContextFacade assets) { this.assets = assets; }

  @GetMapping("/assets/{assetId}")
  public AssetSnapshot asset(@PathVariable Long organizationId, @PathVariable Long assetId) {
    return assets.fetchAssetByIdAndOrganizationId(organizationId, assetId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @GetMapping("/devices/{deviceId}")
  public IoTDeviceSnapshot device(@PathVariable Long organizationId, @PathVariable Long deviceId) {
    return assets.fetchIoTDeviceByIdAndOrganizationId(organizationId, deviceId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @GetMapping("/gateways/{gatewayId}")
  public GatewaySnapshot gateway(@PathVariable Long organizationId, @PathVariable Long gatewayId) {
    return assets.fetchGatewayByIdAndOrganizationId(organizationId, gatewayId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @GetMapping("/assets/{assetId}/settings")
  public AssetSettingsSnapshot settings(@PathVariable Long organizationId, @PathVariable Long assetId) {
    return assets.fetchEffectiveAssetSettingsByAssetId(organizationId, assetId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @GetMapping("/assigned-devices")
  public List<IoTDeviceSnapshot> assigned(@PathVariable Long organizationId,
      @RequestParam(required = false) Long assetId) {
    return assets.fetchAssignedIoTDevices(organizationId, assetId);
  }
}
