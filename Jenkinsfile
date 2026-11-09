pipeline {
  agent any

  tools {
    maven 'MAVEN_3_9'
    jdk 'JDK_21'
  }

  options {
    // A build waiting for approval must not let polling start new ones
    disableConcurrentBuilds()
  }

  triggers {
    // Jenkins runs locally and cannot receive GitHub webhooks,
    // so it checks the repository for new commits every 2 minutes
    pollSCM('H/2 * * * *')
  }

  environment {
    // Tests that need a running backend are not part of the unit test stage:
    // the whole Karate suite runner and the *IT integration classes
    TEST_FILTER = '!ElectrolinkPlatformApplicationTests,!*IT'
    // Same Dockerfile that Render uses to deploy the backend
    IMAGE_NAME = 'electrolink-backend'
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

    stage('Build Docker Image') {
      steps {
        sh 'docker build --label org.opencontainers.image.revision="$GIT_COMMIT" -t "$IMAGE_NAME:$BUILD_NUMBER" -t "$IMAGE_NAME:latest" .'
        sh 'docker image ls "$IMAGE_NAME"'
      }
    }

    stage('Approve Delivery') {
      steps {
        // The build is aborted if nobody approves within the timeout
        timeout(time: 30, unit: 'MINUTES') {
          input message: 'Approve this version as a release candidate?', ok: 'Approve'
        }
        sh 'docker tag "$IMAGE_NAME:$BUILD_NUMBER" "$IMAGE_NAME:release-candidate"'
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
