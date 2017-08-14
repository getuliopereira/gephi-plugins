package br.ufu.facom.ppgco.ia.gephi.plugin.metric;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author getulio
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class BridgingCentralityMetricBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(BridgingCentralityMetricBuilder.class, "BridgingCentralityMetricBuilder.name");
    }

    public Statistics getStatistics() {
        return new BridgingCentralityMetric();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return BridgingCentralityMetric.class;
    }
}
