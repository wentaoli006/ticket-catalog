package com.owen.infrastructure.config;

import com.owen.domain.supplier.acl.PoiPort;
import com.owen.domain.supplier.acl.SupplierCatalogPort;
import com.owen.domain.supplier.repository.IApiProjectRepository;
import com.owen.domain.supplier.repository.IPoiRepository;
import com.owen.domain.supplier.service.SupplierStaticPullService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 装配静态拉取服务。领域类不依赖 Spring，新供应商只需要再提供一个 CatalogPort。
 */
@Configuration
public class SupplierPullConfiguration {

    @Bean
    public SupplierStaticPullService supplierStaticPullService(List<SupplierCatalogPort> catalogPorts,
                                                                PoiPort poiPort,
                                                                IApiProjectRepository projectRepository,
                                                                IPoiRepository poiRepository) {
        return new SupplierStaticPullService(catalogPorts, poiPort, projectRepository, poiRepository);
    }

}
