
package com.example.demo;
//
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.jdbc.core.BatchPreparedStatementSetter;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.stereotype.Component;
//
//@Component
//public class MessageListener {
//
//    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate; // Inject JdbcTemplate
//    @Autowired
//    private RabbitMQService rabbitMQService;
//    @Value("${table.name}")
//     private String tableName;
//    
//    
//    public void saveToDatabase(String tableName, Map<String, String> columnData) {
//        try {
//            StringBuilder columns = new StringBuilder();
//            StringBuilder placeholders = new StringBuilder();
//            List<String> values = new ArrayList<>();
//
//            for (Map.Entry<String, String> entry : columnData.entrySet()) {
//                columns.append(entry.getKey()).append(", ");
//                placeholders.append("?, ");
//                values.add(entry.getValue());
//            }
//
//            // Remove trailing commas
//            columns.delete(columns.length() - 2, columns.length());
//            placeholders.delete(placeholders.length() - 2, placeholders.length());
//
//            String sql = "INSERT INTO " + tableName + " (" + columns.toString() + ") VALUES (" + placeholders.toString() + ")";
//            logger.info("one row   saved to database.");
//
//            jdbcTemplate.update(sql, values.toArray());
//
//        } catch (Exception e) {
//            logger.error("Error saving data to database: {}", e.getMessage());
//        }
//    }
//
//
//    
//    
//  
//    @RabbitListener(queues = "#{@getDynamicQueueName}")
//    public void processExcelData(List<String> rowData, @Header("amqp_receivedRoutingKey") String queueName) {
//        try {
//            List<String> columnNames = getColumnNamesFromDatabase(tableName, 200000);
//
//            if (columnNames.size() == rowData.size()) {
//                Map<String, String> columnData = convertListToMap(columnNames, rowData);
//                saveToDatabase(tableName, columnData);
//                logger.info("Data saved to database for queue: {}", queueName);
//            } else {
//                logger.error("Column names and row data sizes do not match.");
//            }
//
//        } catch (Exception e) {
//            logger.error("Error processing Excel data: " + e.getMessage(), e);
//        } finally {
//           rabbitMQService.deleteQueue(queueName);
//        }
//    }
//
//    public List<String> getColumnNamesFromDatabase(String tableName, int limit) {
//
//        try {
//            return jdbcTemplate.queryForList("SELECT column_name FROM information_schema.columns WHERE table_name = ?", String.class, tableName);
//        } catch (Exception e) {
//            logger.error("Error getting column names: {}", e.getMessage());
//            return new ArrayList<>();
//        }
//    }
//
//    public Map<String, String> convertListToMap(List<String> columnNames, List<String> rowData) {
//        Map<String, String> columnData = new LinkedHashMap<>();
//
//        for (int i = 0; i < columnNames.size(); i++) {
//            columnData.put(columnNames.get(i), rowData.get(i));
//        }
//
//        return columnData;
//    }
//}


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.support.ConsumerCancelledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    @Autowired
    private JdbcTemplate jdbcTemplate; // Inject JdbcTemplate
//    @Autowired
//    private RabbitMQService rabbitMQService;
    
    @Autowired
    private AmqpAdmin amqpAdmin;

    @Value("${table.name}")
    private String tableName;

    // Keep track of the number of processed rows
    private int processedRowCount = 0;
    private int expectedRowCount = 6; // Set this to the total number of rows you expect

    private synchronized void incrementProcessedRowCount() {
        processedRowCount++;
    }

    private synchronized boolean allRowsProcessed() {
        // Check if all rows have been processed
        return processedRowCount >= expectedRowCount;
    }

 

    public void saveToDatabase(String tableName, Map<String, String> columnData) {
        try {
            StringBuilder columns = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();
            List<String> values = new ArrayList<>();

            for (Map.Entry<String, String> entry : columnData.entrySet()) {
                columns.append(entry.getKey()).append(", ");
                placeholders.append("?, ");
                values.add(entry.getValue());
            }

            // Remove trailing commas
            columns.delete(columns.length() - 2, columns.length());
            placeholders.delete(placeholders.length() - 2, placeholders.length());

            String sql = "INSERT INTO " + tableName + " (" + columns.toString() + ") VALUES (" + placeholders.toString() + ")";
            logger.info("one row   saved to database.");

            jdbcTemplate.update(sql, values.toArray());

            // Increment the processed row count after saving to the database
            incrementProcessedRowCount();

        } catch (Exception e) {
            logger.error("Error saving data to database: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "#{@getDynamicQueueName}")
    public void processExcelData(List<String> rowData, @Header("amqp_receivedRoutingKey") String queueName) {
        try {
            List<String> columnNames = getColumnNamesFromDatabase(tableName, 200000);

            if (columnNames.size() == rowData.size()) {
                Map<String, String> columnData = convertListToMap(columnNames, rowData);
                saveToDatabase(tableName, columnData);
                logger.info("Data saved to database for queue: {}", queueName);

                // Check if all rows have been processed
                if (allRowsProcessed()) {
                    // Call the method to delete the queue
                	amqpAdmin.deleteQueue(queueName);
                	logger.info("Queue {} deleted after all rows are saved to the database.", queueName);
                }
            } else {
                logger.error("Column names and row data sizes do not match.");
            }

        } catch (Exception e) {
            logger.error("Error processing Excel data: " + e.getMessage(), e);
        }
    }
    


    public List<String> getColumnNamesFromDatabase(String tableName, int limit) {
        try {
            return jdbcTemplate.queryForList("SELECT column_name FROM information_schema.columns WHERE table_name = ?", String.class, tableName);
        } catch (Exception e) {
            logger.error("Error getting column names: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String, String> convertListToMap(List<String> columnNames, List<String> rowData) {
        Map<String, String> columnData = new LinkedHashMap<>();

        for (int i = 0; i < columnNames.size(); i++) {
            columnData.put(columnNames.get(i), rowData.get(i));
        }

        return columnData;
    }
}

//rabbitMQService.deleteQueue(queueName);


