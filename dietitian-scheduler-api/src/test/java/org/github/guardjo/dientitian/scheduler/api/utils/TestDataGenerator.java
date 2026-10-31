package org.github.guardjo.dientitian.scheduler.api.utils;

import org.github.guardjo.dientitian.scheduler.api.model.entity.AccountEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.PersonalEventEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.RefreshTokenEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.ScheduleEntity;
import org.github.guardjo.dientitian.scheduler.api.model.entity.ShiftTypeEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    public static ShiftTypeEntity shiftTypeEntity(String label, LocalTime startTime, LocalTime endTime, String color) {
        return ShiftTypeEntity.builder()
                .label(label)
                .startTime(startTime)
                .endTime(endTime)
                .color(color)
                .build();
    }

    public static ScheduleEntity scheduleEntity(AccountEntity account, LocalDate workDate, ShiftTypeEntity shiftType) {
        return ScheduleEntity.builder()
                .account(account)
                .workDate(workDate)
                .shiftType(shiftType)
                .build();
    }

    public static RefreshTokenEntity refreshTokenEntity(AccountEntity account, String tokenHash, LocalDateTime expiredAt) {
        return RefreshTokenEntity.builder()
                .account(account)
                .tokenHash(tokenHash)
                .expiredAt(expiredAt)
                .build();
    }

    public static RefreshTokenEntity refreshTokenEntity(Long id, AccountEntity account, String tokenHash, LocalDateTime expiredAt) {
        return RefreshTokenEntity.builder()
                .id(id)
                .account(account)
                .tokenHash(tokenHash)
                .expiredAt(expiredAt)
                .build();
    }

    public static PersonalEventEntity personalEventEntity(AccountEntity account, String title, LocalDate eventDate, LocalTime startTime, LocalTime endTime) {
        return PersonalEventEntity.builder()
                .account(account)
                .title(title)
                .eventDate(eventDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    public static PersonalEventEntity personalEventEntity(Long id, AccountEntity account, String title, LocalDate eventDate, LocalTime startTime, LocalTime endTime) {
        return PersonalEventEntity.builder()
                .id(id)
                .account(account)
                .title(title)
                .eventDate(eventDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }
}
