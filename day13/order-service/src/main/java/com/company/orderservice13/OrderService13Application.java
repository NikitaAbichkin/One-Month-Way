package com.company.orderservice13;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootApplication
public class OrderService13Application {
    public static void main(String[] args) throws SQLException {
        ConfigurableApplicationContext ctx = SpringApplication.run(OrderService13Application.class);

        DataSource dataSource = ctx.getBean(DataSource.class);

        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet resultSet = st.executeQuery("SELECT * FROM products")){

            while ( resultSet.next()){
                System.out.println(resultSet.getString(1));
            }
        }

    }

}
