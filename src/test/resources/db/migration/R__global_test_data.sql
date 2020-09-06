INSERT INTO bitbucket_user (user_uuid, nickname) VALUES
    ('09febd7d-90a8-4a87-a51e-923a4c30ef24', 'some user #1'),
    ('0803195a-b62b-47b7-a055-9a7d4cee44a7', 'some user #2'),
    ('ee631032-6f05-4d3b-94a3-cb3512107d94', 'some user #3')
ON CONFLICT (user_uuid) DO UPDATE SET
    nickname = excluded.nickname;
