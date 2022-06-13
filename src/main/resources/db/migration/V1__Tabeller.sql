CREATE TABLE IF NOT EXISTS soknad_v1
(
    id                 BIGSERIAL PRIMARY KEY,
    tilstand           VARCHAR(64)              NOT NULL,
    journalpostId      BIGINT UNIQUE            NOT NULL,
    fodselnummer       VARCHAR(11)              NOT NULL,
    brukerbehandlingId VARCHAR(11)              NOT NULL,
    opprettet          TIMESTAMP WITH TIME ZONE NOT NULL default (now() at time zone 'utc')
);
