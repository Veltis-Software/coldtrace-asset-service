package com.acme.coldtrace.platform.assetmanagement.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class AssetSecurity {
  public record Session(Long userId,Long organizationId,String fullName,List<String> permissions) {}
  private final RestClient identity;
  private final byte[] key;
  public AssetSecurity(@Value("${coldtrace.identity.url}")String url,
      @Value("${coldtrace.internal.service-key}")String secret) {
    if(secret==null||secret.length()<32)throw new IllegalArgumentException("Internal service key is required");
    key=secret.getBytes(StandardCharsets.UTF_8);
    var factory=new JdkClientHttpRequestFactory(java.net.http.HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2)).build());factory.setReadTimeout(Duration.ofSeconds(5));
    identity=RestClient.builder().baseUrl(url).requestFactory(factory).build();
  }
  @Bean
  SecurityFilterChain assetFilterChain(HttpSecurity http)throws Exception {
    var organizations=Pattern.compile("^/api/v1/organizations/([0-9]+)(?:/.*)?$");
    var filter=new OncePerRequestFilter() {
      @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,
          FilterChain chain)throws IOException,ServletException {
        String path=request.getRequestURI();
        boolean publicRoute="GET".equals(request.getMethod())&&(path.startsWith("/actuator/health")||path.startsWith("/v3/api-docs")||path.startsWith("/swagger-ui"));
        if(publicRoute){chain.doFilter(request,response);return;}
        if(path.startsWith("/internal/")) {
          String candidate=request.getHeader("X-Service-Key");
          if(candidate==null||!MessageDigest.isEqual(key,candidate.getBytes(StandardCharsets.UTF_8))){response.sendError(401);return;}
          chain.doFilter(request,response);return;
        }
        String bearer=request.getHeader("Authorization");
        if(bearer==null||!bearer.startsWith("Bearer ")){response.sendError(401);return;}
        Session session;
        try {session=identity.get().uri("/api/v1/session/context").header("Authorization",bearer).retrieve().body(Session.class);}
        catch(HttpClientErrorException error){response.sendError(error.getStatusCode().value()==401?401:503);return;}
        catch(RestClientException error){response.sendError(503);return;}
        if(session==null||session.userId()==null||session.organizationId()==null){response.sendError(401);return;}
        var match=organizations.matcher(path);
        if(!match.matches()){response.sendError(404);return;}
        if(!session.organizationId().toString().equals(match.group(1))){response.sendError(403);return;}
        if(!"GET".equals(request.getMethod())
            &&(session.permissions()==null||!session.permissions().contains("assets:manage"))) {
          response.sendError(403);return;
        }
        if("GET".equals(request.getMethod())&&(session.permissions()==null||session.permissions().stream().noneMatch(p->p.equals("assets:manage")||p.equals("assets:view")||p.equals("operations:view")))) { response.sendError(403);return; }
        request.setAttribute("identitySession",session);
        var authentication=new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(session.userId().toString(),null,java.util.List.of());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request,response);
      }
    };
    return http.cors(org.springframework.security.config.Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a->a.anyRequest().permitAll())
        .addFilterBefore(filter,UsernamePasswordAuthenticationFilter.class).build();
  }
}
