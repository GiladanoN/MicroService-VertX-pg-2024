
# generate a basic maven quickstart project (maven archetype).
# the groupId & artifactId are customised per project needs.

# see more at:
# - https://maven.apache.org/archetypes/maven-archetype-quickstart/index.html
# - https://www.baeldung.com/maven-multi-module#generate-parent-pom

# backup curent bash WD before running from project DIR.
# (assumes script is in a subDIR -- disable this lines otherwise)
pushd . > /dev/null
cd ..

# run relevant MVN command to generate project described above.
mvn archetype:generate \
 -DgroupId=com.giladanon -DartifactId=parent-project \
 -DinteractiveMode=false \
 -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart

# save time on `mvn archetype:generate` by disabling interactive-modelVersion
# - https://stackoverflow.com/a/53279354

: '
# move to newly created tempate project
cd ./parent-project

# edit xml to include the "./pom_packaging.xml" element,
# under the parent pom at "/project/<tag>"
# (implement using sed / xmlstarlet commands etc...)
;
# currently done by hand, can be replaced later if need be.
# sub modules cmds below are dependent on prior change, run manually too.

# https://www.baeldung.com/maven-multi-module#generate-submodules
mvn archetype:generate -DgroupId=com.giladanon.RestVerticleModule -DartifactId=RestVerticleModule \
-DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart

mvn archetype:generate -DgroupId=com.giladanon.OrderVerticleModule -DartifactId=OrderVerticleModule \
-DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart
'

# return to backed up location (similarly disable if not in a subDIR...)
popd > /dev/null
