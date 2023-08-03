package com.business.security.user;

import com.business.security.exception.GenericException;
import com.business.security.exception.UserAlreadyExistException;
import com.business.security.generated.types.UserInput;
import com.business.security.util.KeycloakProvider;
import jakarta.ws.rs.core.Response;
import org.jetbrains.annotations.NotNull;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.List;

@Service
public class UserService implements IUserService{

    @Override
    public List<User> users(String usernameFilter) {
        if (usernameFilter == null){
            return User.mapFrom(KeycloakProvider.getRealmResource()
                    .users()
                    .list());
        }
        return User.mapFrom(KeycloakProvider.getRealmResource()
                .users()
                .searchByUsername(usernameFilter, false));
    }

    @Override
    public User createUser(@NonNull UserInput userInput) {
        int status;
        UsersResource usersResource = KeycloakProvider.getUserResource();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(userInput.getFirstname());
        userRepresentation.setLastName(userInput.getLastname());
        userRepresentation.setEmail(userInput.getEmail());
        userRepresentation.setUsername(userInput.getUsername());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);

       Response response = usersResource.create(userRepresentation);
       status = response.getStatus();

        if (status == 201) {
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf("/") + 1);

            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setValue(userInput.getPassword());

            usersResource.get(userId).resetPassword(credentialRepresentation);

            RealmResource realmResource = KeycloakProvider.getRealmResource();

            List<RoleRepresentation> rolesRepresentation;

            if (userInput.getRoles() == null || userInput.getRoles().isEmpty()) {
                rolesRepresentation = List.of(realmResource.roles().get("user").toRepresentation());
            } else {
                rolesRepresentation = realmResource.roles()
                        .list()
                        .stream()
                        .filter(role -> userInput.getRoles()
                                .stream()
                                .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                        .toList();
            }

            realmResource.users().get(userId).roles().realmLevel().add(rolesRepresentation);

            return User.mapFrom(KeycloakProvider.getUserResource().get(userId)); //"User created successfully!!";

        } else if (status == 409) {
            throw new UserAlreadyExistException("User exist already!");
        } else {
            throw new GenericException("Error creating user, please contact with the administrator.");
        }
    }

    @Override
    public Boolean deleteUser(@NonNull String userId) {
        try {
            KeycloakProvider.getUserResource()
                    .get(userId)
                    .remove();
        }catch (Exception exception) {
            return false;
        }
        return true;
    }

    @Override
    public User updateUser(String userId, @NonNull UserInput userInput) {

        RealmResource realmResource = KeycloakProvider.getRealmResource();
        List<RoleRepresentation> rolesRepresentation;
        if (userInput.getRoles() == null || userInput.getRoles().isEmpty()) {
            rolesRepresentation = List.of(realmResource.roles().get("user").toRepresentation());
        } else {
            rolesRepresentation = realmResource.roles()
                    .list()
                    .stream()
                    .filter(role -> userInput.getRoles()
                            .stream()
                            .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                    .toList();
        }

        realmResource.users().get(userId).roles().realmLevel().remove(
                KeycloakProvider.getUserResource().get(userId).roles().realmLevel().listAll());
        realmResource.users().get(userId).roles().realmLevel().add(rolesRepresentation);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(OAuth2Constants.PASSWORD);
        credentialRepresentation.setValue(userInput.getPassword());

        UserRepresentation userRepresentation = getUserRepresentation(userInput, credentialRepresentation);

        UserResource userResource = KeycloakProvider.getUserResource().get(userId);
        userResource.update(userRepresentation);

        return User.mapFrom(userResource);
    }

    @NotNull
    private static UserRepresentation getUserRepresentation(UserInput userInput, CredentialRepresentation credentialRepresentation) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(userInput.getFirstname());
        userRepresentation.setLastName(userInput.getLastname());
        userRepresentation.setEmail(userInput.getEmail());
        userRepresentation.setUsername(userInput.getUsername());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
        return userRepresentation;
    }
}
