package com.github.iammuuo.nip.authz.entity;

import java.time.Instant;
import java.util.UUID;

import com.github.iammuuo.nip.account.entity.Account;
import com.github.iammuuo.nip.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * ApiKey
 */
@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ApiKey extends BaseEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "key_hash", nullable = false, length = 64, unique = true)
  private String keyHash;

  @Column(name = "key_id", nullable = false, length = 36, unique = true)
  private String keyId;

  private String label;

  @Column(name = "scopes", nullable = false, length = 512)
  private String scopes;

  @Column(name = "last_used_at")
  Instant lastUsedAt;

  @Column(name = "expires_at")
  Instant expiresAt;

  @ManyToOne
  @JoinColumn(name = "account_id", referencedColumnName = "id")
  Account account;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  boolean active = true;

}
