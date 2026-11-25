package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.PurchaseOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {
    @Select("SELECT COALESCE(SUM(total_amount),0) FROM purchase_order" +
            "WHERE created_at >= #{startTime} AND created_at < #{endTime}" +
            "AND status IN (1,2)")
    BigDecimal sumAmountBYDateRange(@Param("startTime")LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);
}
