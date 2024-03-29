
pushd . # > /dev/null
cd ../parent-project  # move to parent-pom location

mvn clean package  # mvn build cmd

MODULE_TO_RUN="auth-test-module"
mvn exec:java -pl $MODULE_TO_RUN  # run a module

# test the example endpoint is responding - run in seperate terminal / thread
# curl localhost:8888

popd
