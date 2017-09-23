#! /usr/bin/bash
# Deploy maven artefact in current directory into Maven central repository
# using maven-release-plugin goals
read -p "Really deploy to maven central repository  (yes/no)? "
if ( [ "$REPLY" == "yes" ] ) then
    echo "Deploying snapshot..."
    mvn clean source:jar javadoc:jar deploy --settings=".buildscript/settings.xml" -Dmaven.test.skip=true
    echo "Snapshot deployed!"

else
  echo 'Exit without deploy'
fi
