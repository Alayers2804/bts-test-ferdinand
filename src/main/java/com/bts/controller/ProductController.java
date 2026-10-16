package com.bts.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.bts.dto.ProductRequest;
import com.bts.dto.ProductResponse;
import com.bts.entity.Product;
import com.bts.service.ProductService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@Tag(
    name = "Products",
    description = "Endpoints for searching, retrieving, creating, updating, and deleting products"
)
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(
        summary = "Get all products",
        description = "Returns a paginated product list with optional title search and category filtering."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination or filter parameters",
            content = @Content
        )
    })
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @Parameter(
                description = "Search products by title",
                example = "T-Shirt"
            )
            @RequestParam(required = false) String search,

            @Parameter(
                description = "Filter by product category",
                example = "Clothes"
            )
            @RequestParam(required = false) String category,

            @Parameter(
                description = "Number of products per page",
                example = "10"
            )
            @RequestParam(defaultValue = "10") int limit,

            @Parameter(
                description = "Page number, starting at 1",
                example = "1"
            )
            @RequestParam(defaultValue = "1") int page) {

        List<ProductResponse> products = productService
                .getAllProducts(search, category, limit, page)
                .stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get product by ID",
        description = "Retrieves the details of a single product using its ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Product found",
            content = @Content(
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content
        )
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(
                description = "Product ID",
                example = "1",
                required = true
            )
            @PathVariable Long id) {

        Product product = productService.getProductById(id);

        return ResponseEntity.ok(new ProductResponse(product));
    }

    @PostMapping
    @Operation(
        summary = "Create a product",
        description = "Creates a product using the supplied product details. Requires authorization."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid product data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions",
            content = @Content
        )
    })
    public ResponseEntity<ProductResponse> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Product details to create",
                required = true
            )
            @Valid @RequestBody ProductRequest request) {

        Product product = productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ProductResponse(product));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update a product",
        description = "Updates an existing product. Requires authorization."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Product updated successfully",
            content = @Content(
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid product data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content
        )
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(
                description = "ID of the product to update",
                example = "1",
                required = true
            )
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated product details",
                required = true
            )
            @Valid @RequestBody ProductRequest request) {

        Product product = productService.updateProduct(id, request);

        return ResponseEntity.ok(new ProductResponse(product));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a product",
        description = "Deletes an existing product. Requires authorization."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Product deleted successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content
        )
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(
                description = "ID of the product to delete",
                example = "1",
                required = true
            )
            @PathVariable Long id) {

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
}