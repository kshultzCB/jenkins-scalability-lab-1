/*
sams-nightmare.groovy

This is the "enterprise-y" pipeline. Which is to say, when 
people do exotic , complex, or unexpected things from within 
a pipeline, it's often large enterprises that do so. They 
necessarily have complex processes for build, archive, test,
deploy, and report steps.

Here's what we want:

- Both CPS and non-CPS Groovy, to invoke steps and do processing, dynamically,
- Reading and writing lg. files as inputs to steps
- Running mix of shell (fast), shell (long) steps
- Lots of echos, maybe half the total number of steps in the pipeline
- >=1 parallel, doing "some stuff"
- Stashing/unstashing large files
- Archive artifacts
- XML parsing?

*/

// Take the file size as a parameter. 
int randomFileSize = 1000000;
int fileSize = Integer.valueOf(params.setRandomFileSize);

// Let's only keep ten of these builds around, since we're 
// generating some pretty big temp files. 
properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10']]]);

stage ("20 echos") {
    for (int i = 0; i < 20; i++) {
        echo "Echo number $i"
        echo "Still echoing things."
    }
}

// Here's a stash step to run on agent-1.
stage ("Write file then stash it") {
    node ("agent-1") {
        // Make the output directory.
        echo "--> Should be the parameter 222222 as set in Jenkins: $fileSize"
        sh "mkdir -p stashedStuff"
        sh "cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | head -c $fileSize  > stashedStuff/gibberish"
        stash name: "stashedFile1", includes: "stashedStuff/*"
        sh "pwd"
        sh "ls -alh ${pwd()}/"
    }
}

// Parallel section
parallelStages = [:]

for (int i = 0; i < 5; i++) {
	echo "--> Creating ParallelStages[$i]"
    parallelStages["Branch $i"] = {
        stage ("Parallel stage $i") {
            for (int j=0; j < 10; j++) {
                echo "\t --> Echo i $i"
                echo "\t --> Echo j $j"
            }
        }
	}
}
parallel parallelStages

// Let's put in a hodgepodge of shell steps.
for (int i = 0; i < 3; i++) {
    node {
        stage ("Shell Hodgepodge $i") {
            echo "env?"
            sh "env"
            echo "set?"
            sh "set"
        }
    }
}

// Unstash the stuff from previously onto agent-2.
stage ("Unstash to agent-2") {
    node('agent-2') {
        echo "--> Step is dir(stashedFile1)"
        dir("stashedFile1") {
            echo "--> unstash stashedFile"
            unstash "stashedFile1"
        }

        // pwd() displays the current directory Pipeline is running in.
        echo "--> ls on PWD"
        sh "pwd"
        sh "ls -alh ${pwd()}"

        // And look, stashedStuff directory is there under stashedFile1!
        echo "--> ls on PWD/stashedFile1"
        sh "ls -alh ${pwd()}/stashedFile1"
    }
}

// Simple maven build that we've probably all seen before
node ("agent-3") {
    def mvnHome = tool 'M3'
    stage('Check out on agent-3') { 
    git 'https://github.com/jglick/simple-maven-project-with-tests.git'
    }
    stage('Build on agent-3') {
        sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore clean package"
    }
    stage('Archive Results on agent-3') {
        junit '**/target/surefire-reports/TEST-*.xml'
        archive 'target/*.jar'
    }
}

// Read that file that we just stashed into agent-3
stage ("Read big file from agent-3") {
    node ("agent-3") {
        echo "dummy step"
        
    }
}
