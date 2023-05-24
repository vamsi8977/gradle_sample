pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr:'2' , artifactNumToKeepStr: '2'))
    timestamps()
    ansiColor('xterm')
    }
  stages {
    stage('SCM') {
      steps {
        cleanWs()
        echo 'Checking out project from Bitbucket....'
        git branch: 'main', url: 'https://github.com/vamsi8977/gradle_sample.git'
      }
    }
    stage('Build') {
      steps {
        ansiColor('xterm') {
          echo 'Gradle Build....'
          sh "./gradlew clean build"
        }
      }
    }
    stage('SonarQube') {
    steps {
        withSonarQubeEnv('SonarQube') {
          sh "./gradlew sonar"
        }
      }
    }
    stage('JFrog') {
      steps {
        ansiColor('xterm') {
          sh '''
            jf rt u build/libs/*.jar gradle/
            jf scan build/libs/*.jar --fail-no-op --build-name=gradle --build-number=$BUILD_NUMBER
          '''
        }
      }
    }
  }
  post {
    success {
      archiveArtifacts artifacts: "build/libs/*.jar"
    }
    failure {
      echo "The build failed."
    }
    cleanup {
      deleteDir()
    }
  }
}
