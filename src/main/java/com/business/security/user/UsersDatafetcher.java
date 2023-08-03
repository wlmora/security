package com.business.security.user;

import com.business.security.generated.DgsConstants.MUTATION;
import com.business.security.generated.types.UserInput;
import com.netflix.graphql.dgs.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@SuppressWarnings("DgsInputArgumentValidationInspector")
@DgsComponent
@RequiredArgsConstructor
public class UsersDatafetcher {

    private final UserService userService;

    @DgsQuery
    @PreAuthorize("hasRole('user_client') or hasRole('admin_client')")
    public List<User> users(@InputArgument String usernameFilter) {
        return userService.users(usernameFilter);
    }

    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.CreateUser)
    @PreAuthorize("hasRole('admin_client')")
    public User createUser(@InputArgument("input") UserInput userInput) {
        return userService.createUser(userInput);
    }

    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.DeleteUser)
    @PreAuthorize("hasRole('admin_client')")
    public Boolean deleteUser(@InputArgument("id") String userId) {
        return userService.deleteUser(userId);
    }

    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UpdateUser)
    @PreAuthorize("hasRole('admin_client')")
    public User updateUser(@InputArgument("id") String userId, @InputArgument("input") UserInput userInput) {
        return userService.updateUser(userId, userInput);
    }

}
