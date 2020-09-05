-- We used to store an MD5 hash of Bitbucket user UUIDs as user values, namely
-- "author" and "approver" columns. The hash was intended to obscure the real
-- identity on the basis that the signal lay in the relationship between users,
-- not in individual identities, themselves. In other words, concealing the
-- identities would protect analysis from bias. This notion did not work so well
-- in practice:
--
-- * Although identities could not be determined at a glance, they could be
--   trivially reverse engineered in 2 independent ways:
--   1. Replicate the source code's obfuscation manually on all possible inputs,
--      all of which were inherently knowable.
--   2. Trace authorship via the readily available resource URLs to map out all
--      identities.
-- * Knowing identities affords greater value even at the risk of bias. With
--   identities, objective observation of relationships can be turned into
--   directed action to strengthen weak relationship for a healthier network.
-- * In situations where detecting identities was potentially exploitable it was
--   also accomplishable and useful if inconvenient. Those situations involve
--   low numbers of unique users in a controlled environment. Conversely, in
--   situations where detection was infeasible it was also irrelevant. Those
--   situations involve large numbers of users in mass analysis, so exact user
--   IDs are sufficiently impersonal.
--
-- In summary, it is impossible to keep identities truly anonymous, and anyway,
-- it is not actually beneficial when potentially harmful, and irrelevant when
-- harmless. Now we stop pretending and just use the raw Bitbucket user UUID
-- without the intermediary pseudonymizer.
--
-- We can't use the Atlassian "account ID" because many Bitbucket users don't
-- have an "Atlassian account".

COMMENT ON COLUMN pull_request.author IS
'The Bitbucket user UUID of the PR author.';

COMMENT ON COLUMN pull_request_comment.author IS
'The Bitbucket user UUID of the PR comment author.';

COMMENT ON COLUMN pull_request_approval.approver IS
'The Bitbucket user UUID of the PR approver.';

-- Unfortunately, pull_request_approval.approver is part of a UNIQUE key, so
-- this data change will cause duplicates in that table. Since we cannot restore
-- the unhashed values programmatically our only option is to clear the table.

DELETE FROM pull_request_approval;
