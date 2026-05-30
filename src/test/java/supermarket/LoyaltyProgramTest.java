package supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import supermarket.loyalty.LoyaltyProgram;

class LoyaltyProgramTest {

    private final LoyaltyProgram program = new LoyaltyProgram();

    @Test
    void earnsOnePointPerWholeEuro() {
        assertEquals(30, program.pointsFor(30.74));
        assertEquals(40, program.pointsFor(40.00));
    }

    @Test
    void earnsNoPointsBelowOneEuro() {
        assertEquals(0, program.pointsFor(0.99));
    }
}
