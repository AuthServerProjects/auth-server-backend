package com.behpardakht.side_pay.auth.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "authorizations")
public class Authorizations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "authorization_id")
    private String authorizationId;

    @Column(name = "registered_client_id")
    private String registeredClientId;

    @Column(name = "principal_name")
    private String principalName;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "refresh_token")
    private String refreshToken;
}