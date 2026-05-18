package com.demo.warehouse.config;

import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import com.demo.warehouse.repository.BaseRepository;
import com.demo.warehouse.repository.BaseRepositoryImpl;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.demo.warehouse.repository",
    repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class
)
public class SpringDataJpaConfig {
}

class BaseRepositoryFactoryBean<R extends Repository<T, ID>, T, ID> extends JpaRepositoryFactoryBean<R, T, ID> {

    public BaseRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        return new BaseRepositoryFactory(entityManager);
    }

    private static class BaseRepositoryFactory extends JpaRepositoryFactory {

        public BaseRepositoryFactory(EntityManager entityManager) {
            super(entityManager);
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
            // Only use BaseRepositoryImpl for repositories that extend BaseRepository
            if (BaseRepository.class.isAssignableFrom(information.getRepositoryInterface())) {
                JpaEntityInformation<?, ?> entityInformation = getEntityInformation(information.getDomainType());
                return new BaseRepositoryImpl<>((JpaEntityInformation) entityInformation, entityManager);
            }
            // For other repositories (like UserRepository extending JpaRepository), use default behavior
            return super.getTargetRepository(information, entityManager);
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            // Only use BaseRepository as base class for repositories that extend it
            if (BaseRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
                return BaseRepository.class;
            }
            // For other repositories, use default base class
            return super.getRepositoryBaseClass(metadata);
        }
    }
}

