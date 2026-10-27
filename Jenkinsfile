
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

    

post {
    success {
        emailext(
            to: 'harmanpreetsaini1001@gmail.com',
            subject: "Pipeline SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            mimeType: 'text/html',
            attachmentsPattern: 'trivy-fs-report.txt,trivy-image-report.txt',
            body: """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">

                    <h2 style="color: #188038;">&#10004; Pipeline SUCCESS</h2>

                    <p>Your Jenkins pipeline completed successfully.</p>

                    <table cellpadding="8" cellspacing="0"
                           style="border-collapse: collapse; border: 1px solid #ddd;">
                        <tr>
                            <td><b>Job</b></td>
                            <td>${env.JOB_NAME}</td>
                        </tr>
                        <tr>
                            <td><b>Build Number</b></td>
                            <td>${env.BUILD_NUMBER}</td>
                        </tr>
                        <tr>
                            <td><b>Build Status</b></td>
                            <td style="color: #188038;"><b>SUCCESS</b></td>
                        </tr>
                    </table>

                    <p>Trivy filesystem and Docker image scan reports are attached when generated.</p>

                    <p>
                        <a href="${env.BUILD_URL}">View Jenkins Build</a>
                    </p>

                    <p style="color: #666; font-size: 12px;">
                        Automated notification from Project 5 CI/CD.
                    </p>

                </body>
                </html>
            """
        )
    }

    failure {
        emailext(
            to: 'harmanpreetsaini1001@gmail.com',
            subject: "Pipeline FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: """Pipeline failed.

Job: ${env.JOB_NAME}
Build: ${env.BUILD_NUMBER}

Console output:
${env.BUILD_URL}console"""
        )
    }
}
