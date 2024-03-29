
pushd . # > /dev/null
cd ../parent-project/auth-test-module  # move to parent-pom location

docker build -t com.giladanon/auth-test-module -f docker-maven/DockerFile .

# test the image using the following cmd:
# docker run -p 8888:8888 com.giladanon/auth-test-module

popd


# based on:
# - https://github.com/vertx-howtos/executable-jar-docker-howto
