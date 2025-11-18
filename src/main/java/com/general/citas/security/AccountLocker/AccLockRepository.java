package com.general.citas.security.AccountLocker;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.general.citas.model.User;

@Repository
public interface AccLockRepository  extends JpaRepository<AccountLock , Long>{

    Optional<AccountLock> findByUser(User user);

}
