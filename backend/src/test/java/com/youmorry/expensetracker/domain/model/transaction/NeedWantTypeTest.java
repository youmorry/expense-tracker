package com.youmorry.expensetracker.domain.model.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NeedWantTypeTest {

  @Test
  void values_returnsAllThreeTypes() {
    assertEquals(3, NeedWantType.values().length);
  }

  @Test
  void valueOf_withNeed_returnsNeed() {
    assertEquals(NeedWantType.NEED, NeedWantType.valueOf("NEED"));
  }

  @Test
  void valueOf_withWant_returnsWant() {
    assertEquals(NeedWantType.WANT, NeedWantType.valueOf("WANT"));
  }

  @Test
  void valueOf_withUnset_returnsUnset() {
    assertEquals(NeedWantType.UNSET, NeedWantType.valueOf("UNSET"));
  }
}
