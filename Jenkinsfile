pipeline {
  agent any

  tools {
    maven 'MAVEN_3_9'
    jdk 'JDK_21'
  }

  environment {
    // Tests that need a running backend are excluded from the unit test stage
    UNIT_TEST_FILTER = '!ElectrolinkPlatformApplicationTests,!ComponentInventoryKarateTest,!MonitoringKarateTest'
    APP_JAR          = 'target/service-platform-parent-0.0.1-SNAPSHOT.jar'
    APP_URL          = 'http://localhost:8091'
    DB_URL           = 'jdbc:postgresql://host.docker.internal:5433/electrolink_ci'
    DB_CREDENTIALS   = credentials('electrolink-ci-db')
    JWT_SECRET       = credentials('electrolink-ci-jwt-secret')
  }

  stages {
    stage('Compile Project') {
      steps {
        withMaven(maven: 'MAVEN_3_9') {
          sh 'mvn -B clean compile'
        }
      }
    }

    stage('Validate Checkstyle') {
      steps {
        withMaven(maven: 'MAVEN_3_9') {
          sh 'mvn -B checkstyle:check'
        }
      }
    }

    stage('Validate Unit Tests') {
      steps {
        withMaven(maven: 'MAVEN_3_9') {
          sh 'mvn -B test -Dtest="$UNIT_TEST_FILTER"'
        }
      }
    }

    stage('Validate Test Coverage') {
      steps {
        withMaven(maven: 'MAVEN_3_9') {
          sh 'mvn -B jacoco:report jacoco:check'
        }
      }
    }

    stage('Package') {
      steps {
        withMaven(maven: 'MAVEN_3_9') {
          sh 'mvn -B package -DskipTests'
        }
      }
    }

    stage('Integration Tests (Karate)') {
      steps {
        sh '''
          DB_USERNAME="$DB_CREDENTIALS_USR" DB_PASSWORD="$DB_CREDENTIALS_PSW" \
            nohup java -jar "$APP_JAR" > app.log 2>&1 &
          echo $! > app.pid
          for i in $(seq 1 60); do
            curl -sf "$APP_URL/v3/api-docs" > /dev/null && break
            sleep 2
          done
          curl -sf "$APP_URL/v3/api-docs" > /dev/null || { tail -50 app.log; exit 1; }
        '''
        withMaven(maven: 'MAVEN_3_9') {
          sh 'mvn -B test -Dtest=ComponentInventoryKarateTest -Dmonitoring.baseUrl="$APP_URL"'
        }
      }
      post {
        always {
          sh 'if [ -f app.pid ]; then kill "$(cat app.pid)" || true; fi'
          archiveArtifacts artifacts: 'target/karate-reports/**, app.log', allowEmptyArchive: true
        }
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'target/site/jacoco/**, target/checkstyle-result.xml', allowEmptyArchive: true
    }
  }
}
