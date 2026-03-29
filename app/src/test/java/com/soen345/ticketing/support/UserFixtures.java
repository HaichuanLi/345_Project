package com.soen345.ticketing.support;

import com.soen345.ticketing.domain.user.Role;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserStatus;

import java.util.UUID;

public final class UserFixtures {
    private UserFixtures() {
    }

    public static User customer(String email, String passwordHash) {
        return new User(
                UUID.randomUUID(),
                "Customer User",
                email,
                null,
                passwordHash,
                Role.CUSTOMER,
                UserStatus.ACTIVE
        );
    }

    public static User customerWithPhone(String phone, String passwordHash) {
        return new User(
                UUID.randomUUID(),
                "Customer User",
                null,
                phone,
                passwordHash,
                Role.CUSTOMER,
                UserStatus.ACTIVE
        );
    }

    public static User admin(String email, String passwordHash) {
        return new User(
                UUID.randomUUID(),
                "Admin User",
                email,
                null,
                passwordHash,
                Role.ADMIN,
                UserStatus.ACTIVE
        );
    }
}
