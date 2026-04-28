package com.smartcampus.crowd.repository;

import com.smartcampus.crowd.model.CrowdData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CrowdRepository extends JpaRepository<CrowdData, Long> {

    // ── Derived query methods (Spring generates SQL from method name) ──────────
    Optional<CrowdData> findTopByLocationOrderByTimestampDesc(String location);
    Optional<CrowdData> findTopByLocationAndSourceOrderByTimestampDesc(String location, String source);
    List<CrowdData>     findTop10ByLocationAndSourceOrderByTimestampDesc(String location, String source);
    List<CrowdData>     findTop3ByLocationOrderByTimestampDesc(String location);
    List<CrowdData>     findTop10ByLocationOrderByTimestampDesc(String location);
    List<CrowdData>     findByLocation(String location);
    List<CrowdData>     findByLocationAndSourceOrderByTimestampAsc(String location, String source);
    List<CrowdData>     findByLocationAndSource(String location, String source);
    long                countByLocationAndSource(String location, String source);

    // ── Native SQL queries (all use timestamp column name) ────────────────────









    /**
     * Best-time query: quietest hour (9-17) for today's day-of-week.
     * Index 0 = lowest average crowd.
     */
    @Query(value = "SELECT HOUR(timestamp) as hour, ROUND(AVG(crowd_count), 1) as avg_count " +
                   "FROM crowd_data " +
                   "WHERE location = :location AND source = 'Historical' " +
                   "AND DAYOFWEEK(timestamp) = DAYOFWEEK(CURDATE()) " +
                   "AND HOUR(timestamp) BETWEEN 9 AND 17 " +
                   "GROUP BY HOUR(timestamp) " +
                   "ORDER BY avg_count ASC, HOUR(timestamp) ASC", nativeQuery = true)
    List<Object[]> getBestHoursForLocation(@Param("location") String location);

    /**
     * Prediction query: historical avg for next hour on today's day-of-week.
     */
    @Query(value = "SELECT AVG(crowd_count) FROM crowd_data " +
                   "WHERE location = :location AND source = 'Historical' " +
                   "AND DAYOFWEEK(timestamp) = DAYOFWEEK(CURDATE()) " +
                   "AND HOUR(timestamp) = :hour", nativeQuery = true)
    Double getPredictionAvg(@Param("location") String location, @Param("hour") int hour);



    /** Fallback best-time: any day-of-week, hours 9-17, ordered by lowest avg. */
    @Query(value = "SELECT HOUR(timestamp) as hour, ROUND(AVG(crowd_count), 1) as avg_count " +
                   "FROM crowd_data " +
                   "WHERE location = :location AND source = 'Historical' " +
                   "AND HOUR(timestamp) BETWEEN 9 AND 17 " +
                   "GROUP BY HOUR(timestamp) " +
                   "ORDER BY avg_count ASC, HOUR(timestamp) ASC", nativeQuery = true)
    List<Object[]> getBestHoursForLocationAnyDay(@Param("location") String location);

    /** Fallback prediction: any day-of-week avg for a given hour. */
    @Query(value = "SELECT AVG(crowd_count) FROM crowd_data " +
                   "WHERE location = :location AND source = 'Historical' " +
                   "AND HOUR(timestamp) = :hour", nativeQuery = true)
    Double getPredictionAvgAnyDay(@Param("location") String location, @Param("hour") int hour);

    /**
     * Cleanup job: returns Simulated rows older than cutoff for batch deletion.
     * Historical rows are never deleted.
     */
    @Query("SELECT c FROM CrowdData c WHERE c.source = 'Simulated' AND c.timestamp < :cutoff")
    List<CrowdData> findSimulatedOlderThan(@Param("cutoff") java.time.LocalDateTime cutoff);
}
