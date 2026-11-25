package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.Category;

import java.util.List;
import java.util.Map;

public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT c.category_id AS id,c.category_name,COUNT(p.product_id) AS count" +
            "FROM category c" +
            "LEFT JOIN product p ON c.category_id = p.category_id" +
            "GROUP BY c.category_id,c.category_name" +
            "ORDER BY count DESC")
    List<Map<String,Object>> getCategoriesWithProductCount();
}
