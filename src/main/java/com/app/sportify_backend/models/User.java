package com.app.sportify_backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Document(collection = "users")
@Getter
@Setter
public class User implements UserDetails {

    @Id
    private String id;

    private String firstname;
    private String lastname;


    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String phone;

    @JsonIgnore
    private String password;

    private Role role;

    @Indexed(unique = true)
    private String playerCode;

    private boolean isEnabled = true;

    private LocalDateTime registrationDate;
    private LocalDateTime activationDate;

    private String profileImageUrl;
    private String resetOtp;
    private LocalDateTime otpExpiration;

    @JsonIgnore
    private String refreshToken;

    private List<String> teamIds = new ArrayList<>();

    private String activeTeamId;

    @JsonIgnore
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Pitch pendingPitch;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pitchId;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return Collections.emptyList();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

}
