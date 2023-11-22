//package com.example.demo;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//@Service
//public class RabbitMQService {
//    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);
//
////    @Value("${spring.rabbitmq.host}")
////    private String rabbitMQManagementApiUrl;
////
////    @Value("${spring.rabbitmq.port}")
////    private String rabbitMQPort;
////    
//    @Value("${spring.rabbitmq.username}")
//    private String rabbitMQUsername;
//
//    @Value("${spring.rabbitmq.password}")
//    private String rabbitMQPassword;
//
//
//    
//    public void deleteQueue(String queueName) {
//        try {
//          //  String apiUrl = "http://" + rabbitMQManagementApiUrl + ":" + rabbitMQUsername + ":" + rabbitMQPassword + "@localhost:15672/api/queues/%2F/" + queueName;
//        	  String apiUrl = "http://localhost:15672/api/queues/test/" + queueName;
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBasicAuth("Z3Vlc3Q6Z3Vlc3Q=");
//            headers.set("Content-Type", "application/json");
//            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
//
//            RestTemplate restTemplate = new RestTemplate();
//           ResponseEntity<String> data = restTemplate.exchange(apiUrl,HttpMethod.DELETE,requestEntity,String.class,"test","queueName");
//
//            // Log the queue name after successful deletion
//            logger.info("Queue {} deleted successfully.", queueName);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////    public void deleteQueue(String queueName) {
////        try {
////            String apiUrl = "http://" + rabbitMQUsername + ":" + rabbitMQPassword + "@localhost:15672/api/queues/%2F/" + queueName;
////
////            HttpHeaders headers = new HttpHeaders();
////            headers.setBasicAuth(rabbitMQUsername, rabbitMQPassword);
////
////            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
////
////            RestTemplate restTemplate = new RestTemplate();
////            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, String.class);
////
////            if (response.getStatusCode() == HttpStatus.OK) {
////                // Queue exists, proceed with deletion
////                restTemplate.exchange(apiUrl, HttpMethod.DELETE, requestEntity, String.class);
////                logger.info("Queue {} deleted successfully.", queueName);
////            } else {
////                // Queue not found
////                logger.warn("Queue {} not found.", queueName);
////            }
////
////        } catch (HttpClientErrorException.NotFound notFoundException) {
////            logger.warn("Queue {} not found.", queueName);
////        } catch (Exception e) {
////            logger.error("Error deleting queue {}: {}", queueName, e.getMessage(), e);
////        }
////    }
////}
//
//
////public void deleteQueue(String queueName) {
////try {
////  String apiUrl = "http://localhost:15672/api/queues/%2F/" + queueName;
////
////  RestTemplate restTemplate = new RestTemplate();
////
////  restTemplate.delete(apiUrl);
////
////  // Log the queue name after successful deletion
////  logger.info("Queue {} deleted successfully.", queueName);
////
////} catch (Exception e) {
////  logger.error("Error deleting queue {}: {}", queueName, e.getMessage(), e);
////}
////}