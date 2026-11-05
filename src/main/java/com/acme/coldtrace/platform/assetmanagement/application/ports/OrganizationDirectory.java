package com.acme.coldtrace.platform.assetmanagement.application.ports;

/** Owner existence query, without identity repositories or aggregates. */
public interface OrganizationDirectory {
  boolean existsById(Long organizationId);
}
