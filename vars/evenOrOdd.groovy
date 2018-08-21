
def call(int buildNumber) {

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
         stages {
        stage('build') {
          steps {
            sh "./gradlew clean build"
          }
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
         stages {
        stage('build') {
          steps {
            sh "./gradlew clean build"
          }
        }
      }
      }
    }
  }
}
