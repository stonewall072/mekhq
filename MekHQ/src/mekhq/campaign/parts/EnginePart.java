/*
 * Copyright (c) 2009 Jay Lawson (jaylawson39 at yahoo.com). All rights reserved.
 * Copyright (C) 2013-2025 The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.parts;

import java.io.PrintWriter;

import megamek.common.*;
import megamek.common.annotations.Nullable;
import megamek.common.verifier.TestEntity;
import megamek.logging.MMLogger;
import mekhq.campaign.Campaign;
import mekhq.campaign.finances.Money;
import mekhq.campaign.parts.enums.PartRepairType;
import mekhq.campaign.personnel.skills.SkillType;
import mekhq.campaign.unit.Unit;
import mekhq.utilities.MHQXMLUtility;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Jay Lawson (jaylawson39 at yahoo.com)
 */
public class EnginePart extends Part {
    private static final MMLogger logger = MMLogger.create(EnginePart.class);

    protected Engine engine;
    protected boolean forHover;

    public EnginePart() {
        this(0, new Engine(0, 0, -1), null, false);
    }

    public EnginePart(int tonnage, Engine e, Campaign c, boolean hover) {
        super(tonnage, c);
        this.engine = e;
        this.forHover = hover;
        this.name = engine.getEngineName() + " Engine";
        this.unitTonnageMatters = true;
    }

    @Override
    public EnginePart clone() {
        EnginePart clone = new EnginePart(getUnitTonnage(),
                new Engine(engine.getRating(), engine.getEngineType(), engine.getFlags()), campaign, forHover);
        clone.copyBaseData(this);
        return clone;
    }

    public Engine getEngine() {
        return engine;
    }

    @Override
    public double getTonnage() {
        double weight = Engine.ENGINE_RATINGS[(int) Math.ceil(engine.getRating() / 5.0)];
        switch (engine.getEngineType()) {
            case Engine.COMBUSTION_ENGINE:
                weight *= 2.0f;
                break;
            case Engine.NORMAL_ENGINE:
                break;
            case Engine.XL_ENGINE:
                weight *= 0.5f;
                break;
            case Engine.LIGHT_ENGINE:
                weight *= 0.75f;
                break;
            case Engine.XXL_ENGINE:
                weight /= 3f;
                break;
            case Engine.COMPACT_ENGINE:
                weight *= 1.5f;
                break;
            case Engine.FISSION:
                weight *= 1.75;
                weight = Math.max(5, weight);
                break;
            case Engine.FUEL_CELL:
                weight *= 1.2;
                break;
            case Engine.NONE:
                return 0;
        }
        weight = TestEntity.ceilMaxHalf(weight, TestEntity.Ceil.HALFTON);

        if (engine.hasFlag(Engine.TANK_ENGINE) && engine.isFusion()) {
            weight *= 1.5;
        }
        double toReturn = TestEntity.ceilMaxHalf(weight, TestEntity.Ceil.HALFTON);
        // hover have a minimum weight of 20%
        if (forHover) {
            return Math.max(TestEntity.ceilMaxHalf(getUnitTonnage() / 5.0, TestEntity.Ceil.HALFTON), toReturn);
        }
        return toReturn;
    }

    @Override
    public Money getStickerPrice() {
        return Money.of((double) getEngine().getBaseCost() / 75.0 * getEngine().getRating() * getUnitTonnage());
    }

    @Override
    public void setUnit(Unit u) {
        super.setUnit(u);
        if ((null != u) && u.getEntity().hasETypeFlag(Entity.ETYPE_TANK)) {
            fixTankFlag(u.getEntity().getMovementMode() == EntityMovementMode.HOVER);
        }
    }

    public void fixTankFlag(boolean hover) {
        int flags = engine.getFlags();
        if (!engine.hasFlag(Engine.TANK_ENGINE)) {
            flags |= Engine.TANK_ENGINE;
        }
        engine = new Engine(engine.getRating(), engine.getEngineType(), flags);
        this.name = engine.getEngineName() + " Engine";
        this.forHover = hover;
    }

    public void fixClanFlag() {
        int flags = engine.getFlags();
        if (!engine.hasFlag(Engine.CLAN_ENGINE)) {
            flags |= Engine.CLAN_ENGINE;
        }
        engine = new Engine(engine.getRating(), engine.getEngineType(), flags);
        this.name = engine.getEngineName() + " Engine";
    }

    @Override
    public boolean isSamePartType(Part part) {
        int year = campaign.getGameYear();
        return part instanceof EnginePart && getName().equals(part.getName())
                && getEngine().getEngineType() == ((EnginePart) part).getEngine().getEngineType()
                && getEngine().getRating() == ((EnginePart) part).getEngine().getRating()
                && getEngine().getTechType(year) == ((EnginePart) part).getEngine().getTechType(year)
                && getEngine().hasFlag(Engine.TANK_ENGINE) == ((EnginePart) part).getEngine()
                        .hasFlag(Engine.TANK_ENGINE)
                && getUnitTonnage() == part.getUnitTonnage()
                && getTonnage() == part.getTonnage();
    }

