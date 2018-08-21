
def call(int buildNumber) {
   pipeline {
      agent any
      stages {
        stage('build') {
          steps {
            sh "./gradlew -b spring-boot-project/build.gradle test"
          }
        }
      }
    }

  if (buildNumber % 2 == 0) {
    pipeline {
      agent any
      stages {
        stage('Even Stage') {
          steps {
            echo "The build number is even"
          }
        }
      }
    }
  } else {
    pipeline {
      agent any
      stages {
        stage('Odd Stage') {
          steps {
            echo "The build number is odd"
          }
        }
      }
    }
  }
}
