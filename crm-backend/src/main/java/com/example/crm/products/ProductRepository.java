package com.example.crm.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query("delete from Product product where product.id = :id")
    void deleteProduct(long id);
}
