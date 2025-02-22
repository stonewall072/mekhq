/*
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.gui.model;

import megamek.common.annotations.Nullable;
import mekhq.campaign.parts.Part;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;

/**
 * A table model for displaying parts
 */
public class PartsTableModel extends DataTableModel {
    public final static int COL_QUANTITY = 0;
    public final static int COL_NAME = 1;
    public final static int COL_DETAIL = 2;
    public final static int COL_TECH_BASE = 3;
    public final static int COL_QUALITY = 4;
    public final static int COL_STATUS = 5;
    public final static int COL_REPAIR = 6;
    public final static int COL_COST = 7;
    public final static int COL_TOTAL_COST = 8;
    public final static int COL_TON = 9;
    public final static int N_COL = 10;

    public PartsTableModel() {
        data = new ArrayList<Part>();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return N_COL;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case COL_NAME:
                return "Name";
            case COL_COST:
                return "Value per Unit";
            case COL_TOTAL_COST:
                return "Total Value";
            case COL_QUANTITY:
                return "#";
            case COL_QUALITY:
                return "Quality";
            case COL_TON:
                return "Tonnage";
            case COL_STATUS:
                return "Status";
            case COL_DETAIL:
                return "Detail";
            case COL_TECH_BASE:
                return "Tech Base";
            case COL_REPAIR:
                return "Repair Details";
            default:
                return "?";
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        Part part;
        if (data.isEmpty()) {
            return "";
        } else {
            part = (Part) data.get(row);
        }

        if (col == COL_NAME) {
            String openBrace = "";
            String closeBrace = "";

            if (part.isBrandNew()) {
                openBrace = "<i>";
                closeBrace = "</i>";
            }

            return "<html><nobr>" + openBrace + part.getName() + closeBrace + "</nobr></html>";
        }
        if (col == COL_DETAIL) {
            return "<html><nobr>" + part.getDetails() + "</nobr></html>";
        }
        if (col == COL_COST) {
            return part.getActualValue().toAmountAndSymbolString();
        }
        if (col == COL_TOTAL_COST) {
            return part.getActualValue().multipliedBy(part.getQuantity()).toAmountAndSymbolString();
        }
        if (col == COL_QUANTITY) {
            return part.getQuantity();
        }
        if (col == COL_QUALITY) {
            return part.getQualityName();
        }
        if (col == COL_TON) {
            return Math.round(part.getTonnage() * 100) / 100.0;
        }
        if (col == COL_STATUS) {
            return "<html><nobr>" + part.getStatus() + "</nobr></html>";
        }
        if (col == COL_TECH_BASE) {
            return part.getTechBaseName();
        }
        if (col == COL_REPAIR) {
            return "<html><nobr>" + part.getRepairDesc() + "</nobr></html>";
        }
        return "?";
    }

    public Part getPartAt(int row) {
        return ((Part) data.get(row));
    }

    public int getColumnWidth(int c) {
        switch (c) {
            case COL_NAME:
            case COL_DETAIL:
                return 120;
            case COL_REPAIR:
                return 140;
            case COL_STATUS:
                return 40;
            case COL_TECH_BASE:
            case COL_COST:
            case COL_TOTAL_COST:
                return 20;
            default:
                return 3;
        }
    }

    public int getAlignment(int col) {
        switch (col) {
            case COL_QUALITY:
                return SwingConstants.CENTER;
            case COL_COST:
            case COL_TOTAL_COST:
            case COL_TON:
                return SwingConstants.RIGHT;
            default:
                return SwingConstants.LEFT;
        }
    }

    public @Nullable String getTooltip(int row, int col) {
        switch (col) {
            default:
                return null;
        }
    }

    public PartsTableModel.Renderer getRenderer() {
        return new PartsTableModel.Renderer();
    }

    public class Renderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setOpaque(true);
            int actualCol = table.convertColumnIndexToModel(column);
            int actualRow = table.convertRowIndexToModel(row);
            setHorizontalAlignment(getAlignment(actualCol));
            setToolTipText(getTooltip(actualRow, actualCol));

            return this;
        }
    }
}
