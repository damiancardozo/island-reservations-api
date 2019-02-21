package com.upgrade.islandreservationsapi.repository;

import com.upgrade.islandreservationsapi.model.Configuration;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ConfigurationRepository extends CrudRepository<Configuration, String> {

    @Override
    @Cacheable
    Optional<Configuration> findById(String s);
}
