package com.business.security.user;

import com.business.security.util.KeycloakProvider;
import lombok.Builder;
import lombok.Data;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class User {

    private String id;
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private List<String> roles;

    public static List<User> mapFrom(List<UserRepresentation> userInputs) {
        List<User> users = new ArrayList<>();
        for (UserRepresentation input : userInputs) {
            List<RoleRepresentation> roleRep =
                    KeycloakProvider.getUserResource().get(input.getId()).roles().realmLevel().listAll();
            users.add(User.builder()
                    .id(input.getId())
                    .username(input.getUsername())
                    .firstname(input.getFirstName())
                    .lastname(input.getLastName())
                    .email(input.getEmail())
                    .roles(roleRep
                            .stream().
                            map(RoleRepresentation::getName)
                            .collect(Collectors.toList()))
                    .build());
        }
        return  users;
    }
    public static User mapFrom(UserResource userInputs) {
        List<RoleRepresentation> roleRep = userInputs.roles().realmLevel().listAll();
        return  User.builder()
                .id(userInputs.toRepresentation().getId())
                .username(userInputs.toRepresentation().getUsername())
                .firstname(userInputs.toRepresentation().getFirstName())
                .lastname(userInputs.toRepresentation().getLastName())
                .email(userInputs.toRepresentation().getEmail())
                .roles(roleRep
                        .stream().
                        map(RoleRepresentation::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}
