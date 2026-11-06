package com.github.iammuuo.nip.authz.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.iammuuo.nip.authz.entity.ApiKey;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

}