    @Override
    public void writeToXML(final PrintWriter pw, int indent) {
        indent = writeToXMLBegin(pw, indent);
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "engineType", engine.getEngineType());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "engineRating", engine.getRating());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "engineFlags", engine.getFlags());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "forHover", forHover);
        writeToXMLEnd(pw, indent);
    }

    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        NodeList nl = wn.getChildNodes();
        int engineType = -1;
        int engineRating = -1;
        int engineFlags = 0;

        for (int x = 0; x < nl.getLength(); x++) {
            Node wn2 = nl.item(x);

            try {
                if (wn2.getNodeName().equalsIgnoreCase("engineType")) {
                    engineType = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("engineRating")) {
                    engineRating = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("engineFlags")) {
                    engineFlags = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("forHover")) {
                    forHover = wn2.getTextContent().equalsIgnoreCase("true");
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }

        engine = new Engine(engineRating, engineType, engineFlags);
    }

    @Override
    public void fix() {
        super.fix();
        if (null != unit) {
            if (unit.getEntity() instanceof Mek) {
                unit.repairSystem(CriticalSlot.TYPE_SYSTEM, Mek.SYSTEM_ENGINE);
            }
            if (unit.getEntity() instanceof Aero) {
                ((Aero) unit.getEntity()).setEngineHits(0);
            }
            if (unit.getEntity() instanceof Tank) {
                ((Tank) unit.getEntity()).engineFix();
            }
            if (unit.getEntity() instanceof ProtoMek) {
                ((ProtoMek) unit.getEntity()).setEngineHit(false);
            }
        }
    }

    @Override
    public MissingPart getMissingPart() {
        return new MissingEnginePart(getUnitTonnage(),
                new Engine(engine.getRating(), engine.getEngineType(), engine.getFlags()), campaign, forHover);
    }

    @Override
    public void remove(boolean salvage) {
        if (null != unit) {
            if (unit.getEntity() instanceof Mek) {
                unit.destroySystem(CriticalSlot.TYPE_SYSTEM, Mek.SYSTEM_ENGINE);
            }
            if (unit.getEntity() instanceof Aero) {
                ((Aero) unit.getEntity()).setEngineHits(((Aero) unit.getEntity()).getMaxEngineHits());
            }
            if (unit.getEntity() instanceof Tank) {
                ((Tank) unit.getEntity()).engineHit();
            }
            if (unit.getEntity() instanceof ProtoMek) {
                ((ProtoMek) unit.getEntity()).setEngineHit(true);
            }
            Part spare = campaign.getWarehouse().checkForExistingSparePart(this);
            if (!salvage) {
                campaign.getWarehouse().removePart(this);
            } else if (null != spare) {
                spare.incrementQuantity();
                campaign.getWarehouse().removePart(this);
            }
            unit.removePart(this);
            Part missing = getMissingPart();
            unit.addPart(missing);
            campaign.getQuartermaster().addPart(missing, 0);
        }
        setUnit(null);
    }

    @Override
    public void updateConditionFromEntity(boolean checkForDestruction) {
        if (null != unit) {
            int engineHits = 0;
            int engineCrits = 0;
            Entity entity = unit.getEntity();
            if (unit.getEntity() instanceof Mek) {
                for (int i = 0; i < entity.locations(); i++) {
                    engineHits += entity.getDamagedCriticals(CriticalSlot.TYPE_SYSTEM, Mek.SYSTEM_ENGINE, i);
                    engineCrits += entity.getNumberOfCriticals(CriticalSlot.TYPE_SYSTEM, Mek.SYSTEM_ENGINE, i);
                }
            }
            if (unit.getEntity() instanceof Aero) {
                engineHits = unit.getEntity().getEngineHits();
                engineCrits = 3;
            }
            if (unit.getEntity() instanceof Tank) {
                engineCrits = 2;
                if (((Tank) unit.getEntity()).isEngineHit()) {
                    engineHits = 1;
                }
            }
            if (unit.getEntity() instanceof ProtoMek) {
                engineCrits = 1;
                if (unit.getEntity().getInternal(ProtoMek.LOC_TORSO) == IArmorState.ARMOR_DESTROYED) {
                    engineHits = 1;
                } else {
                    engineHits = unit.getEntity().getEngineHits();
                }
            }
            if (engineHits >= engineCrits) {
                remove(false);
            } else if (engineHits > 0) {
                hits = engineHits;
            } else {
                hits = 0;
            }
        }
    }

    @Override
    public int getBaseTime() {
        // TODO: keep an aero flag here, so we dont need the unit
        if (null != unit && unit.getEntity() instanceof Aero && hits > 0) {
            return 300;
        }
        if (isSalvaging()) {
            return 360;
        }
        if (hits == 1) {
            return 100;
        } else if (hits == 2) {
            return 200;
        } else if (hits > 2) {
            return 300;
        }
        return 0;
    }

    @Override
    public int getDifficulty() {
        // TODO: keep an aero flag here, so we dont need the unit
        if (null != unit && unit.getEntity() instanceof Aero && hits > 0) {
            return 1;
        }
        if (isSalvaging()) {
            return -1;
        }
        if (hits == 1) {
            return -1;
        } else if (hits == 2) {
            return 0;
        } else if (hits > 2) {
            return 2;
        }
        return 0;
    }

    @Override
    public boolean needsFixing() {
        return hits > 0;
    }

    @Override
    public void updateConditionFromPart() {
        if (null != unit) {
            if (hits == 0) {
                if (unit.getEntity() instanceof Mek) {
                    unit.repairSystem(CriticalSlot.TYPE_SYSTEM, Mek.SYSTEM_ENGINE);
                }
                if (unit.getEntity() instanceof Aero) {
                    ((Aero) unit.getEntity()).setEngineHits(0);
                }
                if (unit.getEntity() instanceof Tank) {
                    ((Tank) unit.getEntity()).engineFix();
                }
                if (unit.getEntity() instanceof ProtoMek) {
                    ((ProtoMek) unit.getEntity()).setEngineHit(false);
                }
            } else {
                if (unit.getEntity() instanceof Mek) {
                    unit.damageSystem(CriticalSlot.TYPE_SYSTEM, Mek.SYSTEM_ENGINE, hits);
                }
                if (unit.getEntity() instanceof Aero) {
                    ((Aero) unit.getEntity()).setEngineHits(hits);
                }
                if (unit.getEntity() instanceof Tank) {
                    ((Tank) unit.getEntity()).engineHit();
                }
                if (unit.getEntity() instanceof ProtoMek) {
                    ((ProtoMek) unit.getEntity()).setEngineHit(true);
                }
            }
        }
    }

    @Override
    public @Nullable String checkFixable() {
        if (null == unit) {
            return null;
        }
        if (isSalvaging()) {
            return null;
        }
        for (int i = 0; i < unit.getEntity().locations(); i++) {
            if (unit.isLocationBreached(i)) {
                return unit.getEntity().getLocationName(i) + " is breached.";
            }
            if (unit.getEntity().getNumberOfCriticals(CriticalSlot.TYPE_SYSTEM, Mek.SYSTEM_ENGINE, i) > 0
                    && unit.isLocationDestroyed(i)) {
                return unit.getEntity().getLocationName(i) + " is destroyed.";
            }
        }
        return null;
    }

    @Override
    public boolean isMountedOnDestroyedLocation() {
        if (null == unit) {
            return false;
        }
        for (int i = 0; i < unit.getEntity().locations(); i++) {
            if (unit.getEntity().getNumberOfCriticals(CriticalSlot.TYPE_SYSTEM, Mek.SYSTEM_ENGINE, i) > 0
                    && unit.isLocationDestroyed(i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDetails() {
        return getDetails(true);
    }

    @Override
    public String getDetails(boolean includeRepairDetails) {
        String details = super.getDetails(includeRepairDetails);

        return details + (forHover ? " (hover)" : "");
    }

    @Override
    public boolean isPartForEquipmentNum(int index, int loc) {
        return false;
    }

    @Override
    public boolean isRightTechType(String skillType) {
        if (getEngine().hasFlag(Engine.TANK_ENGINE)) {
            return skillType.equals(SkillType.S_TECH_MECHANIC);
        } else {
            return skillType.equals(SkillType.S_TECH_MEK) || skillType.equals(SkillType.S_TECH_AERO);
        }
    }

    @Override
    public String getLocationName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLocation() {
        return Entity.LOC_NONE;
    }

    @Override
    public TechAdvancement getTechAdvancement() {
        return engine.getTechAdvancement();
    }

    @Override
    public boolean isInLocation(String loc) {
        if (null == unit || null == unit.getEntity()) {
            return false;
        }
        if (unit.getEntity().getLocationFromAbbr(loc) == Mek.LOC_CT) {
            return true;
        }
        boolean needsSideTorso = false;
        switch (getEngine().getEngineType()) {
            case Engine.XL_ENGINE:
            case Engine.LIGHT_ENGINE:
            case Engine.XXL_ENGINE:
                needsSideTorso = true;
                break;
        }
        if (needsSideTorso && (unit.getEntity().getLocationFromAbbr(loc) == Mek.LOC_LT
                || unit.getEntity().getLocationFromAbbr(loc) == Mek.LOC_RT)) {
            return true;
        }
        return false;
    }

    @Override
    public PartRepairType getMRMSOptionType() {
        return PartRepairType.ENGINE;
    }
}
