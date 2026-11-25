package org.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.entity.*;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Resource
    private ProductMapper productMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private InventoryMapper inventoryMapper;
    @Resource
    private PurchaseOrderMapper purchaseOrderMapper;
    @Resource
    private PurchaseOrderDetailMapper purchaseOrderDetailMapper;

    @Transactional
    public void createProduct(Product product) {
        if (isProductNameExists(product.getProductName(),null)){
            throw new ServiceException("商品名称已存在");
        }

        if (categoryMapper.selectById(product.getCategoryId()) == null){
            throw new ServiceException("分类不存在");
        }

        if (product.getBarcode() != null && !product.getBarcode().trim().isEmpty() &&
        isBarcodeExists(product.getBarcode(),null)){
            throw new ServiceException("商品条码已存在");
        }

        if (product.getStatus() == null){
            product.setStatus(1);
        }

        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        if (productMapper.insert(product) <= 0) {
            throw new ServiceException("商品创建失败");
        }

        Inventory inventory =new Inventory();
        inventory.setProductId(product.getProductId());
        inventory.setQuantity(0);
        inventory.setMinQuantity(10);
        inventory.setMaxQuantity(1000);
        inventory.setUpdatedAt(now);

        if (inventoryMapper.insert(inventory) <= 0){
            throw new ServiceException("库存记录创建失败");
        }
    }

    @Transactional
    public void updateProduct(Product product){
        Product existingProduct = productMapper.selectById(product.getProductId());
        if (existingProduct == null) {
            throw new ServiceException("商品不存在");
        }

        if (!existingProduct.getProductName().equals(product.getProductName()) &&
                isProductNameExists(product.getProductName(),product.getProductId())) {
            throw new ServiceException("商品名称已存在");
        }

        if (categoryMapper.selectById(product.getCategoryId()) == null){
            throw new ServiceException("分类不存在");
        }

        if (product.getBarcode() != null && !product.getBarcode().trim().isEmpty() &&
        !product.getBarcode().equals(existingProduct.getBarcode()) &&
        isBarcodeExists(product.getBarcode(),product.getProductId())){
            throw new ServiceException("商品条码已存在");
        }

        product.setUpdatedAt(LocalDateTime.now());

        if (productMapper.updateById(product) <=0) {
            throw new ServiceException("商品更新失败");
        }
    }

    @Transactional
    public void deleteProduct(Integer productId){
        if (productMapper.selectById(productId) == null){
            throw new ServiceException("商品不存在");
        }

        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getProductId,productId)
        );

        if (inventory !=null && inventory.getQuantity() > 0){
            throw new ServiceException("该商品有库存，无法删除");
        }

        if (inventory !=null) {
            inventoryMapper.deleteById(inventory.getInventoryId());
        }

        if (productMapper.deleteById(productId) <=0) {
            throw new ServiceException("商品删除失败");
        }
    }

    public Product getProductById(Integer productId){
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }

        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            product.setCategoryName(category.getCategoryName());
        }

        Inventory inventory =inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getProductId,productId)
        );
        if (inventory != null) {
            product.setInventory(inventory.getQuantity());
        }else {
            product.setInventory(0);
        }
        return  product;
    }

    public List<Product> getAllProducts(){
        List<Product> products =productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .orderByAsc(Product::getProductId)
        );
        if (!products.isEmpty()){
            List<Integer> categoryIds = products.stream()
                    .map(Product::getCategoryId)
                    .distinct()
                    .collect(Collectors.toList());

            List<Category> categories = categoryMapper.selectList(
                    new LambdaQueryWrapper<Category>()
                            .in(Category::getCategoryId,categoryIds)
            );

            Map<Integer,String> categoryIdToNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getCategoryId,Category::getCategoryName));
                    products.forEach(product ->
                            product.setCategoryName(categoryIdToNameMap.get(product.getCategoryId()))
            );

            List<Integer> productIds =products.stream()
                    .map(Product::getProductId)
                    .collect(Collectors.toList());
            List<Inventory> inventories = inventoryMapper.selectList(
                    new LambdaQueryWrapper<Inventory>()
                            .in(Inventory::getProductId,productIds)
            );

            Map<Integer,Integer> productIdToInventoryMap = inventories.stream()
                    .collect(Collectors.toMap(Inventory::getProductId,Inventory::getQuantity));

            products.forEach(product ->
                    product.setInventory(productIdToInventoryMap.getOrDefault(product.getProductId(),0))
            );
        }
        return products;
    }

    public Page<Product> getProductsByPage(
            String productName,
            Integer categoryId,
            String barcode,
            Integer status,
            Integer currentPage,
            Integer pageSize) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();

        if (productName !=null && !productName.trim().isEmpty()){
            queryWrapper.like(Product::getProductName,productName);
        }
        if (categoryId !=null) {
            queryWrapper.eq(Product::getCategoryId,categoryId);
        }
        if (barcode !=null && !barcode.trim().isEmpty()){
            queryWrapper.eq(Product::getBarcode,barcode);
        }
        if (status !=null) {
            queryWrapper.eq(Product::getStatus,status);
        }
        queryWrapper.orderByAsc(Product::getProductId);
        Page<Product> page = productMapper.selectPage(new Page<>(currentPage,pageSize),queryWrapper);

        List<Product> records = page.getRecords();
        if (!records.isEmpty()) {
            List<Integer> categoryIds = records.stream()
                    .map(Product::getCategoryId)
                    .distinct()
                    .collect(Collectors.toList());

            List<Category> categories = categoryMapper.selectList(
                    new LambdaQueryWrapper<Category>()
                            .in(Category::getCategoryId,categoryIds)
            );

            Map<Integer,String> categoryIdToNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getCategoryId,Category::getCategoryName));

            List<Integer> productIds = records.stream()
                    .map(Product::getProductId)
                    .collect(Collectors.toList());
            List<Inventory> inventories = inventoryMapper.selectList(
                    new LambdaQueryWrapper<Inventory>()
                            .in(Inventory::getProductId,productIds)
            );

            Map<Integer,Integer> productIdToInventoryMap = inventories.stream()
                    .collect(Collectors.toMap(Inventory::getProductId,Inventory::getQuantity));

            records.forEach( product -> {
                product.setCategoryName(categoryIdToNameMap.get(product.getCategoryId()));
                product.setInventory(productIdToInventoryMap.getOrDefault(product.getProductId(),0));
            });
        }
        return page;
    }

    public List<Product> getProductsByCategory(Integer categoryId) {
        if (categoryId == null){
            throw new ServiceException("分类ID不能为空");
        }
        if (categoryMapper.selectById(categoryId) == null){
            throw new ServiceException("分类不存在");
        }
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId,categoryId)
                        .orderByAsc(Product::getProductId)
        );
        if (!products.isEmpty()) {
            Category category = categoryMapper.selectById(categoryId);
            String categoryName = category != null ? category.getCategoryName() : "";

            List<Integer> productIds =products.stream()
                    .map(Product::getProductId)
                    .collect(Collectors.toList());
            List<Inventory> inventories = inventoryMapper.selectList(
                    new LambdaQueryWrapper<Inventory>()
                            .in(Inventory::getProductId,productIds)
            );

            Map<Integer,Integer> productIdToInventoryMap = inventories.stream()
                    .collect(Collectors.toMap(Inventory::getProductId,Inventory::getQuantity));

            products.forEach(product -> {
                product.setCategoryName(categoryName);
                product.setInventory(productIdToInventoryMap.getOrDefault(product.getProductId(),0));
            });
        }
        return products;
    }

    public void updateProductStatus(Integer productId,Integer status) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }

        if (status !=0 && status !=1){
            throw new ServiceException("状态值无效，只能是0(下架)或1(上架)");
        }
        Product updateProduct =new Product();
        updateProduct.setProductId(productId);
        updateProduct.setStatus(status);
        updateProduct.setUpdatedAt(LocalDateTime.now());

        if (productMapper.updateById(updateProduct) <=0) {
            throw new ServiceException("商品状态更新失败");
        }
    }

    public List<Product> searchProductsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<Product> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(Product::getProductName,keyword)
                .or()
                .like(Product::getBarcode,keyword)
                .orderByAsc(Product::getProductId);
        List<Product> products= productMapper.selectList(queryWrapper);

        if (!products.isEmpty()){
            List<Integer> categoryIds = products.stream()
                    .map(Product::getCategoryId)
                    .distinct()
                    .collect(Collectors.toList());

            List<Category> categories =categoryMapper.selectList(
                    new LambdaQueryWrapper<Category>()
                            .in(Category::getCategoryId,categoryIds)
            );

            Map<Integer,String> categoryIdToNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getCategoryId,Category::getCategoryName));

            products.forEach(product ->
                product.setCategoryName(categoryIdToNameMap.get(product.getCategoryId()))
                );
        }
        return products;
    }

    private boolean isProductNameExists(String productName,Integer excludeId) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getProductName,productName);
        if (excludeId !=null) {
            queryWrapper.ne(Product::getProductId,excludeId);
        }
        return productMapper.selectCount(queryWrapper) >0;
    }

    private  boolean isBarcodeExists(String barcode,Integer excludeId) {
        LambdaQueryWrapper<Product> queryWrapper=new LambdaQueryWrapper<Product>()
                .eq(Product::getBarcode,barcode);
        if (excludeId !=null) {
            queryWrapper.ne(Product::getProductId,excludeId);
        }
        return productMapper.selectCount(queryWrapper) >0;
    }

    public Integer count(){return Math.toIntExact(productMapper.selectCount(null));}

    public BigDecimal getSalesByCategoryAndTimeRange(String categoryName,String timeRange){
        Category category=categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getCategoryName,categoryName)
        );
        if (category == null) {
            throw new ServiceException("分类不存在: " + categoryName);
        }

        List<Product> products =productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId,category.getCategoryId())
        );

        if (products.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<Integer> productIds = products.stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());

        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        switch (timeRange) {
            case "month":
                startDate = endDate.minusMonths(1);
                break;
            case "quarter":
                startDate = endDate.minusMonths(3);
                break;
            case "year":
                startDate = endDate.minusYears(1);
                break;
            default:
                startDate = endDate.minusMonths(1);
        }

        List<PurchaseOrderDetail> details =purchaseOrderDetailMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderDetail>()
                        .in(PurchaseOrderDetail::getProductId,productIds)
        );
        if (details.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<Integer> orderIds = details.stream()
                .map(PurchaseOrderDetail::getOrderId)
                .distinct()
                .collect(Collectors.toList());

        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .in(PurchaseOrder::getOrderId,orderIds)
                        .between(PurchaseOrder::getCreatedAt,startDate,endDate)
                        .eq(PurchaseOrder::getStatus,2)
        );
        if (orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<Integer> validOrderIds = orders.stream()
                .map(PurchaseOrder::getOrderId)
                .collect(Collectors.toList());

        BigDecimal totalAmount = details.stream()
                .filter(detail -> validOrderIds.contains(detail.getOrderId()))
                .map(PurchaseOrderDetail::getAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return totalAmount;
    }
}


