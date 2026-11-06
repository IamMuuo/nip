package com.github.iammuuo.nip.account.entity;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.github.iammuuo.nip.common.entity.BaseEntity;
import com.github.iammuuo.nip.authz.entity.ApiKey;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Account extends BaseEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column
  private String description;

  @Column(nullable = false)
  @Builder.Default
  private boolean active = true;

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ApiKey> apiKeys;

  @PrePersist
  void onAccountCreated() {
    final Instant now = Instant.now();
    this.active = true;
    this.setCreatedAt(now);
    this.setUpdatedAt(now);

  }

  @PreUpdate
  void onAccountUpdated() {
    this.setUpdatedAt(Instant.now());
  }

  public Account(String name, String description) {
    this.name = name;
    this.description = description;
  }

}
