package com.hongik.devtalk.repository.seminar;

import com.hongik.devtalk.domain.SearchKeywordDaily;
import com.hongik.devtalk.domain.SearchKeywordDaily.SearchKeywordDailyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SearchKeywordDailyRepository extends JpaRepository<SearchKeywordDaily, SearchKeywordDailyId> {

    @Query("""
           select d.id.keywordNorm, sum(d.searchCount)
           from SearchKeywordDaily d
           where d.id.searchDate between :from and :to
             and (:target = 'ALL' or d.id.targetType = :target)
           group by d.id.keywordNorm
           order by sum(d.searchCount) desc
           """)
    List<Object[]> findTopKeywords(@Param("target") String target,
                                   @Param("from") LocalDate from,
                                   @Param("to") LocalDate to);
}
