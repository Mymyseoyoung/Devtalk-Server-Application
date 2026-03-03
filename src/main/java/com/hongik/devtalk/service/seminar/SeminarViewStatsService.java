package com.hongik.devtalk.service.seminar;

import com.hongik.devtalk.domain.SeminarViewDaily;
import com.hongik.devtalk.domain.SeminarViewLog;
import com.hongik.devtalk.repository.seminar.SeminarViewDailyRepository;
import com.hongik.devtalk.repository.seminar.SeminarViewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SeminarViewStatsService {

    private final SeminarViewLogRepository logRepo;
    private final SeminarViewDailyRepository dailyRepo;

    // 본문 진입(200 성공 이후) 시 호출
    @Transactional
    public void recordSeminarView(Long seminarId, String browserId) {
        if (browserId == null || browserId.isBlank()) return;

        LocalDate today = LocalDate.now();

        try {
            logRepo.save(SeminarViewLog.of(seminarId, browserId, today)); // UNIQUE로 하루 1회

            SeminarViewDaily.SeminarViewDailyId id =
                    new SeminarViewDaily.SeminarViewDailyId(seminarId, today);

            Optional<SeminarViewDaily> opt = dailyRepo.findById(id);
            if (opt.isPresent()) {
                SeminarViewDaily d = opt.get();
                d.increment();
                dailyRepo.save(d);
            } else {
                dailyRepo.save(SeminarViewDaily.create(seminarId, today));
            }
        } catch (DataIntegrityViolationException ignore) {
            // 중복이면 무시
        }
    }

    @Transactional(readOnly = true)
    public List<ViewPoint> getDailyGraph(Long seminarId, LocalDate from, LocalDate to) {
        var rows = dailyRepo.findByIdSeminarIdAndIdViewDateBetween(seminarId, from, to);

        Map<LocalDate, Integer> map = new HashMap<>();
        for (var r : rows) map.put(r.getId().getViewDate(), r.getViewCount());

        List<ViewPoint> points = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            points.add(new ViewPoint(d.toString(), map.getOrDefault(d, 0)));
        }
        return points;
    }

    public record ViewPoint(String date, int count) {}
}