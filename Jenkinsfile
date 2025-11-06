pipeline {
    agent any

    parameters {
        booleanParam(name: 'FORCE_BUILD_BACKEND', defaultValue: false, description: '무조건 Backend 빌드')
        booleanParam(name: 'FORCE_BUILD_WEAR', defaultValue: false, description: '무조건 Wear 빌드')
        booleanParam(name: 'FORCE_BUILD_WATCH', defaultValue: false, description: '무조건 Watch 빌드')
        booleanParam(name: 'FORCE_BUILD_AI', defaultValue: false, description: '무조건 AI 빌드')
    }

    environment {
        // GitLab
        GITLAB_CREDENTIALS = 'gitlab-token'
        GITLAB_URL = 'https://lab.ssafy.com/s13-final/S13P31A106.git'
        
        // Docker Registry
        REGISTRY_LOCAL = "localhost:${env.REGISTRY_PORT}"
        REGISTRY_PRIVATE = "${env.REGISTRY_PRIVATE_IP}:${env.REGISTRY_PORT}"
        REGISTRY_PUBLIC = "${env.REGISTRY_PUBLIC_IP}:${env.REGISTRY_PORT}"
        
        // Backend
        BACKEND_IMAGE = 'hand-backend'
        BACKEND_SERVER = "${env.BACKEND_SERVER_IP}"
        
        // Credentials
        SSH_CREDENTIALS = 'server-ssh-key'
    }
    
    options {
        gitLabConnection('GitLab')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }
    
    triggers {
        gitlab(
            triggerOnPush: true,
            triggerOnMergeRequest: false,
            triggerOnAcceptedMergeRequest: true,
            branchFilterType: 'NameBasedFilter',
            includeBranchesSpec: 'dev',
            targetBranchRegex: 'dev',
            secretToken: "${env.GITLAB_WEBHOOK_TOKEN}"
        )
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out code...'
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/dev']],
                    userRemoteConfigs: [[
                        url: "${GITLAB_URL}",
                        credentialsId: "${GITLAB_CREDENTIALS}"
                    ]]
                ])
                sh 'git log -1 --oneline'
            }
        }

        stage('Backend CI/CD') {
            when {
                beforeAgent true
                anyOf {
                    changeset pattern: "backend/**", caseSensitive: true
                    expression { return params.FORCE_BUILD_BACKEND }
                }
            }
            stages {
                stage('Backend Docker Build & Push') {
                    steps {
                        dir('backend') {
                            echo '🐳 Building and Pushing Docker Image to Registry...'
                            sh """
                                # Docker Multi-stage build로 Gradle 빌드 포함
                                docker build -t ${REGISTRY_LOCAL}/${BACKEND_IMAGE}:${BUILD_NUMBER} .
                                docker tag ${REGISTRY_LOCAL}/${BACKEND_IMAGE}:${BUILD_NUMBER} ${REGISTRY_LOCAL}/${BACKEND_IMAGE}:latest

                                # Registry에 Push
                                docker push ${REGISTRY_LOCAL}/${BACKEND_IMAGE}:${BUILD_NUMBER}
                                docker push ${REGISTRY_LOCAL}/${BACKEND_IMAGE}:latest

                                echo "✅ Pushed to Registry: ${REGISTRY_LOCAL}/${BACKEND_IMAGE}:latest"
                            """
                        }
                    }
                }
                
                stage('Backend Deploy to Server 2') {
                    steps {
                        echo '🚀 Deploying Backend to Server 2...'
                        withCredentials([
                            file(credentialsId: 'backend-env', variable: 'ENV_FILE')
                        ]) {
                            sshagent([SSH_CREDENTIALS]) {
                                sh """
                                    # .env 파일 전송
                                    echo "📤 Transferring .env file..."
                                    scp -o StrictHostKeyChecking=no \${ENV_FILE} ubuntu@${BACKEND_SERVER}:/home/ubuntu/.env
                                    
                                    # 서버2에서 배포 실행
                                    ssh -o StrictHostKeyChecking=no ubuntu@${BACKEND_SERVER} '
                                        # Registry에서 이미지 Pull
                                        echo "📥 Pulling image from Registry..."
                                        docker pull ${REGISTRY_PUBLIC}/${BACKEND_IMAGE}:latest
                                        
                                        # 기존 컨테이너 중지 및 제거
                                        echo "🛑 Stopping old container..."
                                        docker stop hand-backend 2>/dev/null || true
                                        docker rm hand-backend 2>/dev/null || true
                                        
                                        # 새 컨테이너 실행
                                        echo "🚀 Starting new container..."
                                        docker run -d \\
                                            --name hand-backend \\
                                            -p 8080:8080 \\
                                            --env-file /home/ubuntu/.env \\
                                            --restart unless-stopped \\
                                            ${REGISTRY_PUBLIC}/${BACKEND_IMAGE}:latest
                                        
                                        # 컨테이너 실행 확인
                                        echo "⏳ Waiting for container to start..."
                                        sleep 10
                                        
                                        if docker ps | grep -q hand-backend; then
                                            echo "✅ Container is running!"
                                            docker ps | grep hand-backend
                                        else
                                            echo "❌ Container failed to start!"
                                            docker logs hand-backend
                                            exit 1
                                        fi
                                    '
                                """
                            }
                        }
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ Backend 배포 성공!'
            updateGitlabCommitStatus name: 'build', state: 'success'
        }
        failure {
            echo '❌ Backend 배포 실패!'
            updateGitlabCommitStatus name: 'build', state: 'failed'
        }
        always {
            echo '🧹 Cleaning up...'
            sh '''
                # 빌드 번호가 붙은 오래된 이미지 정리 (latest는 유지)
                docker images | grep ${BACKEND_IMAGE} | grep -v latest | awk '{print $3}' | xargs -r docker rmi -f || true
                # Dangling 이미지 정리
                docker image prune -f || true
            '''
            cleanWs()
        }
    }
}