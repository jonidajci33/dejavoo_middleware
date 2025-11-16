package com.dejavoo.middleware.service;

import com.dejavoo.middleware.entity.Merchant;
import com.dejavoo.middleware.repository.MerchantRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MerchantService {

    final private MerchantRepository merchantRepository;

    public Merchant findMerchantById(Long merchantId){
        return merchantRepository.findById(merchantId).orElse(null);
    }
}
