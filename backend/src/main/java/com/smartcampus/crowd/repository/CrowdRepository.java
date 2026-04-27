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
    Optional<CrowdData> findTopByLocationOrderByTimestampDesc(String location);
    Optional<CrowdData> findTopByLocationAndSourceOrderByTimestampDesc(String location, String source);
    List<CrowdData> findTop10ByLocationAndSourceOrderByTimestampDesc(String location, String source);
    List<CrowdData> findTop3ByLocationOrderByTimestampDesc(String location);
    List<CrowdData> findTop10ByLocationOrderByTimestampDesc(String location);
    List<CrowdData> findByLocation(String location);
    List<CrowdData> findByLocationAndSourceOrderByTimestampAsc(String location, String source);
    List<CrowdData> findByLocationAndSource(String location, String source);
    long countByLocationAndSource(String location, String source);

    @Query(value = "SELECT AVG(crowd_count) FROM crowd_data WHERE location = :location AND HOUR(timestamp) = :hour AND source = :source", nativeQuery = true)
    Double getAverageCrowdByLocationAndHour(@Param("location") String location, @Param("hour") int hour, @Param("source") String source);

    @Query(value = "SELECT AVG(crowd_count) FROM crowd_data " +
                   "WHERE location = :location AND HOUR(timestamp) = :hour " +
                   "AND DAYOFWEEK(timestamp) = :dayOfWeek " +
                   "AND timestamp >= DATE_SUB(NOW(), INTERVAL :days DAY) " +
                   "AND source = :source", nativeQuery = true)
    Double getAverageByLocationHourDayAndDays(@Param("location") String location,
                                              @Param("hour") int hour,
                                              @Param("dayOfWeek") int dayOfWeek,
                                              @Param("days") int days,
                                              @Param("source") String source);

    @Query(value = "SELECT HOUR(timestamp) as hour, AVG(crowd_count) as avg_count FROM crowd_data " +
                   "WHERE location = :location AND source = 'Historical' " +
                   "AND HOUR(timestamp) BETWEEN 9 AND 17 " +
                   "AND DAYOFWEEK(timestamp) = :dayOfWeek " +
                   "GROUP BY HOUR(timestamp) " +
                   "ORDER BY avg_count ASC LIMIT 1", nativeQuery = true)
    List<Object[]> getBestTimeByLocationAndDay(@Param("location") String location,
                                               @Param("dayOfWeek") int dayOfWeek);

    @Query(value = "SELECT HOUR(timestamp) as hour, AVG(crowd_count) as avg_count FROM crowd_data " +
                   "WHERE location = :location AND source = 'Historical' " +
                   "AND HOUR(timestamp) BETWEEN 9 AND 17 " +
                   "AND DAYOFWEEK(timestamp) = :dayOfWeek " +
                   "GROUP BY HOUR(timestamp) " +
                   "ORDER BY HOUR(timestamp) ASC", nativeQuery = true)
    List<Object[]> getAllHourAveragesByLocationAndDay(@Param("location") String location,
                                                     @Param("dayOfWeek") int dayOfWeek);

    /**
     * Best-time query: all hours 9-17 with their avg crowd for this location,
     * filtered to today's day-of-week via DAYOFWEEK(CURDATE()) in SQL.
     * Ordered ASC so index 0 = quietest hour.
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
     * Prediction query: avg crowd for a specific location+hour on today's day-of-week.
     * Always independent of current simulated crowd.
     */
    @Query(value = "SELECT AVG(crowd_count) FROM crowd_data " +
                   "WHERE location = :location AND source = 'Historical' " +
                   "AND DAYOFWEEK(timestamp) = DAYOFWEEK(CURDATE()) " +
                   "AND HOUR(timestamp) = :hour", nativeQuery = true)
    Double getPredictionAvg(@Param("location") String location, @Param("hour") int hour);

    @Query(value = "SELECT HOUR(timestamp) as hour, ROUND(AVG(crowd_count), 0) as avg_count " +
                   "FROM crowd_data " +
                   "WHERE location = :location " +
                   "AND source = 'Historical' " +
                   "AND timestamp >= DATE_SUB(NOW(), INTERVAL 10 DAY) " +
                   "AND HOUR(timestamp) BETWEEN 8 AND 19 " +
                   "GROUP BY HOUR(timestamp) " +
                   "ORDER BY HOUR(timestamp) ASC", nativeQuery = true)
    List<Object[]> getHistoricalHourlyTrend(@Param("location") String location);

    /** Fallback best-time: any day-of-week, hours 9-17, ordered by lowest avg. */
    @Query(value = "SELECT HOUR(timestamp) as hour, ROUND(AVG(crowd_count), 1) as avg_count " +
                   "FROM crowd_data " +
                   "WHERE location = :location AND source = 'Historical' " +
                   "AND HOUR(timestamp) BETWEEN 9 AND 17 " +
                   "GROUP BY HOUR(timestamp) " +
                   "ORDER BY avg_count ASC, HOUR(timestamp) ASC", nativeQuery = true)
    List<Object[]> getBestHoursForLocationAnyDay(@Param("location") String location);

    /** Fallback prediction: any day-of-week avg for next hour. */
    @Query(value = "SELECT AVG(crowd_count) FROM crowd_data " +
                   "WHERE location = :location AND source = 'Historical' " +
                   "AND HOUR(timestamp) = :hour", nativeQuery = true)
    Double getPredictionAvgAnyDay(@Param("location") String location, @Param("hour") int hour);

    /**
     * Used by the daily cleanup job — returns Simulated rows older than the
     * given cutoff so they can be batch-deleted.
     * Historical rows (source = 'Historical') are never touched.
     */
    @Query("SELECT c FROM CrowdData c WHERE c.source = 'Simulated' AND c.timestamp < :cutoff")
    List<CrowdData> findSimulatedOlderThan(@Param("cutoff") java.time.LocalDateTime cutoff);
}
