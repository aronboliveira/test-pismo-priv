package com.pismochallenge.api.controller;

import com.pismochallenge.api.dto.response.TransactionResponse;
import com.pismochallenge.api.exception.ResourceNotFoundException;
import com.pismochallenge.api.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void createTransaction_shouldReturn201() throws Exception {
        when(transactionService.createTransaction(any()))
            .thenReturn(new TransactionResponse(1L, 1L, 4, new BigDecimal("123.45")));

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"account_id\": 1, \"operation_type_id\": 4, \"amount\": 123.45}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.transaction_id").value(1))
            .andExpect(jsonPath("$.account_id").value(1))
            .andExpect(jsonPath("$.operation_type_id").value(4))
            .andExpect(jsonPath("$.amount").value(123.45));
    }

    @Test
    void createTransaction_shouldReturn400_whenAmountMissing() throws Exception {
        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"account_id\": 1, \"operation_type_id\": 4}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_shouldReturn400_whenAmountNegative() throws Exception {
        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"account_id\": 1, \"operation_type_id\": 4, \"amount\": -50.0}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_shouldReturn404_whenAccountNotFound() throws Exception {
        when(transactionService.createTransaction(any()))
            .thenThrow(new ResourceNotFoundException("Account not found with id: 99"));

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"account_id\": 99, \"operation_type_id\": 1, \"amount\": 50.0}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Account not found with id: 99"));
    }
}
