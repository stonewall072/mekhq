/*
 * Copyright (C) 2025 The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL),
 * version 3 or (at your option) any later version,
 * as published by the Free Software Foundation.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * A copy of the GPL should have been included with this project;
 * if not, see <https://www.gnu.org/licenses/>.
 *
 * NOTICE: The MegaMek organization is a non-profit group of volunteers
 * creating free software for the BattleTech community.
 *
 * MechWarrior, BattleMech, `Mech and AeroTech are registered trademarks
 * of The Topps Company, Inc. All Rights Reserved.
 *
 * Catalyst Game Labs and the Catalyst Game Labs logo are trademarks of
 * InMediaRes Productions, LLC.
 */

package mekhq.gui.dialog.nagDialogs.nagLogic;

import static mekhq.gui.dialog.nagDialogs.nagLogic.UnableToAffordShoppingListNag.unableToAffordShoppingList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mekhq.campaign.finances.Money;
import org.junit.jupiter.api.Test;

public class UnableToAffordShoppingListNagLogicTest {

    @Test
    void canAfford() {
        Money currentFunds = Money.of(10);
        Money totalBuyCost = Money.of(5);
        assertFalse(unableToAffordShoppingList(totalBuyCost, currentFunds));
    }

    @Test
    void canNotAfford() {
        Money currentFunds = Money.of(5);
        Money totalBuyCost = Money.of(10);
        assertTrue(unableToAffordShoppingList(totalBuyCost, currentFunds));
    }

    @Test
    void bothZero() {
        Money currentFunds = Money.zero();
        Money totalBuyCost = Money.zero();
        assertFalse(unableToAffordShoppingList(totalBuyCost, currentFunds));
    }

    @Test
    void bothSame() {
        Money currentFunds = Money.of(10);
        Money totalBuyCost = Money.of(10);
        assertFalse(unableToAffordShoppingList(totalBuyCost, currentFunds));
    }
}
