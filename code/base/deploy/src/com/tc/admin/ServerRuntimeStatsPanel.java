/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright notice.  All rights reserved.
 */
package com.tc.admin;

import org.dijon.Container;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;

import com.tc.admin.dso.RuntimeStatsPanel;
import com.tc.management.beans.TCServerInfoMBean;
import com.tc.statistics.StatisticData;
import com.tc.stats.DSOMBean;
import com.tc.stats.statistics.CountStatistic;
import com.tc.stats.statistics.Statistic;

import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.border.TitledBorder;

public class ServerRuntimeStatsPanel extends RuntimeStatsPanel {
  private ServerPanel             m_serverPanel;

  private ChartPanel              m_memoryPanel;
  private TimeSeries[]            m_memoryTimeSeries;
  private JFreeChart              m_memoryChart;

  private ChartPanel              m_cpuPanel;
  private TimeSeries[]            m_cpuTimeSeries;
  private JFreeChart              m_cpuChart;
  private Map<String, TimeSeries> m_cpuTimeSeriesMap;

  private ChartPanel              m_flushRatePanel;
  private TimeSeries              m_flushRateSeries;
  private JFreeChart              m_flushRateChart;

  private ChartPanel              m_faultRatePanel;
  private TimeSeries              m_faultRateSeries;
  private JFreeChart              m_faultRateChart;

  private ChartPanel              m_txnRatePanel;
  private TimeSeries              m_txnRateSeries;
  private JFreeChart              m_txnRateChart;

  private ChartPanel              m_cacheMissRatePanel;
  private TimeSeries              m_cacheMissRateSeries;
  private JFreeChart              m_cacheMissRateChart;

  private static final String[]   STATS = { "ObjectFlushRate", "ObjectFaultRate", "TransactionRate", "CacheMissRate" };

  public ServerRuntimeStatsPanel() {
    super();
  }

  void setServerPanel(ServerPanel serverPanel) {
    m_serverPanel = serverPanel;
  }

  protected void setup(Container chartsPanel) {
    chartsPanel.setLayout(new GridLayout(0, 2));
    setupMemoryPanel(chartsPanel);
    setupCpuPanel(chartsPanel);
    setupTxnRatePanel(chartsPanel);
    setupCacheMissRatePanel(chartsPanel);
    setupFlushRatePanel(chartsPanel);
    setupFaultRatePanel(chartsPanel);
  }

  private void setupFlushRatePanel(Container parent) {
    m_flushRateSeries = createTimeSeries("");
    m_flushRateChart = createChart(m_flushRateSeries);
    m_flushRatePanel = new ChartPanel(m_flushRateChart, false);
    parent.add(m_flushRatePanel);
    m_flushRatePanel.setPreferredSize(fDefaultGraphSize);
    m_flushRatePanel.setBorder(new TitledBorder("Object Disk Flush Rate"));
  }

  private void setupFaultRatePanel(Container parent) {
    m_faultRateSeries = createTimeSeries("");
    m_faultRateChart = createChart(m_faultRateSeries);
    m_faultRatePanel = new ChartPanel(m_faultRateChart, false);
    parent.add(m_faultRatePanel);
    m_faultRatePanel.setPreferredSize(fDefaultGraphSize);
    m_faultRatePanel.setBorder(new TitledBorder("Object Disk Fault Rate"));
  }

  private void setupTxnRatePanel(Container parent) {
    m_txnRateSeries = createTimeSeries("");
    m_txnRateChart = createChart(m_txnRateSeries);
    m_txnRatePanel = new ChartPanel(m_txnRateChart, false);
    parent.add(m_txnRatePanel);
    m_txnRatePanel.setPreferredSize(fDefaultGraphSize);
    m_txnRatePanel.setBorder(new TitledBorder("Transaction Rate"));
  }

  private void setupCacheMissRatePanel(Container parent) {
    m_cacheMissRateSeries = createTimeSeries("");
    m_cacheMissRateChart = createChart(m_cacheMissRateSeries);
    m_cacheMissRatePanel = new ChartPanel(m_cacheMissRateChart, false);
    parent.add(m_cacheMissRatePanel);
    m_cacheMissRatePanel.setPreferredSize(fDefaultGraphSize);
    m_cacheMissRatePanel.setBorder(new TitledBorder("Cache Miss Rate"));
  }

