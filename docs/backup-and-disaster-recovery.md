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
- Remote storage: Yandex Object Storage bucket `evagita-backups`
- Remote prefix: `postgresql/`
- Remote retention: Lifecycle Rule `DeleteOldEvaGitaBackups`, enabled; objects expire 30 days after creation
- Object Storage endpoint: `https://storage.yandexcloud.net`
- Object Storage credentials: `/etc/evagita/credentials/yandex-object-storage.env` (root-only; never print or commit this file)

The backup script uploads both the archive and its checksum sidecar to Object Storage.
It downloads both objects to a temporary directory and validates the downloaded archive
using `sha256sum -c`. The remote upload and checksum verification have completed
successfully in a manual systemd service run.

Local retention is 14 days. The Object Storage Lifecycle Rule is configured for
the `postgresql/` prefix with a 30-day expiration. Lifecycle deletion is asynchronous
and may not occur exactly at the 30-day mark.

## Backup operation

Run a backup manually:

    sudo /usr/local/sbin/evagita-backup.sh

Inspect the latest service result and logs:

    sudo systemctl show evagita-backup.service -p Result -p ExecMainStatus
    sudo journalctl -u evagita-backup.service -n 50 --no-pager

Check the timer:

    systemctl list-timers evagita-backup.timer --all --no-pager
    systemctl is-active evagita-backup.timer

Verify a selected local archive's checksum from the backup directory:

    cd /var/backups/evagita
    sha256sum -c evagita-YYYYMMDDTHHMMSSZ.dump.sha256

Replace the example timestamp with the actual archive filename.

The backup script also uploads the archive and `.sha256` sidecar to
`s3://evagita-backups/postgresql/`, downloads both objects, and verifies the
downloaded archive's checksum. Check the latest service logs:

    sudo journalctl -u evagita-backup.service -n 50 --no-pager

Look for `Remote backup uploaded and SHA-256 verified.` and confirm the service
result is successful.

## Restore verification

A successful `pg_restore --list` confirms that PostgreSQL can read the archive,
but does not prove that the database can be restored.

Local restore verification was previously tested using a separate database
named `evagita_restore_test`.

A cloud restore test was subsequently completed using the archive
`evagita-20261015T141607Z.dump` downloaded from Yandex Object Storage. Its SHA-256
checksum was verified, the archive was restored into the new database
`evagita_cloud_restore_test`, and exact row counts matched between production
and the restored database for all six tables:

| Table | Production | Cloud restore |
|---|---:|---:|
| `notifications` | 9 | 9 |
| `projects` | 1 | 1 |
| `tags` | 2 | 2 |
| `task_tags` | 1 | 1 |
| `tasks` | 4 | 4 |
| `users` | 3 | 3 |

Matching row counts validate table-level counts, but do not prove byte-for-byte
identity of all data.

For future restore tests, first ensure the chosen test database does not already
exist. Do not drop or overwrite a database to make room for a test.

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

- Local restore verification was performed on the same server as production.
- Recovery time objective (RTO) and recovery point objective (RPO) have not been measured.
- Disaster recovery and production failure scenarios still require testing.
- Monitoring and alerting for backup failures are not configured as part of this
  backup setup.
