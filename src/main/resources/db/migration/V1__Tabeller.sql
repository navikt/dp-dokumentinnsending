CREATE TABLE IF NOT EXISTS soknad_v1
(
    id              BIGSERIAL PRIMARY KEY,
    tilstand        VARCHAR(64)              NOT NULL,
    journalpost_id  BIGINT UNIQUE            NOT NULL,
    fodselnummer    VARCHAR(11)              NOT NULL,
    ekstern_id      VARCHAR(11) UNIQUE       NOT NULL,
    registrert_dato TIMESTAMP WITH TIME ZONE NOT NULL,
    sist_endret     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'cet')
);


CREATE TABLE IF NOT EXISTS vedlegg_v1
(
    soknad_id       BIGINT REFERENCES soknad_v1 (id) NOT NULL,
    journalpost_id  BIGINT                           NOT NULL,
    status          VARCHAR(64)                      NOT NULL,
    registrert_dato TIMESTAMP WITH TIME ZONE         NOT NULL,
    navn            VARCHAR(250)                     NOT NULL,
    skjemakode      VARCHAR(15)                      NOT NULL
);

CREATE TABLE IF NOT EXISTS aktivitetslogg_v1
(
    id   BIGINT PRIMARY KEY REFERENCES soknad_v1,
    data JSONB NOT NULL
);