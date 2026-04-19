package com.pismochallenge.api.config;

import com.pismochallenge.api.entity.OperationType;
import com.pismochallenge.api.repository.OperationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final OperationTypeRepository operationTypeRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (operationTypeRepository.count() == 0) {
            log.info("Inicializando tipos de operação...");
            operationTypeRepository.saveAll(List.of(
                new OperationType(1, "PURCHASE"),
                new OperationType(2, "INSTALLMENT PURCHASE"),
                new OperationType(3, "WITHDRAWAL"),
                new OperationType(4, "PAYMENT")
            ));
            log.info("Tipos de operação inicializados com sucesso.");
        }
    }
}
