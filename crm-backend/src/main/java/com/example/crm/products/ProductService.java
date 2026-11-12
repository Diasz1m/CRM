package com.example.crm.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ProductService {

    private final ProductRepository products;

    ProductService(ProductRepository products) {
        this.products = products;
    }

    Page<ProductResponse> list(Pageable page) {
        return products.findAll(page).map(Product::toResponse);
    }

    @Transactional
    ProductResponse create(ProductRequest request) {
        return products.save(new Product(request)).toResponse();
    }

    @Transactional
    void delete(long id) {
        products.deleteProduct(id);
    }
}
