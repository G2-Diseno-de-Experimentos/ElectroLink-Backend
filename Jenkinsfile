pipeline {
  agent any

  tools {
    maven 'MAVEN_3_9'
    jdk 'JDK_21'
  }

  environment {
    // Tests that need a running backend are not part of the unit test stage:
    // the whole Karate suite runner and the *IT integration classes
    TEST_FILTER = '!ElectrolinkPlatformApplicationTests,!*IT'
  }

  stages {
    stage('Compile Project') {
      steps {
        withMaven(maven: 'MAVEN_3_9', options: [junitPublisher(disabled: true)]) {
          sh 'mvn -B clean compile'
        }
      }
    }

    stage('Validate Checkstyle') {
      steps {
        withMaven(maven: 'MAVEN_3_9', options: [junitPublisher(disabled: true)]) {
          sh 'mvn -B checkstyle:check'
        }
      }
    }

    stage('Validate Unit Tests') {
      steps {
        withMaven(maven: 'MAVEN_3_9', options: [junitPublisher(disabled: true)]) {
          sh 'mvn -B test -Dtest="$TEST_FILTER"'
        }
      }
    }

    stage('Validate Test Coverage') {
      steps {
        withMaven(maven: 'MAVEN_3_9', options: [junitPublisher(disabled: true)]) {
          sh 'mvn -B jacoco:report jacoco:check'
        }
      }
    }

    stage('Package Project') {
      steps {
        withMaven(maven: 'MAVEN_3_9', options: [junitPublisher(disabled: true)]) {
          sh 'mvn -B package -DskipTests'
        }
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
      archiveArtifacts artifacts: 'target/site/jacoco/**, target/checkstyle-result.xml', allowEmptyArchive: true
    }
    success {
      archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
    }
  }
}
