
pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven-3.8.4'
    }

    stages {
        stage('Compile') {
            steps {
                sh 'mvn compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube-Project5') {
                    sh '''
                        mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:5.2.0.4988:sonar \
                          -Dsonar.projectKey=project-board \
                          -Dsonar.projectName=Project-board
                    '''
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn package'
            }
        }

        stage('Publish to Nexus') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'nexus-credentials',
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    sh '''
                        set +x
                        SETTINGS_FILE=$(mktemp)
                        trap 'rm -f "$SETTINGS_FILE"' EXIT

                        cat > "$SETTINGS_FILE" <<EOF
<settings>
  <servers>
    <server>
      <id>maven-releases</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
    <server>
      <id>maven-snapshots</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
EOF

                        mvn deploy -DskipTests -s "$SETTINGS_FILE"
                    '''
                }
            }
        }

        stage('Trivy Filesystem Scan') {
            steps {
                sh '''
                    trivy fs \
                      --scanners vuln \
                      --exit-code 1 \
                      --severity HIGH,CRITICAL \
                      .
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    docker build -t harman1702/project-board:${BUILD_NUMBER} .
                    docker tag harman1702/project-board:${BUILD_NUMBER} \
                        harman1702/project-board:latest
                '''
            }
        }

        stage('Trivy Docker Image Scan') {
            steps {
                sh '''
                    trivy image \
                      --exit-code 1 \
                      --severity HIGH,CRITICAL \
                      harman1702/project-board:${BUILD_NUMBER}
                '''
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKERHUB_USER',
                    passwordVariable: 'DOCKERHUB_TOKEN'
                )]) {
                    sh '''
                        set +x
                        echo "$DOCKERHUB_TOKEN" | docker login \
                            -u "$DOCKERHUB_USER" --password-stdin

                        docker push harman1702/project-board:${BUILD_NUMBER}
                        docker push harman1702/project-board:latest

                        docker logout
                    '''
                }
            }
        }
    }
}
