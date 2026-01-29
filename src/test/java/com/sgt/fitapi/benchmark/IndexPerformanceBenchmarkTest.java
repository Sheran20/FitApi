package com.sgt.fitapi.benchmark;

import com.sgt.fitapi.model.Exercise;
import com.sgt.fitapi.model.User;
import com.sgt.fitapi.model.WorkoutSession;
import com.sgt.fitapi.model.WorkoutSet;
import com.sgt.fitapi.repository.ExerciseRepository;
import com.sgt.fitapi.repository.UserRepository;
import com.sgt.fitapi.repository.WorkoutSessionRepository;
import com.sgt.fitapi.repository.WorkoutSetRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Performance benchmark test to measure the impact of database indexes.
 *
 * This test:
 * 1. Populates the database with test data
 * 2. Runs queries WITH indexes and measures execution time
 * 3. Drops indexes and runs the same queries
 * 4. Compares and reports the performance difference
 *
 * PREREQUISITE: Start the docker-compose database first:
 *   docker compose up -d
 *
 * Run with: mvn test -Dtest=IndexPerformanceBenchmarkTest
 *   or from IDE with the "docker" profile active
 *
 * NOTE: This test uses the docker-compose PostgreSQL database.
 *       Test data is cleaned up after the test completes.
 */
