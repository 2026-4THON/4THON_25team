package com.fourthon.backend.kopis.repository;

import com.fourthon.backend.kopis.domain.SyncState;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncStateRepository extends JpaRepository<SyncState, String> {
}
