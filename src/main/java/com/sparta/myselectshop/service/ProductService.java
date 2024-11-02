package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        // 받아온 dto를 entity 객체로 만든다.
        Product product = productRepository.save(new Product(requestDto));
        return new ProductResponseDto(product);

    }
}
