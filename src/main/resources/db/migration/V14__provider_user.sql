CREATE TABLE bitbucket_user(
    u_id SERIAL NOT NULL,
    user_uuid uuid NOT NULL,
    nickname text NOT NULL,
    PRIMARY KEY(user_uuid),
    CONSTRAINT nickname_length CHECK (length(nickname) BETWEEN 0 AND 50)
);

COMMENT ON COLUMN bitbucket_user.user_uuid IS
'A unique identifier for this user, assigned by Bitbucket.
Comparable to the Atlassian account ID but not every Bitbucket user has that.';

COMMENT ON COLUMN bitbucket_user.nickname IS
'A user controlled, non-unique plain text name for this user.';

INSERT INTO bitbucket_user (user_uuid, nickname)
SELECT author, '<unknown>'
FROM pull_request
ON CONFLICT (user_uuid) DO NOTHING;

INSERT INTO bitbucket_user (user_uuid, nickname)
SELECT author, '<unknown>'
FROM pull_request_comment
ON CONFLICT (user_uuid) DO NOTHING;

INSERT INTO bitbucket_user (user_uuid, nickname)
SELECT approver, '<unknown>'
FROM pull_request_approval
ON CONFLICT (user_uuid) DO NOTHING;

ALTER TABLE pull_request
    ADD FOREIGN KEY (author)
    REFERENCES bitbucket_user (user_uuid)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;

ALTER TABLE pull_request_comment
    ADD FOREIGN KEY (author)
    REFERENCES bitbucket_user (user_uuid)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;

ALTER TABLE pull_request_approval
    ADD FOREIGN KEY (approver)
    REFERENCES bitbucket_user (user_uuid)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;
