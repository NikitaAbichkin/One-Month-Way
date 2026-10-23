package com.company.marketplace.catalog.repository;

import com.company.marketplace.catalog.dto.ProductRequest;
import com.company.marketplace.catalog.model.Product;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {
    private final JdbcClient jdbcClient;

    public ProductRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Long insert(ProductRequest request) {
        return jdbcClient.sql("""
        insert into products (name, category, price) values (:name, :category, :price)
        returning id
        """)
                .param("name", request.name())
                .param("category", request.category() == null ? "" : request.category())
                .param("price", request.price())
                .query(Long.class)
                .single();
    }

    // 0 - такого товара нет
    public int update(Long id, ProductRequest request) {
        return jdbcClient.sql("""
        update products set name = :name, category = :category, price = :price where id = :id
        """)
                .param("id", id)
                .param("name", request.name())
                .param("category", request.category() == null ? "" : request.category())
                .param("price", request.price())
                .update();
    }

    public Optional<Product> findById(Long id) {
        return jdbcClient.sql("select id, name, category, price from products where id = :id")
                .param("id", id)
                .query(Product.class)
                .optional();
    }

    // поиск по части названия и/или по категории; пустая строка - фильтр не применяется
    public List<Product> search(String query, String category) {
        return jdbcClient.sql("""
        select id, name, category, price from products
        where (:q = '' or name ilike '%' || :q || '%')
          and (:category = '' or category = :category)
        order by id
        """)
                .param("q", query)
                .param("category", category)
                .query(Product.class)
                .list();
    }
}
