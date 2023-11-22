package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MQConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MQConfig.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<String> queueNames = new ArrayList<>();

    private int currentQueueIndex = 0;

    @PostConstruct
    public void init() {
        queueNames = getQueueNamesFromDatabase();
        LOGGER.info("Initialized with {} queue names from the database", queueNames.size());
    }
//    @Autowired
//    private ConnectionFactory connectionFactory;
//
//    @Autowired
//    private MessageListener messageListener; // Assuming your MessageListener is in the same package

//    @Bean
//    public SimpleMessageListenerContainer messageListenerContainer() {
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.setQueueNames("queueName"); // Replace with your actual queue name
//        container.setMessageListener((org.springframework.amqp.core.MessageListener) messageListener);
//
//        return container;
//    }
    @Bean
    public Queue getDynamicQueueName() {
        if (!queueNames.isEmpty() && currentQueueIndex < queueNames.size()) {
            String queueName = queueNames.get(currentQueueIndex);
            currentQueueIndex++;

            try {
                jdbcTemplate.update("DELETE FROM dataimport WHERE Column_1 = ?", queueName);
                LOGGER.info("Deleted queue name '{}' from the database", queueName);
            } catch (Exception e) {
                LOGGER.error("Error deleting queue name from database: {}", e.getMessage(), e);
                throw new RuntimeException("Error deleting queue name from database: " + e.getMessage());
            }

            return new Queue(queueName);
        } else {
            LOGGER.error("No more queue names available.");
            throw new RuntimeException("No more queue names available.");
        }
    }

    private List<String> getQueueNamesFromDatabase() {
        try {
            List<String> queueNames = jdbcTemplate.queryForList("SELECT Column_1 FROM dataimport", String.class);
            LOGGER.info("Fetched {} queue names from the database", queueNames.size());
            return queueNames;
        } catch (Exception e) {
            LOGGER.error("Error fetching queue names from database: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching queue names from database: " + e.getMessage());
        }
    }
}
