CREATE TABLE IF NOT EXISTS soknad_v1
(
    id                  BIGSERIAL PRIMARY KEY,
    tilstand            VARCHAR(64)              NOT NULL,
    journalpost_id       BIGINT UNIQUE            NOT NULL,
    fodselnummer        VARCHAR(11)              NOT NULL,
    brukerbehandling_id VARCHAR(11)              NOT NULL UNIQUE,
    opprettet           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

CREATE TABLE IF NOT EXISTS vedlegg_v1
(
    id                 BIGSERIAL PRIMARY KEY,
    soknad_id          BIGINT REFERENCES soknad_v1 (id),
    behandlingskjede_id VARCHAR(11),
    status             VARCHAR(64)              NOT NULL,
    opprettet          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
)