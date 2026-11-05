package com.acme.coldtrace.platform.iam.interfaces.acl;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.acme.coldtrace.platform.assetmanagement.infrastructure.security.AssetSecurity;
@Component
public class SessionContextFacade {
 public record SessionContext(Long userId,Long organizationId,String fullName,List<String>permissions) {}
 public Optional<SessionContext> resolve(String principal) {
  if(!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes))return Optional.empty();
  Object value=attributes.getRequest().getAttribute("identitySession");
  if(!(value instanceof AssetSecurity.Session session)||!session.userId().toString().equals(principal))return Optional.empty();
  return Optional.of(new SessionContext(session.userId(),session.organizationId(),session.fullName(),session.permissions()));
 }
}
