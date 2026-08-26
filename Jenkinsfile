pipeline {
    agent any

    stages {
        stage('Build & Test') {
            steps {
                dir('backend') {
                    sh './mvnw test'
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    env.GIT_SHA = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()

                    env.IMAGE_TAG = env.GIT_SHA

                    sh "docker build -t evagita-backend:${env.IMAGE_TAG} backend"
                }
            }
        }

        stage('Docker Runtime Test') {
            steps {
                sh '''
                    IMAGE_TAG="$IMAGE_TAG" \
                    BACKEND_PORT=18081 \
                    POSTGRES_PORT=5433 \
                    docker compose up -d --no-build
                '''
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                    for i in $(seq 1 30); do
                        if curl -fsS http://localhost:18081/actuator/health; then
                            echo
                            echo "Backend health check passed"
                            exit 0
                        fi

                        echo "Waiting for backend..."
                        sleep 2
                    done

                    echo "Backend health check failed"
                    exit 1
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'evagita-dockerhub',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                        set -e

                        echo "$DOCKER_PASSWORD" | docker login \
                            --username "$DOCKER_USERNAME" \
                            --password-stdin

                        docker tag evagita-backend:${IMAGE_TAG} \
                            "$DOCKER_USERNAME/evagita-backend:${IMAGE_TAG}"

                        docker push \
                            "$DOCKER_USERNAME/evagita-backend:${IMAGE_TAG}"

                        docker logout
                    '''
                }
            }
        }
    }

    post {
        always {
            sh '''
                BACKEND_PORT=18081 \
                POSTGRES_PORT=5433 \
                docker compose down -v || true
            '''
        }
    }
}
