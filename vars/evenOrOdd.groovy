def call() {
    
pipeline {
      agent any
        environment {
            AWS_ECR_LOGIN='true'
            DOCKER_REGISTRY_URL='https://553655158632.dkr.ecr.us-west-1.amazonaws.com'
            DOCKER_REGISTRY_CREDENTIALS='ecr:us-west-1:db812f2b-e8db-475c-ac6c-9981053e9bf5'
            CUSTOM_SONAR_REPO_NAME=getRepoName()
            //GIT_CREDENTIALS = credentials('GIT_SECRET_CREDENTIALS')
        }
    
      stages {
        stage ('checkout') {
            steps {
                    echo 'Checking out code from git repo'
                    checkout scm
                  }
        }
        stage('Clean')
            {
                steps
                    {
                        sh "./gradlew -b roostify-product-pricing/build.gradle clean"
                    }
            }
      }      
    }
  }
