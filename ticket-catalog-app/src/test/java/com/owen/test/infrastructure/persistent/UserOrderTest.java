package com.owen.test.infrastructure.persistent;

import com.owen.infrastructure.persistent.dao.IUserOrderDao;
import com.owen.infrastructure.persistent.po.UserOrderPO;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 单元测试
 */
@Slf4j
@SpringBootTest
public class UserOrderTest {

    @Resource
    private IUserOrderDao userOrderDao;

    @Test
    public void test_selectByUserId() {
        List<UserOrderPO> list = userOrderDao.selectByUserId("demo_FOawiP");
        log.info("测试结果：{}", JSON.toJSONString(list));
    }

    @Test
    public void test_insert() {
        for (int i = 0; i < 10; i++) {
            UserOrderPO userOrderPO = UserOrderPO.builder()
                    .userName("张三")
                    .userId("demo_" + RandomStringUtils.randomAlphabetic(6))
                    .userMobile("+86 13521408***")
                    .sku("13811216")
                    .skuName("示例商品")
                    .orderId(RandomStringUtils.randomNumeric(11))
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(128))
                    .discountAmount(BigDecimal.valueOf(50))
                    .tax(BigDecimal.ZERO)
                    .totalAmount(BigDecimal.valueOf(78))
                    .orderDate(new Date())
                    .orderStatus(0)
                    .isDelete(0)
                    .uuid(UUID.randomUUID().toString().replace("-", ""))
                    .ipv4("127.0.0.1")
                    .ipv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334".getBytes())
                    .extData("{\"device\": {\"machine\": \"IPhone 14 Pro\", \"location\": \"shanghai\"}}")
                    .build();

            userOrderDao.insert(userOrderPO);
        }
    }

    /**
     * 路由测试
     */
    @Test
    public void test_idx() {
        for (int i = 0; i < 50; i++) {
            String user_id = "demo_" + RandomStringUtils.randomAlphabetic(6);
            log.info("测试结果 {}", (user_id.hashCode() ^ (user_id.hashCode()) >>> 16) & 3);
        }
    }

}
