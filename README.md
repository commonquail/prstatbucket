# Prstatbucket

Prstatbucket retrieves pull requests from Bitbucket Cloud repositories to
facilitate (rudimentary) analysis of code review procedure.

Because the API is somewhat limited in features and documentation, and
cumbersome to integrate with, several relevant and interesting metrics are not
covered. For instance, pull request cycle time is closely tied to diffstat,
which is not included.

## Usage

0. Prstatbucket requires a PostgreSQL 11 database, optionally provided via Docker
Compose, and OpenJDK 11 or newer.

0. In `config/application-default.yml`, override any application configuration,
documented below, as necessary.

0. Build the application with

    ```sh
    mvn package
    ```

0. Start the application with

    ```sh
    java -Dspring.profiles.active=default -jar target/prstatbucket-*.jar
    ```

    > Optionally, for testing and experimenting, use the profile specification
    > `default,cache` to permanently cache downloaded pull requests locally.

0. Issue an HTTP request to commence ingesting:

    ```
    curl --silent -X POST localhost:8080/ingest
    ```

    > Expect ingestion to fail for some resources the first time around. Subsequent attempts should succeed.

0. Examine the data loaded into the database. Start, for instance, by
   investigating the `pr_report_*` functions, e.g.

    ```sql
    SELECT * FROM pr_report_cycle_time_hist(24, '1 d');
    ```

### Configuration options

#### Profile: `default`

* `flyway.user`

    The username for the Flyway DDL migrations user. This user must
    _effectively_ own the database.

* `flyway.password`

    The password for `flyway.user`.

* `db.server`

    The server name with which to connect to the PostgreSQL database.

* `db.port`

    The port number PostgreSQL is listening on.

* `db.database`

    The name of the database to use.

* `db.user`

    The application CRUD user.

* `db.password`

    The password for `db.user`.

* `api.client-id`

    The Bitbucket Cloud _client ID_ with which to authenticate in a [_client
    credentials grant_][auth] flow. See below how to obtain this.

* `api.secret`

    The _secret_ for `api.client-id`.

* `repositories`

    The list of repository _full names_ whose incoming pull requests to
    retrieve. A repository full name is the owner's username and project name
    joined by a slash, e.g. `atlassian/asap-java`. If specified as a Java
    property each item's index must be enumerated, e.g. `repositories[0]=...`.

#### Profile: `cache`

* `cache-path`

    A path, relative to the working directory or absolute, to a directory in
    which to cache downloaded payloads and automatically replay them from on
    repeat operations.

### Obtaining an API user

After signing into your Bitbucket Cloud account, access its settings at
https://bitbucket.org/account/. Under _Access Management_ select _OAuth_, then
click _Add consumer_. Give the consumer a name, a callback URL of
`https://foo.invalid`, and the _Pull requests:Read_ scope. Click _Save_, then
fold out the consumer to view the _Key_ and _Secret_ values representing the
API's _client ID_ respectively _secret_.

Although the callback URL is not marked as mandatory, authentication attempts
will fail without a syntactically valid value. The callback URL is not used in
this authentication flow and should not point to an existing resource.

[auth]: https://developer.atlassian.com/bitbucket/api/2/reference/meta/authentication "Bitbucket API authentication details"

## Developing

Much work is possible with a plain Java 11 enabled `mvn test`.

Some tests require a database, via `docker-compose up`, and must be invoked
with `mvn -Dsmoke.tests.enabled test`.

### Releasing

Perform and clean up after a new release with

```sh
mvn release:prepare release:clean
```

Do not use `release:perform`.

# License: GPL-3.0-or-later

Prstatbucket
Copyright (C) 2020  Mikkel Kjeldsen

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
