package org.github.guardjo.dientitian.scheduler.api.utils;

import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;

public class TestDataGenerator {
    private TestDataGenerator() {
    }

    public static AccountEntity accountEntity(String username, String name, String password) {
        return AccountEntity.builder()
                .username(username)
                .name(name)
                .password(password)
                .build();
    }

    public static AccountEntity accountEntity(long id, String username, String name, String password) {
        return AccountEntity.builder()
                .id(id)
                .username(username)
                .name(name)
                .password(password)
                .build();
    }
}
