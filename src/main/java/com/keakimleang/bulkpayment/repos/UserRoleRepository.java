package com.keakimleang.bulkpayment.repos;

import com.keakimleang.bulkpayment.securities.User;
import com.keakimleang.bulkpayment.securities.UserRole;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Long> {

}
