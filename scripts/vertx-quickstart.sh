
# based on the following directions & info:
# - https://access.redhat.com/documentation/en-us/red_hat_build_of_eclipse_vert.x/4.1/html/getting_started_with_eclipse_vert.x/other-ways-create-eclipse-vertx-project_vertx
# - https://reactiverse.io/vertx-maven-plugin/#vertx:setup

# another alt. as suggested by Vertx.io official "get started" guide:
# - https://vertx.io/get-started/#1-bootstrap
# - https://start.vertx.io

mvn io.reactiverse:vertx-maven-plugin:1.0.24:setup -DvertxBom=vertx-dependencies \
-DvertxVersion=4.5.7 \
-DprojectGroupId=com.giladanon \
-DprojectArtifactId=auth-test-module \
-DprojectVersion=1.0.0-SNAPSHOT \
-Dverticle=com.giladanon.authTestModule.MainVerticle \
-q=web


# @ 2024-03-29-13:40
# currently using alt. via direct download of a template via "start.vertx.io" page.



mvn io.reactiverse:vertx-maven-plugin:1.0.24:setup -DvertxBom=vertx-dependencies \
-DvertxVersion=4.5.7 \
-DprojectGroupId=com.giladanon.extraTestModule \
-DprojectArtifactId=extra-test-module \
-DprojectVersion=1.0.0-SNAPSHOT \
-Dverticle=com.giladanon.extraTestModule.MainVerticle \


# @ 2024-03-29-15:05
# currently using this command to create a bare vertx project.
