/*
 * Copyright (C) 2021-2025 The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.universe.generators.battleMekQualityGenerators;

import mekhq.campaign.rating.IUnitRating;
import mekhq.campaign.universe.enums.BattleMekQualityGenerationMethod;

/**
 * @author Justin "Windchild" Bowen
 */
public class AtBBattleMekQualityGenerator extends AbstractBattleMekQualityGenerator {
    //region Constructors
    public AtBBattleMekQualityGenerator() {
        super(BattleMekQualityGenerationMethod.AGAINST_THE_BOT);
    }
    //endregion Constructors

    @Override
    public int generate(final int roll) {
        switch (roll) {
            case 2:
            case 3:
            case 4:
            case 5:
                return IUnitRating.DRAGOON_F;
            case 6:
            case 7:
            case 8:
                return IUnitRating.DRAGOON_D;
            case 9:
            case 10:
                return IUnitRating.DRAGOON_C;
            case 11:
                return IUnitRating.DRAGOON_B;
            case 12:
                return IUnitRating.DRAGOON_A;
            default:
                return IUnitRating.DRAGOON_ASTAR;
        }
    }
}
