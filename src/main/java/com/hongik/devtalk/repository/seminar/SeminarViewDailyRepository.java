package com.hongik.devtalk.repository.seminar;

import com.hongik.devtalk.domain.SeminarViewDaily;
import com.hongik.devtalk.domain.SeminarViewDaily.SeminarViewDailyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SeminarViewDailyRepository extends JpaRepository<SeminarViewDaily, SeminarViewDailyId> {
    List<SeminarViewDaily> findByIdSeminarIdAndIdViewDateBetween(Long seminarId, LocalDate from, LocalDate to);
}