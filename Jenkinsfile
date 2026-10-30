pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                dir('D:\\BSTU\\Java\\lab2\\university') {
                    bat 'mvn compile'
                }
            }
        }
        
        stage('Test') {
            steps {
                dir('D:\\BSTU\\Java\\lab2\\university') {
                    bat 'mvn test'
                }
            }
            post {
                always {
                    dir('D:\\BSTU\\Java\\lab2\\university') {
                        // публикация JUnit-отчетов
                        junit 'target/surefire-reports/*.xml'
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                dir('D:\\BSTU\\Java\\lab2\\university') {
                    bat 'mvn package -DskipTests'
                }
            }
        }
    }
}