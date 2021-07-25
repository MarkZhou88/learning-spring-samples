package learning.spring.binarytea.service;

import learning.spring.binarytea.BinaryTeaProperties;
import learning.spring.binarytea.model.Amount;
import learning.spring.binarytea.model.MenuItem;
import learning.spring.binarytea.model.Order;
import learning.spring.binarytea.model.OrderStatus;
import learning.spring.binarytea.repository.OrderRepository;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.transaction.Transactional;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private BinaryTeaProperties binaryTeaProperties;

    @Secured({ "ROLE_MANAGER", "ROLE_TEA_MAKER", "ROLE_USER" })
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @RolesAllowed({ "MANAGER", "TEA_MAKER" })
    public Order createOrder(List<MenuItem> itemList, int discount) {
        int newDiscount = discount == 100 ? binaryTeaProperties.getDiscount() : discount;
        Money total = itemList.stream().map(i -> i.getPrice())
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        l -> Money.total(l)));
        Money pay = total.multipliedBy(newDiscount / 100d, RoundingMode.HALF_DOWN);

        Amount amount = Amount.builder()
                .discount(newDiscount)
                .totalAmount(total)
                .payAmount(pay)
                .build();
        Order order = Order.builder()
                .amount(amount)
                .status(OrderStatus.ORDERED)
                .items(itemList)
                .build();
        return orderRepository.save(order);
    }
}
