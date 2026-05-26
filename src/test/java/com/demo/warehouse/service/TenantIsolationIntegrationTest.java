package com.demo.warehouse.service;

import com.demo.warehouse.domain.Dress;
import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.mapper.DressDtos;
import com.demo.warehouse.repository.DressRepository;
import com.demo.warehouse.repository.UserRepository;
import com.demo.warehouse.tenantFilter.UserContextHolder;
import com.demo.warehouse.testutils.TestFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TenantIsolationIntegrationTest {

    @Autowired
    private DressService dressService;

    @Autowired
    private DressRepository dressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.demo.warehouse.repository.TenantRepository tenantRepository;

    private Tenant tenant1;
    private Tenant tenant2;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Create two tenants
        tenant1 = new Tenant();
        
        tenant1.setName("Tenant 1");

        tenant2 = new Tenant();
        tenant2.setName("Tenant 2");

        // Save tenants first
        tenantRepository.save(tenant1);
        tenantRepository.save(tenant2);

        // Create users for each tenant
        user1 = new User();
        user1.setUsername("user1");
        user1.setAuth0Sub("auth0|user1");
        user1.setTenant(tenant1);
        user1.setActiveUserContextId(null);

        user2 = new User();
        user2.setUsername("user2");
        user2.setAuth0Sub("auth0|user2");
        user2.setTenant(tenant2);
        user2.setActiveUserContextId(null);

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        // Create dresses for tenant 1
        var dress1 = new Dress();
        dress1.setTitle("Dress 1 - Tenant 1");
        dress1.setSku("SKU001");
        dress1.setSize("M");
        dress1.setColor("#FF0000");
        dress1.setStock(10);
        dress1.setPrice(new BigDecimal("100.00"));
        dress1.setTenant(tenant1);
        dressRepository.save(dress1);

        var dress2 = new Dress();
        dress2.setTitle("Dress 2 - Tenant 1");
        dress2.setSku("SKU002");
        dress2.setSize("L");
        dress2.setColor("#00FF00");
        dress2.setStock(20);
        dress2.setPrice(new BigDecimal("150.00"));
        dress2.setTenant(tenant1);
        dressRepository.save(dress2);

        // Create dresses for tenant 2
        var dress3 = new Dress();
        dress3.setTitle("Dress 3 - Tenant 2");
        dress3.setSku("SKU003");
        dress3.setSize("S");
        dress3.setColor("#0000FF");
        dress3.setStock(5);
        dress3.setPrice(new BigDecimal("80.00"));
        dress3.setTenant(tenant2);
        dressRepository.save(dress3);

        var dress4 = new Dress();
        dress4.setTitle("Dress 4 - Tenant 2");
        dress4.setSku("SKU004");
        dress4.setSize("XL");
        dress4.setColor("#FFFF00");
        dress4.setStock(15);
        dress4.setPrice(new BigDecimal("200.00"));
        dress4.setTenant(tenant2);
        dressRepository.save(dress4);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void whenQueryingAsTenant1_ShouldOnlyReturnTenant1Dresses() {
        // Arrange
        TestFactory.setUserContextHolder(user1);

        // Act
        var spec = com.demo.warehouse.specification.DressSpecification.filterBy(
                new DressDtos.DressFilterRequest(null, null, null, null, null, null, null, null));
        var result = dressService.page(spec, PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(d -> d.title().contains("Tenant 1")));
    }

    @Test
    void whenQueryingAsTenant2_ShouldOnlyReturnTenant2Dresses() {
        // Arrange
        TestFactory.setUserContextHolder(user2);
        
        // Act
        var spec = com.demo.warehouse.specification.DressSpecification.filterBy(
                new DressDtos.DressFilterRequest(null, null, null, null, null, null, null, null));
        var result = dressService.page(spec, PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(d -> d.title().contains("Tenant 2")));
    }

    @Test
    void whenQueryingWithFilter_ShouldOnlyReturnMatchingTenantDresses() {
        // Arrange
        TestFactory.setUserContextHolder(user1);
        
        // Act
        var spec = com.demo.warehouse.specification.DressSpecification.filterBy(
                new DressDtos.DressFilterRequest("Dress 1", null, null, null, null, null, null, null));
        var result = dressService.page(spec, PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Dress 1 - Tenant 1", result.getContent().get(0).title());
    }

    @Test
    void whenContextNotSet_ShouldThrowException() {
        // Arrange - No context set
        UserContextHolder.clear();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            var spec = com.demo.warehouse.specification.DressSpecification.filterBy(
                    new DressDtos.DressFilterRequest(null, null, null, null, null, null, null, null));
            dressService.page(spec, PageRequest.of(0, 10));
        });
    }

    @Test
    void whenCreatingDress_ShouldAssignCorrectTenant() {
        // Arrange
        TestFactory.setUserContextHolder(user1);
        
        var request = new DressDtos.DressCreateRequest(
                "New Dress",
                "SKU005",
                "M",
                "#FF00FF",
                new BigDecimal("120.00")
        );

        // Act
        var result = dressService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals("New Dress", result.title());
        assertEquals("SKU005", result.sku());
        
        // Verify the dress was created with the correct tenant
        var savedDress = dressRepository.findById(result.id());
        assertTrue(savedDress.isPresent());
        assertEquals(tenant1.getId(), savedDress.get().getTenant().getId());
    }

    @Test
    void whenUpdatingDress_ShouldOnlyAccessOwnTenantDress() {
        // Arrange
        TestFactory.setUserContextHolder(user1);
        
        // Get a dress from tenant 1 using service
        var spec = com.demo.warehouse.specification.DressSpecification.filterBy(
                new DressDtos.DressFilterRequest("Dress 1", null, null, null, null, null, null, null));
        var dresses = dressService.page(spec, PageRequest.of(0, 1));
        var tenant1DressId = dresses.getContent().get(0).id();

        var updateRequest = new DressDtos.DressUpdateRequest(
                tenant1DressId,
                "Updated Dress 1",
                "SKU001-UPDATED",
                "L",
                "#00FFFF",
                new BigDecimal("130.00")
        );

        // Act
        var result = dressService.update(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Dress 1", result.title());
        assertEquals("SKU001-UPDATED", result.sku());
    }
}
