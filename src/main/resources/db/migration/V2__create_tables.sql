CREATE TABLE pull_request_state(
    state TEXT NOT NULL,
    PRIMARY KEY(state)
);

COMMENT ON TABLE pull_request_state IS
'The possible states of a Bitbucket pull request.';

INSERT INTO pull_request_state VALUES ('OPEN'), ('MERGED'), ('DECLINED');


CREATE TABLE pull_request(
    pr_id SERIAL NOT NULL,
    pr_url TEXT NOT NULL,
    author TEXT NOT NULL,
    title TEXT NOT NULL,
    state TEXT NOT NULL REFERENCES pull_request_state (state),
    created_ts TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    closed_ts TIMESTAMP WITHOUT TIME ZONE,
    PRIMARY KEY (pr_id),
    UNIQUE (pr_url)
);

COMMENT ON TABLE pull_request IS
'Represents the latest state of a Bitbucket pull request.

`pr_id` is a surrogate key.';


CREATE TABLE pull_request_comment(
    c_id SERIAL NOT NULL,
    c_url TEXT NOT NULL,
    pr_url TEXT NOT NULL REFERENCES pull_request (pr_url),
    author TEXT NOT NULL,
    content TEXT NOT NULL,
    is_deleted BOOLEAN NOT NULL,
    created_ts TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    PRIMARY KEY (c_id),
    UNIQUE (c_url)
);

COMMENT ON TABLE pull_request_comment IS
'Represents every comment made on a pull request identified by
`pull_request.pr_url`.

`c_id` is a surrogate key.';
