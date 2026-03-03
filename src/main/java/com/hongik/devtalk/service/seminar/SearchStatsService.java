package com.hongik.devtalk.service.seminar;

import com.hongik.devtalk.domain.SearchKeywordDaily;
import com.hongik.devtalk.domain.SearchLogHourly;
import com.hongik.devtalk.repository.seminar.SearchKeywordDailyRepository;
import com.hongik.devtalk.repository.seminar.SearchLogHourlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchStatsService {

    public static final String TARGET_SEMINAR = "SEMINAR";
    public static final String TARGET_SPEAKER = "SPEAKER";
    public static final String TARGET_ALL = "ALL";

    private final SearchLogHourlyRepository logRepo;
    private final SearchKeywordDailyRepository dailyRepo;

    @Transactional
    public void recordSearch(String targetType, String rawKeyword, String browserId) {
        String keywordNorm = normalize(rawKeyword);
        if (keywordNorm.isEmpty()) return;                 // ✅ 빈값 제외
        if (browserId == null || browserId.isBlank()) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hourBucket = now.withMinute(0).withSecond(0).withNano(0); // ✅ 1시간 버킷

        try {
            // ✅ 1시간 중복 제거 (UNIQUE)
            logRepo.save(SearchLogHourly.of(targetType, browserId, keywordNorm, hourBucket));

            LocalDate today = now.toLocalDate();
            SearchKeywordDaily.SearchKeywordDailyId id =
                    new SearchKeywordDaily.SearchKeywordDailyId(targetType, keywordNorm, today);

            Optional<SearchKeywordDaily> opt = dailyRepo.findById(id);
            if (opt.isPresent()) {
                var d = opt.get();
                d.increment();
                dailyRepo.save(d);
            } else {
                dailyRepo.save(SearchKeywordDaily.create(targetType, keywordNorm, today));
            }
        } catch (DataIntegrityViolationException ignore) {
            // 1시간 내 재검색이면 무시
        }
    }

    @Transactional(readOnly = true)
    public List<TopKeyword> getTop5(String target, LocalDate from, LocalDate to) {
        List<Object[]> rows = dailyRepo.findTopKeywords(target, from, to);
        List<TopKeyword> result = new ArrayList<>();
        for (int i = 0; i < Math.min(5, rows.size()); i++) {
            Object[] r = rows.get(i);
            String keyword = (String) r[0];
            Long cnt = (Long) r[1];
            result.add(new TopKeyword(keyword, cnt.intValue()));
        }
        return result;
    }

    public record TopKeyword(String keyword, int count) {}

    private String normalize(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.isEmpty()) return "";
        t = t.replaceAll("\\s+", " ");
        t = t.toLowerCase();
        return t;
    }
}