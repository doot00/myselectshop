package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.*;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.FolderRepository;
import com.sparta.myselectshop.repository.ProductFolderRepository;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    private final FolderRepository folderRepository;
    private final ProductFolderRepository productFolderRepository;
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

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProducts(User user, int page, int size, String sortBy, boolean isAsc){
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort); // 객체를 넘겨 준다.

        UserRoleEnum userRoleEnum = user.getRole(); // 권한 정보가 담겨 진다.
        Page<Product> productList;

        if(userRoleEnum == UserRoleEnum.USER) {
            productList = productRepository.findAllByUser(user, pageable);
        } else {
            productList = productRepository.findAll(pageable);
        }

        return productList.map(ProductResponseDto::new);
    }


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

    public void addFolder(Long productId, Long folderId, User user) {
        Product product = productRepository.findById(productId).orElseThrow(
                ()->new NullPointerException("해당 상품이 존재하지 않습니다.")
        );

        Folder folder = folderRepository.findById(folderId).orElseThrow(
                ()->new NullPointerException("해당 폴더가 존재하지 않습니다.")
        );

        if(!product.getUser().getId().equals(user.getId())
            // 해당하지 않으면 false가 나온다.
            || !folder.getUser().getId().equals(user.getId())){
            throw  new IllegalArgumentException("회원님의 관심상품이 아니거나, 회원님의 폴더가 아닙니다.");
        }

        // 해당 폴더가 등록된 폴더인지 확인 할 필요가 있다. 중복 확인하기
        Optional<ProductFolder> overlapFolder = productFolderRepository.findByProductAndFolder(product, folder);

        if(overlapFolder.isPresent()){
            throw new IllegalArgumentException("중복된 폴더입니다.");
        }

        productFolderRepository.save(new ProductFolder(product, folder)); // 최종적으로 데이터를 저장한다.

    }

    public Page<ProductResponseDto> getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, User user) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 해당 폴더에 등록되어 있는 상품을 가져온다.
        Page<Product> productList = productRepository.findAllByUserAndProductFolderList_FolderId(user, folderId, pageable);
        Page<ProductResponseDto> responseDtoList = productList.map(ProductResponseDto::new);

        return responseDtoList;
    }
}
