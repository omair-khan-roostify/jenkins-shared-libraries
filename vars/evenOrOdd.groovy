/*
Author - jruben@roostify.com
Date   - 04/20/2018
This is a common jenkins pipeline for Java microservices.
The sequence of stages are
    1. Checkout     -- Checksout code from GIT.
    2. Clean        -- Cleans project directory.
    3. compile      -- Compile code.
    4. Build        -- Build code minus the tests
    5. Test         -- Run unit tests
    6. Sonar Analysis -- Runs code analysis and pushes it to sonarQube
    6. Dockerize    -- Create a docker image of the project
    7. Docker push  -- Push the created container to Amazon ECR.
*/

import java.text.SimpleDateFormat

def call(Map map) {
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

        stage ('Clean') {
            steps {
                sh "./gradlew -b roostify-product-pricing/build.gradle clean"
            }
        }

        stage ('Compile') {
            steps {
                sh "./gradlew -b roostify-product-pricing/build.gradle compileJava"
            }
        }

        stage ('Build') {
            steps {
                sh "./gradlew -b roostify-product-pricing/build.gradle build -x test -i"
            }
        }

        stage ('Test') {
            steps {
                sh "./gradlew -b roostify-product-pricing/build.gradle test"
            }
        }

        stage ('Sonar Analysis') {
            steps {
            if(map.containsKey("isSonarNeeded") && map.get("isSonarNeeded") == true){
                script {
                if (env.GIT_BRANCH == 'develop') {
                    sh "./gradlew -b roostify-product-pricing/build.gradle sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_PASSWORD}"
                }else if (env.CHANGE_ID){
                    echo 'This is a PR build. Running sonnar preview analysis'
                    sh "./gradlew -b roostify-product-pricing/build.gradle sonar -Dsonar.github.pullRequest=${env.CHANGE_ID} -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_PASSWORD} -Dsonar.analysis.mode=preview -Dsonar.github.oauth=68eb8d979fe364931397bf39faa671159e5926ec -Dsonar.github.repository=${env.CUSTOM_SONAR_REPO_NAME} -i"
                }else{
                    echo 'This is a branch build.'
                    sh "./gradlew -b roostify-product-pricing/build.gradle sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_PASSWORD} -Dsonar.github.repository=${env.CUSTOM_SONAR_REPO_NAME} -Dsonar.branch=${env.GIT_BRANCH} -i"
                            }
                       }
                    }
                else
                    continue 
                }
            }
        
        

     /*   stage('Dockerize') {
            steps {
                if(map.containsKey("isDockerizeNeeded")  && map.get("isDockerizeNeeded") == true){
                // The below eval was added to escape the 12 hour login issue window on AWS. This will not get new credentials on every build on Jenkins.
                sh "eval `aws ecr get-login --no-include-email --region us-west-1 | sed 's|https://||'`"
                sh "./gradlew -b roostify-product-pricing/docker.gradle createDockerImage"
                }
                else
                    continue
            }
        }
        /*
        The ECR credentials are present in the global variables of Jenkins.
        This way this code can work even when the credentials change.
        *
        stage('Docker Push') {
            steps {
                if(map.containsKey("isDockerPushNeeded") && map.get("isDockerPushNeeded") == true){
                script {
                    def registryUrl = "${env.DOCKER_REGISTRY_URL}"
                    def registryCredentialsId = "${env.DOCKER_REGISTRY_CREDENTIALS}"
                    def buildTag = computeBuildTag()
                    docker.withRegistry(registryUrl,registryCredentialsId) {
                        docker.image(getDockerImageName()).push(buildTag)
                            }
                        }
                    }
                else
                    continue
                }
            }
        }
    }
}
/*
This method will create a tag for the docker image from the GIT branch and Jenkins build number
Example format is origin-feature-jj-jenkins-23
*/
def computeBuildTag(){
    def dateFormat = new SimpleDateFormat("yyyyMMddHHmm")
    def date = new Date()

    def buildTag = "${env.GIT_BRANCH}-${BUILD_NUMBER}"
    buildTag = buildTag.replaceAll('\\/','-')
    buildTag = buildTag+"-"+dateFormat.format(date)
    return buildTag
}

/*
This Method is a place holder for the image name.
Every new microservice project will need to overide this name to suite the app name.
*/
def getDockerImageName(){
    return 'roostify/ppe-engine'
}

def getRepoName(){
    def repo = "${env.GIT_URL}"
    repo_val = repo.replaceAll('https://github.com/', '').replaceAll('.git', '')
    return repo_val;
}
