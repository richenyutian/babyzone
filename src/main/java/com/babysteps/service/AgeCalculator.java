package com.babysteps.service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

@Component
public class AgeCalculator {

    public long daysSinceBirth(LocalDate birthDate, LocalDate targetDate) {
        if (targetDate.isBefore(birthDate)) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(birthDate, targetDate);
    }

    public String formatAge(LocalDate birthDate, LocalDate targetDate) {
        if (targetDate.isBefore(birthDate)) {
            return "未出生";
        }

        Period period = Period.between(birthDate, targetDate);
        if (period.getYears() > 0) {
            return period.getYears() + "岁" + period.getMonths() + "个月" + period.getDays() + "天";
        }
        if (period.getMonths() > 0) {
            return period.getMonths() + "个月" + period.getDays() + "天";
        }
        long days = daysSinceBirth(birthDate, targetDate);
        return days + "天";
    }
}
