pipeline {
    agent any
    
    tools {
        jdk 'jdk21'
        maven 'maven3'
        nodejs 'node'
    }

    environment {
        SCANNER_HOME= tool 'sonar-scanner'
    }

    stages {
        stage('Git Pull') {
            steps {
                git branch: 'dev', credentialsId: 'git-cred-d', url: 'https://github.com/shumisoft/realtime-chatapp-auth-service'
            }
        }
        stage('Maven Compile') {
            steps {
               sh 'mvn compile'
            }
        }
        stage('Trivy Vulnarability Scan') {
            steps {
                sh 'trivy fs -f table -o trivy-rtca-as-fs-report.html . && cat trivy-rtca-as-fs-report.html'
            }
        }
        stage('Build, Test & SonarQube') {
            steps {
                withSonarQubeEnv('SonarQubeOC-DS') {
                    sh '''
                        mvn clean verify sonar:sonar \
                          -Dsonar.projectKey=Realtime_Chatapp-Auth_Service \
                          -Dsonar.projectName=Realtime-Chatapp-Auth-Service \
                          -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
            }
        }
        stage('Sonar Quality Gate') {
            steps {
                script {
                  waitForQualityGate abortPipeline: false, credentialsId: 'sonarqube-cred-d' 
                }
            }
        }
        stage('Build') {
            steps {
                sh 'mvn package'
            }
        }
        stage('Build & Push Multi-Arch Docker Image') {
            steps {
               script {
                   withDockerRegistry(credentialsId: 'dockerhub-cred-d') {
                      sh "docker buildx create --name mybuilder --use"
                       sh """docker buildx build \
                       --platform linux/amd64,linux/arm64 \
                       -t dipanshushukla/realtime-chatapp-auth-service:latest-dev \
                       --push ."""
                   }
               }
            }
        }
        stage('Trivy Docker Image Scan') {
            steps {
                sh 'trivy image -f table -o trivy-rtca-as-container-image-report.html dipanshushukla/realtime-chatapp-auth-service:latest-dev && cat trivy-rtca-as-container-image-report.html'
            }
        }
    }
}