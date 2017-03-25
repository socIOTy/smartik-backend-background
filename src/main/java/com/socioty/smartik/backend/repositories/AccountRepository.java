package com.socioty.smartik.backend.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.socioty.smartik.backend.model.Account;

public interface AccountRepository extends MongoRepository<Account, String> {

	public Account findByEmail(String email);
}
