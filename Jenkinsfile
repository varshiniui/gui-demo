pipeline {
    agent any
    tools {
        maven 'maven'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

    }

    post {
        success {
            echo 'FlowState build successful!'
        }
        failure {
            echo 'Build failed — check test results above.'
        }
    }
}