package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import supermarket.model.Customer;
import supermarket.model.Receipt;
import supermarket.payment.PaymentResult;
import supermarket.register.CashRegister;
import supermarket.register.Supermarket;

class BillExtrasTest {

    @Test
    void billCombinesCouponAndVat() {
        Supermarket market = new Supermarket();
        market.addItem("rice", "grocery", 2.00, 1.00, 100);
        market.setCategoryTax("grocery", 10); // 10% VAT
        market.addCoupon(10);                  // 10% store coupon
        Customer eve = market.registerCustomer("Eve", "Stone", "eve", "addr", "pwd");

        CashRegister register = market.getCashRegister();
        register.startCheckout(eve);
        register.scanItem(market.getItem("rice"), 10); // raw €20.00

        Receipt bill = register.computeBill();
        assertEquals(20.00, bill.getSubtotalBeforeDiscount(), 1e-9);
        assertEquals(2.00, bill.getPromotionDiscount(), 1e-9);   // 10% of €20
        assertEquals(18.00, bill.getSubtotalAfterDiscount(), 1e-9); // after coupon, normal plan
        assertEquals(2.00, bill.getTax(), 1e-9);                 // 10% VAT on €20 net
        assertEquals(20.00, bill.getTotal(), 1e-9);              // 18 + 2 VAT
    }

    @Test
    void buyOneGetOneFreeReducesTheBill() {
        Supermarket market = new Supermarket();
        market.addItem("soda", "grocery", 3.00, 1.00, 100);
        market.addBogoPromotion("soda", 1, 1); // buy 1 get 1 free
        Customer eve = market.registerCustomer("Eve", "Stone", "eve", "addr", "pwd");

        CashRegister register = market.getCashRegister();
        register.startCheckout(eve);
        register.scanItem(market.getItem("soda"), 4); // 4 units -> 2 free

        Receipt bill = register.computeBill();
        assertEquals(6.00, bill.getPromotionDiscount(), 1e-9);   // 2 free x €3
        assertEquals(6.00, bill.getSubtotalAfterDiscount(), 1e-9); // €12 - €6
        assertEquals(6.00, bill.getTotal(), 1e-9);
    }

    @Test
    void loyaltyPointsAreEarnedOnSuccessfulPayment() {
        Supermarket market = new Supermarket();
        market.setup(); // registers the 4242 test card
        market.addItem("rice", "grocery", 2.00, 1.00, 100);
        Customer eve = market.registerCustomer("Eve", "Stone", "eve", "addr", "pwd");

        CashRegister register = market.getCashRegister();
        register.startCheckout(eve);
        register.scanItem(market.getItem("rice"), 10); // €20.00

        assertEquals(PaymentResult.SUCCESS, register.pay("4242424242424242", "1234"));
        assertEquals(20, register.getLastPointsEarned());
        assertEquals(20, eve.getLoyaltyPoints());
    }
}
