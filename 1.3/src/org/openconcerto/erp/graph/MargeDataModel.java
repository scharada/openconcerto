/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 OpenConcerto, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU General Public License Version 3
 * only ("GPL"). You may not use this file except in compliance with the License. You can obtain a
 * copy of the License at http://www.gnu.org/licenses/gpl-3.0.html See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 */
 
 package org.openconcerto.erp.graph;

import org.openconcerto.sql.Configuration;
import org.openconcerto.sql.element.SQLElementDirectory;
import org.openconcerto.sql.model.SQLSelect;
import org.openconcerto.sql.model.SQLTable;
import org.openconcerto.sql.model.Where;
import org.openconcerto.utils.DecimalUtils;
import org.openconcerto.utils.GestionDevise;
import org.openconcerto.utils.NumberUtils;
import org.openconcerto.utils.RTInterruptedException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

import org.jopenchart.DataModel1D;
import org.jopenchart.barchart.VerticalBarChart;

public class MargeDataModel extends DataModel1D {

    private VerticalBarChart chart;
    private Thread thread;
    private int year;

    public MargeDataModel(final VerticalBarChart chart, final int year) {
        this.chart = chart;
        loadYear(year);
    }

    @Override
    public int getSize() {
        return 12;
    }

    public synchronized void loadYear(Object value) {
        if (!(value instanceof Number)) {
            return;
        }
        if (thread != null) {
            thread.interrupt();
        }
        year = ((Number) value).intValue();

        thread = new Thread() {
            @Override
            public void run() {
                setState(LOADING);
                // Clear
                MargeDataModel.this.clear();
                fireDataModelChanged();
                try {
                    for (int i = 0; i < 12; i++) {
                        if (isInterrupted()) {
                            break;
                        }
                        Calendar c = Calendar.getInstance();
                        c.set(year, i, 1);
                        Date d1 = new Date(c.getTimeInMillis());
                        c.set(Calendar.DATE, c.getActualMaximum(Calendar.DATE));
                        Date d2 = new Date(c.getTimeInMillis());
                        Thread.yield();

                        final SQLElementDirectory directory = Configuration.getInstance().getDirectory();
                        SQLTable tableSaisieVenteF = directory.getElement("SAISIE_VENTE_FACTURE").getTable();
                        SQLTable tableSaisieVenteFElt = directory.getElement("SAISIE_VENTE_FACTURE_ELEMENT").getTable();
                        final SQLSelect sel = new SQLSelect(tableSaisieVenteF.getBase());
                        sel.addSelect(tableSaisieVenteFElt.getField("T_PA_HT"), "SUM");
                        sel.addSelect(tableSaisieVenteFElt.getField("T_PV_HT"), "SUM");
                        final Where w = new Where(tableSaisieVenteF.getField("DATE"), d1, d2);
                        final Where w2 = new Where(tableSaisieVenteFElt.getField("ID_SAISIE_VENTE_FACTURE"), "=", tableSaisieVenteF.getKey());
                        sel.setWhere(w.and(w2));

                        BigDecimal total = BigDecimal.ZERO;
                        Object[] o = tableSaisieVenteF.getBase().getDataSource().executeA1(sel.asString());
                        if (o != null) {
                            BigDecimal pa = (BigDecimal) o[0];
                            BigDecimal pv = (BigDecimal) o[1];
                            if (pa != null && pv != null && (!NumberUtils.areNumericallyEqual(pa, BigDecimal.ZERO) || !NumberUtils.areNumericallyEqual(pv, BigDecimal.ZERO))) {
                                total = pv.subtract(pa);
                            }
                        }

                        final double value = total.doubleValue();

                        if (value > chart.getHigherRange().doubleValue()) {

                            // String currencyToString = GestionDevise.currencyToString(euros * 100,
                            // true);
                            chart.getLeftAxis().getLabels().get(2).setLabel(total.setScale(0, RoundingMode.HALF_UP).toString() + " €");

                            chart.getLeftAxis().getLabels().get(1).setLabel(total.divide(new BigDecimal(2), MathContext.DECIMAL128).setScale(0, RoundingMode.HALF_UP) + " €");
                            chart.setHigherRange(value);
                        }
                        if (((int) value) != 0) {
                            MargeDataModel.this.setValueAt(i, value);
                            fireDataModelChanged();
                            Thread.sleep(20);
                        }

                    }
                    if (!isInterrupted()) {
                        setState(LOADED);
                        fireDataModelChanged();
                    }
                } catch (InterruptedException e) {
                    // Thread stopped because of year changed
                } catch (RTInterruptedException e) {
                    // Thread stopped because of year changed
                }

            }
        };

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

    }

    public int getYear() {
        return year;
    }
}
