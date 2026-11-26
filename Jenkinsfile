pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                dir('D:\\BSTU\\Java\\lab2\\university') {
                    bat 'mvn compile'
                }
            }
        }
        
        stage('Static Analysis') {
            steps {
                dir('D:\\BSTU\\Java\\lab2\\university') {
                    bat 'mvn checkstyle:check'
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
    post {
        success {
            echo 'Пайплайн успешно завершен!'
        }
        failure {
            echo 'Пайплайн завершился с ошибкой!'
        }
    }
}