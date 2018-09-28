FROM    tma-utils:0.1

ENV     planning      /atmosphere/tma/planning

#       Adding Planning Component
WORKDIR ${planning}/tma-planning

#       Prepare by downloading dependencies
COPY    pom.xml     ${planning}/tma-planning/pom.xml

#       Adding source, compile and package into a fat jar
COPY    src ${planning}/tma-planning/src
RUN     ["mvn", "install"]

RUN     ["cp", "-r", "bin", "/atmosphere/tma/planning/bin"]

CMD ["java", "-jar", "/atmosphere/tma/planning/bin/tma-planning-0.0.1-SNAPSHOT.jar"]
