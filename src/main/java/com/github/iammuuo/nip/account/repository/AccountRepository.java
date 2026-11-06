package com.github.iammuuo.nip.account.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.github.iammuuo.nip.account.entity.Account;

/**
 * AccountRepository
 */
public interface AccountRepository extends JpaRepository<Account, UUID> {

}
