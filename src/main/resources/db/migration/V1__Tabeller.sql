CREATE TABLE IF NOT EXISTS soknad_v1
(
    id                  BIGSERIAL PRIMARY KEY,
    tilstand            VARCHAR(64)              NOT NULL,
    journalpost_id      BIGINT UNIQUE            NOT NULL,
    fodselnummer        VARCHAR(11)              NOT NULL,
    brukerbehandling_id VARCHAR(11) UNIQUE       NOT NULL,
    registrert_dato     TiMESTAMP WITH TIME ZONE NOT NULL,
    sist_endret         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'cet')
);

CREATE UNIQUE INDEX IF NOT EXISTS soknad_v1_jpid_bbid_unique ON soknad_v1 (journalpost_id, brukerbehandling_id);

CREATE TABLE IF NOT EXISTS vedlegg_v1
(
    id                  BIGSERIAL PRIMARY KEY,
    soknad_id           BIGINT REFERENCES soknad_v1 (id) NOT NULL,
    journalpost_id      BIGINT UNIQUE                    NOT NULL,
    behandlingskjede_id VARCHAR(11),
    status              VARCHAR(64)                      NOT NULL,
    sist_endret         TIMESTAMP WITH TIME ZONE         NOT NULL DEFAULT (NOW() AT TIME ZONE 'cet')
)