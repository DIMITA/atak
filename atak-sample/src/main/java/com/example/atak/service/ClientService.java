package com.example.atak.service;

import atak.generated.dto.ClientRequestDto;
import atak.generated.service.AbstractClientService;
import com.example.atak.entity.Client;
import io.atak.annotation.AtakHook;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Custom ClientService — shows how to override lifecycle + security hooks.
 */
@Service
public class ClientService extends AbstractClientService {

    // --- lifecycle hooks ---

    @Override
    @AtakHook(AtakHook.Phase.BEFORE_CREATE)
    protected void atakBeforeCreate(ClientRequestDto dto) {
        if (dto.getEmail() != null) {
            dto.setEmail(dto.getEmail().toLowerCase().trim());
        }
    }

    // --- security hooks (generated because @AtakSecurity is on Client) ---

    @Override
    protected void atakCheckCreate(Authentication auth) {
        requireRole(auth, "ROLE_USER");
    }

    @Override
    protected void atakCheckRead(Authentication auth) {
        requireRole(auth, "ROLE_USER");
    }

    @Override
    protected void atakCheckUpdate(Authentication auth, Client entity) {
        requireRole(auth, "ROLE_USER");
    }

    @Override
    protected void atakCheckDelete(Authentication auth, Client entity) {
        requireRole(auth, "ROLE_ADMIN");
    }

    private void requireRole(Authentication auth, String role) {
        if (auth == null || !auth.getAuthorities().contains(new SimpleGrantedAuthority(role))) {
            throw new AccessDeniedException("Required role: " + role);
        }
    }
}