  private void setupMemoryPanel(Container parent) {
    m_memoryTimeSeries = new TimeSeries[2];
    m_memoryTimeSeries[0] = createTimeSeries("memory max");
    m_memoryTimeSeries[1] = createTimeSeries("memory used");
    m_memoryChart = createChart(m_memoryTimeSeries);
    XYPlot plot = (XYPlot) m_memoryChart.getPlot();
    NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
    numberAxis.setAutoRangeIncludesZero(true);
    DecimalFormat formatter = new DecimalFormat("0M");
    numberAxis.setNumberFormatOverride(formatter);
    m_memoryPanel = new ChartPanel(m_memoryChart, false);
    parent.add(m_memoryPanel);
    m_memoryPanel.setPreferredSize(fDefaultGraphSize);
    m_memoryPanel.setBorder(new TitledBorder("Memory"));
  }

  private void setupCpuSeries(int processorCount) {
    m_cpuTimeSeriesMap = new HashMap<String, TimeSeries>();
    m_cpuTimeSeries = new TimeSeries[processorCount];
    for (int i = 0; i < processorCount; i++) {
      String cpuName = "cpu " + i;
      m_cpuTimeSeriesMap.put(cpuName, m_cpuTimeSeries[i] = createTimeSeries(cpuName));
    }
    m_cpuChart = createChart(m_cpuTimeSeries);
    XYPlot plot = (XYPlot) m_cpuChart.getPlot();
    NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
    numberAxis.setRange(0.0, 1.0);
    m_cpuPanel.setChart(m_cpuChart);
  }

  private void setupCpuPanel(Container parent) {
    m_cpuPanel = new ChartPanel(null, false);
    parent.add(m_cpuPanel);
    m_cpuPanel.setPreferredSize(fDefaultGraphSize);
    m_cpuPanel.setBorder(new TitledBorder("CPU"));
  }

  protected void retrieveStatistics() {
    try {
      TCServerInfoMBean tcServerInfoBean = m_serverPanel.getServerInfoBean();
      if (tcServerInfoBean != null) {
        Map statMap = tcServerInfoBean.getStatistics();

        m_memoryTimeSeries[0].addOrUpdate(new Second(), ((Number) statMap.get("memory max")).longValue() / 1024000d);
        m_memoryTimeSeries[1].addOrUpdate(new Second(), ((Number) statMap.get("memory used")).longValue() / 1024000d);

        if (m_cpuPanel != null) {
          StatisticData[] cpuUsageData = (StatisticData[]) statMap.get("cpu usage");
          if (cpuUsageData != null) {
            if (m_cpuTimeSeries == null) {
              setupCpuSeries(cpuUsageData.length);
            }
            for (int i = 0; i < cpuUsageData.length; i++) {
              StatisticData cpuData = cpuUsageData[i];
              String cpuName = cpuData.getElement();
              TimeSeries timeSeries = m_cpuTimeSeriesMap.get(cpuName);
              if (timeSeries != null) {
                timeSeries.addOrUpdate(new Second(), ((Number) cpuData.getData()).doubleValue());
              }
            }
          } else {
            // Sigar must not be available; hide cpu panel
            m_chartsPanel.remove(m_cpuPanel);
            m_chartsPanel.revalidate();
            m_chartsPanel.repaint();
            m_cpuPanel = null;
            m_cpuChart = null;
          }
        }
      }

      DSOMBean dsoBean = m_serverPanel.getDSOBean();
      if (dsoBean != null) {
        Statistic[] stats = dsoBean.getStatistics(STATS);
        updateSeries(m_flushRateSeries, (CountStatistic) stats[0]);
        updateSeries(m_faultRateSeries, (CountStatistic) stats[1]);
        updateSeries(m_txnRateSeries, (CountStatistic) stats[2]);
        updateSeries(m_cacheMissRateSeries, (CountStatistic) stats[3]);
      }
    } catch (Exception e) {/**/
    }
  }
}
