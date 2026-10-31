package com.brightloop.subscription.repo;

import com.brightloop.subscription.entity.SubscriptionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionLineRepository extends JpaRepository<SubscriptionLine, UUID> {
    List<SubscriptionLine> findBySubscriptionId(UUID subscriptionId);
}
