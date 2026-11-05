package com.acme.coldtrace.platform.assetmanagement.infrastructure.identity;

import com.acme.coldtrace.platform.assetmanagement.application.ports.OrganizationDirectory;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IdentityOrganizationDirectory implements OrganizationDirectory {
  private final RestClient identity;
  public IdentityOrganizationDirectory(@Value("${coldtrace.identity.url}")String url,
      @Value("${coldtrace.internal.service-key}")String key) {
    if(key==null||key.length()<32)throw new IllegalArgumentException("Internal service key is required");
    var factory=new org.springframework.http.client.JdkClientHttpRequestFactory(
      java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
    factory.setReadTimeout(Duration.ofSeconds(5));
    identity=RestClient.builder().baseUrl(url).requestFactory(factory).defaultHeader("X-Service-Key",key).build();
  }
  @Override public boolean existsById(Long id) {
    if(id==null||id<1)return false;
    return Boolean.TRUE.equals(identity.get().uri("/internal/identity/organizations/{id}/exists",id).retrieve().body(Boolean.class));
  }
}