@SpringBootTest
@ActiveProfiles("docker")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IndexPerformanceBenchmarkTest {

    // Configuration - adjust these for larger/smaller tests
    // Moderate dataset for faster test runs while still showing differences
    private static final int NUM_USERS = 100;
    private static final int SESSIONS_PER_USER = 50;
    private static final int SETS_PER_SESSION = 5;
    private static final int JVM_WARMUP_ITERATIONS = 10;  // Warm up JVM before any measurement
    private static final int WARMUP_ITERATIONS = 3;
    private static final int BENCHMARK_ITERATIONS = 10;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WorkoutSessionRepository sessionRepository;

    @Autowired
    private WorkoutSetRepository setRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final Map<String, List<Long>> withIndexResults = new LinkedHashMap<>();
    private final Map<String, List<Long>> withoutIndexResults = new LinkedHashMap<>();

    private Long testUserId;
    private Long testSessionId;
    private Long testExerciseId;
    private final List<Long> createdUserIds = new ArrayList<>();
    private final List<Long> createdSessionIds = new ArrayList<>();

    @BeforeAll
    void setupTestData() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("INDEX PERFORMANCE BENCHMARK");
        System.out.println("=".repeat(70));
        System.out.println("Configuration:");
        System.out.println("  - Users: " + NUM_USERS);
        System.out.println("  - Sessions per user: " + SESSIONS_PER_USER);
        System.out.println("  - Sets per session: " + SETS_PER_SESSION);
        System.out.println("  - Total sessions: " + (NUM_USERS * SESSIONS_PER_USER));
        System.out.println("  - Total sets: " + (NUM_USERS * SESSIONS_PER_USER * SETS_PER_SESSION));
        System.out.println("=".repeat(70));

        System.out.println("\nGenerating test data...");
        long startTime = System.currentTimeMillis();

        // Get existing exercises from seed data
        List<Exercise> exercises = exerciseRepository.findAll();
        if (exercises.isEmpty()) {
            throw new IllegalStateException("No exercises found - check Flyway migration");
        }
        testExerciseId = exercises.get(0).getId();

        // Create test users
        List<User> users = new ArrayList<>();
        for (int i = 0; i < NUM_USERS; i++) {
            User user = new User(
                    "benchmark" + i + "@test.com",
                    "$2a$10$dummyhashedpassword",
                    "Benchmark User " + i
            );
            users.add(user);
        }
        userRepository.saveAll(users);
        for (User user : users) {
            createdUserIds.add(user.getId());
        }
        testUserId = users.get(0).getId();

        // Create workout sessions
        Random random = ThreadLocalRandom.current();
        List<WorkoutSession> allSessions = new ArrayList<>();

        for (User user : users) {
            for (int s = 0; s < SESSIONS_PER_USER; s++) {
                WorkoutSession session = new WorkoutSession();
                session.setUserId(user.getId());
                session.setStartedAt(Instant.now().minus(random.nextInt(365), ChronoUnit.DAYS));
                session.setTimezone("UTC");
                session.setNotes("Benchmark session " + s);
                allSessions.add(session);
            }
        }
        sessionRepository.saveAll(allSessions);
        for (WorkoutSession session : allSessions) {
            createdSessionIds.add(session.getId());
        }
        testSessionId = allSessions.get(0).getId();

        // Create workout sets
        List<WorkoutSet> allSets = new ArrayList<>();
        for (WorkoutSession session : allSessions) {
            for (int setNum = 1; setNum <= SETS_PER_SESSION; setNum++) {
                WorkoutSet set = new WorkoutSet();
                set.setWorkoutSession(session);
                set.setExercise(exercises.get(random.nextInt(exercises.size())));
                set.setSetNumber(setNum);
                set.setReps(random.nextInt(12) + 1);
                set.setWeight(random.nextDouble() * 100 + 20);
                set.setRpe(random.nextDouble() * 4 + 6);
                allSets.add(set);
            }
        }
        setRepository.saveAll(allSets);

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Test data generated in " + elapsed + "ms");
        System.out.println("  - Users: " + userRepository.count());
        System.out.println("  - Sessions: " + sessionRepository.count());
        System.out.println("  - Sets: " + setRepository.count());
    }

    @AfterAll
    void cleanup() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CLEANUP");
        System.out.println("=".repeat(70));

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.execute(status -> {
            // Recreate indexes (in case they were dropped)
            // Note: idx_users_email is NOT recreated - it's redundant with the UNIQUE constraint index
            System.out.println("Recreating indexes...");
            entityManager.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_workout_sets_session ON workout_sets (workout_session_id)").executeUpdate();
            entityManager.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_workout_sets_exercise ON workout_sets (exercise_id)").executeUpdate();
            entityManager.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_workout_sets_session_exercise ON workout_sets (workout_session_id, exercise_id)").executeUpdate();
            entityManager.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_workout_sessions_user_started ON workout_sessions (user_id, started_at DESC)").executeUpdate();
            entityManager.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_workout_sets_session_setnum ON workout_sets (workout_session_id, set_number)").executeUpdate();

            // Delete test data
            System.out.println("Deleting test data...");
            entityManager.createNativeQuery("DELETE FROM workout_sets WHERE workout_session_id IN (SELECT id FROM workout_sessions WHERE notes LIKE 'Benchmark session%')").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM workout_sessions WHERE notes LIKE 'Benchmark session%'").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM users WHERE email LIKE 'benchmark%@test.com'").executeUpdate();

            return null;
        });

        System.out.println("Cleanup complete.");
    }

    @Test
    @Order(1)
    void jvmWarmup() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("JVM WARMUP PHASE");
        System.out.println("=".repeat(70));
        System.out.println("Warming up JVM, HikariCP, Hibernate (" + JVM_WARMUP_ITERATIONS + " iterations)...");

        // Run queries multiple times to warm up JIT compiler, connection pool, etc.
        for (int i = 0; i < JVM_WARMUP_ITERATIONS; i++) {
            entityManager.clear();
            runAllQueries();
        }
        System.out.println("JVM warmup complete. Both benchmarks will now run on warm JVM.");
    }

    @Test
    @Order(2)
    @Transactional
    void dropIndexes() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DROPPING INDEXES (running WITHOUT indexes first)");
        System.out.println("=".repeat(70));

        entityManager.createNativeQuery("DROP INDEX IF EXISTS idx_workout_sets_session").executeUpdate();
        entityManager.createNativeQuery("DROP INDEX IF EXISTS idx_workout_sets_exercise").executeUpdate();
        entityManager.createNativeQuery("DROP INDEX IF EXISTS idx_workout_sets_session_exercise").executeUpdate();
        entityManager.createNativeQuery("DROP INDEX IF EXISTS idx_workout_sessions_user_started").executeUpdate();
        entityManager.createNativeQuery("DROP INDEX IF EXISTS idx_workout_sets_session_setnum").executeUpdate();

        System.out.println("Indexes dropped successfully.");
    }

    @Test
    @Order(3)
    void benchmarkWithoutIndexes() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("BENCHMARK WITHOUT INDEXES (Sequential Scans)");
        System.out.println("=".repeat(70));

        runBenchmarkSuite(withoutIndexResults);
        printResults("WITHOUT INDEXES", withoutIndexResults);
    }

    @Test
    @Order(4)
    @Transactional
    void recreateIndexes() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("RECREATING INDEXES");
        System.out.println("=".repeat(70));

        entityManager.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_workout_sets_session ON workout_sets (workout_session_id)").executeUpdate();
        entityManager.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_workout_sets_exercise ON workout_sets (exercise_id)").executeUpdate();
        entityManager.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_workout_sets_session_exercise ON workout_sets (workout_session_id, exercise_id)").executeUpdate();
        entityManager.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_workout_sessions_user_started ON workout_sessions (user_id, started_at DESC)").executeUpdate();
        entityManager.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_workout_sets_session_setnum ON workout_sets (workout_session_id, set_number)").executeUpdate();

        System.out.println("Indexes recreated successfully.");
    }

    @Test
    @Order(5)
    void benchmarkWithIndexes() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("BENCHMARK WITH INDEXES");
        System.out.println("=".repeat(70));

        runBenchmarkSuite(withIndexResults);
        printResults("WITH INDEXES", withIndexResults);
    }

    @Test
    @Order(6)
    void printComparison() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PERFORMANCE COMPARISON");
        System.out.println("=".repeat(70));

        System.out.printf("%-45s %12s %12s %10s%n",
                "Query", "With Index", "No Index", "Speedup");
        System.out.println("-".repeat(70));

        for (String queryName : withIndexResults.keySet()) {
            double avgWithIndex = average(withIndexResults.get(queryName));
            double avgWithoutIndex = average(withoutIndexResults.get(queryName));
            double speedup = avgWithoutIndex / avgWithIndex;

            System.out.printf("%-45s %10.2fms %10.2fms %9.1fx%n",
                    queryName, avgWithIndex, avgWithoutIndex, speedup);
        }

        System.out.println("=".repeat(70));
        System.out.println("CONCLUSION:");
        System.out.println("Values > 1.0x indicate index is faster.");
        System.out.println("Larger datasets will show more dramatic differences.");
        System.out.println("=".repeat(70));
    }

    private void runBenchmarkSuite(Map<String, List<Long>> results) {
        // Clear entity manager cache before each suite
        entityManager.clear();

        // Warmup
        System.out.println("Warming up (" + WARMUP_ITERATIONS + " iterations)...");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runAllQueries();
        }

        // Benchmark
        System.out.println("Running benchmark (" + BENCHMARK_ITERATIONS + " iterations)...");
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            entityManager.clear(); // Clear cache between iterations

            results.computeIfAbsent("User lookup by email", k -> new ArrayList<>())
                    .add(measureQuery(() -> userRepository.findByEmail("benchmark50@test.com")));

            results.computeIfAbsent("User sessions (paginated, sorted)", k -> new ArrayList<>())
                    .add(measureQuery(() -> sessionRepository.findByUserId(testUserId,
                            PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "startedAt")))));

            results.computeIfAbsent("Sessions in date range", k -> new ArrayList<>())
                    .add(measureQuery(() -> sessionRepository.findByUserIdAndStartedAtBetween(
                            testUserId,
                            Instant.now().minus(30, ChronoUnit.DAYS),
                            Instant.now(),
                            PageRequest.of(0, 20))));

            results.computeIfAbsent("Sets for session", k -> new ArrayList<>())
                    .add(measureQuery(() -> setRepository.findByWorkoutSessionId(testSessionId)));

            results.computeIfAbsent("Sets for session + exercise", k -> new ArrayList<>())
                    .add(measureQuery(() -> setRepository.findByWorkoutSessionIdAndExerciseId(
                            testSessionId, testExerciseId)));

            results.computeIfAbsent("Check email exists", k -> new ArrayList<>())
                    .add(measureQuery(() -> userRepository.existsByEmail("benchmark75@test.com")));
        }
    }

    private void runAllQueries() {
        userRepository.findByEmail("benchmark50@test.com");
        sessionRepository.findByUserId(testUserId,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "startedAt")));
        sessionRepository.findByUserIdAndStartedAtBetween(
                testUserId,
                Instant.now().minus(30, ChronoUnit.DAYS),
                Instant.now(),
                PageRequest.of(0, 20));
        setRepository.findByWorkoutSessionId(testSessionId);
        setRepository.findByWorkoutSessionIdAndExerciseId(testSessionId, testExerciseId);
        userRepository.existsByEmail("benchmark75@test.com");
    }

    private long measureQuery(Runnable query) {
        long start = System.nanoTime();
        query.run();
        return (System.nanoTime() - start) / 1_000_000; // Convert to milliseconds
    }

    private void printResults(String label, Map<String, List<Long>> results) {
        System.out.println("\nResults (" + label + "):");
        System.out.printf("%-45s %10s %10s %10s%n", "Query", "Avg (ms)", "Min (ms)", "Max (ms)");
        System.out.println("-".repeat(75));

        for (Map.Entry<String, List<Long>> entry : results.entrySet()) {
            List<Long> times = entry.getValue();
            System.out.printf("%-45s %10.2f %10d %10d%n",
                    entry.getKey(),
                    average(times),
                    Collections.min(times),
                    Collections.max(times));
        }
    }

    private double average(List<Long> values) {
        return values.stream().mapToLong(Long::longValue).average().orElse(0);
    }
}
