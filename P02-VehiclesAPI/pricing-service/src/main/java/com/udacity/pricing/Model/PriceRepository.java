package com.udacity.pricing.Model;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

@Repository
public interface PriceRepository extends CrudRepository<Price, Long> {

}
