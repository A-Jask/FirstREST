package com.udacity.pricing.domain.price;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

@Repository
public interface PriceRepository extends CrudRepository<Price, Long> {

}
