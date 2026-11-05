package com.acme.coldtrace.platform.assetmanagement.infrastructure.subscriptions;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionServiceAdapterTest {
  private static final String KEY = "isolated-contract-service-key-at-least-32-characters";
  private HttpServer owner;
  private SubscriptionServiceAdapter adapter;
  private int status;
  private String body;

  @BeforeEach void startOwner() throws Exception {
    owner = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    owner.createContext("/internal/organizations/7/entitlements/locations", exchange -> {
      assertEquals(KEY, exchange.getRequestHeaders().getFirst("X-Service-Key"));
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(status, bytes.length);
      exchange.getResponseBody().write(bytes);
      exchange.close();
    });
    owner.start();
    adapter = new SubscriptionServiceAdapter("http://127.0.0.1:" + owner.getAddress().getPort(), KEY);
  }

  @AfterEach void stopOwner() { owner.stop(0); }

  @Test void preservesExplicitPlanDenial() {
    status = 200;
    body = "{\"organizationId\":7,\"key\":\"locations\",\"enabled\":false,\"used\":1,\"limit\":1}";
    var decision = adapter.checkEntitlement(7L, "locations").orElseThrow();
    assertFalse(decision.enabled());
    assertEquals(1, decision.used());
  }

  @Test void unavailableOwnerDoesNotBecomeAPlanDenial() {
    status = 503; body = "{}";
    assertEquals(503, assertThrows(ResponseStatusException.class,
        () -> adapter.canUseEntitlement(7L, "locations")).getStatusCode().value());
  }

  @Test void anotherOrganizationsDecisionCannotAuthorizeAnOperation() {
    status = 200;
    body = "{\"organizationId\":8,\"key\":\"locations\",\"enabled\":true}";
    assertEquals(503, assertThrows(ResponseStatusException.class,
        () -> adapter.canUseEntitlement(7L, "locations")).getStatusCode().value());
  }
}
