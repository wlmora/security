package com.business.security.user;

import com.business.security.generated.types.UserInput;

import java.util.List;

public interface IUserService {
    List<User> users(String usernameFilter);
    User createUser(UserInput userInput);
    Boolean deleteUser(String userId);
    User updateUser(String userId, UserInput userInput);
}
