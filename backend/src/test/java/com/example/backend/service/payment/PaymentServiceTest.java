package com.example.backend.service.payment;

import com.example.backend.config.payment.PaymentProperties;
import com.example.backend.entities.Customer;
import com.example.backend.entities.Order;
import com.example.backend.mapper.IPaymentMapper;
import com.example.backend.repository.IPaymentRepository;
import com.example.backend.service.order.share.IOrderSupportService;
import com.example.backend.service.payment.support.IPaymentCodeGenerator;
import com.example.backend.share.exception.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private IPaymentRepository paymentRepository;

    @Mock
    private IOrderSupportService orderSupportService;

    @Mock
    private IPaymentCodeGenerator paymentCodeGenerator;

    @Mock
    private PaymentProperties paymentProperties;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private IPaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void getCurrentPaymentRejectsOrderFromAnotherCustomer() {
        Customer owner = new Customer();
        owner.setId(1L);

        Order order = new Order();
        order.setCustomer(owner);

        when(orderSupportService.findOrderWithItems(99L)).thenReturn(order);

        assertThatThrownBy(() -> paymentService.getCurrentPayment(99L, 2L))
                .isInstanceOf(AppException.class)
                .hasMessage("You do not have permission to access this order");

        verifyNoInteractions(paymentRepository);
    }
}
