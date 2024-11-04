package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public static final int MIN_MY_PRICE = 100;

    public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {
        // 받아온 dto를 entity 객체로 만든다.
        Product product = productRepository.save(new Product(requestDto, user));
        return new ProductResponseDto(product);

    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductMypriceRequestDto requestDto) {
        int myprice = requestDto.getMyprice(); // price를 가져온다. 100원 이상이어야 한다.
        if (myprice < MIN_MY_PRICE) {
            // 100보다 작으면
            throw new IllegalArgumentException("유효하지 않은 관심 가격입니다. 최소 " + MIN_MY_PRICE + "원 이상으로 설정해 주세요.");
        }
        Product product = productRepository.findById(id).orElseThrow(() ->
                new NullPointerException("해당 상품을 찾을 수 없습니다.")
        );

        product.update(requestDto);

        return new ProductResponseDto(product);

    }

    public List<ProductResponseDto> getProducts(User user){
        List<Product> productList = productRepository.findAllByUser(user);
        List<ProductResponseDto> responseDtoList = new ArrayList<>();

        for (Product product : productList) {
            responseDtoList.add(new ProductResponseDto(product));
        }

        return responseDtoList;
    }
//    public Page<ProductResponseDto> getProducts(User user, int page, int size, String sortBy, boolean isAsc) {
//        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC; // false면 내림차순이 된다.
//        Sort sort = Sort.by(direction, sortBy); // 정렬항목
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        // 권한 확인
//        UserRoleEnum userRoleEnum = user.getRole(); // 유저의 권한 정보
//        Page<Product> productList;
//
//        if(userRoleEnum == UserRoleEnum.USER) { // 일반 계정이라면, 쿼리 메서드 호출
//            productList = productRepository.findAllByUser(user, pageable);
//        } else {
//            productList = productRepository.findAll(pageable);
//        }
//
//        // 페이지 타입으로 변경해야 하기 떄문에
//        return productList.map(ProductResponseDto::new); // 메서드를 통해 하나씩 돌리면서 생성자가 호출이 된다.
//    }

    @Transactional
    public void updateBySearch(Long id, ItemDto itemDto) {
        Product product = productRepository.findById(id).orElseThrow(()->
                new NullPointerException("해당 상품은 존재하지 않습니다.")
        );
        product.updateByItemDto(itemDto);
    }

    public List<ProductResponseDto> getAllProducts() {
        List<Product> productList = productRepository.findAll(); // findAll로 들어오도록 설정한다.
        List<ProductResponseDto> responseDtoList = new ArrayList<>();

        for (Product product : productList) {
            responseDtoList.add(new ProductResponseDto(product));
        }

        return responseDtoList;
    }
}
