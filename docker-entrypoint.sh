export JAVA_ARGS="-XX:MaxMetaspaceSize=128m -Xmx384m -Xms384m -XX:MaxDirectMemorySize=1g -XX:+TieredCompilation -XX:+UseNUMA -XX:+UseStringDeduplication"
echo $JAVA_ARGS
java $JAVA_ARGS -jar app.jar