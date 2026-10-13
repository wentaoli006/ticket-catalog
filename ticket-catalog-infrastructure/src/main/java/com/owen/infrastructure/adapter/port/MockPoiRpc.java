package com.owen.infrastructure.adapter.port;

import com.owen.domain.supplier.adapter.port.IPoiPort;
import com.owen.types.exception.AppException;
import com.owen.types.log.Logs;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 外部 POI RPC 的本地替身。按供应商名称和供应商项目 ID 返回稳定的正 Long。
 */
@Component
public class MockPoiRpc implements IPoiPort {

    @Override
    public Long apply(String supplierName, String supplierProjectId) {
        if (supplierName == null || supplierName.isBlank() || supplierProjectId == null || supplierProjectId.isBlank()) {
            throw new AppException("POI_APPLY_FAILED", "申请 POI 缺少供应商名称或项目ID");
        }
        long poiId = Integer.toUnsignedLong((supplierName + ":" + supplierProjectId).hashCode()) % Integer.MAX_VALUE + 1;
        Logs.info("mock申请POI", supplierName + " 项目" + supplierProjectId + " 申请到 " + poiId, Map.of(
                "supplierProjectId", supplierProjectId,
                "poiId", poiId));
        return poiId;
    }

}
