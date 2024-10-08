package com.example.shoppingbasket.services

import com.example.shoppingbasket.models.BasketItem
import com.example.shoppingbasket.models.BasketUpdateRequest
import com.example.shoppingbasket.models.Register
import com.example.shoppingbasket.models.Product
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class BasketServiceTest {
    private val sut = BasketService()

    private val productMilk = Product(11, "milk", BigDecimal.valueOf(1.4))
    private val productEgg = Product(22, "eggs", BigDecimal.valueOf(2.3))

    @Test
    fun addOneItem() {
        val actual = sut.addItemToBasket(BasketUpdateRequest(1, productMilk, 2))

        assertEquals(sut.basketMap.toString(), mutableMapOf(1 to mutableMapOf(11 to 2)).toString())
        assertEquals(actual, listOf(Register(11, 2)))
    }

    @Test
    fun addTheSameItemMultipleTimes() {
        sut.addItemToBasket(BasketUpdateRequest(1, productMilk, 2))
        val actual = sut.addItemToBasket(BasketUpdateRequest(1, productMilk, 3))

        assertEquals(sut.basketMap.toString(), mutableMapOf(1 to mutableMapOf(11 to 5)).toString())
        assertEquals(actual, listOf(Register(11, 5)))
    }

    @Test
    fun addMultipleItems() {
        sut.addItemToBasket(BasketUpdateRequest(1, productMilk, 3))
        val actual = sut.addItemToBasket(BasketUpdateRequest(1, productEgg, 1))

        assertEquals(sut.basketMap.toString(), mutableMapOf(1 to mutableMapOf(11 to 3, 22 to 1)).toString())
        assertEquals(actual, listOf(Register(11, 3), Register(22, 1)))
    }

    @Test
    fun addToMultipleSessions() {
        sut.addItemToBasket(BasketUpdateRequest(1, productMilk, 3))
        val actual1 = sut.addItemToBasket(BasketUpdateRequest(1, productMilk, 1))
        sut.addItemToBasket(BasketUpdateRequest(2, productEgg, 1))
        val actual2 = sut.addItemToBasket(BasketUpdateRequest(2, productMilk, 1))

        assertEquals(
            sut.basketMap.toString(), mutableMapOf(
                1 to mutableMapOf(11 to 4),
                2 to mutableMapOf(22 to 1, 11 to 1)
            ).toString()
        )
        assertEquals(actual1, listOf(Register(11, 4)))
        assertEquals(actual2, listOf(Register(22, 1), Register(11, 1)))
    }

    @Test
    fun removeOneItemUpdatesCount() {
        sut.addItemToBasket(BasketUpdateRequest(1, productMilk, 3))
        val actual = sut.removeItemFromBasket(BasketUpdateRequest(1, productMilk, 1))

        assertEquals(sut.basketMap.toString(), mutableMapOf(1 to mutableMapOf(11 to 2)).toString())
        assertEquals(actual, listOf(Register(11, 2)))
    }

    @Test
    fun removeOneItemEntirely() {
        sut.addItemToBasket(BasketUpdateRequest(1, productMilk, 3))
        sut.addItemToBasket(BasketUpdateRequest(1, productEgg, 1))
        val actual = sut.removeItemFromBasket(BasketUpdateRequest(1, productMilk, 3))

        assertEquals(sut.basketMap.toString(), mutableMapOf(1 to mutableMapOf(22 to 1)).toString())
        assertEquals(actual, listOf(Register(22, 1)))
    }

    @Test
    fun removeLastItemRemovesBasket() {
        sut.addItemToBasket(BasketUpdateRequest(1, productMilk, 3))
        val actual = sut.removeItemFromBasket(BasketUpdateRequest(1, productMilk, 3))

        assertTrue(sut.basketMap.isEmpty())
        assertEquals(actual, emptyList<Register>())
    }

    @Test
    fun removeItemNotInBasket() {
        sut.addItemToBasket(BasketUpdateRequest(1, productMilk, 3))
        val actual = sut.removeItemFromBasket(BasketUpdateRequest(1, productEgg, 3))

        assertEquals(sut.basketMap.toString(), mutableMapOf(1 to mutableMapOf(11 to 3)).toString())
        assertEquals(actual, listOf(Register(11, 3)))
    }

    @Test
    fun removeItemFromNonExistentBasket() {
        sut.addItemToBasket(BasketUpdateRequest(1, productMilk, 3))
        val actual = sut.removeItemFromBasket(BasketUpdateRequest(2, productEgg, 3))

        assertEquals(sut.basketMap.toString(), mutableMapOf(1 to mutableMapOf(11 to 3)).toString())
        assertEquals(actual, emptyList<Register>())
    }

    @Test
    fun getDiscountedCount() {
        assertEquals(sut.getDiscountedCount(4), 2)
        assertEquals(sut.getDiscountedCount(3), 2)
        assertEquals(sut.getDiscountedCount(1), 1)
        assertEquals(sut.getDiscountedCount(0), 0)
    }

    @Test
    fun checkoutExistingBasket() {
        sut.addItemToBasket(BasketUpdateRequest(1, productMilk, 5))
        sut.addItemToBasket(BasketUpdateRequest(1, productEgg, 2))

        val actual = sut.calculateCheckout(1)

        assertNotNull(actual)
        assertEquals(listOf(BasketItem(productMilk, 5), BasketItem(productEgg, 2)), actual!!.allItemsInBasket)
        assertEquals(listOf(BasketItem(productMilk, 3), BasketItem(productEgg, 1)), actual.itemsToPayFor)
        assertEquals(BigDecimal.valueOf(6.5), actual.finalPriceIncludingDiscount)
    }

    @Test
    fun checkoutNoneExistentBasket() {
        val actual = sut.calculateCheckout(1)
        assertNull(actual)

    }


}

