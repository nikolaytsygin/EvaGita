package com.eva.evagita.migration;

import com.eva.evagita.config.FlywayPostgresTestConfiguration;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles("migration-test")
@Import(FlywayMigrationIntegrationTest.RabbitDisabledConfiguration.class)
class FlywayMigrationIntegrationTest extends FlywayPostgresTestConfiguration {

    @TestConfiguration(proxyBeanMethods = false)
    static class RabbitDisabledConfiguration {

        @Bean(name = "rabbitListenerContainerFactory")
        SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
                ConnectionFactory connectionFactory,
                JacksonJsonMessageConverter messageConverter
        ) {
            SimpleRabbitListenerContainerFactory factory =
                    new SimpleRabbitListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            factory.setMessageConverter(messageConverter);
            factory.setAutoStartup(false);
            return factory;
        }
    }

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "users", "projects", "tags", "tasks", "task_tags", "notifications"
    );

    private static final Set<String> EXPECTED_FOREIGN_KEYS = Set.of(
            "fk9y21adhxn0ayjhfocscqox7bh",
            "fkhswfwa3ga88vxv1pmboss6jhm",
            "fk_tags_user",
            "fk7xi1reghkj37gqwlr1ujxrxll",
            "fkeiqe3k9ent7icelm1cihqn164",
            "fk_tasks_user",
            "fksfhn82y57i3k9uxww1s007acc"
    );

    private static final Set<String> EXPECTED_PRIMARY_KEYS = Set.of(
            "users_pkey", "projects_pkey", "tags_pkey", "tasks_pkey",
            "task_tags_pkey", "notifications_pkey"
    );

    private static final Set<String> EXPECTED_UNIQUES = Set.of(
            "uk6dotkott2kjsp8vw4d0m25fb7",
            "ukr43af9ap4edm43mmtq01oddj6",
            "uk_tag_user_name"
    );

    private static final Set<String> EXPECTED_CHECKS = Set.of(
            "tasks_priority_check", "tasks_status_check", "notifications_type_check"
    );

    private static final Set<String> EXPECTED_IDENTITY_SEQUENCES = Set.of(
            "users_id_seq", "projects_id_seq", "tags_id_seq",
            "tasks_id_seq", "notifications_id_seq"
    );

    private static final Set<String> EXPECTED_INDEXES = Set.of(
            "users_pkey", "projects_pkey", "tags_pkey", "uk_tag_user_name",
            "tasks_pkey", "task_tags_pkey", "notifications_pkey",
            "uk6dotkott2kjsp8vw4d0m25fb7", "ukr43af9ap4edm43mmtq01oddj6"
    );

    private static final Map<String, Map<String, ExpectedColumn>> EXPECTED_COLUMNS = Map.of(
            "users", Map.ofEntries(
                    Map.entry("id", identityColumn("bigint")),
                    Map.entry("created_at", requiredColumn("timestamp(6) without time zone")),
                    Map.entry("email", requiredColumn("character varying(255)")),
                    Map.entry("password", requiredColumn("character varying(255)")),
                    Map.entry("username", requiredColumn("character varying(255)")),
                    Map.entry("token_version", columnWithDefault(
                            "bigint", "0"))
            ),
            "projects", Map.ofEntries(
                    Map.entry("id", identityColumn("bigint")),
                    Map.entry("created_at", requiredColumn("timestamp(6) without time zone")),
                    Map.entry("description", nullableColumn("text")),
                    Map.entry("name", requiredColumn("character varying(255)")),
                    Map.entry("updated_at", requiredColumn("timestamp(6) without time zone")),
                    Map.entry("user_id", requiredColumn("bigint"))
            ),
            "tags", Map.ofEntries(
                    Map.entry("id", identityColumn("bigint")),
                    Map.entry("name", requiredColumn("character varying(255)")),
                    Map.entry("user_id", requiredColumn("bigint"))
            ),
            "tasks", Map.ofEntries(
                    Map.entry("id", identityColumn("bigint")),
                    Map.entry("created_at", requiredColumn("timestamp(6) without time zone")),
                    Map.entry("description", nullableColumn("text")),
                    Map.entry("due_date", nullableColumn("date")),
                    Map.entry("priority", requiredColumn("character varying(255)")),
                    Map.entry("status", requiredColumn("character varying(255)")),
                    Map.entry("title", requiredColumn("character varying(255)")),
                    Map.entry("updated_at", requiredColumn("timestamp(6) without time zone")),
                    Map.entry("project_id", nullableColumn("bigint")),
                    Map.entry("user_id", requiredColumn("bigint"))
            ),
            "task_tags", Map.ofEntries(
                    Map.entry("task_id", requiredColumn("bigint")),
                    Map.entry("tag_id", requiredColumn("bigint"))
            ),
            "notifications", Map.ofEntries(
                    Map.entry("id", identityColumn("bigint")),
                    Map.entry("created_at", requiredColumn("timestamp(6) without time zone")),
                    Map.entry("message", requiredColumn("text")),
                    Map.entry("read", requiredColumn("boolean")),
                    Map.entry("type", requiredColumn("character varying(255)")),
                    Map.entry("updated_at", requiredColumn("timestamp(6) without time zone")),
                    Map.entry("user_id", requiredColumn("bigint"))
            )
    );

    private static final Map<String, String> EXPECTED_SEQUENCE_OWNERS = Map.of(
            "users.id", "public.users_id_seq",
            "projects.id", "public.projects_id_seq",
            "tags.id", "public.tags_id_seq",
            "tasks.id", "public.tasks_id_seq",
            "notifications.id", "public.notifications_id_seq"
    );

    private static final Set<ForeignKeyDefinition> EXPECTED_FOREIGN_KEY_DEFINITIONS = Set.of(
            new ForeignKeyDefinition(
                    "fk9y21adhxn0ayjhfocscqox7bh", "notifications", "user_id",
                    "users", "id", "NO ACTION", "NO ACTION"),
            new ForeignKeyDefinition(
                    "fkhswfwa3ga88vxv1pmboss6jhm", "projects", "user_id",
                    "users", "id", "NO ACTION", "NO ACTION"),
            new ForeignKeyDefinition(
                    "fk_tags_user", "tags", "user_id",
                    "users", "id", "NO ACTION", "NO ACTION"),
            new ForeignKeyDefinition(
                    "fk7xi1reghkj37gqwlr1ujxrxll", "task_tags", "task_id",
                    "tasks", "id", "NO ACTION", "NO ACTION"),
            new ForeignKeyDefinition(
                    "fkeiqe3k9ent7icelm1cihqn164", "task_tags", "tag_id",
                    "tags", "id", "NO ACTION", "NO ACTION"),
            new ForeignKeyDefinition(
                    "fk_tasks_user", "tasks", "user_id",
                    "users", "id", "NO ACTION", "NO ACTION"),
            new ForeignKeyDefinition(
                    "fksfhn82y57i3k9uxww1s007acc", "tasks", "project_id",
                    "projects", "id", "NO ACTION", "NO ACTION")
    );

    private static final Set<IndexDefinition> EXPECTED_INDEX_DEFINITIONS = Set.of(
            new IndexDefinition("users_pkey", "users", true, true, List.of("id")),
            new IndexDefinition("projects_pkey", "projects", true, true, List.of("id")),
            new IndexDefinition("tags_pkey", "tags", true, true, List.of("id")),
            new IndexDefinition("uk_tag_user_name", "tags", true, false,
                    List.of("user_id", "name")),
            new IndexDefinition("tasks_pkey", "tasks", true, true, List.of("id")),
            new IndexDefinition("task_tags_pkey", "task_tags", true, true,
                    List.of("task_id", "tag_id")),
            new IndexDefinition("notifications_pkey", "notifications", true, true,
                    List.of("id")),
            new IndexDefinition("uk6dotkott2kjsp8vw4d0m25fb7", "users", true, false,
                    List.of("email")),
            new IndexDefinition("ukr43af9ap4edm43mmtq01oddj6", "users", true, false,
                    List.of("username"))
    );

    private static final Map<String, CheckExpectation> EXPECTED_CHECKS_BY_NAME = Map.of(
            "tasks_priority_check", new CheckExpectation(
                    "tasks_priority_check", "tasks", "priority",
                    Set.of("LOW", "MEDIUM", "HIGH")),
            "tasks_status_check", new CheckExpectation(
                    "tasks_status_check", "tasks", "status",
                    Set.of("TODO", "IN_PROGRESS", "DONE")),
            "notifications_type_check", new CheckExpectation(
                    "notifications_type_check", "notifications", "type",
                    Set.of("TASK_CREATED", "TASK_UPDATED", "TASK_COMPLETED", "TASK_OVERDUE"))
    );

    private static final Pattern CHECK_LITERAL_PATTERN = Pattern.compile("'([A-Z_]+)'");

    private record ExpectedColumn(
            String dataType,
            boolean nullable,
            String defaultExpression,
            String identityGeneration
    ) {
    }

    private record ActualColumn(
            String tableName,
            String columnName,
            String dataType,
            boolean nullable,
            String defaultExpression,
            String identityGeneration
    ) {
    }

    private record ForeignKeyDefinition(
            String name,
            String childTable,
            String childColumn,
            String parentTable,
            String parentColumn,
            String onUpdate,
            String onDelete
    ) {
    }

    private record IndexDefinition(
            String name,
            String tableName,
            boolean unique,
            boolean primary,
            List<String> columns
    ) {
    }

    private record CheckExpectation(
            String name,
            String tableName,
            String columnName,
            Set<String> values
    ) {
    }

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void migratesV1AndValidatesTheCompleteSchema() {
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion().toString()).isEqualTo("1");
        assertThat(flyway.info().current().getDescription()).isEqualTo("initial schema");

        Set<String> tables = jdbcTemplate.queryForList(
                """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_type = 'BASE TABLE'
                  AND table_name <> 'flyway_schema_history'
                """,
                String.class
        ).stream().collect(Collectors.toSet());
        assertThat(tables).containsExactlyInAnyOrderElementsOf(EXPECTED_TABLES);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT to_regclass('public.daily_notes')", String.class
        )).isNull();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_schema = 'public' AND is_identity = 'YES'",
                Integer.class
        )).isEqualTo(5);

        assertColumnDefinitions();
        assertIdentityDefinitions();
        assertConstraintNames("f", EXPECTED_FOREIGN_KEYS);
        assertConstraintNames("p", EXPECTED_PRIMARY_KEYS);
        assertConstraintNames("u", EXPECTED_UNIQUES);
        assertConstraintNames("c", EXPECTED_CHECKS);
        assertCheckDefinitions();
        assertForeignKeyDefinitions();
        assertIndexDefinitions();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_type "
                        + "WHERE typnamespace = 'public'::regnamespace AND typtype = 'e'",
                Integer.class
        )).isZero();

        assertThat(flyway.info().applied()).hasSize(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history "
                        + "WHERE version = '1' AND success = true",
                Integer.class
        )).isEqualTo(1);
    }

    @Test
    void repeatedMigrateIsIdempotentAndChecksumRemainsStable() {
        MigrationInfo before = flyway.info().current();
        Integer checksumBefore = before.getChecksum();
        int tableCountBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' "
                        + "AND table_name <> 'flyway_schema_history'",
                Integer.class
        );

        flyway.migrate();
        flyway.validate();
        flyway.migrate();

        MigrationInfo after = flyway.info().current();
        assertThat(after.getChecksum()).isEqualTo(checksumBefore);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history "
                        + "WHERE version = '1' AND success = true",
                Integer.class
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' "
                        + "AND table_name <> 'flyway_schema_history'",
                Integer.class
        )).isEqualTo(tableCountBefore);
    }

    private void assertColumnDefinitions() {
        Map<String, Map<String, ActualColumn>> actualColumns = jdbcTemplate.query(
                """
                SELECT c.relname,
                       a.attname,
                       format_type(a.atttypid, a.atttypmod),
                       NOT a.attnotnull,
                       pg_get_expr(d.adbin, d.adrelid),
                       CASE a.attidentity
                           WHEN 'a' THEN 'ALWAYS'
                           WHEN 'd' THEN 'BY DEFAULT'
                           ELSE NULL
                       END
                FROM pg_attribute a
                JOIN pg_class c ON c.oid = a.attrelid
                JOIN pg_namespace n ON n.oid = c.relnamespace
                LEFT JOIN pg_attrdef d
                    ON d.adrelid = a.attrelid AND d.adnum = a.attnum
                WHERE n.nspname = 'public'
                  AND c.relkind = 'r'
                  AND c.relname <> 'flyway_schema_history'
                  AND a.attnum > 0
                  AND NOT a.attisdropped
                """,
                (rs, rowNum) -> new ActualColumn(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getBoolean(4),
                        rs.getString(5),
                        rs.getString(6)
                )
        ).stream().collect(Collectors.groupingBy(
                ActualColumn::tableName,
                Collectors.toMap(ActualColumn::columnName, column -> column)
        ));

        assertThat(actualColumns.keySet())
                .withFailMessage("Unexpected table set in column catalog: %s", actualColumns.keySet())
                .containsExactlyInAnyOrderElementsOf(EXPECTED_COLUMNS.keySet());

        EXPECTED_COLUMNS.forEach((tableName, expectedTableColumns) -> {
            Map<String, ActualColumn> actualTableColumns = actualColumns.get(tableName);
            assertThat(actualTableColumns)
                    .withFailMessage("Missing column metadata for table %s", tableName)
                    .isNotNull();
            assertThat(actualTableColumns.keySet())
                    .withFailMessage("Unexpected columns for table %s", tableName)
                    .containsExactlyInAnyOrderElementsOf(expectedTableColumns.keySet());

            expectedTableColumns.forEach((columnName, expected) -> {
                ActualColumn actual = actualTableColumns.get(columnName);
                String location = tableName + "." + columnName;
                assertThat(actual.dataType())
                        .withFailMessage("Unexpected data type for %s", location)
                        .isEqualTo(expected.dataType());
                assertThat(actual.nullable())
                        .withFailMessage("Unexpected nullable setting for %s", location)
                        .isEqualTo(expected.nullable());
                assertThat(normalizeDefault(actual.defaultExpression()))
                        .withFailMessage("Unexpected default expression for %s", location)
                        .isEqualTo(normalizeDefault(expected.defaultExpression()));
                assertThat(actual.identityGeneration())
                        .withFailMessage("Unexpected identity generation for %s", location)
                        .isEqualTo(expected.identityGeneration());
            });
        });
    }

    private void assertIdentityDefinitions() {
        Map<String, String> actualIdentityColumns = jdbcTemplate.query(
                """
                SELECT table_name || '.' || column_name, identity_generation
                FROM information_schema.columns
                WHERE table_schema = 'public' AND is_identity = 'YES'
                """,
                (rs, rowNum) -> Map.entry(rs.getString(1), rs.getString(2))
        ).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, String> expectedIdentityColumns = EXPECTED_COLUMNS.entrySet().stream()
                .flatMap(table -> table.getValue().entrySet().stream()
                        .filter(column -> column.getValue().identityGeneration() != null)
                        .map(column -> Map.entry(
                                table.getKey() + "." + column.getKey(),
                                column.getValue().identityGeneration()
                        )))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(actualIdentityColumns)
                .withFailMessage("Unexpected identity column definitions")
                .isEqualTo(expectedIdentityColumns);

        Set<String> actualSequences = jdbcTemplate.queryForList(
                """
                SELECT c.relname
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = 'public' AND c.relkind = 'S'
                """,
                String.class
        ).stream().collect(Collectors.toSet());
        assertThat(actualSequences)
                .withFailMessage("Unexpected public sequence set")
                .containsExactlyInAnyOrderElementsOf(EXPECTED_IDENTITY_SEQUENCES);

        Map<String, String> actualOwnership = jdbcTemplate.query(
                """
                SELECT table_class.relname || '.' || table_column.attname,
                       sequence_schema.nspname || '.' || sequence_class.relname
                FROM pg_depend dependency
                JOIN pg_class sequence_class ON sequence_class.oid = dependency.objid
                JOIN pg_namespace sequence_schema ON sequence_schema.oid = sequence_class.relnamespace
                JOIN pg_class table_class ON table_class.oid = dependency.refobjid
                JOIN pg_namespace table_schema ON table_schema.oid = table_class.relnamespace
                JOIN pg_attribute table_column
                    ON table_column.attrelid = table_class.oid
                   AND table_column.attnum = dependency.refobjsubid
                WHERE dependency.deptype = 'i'
                  AND sequence_class.relkind = 'S'
                  AND sequence_schema.nspname = 'public'
                  AND table_schema.nspname = 'public'
                """,
                (rs, rowNum) -> Map.entry(rs.getString(1), rs.getString(2))
        ).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertThat(actualOwnership)
                .withFailMessage("Unexpected identity sequence ownership")
                .isEqualTo(EXPECTED_SEQUENCE_OWNERS);

        for (String sequence : EXPECTED_IDENTITY_SEQUENCES) {
            Map<String, Object> parameters = jdbcTemplate.queryForMap(
                    "SELECT seqstart, seqincrement, seqmin, seqmax, seqcache, seqcycle "
                            + "FROM pg_sequence WHERE seqrelid = ?::regclass",
                    "public." + sequence
            );
            assertThat(parameters.get("seqstart")).isEqualTo(1L);
            assertThat(parameters.get("seqincrement")).isEqualTo(1L);
            assertThat(parameters.get("seqmin")).isEqualTo(1L);
            assertThat(parameters.get("seqmax")).isEqualTo(Long.MAX_VALUE);
            assertThat(parameters.get("seqcache")).isEqualTo(1L);
            assertThat(parameters.get("seqcycle")).isEqualTo(false);
        }
    }

    private void assertForeignKeyDefinitions() {
        Set<ForeignKeyDefinition> actualDefinitions = jdbcTemplate.query(
                """
                SELECT constraint_data.conname,
                       child_table.relname,
                       child_column.attname,
                       parent_table.relname,
                       parent_column.attname,
                       CASE constraint_data.confupdtype
                           WHEN 'a' THEN 'NO ACTION'
                           WHEN 'r' THEN 'RESTRICT'
                           WHEN 'c' THEN 'CASCADE'
                           WHEN 'n' THEN 'SET NULL'
                           WHEN 'd' THEN 'SET DEFAULT'
                       END,
                       CASE constraint_data.confdeltype
                           WHEN 'a' THEN 'NO ACTION'
                           WHEN 'r' THEN 'RESTRICT'
                           WHEN 'c' THEN 'CASCADE'
                           WHEN 'n' THEN 'SET NULL'
                           WHEN 'd' THEN 'SET DEFAULT'
                       END
                FROM pg_constraint constraint_data
                JOIN pg_class child_table ON child_table.oid = constraint_data.conrelid
                JOIN pg_namespace child_schema ON child_schema.oid = child_table.relnamespace
                JOIN pg_class parent_table ON parent_table.oid = constraint_data.confrelid
                JOIN LATERAL unnest(constraint_data.conkey) WITH ORDINALITY
                    child_key(attnum, ord) ON true
                JOIN LATERAL unnest(constraint_data.confkey) WITH ORDINALITY
                    parent_key(attnum, ord) ON parent_key.ord = child_key.ord
                JOIN pg_attribute child_column
                    ON child_column.attrelid = child_table.oid
                   AND child_column.attnum = child_key.attnum
                JOIN pg_attribute parent_column
                    ON parent_column.attrelid = parent_table.oid
                   AND parent_column.attnum = parent_key.attnum
                WHERE constraint_data.contype = 'f'
                  AND child_schema.nspname = 'public'
                """,
                (rs, rowNum) -> new ForeignKeyDefinition(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7)
                )
        ).stream().collect(Collectors.toSet());

        assertThat(actualDefinitions)
                .withFailMessage("Unexpected foreign key definitions")
                .containsExactlyInAnyOrderElementsOf(EXPECTED_FOREIGN_KEY_DEFINITIONS);
    }

    private void assertCheckDefinitions() {
        Map<String, ActualCheck> actualChecks = jdbcTemplate.query(
                """
                SELECT constraint_data.conname,
                       table_data.relname,
                       pg_get_constraintdef(constraint_data.oid)
                FROM pg_constraint constraint_data
                JOIN pg_class table_data ON table_data.oid = constraint_data.conrelid
                JOIN pg_namespace table_schema ON table_schema.oid = table_data.relnamespace
                WHERE constraint_data.contype = 'c'
                  AND table_schema.nspname = 'public'
                """,
                (rs, rowNum) -> Map.entry(
                        rs.getString(1), new ActualCheck(rs.getString(2), rs.getString(3)))
        ).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(actualChecks.keySet())
                .containsExactlyInAnyOrderElementsOf(EXPECTED_CHECKS_BY_NAME.keySet());

        EXPECTED_CHECKS_BY_NAME.forEach((name, expected) -> {
            ActualCheck actual = actualChecks.get(name);
            assertThat(actual.tableName())
                    .withFailMessage("Unexpected table for CHECK %s", name)
                    .isEqualTo(expected.tableName());
            assertThat(actual.definition())
                    .withFailMessage("CHECK %s does not reference expected column", name)
                    .contains(expected.columnName());
            assertThat(actual.definition().toUpperCase())
                    .withFailMessage("CHECK %s is not an enum membership check", name)
                    .contains("ANY");

            Matcher matcher = CHECK_LITERAL_PATTERN.matcher(actual.definition());
            Set<String> actualValues = new java.util.HashSet<>();
            while (matcher.find()) {
                actualValues.add(matcher.group(1));
            }
            assertThat(actualValues)
                    .withFailMessage("Unexpected allowed values for CHECK %s", name)
                    .containsExactlyInAnyOrderElementsOf(expected.values());
        });
    }

    private void assertIndexDefinitions() {
        Set<IndexDefinition> actualDefinitions = jdbcTemplate.query(
                """
                SELECT index_data.relname,
                       table_data.relname,
                       index_metadata.indisunique,
                       index_metadata.indisprimary,
                       string_agg(index_column.attname, ',' ORDER BY index_key.ord)
                FROM pg_index index_metadata
                JOIN pg_class index_data ON index_data.oid = index_metadata.indexrelid
                JOIN pg_namespace index_schema ON index_schema.oid = index_data.relnamespace
                JOIN pg_class table_data ON table_data.oid = index_metadata.indrelid
                JOIN LATERAL unnest(index_metadata.indkey) WITH ORDINALITY
                    index_key(attnum, ord) ON true
                JOIN pg_attribute index_column
                    ON index_column.attrelid = table_data.oid
                   AND index_column.attnum = index_key.attnum
                WHERE index_schema.nspname = 'public'
                  AND table_data.relname <> 'flyway_schema_history'
                GROUP BY index_data.relname, table_data.relname,
                         index_metadata.indisunique, index_metadata.indisprimary
                """,
                (rs, rowNum) -> new IndexDefinition(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getBoolean(3),
                        rs.getBoolean(4),
                        List.of(rs.getString(5).split(","))
                )
        ).stream().collect(Collectors.toSet());

        assertThat(actualDefinitions)
                .withFailMessage("Unexpected index definitions")
                .containsExactlyInAnyOrderElementsOf(EXPECTED_INDEX_DEFINITIONS);
        assertThat(actualDefinitions)
                .allMatch(IndexDefinition::unique, "all V1 indexes must be unique backing indexes");
    }

    private static ExpectedColumn requiredColumn(String dataType) {
        return new ExpectedColumn(dataType, false, null, null);
    }

    private static ExpectedColumn nullableColumn(String dataType) {
        return new ExpectedColumn(dataType, true, null, null);
    }

    private static ExpectedColumn identityColumn(String dataType) {
        return new ExpectedColumn(dataType, false, null, "BY DEFAULT");
    }

    private static ExpectedColumn columnWithDefault(String dataType, String defaultExpression) {
        return new ExpectedColumn(dataType, false, defaultExpression, null);
    }

    private static String normalizeDefault(String expression) {
        if (expression == null) {
            return null;
        }
        return expression
                .toLowerCase()
                .replaceAll("\\s+", "")
                .replace("::bigint", "");
    }

    private void assertConstraintNames(String type, Set<String> expectedNames) {
        List<String> actualNames = jdbcTemplate.queryForList(
                "SELECT conname FROM pg_constraint "
                        + "WHERE contype = ? AND connamespace = 'public'::regnamespace "
                        + "AND conrelid <> 'public.flyway_schema_history'::regclass",
                String.class,
                type
        );
        assertThat(actualNames).containsExactlyInAnyOrderElementsOf(expectedNames);
    }

    private record ActualCheck(String tableName, String definition) {
    }
}
