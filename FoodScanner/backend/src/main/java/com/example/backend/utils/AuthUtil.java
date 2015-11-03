package com.example.backend.utils;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;

/**
 * Created by mlenarto on 9/26/15.
 */
public class AuthUtil {

    /**
     * Throws an exception if the user object doesn't represent an authenticated
     * call.
     * @param user User object to be checked if it represents an authenticated
     *      caller.
     * @throws com.google.api.server.spi.response.UnauthorizedException when the
     *      user object does not represent an admin.
     */
    public static void throwIfNotAuthenticated(final User user) throws
            UnauthorizedException {
        if (user == null || user.getEmail() == null) {
            throw new UnauthorizedException(
                    "Only authenticated users may invoke this operation");
        }
    }
}
