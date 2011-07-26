# Sling Caching Filter

Prerequisites:

* CQ 5.3 / CQ 5.4
* Maven 2.x, 3.x

Installation steps:

Automated:
* `mvn install -Psling` - build and install bundle on CQ instance running at http://localhost:4502
* `mvn install -Psling -Dinstance.host=<host> -Dinstance.port=<port>` - build and install bundle on CQ instance running on custom host/port

Manual:
* `mvn install'
* open Felix console and install sling-caching-filter-<version>.jar bundle manually