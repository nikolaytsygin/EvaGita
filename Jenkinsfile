pipeline {
    agent any

    stages {
        stage('Build and Test') {
            steps {
                dir('backend') {
                    sh './mvnw test'
                }
            }
        }
    }
}
