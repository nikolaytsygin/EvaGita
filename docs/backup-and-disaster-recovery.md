# Backup and Disaster Recovery

## Current configuration

- Database: PostgreSQL (`evagita`)
- PostgreSQL container: `deploy-postgres-1`
- Backup directory: `/var/backups/evagita`
- Backup format: PostgreSQL custom archive (`pg_dump -Fc`)
- Backup script: `/usr/local/sbin/evagita-backup.sh`
- systemd service: `evagita-backup.service`
- systemd timer: `evagita-backup.timer`
- Schedule: daily at 03:15 UTC
- Retention: files older than 14 days are eligible for deletion, using `find -mmin`
- Archive permissions: `0600`
- Checksum: SHA-256 sidecar file (`.dump.sha256`)

Backups are currently stored on the same server as the production database.
Remote or off-site storage is not configured. A server or disk loss can therefore
destroy both the database and its local backups.

## Backup operation

Run a backup manually:

    sudo /usr/local/sbin/evagita-backup.sh

Inspect the latest service result and logs:

    sudo systemctl show evagita-backup.service -p Result -p ExecMainStatus
    sudo journalctl -u evagita-backup.service -n 50 --no-pager

Check the timer:

    systemctl list-timers evagita-backup.timer --all --no-pager
    systemctl is-active evagita-backup.timer

Verify a selected archive's checksum from the backup directory:

    cd /var/backups/evagita
    sha256sum -c evagita-YYYYMMDDTHHMMSSZ.dump.sha256

Replace the example timestamp with the actual archive filename.

## Restore verification

A successful `pg_restore --list` confirms that PostgreSQL can read the archive,
but does not prove that the database can be restored.

The restore procedure was tested using a separate database named
`evagita_restore_test`. The restored tables and row counts were compared with
the source database and matched at the time of the test.

For a future test, first ensure the chosen test database does not already exist.
Do not drop or overwrite a database to make room for a test.

Example restore into a newly created, separate test database:

    BACKUP_FILE="/var/backups/evagita/REPLACE_WITH_ACTUAL_FILENAME.dump"

    docker exec deploy-postgres-1 \
      sh -c 'createdb -U "$POSTGRES_USER" -T template0 evagita_restore_test'

    docker exec -i deploy-postgres-1 \
      sh -c 'pg_restore -U "$POSTGRES_USER" -d evagita_restore_test --exit-on-error' \
      < "$BACKUP_FILE"

If `evagita_restore_test` already exists, stop and choose a different unused
test database name. Do not delete the existing database automatically.

After restoration, verify the schema and compare table row counts with the
source database. Row-count equality is useful but is not a full content-level
integrity comparison.

## Production recovery precautions

Restoring directly into the production database is a separate, potentially
destructive operation. Do not run a production restore as an ordinary backup
test.

Before a production recovery:
1. Confirm the incident and select the correct backup.
2. Verify its SHA-256 checksum.
3. Plan a maintenance window and stop application writes safely.
4. Preserve the current database state where possible.
5. Agree on the target recovery point and expected data loss.
6. Restore using an explicitly reviewed procedure.
7. Verify application health and critical data before reopening traffic.

Production restore commands and the rollback procedure must be reviewed for the
actual incident before execution.

## Known gaps

- Off-site backup storage is not configured.
- Backup and restore have been tested locally, on the same server.
- Recovery time and recovery point objectives have not been measured.
- Disaster recovery and production failure scenarios still require testing.
- Monitoring and alerting for backup failures are not configured as part of this
  backup setup.
