/**
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

package mekhq.campaign.unit;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

import megamek.common.Transporter;
import megamek.common.annotations.Nullable;
import megamek.logging.MMLogger;
import mekhq.Utilities;
import mekhq.campaign.Campaign;
import mekhq.campaign.enums.CampaignTransportType;
import mekhq.campaign.unit.enums.TransporterType;
import mekhq.campaign.utilities.CampaignTransportUtilities;

/**
 * Tracks what units this transport is transporting, and its current capacity for its different transporter types.
 * @see AbstractTransportedUnitsSummary
 * @see CampaignTransportType#SHIP_TRANSPORT
 *
 */
public class ShipTransportedUnitsSummary extends AbstractTransportedUnitsSummary {
    private static final MMLogger logger = MMLogger.create(ShipTransportedUnitsSummary.class);
    /**
     * Initialize the transport details for a transport ship
     * @param transport unit this summary is about
     */
    public ShipTransportedUnitsSummary(Unit transport) {
        super(transport, CampaignTransportType.SHIP_TRANSPORT);
    }

    /**
     * Main method to be used for unloading units from a transport
     *
     * @param transportedUnits Units we wish to unload
     */
    @Override
    public void unloadTransport(Set<Unit>  transportedUnits) {
        super.unloadTransport(transportedUnits);

    }

    @Override
    protected void unloadTransport(Unit transportedUnit) {
        super.unloadTransport(transportedUnit);

        // And if the unit is being transported by us,
        // then update its transport ship assignment (provided the
        // assignment is actually to us!).
        if (transportedUnit.hasTransportShipAssignment()
            && transportedUnit.getTransportShipAssignment().getTransportShip().equals(transport)) {
            transportedUnit.setTransportShipAssignment(null);

        }
    }


    /**
     * Bay loading utility used when assigning units to bay-equipped transport units
     * For each passed-in unit, this will find the first available, transport bay
     * and set
     * both the target bay and the transport ship. Once in the MM lobby,
     * this data
     * will be used to actually load the unit into a bay on the transport.
     *
     * @param transportedUnits units being loaded
     * @param transporterType type (Enum) of bay or Transporter
     * @return old transports; what were  the units' previous transport, if they had one?
     */
    public Set<Unit> loadTransportShip(Vector<Unit> transportedUnits, TransporterType transporterType) {
        Set<Unit> oldTransports = new HashSet<>();
        for (Unit transportedUnit : transportedUnits) {
            Unit oldTransport = loadTransport(transporterType, transportedUnit);
            if (oldTransport != null) {
                oldTransports.add(oldTransport);
            }
        }

        //Let's get matching transporters and then recalculate our transport capacity for this transporter type
        Vector<Transporter> transporters = new Vector<>(transport.getEntity().getTransports().stream()
            .filter(transporter -> TransporterType.getTransporterType(transporter) == transporterType).toList());
        recalculateTransportCapacity(transporters);

        return oldTransports;
    }

    private @Nullable Unit loadTransport(TransporterType transporterType, Unit transportedUnit) {
        Unit oldTransport = null;
        int bayNumber = Utilities.selectBestBayFor(transportedUnit.getEntity(), transport.getEntity());

        TransporterType oldTransporterType = null;
        if(transportedUnit.hasTransportShipAssignment()) {
            oldTransport = transportedUnit.getTransportShipAssignment().getTransportShip();
            oldTransporterType = transportedUnit.getTransportShipAssignment().getTransporterType();
            if (oldTransport.getEntity() != null) {
                oldTransport.unloadFromTransportShip(transportedUnit);
            }
        }

        transportedUnit.setTransportShipAssignment(new TransportShipAssignment(transport, bayNumber));

        if ((transportedUnit.getEntity() != null)) {
            // This shouldn't happen, but it'd be really annoying to debug if it did
            if ((transportedUnit.getEntity().getBayById(bayNumber) != null && TransporterType.getTransporterType(transportedUnit.getEntity().getBayById(bayNumber)) != transporterType)) {
                logger.warn(String.format("Unit was assigned a bay number for a different transport type than the unit is assigned! " +
                    "Transport: %s Unit: %s Assigned Transporter: %s Assigned Bay ID: %s",
                    transport.getName(), transportedUnit.getName(), transporterType, bayNumber));
            }
        }

        addTransportedUnit(transportedUnit);
        if (!Objects.equals(oldTransport, transport)
            && (transportedUnit.getTransportShipAssignment().getTransporterType() != oldTransporterType)) {
            setCurrentTransportCapacity(transporterType,
                getCurrentTransportCapacity(transporterType) - CampaignTransportUtilities.transportCapacityUsage(transporterType, transportedUnit.getEntity()));
        }
        return oldTransport;
    }

    /**
     * Bay unloading utility used when removing units from bay-equipped transport
     * units
     * and/or moving them to a new transport
     *
     * @param transportedUnit The unit that we wish to unload from this transport
     */
    public void unloadFromTransportShip(Unit transportedUnit) {
        unloadTransport(transportedUnit);
    }

    /**
     * Bay unloading utility used when removing a bay-equipped Transport unit
     * This removes all units assigned to the transport from it
     */
    @Override
    public void clearTransportedUnits(Campaign campaign) {
        clearTransportedUnits();

        // And now reset the Transported values for all the units we just booted
        campaign.getHangar().forEachUnit(u -> {
            if (u.hasTransportShipAssignment()
                && Objects.equals(transport, u.getTransportShipAssignment().getTransportShip())) {
                u.setTransportShipAssignment(null);
            }
        });

        recalculateTransportCapacity(transport.getEntity().getTransports());
    }

    /**
     * Fixes references after loading
     *
     * @param campaign
     * @param unit
     */
    @Override
    public void fixReferences(Campaign campaign, Unit unit) {
        Set<Unit> newTransportedUnits = new HashSet<>();
        for (Unit transportedUnit : getTransportedUnits()) {
            if (transportedUnit instanceof Unit.UnitRef) {
                Unit realUnit = campaign.getHangar().getUnit(transportedUnit.getId());
                if (realUnit != null) {
                    newTransportedUnits.add(realUnit);
                } else {
                    logger.error(
                        String.format("Unit %s ('%s') references missing transported unit %s",
                            unit.getId(), unit.getName(), transportedUnit.getId()));
                }
            } else {
                newTransportedUnits.add(transportedUnit);
            }
        }
        replaceTransportedUnits(newTransportedUnits);
    }
}
