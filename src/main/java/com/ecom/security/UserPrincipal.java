package com.ecom.security;


import com.ecom.entity.User;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String name;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean accountLocked;
    private boolean enabled;

    public static UserPrincipal create(User user, Collection<? extends GrantedAuthority> authorities) {
        return UserPrincipal.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountLocked(Boolean.TRUE.equals(user.getIsLocked()))
                .enabled(Boolean.TRUE.equals(user.getIsVerified()))
                .build();
    }

    @Override
    public String getUsername() {
        // keep email as username, id is available via getId()
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
