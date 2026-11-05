package com.acme.coldtrace.platform.assetmanagement.interfaces.rest;

import com.acme.coldtrace.platform.assetmanagement.interfaces.acl.AssetManagementContextFacade;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Service-key authentication is enforced by AssetSecurity before controller dispatch. */
@RestController
public class InternalAssetUsageController {
  public record Usage(int locations,int assets,int iotDevices) {}
  private final AssetManagementContextFacade assets;
  public InternalAssetUsageController(AssetManagementContextFacade assets){this.assets=assets;}
  @GetMapping("/internal/assets/organizations/{id}/usage")
  public Usage usage(@PathVariable long id) {
    if(id<1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    return new Usage(assets.countLocationsByOrganizationId(id),assets.countAssetsByOrganizationId(id),assets.countIotDevicesByOrganizationId(id));
  }
}
