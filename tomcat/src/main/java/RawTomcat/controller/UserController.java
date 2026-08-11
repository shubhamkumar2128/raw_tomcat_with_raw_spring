package RawTomcat.controller;

import RawTomcat.dispatcher.annotations.*;


@MyController
public class UserController {

    @MyGetMapping("/users")
    public String getUsers() {
        return "Users: [Shubham, Rahul, Priya]";
    }

    @MyGetMapping("/users/{id}")
    public String getUserById(@MyPathVariable("id") String id) {
        return "User found with id: " + id + " → Shubham Kumar";
    }

    @MyPostMapping("/users")
    public String createUser(@MyRequestParam("name") String name) {
        return "Created user: " + name;
    }

    @MyDeleteMapping("/users/{id}")
    public String deleteUser(@MyPathVariable("id") String id) {
        return "Deleted user with id: " + id;
    }
}
