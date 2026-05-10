package com.demo.warehouse.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
@NoRepositoryBean
public interface BaseRespository<T,D> extends JpaRepository<T,D>, JpaSpecificationExecutor<T> {
<S> List<S> findAllProjectedBy(Class<S> type);}
